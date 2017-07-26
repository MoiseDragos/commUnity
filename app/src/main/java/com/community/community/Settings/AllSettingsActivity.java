package com.community.community.Settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.community.community.General.UsefulThings;
import com.community.community.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class AllSettingsActivity extends AppCompatActivity implements View.OnClickListener {

    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    private SharedPreferences sharedPreferences = null;
    private SharedPreferences.Editor editor = null;

    private DatabaseReference mDatabase = null;
    private PercentRelativeLayout changePasswordLayout = null;
    private PercentRelativeLayout deleteAccountLayout = null;

    private LinearLayout userLayout = null;

    private Switch switchMyCauses = null;
    private Switch switchMySupportedCauses = null;
    private Switch switchAge = null;
    private Switch switchAddress = null;
    private Switch switchDescription = null;
    private Switch switchPassword = null;
    private Switch switchDeleteAccount = null;

    private EditText oldPassword = null;
    private EditText newPassword = null;
    private EditText confirmNewPassword = null;
    private EditText deletePassword = null;

    private String email = null;
    private String type = null;
    private String uid = null;
    private String nickname = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        switchMyCauses = (Switch) findViewById(R.id.switchMyCauses);
        switchMySupportedCauses = (Switch) findViewById(R.id.switchMySupportedCauses);
        switchAge = (Switch) findViewById(R.id.switchAge);
        switchAddress = (Switch) findViewById(R.id.switchAddress);
        switchDescription = (Switch) findViewById(R.id.switchDescription);
        switchPassword = (Switch) findViewById(R.id.switchPassword);
        switchDeleteAccount = (Switch) findViewById(R.id.switchDeleteAccount);

        changePasswordLayout = (PercentRelativeLayout) findViewById(R.id.changePasswordLayout);
        deleteAccountLayout = (PercentRelativeLayout) findViewById(R.id.deleteAccountLayout);

        userLayout = (LinearLayout) findViewById(R.id.userLayout);

        oldPassword = (EditText) findViewById(R.id.oldPassword);
        newPassword = (EditText) findViewById(R.id.newPassword);
        confirmNewPassword = (EditText) findViewById(R.id.confirmNewPassword);
        deletePassword = (EditText) findViewById(R.id.password);

        Button submit_password = (Button) findViewById(R.id.submit_password);
        submit_password.setOnClickListener(this);
        Button cancel_password = (Button) findViewById(R.id.cancel_password);
        cancel_password.setOnClickListener(this);
        Button submit_delete = (Button) findViewById(R.id.submit_delete);
        submit_delete.setOnClickListener(this);
        Button cancel_delete = (Button) findViewById(R.id.cancel_delete);
        cancel_delete.setOnClickListener(this);

        Intent intent = getIntent();
        if(intent != null){
            email = intent.getStringExtra("email");
            type = intent.getStringExtra("type");
            uid = intent.getStringExtra("uid");
            nickname = intent.getStringExtra("nickname");

            if(type.equals("ngo")){
                setNgoDetails();
            } else {
                setUserDetails();
            }
        }

        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        Log.d(LOG, sharedPreferences.getAll().toString());
        onCheckedListeners();
        setUpSharedPreferences();

    }

    private void setUpSharedPreferences() {

        if(sharedPreferences.getAll().isEmpty()) {
            updateSharedPreferencesFromFirebase();
        } else {
            boolean res, ref = false;
            res = sharedPreferences.getBoolean(UsefulThings.MY_CAUSES, ref);
            switchMyCauses.setChecked(res);

            res = sharedPreferences.getBoolean(UsefulThings.MY_SUPPORTED_CAUSES, ref);
            switchMySupportedCauses.setChecked(res);

            res = sharedPreferences.getBoolean(UsefulThings.MY_AGE, ref);
            switchAge.setChecked(res);

            res = sharedPreferences.getBoolean(UsefulThings.MY_ADDRESS, ref);
            switchAddress.setChecked(res);

            res = sharedPreferences.getBoolean(UsefulThings.MY_DESCRIPTION, ref);
            switchDescription.setChecked(res);
        }
    }

    private void updateSharedPreferencesFromFirebase() {

        final DatabaseReference ref = mDatabase.child("users").child(uid);
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("GeneralSettings")) {
                            DatabaseReference dRef = ref.child("GeneralSettings");
                            dRef.addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            Map<String, Object> map = (Map<String,Object>) dataSnapshot.getValue();

                                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                                String str = entry.getKey();
                                                switch (str) {
                                                    case UsefulThings.MY_CAUSES:
                                                        switchMyCauses.setChecked((boolean) entry.getValue());
                                                        break;
                                                    case UsefulThings.MY_SUPPORTED_CAUSES:
                                                        switchMySupportedCauses.setChecked((boolean) entry.getValue());
                                                        break;
                                                    case UsefulThings.MY_AGE:
                                                        switchAge.setChecked((boolean) entry.getValue());
                                                        break;
                                                    case UsefulThings.MY_ADDRESS:
                                                        switchAddress.setChecked((boolean) entry.getValue());
                                                        break;
                                                    case UsefulThings.MY_DESCRIPTION:
                                                        switchDescription.setChecked((boolean) entry.getValue());
                                                    default:
                                                        break;
                                                }
                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            email = savedInstanceState.getString("email");
            type = savedInstanceState.getString("type");
            uid = savedInstanceState.getString("uid");
            nickname = savedInstanceState.getString("nickname");

            if(type.equals("ngo")){
                setNgoDetails();
            } else {
                setUserDetails();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("email", email);
        outState.putString("type", type);
        outState.putString("uid", uid);
        outState.putString("nickname", nickname);
    }

    private void setUserDetails() {
        userLayout.setVisibility(View.VISIBLE);

        Button applyBtn = (Button) findViewById(R.id.applyBtn);
        applyBtn.setOnClickListener(this);

        Button cancelBtn = (Button) findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(this);
    }

    private void setNgoDetails() {
        userLayout.setVisibility(View.GONE);
    }

    private void onCheckedListeners() {

        editor = sharedPreferences.edit();
        switchMyCauses.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(UsefulThings.MY_CAUSES, isChecked).apply();
            }
        });

        switchMySupportedCauses.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(UsefulThings.MY_SUPPORTED_CAUSES, isChecked).apply();
            }
        });

        switchAge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(UsefulThings.MY_AGE, isChecked).apply();
            }
        });

        switchAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(UsefulThings.MY_ADDRESS, isChecked).apply();
            }
        });

        switchDescription.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(UsefulThings.MY_DESCRIPTION, isChecked).apply();
            }
        });

        switchPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    changePasswordLayout.setVisibility(View.VISIBLE);
                } else {
                    changePasswordLayout.setVisibility(View.GONE);
                }
            }
        });

        switchDeleteAccount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    deleteAccountLayout.setVisibility(View.VISIBLE);
                } else {
                    deleteAccountLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.submit_password:
                if(passwordEmptyFields()){
                    verifyOldPassword(oldPassword.getText().toString(),
                            newPassword.getText().toString(),
                            true);
                }
                break;

            case R.id.cancel_password:
                cancelPassword();
                break;

            case R.id.cancel_delete:
                if(deletePassword == null || deletePassword.getText().length() < 6){
                    Toast.makeText(getApplicationContext(), "Parola veche este prea scurtă", Toast.LENGTH_SHORT).show();
                } else {
                    verifyOldPassword(deletePassword.getText().toString(),
                            newPassword.getText().toString(),
                            false);
                }
                break;

            case R.id.submit_delete:
                cancelAccount();
                break;

            case R.id.applyBtn:
                applyForNGO();
                break;

            case R.id.cancelBtn:
                cancelForNGO();
                break;

            default:
                break;
        }
    }

    private void cancelForNGO(){

        final DatabaseReference[] ref = {mDatabase.child("applyForNgo")};
        ref[0].addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.hasChild(uid)){
                            Toast.makeText(getApplicationContext(), "Nu ați aplicat încă", Toast.LENGTH_SHORT).show();
                        } else {
                            ref[0].child(uid).removeValue().addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(LOG, e.getLocalizedMessage());
                                }
                            });
                            Toast.makeText(getApplicationContext(), "Aplicația a fost anulată", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void applyForNGO(){

        final DatabaseReference[] ref = {mDatabase.child("applyForNgo")};
        ref[0].addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(uid)){
                            Toast.makeText(getApplicationContext(), "Ați aplicat mai demult", Toast.LENGTH_SHORT).show();
                        } else {
                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                            ref[0] = ref[0].child(uid);
                            ref[0].setValue(dateFormat.format(new Date())).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(LOG, e.getLocalizedMessage());
                                }
                            });
                            Toast.makeText(getApplicationContext(), "Ați aplicat cu succes", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private boolean passwordEmptyFields() {

        if(oldPassword == null || oldPassword.getText().length() < 6){
            Toast.makeText(getApplicationContext(), "Parola veche este prea scurtă", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(newPassword == null || newPassword.getText().length() < 6){
            Toast.makeText(getApplicationContext(), "Parola nouă este prea scurtă", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(confirmNewPassword == null || confirmNewPassword.getText().length() < 6){
            Toast.makeText(getApplicationContext(), "Confirmarea parolei este prea scurtă", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(newPassword.getText().length() < 6 || confirmNewPassword.getText().length() < 6){
            Toast.makeText(getApplicationContext(), "Parolă prea scurtă", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!confirmNewPassword.getText().toString().equals(newPassword.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Parolele introduse sunt diferite", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void verifyOldPassword(String oldPassword, final String newPassword, final boolean updatePassword) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if(updatePassword){
                                user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Parola a fost schimbată", Toast.LENGTH_SHORT).show();
                                            cancelPassword();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Parola nu a putut fi schimbată", Toast.LENGTH_SHORT).show();
                                            cancelPassword();
                                        }
                                    }
                                });
                            } else {
                                //TODO: delete user / disable ONG
                                Toast.makeText(getApplicationContext(), "Te voi șterge", Toast.LENGTH_SHORT).show();
                                if(type.equals("ngo")){
                                    mDatabase.child("users").child(uid).child("ProfileSettings").child("status").setValue("inactive");
                                    FirebaseAuth.getInstance().signOut();
                                } else {
                                    removeUser();
                                }
                                cancelAccount();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Parolă greșită", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void removeUser() {
        final DatabaseReference[] ref = {mDatabase.child("users").child(uid).child("MyCauses")};
        ref[0].addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        /* Update MyCauses */
                        if(dataSnapshot.getValue() != null){
                            Map<String, Object> map = (Map<String,Object>) dataSnapshot.getValue();

                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                String causeUid = entry.getKey();

                                mDatabase.child("causes").child(causeUid).child("Info").child("owner").setValue("-");
                                mDatabase.child("causes").child(causeUid).child("Info").child("ownerUID").setValue("-");
                                mDatabase.child("orphanCauses").child(causeUid).setValue("-");
                            }
                        }

                        /* Update SupportedCauses */
                        ref[0] = mDatabase.child("users").child(uid).child("Supporting");
                        ref[0].addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.getValue() != null) {
                                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                                final String causeUid = entry.getKey();

                                                final DatabaseReference dRef = mDatabase.child("causes").child(causeUid).child("SupportedBy").child("number");
                                                dRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        long number = (long) dataSnapshot.getValue();
                                                        dRef.setValue(--number);
                                                        mDatabase.child("causes").child(causeUid).child("SupportedBy").child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                /* Nicknames */
                                                                mDatabase.child("nicknames").child(nickname.replace(".", "-")).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                     @Override
                                                                     public void onSuccess(Void aVoid) {
                                                                         mDatabase.child("users").child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                             @Override
                                                                             public void onSuccess(Void aVoid) {
                                                                                 Toast.makeText(getApplicationContext(), "User-ul a fost șters cu success", Toast.LENGTH_SHORT).show();
                                                                                 FirebaseAuth.getInstance().getCurrentUser().delete();
                                                                             }
                                                                         });
                                                                     }
                                                                 });
                                                                // TODO: reomve profile image
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });
    }

    private void cancelPassword() {
        switchPassword.setChecked(false);
        oldPassword.setText("");
        newPassword.setText("");
        confirmNewPassword.setText("");
        changePasswordLayout.setVisibility(View.GONE);
    }

    private void cancelAccount() {
        switchDeleteAccount.setChecked(false);
        deletePassword.setText("");
        deleteAccountLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {

        mDatabase.child("users").child(uid).child("GeneralSettings").setValue(sharedPreferences.getAll()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Preferintele nu au putut fi salvate", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

}
