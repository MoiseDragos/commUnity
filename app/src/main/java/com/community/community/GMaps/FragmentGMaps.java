package com.community.community.GMaps;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.community.community.CauseProfile.CauseProfileActivity;
import com.community.community.General.UsefulThings;
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
import java.util.Date;
import java.util.Map;

//TODO: No internet
//TODO: EditCause problems
//BackButton

public class FragmentGMaps extends Fragment implements GoogleMap.OnInfoWindowClickListener,
        OnMapReadyCallback {

    private class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;
        private ImageView image;

        MyInfoWindowAdapter(){
            myContentsView = getActivity().getLayoutInflater()
                    .inflate(R.layout.custom_infowindow, null);
            image = (ImageView) myContentsView.findViewById(R.id.image);
        }

        @Override
        public View getInfoContents(Marker marker) {

            final String url = marker.getSnippet();
            Log.d(LOG, "Before MarkerSnippet: " + url);
            Glide
                    .with(getActivity())
                    .load(url)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource,
                                                    GlideAnimation glideAnimation) {

                            Log.d(LOG, "MarkerSnippet: " + url);
                            image.setImageBitmap(resource);
                        }
                    });

            String tag = String.valueOf(marker.getTag());
            String[] parts = tag.split("~");


            TextView supported_number = (TextView)
                    myContentsView.findViewById(R.id.supported_number);
            supported_number.setText(parts[3]);

            TextView title = (TextView)myContentsView.findViewById(R.id.title);
            title.setText(marker.getTitle());

            TextView owner = (TextView)myContentsView.findViewById(R.id.owner);
//            tvSnippet.setText(marker.getSnippet());
            owner.setText(parts[4]);

            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

    }

    private String LOG = this.getClass().getSimpleName();
    
    private GoogleMap mGoogleMap = null;
    private MapView mMapView = null;
    private View mView = null;

    private DatabaseReference mDatabase = null;
    private FirebaseMarker firebaseNewMarker = null;

    private String type = null;

//    private ArrayList<FirebaseMarker> firebaseMarkers = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase =  FirebaseDatabase.getInstance().getReference();
        getTypeFromFirebase();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        setUpMap();

        mGoogleMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
        mGoogleMap.setOnInfoWindowClickListener(this);
        Log.d(LOG, "Maps initialization completed!");
    }

    public void changeType(boolean hybrid){
        if(hybrid) {
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else {
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    private void setUpMap() {
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mGoogleMap.moveCamera( CameraUpdateFactory.newLatLngZoom
                (new LatLng(45.92109159958021, 25.075808800756928) , 5.687861f) );

        addCausesOnMap();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        Log.d(LOG, "AICI!");
        String tag = String.valueOf(marker.getTag());
        String[] parts = tag.split("~");

        String causeId = parts[1] + "_" + parts[2];
        causeId = causeId.replace(".", "-");

        Intent intent = new Intent(getActivity(), CauseProfileActivity.class);
        intent.putExtra("ownerUID", parts[0]);
        intent.putExtra("causeId", causeId);
        intent.putExtra("type", type);
        getActivity().startActivityForResult(intent, 3);
    }

    private void addCausesOnMap() {

        mDatabase.addValueEventListener(new ValueEventListener() {
//        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("causes")) {
                    Map all = (Map) snapshot.getValue();
                    Map<String,Object> causes = (Map<String,Object>) all.get("causes");

//                    firebaseMarkers = new ArrayList<>();
                    Map singleCauseMap;

                    for (Map.Entry<String, Object> entry : causes.entrySet()) {

                        // Get cause from map
                        singleCauseMap = (Map) entry.getValue();
                        Map singleCause = (Map) singleCauseMap.get("Info");
                        Map ownCauses = (Map) singleCauseMap.get("SupportedBy");
                        final String ownCausesNumber;
                        if(ownCauses != null) {
                            ownCausesNumber = String.valueOf((long) ownCauses.get("number") + 1);
                        } else {
                            ownCausesNumber = "1";
                        }

                        final double lat = (Double) singleCause.get("latitude");
                        final double lng = (Double) singleCause.get("longitude");
                        final String ownerUID = (String) singleCause.get("ownerUID");
                        final String name = (String) singleCause.get("name");
                        String profileURL = (String) singleCause.get("thumbnailImageURL");
                        Log.d(LOG, "Name: " + name);
                        Log.d(LOG, "ProfileURL: " + profileURL);

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        if(user != null) {
                            Marker markerName;
                            String owner = (String) singleCause.get("owner");
                            if(owner.equals(user.getEmail())){
                                markerName = mGoogleMap.addMarker(
                                        new MarkerOptions().position(new LatLng(lat, lng))
                                        .icon(BitmapDescriptorFactory
                                                .fromResource(R.drawable.blue_pin_icon))
                                        .title(name));
                            } else {
                                markerName = mGoogleMap.addMarker(
                                        new MarkerOptions().position(new LatLng(lat, lng))
                                        .icon(BitmapDescriptorFactory
                                                .fromResource(R.drawable.green_pin_icon))
                                        .title(name));
                            }
                            markerName.setTag(ownerUID + "~" + lat + "~" + lng
                                    + "~" + ownCausesNumber + "~" + owner);
                            markerName.setSnippet(profileURL);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public interface OnBtnPressedListener {
        void onButtonPressed(int i);
    }

    public void onAdd(){
        // Remove unsubmited marker
        if(firebaseNewMarker != null)
            cancelMarker();

        LatLng ll = getCurrentPosition();

//        float zommLevel = mGoogleMap.getCameraPosition().zoom;
//        Log.d(LOG, "ZoomLevel: " + String.valueOf(zommLevel));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            // Write on map
            firebaseNewMarker = new FirebaseMarker(mGoogleMap, ll.latitude, ll.longitude);
        }
    }

    public void onSubmit(String name, String description, LatLng latLng,
                         FirebaseImages firebaseImages, int ownCauses){

        String email = UsefulThings.currentUser.getEmail();
        String userUid = UsefulThings.currentUser.getUid();

        Log.d(LOG, "Email: " + email);
        Log.d(LOG, "userUid: " + userUid);

        // Write on Firebase - "causes"
        firebaseNewMarker = new FirebaseMarker(email, userUid, latLng,
                name, firebaseImages.getProfileThumbnailURL(), 0);

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

        ref = mDatabase.child("users").child(userUid).child("MyCauses")
                .child(firebaseName).child("Info");
        ref.setValue(firebaseNewMarker).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(LOG, e.getLocalizedMessage());
            }
        });

        ref = mDatabase.child("users").child(userUid).child("MyCauses")
                .child(firebaseName).child("Images");
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

        /* Update membersNumber */
        if(type == null) {
            getTypeFromFirebase();
        }

        if(type != null && type.equals("user")){
            DatabaseReference rootRef = mDatabase.child("users")
                    .child(UsefulThings.currentUser.getUid()).child("MemberOf");
            rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    if(snapshot.getValue() != null) {
                        Map<String, Object> map = (Map<String, Object>) snapshot.getValue();

                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            updateMembers(entry.getKey());
                        }
                    }
                }

                private void updateMembers(String key) {
                    final DatabaseReference rootRef = mDatabase.child("users")
                            .child(key).child("ProfileSettings").child("membersCauses");
                    rootRef.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue() != null){
                                long membersCauses = (long) dataSnapshot.getValue();
                                rootRef.setValue(membersCauses + 1);
                            } else {
                                rootRef.setValue(String.valueOf(1));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
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

    private void getTypeFromFirebase(){

        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference reference = mDatabase.child("users").child(userUid)
                .child("ProfileSettings").child("type");
        reference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        type = String.valueOf(dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}
