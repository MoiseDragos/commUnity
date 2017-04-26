package com.community.community.GMaps;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.community.community.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FragmentGMaps extends Fragment implements OnMapReadyCallback {

    OnAddPressedListener mCallback;

    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private View mView;

    private DatabaseReference mDatabase;
    private DatabaseReference users;
    private DatabaseReference causes;

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

        Log.d("GMaps", "Maps init completed!");
        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(44.439663, 26.096306)));
    }

    // Container Activity must implement this interface
    public interface OnAddPressedListener {
        void onAddPressed();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnAddPressedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }


    //TODO: DEFAULT RULES on firebase write / read (auth != null)

    public void addNewMarker(){
        LatLng ll = mGoogleMap.getCameraPosition().target;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            String email = user.getEmail();

            // Write on map
            FirebaseMarker fMarker = new FirebaseMarker("Nume", "Descriere", email, ll.latitude, ll.longitude);
            fMarker.addMarker(mGoogleMap, ll.latitude, ll.longitude);

            // Write on firebase
            Log.d("GMaps", "mDatabase: " + mDatabase.toString());


            //mDatabase = FirebaseDatabase.getInstance().getReference();
            causes = mDatabase.child("causes").child("Obj1");


            causes.setValue(fMarker).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("GMaps", e.getLocalizedMessage());
                }
            });

//            User firebaseUser = new User(email);
//
//            mDatabase.child("users").child("geageaeaea").setValue(firebaseUser);

            Log.d("GMaps", "CurrentUser:" + user.getEmail());
            Log.d("GMaps", "Lat:" + String.valueOf(ll.latitude));
            Log.d("GMaps", "Lon:" + String.valueOf(ll.longitude));
        }

//        DatabaseReference myRef = mDatabase.getReference("message");
//
//        myRef.setValue("Hello, World!");
//
//        // Read from the database
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                String value = dataSnapshot.getValue(String.class);
//                Log.d("GMaps", "Value is: " + value);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w("GMaps", "Failed to read value.", error.toException());
//            }
//        });
    }
}
