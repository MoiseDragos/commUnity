package com.community.community;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.community.community.BeforeLogin.LoginActivity;
import com.community.community.General.UsefulThings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InitialActivity extends AppCompatActivity {

    private String LOG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.init_activity);

        ProgressBar spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.blue4), PorterDuff.Mode.SRC_IN );
        spinner.setVisibility(View.VISIBLE);

        UsefulThings.initNetworkListener();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

//        try {
//            Thread.sleep(10000);
        if(user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().
                    getReference().child("users").child(user.getUid()).child("ProfileSettings").
                    child("type");
            ref.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue() == null ||
                                    !String.valueOf(dataSnapshot.getValue()).equals("admin")) {
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                i.putExtra("isRegistred", false);
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
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onResume() {
        Log.d(LOG, "onResume");
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
        Log.d(LOG, "onPause");
        super.onPause();
        unregisterReceiver(UsefulThings.mNetworkStateIntentReceiver);
    }
}
