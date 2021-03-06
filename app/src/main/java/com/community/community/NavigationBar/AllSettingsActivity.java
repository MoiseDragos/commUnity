package com.community.community.NavigationBar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.community.community.BeforeLogin.LoginActivity;
import com.community.community.General.UsefulThings;
import com.community.community.General.User;
import com.community.community.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class AllSettingsActivity extends AppCompatActivity implements View.OnClickListener {

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

        if(UsefulThings.currentUser == null) {
            UsefulThings.currentUser = (User) savedInstanceState.getSerializable("userDetails");
        }

        if(UsefulThings.currentUser.getType().equals("ngo")){
            setNgoDetails();
        } else {
            setUserDetails();
        }

        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        Log.d(LOG, sharedPreferences.getAll().toString());
        onCheckedListeners();
        setUpSharedPreferences();

        Button backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                onBackPressed();
            }

        });

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

        final DatabaseReference ref = mDatabase.child("users").child(UsefulThings.currentUser.getUid());
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

                                            Map<String, Object> map =
                                                    (Map<String,Object>) dataSnapshot.getValue();

                                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                                String str = entry.getKey();
                                                switch (str) {
                                                    case UsefulThings.MY_CAUSES:
                                                        switchMyCauses.setChecked(
                                                                (boolean) entry.getValue());
                                                        break;
                                                    case UsefulThings.MY_SUPPORTED_CAUSES:
                                                        switchMySupportedCauses.setChecked(
                                                                (boolean) entry.getValue());
                                                        break;
                                                    case UsefulThings.MY_AGE:
                                                        switchAge.setChecked(
                                                                (boolean) entry.getValue());
                                                        break;
                                                    case UsefulThings.MY_ADDRESS:
                                                        switchAddress.setChecked(
                                                                (boolean) entry.getValue());
                                                        break;
                                                    case UsefulThings.MY_DESCRIPTION:
                                                        switchDescription.setChecked(
                                                                (boolean) entry.getValue());
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("userDetails", UsefulThings.currentUser);
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
                    Toast.makeText(getApplicationContext(),
                            "Parola veche este prea scurtă",
                            Toast.LENGTH_SHORT).show();
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
                        if(!dataSnapshot.hasChild(UsefulThings.currentUser.getUid())){
                            Toast.makeText(getApplicationContext(),
                                    "Nu ați aplicat încă",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                            String date = (String) map.get(UsefulThings.currentUser.getUid());

                            String[] parts = date.split("~");
                            if(parts.length == 1) {
                                ref[0].child(UsefulThings.currentUser.getUid())
                                        .removeValue().addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(LOG, e.getLocalizedMessage());
                                    }
                                });
                                Toast.makeText(getApplicationContext(),
                                        "Cererea a fost anulată",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                String text = " Cererea a fost refuzată.\nNe puteți contacta " +
                                        "pe emai pentru mai multe informații";
                                Spannable centeredText = new SpannableString(text);
                                centeredText.setSpan(new AlignmentSpan.Standard(
                                        Layout.Alignment.ALIGN_CENTER), 0, text.length() - 1,
                                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                                Toast.makeText(getApplicationContext(), centeredText,
                                        Toast.LENGTH_LONG).show();
                            }
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
                        if(dataSnapshot.hasChild(UsefulThings.currentUser.getUid())){
                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                            String date = (String) map.get(UsefulThings.currentUser.getUid());

                            String[] parts = date.split("~");
                            if(parts.length == 1) {
                                Toast.makeText(getApplicationContext(),
                                        "Ați aplicat mai demult",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                String text = " Cererea a fost refuzată.\nNe puteți contacta " +
                                        "pe emai pentru mai multe informații";
                                Spannable centeredText = new SpannableString(text);
                                centeredText.setSpan(new AlignmentSpan.Standard(
                                                Layout.Alignment.ALIGN_CENTER), 0, text.length() - 1,
                                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                                Toast.makeText(getApplicationContext(), centeredText,
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                            ref[0] = ref[0].child(UsefulThings.currentUser.getUid());
                            ref[0].setValue(dateFormat.format(new Date()))
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(LOG, e.getLocalizedMessage());
                                        }
                                    });
                            Toast.makeText(getApplicationContext(),
                                    "Ați aplicat cu succes",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private boolean passwordEmptyFields() {

        if(oldPassword == null || oldPassword.getText().length() < 6){
            Toast.makeText(getApplicationContext(),
                    "Parola veche este prea scurtă",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if(newPassword == null || newPassword.getText().length() < 6){
            Toast.makeText(getApplicationContext(),
                    "Parola nouă este prea scurtă",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if(confirmNewPassword == null || confirmNewPassword.getText().length() < 6){
            Toast.makeText(getApplicationContext(),
                    "Confirmarea parolei este prea scurtă",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if(newPassword.getText().length() < 6 || confirmNewPassword.getText().length() < 6){
            Toast.makeText(getApplicationContext(),
                    "Parolă prea scurtă",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!confirmNewPassword.getText().toString().equals(newPassword.getText().toString())) {
            Toast.makeText(getApplicationContext(),
                    "Parolele introduse sunt diferite",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void verifyOldPassword(String oldPassword,
                                   final String newPassword,
                                   final boolean updatePassword) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Log.d(LOG, "Email: " + user.getEmail());
        Log.d(LOG, "Password: " + oldPassword);

        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), oldPassword);

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if(updatePassword){
                                user.updatePassword(newPassword)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(),
                                                    "Parola a fost schimbată",
                                                    Toast.LENGTH_SHORT).show();
                                            cancelPassword();
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    "Parola nu a putut fi schimbată",
                                                    Toast.LENGTH_SHORT).show();
                                            cancelPassword();
                                        }
                                    }
                                });
                            } else {
//                                Toast.makeText(getApplicationContext(),
//                                        "Te voi șterge",
//                                        Toast.LENGTH_SHORT).show();
                                if(UsefulThings.currentUser.getType().equals("ngo")){
                                    mDatabase.child("users")
                                            .child(UsefulThings.currentUser.getUid())
                                            .child("ProfileSettings").child("status")
                                            .setValue("inactive");

                                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                    mDatabase.child("inactiveNGO")
                                            .child(UsefulThings.currentUser.getUid())
                                            .setValue(dateFormat.format(new Date()));

                                    logOut();
//                                    FirebaseAuth.getInstance().signOut();
                                } else {
                                    removeUser();
                                }
                                cancelAccount();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Parolă greșită",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void removeUser() {

        Log.d(LOG, "0");

        String uid = UsefulThings.currentUser.getUid();
        String email = UsefulThings.currentUser.getEmail();
        String nickname = UsefulThings.currentUser.getNickname();

        int ownCauses = UsefulThings.currentUser.getOwnCausesNumber();

        Log.d(LOG, "1");
        updateProposals("ProposalsMade", "ProposalsReceived", uid);
        updateProposals("ProposalsReceived", "ProposalsMade", uid);
        updateProposals("ProposalsMadeRejected", "ProposalsReceivedRejected", uid);
        updateProposals("ProposalsReceivedRejected", "ProposalsMadeRejected", uid);
        updateProposals("SupporterOf", "Supporters", uid);

        Log.d(LOG, "2");
        updateNicknames(nickname);

        Log.d(LOG, "3");
        updateMemberOf(uid, ownCauses);

        Log.d(LOG, "updateCauses00: " + uid);
        updateCauses(uid);

        Log.d(LOG, "4");
        removeProfileImage(uid);
        Log.d(LOG, "5");
        removeLocalImage(email);
        Log.d(LOG, "6");
        removeAccount(uid);
        Log.d(LOG, "7");
        logOut();

    }

    private void updateNicknames(String nickname) {
        mDatabase.child("nicknames").child(nickname.replace(".", "-")).removeValue();
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        UsefulThings.CAUSE_CACHES = null;
        UsefulThings.currentUser = null;
        startActivity(new Intent(getApplicationContext(),
                LoginActivity.class));
        finish();
    }

    private void removeLocalImage(String email) {
        String dir = getFilesDir().getAbsolutePath();
        File f0 = new File(dir, "myImage_" + email);
        boolean d0 = f0.delete();
        Log.d(LOG, "File deleted: " + dir + "myImage_" + email + d0);
    }

    private void removeProfileImage(String uid) {
        FirebaseStorage.getInstance().getReference().child(UsefulThings.FB_STORAGE_USERS_PATH +
                uid + "/" + UsefulThings.currentUser.getImageName())
                .delete();
        FirebaseAuth.getInstance().getCurrentUser().delete();
    }

    private void removeAccount(String uid) {
        mDatabase.child("users").child(uid).removeValue();
    }

    private void updateCauses(final String uid) {
        Log.d(LOG, "updateCauses");

        final DatabaseReference ref = mDatabase.child("users")
                .child(uid).child("MyCauses");
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            Map<String, Object> map =
                                    (Map<String, Object>) dataSnapshot.getValue();

                            Log.d(LOG, "map: " + map);

                            int length = map.size();

                            Log.d(LOG, "map: " + length);
                            int i = 0;
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                i++;
                                updateCauses(entry.getKey());
                                addOrphan(entry.getKey(), entry.getValue());
                                removeCause(entry.getKey(), length, i, ref);
                            }
                        }
                    }

                    private void removeCause(String key, int length, int i, DatabaseReference ref) {
                        Log.d(LOG, "Key: " + key);
                        mDatabase.child("causes").child(key).removeValue();
                        if(length == i) {
                            ref.removeValue();
                        }
                    }

                    private void addOrphan(String key, Object value) {
                        mDatabase.child("orphanCauses").child(key).setValue(value);
                    }

                    private void updateCauses(final String key) {
                        final DatabaseReference ref = mDatabase.child("causes")
                                .child(key).child("SupportedBy");
                        ref.addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Map<String, Object> map =
                                                (Map<String, Object>) dataSnapshot.getValue();
                                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                                            if(!entry.getKey().equals("number")) {
                                                updateUser(entry.getKey());
                                                ref.child(entry.getKey()).setValue("-");
                                            }
                                        }
                                    }

                                    private void updateUser(String key) {
                                        mDatabase.child("users").child(key)
                                                .child("Supporting").setValue("-");
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void updateMemberOf(final String uid, final int ownCauses) {

        final DatabaseReference ref = mDatabase.child("users")
                .child(uid).child("MemberOf");
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            Map<String, Object> map =
                                    (Map<String, Object>) dataSnapshot.getValue();
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                Log.d(LOG, "Key: " + entry.getKey());
                                updateCausesNumber(entry.getKey());
                                removeMember(entry.getKey());
                            }
                            ref.removeValue();
                        }
                    }

                    private void removeMember(String key) {
                        mDatabase.child("users").child(key).child("Members")
                                .child(uid).removeValue();
                    }

                    private void updateCausesNumber(String key) {
                        final DatabaseReference ref = mDatabase.child("users")
                                .child(key).child("ProfileSettings").child("membersCauses");
                        ref.addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot != null){
                                            long members = (long) dataSnapshot.getValue();
                                            ref.setValue(members - ownCauses);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void updateProposals(final String child1, final String child2, final String uid) {

        final DatabaseReference ref = mDatabase.child("users")
                .child(uid).child(child1);
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null) {
                            Map <String, Object> map =
                                    (Map <String, Object>) dataSnapshot.getValue();
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                mDatabase.child("users").child(entry.getKey()).
                                        child(child2).child(uid)
                                        .removeValue();

                            }
                            ref.removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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

        mDatabase.child("users")
                .child(UsefulThings.currentUser.getUid())
                .child("GeneralSettings").setValue(sharedPreferences.getAll())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Preferintele nu au putut fi salvate",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(UsefulThings.mNetworkStateIntentReceiver == null ||
                UsefulThings.mNetworkStateChangedFilter == null) {
            UsefulThings.initNetworkListener();
        }
        registerReceiver(UsefulThings.mNetworkStateIntentReceiver,
                UsefulThings.mNetworkStateChangedFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(UsefulThings.mNetworkStateIntentReceiver);
    }

}
