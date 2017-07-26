package com.community.community.CauseProfile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.blurry.Blurry;

public class CauseProfileActivity extends AppCompatActivity {

    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    // TODO: timestamp pentru modificari!
    // TODO: cacheCauses = null la schimbarea de user / intrarea in aplicatie!
//    public static HashMap<String, Cause> cacheCauses = new HashMap<>();
    public static LruCache<String, Cause> causeCaches = new LruCache<>(UsefulThings.cacheSize);
    private static final String CAUSE_ID = "cause_id";

//    public static final String FB_STORAGE_PATH = "images/";

    private DatabaseReference mDatabase = null;

    /* Profile details */
    private ImageView blurImage = null;
    private ImageButton editBtn = null;
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
    private long number = -1;
    private String ownerUID = null;
    private String causeId = null;
    private String currentUserUID = null;
    private String currentUserType = null;

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

        /* Edit Button */
        editBtn = (ImageButton) findViewById(R.id.edit_btn);
        editBtn.setOnClickListener(callImageButtonClickListener);

        /* Submit Buttons */
        saveBtn = (Button)findViewById(R.id.submit_marker);
        saveBtn.setOnClickListener(callImageButtonClickListener);
        cancelBtn = (Button)findViewById(R.id.cancel_marker);
        cancelBtn.setOnClickListener(callImageButtonClickListener);

        if(savedInstanceState != null && savedInstanceState.containsKey(CAUSE_ID)) {
            Log.d(LOG, "Am cache: " + savedInstanceState.getString(CAUSE_ID));
//            causeId = savedInstanceState.getString(CAUSE_ID);
        }

        /* Get Data*/
        getCauseData();

    }

    private void getCauseData(){

        Intent intent = getIntent();
        Boolean removeIt = false;
        Boolean changed = intent.getBooleanExtra("changed", removeIt);
        ownerUID = intent.getStringExtra("ownerUID");
        causeId = intent.getStringExtra("causeId");
        currentUserType = intent.getStringExtra("type");

//        Cause cacheData = cacheCauses.get(causeId);
        Cause cacheData = causeCaches.get(causeId);

        if(cacheData != null)
            Log.d(LOG, "cacheData: " + cacheData.toString());

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
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {

        Log.d(LOG, "======== onSaveInstanceState");
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString(CAUSE_ID, causeId);
//        savedInstanceState.putSerializable(CACHE, cacheCauses);
    }

    private void setCauseData(Cause cacheData){
        Log.d(LOG, "2");
        //TODO: Pentru un cuvant foarte lung nu merge DocumentText.setText()....
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

        circleImage.setImageBitmap(profileImage);

        if(causeInfo == null) {
            causeInfo = new Cause(profileImage, ownerUID, cacheData.getDescription(),
                    cacheData.getName(), cacheData.getOwner());
        }

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

                causeInfo.setOptionalImage1(cacheData.getOptionalImage1());

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

                causeInfo.setOptionalImage1(cacheData.getOptionalImage1());
                causeInfo.setOptionalImage2(cacheData.getOptionalImage2());
        }

        setSupportedBtn();
    }

    private void getFirebaseCauseData(){

        DatabaseReference rootRef = mDatabase.child("users").child(ownerUID)
                .child("MyCauses").child(causeId);
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
//                                                                        cacheCauses.put(causeId, causeInfo);
                                                                        causeCaches.put(causeId, causeInfo);

                                                                        setCauseData(causeInfo);
                                                                        Log.d(LOG, "Glide");
                                                                    }
                                                                });
                                                    } else {
//                                                        cacheCauses.put(causeId, causeInfo);
                                                        causeCaches.put(causeId, causeInfo);

                                                        setCauseData(causeInfo);
                                                        Log.d(LOG, "Glide");
                                                    }

                                                }
                                            });
                                } else {
//                                    cacheCauses.put(causeId, causeInfo);
                                    causeCaches.put(causeId, causeInfo);

                                    setCauseData(causeInfo);
                                    Log.d(LOG, "Glide");
                                }
                            }
                        });
                Log.d(LOG, "Out of Glide");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setSupportedBtn(){
        final DatabaseReference dRef = mDatabase.child("causes").child(causeId).child("SupportedBy");
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Map ref = (Map) snapshot.getValue();
                number = (long) ref.get("number");

                Log.d(LOG, "number: " + number);

                if(!currentUserUID.equals(ownerUID)){
                    Log.d(LOG, "AltUser!");

                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    boolean ok = false;

                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        if(entry.getKey().equals(currentUserUID)){
                            ok = true;
                            break;
                        }
                    }

                    if(!ok) {
                        noMoreSupportBtn.setVisibility(View.GONE);
                        supportBtn.setVisibility(View.VISIBLE);
                    } else {
                        noMoreSupportBtn.setVisibility(View.VISIBLE);
                        supportBtn.setVisibility(View.GONE);
                    }
                } else {
                    editBtn.setVisibility(View.VISIBLE);
                    Log.d(LOG, "Acelasi user!");
                }

                if(causeInfo != null) {
                    causeInfo.setSupportedBy(String.valueOf(number));
                }
                supportedNumber.setText(String.valueOf(number + 1));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

    public void onClick(View view) {

//        final ProgressDialog dialog = new ProgressDialog(this);
//        dialog.setTitle("Downloading details...");
//        dialog.show();

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
                   mUser.setOwnCausesNumber(Integer.valueOf(String.valueOf(mapRef.get("ownCauses"))));
                   mUser.setSupportedCausesNumber(Integer.valueOf(String.valueOf(mapRef.get("supportedCauses"))));

                   Object ref = mapRef.get("address");
                   if(ref != null) {
                       mUser.setAddress(String.valueOf(ref));
                   }

                   ref = mapRef.get("describe");
                   if(ref != null) {
                       mUser.setDescribe(String.valueOf(ref));
                   }

                   if(ref != null) {
                       mUser.setAge(Integer.valueOf(String.valueOf(mapRef.get("age"))));
                   }

//                   dialog.dismiss();
                   Intent i = new Intent(getApplicationContext(), PublicProfileActivity.class);
                   i.putExtra("userDetails", mUser);
                   i.putExtra("currentUserUid", currentUserUID);
                   i.putExtra("type", currentUserType);
                   startActivity(i);
               }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
        });




    }

    private CauseProfileActivity.CallImageButtonClickListener callImageButtonClickListener = new CauseProfileActivity.CallImageButtonClickListener();
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

        final DatabaseReference dRef = mDatabase.child("causes").child(causeId).child("SupportedBy");

        dRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if(snapshot.hasChild(currentUserUID)){
                    dRef.child(currentUserUID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            DatabaseReference ref = mDatabase.child("users").child(currentUserUID).child("Supporting");
                            ref.child(causeId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    --number;
                                    DatabaseReference ref = mDatabase.child("causes").child(causeId).child("SupportedBy").child("number");
                                    ref.setValue(number).addOnSuccessListener(new OnSuccessListener<Void>() {
                                         @Override
                                         public void onSuccess(Void aVoid) {
                                             DatabaseReference ref = mDatabase.child("users").child(ownerUID).child("MyCauses")
                                                     .child(causeId).child("Info").child("supportedBy");
                                             ref.setValue(number).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        final DatabaseReference[] ref = {mDatabase.child("causes").child(causeId).child("SupportedBy")};
        ref[0].child("number").setValue(number).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                DatabaseReference dRef = mDatabase.child("users").child(currentUserUID).child("Supporting");
                dRef.child(causeId).setValue(ownerUID).addOnSuccessListener(new OnSuccessListener<Void>() {
                     @Override
                     public void onSuccess(Void aVoid) {
                         ref[0] = ref[0].child(currentUserUID);
                         ref[0].setValue(ownerUID).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                DatabaseReference ref = mDatabase.child("users").child(ownerUID).child("MyCauses")
                                        .child(causeId).child("Info").child("supportedBy");
                                ref.setValue(number).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG, "======== onDestroy");
    }

    public void setBtnVisibility(int visibility){
        saveBtn.setVisibility(visibility);
        cancelBtn.setVisibility(visibility);
    }
}



//                    boolean ok = false;
//                    for(long i = 1; i <= number; i++){
//                        if(ref.get(String.valueOf(i)) != null && ref.get(String.valueOf(i)).equals(currentUserUID)){
//                            ok = true;
//                            break;
//                        }
//                    }
//
//                    if(!ok) {
//                        noMoreSupportBtn.setVisibility(View.GONE);
//                        supportBtn.setVisibility(View.VISIBLE);
//                    } else {
//                        noMoreSupportBtn.setVisibility(View.VISIBLE);
//                        supportBtn.setVisibility(View.GONE);
//                    }


//                Map all = (Map) snapshot.getValue();
//                Log.d(LOG, "number: " + number);
//                long pos = 0;
//                for(long i = 1; i <= number; i++){
//                    if(all.get(String.valueOf(i)).equals(currentUserUID)){
//                        pos = i;
//                        break;
//                    }
//                }
//
//                Log.d(LOG, "pos: " + pos);
//
//                if(pos != number) {
//                    String replace = String.valueOf(all.get(String.valueOf(number)));
//                    Log.d(LOG, "all.get(number): " + replace);
//
//                    dRef.child(String.valueOf(pos)).setValue(replace).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            removeLastElement(dRef);
//                        }
//                    });
//                } else {
//                    removeLastElement(dRef);
//                }
//
//            private void removeLastElement(final DatabaseReference dRef) {
//                dRef.child(String.valueOf(number)).getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        dRef.child("number").setValue(--number).addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                DatabaseReference ref = mDatabase.child("users").child(ownerUID).child("Supporting");
//                                ref.child(causeId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                  @Override
//                                  public void onSuccess(Void aVoid) {
//                                      noMoreSupportBtn.setVisibility(View.GONE);
//                                      supportBtn.setVisibility(View.VISIBLE);
//                                      noMoreSupportBtn.setClickable(true);
//                                      supportBtn.setClickable(true);
//                                  }
//                              });
//                            }
//                        });
//                    }
//                });
//            }