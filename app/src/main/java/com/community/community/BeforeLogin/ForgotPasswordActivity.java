package com.community.community.BeforeLogin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ProviderQueryResult;

import java.util.List;


public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mEmailField;
    private Button mLoginBtn;
    private TextView mSingUpAct;
    private TextView mLogInAct;

    private ProgressDialog progressDialog;

    /* Firebase */
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        /* Firebase */
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(mAuth.getCurrentUser() != null){

                    //TODO: nu cred ca are cum sa intre aici! Verifica!
                    finish();
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.putExtra("isRegistred", true);
                    startActivity(i);
//                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                }

            }
        };

        mEmailField = (EditText) findViewById(R.id.email_id);
        mLoginBtn = (Button) findViewById(R.id.sendEmail_id);
        mSingUpAct = (TextView) findViewById(R.id.singUp_activity_id);
        mLogInAct = (TextView) findViewById(R.id.login_activity_id);

        progressDialog = new ProgressDialog(this);

        mLoginBtn.setOnClickListener(this);
        mSingUpAct.setOnClickListener(this);
        mLogInAct.setOnClickListener(this);

        //TODO: EditText pressed long
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    private boolean verify_email(final String email) {

        if(TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Adăugați un email", Toast.LENGTH_SHORT).show();
            return true;
        }

        progressDialog.setMessage("Verify email...");
        progressDialog.show();

        final Task<ProviderQueryResult> queryResultTask = mAuth.fetchProvidersForEmail(email)
                .addOnCompleteListener(this, new OnCompleteListener<ProviderQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<ProviderQueryResult> task) {

                progressDialog.dismiss();

                if(task.isSuccessful()){
                    List<String> taskList = task.getResult().getProviders();

                    if(taskList.isEmpty()){
                        Toast.makeText(ForgotPasswordActivity.this, "Cont inexistent", Toast.LENGTH_SHORT).show();

                    }
                } else {
                  // TODO:
                    Toast.makeText(ForgotPasswordActivity.this, "Firebase error!", Toast.LENGTH_SHORT).show();
                }
            }
        });

//        progressDialog.setMessage("Sending email...");
//        progressDialog.show();
//
//        try {
//            Thread.sleep(2000);
//        } catch(InterruptedException ex) {
//            Thread.currentThread().interrupt();
//        }
//        progressDialog.dismiss();
        //TODO: alte verificari

        return false;
    }

    private void sendEmail(){

        String email = mEmailField.getText().toString().trim();

        if(verify_email(email)) return;

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ForgotPasswordActivity.this, "Sending email...", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }
            }
        });

    }

    private void function() {
    }

    @Override
    public void onClick(View v) {

        if(v == mLoginBtn ){
            sendEmail();
        }

        if(v == mSingUpAct){
            finish();
            startActivity(new Intent(this, RegisterActivity.class));
        }

        if(v == mLogInAct){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}
