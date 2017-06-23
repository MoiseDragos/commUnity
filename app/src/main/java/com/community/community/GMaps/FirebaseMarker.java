package com.community.community.GMaps;

import android.graphics.Bitmap;
import android.util.Log;

import com.community.community.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class FirebaseMarker {

    private Bitmap thumbnailImage = null;
    private String thumbnailImageURL = null;
    private String date = null;
    private String name = null;
    private String owner = null;
    private String ownerUID = null;
    private String description = null;
    private double latitude = Double.NaN;
    private double longitude = Double.NaN;
    private int supportedBy;
    private Marker markerName = null;

    /* From Firebase marker constructor */
    public FirebaseMarker(Bitmap bitmap, String uid, double lat, double lng, String name){
        this.thumbnailImage = bitmap;
        this.ownerUID = uid;
        this.latitude = lat;
        this.longitude = lng;
        this.name = name;
    }

    /* Submit "causes" marker constructor */
    public FirebaseMarker(String email, String uid, LatLng latLng, String name, String profileImageURL) {
        this.owner = email;
        this.ownerUID = uid;
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
        this.name = name;
        this.thumbnailImageURL = profileImageURL;
    }

    /* Submit "users" marker constructor */
    public FirebaseMarker(LatLng latLng, String email, String description, String name, String date) {
        this.owner = email;
        this.description = description;
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
        this.name = name;
        this.date = date;
        this.supportedBy = 1;
    }

    /* Draft marker constructor */
    public FirebaseMarker(GoogleMap gMap, double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        addDraftMarker(gMap);
    }

    public void addDraftMarker(final GoogleMap gMap){

        gMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker arg0) {
                // TODO Auto-generated method stub
                Log.d("GMaps", "onMarkerDragStart..."+arg0.getPosition().latitude+"..."+arg0.getPosition().longitude);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onMarkerDragEnd(Marker arg0) {
                // TODO Auto-generated method stub
                Log.d("GMaps", "onMarkerDragEnd..."+arg0.getPosition().latitude+"..."+arg0.getPosition().longitude);
                latitude = arg0.getPosition().latitude;
                longitude = arg0.getPosition().longitude;

                gMap.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));
            }

            @Override
            public void onMarkerDrag(Marker arg0) {
                // TODO Auto-generated method stub
                Log.d("GMaps", "onMarkerDrag...");
            }
        });

        markerName = gMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.unsubmited_causes))
                .draggable(true)
                .title(owner));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String email) { this.owner = email; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
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

    public String getOwnerUID() {
        return ownerUID;
    }

    public void setOwnerUID(String ownerUID) {
        this.ownerUID = ownerUID;
    }

    public Bitmap getThumbnailImage() {
        return thumbnailImage;
    }

    public void setThumbnailImage(Bitmap thumbnailImage) {
        this.thumbnailImage = thumbnailImage;
    }

    public String getThumbnailImageURL() {
        return thumbnailImageURL;
    }

    public void setThumbnailImageURL(String thumbnailImageURL) {
        this.thumbnailImageURL = thumbnailImageURL;
    }

    public void cancelMarker(){
        markerName.remove();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getSupportedBy() {
        return supportedBy;
    }

    public void setSupportedBy(int supportedBy) {
        this.supportedBy = supportedBy;
    }
}
