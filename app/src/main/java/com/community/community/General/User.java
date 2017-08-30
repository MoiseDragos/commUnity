package com.community.community.General;

import java.io.Serializable;

@SuppressWarnings("serial")
public class User implements Serializable {

    private String nickname = null;
    private String email = null;
    private String describe = null;
    private String address = null;
    private String uid = null;
    private String status = null;
    private String type = null;
    private String site = null;
    private String official_address = null;
    private String donate = null;
    private int age = 0;
    private int ownCausesNumber = 0;
    private int supportedCausesNumber = 0;

    public User() {}

    public User(String nickname, String email, String describe, String address, String uid,
                String status, String imageName, String imageURL, int age, int ownCausesNumber,
                int supportedCausesNumber, String official_address, String site, String donate){

        this.nickname = nickname;
        this.email = email;
        this.describe = describe;
        this.address = address;
        this.uid = uid;
        this.status = status;
        this.imageName = imageName;
        this.imageURL = imageURL;
        this.age = age;
        this.ownCausesNumber = ownCausesNumber;
        this.supportedCausesNumber = supportedCausesNumber;
        this.official_address = official_address;
        this.site = site;
        this.donate = donate;
    }


    private String imageName = null;
    private String imageURL = null;

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

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getOfficial_address() {
        return official_address;
    }

    public void setOfficial_address(String official_address) {
        this.official_address = official_address;
    }

    public String getDonate() {
        return donate;
    }

    public void setDonate(String donate) {
        this.donate = donate;
    }
}


