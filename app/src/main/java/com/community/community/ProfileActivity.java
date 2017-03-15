package com.community.community;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    /* Firebase */
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Button mLogoutBtn;
    private TextView mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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

        FirebaseUser user = mAuth.getCurrentUser();

        mEmail = (TextView) findViewById(R.id.userEmail);
        mLogoutBtn = (Button) findViewById(R.id.logOutBtn);

        mEmail.setText(user.getEmail());

        mLogoutBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if(v == mLogoutBtn){
            mAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

    }
}
