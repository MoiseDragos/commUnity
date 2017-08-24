package com.community.community.GMaps;


import java.io.Serializable;

public class FirebaseImages implements Serializable {

    private String profileImageName;
    private String profileThumbnailURL;
    private String profileImageURL;

    private String optionalImageName1;
    private String optionalThumbnailURL1;
    private String optionalImageURL1;

    private String optionalImageName2;
    private String optionalThumbnailURL2;
    private String optionalImageURL2;

    public FirebaseImages(){}

    public FirebaseImages(String profileImageName, String profileImageURL){
        this.profileImageName = profileImageName;
        this.profileImageURL = profileImageURL;
    }

    public String getProfileImageName() {
        return profileImageName;
    }

    public void setProfileImageName(String profileImageName) {
        this.profileImageName = profileImageName;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    public void setProfileImageURL(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }

    public String getOptionalImageName1() {
        return optionalImageName1;
    }

    public void setOptionalImageName1(String optionalImageName1) {
        this.optionalImageName1 = optionalImageName1;
    }

    public String getOptionalImageURL1() {
        return optionalImageURL1;
    }

    public void setOptionalImageURL1(String optionalImageURL1) {
        this.optionalImageURL1 = optionalImageURL1;
    }

    public String getOptionalImageName2() {
        return optionalImageName2;
    }

    public void setOptionalImageName2(String optionalImageName2) {
        this.optionalImageName2 = optionalImageName2;
    }

    public String getOptionalImageURL2() {
        return optionalImageURL2;
    }

    public void setOptionalImageURL2(String optionalImageURL2) {
        this.optionalImageURL2 = optionalImageURL2;
    }

    void setProfileThumbnailURL(String profileThumbnailURL) {
        this.profileThumbnailURL = profileThumbnailURL;
    }

    void setOptionalThumbnailURL1(String optionalThumbnailURL1) {
        this.optionalThumbnailURL1 = optionalThumbnailURL1;
    }

    void setOptionalThumbnailURL2(String optionalThumbnailURL2) {
        this.optionalThumbnailURL2 = optionalThumbnailURL2;
    }
}
