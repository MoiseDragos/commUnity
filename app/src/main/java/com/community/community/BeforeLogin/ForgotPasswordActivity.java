package com.community.community.BeforeLogin;

import android.app.Activity;
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

import com.community.community.General.BackPressedActivity;
import com.community.community.General.UsefulThings;
import com.community.community.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ProviderQueryResult;

import java.util.List;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mEmailField = null;
    private ProgressDialog progressDialog = null;
    private Button mLoginBtn = null;
    private TextView mSingUpAct = null;
    private TextView mLogInAct = null;

    /* Firebase */
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_activity);

        /* Firebase */
        mAuth = FirebaseAuth.getInstance();

        mEmailField = (EditText) findViewById(R.id.email_id);
        progressDialog = new ProgressDialog(this);

        mLoginBtn = (Button) findViewById(R.id.sendEmail_id);
        mLoginBtn.setOnClickListener(this);

        mSingUpAct = (TextView) findViewById(R.id.singUp_activity_id);
        mSingUpAct.setOnClickListener(this);

        mLogInAct = (TextView) findViewById(R.id.login_activity_id);
        mLogInAct.setOnClickListener(this);
    }

    private boolean verify_email(final String email) {

        if(TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Adăugați un email", Toast.LENGTH_SHORT).show();
            return true;
        }

        if(email.length() < 3) {
            Toast.makeText(this, "Email prea scurt", Toast.LENGTH_SHORT).show();
            return true;
        }

        progressDialog.setMessage("Verify email...");
        progressDialog.show();

        mAuth.fetchProvidersForEmail(email)
                .addOnCompleteListener(this, new OnCompleteListener<ProviderQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<ProviderQueryResult> task) {

                progressDialog.dismiss();

                if(task.isSuccessful()){
                    List<String> taskList = task.getResult().getProviders();

                    if(taskList.isEmpty()){
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Cont inexistent", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Email inconsistent!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return false;
    }

    private void sendEmail(){

        String email = mEmailField.getText().toString().trim();

        if(verify_email(email)) return;

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Sending email...", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }
            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.sendEmail_id:
                sendEmail();
                break;
            case R.id.singUp_activity_id:
                startActivity(new Intent(this, RegisterActivity.class));
                finish();
                break;
            case R.id.login_activity_id:
                startActivity(new Intent(this, LoginActivity.class));
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
        mAuth = null;

        mEmailField = null;
        progressDialog = null;
        mLoginBtn = null;
        mSingUpAct = null;
        mLogInAct = null;
    }
}
