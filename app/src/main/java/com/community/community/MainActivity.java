package com.community.community;

import android.app.FragmentManager;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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
import android.widget.TextView;
import android.widget.Toast;

import com.community.community.BeforeLogin.LoginActivity;
import com.community.community.GMaps.FragmentGMaps;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    private DatabaseReference users;

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

    /* Registred */
    private boolean isRegistred = false;

    private CallImageButtonClickListener callImageButtonClickListener = new CallImageButtonClickListener();
    public class CallImageButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.edit_profile:
                    mDrawerLayout.openDrawer(Gravity.START);
                    break;
                case R.id.search_marker:
                    onButtonPressed(1);
                    break;
                case R.id.add_marker:
                    setVesibility(View.VISIBLE);
                    onButtonPressed(2);
                    break;
                case R.id.submit_marker:
                    setVesibility(View.GONE);

                    Intent i = new Intent(getApplicationContext(), SubmitCauseActivity.class);
                    startActivityForResult(i, 1);
                    break;
                case R.id.cancel_marker:
                    setVesibility(View.GONE);
                    onButtonPressed(3);
                    break;

                default:
                    break;
            }
        }
    }

    public void setVesibility(int visibility){
        saveBtn.setVisibility(visibility);
        cancelBtn.setVisibility(visibility);
    }

    //TODO: another way
    private String causeName = null;
    private String causeDescription = null;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG, "onActivityResult");
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                causeName = data.getStringExtra("NameFiled");
                causeDescription = data.getStringExtra("DescriptionFiled");;
                onButtonPressed(4);
            } else {
                onButtonPressed(3);
            }
        }
    }

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

        Bundle extras = getIntent().getExtras();
        if(extras != null && isRegistred == false) {
            isRegistred = true;
            Boolean res = extras.getBoolean("isRegistred");
            Log.d(LOG, res.toString());
            if(res) {
                writeNewUser();
            }
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
                                Toast.makeText(getApplicationContext(), "Account", Toast.LENGTH_SHORT).show();
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
        mNavViewImage.setImageDrawable(getResources().getDrawable(R.drawable.profile));

        mEmail = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.userEmail);
        mEmail.setText(mAuth.getCurrentUser().getEmail());
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
                    mapFragment.onSubmit(causeName, causeDescription);
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

    private void writeNewUser() {
        FirebaseUser user = mAuth.getCurrentUser();

        users = mDatabase.child("users").child(user.getUid()).child("ProfileSettings").child("status");
        users.setValue("active").addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(LOG, e.getLocalizedMessage());
            }
        });

//        CircleImageView imageView = (CircleImageView) mapFragment.getView().findViewById(R.id.edit_profile);
//        int resourceImage = this.getResources().getIdentifier("profile", "drawable", this.getPackageName());
//        imageView.setImageResource(resourceImage);
    }
}
