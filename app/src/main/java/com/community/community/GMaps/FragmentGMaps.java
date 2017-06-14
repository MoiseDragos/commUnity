package com.community.community.GMaps;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.community.community.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class FragmentGMaps extends Fragment implements OnMapReadyCallback {

    // TODO: remove
    private String LOG = this.getClass().getSimpleName();
    
    OnBtnPressedListener mCallback;

    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private View mView;

    private DatabaseReference mDatabase;

    private FirebaseMarker firebaseNewMarker;

    //TODO: remove
    private static final String MAP_VIEW_KEY = "MAP_KEY";

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(LOG, "onDetach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase =  FirebaseDatabase.getInstance().getReference();
        Log.d(LOG, "onCreate");
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mDatabase =  FirebaseDatabase.getInstance().getReference();
//        Log.d(LOG, "onCreate");
//    }

    // TODO: Remove:

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        Log.d(LOG, "onResume");
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.d(LOG, "onResume");
//        if(mGoogleMap == null)
//            Log.d(LOG, "mGoogleMaps == null");
//    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        Log.d(LOG, "onPause");
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        Log.d(LOG, "onPause");
//        if(mGoogleMap == null)
//            Log.d(LOG, "mGoogleMaps == null");
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        Log.d(LOG, "onDestroy");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
        Log.d(LOG, "onSaveInstanceState");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
        Log.d(LOG, "onLowMemory");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(LOG, "onDestroyView");
    }


    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG, "onStop");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG, "onCreateView");
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

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        mView = inflater.inflate(R.layout.fragment_gmaps, container, false);
//        Log.d(LOG, "onCreateView");
//        return mView;
//    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(LOG, "onViewCreate");
        super.onViewCreated(view, savedInstanceState);
//        mMapView = (MapView) mView.findViewById(R.id.map);
//
//        if(mMapView != null){
//            mMapView.onCreate(null);
//            mMapView.onResume();
//            mMapView.getMapAsync(this);
//        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG, "onMapReady");
//        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;
        setUpMap();

        Log.d(LOG, "Maps initialization completed!");
    }

    private void setUpMap() {
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mGoogleMap.moveCamera( CameraUpdateFactory.newLatLngZoom
                (new LatLng(45.92109159958021, 25.075808800756928) , 5.687861f) );

        addCausesOnMap();
        // TODO:
        // 1. Display existing objects (DONE!)
        // 2. mGoogleMap.setMyLocationEnabled(true);
    }

    private void addCausesOnMap() {

        // TODO: Le salvam la deschiderea aplicatiei ca sa nu le cerem mereu de pe Firebase
        // TODO: Atentie daca apar modificari de la alti useri?!

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("causes")) {
                    DatabaseReference dRef = rootRef.child("causes");

                    dRef.addListenerForSingleValueEvent( new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            Map<String,Object> causes = (Map<String,Object>) dataSnapshot.getValue();
                            Map singleCauseMap;
                            Map singleCause;
                            //iterate through each user, ignoring their UID
                            for (Map.Entry<String, Object> entry : causes.entrySet()){

                                //Get cause map
                                singleCauseMap = (Map) entry.getValue();
                                singleCause = (Map) singleCauseMap.get("Info");
                                double lat = (Double) singleCause.get("latitude");
                                double lg = (Double) singleCause.get("longitude");
                                String owner = (String) singleCause.get("owner");
                                String name = (String) singleCause.get("name");

                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                FirebaseUser user = mAuth.getCurrentUser();
                                Log.d(LOG, lat + " " + lg + " " + user.getEmail());

                                //TODO: Daca am 2 obiective cu acelasi nume?!
                                if(owner.equals(user.getEmail())){
                                    mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lg))
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_causes))
                                            .title(name));
                                } else {
                                    mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lg))
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.other_causes))
                                            .title(name));
                                }
                            }
                            //TODO: ProgressBar pana termina de adaugat toate obiectivele!
                            //TODO: Nu mai reincarc obiectivele cand schimb din portret -> landscape si invers
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //handle databaseError
                        }
                    });
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
            String email = user.getEmail();

            Log.d(LOG, "firebaseNewMarker: "+ email + "  " + ll.latitude + "  " + ll.longitude);
            // Write on map
            firebaseNewMarker = new FirebaseMarker(mGoogleMap, email, ll.latitude, ll.longitude);
        }
    }

    public void onSubmit(String name, String description, LatLng latLng, FirebaseImages firebaseImages, int ownCauses){
        Log.d(LOG, "onSubmit");
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        firebaseNewMarker = new FirebaseMarker(email, latLng, name, description);

        // Write on Firebase - "causes"
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

        ref = mDatabase.child("causes").child(firebaseName).child("Images");
        ref.setValue(firebaseImages).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(LOG, e.getLocalizedMessage());
            }
        });

        // Write on Firebase - "users"
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ref = mDatabase.child("users").child(userUid).child("MyCauses").child(firebaseName).child("Info");
        ref.setValue(firebaseNewMarker).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(LOG, e.getLocalizedMessage());
            }
        });

        //TODO: E nevoie?

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

    //TODO: remove
    public boolean verify(){
        boolean ok = false;

        if(mGoogleMap == null){
            ok = true;
            Log.d(LOG, "mGoogleMap == null!!!!");
        }

        if(firebaseNewMarker == null) {
            ok = true;
            Log.d(LOG, "firebaseNewMarker == null!!!!!!!!!");
        }

        return ok;
    }

}
