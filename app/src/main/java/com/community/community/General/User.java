package com.community.community.General;

import android.graphics.Bitmap;
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
    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    private String nickname = null;
    private String email = null;
    private String describe = null;
    private String address = null;
    private String uid = null;
    private String status = null;
    private int age = 0;
    private int ownCausesNumber = 0;
    private int supportedCausesNumber = 0;
    private boolean changedProfilePic = false;

    private String imageName = null;
    private String imageURL = null;

    public void updateFirebaseUserProfile(){
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
    }

    public void updateLocalUserProfile(User user) {

        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("ProfileSettings");

        if(!user.nickname.equals(nickname)){
            nickname = user.nickname;
            dRef.child("nickname").setValue(nickname);
        }

        if(describe == null) {
            if(user.describe != null){
                describe = user.describe;
                dRef.child("describe").setValue(describe);
            }
        } else {
            if(!user.describe.equals(describe)) {
                describe = user.describe;
                dRef.child("describe").setValue(describe);
            }
        }

        if(address == null) {
            if(user.address != null){
                address = user.address;
                dRef.child("address").setValue(address);
            }
        } else {
            if(!user.address.equals(address)) {
                address = user.address;
                dRef.child("address").setValue(address);
            }
        }

        if(user.age != age){
            age = user.age;
            dRef.child("age").setValue(age);
        }

        if(imageURL == null) {
            if (user.imageURL != null) {
                imageURL = user.imageURL;
                dRef.child("imageURL").setValue(user.imageURL);
            }
        } else {
            if(!user.imageURL.equals(imageURL)){
                imageURL = user.imageURL;
                dRef.child("imageURL").setValue(user.imageURL);
            }
        }

        if(imageName == null) {
            if (user.imageName != null) {
                imageName = user.imageName;
                dRef.child("imageName").setValue(user.imageName);
            }
        } else {
            if(!user.imageName.equals(imageName)){
                imageName = user.imageName;
                dRef.child("imageName").setValue(user.imageName);
            }
        }

        // For NGO
        if(!user.status.equals(status)){
            status = user.status;
            dRef.child("status").setValue(user.status);
        }
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

    public boolean isChangedProfilePic() {
        return changedProfilePic;
    }

    public void setChangedProfilePic(boolean changedProfilePic) {
        this.changedProfilePic = changedProfilePic;
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

    public String getStatus() {
        return status;
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
}


