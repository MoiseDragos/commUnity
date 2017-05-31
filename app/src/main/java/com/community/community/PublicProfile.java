package com.community.community;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.community.community.BeforeLogin.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class PublicProfile extends AppCompatActivity {

    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    /* Firebase */
    private FirebaseAuth mAuth = null;
    private FirebaseAuth.AuthStateListener mAuthListener = null;
    private DatabaseReference mDatabase = null;

    /* Profile details */
    private ImageView blurImage = null;
    private CircleImageView circleImage = null;
    private TextView nickname = null;
    private TextView email = null;
    private TextView ownNumber = null;
    private TextView supportedNumber = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.public_profile_activity);

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
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Log.d(LOG, "1");
        blurImage = (ImageView) findViewById(R.id.blur_profile_image);
        circleImage = (CircleImageView) findViewById(R.id.profile_image);
        nickname = (TextView) findViewById(R.id.userNickname);
        email = (TextView) findViewById(R.id.userEmail);
        ownNumber = (TextView) findViewById(R.id.own_number);
        supportedNumber = (TextView) findViewById(R.id.supported_number);
        Log.d(LOG, "2");
        initialize();
    }

    private void initialize() {
        FirebaseUser user = mAuth.getCurrentUser();

        blurImage.setImageDrawable(getResources().getDrawable(R.drawable.profile));
        circleImage.setImageDrawable(getResources().getDrawable(R.drawable.profile));
        DatabaseReference ref = mDatabase.child("users").child(user.getUid()).child("ProfileSettings").child("nickname");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(LOG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(LOG, "Failed to read value.", error.toException());
            }
        });
        nickname.setText(ref.getKey());

        ref = mDatabase.child("users").child(user.getUid()).child("ProfileSettings").child("email");
        email.setText(ref.getKey());

        ref = mDatabase.child("users").child(user.getUid()).child("MyCauses").child("number");
        ownNumber.setText(ref.getKey());

        ref = mDatabase.child("users").child(user.getUid()).child("SupportedCauses").child("number");
        supportedNumber.setText(ref.getKey());
    }
}
