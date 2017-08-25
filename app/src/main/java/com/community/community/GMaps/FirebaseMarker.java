package com.community.community.GMaps;

import com.community.community.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

class FirebaseMarker {

    private String thumbnailImageURL = null;
    private String date = null;
    private String name = null;
    private String owner = null;
    private String ownerUID = null;
    private String description = null;
    private double latitude = Double.NaN;
    private double longitude = Double.NaN;
    private Marker markerName = null;
    private long supportedBy;

    /* Submit "causes" marker constructor */
    FirebaseMarker(String email, String uid, LatLng latLng, String name,
                   String profileImageURL, long supportedBy) {
        this.owner = email;
        this.ownerUID = uid;
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
        this.name = name;
        this.thumbnailImageURL = profileImageURL;
        this.supportedBy = supportedBy;
    }

    /* Submit "users" marker constructor */
    FirebaseMarker(LatLng latLng, String email, String description, String name, String date) {
        this.owner = email;
        this.description = description;
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
        this.name = name;
        this.date = date;
    }

    /* Draft marker constructor */
    FirebaseMarker(GoogleMap gMap, double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        addDraftMarker(gMap);
    }

    private void addDraftMarker(final GoogleMap gMap){

        gMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker arg0) {
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onMarkerDragEnd(Marker arg0) {
                latitude = arg0.getPosition().latitude;
                longitude = arg0.getPosition().longitude;

                gMap.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));
            }

            @Override
            public void onMarkerDrag(Marker arg0) {
            }
        });

        markerName = gMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_pin_icon))
                .draggable(true)
                .title(owner));
    }

    void cancelMarker(){
        markerName.remove();
    }

    public String getThumbnailImageURL() {
        return thumbnailImageURL;
    }

    public void setThumbnailImageURL(String thumbnailImageURL) {
        this.thumbnailImageURL = thumbnailImageURL;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerUID() {
        return ownerUID;
    }

    public void setOwnerUID(String ownerUID) {
        this.ownerUID = ownerUID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Marker getMarkerName() {
        return markerName;
    }

    public void setMarkerName(Marker markerName) {
        this.markerName = markerName;
    }

    public long getSupportedBy() {
        return supportedBy;
    }

    public void setSupportedBy(long supportedBy) {
        this.supportedBy = supportedBy;
    }
}
