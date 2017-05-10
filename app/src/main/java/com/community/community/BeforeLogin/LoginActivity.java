package com.community.community.BeforeLogin;

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

import com.community.community.MainActivity;
import com.community.community.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mLoginBtn;
    private TextView mRegisterAct;
    private TextView mForgotPassAct;

    private ProgressDialog progressDialog;

    /* Firebase */
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        /* Firebase */
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(mAuth.getCurrentUser() != null){

                    finish();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                }

            }
        };

        mEmailField = (EditText) findViewById(R.id.email_id);
        mPasswordField = (EditText) findViewById(R.id.password_id);
        mLoginBtn = (Button) findViewById(R.id.login_id);
        mRegisterAct = (TextView) findViewById(R.id.register_id);
        mForgotPassAct = (TextView) findViewById(R.id.forgot_password_id);

        progressDialog = new ProgressDialog(this);

        mLoginBtn.setOnClickListener(this);
        mRegisterAct.setOnClickListener(this);
        mForgotPassAct.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    private boolean verify_email(String email) {

        if(TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Adăugați un email", Toast.LENGTH_SHORT).show();
            return true;
        }

        //TODO: alte verificari

        return false;
    }

    private boolean verify_password(String password) {

        if(TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Adăugați o parola", Toast.LENGTH_SHORT).show();
            return true;
        }

        //TODO: alte verificari

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
                        if(task.isSuccessful()){
                            //Toast.makeText(LoginActivity.this, "Înregistrare reușită", Toast.LENGTH_SHORT).show();
                            //TODO: Mai multe pagini deschise?! Back
                            finish();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        } else {
                            Toast.makeText(LoginActivity.this, "Logare nereușită", Toast.LENGTH_SHORT).show();
                        }

                        progressDialog.dismiss();
                    }
                });

    }

    @Override
    public void onClick(View v) {

        if(v == mLoginBtn ){
            loginUser();
        }

        if(v == mRegisterAct){
            finish();
            startActivity(new Intent(this, RegisterActivity.class));
        }

        if(v == mForgotPassAct){
            finish();
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        }

    }
}
