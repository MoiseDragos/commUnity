package com.community.community.CauseProfile;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluejamesbond.text.DocumentView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.community.community.GMaps.FirebaseMarker;
import com.community.community.General.Cause;
import com.community.community.General.User;
import com.community.community.PublicProfile.EditPublicProfileActivity;
import com.community.community.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.blurry.Blurry;

public class CauseProfileActivity extends AppCompatActivity {

    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    public static final String FB_STORAGE_PATH = "images/";

    /* Relative Layouts */
    private android.support.percent.PercentRelativeLayout percentRelativeLayoutDescribe = null;
    private android.support.percent.PercentRelativeLayout relative_layout_pic1 = null;
    private android.support.percent.PercentRelativeLayout relative_layout_pic2 = null;
    private android.support.percent.PercentRelativeLayout relative_layout_pic3 = null;

    /* Images */
    private ImageView pic11 = null;
    private ImageView pic21 = null;
    private ImageView pic22 = null;
    private ImageView pic31 = null;
    private ImageView pic32 = null;
    private ImageView pic33 = null;

    /* Profile details */
    private ImageView blurImage = null;
    private CircleImageView circleImage = null;
    private TextView nickname = null;
    private TextView causes_name = null;
    private TextView supportedNumber = null;
    private DocumentView describe = null;
    private ScrollView scrollView = null;
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

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        percentRelativeLayoutDescribe = (android.support.percent.PercentRelativeLayout) findViewById(R.id.relative_layout_3);
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

        /* Get */
        getFirebaseCauseData();

    }

    private void getFirebaseCauseData(){
        Intent intent = getIntent();
        final String ownerUID = intent.getStringExtra("ownerUID");
        final String causeId = intent.getStringExtra("causeId");

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

                Log.d(LOG, "Description:" + description);

                describe.setText(description);
                causes_name.setText(name);
                nickname.setText(owner);
                supportedNumber.setText(supportedString);

                /* Cause Images */
                ref = (Map) all.get("Images");
                final String profileURL = (String) ref.get("profileImageURL");
                final String optionalURL1 = (String) ref.get("optionalImageURL1");
                final String optionalURL2 = (String) ref.get("optionalImageURL2");

                setImageLayoutVisibility(optionalURL1, optionalURL2);

                final Map finalRef = ref;
                Glide
                        .with(getApplication())
                        .load(profileURL)
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                blurImage.setImageBitmap(resource);
                                Blurry.with(getApplicationContext())
//                                      .radius(50)
                                        .async()
                                        .from(resource)
                                        .into(blurImage);

                                circleImage.setImageBitmap(resource);

                                switch (imagesNumber){
                                    case 1:
                                        /* One picture uploaded */
                                        pic11 = (ImageView) findViewById(R.id.pic11);
                                        pic11.setImageBitmap(resource);
                                        break;
                                    case 2:
                                        /* Two pictures uploaded */
                                        pic21 = (ImageView) findViewById(R.id.pic21);
                                        pic22 = (ImageView) findViewById(R.id.pic22);
                                        pic21.setImageBitmap(resource);
                                        break;
                                    case 3:
                                        /* Three pictures uploaded */
                                        pic31 = (ImageView) findViewById(R.id.pic31);
                                        pic32 = (ImageView) findViewById(R.id.pic32);
                                        pic33 = (ImageView) findViewById(R.id.pic33);
                                        pic31.setImageBitmap(resource);
                                }

                                causeInfo = new Cause(resource, ownerUID, description,
                                        name, owner, supportedString, profileURL, profileImageName);

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

                                                    if(imagesNumber == 2){
                                                        pic22.setImageBitmap(resource);
                                                    } else {
                                                        pic32.setImageBitmap(resource);
                                                    }
                                                }
                                            });
                                }

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
                                                    causeInfo.setOptionalImage1(resource);

                                                    if(imagesNumber == 2){
                                                        pic22.setImageBitmap(resource);
                                                    } else {
                                                        pic33.setImageBitmap(resource);
                                                    }
                                                }
                                            });
                                }

                            }
                        });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO
            }
        });
    }

    private void setImageLayoutVisibility(String optionalURL1, String optionalURL2) {
        if(optionalURL1 != null){
            if(optionalURL2 != null) {
                relative_layout_pic3 = (android.support.percent.PercentRelativeLayout) findViewById(R.id.relative_layout_pic3);
                relative_layout_pic3.setVisibility(View.VISIBLE);
                imagesNumber = 3;
            } else {
                relative_layout_pic2 = (android.support.percent.PercentRelativeLayout) findViewById(R.id.relative_layout_pic2);
                relative_layout_pic2.setVisibility(View.VISIBLE);
                imagesNumber = 2;
            }
        } else {
            if(optionalURL2 != null) {
                relative_layout_pic2 = (android.support.percent.PercentRelativeLayout) findViewById(R.id.relative_layout_pic2);
                relative_layout_pic2.setVisibility(View.VISIBLE);
                imagesNumber = 2;
            } else {
                relative_layout_pic1 = (android.support.percent.PercentRelativeLayout) findViewById(R.id.relative_layout_pic1);
                relative_layout_pic1.setVisibility(View.VISIBLE);
                imagesNumber = 1;
            }
        }
    }

    private CauseProfileActivity.CallImageButtonClickListener callImageButtonClickListener = new CauseProfileActivity.CallImageButtonClickListener();
    public class CallImageButtonClickListener implements View.OnClickListener {
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