package com.community.community.GMaps;

import android.util.Log;

import com.community.community.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class FirebaseMarker {
    private String name = null;
    private String owner = null;
    private String description = null;
    private double latitude = Double.NaN;
    private double longitude = Double.NaN;
    private Marker markerName = null;


    public FirebaseMarker(String email, LatLng latLng, String name, String description){
        this.name = name;
        this.owner = email;
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
        this.description = description;

        if(markerName != null)
            cancelMarker();
    }

    public FirebaseMarker(GoogleMap gMap, String email, double latitude, double longitude) {
        this.owner = email;
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

    public void cancelMarker(){
        markerName.remove();
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
}
