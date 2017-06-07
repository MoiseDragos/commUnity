package com.community.community;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.community.community.BeforeLogin.LoginActivity;
import com.community.community.GMaps.FragmentGMaps;
import com.community.community.GMaps.SubmitCauseActivity;
import com.community.community.General.User;
import com.community.community.PublicProfile.PublicProfileActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements FragmentGMaps.OnBtnPressedListener {

    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    /* Fragments */
    private FragmentManager fragmentManager = null;
    private FragmentGMaps mapFragment = null;

    /* Firebase */
    private FirebaseAuth mAuth = null;
    private FirebaseAuth.AuthStateListener mAuthListener = null;
    private DatabaseReference mDatabase = null;
    public static final String FB_STORAGE_PATH = "images/";

    /* NavigationView */
    private DrawerLayout mDrawerLayout = null;
    private NavigationView mNavigationView = null;
    private ImageButton navBtn = null;
    private TextView mEmail = null;
    private CircleImageView mNavViewImage = null;

    /* Google Maps */
    private ImageButton addBtn = null;
    private ImageButton searchBtn = null;
    private EditText searchEditText = null;

    /* Submit Buttons*/
    private Button saveBtn = null;
    private Button cancelBtn = null;

    /* User */
    private User userPublicProfile = null;

    /* Registred */
    private boolean isRegistred = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

         /* Firebase */
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() != null){

                    finish();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));

                }

            }
        };

        mDatabase = FirebaseDatabase.getInstance().getReference();
//        user = new User(mAuth.getCurrentUser().getEmail(), mAuth.getCurrentUser().getUid());

        userPublicProfile = new User();
        userPublicProfile.setUid(mAuth.getCurrentUser().getUid());

        /* Add new user in Firebase */
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            if(!isRegistred) {
                isRegistred = true;
                Boolean res = extras.getBoolean("isRegistred");
                Log.d(LOG, "Res: " + res.toString());
                if(res) {
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

        /* Fragments */
        fragmentManager = getFragmentManager();
        mapFragment = (FragmentGMaps) getSupportFragmentManager().findFragmentById(R.id.map);

        /* Google Maps */
        addBtn = (ImageButton)findViewById(R.id.add_marker);
        addBtn.setOnClickListener(callImageButtonClickListener);

        searchBtn = (ImageButton)findViewById(R.id.search_marker);
        searchBtn.setOnClickListener(callImageButtonClickListener);

        /* NavigationView */
        navBtn = (ImageButton)findViewById(R.id.edit_profile);
        navBtn.setOnClickListener(callImageButtonClickListener);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        /* Submit Button */
        saveBtn = (Button)findViewById(R.id.submit_marker);
        saveBtn.setOnClickListener(callImageButtonClickListener);
        cancelBtn = (Button)findViewById(R.id.cancel_marker);
        cancelBtn.setOnClickListener(callImageButtonClickListener);

        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.nav_account:
                                Intent i = new Intent(getApplicationContext(), PublicProfileActivity.class);
                                i.putExtra("userDetails", userPublicProfile);
                                startActivityForResult(i, 2);
                                break;
                            case R.id.nav_settings:
                                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
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

        /* NavigationView Default */
        mNavViewImage = (CircleImageView) mNavigationView.getHeaderView(0).findViewById(R.id.profile_image);
        Bitmap icon = getIcon();
        mNavViewImage.setImageBitmap(icon);

        mEmail = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.userEmail);
        mEmail.setText(mAuth.getCurrentUser().getEmail());
    }

    // TODO: Remove hack!
//    private boolean imageIconBool = true;

    private CallImageButtonClickListener callImageButtonClickListener = new CallImageButtonClickListener();
    public class CallImageButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.edit_profile:
                    mDrawerLayout.openDrawer(Gravity.START);
//                    if(imageIconBool) {
//                        imageIconBool = false;
//                        Bitmap icon = getIcon();
//                        mNavViewImage.setImageBitmap(icon);
//                    }
                    break;
                case R.id.search_marker:
                    onButtonPressed(1);
                    break;
                case R.id.add_marker:
                    setVisibility(View.VISIBLE);
                    onButtonPressed(2);
                    break;
                case R.id.submit_marker:
                    setVisibility(View.GONE);
                    Intent i = new Intent(getApplicationContext(), SubmitCauseActivity.class);
                    startActivityForResult(i, 1);
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
//
//    private void checkPermission() {
//        if (ContextCompat.checkSelfPermission(this,
//                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
//                android.Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{
//                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                            android.Manifest.permission.READ_EXTERNAL_STORAGE},
//                    123);
//
//        }
//    }

    //TODO: another way
    private String causeName = null;
    private String causeDescription = null;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                causeName = data.getStringExtra("NameFiled");
                causeDescription = data.getStringExtra("DescriptionFiled");;
                onButtonPressed(4);
            } else {
                onButtonPressed(3);
            }
        } else if (requestCode == 2){
            if(resultCode == RESULT_OK) {
                Boolean ok = false;
                if (data.getBooleanExtra("changed", ok)) {
                    Log.d(LOG, "Am lucruri de schimbat!");
                    userPublicProfile.updateLocalUserProfile((User) data.getSerializableExtra("userDetails"));
                    Bitmap icon = getIcon();
                    mNavViewImage.setImageBitmap(icon);
                } else {
                    Log.d(LOG, "Nu am lucruri de schimbat!");
                }
            } else {
                Log.d(LOG, "Nu am lucruri de schimbat!");
            }
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
                    mapFragment.onCancel();
                    break;
                case 4:
                    int ownCausesNumber = userPublicProfile.getOwnCausesNumber();
                    userPublicProfile.setOwnCausesNumber(ownCausesNumber + 1);
                    mapFragment.onSubmit(causeName, causeDescription, ownCausesNumber + 1);
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
        searchEditText = (EditText) findViewById(R.id.search);
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


//        CircleImageView imageView = (CircleImageView) mapFragment.getView().findViewById(R.id.edit_profile);
//        int resourceImage = this.getResources().getIdentifier("profile", "drawable", this.getPackageName());
//        imageView.setImageResource(resourceImage);
    }

    public Bitmap getIcon() {
        Bitmap icon = null;

        try {
            /* The image was saved locally */
            icon = BitmapFactory.decodeStream(MainActivity.this
                    .openFileInput("myImage_" + mAuth.getCurrentUser().getEmail()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.drawable.profile);
        }

        return icon;
    }

    //TODO: from storage
    public Bitmap getIcon2() {
        Bitmap icon = null;

        boolean ok = true;
        try {
            /* The image was saved locally */
            icon = BitmapFactory.decodeStream(MainActivity.this
                    .openFileInput("myImage_" + mAuth.getCurrentUser().getEmail()));
            ok = false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(LOG, "Nu am imaginea local!");
        }

        if(ok && userPublicProfile.getImageURL() != null){
            Log.d(LOG, "Caut imaginea pe Firebase!");
            /* Download Image */
            icon = downloadImage();
            if(icon != null){
                mNavViewImage.setImageBitmap(icon);
                Log.d(LOG, "Am gasit imaginea!");
                ok = false;
                /* Save image locally */
                //TODO: Async
                createImageFromBitmap(icon);
                Log.d(LOG, "Am salvat local imaginea!");
            }
        }

        if(ok) {
            icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.drawable.profile);
        }

        return icon;
    }

    private Bitmap downloadImage() {
//        StorageReference ref = FirebaseStorage.getInstance().getReference().child(FB_STORAGE_PATH +
//                userPublicProfile.getUid() + "/" + userPublicProfile.getImageName());

        ImageView downloadedImageView = (ImageView)findViewById(R.id.downloaded_ImageView);
        downloadedImageView.setVisibility(View.GONE);
        Glide.with(getApplicationContext())
                .load(userPublicProfile.getImageURL())
                .into(downloadedImageView);

        downloadedImageView.buildDrawingCache();

        return downloadedImageView.getDrawingCache();
    }

    public String createImageFromBitmap(Bitmap bitmap) {

//        if(Build.VERSION.SDK_INT > 22){
//            checkPermission();
//        }

        String fileName = "myImage_" + userPublicProfile.getEmail();//no .png or .jpg needed

        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 1, bytes);
            FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
            Log.d(LOG, "--------Nu pot scrie!----------");
        }
        Log.d(LOG, "Filename: " + fileName);
        return fileName;
    }

}
