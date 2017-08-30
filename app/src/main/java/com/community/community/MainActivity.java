package com.community.community;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.util.LruCache;
import com.community.community.BeforeLogin.LoginActivity;
import com.community.community.General.BackPressedActivity;
import com.community.community.General.UsefulThings;
import com.community.community.CauseProfile.CausesActivity;
import com.community.community.GMaps.FirebaseImages;
import com.community.community.GMaps.FragmentGMaps;
import com.community.community.GMaps.SubmitCauseActivity;
import com.community.community.General.User;
import com.community.community.PublicProfile.PublicProfileActivity;
import com.community.community.Settings.AllSettingsActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements FragmentGMaps.OnBtnPressedListener {

    private String LOG = this.getClass().getSimpleName();

    /* Fragments */
    private FragmentGMaps mapFragment = null;

    /* Firebase */
    private FirebaseAuth mAuth = null;
    private DatabaseReference mDatabase = null;

    /* NavigationView */
    private DrawerLayout mDrawerLayout = null;
    private NavigationView mNavigationView = null;
    private CircleImageView mNavViewImage = null;

    /* Submit Buttons*/
    private Button saveBtn = null;
    private Button cancelBtn = null;
    private String causeName = null;
    private String causeDescription = null;
    private Double lat = null;
    private Double lng = null;
    private FirebaseImages firebaseImages = null;

    /* Booleans */
    private boolean isRegistered = false;
    private boolean localIcon = false;

    private boolean mapTypeNormal = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Log.d(LOG, "======== onCreate");

//        UsefulThings.initNetworkListener();

        UsefulThings.causeCaches = new LruCache<>(UsefulThings.causeCacheSize);

         /* Firebase */
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        /* Fragments */
        mapFragment = (FragmentGMaps) getSupportFragmentManager().findFragmentById(R.id.map);

        /* Google Maps */
        ImageButton addBtn = (ImageButton) findViewById(R.id.add_marker);
        addBtn.setOnClickListener(callImageButtonClickListener);

        ImageButton searchBtn = (ImageButton) findViewById(R.id.search_marker);
        searchBtn.setOnClickListener(callImageButtonClickListener);

        /* NavigationView */
        ImageButton navBtn = (ImageButton) findViewById(R.id.edit_profile);
        navBtn.setOnClickListener(callImageButtonClickListener);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        /* Submit Button */
        saveBtn = (Button)findViewById(R.id.submit_marker);
        saveBtn.setOnClickListener(callImageButtonClickListener);
        cancelBtn = (Button)findViewById(R.id.cancel_marker);
        cancelBtn.setOnClickListener(callImageButtonClickListener);

        mNavigationViewListener();

        /* NavigationView Default */
        mNavViewImage = (CircleImageView) mNavigationView.getHeaderView(0).findViewById(R.id.profile_image);

        TextView mEmail = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.userEmail);
        mEmail.setText(mAuth.getCurrentUser().getEmail());

        /* Normal / Hybrid Buttons */
        ImageButton normalBtn = (ImageButton) findViewById(R.id.normal);
        normalBtn.setOnClickListener(callImageButtonClickListener);

        ImageButton hybridBtn = (ImageButton) findViewById(R.id.hybrid);
        hybridBtn.setOnClickListener(callImageButtonClickListener);

        verifyUserState();

//        Log.d(LOG, "Current user: " + UsefulThings.currentUser.getEmail());

        while(UsefulThings.currentUser != null) {
            Bitmap icon = getLocalIcon();
            if (localIcon) {
                mNavViewImage.setImageBitmap(icon);
            }
            break;
        }
    }

    private void mNavigationViewListener() {
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        switch (menuItem.getItemId()) {

                            case R.id.nav_account:
                                startActivity(new Intent(getApplicationContext(),
                                        PublicProfileActivity.class));
                                break;

                            case R.id.nav_settings:
                                startActivity(new Intent(getApplicationContext(),
                                        AllSettingsActivity.class));
                                break;

                            case R.id.nav_my_causes:
                                Intent in = new Intent(getApplicationContext(),
                                        CausesActivity.class);
                                in.putExtra("activity", UsefulThings.MY_CAUSES_ACTIVITY);
                                startActivity(in);
                                break;

                            case R.id.nav_supported_causes:
                                Intent in2 = new Intent(getApplicationContext(),
                                        CausesActivity.class);
                                in2.putExtra("activity", UsefulThings.MY_SUPPORTED_CAUSES_ACTIVITY);
                                startActivity(in2);
                                break;

                            case R.id.nav_all_causes:
                                Intent in3 = new Intent(getApplicationContext(),
                                        CausesActivity.class);
                                in3.putExtra("activity", UsefulThings.ALL_CAUSES_ACTIVITY);
                                startActivity(in3);
                                break;

                            case R.id.nav_members:
                                Intent in4 = new Intent(getApplicationContext(),
                                        NgoActivity.class);
                                startActivity(in4);
                                break;

                            case R.id.nav_proposals:
                                Intent in5 = new Intent(getApplicationContext(),
                                        ProposalsActivity.class);
                                startActivity(in5);
                                break;

                            case R.id.nav_logout:
                                mAuth.signOut();
                                UsefulThings.causeCaches = null;
                                startActivity(new Intent(getApplicationContext(),
                                        LoginActivity.class));
                                finish();
                                break;
                            default:
                                break;
                        }
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void verifyUserState() {
        Bundle extras = getIntent().getExtras();
//        Log.d(LOG, "extras: " + extras);
//        Log.d(LOG, "isRegistered: " + isRegistered);
        if(extras != null) {
            if(!isRegistered) {
                isRegistered = true;
                Boolean res = extras.getBoolean("isRegistred");
                Log.d(LOG, "res: " + res);
                if(res) {
                    writeNewUser();
                } else {
                    downloadUserDetails();
                }
            }
        }
    }

    private void downloadUserDetails() {
        Log.d(LOG, "downloadUserDetails!");

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid).child("ProfileSettings");

        dRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Object> data = (Map<String,Object>) snapshot.getValue();

                String nickname = data.get("nickname").toString();
                String email = data.get("email").toString();
                String status = data.get("status").toString();
                int ownCausesNumber = Integer.valueOf(data.get("ownCauses").toString());
                int supportedCausesNumber = Integer.valueOf(data.get("supportedCauses").toString());

                String imageName = null;
                if(snapshot.hasChild("imageName")) {
                    imageName = data.get("imageName").toString();
                }

                String imageURL = null;
                if(snapshot.hasChild("imageURL")) {
                    imageURL = data.get("imageURL").toString();
                }

                String describe = null;
                if(snapshot.hasChild("describe")) {
                    describe = data.get("describe").toString();
                }

                String address = null;
                if(snapshot.hasChild("address")) {
                    address = data.get("address").toString();
                }

                int age = 0;
                if(snapshot.hasChild("age")) {
                    age = Integer.valueOf(data.get("age").toString());
                }

                String official_address = null;
                if(snapshot.hasChild("official_address")) {
                    official_address = data.get("official_address").toString();
                }

                String website = null;
                if(snapshot.hasChild("site")) {
                    website = data.get("site").toString();
                }

                String donate = null;
                if(snapshot.hasChild("donate")) {
                    donate = data.get("donate").toString();
                }

                UsefulThings.currentUser = new User(nickname, email, describe, address, uid,
                        status, imageName, imageURL, age, ownCausesNumber, supportedCausesNumber,
                        official_address, website, donate);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //handle databaseError
            }
        });

        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Object> data = (Map<String,Object>) snapshot.getValue();
                if(data != null && data.containsKey("type")) {
                    String type = data.get("type").toString();
                    UsefulThings.currentUser.setType(type);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void writeNewUser() {
        final FirebaseUser user = mAuth.getCurrentUser();
        String email = user.getEmail();

        /* Firebase */
        final DatabaseReference rootRef = mDatabase.child("users")
                .child(user.getUid()).child("ProfileSettings");
        final DatabaseReference[] ref = {rootRef.child("status")};
        ref[0].setValue("active");
        ref[0] = rootRef.child("nickname");
        ref[0].setValue(email);
        ref[0] = rootRef.child("email");
        ref[0].setValue(email);
        ref[0] = rootRef.child("ownCauses");
        ref[0].setValue("0");
        ref[0] = rootRef.child("supportedCauses");
        ref[0].setValue("0");

        String nick = email.replace(".", "-");
        ref[0] = mDatabase.child("nicknames").child(nick);
        ref[0].setValue(email + "~" + user.getUid());

        /* UserPublicProfile */
        UsefulThings.currentUser = new User();
        UsefulThings.currentUser.setStatus("active");
        UsefulThings.currentUser.setNickname(email);
        UsefulThings.currentUser.setOwnCausesNumber(0);
        UsefulThings.currentUser.setSupportedCausesNumber(0);
        UsefulThings.currentUser.setUid(user.getUid());

        final DatabaseReference reference = mDatabase.child("users")
                .child(user.getUid()).child("NGODetails");
        reference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null) {
                            mDatabase.child("ngo").child(user.getUid()).setValue(user.getEmail());

                            ref[0] = rootRef.child("type");
                            ref[0].setValue("ngo");
                            UsefulThings.currentUser.setType("ngo");

                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                            String address = (String) map.get("address");
                            ref[0] = rootRef.child("official_address");
                            ref[0].setValue(address);
                            UsefulThings.currentUser.setOfficial_address(address);

                            String site = (String) map.get("site");
                            ref[0] = rootRef.child("site");
                            ref[0].setValue(site);
                            UsefulThings.currentUser.setSite(site);

                            reference.removeValue();
                        } else {
                            ref[0] = rootRef.child("type");
                            ref[0].setValue("user");
                            UsefulThings.currentUser.setType("user");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private CallImageButtonClickListener callImageButtonClickListener = new CallImageButtonClickListener();
    private class CallImageButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.edit_profile:
                    hideVirtualKeyboard();
                    if(!localIcon) {
                        Log.d(LOG, "No localIcon!");
                        setIcon();
                    } else {
                        mDrawerLayout.openDrawer(Gravity.START);
                    }

                    break;
                case R.id.search_marker:
                    onButtonPressed(1);
                    hideVirtualKeyboard();
                    break;
                case R.id.add_marker:
                    hideVirtualKeyboard();
                    setVisibility(View.VISIBLE);
                    onButtonPressed(2);
                    break;
                case R.id.submit_marker:
                    setVisibility(View.GONE);
                    Intent i = new Intent(getApplicationContext(), SubmitCauseActivity.class);
                    Bundle b = new Bundle();
                    b.putDouble("lat", mapFragment.getCurrentPosition().latitude);
                    b.putDouble("lng", mapFragment.getCurrentPosition().longitude);
                    i.putExtras(b);
                    startActivityForResult(i, 1);
                    break;
                case R.id.cancel_marker:
                    setVisibility(View.GONE);
                    onButtonPressed(3);
                    break;

                case R.id.normal:
                    if(mapTypeNormal) {
                        mapTypeNormal = false;
                        onButtonPressed(5);
                    }
                    break;

                case R.id.hybrid:
                    if(!mapTypeNormal) {
                        mapTypeNormal = true;
                        onButtonPressed(6);
                    }
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Bundle b = data.getExtras();
                if (b != null) {
                    causeName = b.getString("NameFiled");
                    causeDescription = b.getString("DescriptionFiled");
                    lat = b.getDouble("lat");
                    lng = b.getDouble("lng");
                    firebaseImages = (FirebaseImages) b.getSerializable("FirebaseImages");
                    onButtonPressed(4);
                } else {
                    Toast.makeText(getApplication(),
                            "Nu am putut salva informațiile", Toast.LENGTH_SHORT).show();
                }
            } else {
                onButtonPressed(3);
            }
        } else if (resultCode == Activity.RESULT_OK) {
            Bundle b = data.getExtras();
            if(b.getBoolean("result")) {
                finish();
            }
        }
//        } else if (requestCode == 2) {
//            if (resultCode == RESULT_OK) {
//                    Bitmap icon = getLocalIcon();
//                    mNavViewImage.setImageBitmap(icon);
//                } else {
//                    Log.d(LOG, "Nu am lucruri de schimbat!");
//                }
//            } else {
//                Log.d(LOG, "Nu am lucruri de schimbat!");
//            }
//        }
//        } else if (requestCode == 3){
//            Toast.makeText(this, "Am venit din fragment", Toast.LENGTH_SHORT).show();
//        }
    }

    public void onButtonPressed(int i) {

        if (mapFragment != null) {
            switch (i){
                case 1:
                    mapFragment.onSearch(onSearch());
                    break;
                case 2:
                    mapFragment.onAdd();
                    break;
                case 3:
                    mapFragment.cancelMarker();
                    break;
                case 4:
                    mapFragment.cancelMarker();
                    int ownCausesNumber = UsefulThings.currentUser.getOwnCausesNumber();
                    UsefulThings.currentUser.setOwnCausesNumber(ownCausesNumber + 1);
                    mapFragment.onSubmit(causeName, causeDescription, new LatLng(lat, lng),
                            firebaseImages, ownCausesNumber + 1);
                    break;
                case 5:
                    mapFragment.changeType(true);
                    break;
                case 6:
                    mapFragment.changeType(false);
                default:
                    break;
            }
        }
    }

    public LatLng onSearch() {
        EditText searchEditText = (EditText) findViewById(R.id.search);
        String location = searchEditText.getText().toString();

        List<Address> addList = null;

        if(!location.equals("")){
            Geocoder geoc = new Geocoder(this);

            try{
                addList = geoc.getFromLocationName(location, 1);
            }catch (IOException e){
                e.printStackTrace();
            }
            if(!addList.isEmpty()) {
                Address address = addList.get(0);
                return new LatLng(address.getLatitude(), address.getLongitude());
            }
        }
        return null;
    }

    /* ------------ Image Profile Section ------------ */
    public Bitmap getLocalIcon() {
        Bitmap icon = null;

        try {
            /* The image was saved locally */
            icon = BitmapFactory.decodeStream(MainActivity.this
                    .openFileInput("myImage_" + mAuth.getCurrentUser().getEmail()));
            localIcon = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return icon;
    }

    public void setIcon() {

        if(UsefulThings.currentUser.getImageURL() != null) {
            Log.d(LOG, "Caut imaginea pe Firebase!");
            /* Download Image */
            downloadImage();
        } else {
            Log.d(LOG, "Default!");
            mNavViewImage.setImageBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.drawable.profile));
            mDrawerLayout.openDrawer(Gravity.START);
        }
    }

    private void downloadImage() {

        Log.d(LOG, "downloadingImage...");

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Downloading profile image...");
        dialog.show();

        Glide
                .with(this)
                .load(UsefulThings.currentUser.getImageURL())
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        mNavViewImage.setImageBitmap(resource);
                        dialog.dismiss();
                        new saveImageLocal().execute(resource);
                        mDrawerLayout.openDrawer(Gravity.START);
                    }
                });
    }

    private class saveImageLocal extends AsyncTask<Bitmap, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            //        if(Build.VERSION.SDK_INT > 22){
//            checkPermission();
//        }
            String fileName = "myImage_" + UsefulThings.currentUser.getEmail();

            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                params[0].compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
                fo.write(bytes.toByteArray());
                fo.close();
                localIcon = true;
            } catch (Exception e) {
                e.printStackTrace();
//                fileName = null;
                Log.d(LOG, "--------Nu pot scrie!----------");
            }

            return null;
        }
    }
    /* ------------ End of Image Profile Section ------------ */

    public void hideVirtualKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void setVisibility(int visibility){
        saveBtn.setVisibility(visibility);
        cancelBtn.setVisibility(visibility);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapFragment = null;
        mAuth = null;
        mDatabase = null;
        mDrawerLayout = null;
        mNavigationView = null;
        mNavViewImage = null;
        saveBtn = null;
        cancelBtn = null;
        causeName = null;
        causeDescription = null;
        lat = null;
        lng = null;
        firebaseImages = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(UsefulThings.mNetworkStateIntentReceiver,
                UsefulThings.mNetworkStateChangedFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(UsefulThings.mNetworkStateIntentReceiver);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), BackPressedActivity.class);
        startActivityForResult(i, 100);
    }
}
