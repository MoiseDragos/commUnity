package com.community.community;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.community.community.BeforeLogin.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SubmitCauseActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressDialog progressDialog;

    private EditText mNameField;
    private EditText mDescriptionField;
    private Button mSubmitBtn;
    private Button mCancelBtn;

    /* Firebase */
    private FirebaseAuth mAuth = null;
    private FirebaseAuth.AuthStateListener mAuthListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submit_cause_activity);

         /* Firebase */
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() != null){

                    finish();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));

                }

            }
        };

        mNameField = (EditText) findViewById(R.id.causes_name);
        mDescriptionField = (EditText) findViewById(R.id.describe_name);
        mSubmitBtn = (Button) findViewById(R.id.submit_cause);
        mCancelBtn = (Button) findViewById(R.id.cancel_cause);

        progressDialog = new ProgressDialog(this);

        mSubmitBtn.setOnClickListener(this);
        mCancelBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {


        if(v == mSubmitBtn ){

            if(!verify_name() || !verify_description()){
                return;
            }
            //TODO:
            // 1. Verify name / describe
            // 2. Max line for EditText (ScrollView?)
            Intent intent = new Intent();
            intent.putExtra("NameFiled", mNameField.getText().toString());
            intent.putExtra("Description", mDescriptionField.getText().toString());
            setResult(RESULT_OK, intent);
            finish();
        }

        if(v == mCancelBtn){
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }

    }

    private boolean verify_description() {
        Log.d("GMap", "Name size: " + String.valueOf(mDescriptionField.length()));

        if(mDescriptionField.length() < 5){
            Toast.makeText(getApplicationContext(), "Descriere prea scurtă!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean verify_name() {
        Log.d("GMap", "Name size: " + String.valueOf(mNameField.length()));
        if(mNameField.length() < 5){
            Toast.makeText(getApplicationContext(), "Nume prea scurt!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
