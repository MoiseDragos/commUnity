package com.community.community.User;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class User {
    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    private String nickname;
    private String email;
    private String uid;
    private String status;
    private int ownCausesNumber;
    private int supportedCausesNumber;

    public User() { }

    public User(String email) {
        this.nickname = email;
        this.email = email;
    }

    public User(String email, String uid) {
        this.uid = uid;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String username) {
        this.nickname = username;
    }

    public int getOwnCausesNumber() {
        return ownCausesNumber;
    }

    public void setOwnCausesNumber(int ownCausesNumber) {
        this.ownCausesNumber = ownCausesNumber;
    }

    public int getSupportedCausesNumber() {
        return supportedCausesNumber;
    }

    public void setSupportedCausesNumber(int supportedCausesNumber) {
        this.supportedCausesNumber = supportedCausesNumber;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void updateUserProfile(){
        Log.d(LOG, "updateUserProfile");
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("ProfileSettings");
        Log.d(LOG, "Aici!");
        dRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d(LOG, "Aici2!");
                Map<String, Object> data = (Map<String,Object>) snapshot.getValue();

                nickname = data.get("nickname").toString();
                email = data.get("email").toString();
                ownCausesNumber = Integer.valueOf(data.get("ownCauses").toString());
                status = data.get("status").toString();
                supportedCausesNumber = Integer.valueOf(data.get("supportedCauses").toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //handle databaseError
            }
        });
    }
}


