package com.community.community;

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
import com.community.community.BeforeLogin.LoginActivity;
import com.community.community.GMaps.FirebaseImages;
import com.community.community.GMaps.FragmentGMaps;
import com.community.community.GMaps.SubmitCauseActivity;
import com.community.community.General.User;
import com.community.community.PublicProfile.PublicProfileActivity;
import com.community.community.Settings.UserSettingsActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements FragmentGMaps.OnBtnPressedListener {

    // TODO: remove
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

    /* User */
    private User userPublicProfile = null;
    private static final String USER = "User";

    /* Booleans */
    private boolean isRegistered = false;
    private boolean localIcon = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //TODO: remove
        Log.d(LOG, "======== onCreate");

        //TODO: daca te intregistrezi de pe mai multe dispozitive in acelasi timp?
         /* Firebase */
        mAuth = FirebaseAuth.getInstance();
        new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                Log.d(LOG, "MainActivity");
                if (firebaseAuth.getCurrentUser() != null) {

                    finish();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));

                }

            }
        };

        mDatabase = FirebaseDatabase.getInstance().getReference();

        /* User */
        userPublicProfile = new User();

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

        if (savedInstanceState != null && savedInstanceState.containsKey(USER)) {
            Log.d(LOG, "Am salvat user!");
            userPublicProfile = (User) savedInstanceState.getSerializable(USER);
        } else {
            userPublicProfile.setUid(mAuth.getCurrentUser().getUid());
            verifyUserState();
        }

        Bitmap icon = getLocalIcon();
        if(localIcon) {
            mNavViewImage.setImageBitmap(icon);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        Log.d(LOG, "======== onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
//
//        if (savedInstanceState.containsKey(USER)) {
//            userPublicProfile = (User) savedInstanceState.getSerializable(USER);
//            icon = savedInstanceState.getParcelable(ICON);
//            mNavViewImage.setImageBitmap(icon);
//        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(USER, userPublicProfile);
    }


    private void mNavigationViewListener() {
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.nav_account:
                                Intent i = new Intent(getApplicationContext(), PublicProfileActivity.class);
                                i.putExtra("userDetails", userPublicProfile);
                                startActivityForResult(i, 2);
                                break;
                            case R.id.nav_settings:
                                Intent intent = new Intent(getApplicationContext(), UserSettingsActivity.class);
                                intent.putExtra("email", userPublicProfile.getEmail());
                                intent.putExtra("type", userPublicProfile.getType());
                                intent.putExtra("uid", userPublicProfile.getUid());
                                intent.putExtra("nickname", userPublicProfile.getNickname());
                                startActivity(intent);
//                                startActivity(new Intent(getApplicationContext(), SettingsActivity2.class));
                                break;
                            case R.id.nav_logout:
                                mAuth.signOut();
                                finish();
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                break;
                        }
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void verifyUserState() {
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            if(!isRegistered) {
                isRegistered = true;
                Boolean res = extras.getBoolean("isRegistred");
                Log.d(LOG, "Res: " + res.toString());
                if(res) {
                    /* Add new user in Firebase */
                    writeNewUser();
                } else {
                    userPublicProfile.updateFirebaseUserProfile();
                }
            } else {
                Log.d(LOG, "Nu am ce cauta aici! isRegistred == true");
            }
        } else {
            Log.d(LOG, "Nu am ce cauta aici! extras == null");
        }
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
                    break;
                case R.id.add_marker:
                    hideVirtualKeyboard();
                    setVisibility(View.VISIBLE);
                    onButtonPressed(2);
                    break;
                case R.id.submit_marker:
//                    Log.d(LOG, "Verify 1: " + mapFragment.verify());
                    setVisibility(View.GONE);
                    Intent i = new Intent(getApplicationContext(), SubmitCauseActivity.class);
                    Bundle b = new Bundle();
                    b.putDouble("lat", mapFragment.getCurrentPosition().latitude);
                    b.putDouble("lng", mapFragment.getCurrentPosition().longitude);
                    i.putExtras(b);
                    startActivityForResult(i, 1);
//                    Log.d(LOG, "Verify 2: " + mapFragment.verify());
                    break;
                case R.id.cancel_marker:
                    setVisibility(View.GONE);
                    onButtonPressed(3);
                    break;

                default:
                    break;
            }
        }
    }

    public void hideVirtualKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //TODO: another way
    private String causeName = null;
    private String causeDescription = null;
    private Double lat = null;
    private Double lng = null;
    private FirebaseImages firebaseImages = null;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG, "requestCode: " + requestCode + "\nresultCode: " + resultCode);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                Log.d(LOG, "Am un obiectiv de salvat!");
//                causeName = data.getStringExtra("NameFiled");
//                causeDescription = data.getStringExtra("DescriptionFiled");
                Bundle b = data.getExtras();
                if(b != null) {
                    causeName = b.getString("NameFiled");
                    causeDescription = b.getString("DescriptionFiled");
                    lat = b.getDouble("lat");
                    lng = b.getDouble("lng");
                    firebaseImages = (FirebaseImages) b.getSerializable("FirebaseImages");
                    onButtonPressed(4);
                } else {
                    Toast.makeText(getApplication(), "Nu am putut salva informațiile", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(LOG, "Nu am niciun obiectiv de salvat!");
                onButtonPressed(3);
            }
        } else if (requestCode == 2){
            if(resultCode == RESULT_OK) {
                Boolean ok = false;
                if (data.getBooleanExtra("changed", ok)) {
                    Log.d(LOG, "Am lucruri de schimbat!");
                    userPublicProfile.updateLocalUserProfile((User) data.getSerializableExtra("userDetails"));
                    Bitmap icon = getLocalIcon();
                    mNavViewImage.setImageBitmap(icon);
                } else {
                    Log.d(LOG, "Nu am lucruri de schimbat!");
                }
            } else {
                Log.d(LOG, "Nu am lucruri de schimbat!");
            }
        } else if (requestCode == 3){
            Toast.makeText(this, "Am venit din fragment", Toast.LENGTH_SHORT).show();
        }
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
                    int ownCausesNumber = userPublicProfile.getOwnCausesNumber();
                    userPublicProfile.setOwnCausesNumber(ownCausesNumber + 1);
                    mapFragment.onSubmit(causeName, causeDescription, new LatLng(lat, lng), firebaseImages, ownCausesNumber + 1);
                    break;
                default:
                    break;
            }
        } else {
            //TODO:
            Toast.makeText(this, "N-ar trebui sa fiu aici!", Toast.LENGTH_SHORT).show();
//            // Otherwise, we're in the one-pane layout and must swap frags...
//
//            // Create fragment and give it an argument for the selected article
//            FragmentGMaps newFragment = new FragmentGMaps();
//
//            fragmentTransaction = fragmentManager.beginTransaction();
//
//            // Replace whatever is in the fragment_container view with this fragment,
//            // and add the transaction to the back stack so the user can navigate back
//            fragmentTransaction.addToBackStack(null);
//
//            // Commit the transaction
//            fragmentTransaction.commit();
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

    public void setVisibility(int visibility){
        saveBtn.setVisibility(visibility);
        cancelBtn.setVisibility(visibility);
    }

    private void writeNewUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        String email = user.getEmail();

        /* UserPublicProfile */
        userPublicProfile.setStatus("active");
        userPublicProfile.setNickname(email);
        userPublicProfile.setEmail(email);
        userPublicProfile.setOwnCausesNumber(0);
        userPublicProfile.setSupportedCausesNumber(0);
        userPublicProfile.setType("user");

        /* Firebase */
        DatabaseReference rootRef = mDatabase.child("users").child(user.getUid()).child("ProfileSettings");
        DatabaseReference ref = rootRef.child("status");
        ref.setValue("active");
        ref = rootRef.child("nickname");
        ref.setValue(email);
        ref = rootRef.child("email");
        ref.setValue(email);
        ref = rootRef.child("ownCauses");
        ref.setValue("0");
        ref = rootRef.child("supportedCauses");
        ref.setValue("0");
        ref = rootRef.child("type");
        ref.setValue("user");
        String nick = email.replace(".", "-");
        ref = mDatabase.child("nicknames").child(nick);
        ref.setValue(email + "~" + user.getUid());
//        CircleImageView imageView = (CircleImageView) mapFragment.getView().findViewById(R.id.edit_profile);
//        int resourceImage = this.getResources().getIdentifier("profile", "drawable", this.getPackageName());
//        imageView.setImageResource(resourceImage);
    }

    public Bitmap getLocalIcon() {
        Bitmap icon = null;

        try {
            /* The image was saved locally */
            icon = BitmapFactory.decodeStream(MainActivity.this
                    .openFileInput("myImage_" + mAuth.getCurrentUser().getEmail()));
            Log.d(LOG, "Local image!");
            localIcon = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return icon;
    }

    public void setIcon() {

        Log.d(LOG, "userPublicProfile.getImageURL(): " + userPublicProfile.getImageURL());

        if(userPublicProfile.getImageURL() != null){
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
                .load(userPublicProfile.getImageURL())
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
            String fileName = "myImage_" + userPublicProfile.getEmail();//no .png or .jpg needed

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

    //TODO: remove

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG, "======== onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG, "======== onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(LOG, "======== onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG, "======== onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG, "======== onStart");
    }
}
