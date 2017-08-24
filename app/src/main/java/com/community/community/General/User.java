package com.community.community.General;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.Map;

@SuppressWarnings("serial")
public class User implements Serializable {
    private String LOG = this.getClass().getSimpleName();

    private String nickname = null;
    private String email = null;
    private String describe = null;
    private String address = null;
    private String uid = null;
    private String status = null;
    private String type = null;
    private int age = 0;
    private int ownCausesNumber = 0;
    private int supportedCausesNumber = 0;

    private String imageName = null;
    private String imageURL = null;

    public void updateFirebaseUserProfile(){
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("ProfileSettings");

        dRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Object> data = (Map<String,Object>) snapshot.getValue();

                nickname = data.get("nickname").toString();
                email = data.get("email").toString();
                status = data.get("status").toString();
                ownCausesNumber = Integer.valueOf(data.get("ownCauses").toString());
                supportedCausesNumber = Integer.valueOf(data.get("supportedCauses").toString());

//                Log.d(LOG, snapshot.toString());
                if(snapshot.hasChild("imageName")) {
                    imageName = data.get("imageName").toString();
                    Log.d(LOG, "Am imageName!" + imageName);
                }

                if(snapshot.hasChild("imageURL")) {
                    imageURL = data.get("imageURL").toString();
                    Log.d(LOG, "Am imageURL!" + imageURL);
                }

                if(snapshot.hasChild("describe"))
                    describe = data.get("describe").toString();

                if(snapshot.hasChild("address"))
                    address = data.get("address").toString();

                if(snapshot.hasChild("age"))
                    age = Integer.valueOf(data.get("age").toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //handle databaseError
            }
        });

        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Object> data = (Map<String,Object>) snapshot.getValue();
                if(data != null && data.containsKey("type")) {
                    type = data.get("type").toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageURL() {
        Log.d(LOG, "REMOVE! => " + imageURL);
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
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

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}


