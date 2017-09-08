package com.community.community.CauseProfile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.community.community.General.BackPressedActivity;
import com.community.community.General.Cause;
import com.community.community.General.UsefulThings;
import com.community.community.MainActivity;
import com.community.community.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.io.IOException;
import java.util.Map;

public class EditCauseProfileActivity extends AppCompatActivity {

    private String LOG = this.getClass().getSimpleName();

    /* Profile details */
    private EditText name = null;
    private EditText describe = null;

    /* Cause */
    private Cause causeDetails = null;
    private String causeId = null;
    private String ownerUID = null;
    private boolean changed = false;
    private boolean changedImage = false;
    private boolean changedText = false;
    private boolean existOptionalImage1 = false;

    /* Images Details */
    private ImageView profileImage = null;
    private ImageView changeProfileImage = null;

    private ImageView optionalImage1 = null;
    private ImageView changeOptionalImage1 = null;

    private ImageView optionalImage2 = null;
    private ImageView changeOptionalImage2 = null;

    private Bitmap changedProfileImage = null;
    private Bitmap changedOptionalImage1 = null;
    private Bitmap changedOptionalImage2 = null;

    private Uri profileURI = null;
    private Uri optionalURI1 = null;
    private Uri optionalURI2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_cause_profile_activity);

        Log.d(LOG, "====== onCreate");

        name = (EditText) findViewById(R.id.edit_name);
        describe = (EditText) findViewById(R.id.edit_describe);

        profileImage = (ImageView) findViewById(R.id.profile_image);
        changeProfileImage = (ImageView) findViewById(R.id.change_profile_image);
        changeProfileImage.setOnClickListener(callImageButtonClickListener);

        optionalImage1 = (ImageView) findViewById(R.id.optional_image1);
        changeOptionalImage1 = (ImageView) findViewById(R.id.change_optional_image1);
        changeOptionalImage1.setOnClickListener(callImageButtonClickListener);

        optionalImage2 = (ImageView) findViewById(R.id.optional_image2);
        changeOptionalImage2 = (ImageView) findViewById(R.id.change_optional_image2);
        changeOptionalImage2.setOnClickListener(callImageButtonClickListener);

        Button submitBtn = (Button) findViewById(R.id.submit_changes);
        submitBtn.setOnClickListener(callImageButtonClickListener);

        Button cancelBtn = (Button) findViewById(R.id.cancel_changes);
        cancelBtn.setOnClickListener(callImageButtonClickListener);

        Button deleteBtn = (Button) findViewById(R.id.delete);
        deleteBtn.setOnClickListener(callImageButtonClickListener);

        /* Get causeId from CauseProfileActivity */
        causeId = getIntent().getStringExtra("causeId");
        ownerUID = getIntent().getStringExtra("ownerUID");

        Log.d(LOG, causeId);

        if(causeId != null) {
//            causeDetails = CauseProfileActivity.cacheCauses.get(causeId);
            causeDetails = UsefulThings.CAUSE_CACHES.get(causeId);
        } else if (savedInstanceState != null) {
            Log.d(LOG, "Ajung aici!");
            causeDetails = new Cause();
            causeDetails.setName(savedInstanceState.getString("name"));
            causeDetails.setDescription(savedInstanceState.getString("describe"));
            causeDetails.setProfileImage((Bitmap) savedInstanceState.getParcelable("profileImage"));
            causeDetails.setOptionalImage1((Bitmap) savedInstanceState.getParcelable("optionalImage1"));
            causeDetails.setOptionalImage2((Bitmap) savedInstanceState.getParcelable("optionalImage2"));
            ownerUID = savedInstanceState.getString("ownerUid");
            causeId = savedInstanceState.getString("causeId");
        }
        setInitialDetails();

        Button backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                onBackPressed();
            }

        });
    }

    private void setInitialDetails() {

        name.setText(causeDetails.getName());
        describe.setText(causeDetails.getDescription());

        profileImage.setImageBitmap(causeDetails.getProfileImage());

        Bitmap img = causeDetails.getOptionalImage1();

        if(img != null) {
            existOptionalImage1 = true;
            optionalImage1.setImageBitmap(img);
        } else {
            optionalImage1.setImageResource(R.drawable.profile);
        }

        img = causeDetails.getOptionalImage2();

        if(img != null) {
            optionalImage2.setImageBitmap(img);
        } else {
            optionalImage2.setImageResource(R.drawable.profile);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG, "====== onSaveInstanceState");

        outState.putString("ownerUid", ownerUID);
        outState.putString("causeId", causeId);
        outState.putString("name", causeDetails.getName());
        outState.putString("describe", causeDetails.getDescription());
        outState.putParcelable("profileImage", causeDetails.getProfileImage());
        outState.putParcelable("optionalImage1", causeDetails.getProfileImage());
        outState.putParcelable("optionalImage2", causeDetails.getProfileImage());
    }

    private EditCauseProfileActivity.CallImageButtonClickListener callImageButtonClickListener = new EditCauseProfileActivity.CallImageButtonClickListener();
    private class CallImageButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.submit_changes:
                    if(verifyCauseDetails()) {

                        if(changedText) {
                            changedText = false;
                            updateTexts();
                        }

                        if(changedImage) {
                            changedImage = false;
                            updateFirebaseImages();
                        } else {
                            successfulIntent();
                        }
                    }
                    break;

                case R.id.cancel_changes:
                    Intent intent = new Intent();
                    intent.putExtra("changed", false);
                    setResult(RESULT_OK, intent);
                    finish();
                    break;

                case R.id.change_profile_image:
                    if(Build.VERSION.SDK_INT > 22){
                        checkPermission();
                    }
                    startActivityForResult(new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 11);
                    break;

                case R.id.change_optional_image1:
                    if(Build.VERSION.SDK_INT > 22){
                        checkPermission();
                    }
                    startActivityForResult(new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 12);
                    break;

                case R.id.change_optional_image2:
                    if(Build.VERSION.SDK_INT > 22){
                        checkPermission();
                    }
                    startActivityForResult(new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 13);
                    break;

                case R.id.delete:
                    final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
                    scrollView.setVisibility(View.GONE);

                    final PercentRelativeLayout relativeLayout = (PercentRelativeLayout)
                            findViewById(R.id.popUpLayout);
                    relativeLayout.setVisibility(View.VISIBLE);

                    final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

                    Button im_sure = (Button) findViewById(R.id.im_sure);
                    im_sure.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {
                            updateUsers();
                            updateCurrentUser();
                        }

                        private void updateCurrentUser() {
                            updateOwnNumber();
                            removeFirebaseImages();
                            removeCause();
                            Log.d(LOG, "Ajung aici!");
                            startActivity(new Intent(getApplicationContext(),
                                    MainActivity.class));
                            finish();
                        }

                        private void removeFirebaseImages() {

                            DatabaseReference dRef = mDatabase.child("users").child(ownerUID)
                                    .child("MyCauses").child(causeId).child("Images");
                            dRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.getValue() != null) {
                                        Map<String, Object> map = (Map<String, Object>)
                                                dataSnapshot.getValue();

                                        FirebaseStorage ref = FirebaseStorage.getInstance();

                                        if(map.containsKey("profileImageURL")) {
                                            ref.getReferenceFromUrl(
                                                    (String) map.get("profileImageURL")).delete();
                                        }

                                        if(map.containsKey("profileThumbnailURL")) {
                                            ref.getReferenceFromUrl(
                                                    (String) map.get("profileThumbnailURL")).delete();
                                        }

                                        if(map.containsKey("optionalImageURL1")) {
                                            ref.getReferenceFromUrl(
                                                    (String) map.get("optionalImageURL1")).delete();
                                        }

                                        if(map.containsKey("optionalThumbnailURL1")) {
                                            ref.getReferenceFromUrl(
                                                    (String) map.get("optionalThumbnailURL1")).delete();
                                        }

                                        if(map.containsKey("optionalImageURL2")) {
                                            ref.getReferenceFromUrl(
                                                    (String) map.get("optionalImageURL2")).delete();
                                        }

                                        if(map.containsKey("optionalThumbnailURL2")) {
                                            ref.getReferenceFromUrl(
                                                    (String) map.get("optionalThumbnailURL2")).delete();
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            // Create a storage reference from our app
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                            // Create a reference to the file to delete
                            StorageReference desertRef = storageRef.child(UsefulThings.FB_STORAGE_USERS_PATH +
                                    UsefulThings.currentUser.getUid() + "/" + UsefulThings.currentUser.getImageName());
                            Log.d(LOG, "getImageName(): " + UsefulThings.currentUser.getImageName());

                            // Delete the file
                            desertRef.delete();
                        }

                        private void removeCause() {
                            mDatabase.child("users").child(ownerUID).child("MyCauses")
                                    .child(causeId).removeValue();
                            mDatabase.child("causes").child(causeId).removeValue();
                        }

                        private void updateOwnNumber() {
                            final DatabaseReference dRef = mDatabase.child("users").child(ownerUID)
                                    .child("ProfileSettings").child("ownCauses");
                            dRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if(snapshot != null){
                                        dRef.setValue((long) snapshot.getValue() - 1);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        private void updateUsers() {
                            DatabaseReference dRef = mDatabase.child("causes").child(causeId)
                                    .child("SupportedBy");
                            dRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if(snapshot != null){
                                        Map<String, Object> map =
                                                (Map<String, Object>) snapshot.getValue();

                                        for (Map.Entry<String, Object> entry : map.entrySet()) {

                                            if(!entry.getKey().equals("number")) {
                                                updateSupportedNumber(entry.getKey());
                                                removeSupportingCause(entry.getKey());
                                            }
                                        }
                                    }
                                }

                                private void removeSupportingCause(String key) {
                                    mDatabase.child("users").child(key).child("Supporting").
                                            child(causeId).removeValue();
                                }

                                private void updateSupportedNumber(String key) {
                                    final DatabaseReference dRef = mDatabase.child("users").child(key).
                                            child("ProfileSettings").child("supportedCauses");
                                    dRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {
                                            dRef.setValue((long) snapshot.getValue() - 1);
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
                    });

                    Button im_not_sure = (Button) findViewById(R.id.im_not_sure);
                    im_not_sure.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {
                            relativeLayout.setVisibility(View.GONE);
                            scrollView.setVisibility(View.VISIBLE);
                        }
                    });

                    break;

                default:
                    break;
            }
        }
    }

    private void updateTexts() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(ownerUID).child("MyCauses").child(causeId).child("Info");

        rootRef.child("name").setValue(causeDetails.getName());
        rootRef.child("description").setValue(causeDetails.getDescription());

        FirebaseDatabase.getInstance().getReference()
                .child("causes").child(causeId).child("Info").child("name")
                .setValue(causeDetails.getName());

    }

    private void updateFirebaseImages() {

        /* Delete old image */
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(ownerUID).child("MyCauses").child(causeId).child("Images");
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                final Map ref = (Map) snapshot.getValue();

                String imageName;
                /* Delete profile image */
                if(changedProfileImage != null) {
                    imageName = (String) ref.get("profileImageName");
                    new asyncImageUpload(imageName, UsefulThings.currentUser.getUid()).execute();
                }

                if(changedOptionalImage1 != null) {
                    imageName = (String) ref.get("optionalImageName1");
                    new asyncImageUpload(imageName, UsefulThings.currentUser.getUid()).execute();
                }

                if(changedOptionalImage2 != null) {
                    imageName = (String) ref.get("optionalImageName2");
                    new asyncImageUpload(imageName, UsefulThings.currentUser.getUid()).execute();
                }

                uploadImagesToFirebase(causeId);
            }

            class asyncImageUpload extends AsyncTask<String, Void, String> {

                private String name;
                private String uid;

                asyncImageUpload(String name, String uid){
                    this.name = name;
                    this.uid = uid;
                }

                @Override
                protected String doInBackground(String... params) {

                    removeOldImageFromFirebase("/");
                    removeOldImageFromFirebase("/thumbnail_");
                    return "Executed";
                }


                private void removeOldImageFromFirebase(String separator) {
                    FirebaseStorage.getInstance().getReference()
                            .child(UsefulThings.FB_STORAGE_USERS_PATH + uid + separator + name)
                            .delete();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @SuppressWarnings("VisibleForTests")
    private void uploadImagesToFirebase(final String causeId) {

//        Log.d(LOG, optionalImage1 + "\n" + optionalURI1 + "\n" + optionalURI2);
//        Log.d(LOG, changedOptionalImage1 + "\n" + changedOptionalImage2);
//
//        if(!existOptionalImage1 && optionalURI1 == null && optionalURI2 != null) {
//            optionalURI1 = optionalURI2;
//            optionalURI2 = null;
//            changedOptionalImage1 = changedOptionalImage2;
//            changedOptionalImage2 = null;
//        }
//        Log.d(LOG, optionalImage1 + "\n" + optionalURI1 + "\n" + optionalURI2);
//        Log.d(LOG, changedOptionalImage1 + "\n" + changedOptionalImage2);
//        realNames = new ArrayList<>();

        final ProgressDialog dialog = new ProgressDialog(this);
        if(changedProfileImage != null) {
            dialog.setTitle("Uploading profile image...");
            dialog.show();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);

            final String realName = getImageName(profileURI);

            StorageReference ref = FirebaseStorage.getInstance().getReference()
                    .child(UsefulThings.FB_STORAGE_PATH + causeId + "/" + realName);
            Log.d(LOG, ref.toString());
            ref.putFile(profileURI)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri uri = getCompressedImageUri(getApplicationContext(),
                                    changedProfileImage);

                            updateURLs(taskSnapshot, uri, "profileImageURL"
                                    , "profileImageName", false);

                            uploadCompressedProfileImageToFirebase(uri, realName,
                                    dialog, causeId);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            failureIntent(e);
                        }
                    });
        } else {
            if(optionalURI1 != null) {
                dialog.setTitle("Uploading optional image 1...");
                dialog.show();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);

                uploadOptionalImage1(dialog, causeId, optionalURI1, changedOptionalImage1);
            } else if(optionalURI2 != null) {

                if(!existOptionalImage1 && optionalURI1 == null) {
                    dialog.setTitle("Uploading optional image 1...");
                    dialog.show();
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    uploadOptionalImage1(dialog, causeId, optionalURI2, changedOptionalImage2);
                } else {
                    dialog.setTitle("Uploading optional image 2...");
                    dialog.show();
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    uploadOptionalImage2(dialog, causeId);
                }

            }
        }
    }

    @SuppressWarnings("VisibleForTests")
    private void updateURLs(UploadTask.TaskSnapshot taskSnapshot, Uri uri,
                            String imageURL, String imageName, boolean thumbnail) {

        String url = taskSnapshot.getDownloadUrl().toString();
        UsefulThings.currentUser.setImageURL(url);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = mDatabase.child("users").child(UsefulThings.currentUser.getUid())
                .child("MyCauses").child(causeId).child("Images");

        if(thumbnail) {
            ref.child(imageURL).setValue(url);
            if(imageName == null) {
                mDatabase.child("causes").child(causeId).child("Info")
                        .child("thumbnailImageURL").setValue(url);
            }
        } else {
            String realPath = UsefulThings.getRealPath(uri,
                    EditCauseProfileActivity.this);

            Uri file = Uri.fromFile(new File(realPath));

            ref.child(imageURL).setValue(url);
            ref.child(imageName).setValue(file.getLastPathSegment());
        }
    }

    @SuppressWarnings("VisibleForTests")
    private void uploadCompressedProfileImageToFirebase(final Uri uri, String realName,
                                                        final ProgressDialog dialog,
                                                        final String causeId) {

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child(UsefulThings.FB_STORAGE_PATH + causeId + "/thumbnail_" + realName);

        ref.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        updateURLs(taskSnapshot, uri, "profileThumbnailURL"
                                , null, true);

                        if(optionalURI1 != null) {
                            dialog.setTitle("Uploading optional image 1...");
                            dialog.show();
                            dialog.setCancelable(false);
                            dialog.setCanceledOnTouchOutside(false);

                            uploadOptionalImage1(dialog, causeId, optionalURI1,
                                    changedOptionalImage1);
                        } else if(optionalURI2 != null) {
                            dialog.setTitle("Uploading optional image 2...");
                            dialog.show();
                            dialog.setCancelable(false);
                            dialog.setCanceledOnTouchOutside(false);

                            if(!existOptionalImage1 && optionalURI1 == null) {
                                uploadOptionalImage1(dialog, causeId, optionalURI2,
                                        changedOptionalImage2);
                            } else {
                                uploadOptionalImage2(dialog, causeId);
                            }
                        } else {
                            dialog.dismiss();
                            successfulIntent();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        failureIntent(e);
                    }
                });
    }

    @SuppressWarnings("VisibleForTests")
    private void uploadOptionalImage1(final ProgressDialog dialog, final String causeId, Uri optUri,
                                      final Bitmap chOptImg) {

        final String realName = getImageName(optUri);

        StorageReference refOpt1 = FirebaseStorage.getInstance().getReference()
                .child(UsefulThings.FB_STORAGE_PATH + causeId + "/" + realName);
        Log.d(LOG, "Ref: " + refOpt1);

        refOpt1.putFile(optUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri uri = getCompressedImageUri(getApplicationContext(),
                                chOptImg);

                        updateURLs(taskSnapshot, uri, "optionalImageURL1"
                                , "optionalImageName1", false);

                        uploadCompressedOptionalImage1ToFirebase(uri, realName, dialog, causeId);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        failureIntent(e);
                    }
                });
    }

    @SuppressWarnings("VisibleForTests")
    private void uploadCompressedOptionalImage1ToFirebase(final Uri uri,
                                                          String realName,
                                                          final ProgressDialog dialog,
                                                          final String causeId) {

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child(UsefulThings.FB_STORAGE_PATH + causeId + "/thumbnail_" + realName);

        ref.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        updateURLs(taskSnapshot, uri, "optionalThumbnailURL1"
                                , "1", true);

                        if(optionalURI1 != null && optionalURI2 != null) {

                            dialog.setTitle("Uploading optional image 2...");
                            dialog.show();
                            dialog.setCancelable(false);
                            dialog.setCanceledOnTouchOutside(false);

                            uploadOptionalImage2(dialog, causeId);
                        } else {
                            dialog.dismiss();
                            successfulIntent();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        failureIntent(e);
                    }
                });
    }

    @SuppressWarnings("VisibleForTests")
    private void uploadOptionalImage2(final ProgressDialog dialog, final String causeId) {
        final String realName = getImageName(optionalURI2);

        StorageReference refOpt2 = FirebaseStorage.getInstance().getReference()
                .child(UsefulThings.FB_STORAGE_PATH + causeId + "/" + realName);

        refOpt2.putFile(optionalURI2)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri uri = getCompressedImageUri(getApplicationContext(),
                                changedOptionalImage2);

                        updateURLs(taskSnapshot, uri, "optionalImageURL2"
                                , "optionalImageName2", false);

                        uploadCompressedOptionalImage2ToFirebase(uri, realName, dialog, causeId);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        failureIntent(e);
                    }
                });
    }

    @SuppressWarnings("VisibleForTests")
    private void uploadCompressedOptionalImage2ToFirebase(final Uri uri,
                                                          String realName,
                                                          final ProgressDialog dialog,
                                                          final String causeId) {

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child(UsefulThings.FB_STORAGE_PATH + causeId + "/thumbnail_" + realName);

        ref.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        updateURLs(taskSnapshot, uri, "optionalThumbnailURL2"
                                , "1", true);

                        dialog.dismiss();
                        successfulIntent();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        failureIntent(e);
                    }
                });
    }

    private Uri getCompressedImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 60, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private String getImageName(Uri uri){
        String realPath = UsefulThings.getRealPath(uri, EditCauseProfileActivity.this);
        Uri file = Uri.fromFile(new File(realPath));

        realPath = file.getLastPathSegment();
//        realNames.add(realPath);
        return realPath;
    }

    private void successfulIntent(){

        Log.d(LOG, "SuccesfulIntent");
        Intent intent = new Intent();
        intent.putExtra("changed", changed);
        intent.putExtra("ownerUID", ownerUID);
        intent.putExtra("causeId", causeId);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void failureIntent(Exception e) {
        Log.d(LOG, e.getMessage());
//        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
//        removeImagesFromFirebase();
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(LOG, "requestCode: " + requestCode + "\nresultCode: " + resultCode);

        switch (requestCode) {
            case 11:
                if (resultCode == RESULT_OK) {

                    Uri imageUri = data.getData();
                    try {
                        Bitmap bitmap = UsefulThings.getThumbnail(imageUri, EditCauseProfileActivity.this);

                        if(bitmap != null) {
                            changeProfileImage.setImageBitmap(bitmap);
                            profileURI = imageUri;
                            changedProfileImage = bitmap;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case 12:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    try {
                        Bitmap bitmap = UsefulThings.getThumbnail(imageUri, EditCauseProfileActivity.this);

                        if(bitmap != null) {
                            changeOptionalImage1.setImageBitmap(bitmap);
                            optionalURI1 = imageUri;
                            changedOptionalImage1 = bitmap;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case 13:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    try {
                        Bitmap bitmap = UsefulThings.getThumbnail(imageUri, EditCauseProfileActivity.this);

                        if(bitmap != null) {
                            changeOptionalImage2.setImageBitmap(bitmap);
                            optionalURI2 = imageUri;
                            changedOptionalImage2 = bitmap;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case 100:
                if(resultCode == Activity.RESULT_OK){
                    Bundle b = data.getExtras();
                    if(b.getBoolean("result")) {
                        Intent intent = new Intent();
                        intent.putExtra("changed", false);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
                break;

            default:
                break;
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    123);

        }
    }

    private boolean verifyCauseDetails() {

        String nick = verifyString(name.getText().toString(), 5, 30, 1);
        if(nick == null) {
            return false;
        }

        if(!causeDetails.getName().equals(nick)) {
            changed = true;
            changedText = true;
            causeDetails.setName(nick);
        }

        String des = verifyString(describe.getText().toString(), 20, 1300, 2);
        if(des == null) {
            return false;
        }

        if(!causeDetails.getDescription().equals(des)) {
            changed = true;
            changedText = true;
            causeDetails.setDescription(des);
        }

        if(changedProfileImage != null) {// && !equals(icon, changedImage)){//!icon.sameAs(changedImage)){
            Log.d(LOG, "ProfileImage!");
            changed = true;
            changedImage = true;
            causeDetails.setProfileImage(changedProfileImage);
        }

        if(changedOptionalImage1 != null) {
            Log.d(LOG, "OptionalImage1!");
            changed = true;
            changedImage = true;
            causeDetails.setOptionalImage1(changedOptionalImage1);
        }

        if(changedOptionalImage2 != null) {
            Log.d(LOG, "OptionalImage2!");
            changed = true;
            changedImage = true;
            causeDetails.setOptionalImage1(changedOptionalImage2);
        }

        Log.d(LOG, "changed: " + changed);
        return true;
    }

    private String verifyString(String str, int min, int max, int forToast) {
        str = str.replaceAll("\\s+$", "");
        str = str.replaceAll("^\\s+", "");
        str = str.replace("\n", "").replace("\r", "");

        int len = str.length();
        if(len < min) {
            if(forToast == 1) {
                Toast.makeText(getApplicationContext(), "Nickname prea scurt!", Toast.LENGTH_SHORT).show();
            } else if(forToast == 2){
                Toast.makeText(getApplicationContext(), "Descriere prea scurtă!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Adresă prea scurtă!", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        if(len > max) {
            if(forToast == 1) {
                Toast.makeText(getApplicationContext(), "Nickname prea lung!", Toast.LENGTH_SHORT).show();
            } else if(forToast == 2){
                Toast.makeText(getApplicationContext(), "Descriere prea lungă!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Adresă prea lungă!", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        return str;
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
    public void onLowMemory() {
        Log.d(LOG, "onLowMemory");
        super.onLowMemory();
    }

    @Override
    protected void onStart() {
        Log.d(LOG, "onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(LOG, "onStop");
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), BackPressedActivity.class);
        i.putExtra("edit", "edit");
        startActivityForResult(i, 100);
    }
}


