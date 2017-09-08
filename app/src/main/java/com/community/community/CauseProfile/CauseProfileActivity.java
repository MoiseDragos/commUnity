package com.community.community.CauseProfile;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bluejamesbond.text.DocumentView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.util.LruCache;
import com.community.community.General.Cause;
import com.community.community.General.UsefulThings;
import com.community.community.General.User;
import com.community.community.PublicProfile.PublicProfileActivity;
import com.community.community.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.blurry.Blurry;

public class CauseProfileActivity extends AppCompatActivity {

    private String LOG = this.getClass().getSimpleName();

    private static final String CAUSE_ID = "cause_id";

    private PercentRelativeLayout buttonsLayout = null;

    private DatabaseReference mDatabase = null;

    /* Profile details */
    private ImageView blurImage = null;
    private ImageButton editBtn = null;
    private CircleImageView circleImage = null;
    private TextView nickname = null;
    private TextView causes_name = null;
    private TextView supportedNumber = null;
    private DocumentView describe = null;
    private int imagesNumber = -1;

    /* Buttons */
    private Button supportBtn = null;
    private Button noMoreSupportBtn = null;

    /* causeInfo */
    private Cause causeInfo = null;
    private long number = -1;
    private long currentSupportedNumber;
    private String ownerUID = null;
    private String causeId = null;

    /* Spinners */
    private ProgressBar spinnerProfile = null;
    private ProgressBar spinnerPic31 = null;
    private ProgressBar spinnerPic32 = null;
    private ProgressBar spinnerPic33 = null;
    private ProgressBar spinnerPic21 = null;
    private ProgressBar spinnerPic22 = null;
    private ProgressBar spinnerPic11 = null;

    private PercentRelativeLayout relative_layout_pic1 = null;
    private PercentRelativeLayout relative_layout_pic2 = null;
    private PercentRelativeLayout relative_layout_pic3 = null;

    private ImageView fullScreenContainer = null;
    private ImageView fullScreenContainerBlur = null;

    private Bitmap img1 = null;
    private Bitmap img2 = null;
    private Bitmap img3 = null;
//    private String currentUserType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cause_profile_activity);

        mDatabase = FirebaseDatabase.getInstance().getReference();

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

        buttonsLayout = (PercentRelativeLayout) findViewById(R.id.buttonsLayout);
        fullScreenContainer = (ImageView) findViewById(R.id.full_screen_container);
        fullScreenContainerBlur = (ImageView) findViewById(R.id.full_screen_container_blur);

        /* Edit Button */
        editBtn = (ImageButton) findViewById(R.id.edit_btn);
        editBtn.setOnClickListener(callImageButtonClickListener);

        if (UsefulThings.currentUser == null) {
            UsefulThings.currentUser = (User) savedInstanceState.getSerializable("userDetails");

            if(UsefulThings.currentUser == null) {
                Log.d(LOG, "Nu am detaliile user-ului curent!");
                finish();
            }
        }

        /* Get Data*/
        getCauseData();

        setSupportedNumberListener();

        Button backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                onBackPressed();
            }

        });
    }

    private void setSupportedNumberListener(){
        DatabaseReference ref = mDatabase.child("users")
                .child(UsefulThings.currentUser.getUid()).child("ProfileSettings")
                .child("supportedCauses");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(UsefulThings.currentUser != null) {
                    Log.d(LOG, "SupportedCauses: " + snapshot.getValue());
                    currentSupportedNumber = Long.valueOf(String.valueOf(snapshot.getValue()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getCauseData(){

        Intent intent = getIntent();
        Boolean removeIt = false;
        Boolean changed = intent.getBooleanExtra("changed", removeIt);
        ownerUID = intent.getStringExtra("ownerUID");
        causeId = intent.getStringExtra("causeId");

        Log.d(LOG, "ownerUID: " + ownerUID + "causeId: " + causeId);
//        currentUserType = intent.getStringExtra("type");

//        Cause cacheData = cacheCauses.get(causeId);
        Cause cacheData = null;

        if(UsefulThings.CAUSE_CACHES != null) {
            cacheData = UsefulThings.CAUSE_CACHES.get(causeId);
        }

        if(changed || cacheData == null) {
            Log.d(LOG, "FirebaseData");
            getFirebaseCauseData();
        } else {
            Log.d(LOG, "CacheData");
            setCauseData(cacheData);
        }
    }

    @Override
    public void onBackPressed() {

        Log.d(LOG, "======== onBackPressed");
        if (fullScreenContainer.getVisibility() == View.VISIBLE) {
            fullScreenContainer.setImageDrawable(null);
            fullScreenContainer.setVisibility(View.GONE);
            fullScreenContainerBlur.setImageDrawable(null);
            fullScreenContainerBlur.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {

        Log.d(LOG, "======== onSaveInstanceState");
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString(CAUSE_ID, causeId);
        savedInstanceState.putSerializable("userDetails", UsefulThings.currentUser);
//        savedInstanceState.putSerializable(CACHE, cacheCauses);
    }

    private void setCauseData(Cause cacheData){

        Log.d(LOG, "getDescription: " + cacheData.getDescription() +
                "\n" + "getName(): " + cacheData.getName());

        describe.setText(cacheData.getDescription());
        causes_name.setText(cacheData.getName());
        nickname.setText(cacheData.getOwner());

        Bitmap profileImage = cacheData.getProfileImage();

        blurImage.setImageBitmap(profileImage);
        Blurry.with(getApplicationContext())
//              .radius(50)
                .async()
                .from(profileImage)
                .into(blurImage);

        if(spinnerProfile != null) {
            spinnerProfile.setVisibility(View.GONE);
        }
        circleImage.setImageBitmap(profileImage);

        if(causeInfo == null) {
            causeInfo = new Cause(profileImage, ownerUID, cacheData.getDescription(),
                    cacheData.getName(), cacheData.getOwner());
        }

        switch (cacheData.getNumberOfPhotos()){
            case 1:
                /* One picture uploaded */
                if(relative_layout_pic1 == null) {
                    relative_layout_pic1 = (PercentRelativeLayout)
                            findViewById(R.id.relative_layout_pic1);
                    relative_layout_pic1.setVisibility(View.VISIBLE);
                }
                imagesNumber = 1;

                if(spinnerPic11 != null) {
                    spinnerPic11.setVisibility(View.GONE);
                }
                ImageView pic11 = (ImageView) findViewById(R.id.pic11);
                pic11.setImageBitmap(profileImage);
                setFullScreenListener(pic11, img1);


                break;
            case 2:
                /* Two pictures uploaded */
                if(relative_layout_pic2 == null) {
                    relative_layout_pic2 = (PercentRelativeLayout)
                            findViewById(R.id.relative_layout_pic2);
                    relative_layout_pic2.setVisibility(View.VISIBLE);
                }
                imagesNumber = 2;

                if(spinnerPic21 != null) {
                    spinnerPic21.setVisibility(View.GONE);
                }
                ImageView pic21 = (ImageView) findViewById(R.id.pic21);
                pic21.setImageBitmap(profileImage);
                setFullScreenListener(pic21, img1);


                if(spinnerPic22 != null) {
                    spinnerPic22.setVisibility(View.GONE);
                }
                ImageView pic22 = (ImageView) findViewById(R.id.pic22);
                pic22.setImageBitmap(cacheData.getOptionalImage1());
                setFullScreenListener(pic22, img2);

                causeInfo.setOptionalImage1(cacheData.getOptionalImage1());

                break;
            case 3:
                /* Three pictures uploaded */
                if(relative_layout_pic3 == null) {
                    relative_layout_pic3 = (PercentRelativeLayout)
                            findViewById(R.id.relative_layout_pic3);
                    relative_layout_pic3.setVisibility(View.VISIBLE);
                }
                imagesNumber = 3;

                if(spinnerPic31 != null) {
                    spinnerPic31.setVisibility(View.GONE);
                }
                ImageView pic31 = (ImageView) findViewById(R.id.pic31);
                pic31.setImageBitmap(cacheData.getOptionalImage1());
                setFullScreenListener(pic31, img2);

                if(spinnerPic32 != null) {
                    spinnerPic32.setVisibility(View.GONE);
                }
                ImageView pic32 = (ImageView) findViewById(R.id.pic32);
                pic32.setImageBitmap(profileImage);
                setFullScreenListener(pic32, img1);

                if(spinnerPic33 != null) {
                    spinnerPic33.setVisibility(View.GONE);
                }
                ImageView pic33 = (ImageView) findViewById(R.id.pic33);
                pic33.setImageBitmap(cacheData.getOptionalImage2());
                setFullScreenListener(pic33, img3);

                causeInfo.setOptionalImage1(cacheData.getOptionalImage1());
                causeInfo.setOptionalImage2(cacheData.getOptionalImage2());
        }

        setSupportedBtn();
    }

    private void setFullScreenListener(ImageView pic, final Bitmap img) {
        Log.d(LOG, "SetListener: " + img);
        if(img == null && causeInfo != null) {
            switch (imagesNumber) {
                case 1:
                    img1 = causeInfo.getProfileImage();
                    break;
                case 2:
                    img1 = causeInfo.getProfileImage();
                    img2 = causeInfo.getOptionalImage1();
                    break;
                case 3:
                    img1 = causeInfo.getProfileImage();
                    img2 = causeInfo.getOptionalImage1();
                    img3 = causeInfo.getOptionalImage2();
                    break;
                default:
                    break;
            }
        }
        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG, "IMG: " + img);
                if(img != null) {
                    fullScreenContainer.setImageBitmap(img);
                    fullScreenContainer.setVisibility(View.VISIBLE);


                    fullScreenContainerBlur.setImageBitmap(img);
                    fullScreenContainerBlur.setVisibility(View.VISIBLE);

                    Blurry.with(getApplicationContext())
                            .async()
                            .from(img)
                            .into(fullScreenContainerBlur);
                }
            }
        });
    }

    private void getFirebaseCauseData(){

        if(UsefulThings.CAUSE_CACHES == null) {
            UsefulThings.CAUSE_CACHES = new LruCache<>(UsefulThings.CAUSE_CACHE_SIZE);
        }

        spinnerProfile = (ProgressBar) findViewById(R.id.progressBarProfile);
        spinnerProfile.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(getApplicationContext()
                        , R.color.blue4), PorterDuff.Mode.SRC_IN );
        Log.d(LOG, "ProfileSpinner VISIBLE");
        spinnerProfile.setVisibility(View.VISIBLE);

        DatabaseReference rootRef = mDatabase.child("users").child(ownerUID)
                .child("MyCauses").child(causeId);
//        rootRef.addValueEventListener(new ValueEventListener() {
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map all = (Map) snapshot.getValue();

                /* Cause Info */
                Map ref = (Map) all.get("Info");
                final String description = (String) ref.get("description");
                final String name = (String) ref.get("name");
                final String owner = (String) ref.get("owner");
                final String profileImageName = (String) ref.get("profileImageName");

                /* Cause Images */
                ref = (Map) all.get("Images");
                final String profileURL = (String) ref.get("profileThumbnailURL");
                final String optionalURL1 = (String) ref.get("optionalThumbnailURL1");
                final String optionalURL2 = (String) ref.get("optionalThumbnailURL2");

                if(imagesNumber != -1) {
                    removeOldLayouts();
                }

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
                                        name, owner, profileURL, profileImageName, imagesNumber);

                                causeInfo.setProfileImage(resource);
                                img1 = resource;

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

                                                    img2 = resource;
                                                    final String refName2 = (String) finalRef.get("optionalImageURL2");

                                                    if(optionalURL2 != null){
                                                        causeInfo.setFirebaseImagesOptional2(optionalURL2, refName2);
                                                        Glide
                                                                .with(getApplication())
                                                                .load(optionalURL2)
                                                                .asBitmap()
                                                                .into(new SimpleTarget<Bitmap>() {
                                                                    @Override
                                                                    public void onResourceReady(Bitmap resource,
                                                                                                GlideAnimation glideAnimation) {
                                                                        causeInfo.setOptionalImage2(resource);

                                                                        img3 = resource;
//                                                                        cacheCauses.put(causeId, causeInfo);
                                                                        UsefulThings.CAUSE_CACHES.put(causeId, causeInfo);

                                                                        setCauseData(causeInfo);
                                                                        Log.d(LOG, "Glide");
                                                                    }
                                                                });
                                                    } else {
//                                                        cacheCauses.put(causeId, causeInfo);
                                                        UsefulThings.CAUSE_CACHES.put(causeId, causeInfo);

                                                        setCauseData(causeInfo);
                                                        Log.d(LOG, "Glide");
                                                    }

                                                }
                                            });
                                } else {
//                                    cacheCauses.put(causeId, causeInfo);
                                    UsefulThings.CAUSE_CACHES.put(causeId, causeInfo);

                                    setCauseData(causeInfo);
                                    Log.d(LOG, "Glide");
                                }
                            }
                        });
                Log.d(LOG, "Out of Glide");
            }

            private void removeOldLayouts() {

                Log.d(LOG, "removeOldLayouts");
                switch (imagesNumber) {
                    case 1:
                        relative_layout_pic1 = (PercentRelativeLayout)
                                findViewById(R.id.relative_layout_pic1);
                        relative_layout_pic1.setVisibility(View.GONE);
                        break;
                    case 2:
                        relative_layout_pic2 = (PercentRelativeLayout)
                                findViewById(R.id.relative_layout_pic2);
                        relative_layout_pic2.setVisibility(View.GONE);
                        break;
                    case 3:
                        relative_layout_pic3 = (PercentRelativeLayout)
                                findViewById(R.id.relative_layout_pic3);
                        relative_layout_pic3.setVisibility(View.GONE);
                        break;

                    default:
                        break;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setSupportedBtn(){
        final DatabaseReference dRef = mDatabase.child("causes")
                .child(causeId).child("SupportedBy");
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if(snapshot.getValue() != null) {
                    Map ref = (Map) snapshot.getValue();
                    number = (long) ref.get("number");

                    if (!UsefulThings.currentUser.getUid().equals(ownerUID)) {
                        buttonsLayout.setVisibility(View.VISIBLE);

                        Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                        boolean ok = false;

                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            if (entry.getKey().equals(UsefulThings.currentUser.getUid())) {
                                ok = true;
                                break;
                            }
                        }

                        if (!ok) {
                            noMoreSupportBtn.setVisibility(View.GONE);
                            supportBtn.setVisibility(View.VISIBLE);
                        } else {
                            noMoreSupportBtn.setVisibility(View.VISIBLE);
                            supportBtn.setVisibility(View.GONE);
                        }
                    } else {
                        buttonsLayout.setVisibility(View.GONE);
                        editBtn.setVisibility(View.VISIBLE);

                        PercentRelativeLayout csv_layout = (PercentRelativeLayout)
                                findViewById(R.id.csv_layout);
                        csv_layout.setVisibility(View.VISIBLE);
                        Button create_csv = (Button)
                                findViewById(R.id.create_csv);
                        create_csv.setOnClickListener(callImageButtonClickListener);
                    }

                    if (causeInfo != null) {
                        causeInfo.setSupportedBy(String.valueOf(number));
                    }
                    supportedNumber.setText(String.valueOf(number + 1));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setImageNumber(String optionalURL1, String optionalURL2) {

        if(optionalURL1 != null){
            if(optionalURL2 != null) {
                setSpinners(3);
                imagesNumber = 3;
            } else {
                setSpinners(2);
                imagesNumber = 2;
            }
        } else {
            if(optionalURL2 != null) {
                setSpinners(2);
                imagesNumber = 2;
            } else {
                setSpinners(1);
                imagesNumber = 1;
            }
        }
    }

    private void setSpinners(int i) {

        Log.d(LOG, "SetSpinners: " + i);
        switch (i) {
            case 1:
                PercentRelativeLayout relative_layout_pic1 = (PercentRelativeLayout)
                        findViewById(R.id.relative_layout_pic1);
                relative_layout_pic1.setVisibility(View.VISIBLE);

                spinnerPic11 = (ProgressBar) findViewById(R.id.progressBarPic11);
                spinnerPic11.getIndeterminateDrawable()
                        .setColorFilter(ContextCompat.getColor(getApplicationContext()
                                , R.color.blue4), PorterDuff.Mode.SRC_IN );
                spinnerPic11.setVisibility(View.VISIBLE);
                break;

            case 2:
                PercentRelativeLayout relative_layout_pic2 = (PercentRelativeLayout)
                        findViewById(R.id.relative_layout_pic2);
                relative_layout_pic2.setVisibility(View.VISIBLE);

                spinnerPic21 = (ProgressBar) findViewById(R.id.progressBarPic21);
                spinnerPic21.getIndeterminateDrawable()
                        .setColorFilter(ContextCompat.getColor(getApplicationContext()
                                , R.color.blue4), PorterDuff.Mode.SRC_IN );
                spinnerPic21.setVisibility(View.VISIBLE);

                spinnerPic22 = (ProgressBar) findViewById(R.id.progressBarPic22);
                spinnerPic22.getIndeterminateDrawable()
                        .setColorFilter(ContextCompat.getColor(getApplicationContext()
                                , R.color.blue4), PorterDuff.Mode.SRC_IN );
                spinnerPic22.setVisibility(View.VISIBLE);
                break;

            case 3:
                PercentRelativeLayout relative_layout_pic3 = (PercentRelativeLayout)
                        findViewById(R.id.relative_layout_pic3);
                relative_layout_pic3.setVisibility(View.VISIBLE);

                spinnerPic31 = (ProgressBar) findViewById(R.id.progressBarPic31);
                spinnerPic31.getIndeterminateDrawable()
                        .setColorFilter(ContextCompat.getColor(getApplicationContext()
                                , R.color.blue4), PorterDuff.Mode.SRC_IN );
                spinnerPic31.setVisibility(View.VISIBLE);

                spinnerPic32 = (ProgressBar) findViewById(R.id.progressBarPic32);
                spinnerPic32.getIndeterminateDrawable()
                        .setColorFilter(ContextCompat.getColor(getApplicationContext()
                                , R.color.blue4), PorterDuff.Mode.SRC_IN );
                spinnerPic32.setVisibility(View.VISIBLE);

                spinnerPic33 = (ProgressBar) findViewById(R.id.progressBarPic33);
                spinnerPic33.getIndeterminateDrawable()
                        .setColorFilter(ContextCompat.getColor(getApplicationContext()
                                , R.color.blue4), PorterDuff.Mode.SRC_IN );
                spinnerPic33.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }
    }

    public void onClick(View view) {

//        final ProgressDialog dialog = new ProgressDialog(this);
//        dialog.setTitle("Downloading details...");
//        dialog.show();

        Log.d(LOG, "ownerUid: " + ownerUID);

        DatabaseReference ref = mDatabase.child("users").child(ownerUID).child("ProfileSettings");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot snapshot) {

                   User mUser = new User();
                   Map mapRef = (Map) snapshot.getValue();

                   mUser.setNickname(String.valueOf(mapRef.get("nickname")));
                   mUser.setEmail(String.valueOf(mapRef.get("email")));
                   mUser.setType(String.valueOf(mapRef.get("type")));
                   mUser.setUid(ownerUID);
                   mUser.setOwnCausesNumber(Integer.valueOf(
                           String.valueOf(mapRef.get("ownCauses"))));
                   mUser.setSupportedCausesNumber(Integer.valueOf(
                           String.valueOf(mapRef.get("supportedCauses"))));

                   Object ref = mapRef.get("address");
                   if(ref != null) {
                       mUser.setAddress(String.valueOf(ref));
                   }

                   ref = mapRef.get("imageURL");
                   if(ref != null) {
                       mUser.setImageURL(String.valueOf(ref));
                   }

                   ref = mapRef.get("describe");
                   if(ref != null) {
                       mUser.setDescribe(String.valueOf(ref));
                   }

                   ref = mapRef.get("age");
                   if(ref != null) {
                       mUser.setAge(Integer.valueOf(String.valueOf(ref)));
                   }

//                   dialog.dismiss();
                   Intent i = new Intent(getApplicationContext(), PublicProfileActivity.class);
                   i.putExtra("userCauseDetails", mUser);
                   startActivity(i);
               }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
        });
    }

    private CauseProfileActivity.CallImageButtonClickListener callImageButtonClickListener =
            new CauseProfileActivity.CallImageButtonClickListener();
    private class CallImageButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.supportBtn:
                    support();
                    break;

                case R.id.noMoreSupportBtn:
                    unSupport();
                    break;

                case R.id.edit_btn:
                    Intent i = new Intent(getApplicationContext(), EditCauseProfileActivity.class);
                    i.putExtra("causeId", causeId);
                    i.putExtra("ownerUID", ownerUID);
                    startActivityForResult(i, 1);
                    break;

                case R.id.create_csv:
                    createCsvFile();
                    break;

                default:
                    break;
            }
        }
    }

    private void createCsvFile() {

        Log.d(LOG, "createCsvFile");
        final ArrayList<String> data = new ArrayList<>();

        data.add("Nume");
        data.add("Email");

        data.add(UsefulThings.currentUser.getNickname());
        data.add(UsefulThings.currentUser.getEmail());

        DatabaseReference dRef = mDatabase.child("causes")
                .child(causeId).child("SupportedBy");

        dRef.addListenerForSingleValueEvent(
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if(snapshot.getValue() != null) {
                        Map<String, Object> map = (Map<String, Object>)
                                snapshot.getValue();

                        int length = map.size();
                        int i = 0;
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            i++;
                            downloadDetails(entry.getKey(), i, length);
                        }
                    }
                }

                private void writeCSV() {

                    File fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                             + File.separator + "CommUnityDir" + File.separator + causeId);

//                    File fileDir = new File(getExternalCacheDir(), "CommUnityDir"
//                            + File.separator + causeId);

                    Log.d(LOG, "Dir: " + fileDir.toString());
                    boolean success = true;
                    if(!fileDir.exists()){
                        try{
                            success = fileDir.mkdir();
                        } catch (Exception e) {
                            Log.d(LOG, "AICI0");
                            e.printStackTrace();
                        }
                    }

                    Log.d(LOG, "AICI1" + success);

                    if(success) {

                        Log.d(LOG, "AICI12");
                        success = true;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

//                        Log.d()
//                        File file = new File(fileDir, sdf.format(timestamp) + ".csv");
//                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
//                                + File.separator +"CommUnityDir" + File.separator + causeId + ".txt");
                        File file = new File(getExternalCacheDir(), "CommUnityDir"
                                + File.separator + causeId + sdf.format(timestamp) + ".csv");
                        Log.d(LOG, "File: " + file.toString());
                        if (!file.exists()) {
                            try {
                                success = file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                        Log.d(LOG, "AICI2");
                        if(success) {
                            if (file.exists()) {
                                try {
                                    FileWriter fileWriter = new FileWriter(file);
                                    BufferedWriter bfWriter = new BufferedWriter(fileWriter);
                                    for(int i = 0; i < data.size(); i+=2) {
                                        bfWriter.write(data.get(i));
                                        bfWriter.write(",");
                                        bfWriter.write(data.get(i+1));
                                        bfWriter.write("\n");
                                    }
                                    Toast.makeText(getApplicationContext(),
                                            "Am salvat datele aici: " + file.toString(),
                                            Toast.LENGTH_LONG).show();
                                    alertDialog(file.toString());
                                    bfWriter.close();
                                } catch (IOException e) {

                                    Log.d(LOG, "AICI3");
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                private void alertDialog(String text) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(CauseProfileActivity.this);
                    dialog.setCancelable(false);
                    dialog.setTitle("Am salvat datele aici:");
                    dialog.setMessage(text);
                    dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //Action for "Delete".
                        }
                    });

                    final AlertDialog alert = dialog.create();
                    alert.show();
                }

                private void downloadDetails(String key,
                                             final int i, final int length) {

                    if(!key.equals("number")) {
                        DatabaseReference dRef = mDatabase.child("users")
                                .child(key).child("ProfileSettings");
                        dRef.addListenerForSingleValueEvent(
                                new ValueEventListener() {

                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Map<String, Object> map = (Map<String, Object>)
                                                dataSnapshot.getValue();

                                        String nick = map.get("nickname").toString();
                                        String email = map.get("email").toString();

                                        data.add(nick);
                                        data.add(email);

                                        if(i == length) {
                                            writeCSV();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    } else {
                        if(i == length) {
                            writeCSV();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Boolean removeIt = false;
                    if(data.getBooleanExtra("changed", removeIt)){
                        removeIt = true;
                        Log.d(LOG, "Au aparut schimbari! " + removeIt);
                        getFirebaseCauseData();
                    } else {
                        Log.d(LOG, "Nu au aparut schimbari!");
                    }
                }
                break;

            default:
                break;
        }
    }

    private void unSupport() {

        supportBtn.setClickable(false);
        noMoreSupportBtn.setClickable(false);

        final DatabaseReference dRef = mDatabase.child("causes")
                .child(causeId).child("SupportedBy");

        dRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if(snapshot.hasChild(UsefulThings.currentUser.getUid())){
                    dRef.child(UsefulThings.currentUser.getUid()).removeValue().addOnSuccessListener(
                            new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            DatabaseReference ref = mDatabase.child("users")
                                    .child(UsefulThings.currentUser.getUid()).child("Supporting");
                            ref.child(causeId).removeValue().addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    --number;
                                    DatabaseReference ref = mDatabase.child("causes")
                                            .child(causeId).child("SupportedBy").child("number");
                                    ref.setValue(number).addOnSuccessListener(
                                            new OnSuccessListener<Void>() {
                                         @Override
                                         public void onSuccess(Void aVoid) {
                                             DatabaseReference ref = mDatabase.child("users")
                                                     .child(ownerUID).child("MyCauses")
                                                     .child(causeId).child("Info").child("supportedBy");
                                             ref.setValue(number).addOnSuccessListener(
                                                     new OnSuccessListener<Void>() {
                                                 @Override
                                                 public void onSuccess(Void aVoid) {
                                                     DatabaseReference ref = mDatabase.child("users")
                                                             .child(UsefulThings.currentUser.getUid())
                                                             .child("ProfileSettings")
                                                             .child("supportedCauses");
                                                     ref.setValue(currentSupportedNumber - 1)
                                                             .addOnSuccessListener(
                                                             new OnSuccessListener<Void>() {
                                                                 @Override
                                                                 public void onSuccess(Void aVoid) {
                                                                     noMoreSupportBtn.setVisibility(View.GONE);
                                                                     supportBtn.setVisibility(View.VISIBLE);
                                                                     noMoreSupportBtn.setClickable(true);
                                                                     supportBtn.setClickable(true);
                                                                 }
                                                             });
                                                 }
                                             });
                                         }
                                     });
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void support() {

        noMoreSupportBtn.setClickable(false);
        supportBtn.setClickable(false);

        number = number + 1;
        final DatabaseReference[] ref = {mDatabase.child("causes")
                .child(causeId).child("SupportedBy")};
        ref[0].child("number").setValue(number).addOnSuccessListener(
                new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                DatabaseReference dRef = mDatabase.child("users")
                        .child(UsefulThings.currentUser.getUid()).child("Supporting");
                dRef.child(causeId).setValue(ownerUID)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                     @Override
                     public void onSuccess(Void aVoid) {
                         ref[0] = ref[0].child(UsefulThings.currentUser.getUid());
                         ref[0].setValue(ownerUID).addOnSuccessListener(
                                 new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                DatabaseReference ref = mDatabase.child("users")
                                        .child(ownerUID).child("MyCauses")
                                        .child(causeId).child("Info").child("supportedBy");
                                ref.setValue(number).addOnSuccessListener(
                                        new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        DatabaseReference ref = mDatabase.child("users")
                                                .child(UsefulThings.currentUser.getUid())
                                                .child("ProfileSettings").child("supportedCauses");
                                        ref.setValue(currentSupportedNumber + 1)
                                                .addOnSuccessListener(
                                                new OnSuccessListener<Void>() {
                                              @Override
                                              public void onSuccess(Void aVoid) {
                                                supportBtn.setVisibility(View.GONE);
                                                noMoreSupportBtn.setVisibility(View.VISIBLE);
                                                supportBtn.setClickable(true);
                                                noMoreSupportBtn.setClickable(true);
                                              }
                                          });
                                    }
                                });
                            }
                         });
                     }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d(LOG, "onResume");
        super.onResume();
        if(UsefulThings.mNetworkStateIntentReceiver == null ||
                UsefulThings.mNetworkStateChangedFilter == null) {
            UsefulThings.initNetworkListener();
        }
        registerReceiver(UsefulThings.mNetworkStateIntentReceiver,
                UsefulThings.mNetworkStateChangedFilter);
    }

    @Override
    protected void onPause() {
        Log.d(LOG, "onPause");
        super.onPause();
        unregisterReceiver(UsefulThings.mNetworkStateIntentReceiver);
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.d(LOG, "onStop");
        super.onStop();
    }
}