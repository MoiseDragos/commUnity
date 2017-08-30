package com.community.community.BeforeLogin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.community.community.AdminActivity;
import com.community.community.General.BackPressedActivity;
import com.community.community.General.UsefulThings;
import com.community.community.MainActivity;
import com.community.community.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private String LOG = this.getClass().getSimpleName();

    private EditText mEmailField = null;
    private EditText mPasswordField = null;

    private ProgressDialog progressDialog = null;

    private Button mLoginBtn = null;
    private TextView mRegisterAct = null;
    private TextView mForgotPassAct = null;

    /* Firebase */
    private FirebaseAuth mAuth = null;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        Log.d(LOG, "onCreate");

        /* Firebase */
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        mEmailField = (EditText) findViewById(R.id.email_id);
        mPasswordField = (EditText) findViewById(R.id.password_id);

        mLoginBtn = (Button) findViewById(R.id.login_id);
        mLoginBtn.setOnClickListener(this);

        mRegisterAct = (TextView) findViewById(R.id.register_id);
        mRegisterAct.setOnClickListener(this);

        mForgotPassAct = (TextView) findViewById(R.id.forgot_password_id);
        mForgotPassAct.setOnClickListener(this);

        verify_validation(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void verify_validation(final boolean fromCreate) {

        final FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(user.getUid()).child("ProfileSettings");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        if (user.isEmailVerified()) {
                            startMainActivity(false);
                        } else {
                            sendEmail();
                        }
                    } else {
                        if(fromCreate) {
                            mAuth.signOut();
                        } else {
                            if (user.isEmailVerified()) {
                                startMainActivity(true);
                            } else {
                                sendEmail();
                            }
                        }
                    }
                }

                private void sendEmail() {

                    mLoginBtn.setEnabled(false);

                    Toast.makeText(getApplicationContext(),
                            "Nu ați activat contul", Toast.LENGTH_LONG).show();

                    user.sendEmailVerification()
                            .addOnCompleteListener(LoginActivity.this,
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            mLoginBtn.setEnabled(true);

                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(),
                                                        "V-am trimis un email pentru activare",
                                                        Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(),
                                                        "Nu v-am putut trimite email de activare",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                    mAuth.signOut();
                }

                private void startMainActivity(final boolean newUser) {

                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = mAuth.getCurrentUser();

                    if (user != null) {
                        final DatabaseReference ref = FirebaseDatabase.getInstance().
                                getReference().child("users").child(user.getUid())
                                .child("ProfileSettings").child("type");
                        ref.addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.getValue() == null ||
                                                !String.valueOf(dataSnapshot.getValue())
                                                        .equals("admin")) {
                                            Intent i = new Intent(getApplicationContext(),
                                                    MainActivity.class);
                                            i.putExtra("isRegistred", newUser);
                                            startActivity(i);
                                            finish();
                                        } else {
                                            startActivity(new Intent(getApplicationContext(),
                                                    AdminActivity.class));
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    } else {
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    private boolean verify_email(String email) {

        if(TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Adăugați un email", Toast.LENGTH_SHORT).show();
            return true;
        }

        if(email.length() < 3) {
            Toast.makeText(this, "Email prea scurt", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    private boolean verify_password(String password) {

        if(TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Adăugați o parola", Toast.LENGTH_SHORT).show();
            return true;
        }

        if(password.length() < 5) {
            Toast.makeText(this, "Parolă prea scurtă", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    private void loginUser(){

        String email = mEmailField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();

        if(verify_email(email)) return;
        if(verify_password(password)) return;

        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(LoginActivity.this,
                                    "Logare nereușită", Toast.LENGTH_SHORT).show();
                        } else {
                            verify_validation(false);
                        }

                        progressDialog.dismiss();
                    }
                });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.login_id:
                loginUser();
                break;
            case R.id.register_id:
                startActivity(new Intent(this, RegisterActivity.class));
                finish();
                break;
            case R.id.forgot_password_id:
                startActivity(new Intent(this, ForgotPasswordActivity.class));
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), BackPressedActivity.class);
        startActivityForResult(i, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 100) {
            if(resultCode == Activity.RESULT_OK){
                Bundle b = data.getExtras();
                if(b.getBoolean("result")) {
                    finish();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(UsefulThings.mNetworkStateIntentReceiver,
                UsefulThings.mNetworkStateChangedFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(UsefulThings.mNetworkStateIntentReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG, "onDestroy");

        progressDialog = null;
        mEmailField = null;
        mPasswordField = null;
        mLoginBtn = null;
        mRegisterAct = null;
        mForgotPassAct = null;
    }
}
