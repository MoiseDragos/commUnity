package com.community.community.GMaps;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class FirebaseMarker {
    private String name;
    private String owner;
    private String description;
    private double latitude;
    private double longitude;

    public FirebaseMarker() { }

    public FirebaseMarker(String name, String description, String email,double latitude, double longitude) {
        this.name = name;
        this.owner = email;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void addMarker(GoogleMap gMap,double latitude, double longitude){
        gMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(owner));
    }

    public String getFirstname() {
        return name;
    }

    public void setFirstname(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String email) { this.owner = email; }

    public String getLastname() {
        return description;
    }

    public void setLastname(String description) {
        this.description = description;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
