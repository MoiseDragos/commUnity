package com.community.community.BeforeLogin;

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

import com.community.community.MainActivity;
import com.community.community.R;
import com.community.community.User.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener  {

    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mRegisterBtn;
    private TextView mLogin;

    private ProgressDialog progressDialog;

    boolean exist = false;

    /* Firebase */
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        /* Firebase */
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() != null){

                    // Intent User Account
                    finish();

                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.putExtra("isRegistred", true);
                    startActivity(i);
//                    Log.d(LOG, "True");
//                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }

            }
        };

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mEmailField = (EditText) findViewById(R.id.email_id);
        mPasswordField = (EditText) findViewById(R.id.password_id);
        mRegisterBtn = (Button) findViewById(R.id.register_id);
        mLogin = (TextView) findViewById(R.id.login_activity_id);

        progressDialog = new ProgressDialog(this);

        mRegisterBtn.setOnClickListener(this);
        mLogin.setOnClickListener(this);



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

        final Task<ProviderQueryResult> queryResultTask = mAuth.fetchProvidersForEmail(email)
                .addOnCompleteListener(this, new OnCompleteListener<ProviderQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                        progressDialog.dismiss();

                        if(task.isSuccessful()){
                            List<String> taskList = task.getResult().getProviders();

                            if(!taskList.isEmpty()){
                                exist = true;
                                Toast.makeText(RegisterActivity.this, "Contul existentă", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        //TODO: other verifications

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

    private void registerUser(){

        final String email = mEmailField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();

        if(verify_email(email)) return;
        if(verify_password(password)) return;

        progressDialog.setMessage("Registering User...");
        progressDialog.show();


//        if(!exist){
//            writeNewUser(email);
//        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this, "Înregistrare reușită", Toast.LENGTH_SHORT).show();
//                            writeNewUser(email);
                            /*TODO: Send activation email*/
//                            finish();
//                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        } else {
                            if(!exist){
                                Toast.makeText(RegisterActivity.this, "Înregistrare nereușită, vă rugăm reîncercați", Toast.LENGTH_SHORT).show();
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
}
