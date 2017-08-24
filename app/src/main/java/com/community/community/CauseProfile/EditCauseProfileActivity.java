package com.community.community.CauseProfile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.community.community.General.Cause;
import com.community.community.General.UsefulThings;
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

        /* Get causeId from CauseProfileActivity */
        causeId = getIntent().getStringExtra("causeId");
        ownerUID = getIntent().getStringExtra("ownerUID");

        Log.d(LOG, causeId);

        if(causeId != null) {
//            causeDetails = CauseProfileActivity.cacheCauses.get(causeId);
            causeDetails = UsefulThings.causeCaches.get(causeId);
        } else if (savedInstanceState != null) {
            Log.d(LOG, "Ajung aici!");
            causeDetails = new Cause();
            causeDetails.setName(savedInstanceState.getString("name"));
            causeDetails.setDescription(savedInstanceState.getString("describe"));
            causeDetails.setProfileImage((Bitmap) savedInstanceState.getParcelable("profileImage"));
            causeDetails.setOptionalImage1((Bitmap) savedInstanceState.getParcelable("optionalImage1"));
            causeDetails.setOptionalImage2((Bitmap) savedInstanceState.getParcelable("optionalImage2"));
        }
        setInitialDetails();
    }

    private void setInitialDetails() {

        name.setText(causeDetails.getName());
        describe.setText(causeDetails.getDescription());

        profileImage.setImageBitmap(causeDetails.getProfileImage());

        Bitmap img = causeDetails.getOptionalImage1();

        if(img != null) {
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

                        if(changedImage) {
                            updateFirebaseImages();
                        }
                        Intent i = new Intent();
                        i.putExtra("changed", changed);
                        i.putExtra("ownerUID", ownerUID);
                        i.putExtra("causeId", causeId);
                        setResult(RESULT_OK, i);
//                        Log.d(LOG, "AfterImage: " + userDetails.getProfileImage());
                        finish();
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
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 1);
                    break;

                case R.id.change_optional_image1:
                    if(Build.VERSION.SDK_INT > 22){
                        checkPermission();
                    }
                    startActivityForResult(new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 2);
                    break;

                case R.id.change_optional_image2:
                    if(Build.VERSION.SDK_INT > 22){
                        checkPermission();
                    }
                    startActivityForResult(new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 3);
                    break;

                default:
                    break;
            }
        }
    }

    private void updateFirebaseImages() {

        final ProgressDialog dialog = new ProgressDialog(this);

        /* Delete old image */
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(ownerUID).child("MyCauses").child(causeId).child("Images");
        Log.d(LOG, String.valueOf(rootRef));
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                final Map ref = (Map) snapshot.getValue();
                final String[] imageName = {null};

                /* Delete profile image */
                if(changedProfileImage != null) {
                    dialog.setTitle("Deleting old profile image...");
                    dialog.show();

                    imageName[0] = (String) ref.get("profileImageName");
                    Log.d(LOG, "++++++++ ImageName: " + imageName[0]);

                    final StorageReference[] desertRef = {FirebaseStorage.getInstance().getReference().child(UsefulThings.FB_STORAGE_PATH +
                            causeId + "/" + imageName[0])};
                    Log.d(LOG, "++++++++ desertRef: " + desertRef[0]);

                    desertRef[0].delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            desertRef[0] = FirebaseStorage.getInstance().getReference().child(UsefulThings.FB_STORAGE_PATH +
                                    causeId + "/thumbnail_" + imageName[0]);

                            desertRef[0].delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {

                                   /* Delete optional image 1 */
                                   if(changedOptionalImage1 != null){
                                       dialog.setTitle("Deleting old optional image 1...");
                                       dialog.show();

                                       imageName[0] = (String) ref.get("optionalImageName1");
                                       desertRef[0] = FirebaseStorage.getInstance().getReference().child(UsefulThings.FB_STORAGE_PATH +
                                               causeId + "/" + imageName[0]);

                                       desertRef[0].delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                           @Override
                                           public void onSuccess(Void aVoid) {

                                               desertRef[0] = FirebaseStorage.getInstance().getReference().child(UsefulThings.FB_STORAGE_PATH +
                                                       causeId + "/thumbnail_" + imageName[0]);

                                               desertRef[0].delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                   @Override
                                                   public void onSuccess(Void aVoid) {

                                                       /* Delete optionla image 2 */
                                                       if(changedOptionalImage2 != null) {
                                                           dialog.setTitle("Deleting old optional image 2...");
                                                           dialog.show();

                                                           imageName[0] = (String) ref.get("optionalImageName2");
                                                           desertRef[0] = FirebaseStorage.getInstance().getReference().child(UsefulThings.FB_STORAGE_PATH +
                                                                   causeId + "/" + imageName[0]);

                                                           desertRef[0].delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                               @Override
                                                               public void onSuccess(Void aVoid) {

                                                                   desertRef[0] = FirebaseStorage.getInstance().getReference().child(UsefulThings.FB_STORAGE_PATH +
                                                                           causeId + "/thumbnail_" + imageName[0]);

                                                                   desertRef[0].delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                       @Override
                                                                       public void onSuccess(Void aVoid) {
                                                                           dialog.dismiss();
                                                                           uploadImagesToFirebase(causeId);
                                                                       }
                                                                   });
                                                               }
                                                           });
                                                       } else {
                                                           dialog.dismiss();
                                                           uploadImagesToFirebase(causeId);
                                                       }
                                                   }
                                               });

                                           }
                                       }).addOnFailureListener(new OnFailureListener() {
                                           @Override
                                           public void onFailure(@NonNull Exception exception) {
                                               Log.d(LOG, "Imaginea nu exista!");
                                           }
                                       });

                                   } else {
                                       dialog.dismiss();
                                       uploadImagesToFirebase(causeId);
                                   }

                               }
                           });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d(LOG, "Imaginea nu exista!");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @SuppressWarnings("VisibleForTests")
    private void uploadImagesToFirebase(final String causeId) {

//        realNames = new ArrayList<>();

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Uploading profile image...");
        dialog.show();

        final String realName = getImageName(profileURI);

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(UsefulThings.FB_STORAGE_PATH +
                causeId + "/" + realName);
        Log.d(LOG, "Ref: " + ref);

        ref.putFile(profileURI)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        firebaseImages.setProfileImageURL(taskSnapshot.getDownloadUrl().toString());
//                        firebaseImages.setProfileImageName(realName);

//                        Uri uri = getCompressedImageUri(getApplicationContext(), profileBitmap);
                        Uri uri = getCompressedImageUri(getApplicationContext(), changedProfileImage);
                        uploadCompressedProfileImageToFirebase(uri, realName, dialog, causeId);
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
    private void uploadCompressedProfileImageToFirebase(Uri uri, String realName, final ProgressDialog dialog, final String causeId) {

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(UsefulThings.FB_STORAGE_PATH +
                causeId + "/thumbnail_" + realName);

        ref.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        firebaseImages.setProfileThumbnailURL(taskSnapshot.getDownloadUrl().toString());

                        if(optionalURI1 != null) {
                            dialog.setTitle("Uploading optional image 1...");
                            dialog.show();

                            uploadOptionalImage1(dialog, causeId);
                        } else if(optionalURI2 != null) {
                            dialog.setTitle("Uploading optional image 2...");
                            dialog.show();

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
    private void uploadOptionalImage1(final ProgressDialog dialog, final String causeId) {

        final String realName = getImageName(optionalURI1);

        StorageReference refOpt1 = FirebaseStorage.getInstance().getReference().child(UsefulThings.FB_STORAGE_PATH +
                causeId + "/" + realName);
        Log.d(LOG, "Ref: " + refOpt1);

        refOpt1.putFile(optionalURI1)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        firebaseImages.setOptionalImageURL1(taskSnapshot.getDownloadUrl().toString());
//                        firebaseImages.setOptionalImageName1(realName);

//                        Uri uri = getCompressedImageUri(getApplicationContext(), optionalBitmap1);
                        Uri uri = getCompressedImageUri(getApplicationContext(), changedOptionalImage1);
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
    private void uploadCompressedOptionalImage1ToFirebase(Uri uri, String realName, final ProgressDialog dialog, final String causeId) {

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(UsefulThings.FB_STORAGE_PATH +
                causeId + "/thumbnail_" + realName);

        ref.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        firebaseImages.setOptionalThumbnailURL1(taskSnapshot.getDownloadUrl().toString());

                        if (optionalURI2 != null) {

                            dialog.setTitle("Uploading optional image 2...");
                            dialog.show();

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

        StorageReference refOpt2 = FirebaseStorage.getInstance().getReference().child(UsefulThings.FB_STORAGE_PATH +
                causeId + "/" + realName);
        Log.d(LOG, "Ref: " + refOpt2);

        refOpt2.putFile(optionalURI2)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        firebaseImages.setOptionalImageURL2(taskSnapshot.getDownloadUrl().toString());
//                        firebaseImages.setOptionalImageName2(realName);

//                        Uri uri = getCompressedImageUri(getApplicationContext(), optionalBitmap2);
                        Uri uri = getCompressedImageUri(getApplicationContext(), changedOptionalImage2);
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
    private void uploadCompressedOptionalImage2ToFirebase(Uri uri, String realName, final ProgressDialog dialog, final String causeId) {

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(UsefulThings.FB_STORAGE_PATH +
                causeId + "/thumbnail_" + realName);

        ref.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        firebaseImages.setOptionalThumbnailURL2(taskSnapshot.getDownloadUrl().toString());

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

    public Uri getCompressedImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 60, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getImageName(Uri uri){
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
            case 1:
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

            case 2:
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

            case 3:
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
            causeDetails.setName(nick);
        }

        String des = verifyString(describe.getText().toString(), 20, 1300, 2);
        if(des == null) {
            return false;
        }

        if(!causeDetails.getDescription().equals(des)) {
            changed = true;
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
}


