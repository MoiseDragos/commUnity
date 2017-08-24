package com.community.community.BeforeLogin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.community.community.General.BackPressedActivity;
import com.community.community.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;

import java.util.List;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener  {

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mRegisterBtn;
    private TextView mLogin;

    private ProgressDialog progressDialog;

    private boolean exist = false;

    /* Firebase */
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        /* Firebase */
        mAuth = FirebaseAuth.getInstance();

        mEmailField = (EditText) findViewById(R.id.email_id);
        mPasswordField = (EditText) findViewById(R.id.password_id);
        mRegisterBtn = (Button) findViewById(R.id.register_id);
        mLogin = (TextView) findViewById(R.id.login_activity_id);

        progressDialog = new ProgressDialog(this);

        mRegisterBtn.setOnClickListener(this);
        mLogin.setOnClickListener(this);

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

        mAuth.fetchProvidersForEmail(email)
                .addOnCompleteListener(this, new OnCompleteListener<ProviderQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                        progressDialog.dismiss();

                        if(task.isSuccessful()){
                            List<String> taskList = task.getResult().getProviders();

                            if(!taskList.isEmpty()){
                                exist = true;
                                Toast.makeText(RegisterActivity.this,
                                        "Contul existentă", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        return false;
    }

    private boolean verify_password(String password) {

        if(TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Adăugați o parolă", Toast.LENGTH_SHORT).show();
            return true;
        }

        if(password.length() < 6) {
            Toast.makeText(this, "Parolă prea scurtă", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    private void registerUser(){

        final String email = mEmailField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();

        if(verify_email(email)) return;
        if(verify_password(password)) return;

        progressDialog.setMessage("Registering User...");
        progressDialog.show();


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mRegisterBtn.setEnabled(false);

                        if(task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this,
                                    "Înregistrare reușită", Toast.LENGTH_SHORT).show();

                            final FirebaseUser user = mAuth.getCurrentUser();
                            if(user != null) {
                                user.sendEmailVerification()
                                    .addOnCompleteListener(RegisterActivity.this,
                                        new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                mRegisterBtn.setEnabled(true);

                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getApplicationContext(),
                                                            "V-am trimis un email pentru activare",
                                                            Toast.LENGTH_LONG).show();
                                                    startLoginActivity();
                                                } else {
                                                    Toast.makeText(getApplicationContext(),
                                                            "Nu v-am putut trimite email de activare",
                                                            Toast.LENGTH_SHORT).show();
                                                    startLoginActivity();
                                                }
                                            }

                                            private void startLoginActivity() {
                                                startActivity(new Intent(getApplicationContext(),
                                                        LoginActivity.class));
                                                finish();
                                            }
                                        });
                            }
                        } else {
                            if(!exist){
                                Toast.makeText(RegisterActivity.this,
                                        "Înregistrare nereușită, vă rugăm reîncercați",
                                        Toast.LENGTH_SHORT).show();
                            }
                            exist = false;
                        }

                        progressDialog.dismiss();

                    }
                });

    }

    @Override
    public void onClick(View v) {

        if(v == mRegisterBtn ){
            registerUser();
        }

        if(v == mLogin){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
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
    protected void onDestroy() {
        super.onDestroy();
        mEmailField = null;
        mPasswordField = null;
        mRegisterBtn = null;
        mLogin = null;
        progressDialog = null;
    }
}
