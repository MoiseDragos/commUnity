package com.community.community.CauseProfile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluejamesbond.text.DocumentView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.community.community.General.Cause;
import com.community.community.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.blurry.Blurry;

public class CauseProfileActivity extends AppCompatActivity {

    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    // TODO: timestamp pentru modificari!
    public static HashMap<String, Cause> cacheCauses = new HashMap<>();

//    public static final String FB_STORAGE_PATH = "images/";

    /* Profile details */
    private ImageView blurImage = null;
    private CircleImageView circleImage = null;
    private TextView nickname = null;
    private TextView causes_name = null;
    private TextView supportedNumber = null;
    private DocumentView describe = null;
    private int imagesNumber;

    /* Buttons */
    private Button saveBtn = null;
    private Button cancelBtn = null;
    private Button supportBtn = null;
    private Button noMoreSupportBtn = null;

    /* causeInfo */
    private Cause causeInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cause_profile_activity);

        /* Profile details */
        blurImage = (ImageView) findViewById(R.id.blur_profile_image);
        supportedNumber = (TextView) findViewById(R.id.supported_number);
        circleImage = (CircleImageView) findViewById(R.id.profile_image);
        nickname = (TextView) findViewById(R.id.userNickname);
        causes_name = (TextView) findViewById(R.id.causes_name);

        describe = (DocumentView) findViewById(R.id.describe_view);

        /* Support Buttons */
        supportBtn = (Button) findViewById(R.id.supportBtn);
        supportBtn.setOnClickListener(callImageButtonClickListener);
        noMoreSupportBtn = (Button) findViewById(R.id.noMoreSupportBtn);
        noMoreSupportBtn.setOnClickListener(callImageButtonClickListener);

        /* Submit Buttons */
        saveBtn = (Button)findViewById(R.id.submit_marker);
        saveBtn.setOnClickListener(callImageButtonClickListener);
        cancelBtn = (Button)findViewById(R.id.cancel_marker);
        cancelBtn.setOnClickListener(callImageButtonClickListener);

        /* Get Data*/
        getCauseData();

    }
    private void getCauseData(){

        Intent intent = getIntent();
        String ownerUID = intent.getStringExtra("ownerUID");
        String causeId = intent.getStringExtra("causeId");

        Cause cacheData = cacheCauses.get(causeId);

        if(cacheData != null){
            Log.d(LOG, "CacheData");
            setCauseData(cacheData);
        } else {
            Log.d(LOG, "FirebaseData");
            getFirebaseCauseData(ownerUID, causeId);
        }
    }

    private void setCauseData(Cause cacheData){

        //TODO: Pentru un cuvant foarte lung nu merge DocumentText.setText()....
        describe.setText(cacheData.getDescription());
        causes_name.setText(cacheData.getName());
        nickname.setText(cacheData.getOwner());
        supportedNumber.setText(cacheData.getSupportedBy());

        Bitmap profileImage = cacheData.getProfileImage();

        blurImage.setImageBitmap(profileImage);
        Blurry.with(getApplicationContext())
//              .radius(50)
                .async()
                .from(profileImage)
                .into(blurImage);

        circleImage.setImageBitmap(profileImage);

        switch (cacheData.getNumberOfPhotos()){
            case 1:
                /* One picture uploaded */
                android.support.percent.PercentRelativeLayout relative_layout_pic1 =
                        (android.support.percent.PercentRelativeLayout) findViewById(R.id.relative_layout_pic1);
                relative_layout_pic1.setVisibility(View.VISIBLE);
                imagesNumber = 1;

                ImageView pic11 = (ImageView) findViewById(R.id.pic11);
                pic11.setImageBitmap(profileImage);
                break;
            case 2:
                /* Two pictures uploaded */
                android.support.percent.PercentRelativeLayout relative_layout_pic2 =
                        (android.support.percent.PercentRelativeLayout) findViewById(R.id.relative_layout_pic2);
                relative_layout_pic2.setVisibility(View.VISIBLE);
                imagesNumber = 2;

                ImageView pic21 = (ImageView) findViewById(R.id.pic21);
                pic21.setImageBitmap(profileImage);

                ImageView pic22 = (ImageView) findViewById(R.id.pic22);
                pic22.setImageBitmap(cacheData.getOptionalImage1());
                break;
            case 3:
                /* Three pictures uploaded */
                android.support.percent.PercentRelativeLayout relative_layout_pic3 =
                        (android.support.percent.PercentRelativeLayout) findViewById(R.id.relative_layout_pic3);
                relative_layout_pic3.setVisibility(View.VISIBLE);
                imagesNumber = 3;

                ImageView pic31 = (ImageView) findViewById(R.id.pic31);
                pic31.setImageBitmap(cacheData.getOptionalImage1());

                ImageView pic32 = (ImageView) findViewById(R.id.pic32);
                pic32.setImageBitmap(profileImage);

                ImageView pic33 = (ImageView) findViewById(R.id.pic33);
                pic33.setImageBitmap(cacheData.getOptionalImage2());
        }

    }

    private void getFirebaseCauseData(final String ownerUID, final String causeId){

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(ownerUID).child("MyCauses").child(causeId);//.child("Info");
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map all = (Map) snapshot.getValue();

                /* Cause Info */
                Map ref = (Map) all.get("Info");
//                String date = (String) info.get("date");
                final String description = (String) ref.get("description");
//                double lat = (double) info.get("latitude");
//                double lng = (double) info.get("longitude");
                final String name = (String) ref.get("name");
                final String owner = (String) ref.get("owner");
                final String supportedString = String.valueOf(ref.get("supportedBy"));
                final String profileImageName = (String) ref.get("profileImageName");

                /* Cause Images */
                ref = (Map) all.get("Images");
                final String profileURL = (String) ref.get("profileThumbnailURL");
                final String optionalURL1 = (String) ref.get("optionalThumbnailURL1");
                final String optionalURL2 = (String) ref.get("optionalThumbnailURL2");
//                Log.d(LOG, "ProfileURL: " + profileURL);
//                Log.d(LOG, "opt1URL: " + optionalURL1);
//                Log.d(LOG, "opt2URL: " + optionalURL2);

                setImageNumber(optionalURL1, optionalURL2);
                final Map finalRef = ref;

                Glide
                        .with(getApplication())
                        .load(profileURL)
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                causeInfo = new Cause(resource, ownerUID, description,
                                        name, owner, supportedString, profileURL, profileImageName, imagesNumber);

                                causeInfo.setProfileImage(resource);

                                final String refName1 = (String) finalRef.get("optionalImageURL1");

                                if(optionalURL1 != null){
                                    causeInfo.setFirebaseImagesOptional1(optionalURL1, refName1);
                                    Glide
                                            .with(getApplication())
                                            .load(optionalURL1)
                                            .asBitmap()
                                            .into(new SimpleTarget<Bitmap>() {
                                                @Override
                                                public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                                    causeInfo.setOptionalImage1(resource);

                                                    final String refName2 = (String) finalRef.get("optionalImageURL2");

                                                    if(optionalURL2 != null){
                                                        causeInfo.setFirebaseImagesOptional2(optionalURL2, refName2);
                                                        Glide
                                                                .with(getApplication())
                                                                .load(optionalURL2)
                                                                .asBitmap()
                                                                .into(new SimpleTarget<Bitmap>() {
                                                                    @Override
                                                                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                                                        causeInfo.setOptionalImage2(resource);

                                                                        //TODO: daca obiectivul este eliminat de un alt utilizator?
                                                                        cacheCauses.put(causeId, causeInfo);

                                                                        setCauseData(causeInfo);
                                                                        Log.d(LOG, "Glide");
                                                                    }
                                                                });
                                                    } else {
                                                        cacheCauses.put(causeId, causeInfo);

                                                        setCauseData(causeInfo);
                                                        Log.d(LOG, "Glide");
                                                    }

                                                }
                                            });
                                } else {
                                    cacheCauses.put(causeId, causeInfo);

                                    setCauseData(causeInfo);
                                    Log.d(LOG, "Glide");
                                }
                            }
                        });
                Log.d(LOG, "Out of Glide");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO
            }
        });
    }

    private void setImageNumber(String optionalURL1, String optionalURL2) {

        if(optionalURL1 != null){
            if(optionalURL2 != null) {
                imagesNumber = 3;
            } else {
                imagesNumber = 2;
            }
        } else {
            if(optionalURL2 != null) {
                imagesNumber = 2;
            } else {
                imagesNumber = 1;
            }
        }
    }


//    private void setImageLayoutVisibility(String optionalURL1, String optionalURL2) {
//
//        if(optionalURL1 != null){
//            if(optionalURL2 != null) {
//                android.support.percent.PercentRelativeLayout relative_layout_pic3 =
//                        (android.support.percent.PercentRelativeLayout) findViewById(R.id.relative_layout_pic3);
//                relative_layout_pic3.setVisibility(View.VISIBLE);
//                imagesNumber = 3;
//            } else {
//                android.support.percent.PercentRelativeLayout relative_layout_pic2 =
//                        (android.support.percent.PercentRelativeLayout) findViewById(R.id.relative_layout_pic2);
//                relative_layout_pic2.setVisibility(View.VISIBLE);
//                imagesNumber = 2;
//            }
//        } else {
//            if(optionalURL2 != null) {
//                android.support.percent.PercentRelativeLayout relative_layout_pic2 =
//                        (android.support.percent.PercentRelativeLayout) findViewById(R.id.relative_layout_pic2);
//                relative_layout_pic2.setVisibility(View.VISIBLE);
//                imagesNumber = 2;
//            } else {
//                android.support.percent.PercentRelativeLayout relative_layout_pic1 =
//                        (android.support.percent.PercentRelativeLayout) findViewById(R.id.relative_layout_pic1);
//                relative_layout_pic1.setVisibility(View.VISIBLE);
//                imagesNumber = 1;
//            }
//        }
//    }

    private CauseProfileActivity.CallImageButtonClickListener callImageButtonClickListener = new CauseProfileActivity.CallImageButtonClickListener();

    public void onClick(View view) {

        //TODO: click pe nume

    }

    private class CallImageButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.supportBtn:
                    supportBtn.setVisibility(View.GONE);
                    noMoreSupportBtn.setVisibility(View.VISIBLE);
                    break;

                case R.id.noMoreSupportBtn:
                    noMoreSupportBtn.setVisibility(View.GONE);
                    supportBtn.setVisibility(View.VISIBLE);
                    break;

                case R.id.edit_btn:
//                    Intent i = new Intent(getApplicationContext(), EditCauseProfileActivity.class);
//                    i.putExtra("userDetails", userDetails);
//                    startActivityForResult(i, 1);
                    Toast.makeText(getApplication(), "EditBtn", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.submit_marker:
                    setBtnVisibility(View.GONE);
                    Toast.makeText(getApplication(), "SubmitMarker", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.cancel_marker:
                    setBtnVisibility(View.GONE);
                    Toast.makeText(getApplication(), "CancelMarker", Toast.LENGTH_SHORT).show();
                    finish();
                    break;

                default:
                    break;
            }
        }
    }

    public void setBtnVisibility(int visibility){
        saveBtn.setVisibility(visibility);
        cancelBtn.setVisibility(visibility);
    }
}