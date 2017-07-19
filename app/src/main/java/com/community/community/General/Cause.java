package com.community.community.General;

import android.graphics.Bitmap;

import com.community.community.GMaps.FirebaseImages;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Cause implements Serializable {
    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    private String owner = null;
    private String ownerUID = null;
    private String name = null;
    private String supportedBy = null;
    private String description = null;
    private int numberOfPhotos = 0;
    private FirebaseImages firebaseImages = null;

    private Bitmap profileImage = null;
    private Bitmap optionalImage1 = null;
    private Bitmap optionalImage2 = null;

    public Cause(){}

    public Cause(Bitmap bitmap, String uid, String des, String name, String owner){
        this.profileImage = bitmap;
        this.ownerUID = uid;
        this.description = des;
        this.name = name;
        this.owner = owner;
    }

    public Cause(Bitmap bitmap, String uid, String des, String name, String owner,
          String url, String imageName, int imagesNumber) {

        this.profileImage = bitmap;
        this.ownerUID = uid;
        this.description = des;
        this.name = name;
        this.owner = owner;
        this.firebaseImages = new FirebaseImages(imageName, url);
        this.numberOfPhotos = imagesNumber;
    }

    public String getLOG() {
        return LOG;
    }

    public void setLOG(String LOG) {
        this.LOG = LOG;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerUID() {
        return ownerUID;
    }

    public void setOwnerUID(String ownerUID) {
        this.ownerUID = ownerUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSupportedBy() {
        return supportedBy;
    }

    public void setSupportedBy(String supportedBy) {
        this.supportedBy = supportedBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FirebaseImages getFirebaseImages() {
        return firebaseImages;
    }

    public void setFirebaseImages(FirebaseImages firebaseImages) {
        this.firebaseImages = firebaseImages;
    }

    public void setFirebaseImagesProfile(String imageUrl, String imageName) {
        firebaseImages.setProfileImageName(imageName);
        firebaseImages.setProfileImageURL(imageUrl);
    }

    public String getFirebaseImagesProfileURL() {
        return firebaseImages.getProfileImageURL();
    }

    public String getFirebaseImagesProfileName() {
        return firebaseImages.getProfileImageName();
    }

    public void setFirebaseImagesOptional1(String imageUrl, String imageName) {
        firebaseImages.setOptionalImageName1(imageName);
        firebaseImages.setOptionalImageURL1(imageUrl);
    }

    public String getFirebaseImagesURLOptional1() {
        return firebaseImages.getOptionalImageURL1();
    }

    public String getFirebaseImagesNameOptional1() {
        return firebaseImages.getOptionalImageName1();
    }

    public void setFirebaseImagesOptional2(String imageUrl, String imageName) {
        firebaseImages.setOptionalImageName2(imageName);
        firebaseImages.setOptionalImageURL2(imageUrl);
    }

    public String getFirebaseImagesURLOptional2() {
        return firebaseImages.getOptionalImageURL2();
    }

    public String getFirebaseImagesNameOptional2() {
        return firebaseImages.getOptionalImageName2();
    }

    public Bitmap getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Bitmap profileImage) {
        this.profileImage = profileImage;
    }

    public Bitmap getOptionalImage1() {
        return optionalImage1;
    }

    public void setOptionalImage1(Bitmap optionalImage1) {
        this.optionalImage1 = optionalImage1;
    }

    public Bitmap getOptionalImage2() {
        return optionalImage2;
    }

    public void setOptionalImage2(Bitmap optionalImage2) {
        this.optionalImage2 = optionalImage2;
    }

    public int getNumberOfPhotos() {
        return numberOfPhotos;
    }

    public void setNumberOfPhotos(int numberOfPhotos) {
        this.numberOfPhotos = numberOfPhotos;
    }

}


