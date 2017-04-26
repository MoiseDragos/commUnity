package com.community.community;

import android.app.FragmentManager;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.community.community.GMaps.FragmentGMaps;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements FragmentGMaps.OnAddPressedListener{

    /* Fragments */
    FragmentManager fragmentManager = null;

    /* Firebase */
    private FirebaseAuth mAuth = null;
    private FirebaseAuth.AuthStateListener mAuthListener = null;
    private FirebaseDatabase mDatabase = null;

    /* NavigationView */
    private DrawerLayout mDrawerLayout = null;
    private NavigationView mNavigationView = null;
    private ImageButton navBtn = null;
    private TextView mEmail = null;

    /* Google Maps */
    private ImageButton addBtn = null;
    FragmentGMaps mapFragment = null;

    private CallImageButtonClickListener callImageButtonClickListener = new CallImageButtonClickListener();
    private class CallImageButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {


            switch (view.getId()) {

                case R.id.add_marker:
                    onAddPressed();
                    break;

                case R.id.edit_profile:
                    mDrawerLayout.openDrawer(Gravity.START);
                    break;

                default:
                    break;
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

        /* Fragments */
        fragmentManager = getFragmentManager();

        /* Google Maps */
        addBtn = (ImageButton)findViewById(R.id.add_marker);
        addBtn.setOnClickListener(callImageButtonClickListener);

        /* NavigationView */
        navBtn = (ImageButton)findViewById(R.id.edit_profile);
        navBtn.setOnClickListener(callImageButtonClickListener);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

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


        FirebaseUser user = mAuth.getCurrentUser();
        mEmail = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.userEmail);
        mEmail.setText(user.getEmail());
   }

    public void onAddPressed() {
        mapFragment = (FragmentGMaps) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.addNewMarker();
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
}
