package com.community.community.GMaps;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.community.community.CauseProfile.CauseProfileActivity;
import com.community.community.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class FragmentGMaps extends Fragment implements GoogleMap.OnInfoWindowClickListener, OnMapReadyCallback {

    // TODO: remove
    private String LOG = this.getClass().getSimpleName();
    
    OnBtnPressedListener mCallback;

    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private View mView;

    private DatabaseReference mDatabase;
    private FirebaseMarker firebaseNewMarker;

    // TODO: remove?
    private ArrayList<FirebaseMarker> firebaseMarkers;

    @Override
    public void onMapReady(GoogleMap googleMap) {
//        Log.d(LOG, "onMapReady");
        mGoogleMap = googleMap;
        setUpMap();

        mGoogleMap.setOnInfoWindowClickListener(this);
        Log.d(LOG, "Maps initialization completed!");
    }

    private void setUpMap() {
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mGoogleMap.moveCamera( CameraUpdateFactory.newLatLngZoom
                (new LatLng(45.92109159958021, 25.075808800756928) , 5.687861f) );

        // TODO: remove
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        addCausesOnMap();
        // TODO: mGoogleMap.setMyLocationEnabled(true);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String tag = String.valueOf(marker.getTag());
        String[] parts = tag.split("~");

        Log.d(LOG, "ownerUID: " + parts[0]);

        String causeId = parts[1] + "_" + parts[2];
        causeId = causeId.replace(".", "-");
        Log.d(LOG, "causeId: " + causeId);

        Intent intent = new Intent(getActivity(), CauseProfileActivity.class);
        intent.putExtra("ownerUID", parts[0]);
        intent.putExtra("causeId", causeId);
        getActivity().startActivityForResult(intent, 3);
    }

    private void addCausesOnMap() {

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("causes")) {
                    Map all = (Map) snapshot.getValue();
                    Map<String,Object> causes = (Map<String,Object>) all.get("causes");

                    firebaseMarkers = new ArrayList<FirebaseMarker>();
                    Map singleCauseMap;

                    for (Map.Entry<String, Object> entry : causes.entrySet()) {

                        // Get cause from map
                        singleCauseMap = (Map) entry.getValue();
                        Map singleCause = (Map) singleCauseMap.get("Info");

                        final double lat = (Double) singleCause.get("latitude");
                        final double lng = (Double) singleCause.get("longitude");
                        final String ownerUID = (String) singleCause.get("ownerUID");
                        final String name = (String) singleCause.get("name");
                        String profileURL = (String) singleCause.get("thumbnailImageURL");

                        // TODO: remove?
                        Glide
                                .with(getActivity())
                                .load(profileURL)
                                .asBitmap()
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                        FirebaseMarker firebaseAddMarker = new FirebaseMarker(resource,
                                                ownerUID, lat, lng, name);

                                        firebaseMarkers.add(firebaseAddMarker);
//                                                (String) singleCause.get("ownerUID"),
//                                                (String) singleCause.get("latitude"),
//                                                (String) singleCause.get("longitude"),
//                                                (String) singleCause.get("name"));
                                    }
                                });

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                        Log.d(LOG, lat + " " + lg + " " + user.getEmail());

                        //TODO: Daca am 2 obiective cu acelasi nume?!
                        Marker markerName;
                        String owner = (String) singleCause.get("owner");
                        if(owner.equals(user.getEmail())){
                            markerName = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_causes))
                                    .title(name));
                        } else {
                            markerName = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.other_causes))
                                    .title(name));
                        }
                        markerName.setTag(ownerUID + "~" + lat + "~" + lng);
                    }

                    //TODO: ProgressBar pana termina de adaugat toate obiectivele!
                    //TODO: Nu mai reincarc obiectivele cand schimb din portret -> landscape si invers
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //handle databaseError
            }
        });
    }

    // Container Activity must implement this interface
    public interface OnBtnPressedListener {
        void onButtonPressed(int i);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnBtnPressedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }


    //TODO: DEFAULT RULES on firebase write / read (auth != null)

    public void onAdd(){
        // Remove unsubmited marker
        if(firebaseNewMarker != null)
            cancelMarker();

        LatLng ll = getCurrentPosition();

        //TODO: remove
        float zommLevel = mGoogleMap.getCameraPosition().zoom;
        Log.d(LOG, "ZoomLevel: " + String.valueOf(zommLevel));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            // Write on map
            firebaseNewMarker = new FirebaseMarker(mGoogleMap, ll.latitude, ll.longitude);
        }
    }

    public void onSubmit(String name, String description, LatLng latLng, FirebaseImages firebaseImages, int ownCauses){
//        Log.d(LOG, "onSubmit");
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Write on Firebase - "causes"
        firebaseNewMarker = new FirebaseMarker(email, userUid, latLng, name, firebaseImages.getProfileImageURL());
        String firebaseName = latLng.latitude + "_" + latLng.longitude;
        firebaseName = firebaseName.replace(".", "-");
        Log.d(LOG, firebaseName);

        DatabaseReference ref = mDatabase.child("causes").child(firebaseName).child("Info");
        ref.setValue(firebaseNewMarker).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(LOG, e.getLocalizedMessage());
            }
        });

        ref = mDatabase.child("causes").child(firebaseName).child("SupportedBy").child("number");
        ref.setValue(0).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(LOG, e.getLocalizedMessage());
            }
        });

        // Write on Firebase - "users"
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        firebaseNewMarker = new FirebaseMarker(latLng, email, description, name,
                String.valueOf(dateFormat.format(new Date())));

        ref = mDatabase.child("users").child(userUid).child("MyCauses").child(firebaseName).child("Info");
        ref.setValue(firebaseNewMarker).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(LOG, e.getLocalizedMessage());
            }
        });

        ref = mDatabase.child("users").child(userUid).child("MyCauses").child(firebaseName).child("Images");
        ref.setValue(firebaseImages).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(LOG, e.getLocalizedMessage());
            }
        });

        firebaseNewMarker = null;

        /* Update ownCausesNumber */
        ref = mDatabase.child("users").child(userUid).child("ProfileSettings").child("ownCauses");
        ref.setValue(ownCauses);
    }

    public void cancelMarker(){
        if(firebaseNewMarker != null)
            firebaseNewMarker.cancelMarker();
    }

    public void onSearch(LatLng latLng){
        if(latLng != null)
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.312822f));
        else
            Toast.makeText(this.getContext(), "Adaugă o adresă validă", Toast.LENGTH_SHORT).show();
    }

    public LatLng getCurrentPosition(){
        return mGoogleMap.getCameraPosition().target;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase =  FirebaseDatabase.getInstance().getReference();
//        Log.d(LOG, "onCreate");
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
//        Log.d(LOG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
//        Log.d(LOG, "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
//        Log.d(LOG, "onDestroy");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
//        Log.d(LOG, "onSaveInstanceState");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
//        Log.d(LOG, "onLowMemory");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        Log.d(LOG, "onCreateView");
        try {
            mView = inflater.inflate(R.layout.fragment_gmaps, container, false);
            MapsInitializer.initialize(this.getActivity());
            mMapView = (MapView) mView.findViewById(R.id.map);
            mMapView.onCreate(savedInstanceState);
            mMapView.getMapAsync(this);
        } catch (InflateException e) {
            Log.e(LOG, "Inflate exception");
        }
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}
