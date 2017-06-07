package com.community.community.GMaps;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase =  FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_gmaps, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMapView = (MapView) mView.findViewById(R.id.map);

        if(mMapView != null){
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        mGoogleMap = googleMap;
        setUpMap();

        Log.d(LOG, "Maps init completed!");
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

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("causes")) {
                    DatabaseReference dRef = rootRef.child("causes");


                    dRef.addListenerForSingleValueEvent( new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Get map of users in datasnapshot
                            addInitMarkers((Map<String,Object>) dataSnapshot.getValue());
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

    private void addInitMarkers(Map<String,Object> causes) {

        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : causes.entrySet()){

            //Get cause map
            Map singleCauseMap = (Map) entry.getValue();
            Map singleCause = (Map) singleCauseMap.get("Info");
            double lat = (Double) singleCause.get("latitude");
            double lg = (Double) singleCause.get("longitude");
            String owner = (String) singleCause.get("owner");

            Log.d(LOG, lat + " " + lg + " " + owner);

            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            if(owner.equals(user.getEmail())){
                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lg))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_causes))
                        .title(entry.getKey()));
            } else {
                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lg))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.other_causes))
                        .title(entry.getKey()));
            }
        }
        //TODO: ProgressBar pana termina de adaugat toate obiectivele!
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
            onCancel();

        LatLng ll = mGoogleMap.getCameraPosition().target;

        //TODO: remove
        float zommLevel = mGoogleMap.getCameraPosition().zoom;
        Log.d(LOG, "ZoomLevel: " + String.valueOf(zommLevel));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            String email = user.getEmail();

            // Write on map
            firebaseNewMarker = new FirebaseMarker(mGoogleMap, email, ll.latitude, ll.longitude);
        }
    }

    public void onSubmit(String name, String description, int ownCauses){

        //Confirm Position
        firebaseNewMarker.submitMarker(name, description);

        // Write on Firebase - "causes"

        DatabaseReference ref = mDatabase.child("causes").child(name).child("Info");
        ref.setValue(firebaseNewMarker).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(LOG, e.getLocalizedMessage());
            }
        });

//        Log.d(LOG, firebaseNewMarker.getName() + "\n"
//                + firebaseNewMarker.getDescription() + "\n"
//                + firebaseNewMarker.getLatitude() + "\n"
//                + firebaseNewMarker.getLongitude() + "\n"
//                + firebaseNewMarker.getOwner());


        // Write on Firebase - "users"
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userUid = mAuth.getCurrentUser().getUid();

        ref = mDatabase.child("users").child(userUid).child("MyCauses").child(name);
        ref.setValue(firebaseNewMarker).addOnFailureListener(new OnFailureListener() {
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

    public void onCancel(){
        firebaseNewMarker.cancelMarker();
    }

    public void onSearch(LatLng latLng){
        if(latLng != null)
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.312822f));
        else
            Toast.makeText(this.getContext(), "Adaugă o adresă validă", Toast.LENGTH_SHORT).show();
    }

}
