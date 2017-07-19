package com.community.community.GMaps;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.community.community.General.UsefulThings;
import com.community.community.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SubmitCauseActivity extends AppCompatActivity implements View.OnClickListener {

    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    private FirebaseImages firebaseImages;

    private double lat;
    private double lng;

    private EditText mCauseNameField;
    private EditText mDescriptionField;

    private ImageButton profileImageBtn;
    private ImageButton optionalImage1;
    private ImageButton optionalImage2;

    private Uri profileURI = null;
    private Uri optionalURI1 = null;
    private Uri optionalURI2 = null;

    private Bitmap profileBitmap = null;
    private Bitmap optionalBitmap1 = null;
    private Bitmap optionalBitmap2 = null;

    ArrayList<String> realNames = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submit_cause_activity);

        mCauseNameField = (EditText) findViewById(R.id.causes_name);
        mDescriptionField = (EditText) findViewById(R.id.describe_name);

        profileImageBtn = (ImageButton) findViewById(R.id.profile_image);
        profileImageBtn.setOnClickListener(this);
        optionalImage1 = (ImageButton) findViewById(R.id.pic21);
        optionalImage1.setOnClickListener(this);
        optionalImage2 = (ImageButton) findViewById(R.id.pic22);
        optionalImage2.setOnClickListener(this);

        Button mSubmitBtn = (Button) findViewById(R.id.submit_cause);
        mSubmitBtn.setOnClickListener(this);
        Button mCancelBtn = (Button) findViewById(R.id.cancel_cause);
        mCancelBtn.setOnClickListener(this);

        firebaseImages = new FirebaseImages();

        Bundle b = getIntent().getExtras();
        if(b != null) {
            lat = b.getDouble("lat");
            lng = b.getDouble("lng");
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.profile_image:
                checkPermission();

                Intent intent1 = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent1, 1);
                break;

            case R.id.pic21:
                checkPermission();
                Intent intent2 = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent2, 2);
                break;

            case R.id.pic22:
                checkPermission();

                Intent intent3 = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent3, 3);
                break;

            case R.id.submit_cause:
                if(verifyFields()) {
                    adjustURLs();
                    String causeId = lat + "_" + lng;
                    causeId = causeId.replace(".", "-");
                    uploadImagesToFirebase(causeId);
                }
                break;

            case R.id.cancel_cause:
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
                break;

            default:
                break;
        }
    }

    private void adjustURLs() {
        if(optionalURI2 != null && optionalURI1 == null){
            optionalURI1 = optionalURI2;
            optionalBitmap1 = optionalBitmap2;
            optionalURI2 = null;
            optionalBitmap2 = null;
        }
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
                        firebaseImages.setProfileImageURL(taskSnapshot.getDownloadUrl().toString());
                        firebaseImages.setProfileImageName(realName);

                        Uri uri = getCompressedImageUri(getApplicationContext(), profileBitmap);
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
                        firebaseImages.setProfileThumbnailURL(taskSnapshot.getDownloadUrl().toString());

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
                        firebaseImages.setOptionalImageURL1(taskSnapshot.getDownloadUrl().toString());
                        firebaseImages.setOptionalImageName1(realName);

                        Uri uri = getCompressedImageUri(getApplicationContext(), optionalBitmap1);
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
                        firebaseImages.setOptionalThumbnailURL1(taskSnapshot.getDownloadUrl().toString());

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
                        firebaseImages.setOptionalImageURL2(taskSnapshot.getDownloadUrl().toString());
                        firebaseImages.setOptionalImageName2(realName);

                        Uri uri = getCompressedImageUri(getApplicationContext(), optionalBitmap2);
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
                        firebaseImages.setOptionalThumbnailURL2(taskSnapshot.getDownloadUrl().toString());

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
        String realPath = getRealPath(uri);
        Uri file = Uri.fromFile(new File(realPath));

        realPath = file.getLastPathSegment();
//        realNames.add(realPath);
        return realPath;
    }

    public  String getRealPath(Uri uri){
        String realPath;

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        if(cursor != null && cursor.moveToFirst()){
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            realPath = cursor.getString(columnIndex);
            cursor.close();
        } else {
            realPath = uri.getPath();
        }
        return realPath;
    }

    private void successfulIntent(){

        Log.d(LOG, "SuccesfulIntent");
        Intent intent = new Intent();

        Bundle b = new Bundle();
        b.putDouble("lat", lat);
        b.putDouble("lng", lng);
        b.putString("NameFiled", mCauseNameField.getText().toString());
        b.putString("DescriptionFiled", mDescriptionField.getText().toString());
        b.putSerializable("FirebaseImages", firebaseImages);
        intent.putExtras(b);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void failureIntent(Exception e) {
        //TODO: stergem tot folder-ul de imagini (poate au reusit sa se adauge cateva)
        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        removeImagesFromFirebase();
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        /* Coordinates */
        if (savedInstanceState.containsKey(UsefulThings.LAT)) {
            lat = savedInstanceState.getDouble(UsefulThings.LAT);
        }


        if (savedInstanceState.containsKey(UsefulThings.LNG)) {
            lng = savedInstanceState.getDouble(UsefulThings.LNG);
        }

        if (savedInstanceState.containsKey(UsefulThings.PROFILE_URI_KEY)) {
            profileURI = Uri.parse(savedInstanceState.getString(UsefulThings.PROFILE_URI_KEY));
            try {
                profileBitmap = getThumbnail(profileURI);
                profileImageBtn.setImageBitmap(profileBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (savedInstanceState.containsKey(UsefulThings.OPTIONAL_URI_1_KEY)) {
            optionalURI1 = Uri.parse(savedInstanceState.getString(UsefulThings.OPTIONAL_URI_1_KEY));
            try {
                optionalBitmap1 = getThumbnail(optionalURI1);
                optionalImage1.setImageBitmap(optionalBitmap1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (savedInstanceState.containsKey(UsefulThings.OPTIONAL_URI_2_KEY)) {
            optionalURI2 = Uri.parse(savedInstanceState.getString(UsefulThings.OPTIONAL_URI_2_KEY));
            try {
                optionalBitmap2 = getThumbnail(optionalURI2);
                optionalImage2.setImageBitmap(optionalBitmap2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {

        /* Coordinates */
        savedInstanceState.putDouble(UsefulThings.LAT, lat);
        savedInstanceState.putDouble(UsefulThings.LNG, lng);

        /* Images */
        if(profileURI != null) {
            savedInstanceState.putString(UsefulThings.PROFILE_URI_KEY, profileURI.toString());
        }

        if(optionalURI1 != null) {
            savedInstanceState.putString(UsefulThings.OPTIONAL_URI_1_KEY, optionalURI1.toString());
        }

        if(optionalURI2 != null) {
            savedInstanceState.putString(UsefulThings.OPTIONAL_URI_2_KEY, optionalURI2.toString());
        }

        super.onSaveInstanceState(savedInstanceState);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            Log.d(LOG, "ImageURI: " + imageUri.toString());
            try {
                Bitmap bitmap = getThumbnail(imageUri);
                if(bitmap != null) {
                    if(requestCode == 1){
//                        Log.d(LOG, "Schimb imaginea de profil!");
                        profileImageBtn.setImageBitmap(bitmap);
                        profileURI = imageUri;
                        profileBitmap = bitmap;
                    } else if(requestCode == 2){
//                        Log.d(LOG, "Schimb imaginea opțională 1");
                        optionalImage1.setImageBitmap(bitmap);
                        optionalURI1 = imageUri;
                        optionalBitmap1 = bitmap;
                    } else {
//                        Log.d(LOG, "Schimb imaginea de profil!");
                        optionalImage2.setImageBitmap(bitmap);
                        optionalURI2 = imageUri;
                        optionalBitmap2 = bitmap;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean verifyFields() {

        String name = verifyString(mCauseNameField.getText().toString(), 5, 30, 1);
        if(name == null) {
            return false;
        }

        String des = verifyString(mDescriptionField.getText().toString(), 20, 1300, 2);
        if(des == null) {
            return false;
        }

        if(profileURI == null){
            Toast.makeText(getApplicationContext(), "Imagine de profil inexistentă!", Toast.LENGTH_SHORT).show();
            return false;
        }

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
            } else {
                Toast.makeText(getApplicationContext(), "Descriere prea scurtă!", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        if(len > max) {
            if(forToast == 1) {
                Toast.makeText(getApplicationContext(), "Nickname prea lung!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Descriere prea lungă!", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        return str;
    }

    public Bitmap getThumbnail(Uri uri) throws IOException{
        Bitmap bitmap;
        try {
            InputStream input = this.getContentResolver().openInputStream(uri);
            BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
            onlyBoundsOptions.inJustDecodeBounds = true;
            onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
            input.close();
            if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
                return null;
            }

            int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth)
                    ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

            DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
            int displayWidth = displayMetrics.widthPixels;
            int displayHeight = displayMetrics.heightPixels;

            if(onlyBoundsOptions.outWidth < 150 || onlyBoundsOptions.outHeight < 150) {
                Toast.makeText(this, "Imaginea încărcată este prea mică", Toast.LENGTH_SHORT).show();
                return null;
            }

            double widthRatio = onlyBoundsOptions.outWidth / (1.0 * displayMetrics.widthPixels);
            double heightRatio = onlyBoundsOptions.outHeight / (1.0 * displayMetrics.heightPixels);

            double THUMBNAIL_SIZE = widthRatio > heightRatio ? displayHeight : displayWidth;

            double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
            bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
            input = this.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
            input.close();
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Schimbarea imaginii de profil nereușită", Toast.LENGTH_SHORT).show();
            return null;
        }
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }

    private void checkPermission() {
        if(Build.VERSION.SDK_INT > 22) {
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
    }

    // TODO: De verificat daca merge ok functia!
    private void removeImagesFromFirebase() {

        for (int i = 0; i < realNames.size(); i++) {
            String name = realNames.get(i);

            StorageReference desertRef = FirebaseStorage.getInstance().getReference().child(UsefulThings.FB_STORAGE_PATH +
                    lat + "_" + lng + "/" + name);

            desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(LOG, "Am sters cu succes imaginea!");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d(LOG, "Imaginea nu exista!");
                }
            });

            desertRef = FirebaseStorage.getInstance().getReference().child(UsefulThings.FB_STORAGE_PATH +
                    lat + "_" + lng + "/thumbnail_" + name);

            desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(LOG, "Am sters cu succes imaginea!");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d(LOG, "Imaginea nu exista!");
                }
            });
        }
    }

}
