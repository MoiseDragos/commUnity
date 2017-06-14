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
import com.community.community.General.User;
import com.community.community.PublicProfile.EditPublicProfileActivity;
import com.community.community.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.blurry.Blurry;

public class CauseProfileActivity extends AppCompatActivity {

    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    public static final String FB_STORAGE_PATH = "images/";

    /* Profile details */
    private android.support.percent.PercentRelativeLayout percentRelativeLayout3 = null;
    private android.support.percent.PercentRelativeLayout percentRelativeLayout4 = null;
    private ImageView blurImage = null;
    private CircleImageView circleImage = null;
    private TextView nickname = null;
    private TextView causes_name = null;
    private TextView supportedNumber = null;
    private DocumentView describe = null;
    private ScrollView scrollView = null;

    /* Buttons */
    private Button saveBtn = null;
    private Button cancelBtn = null;
    private Button supportBtn = null;
    private Button noMoreSupportBtn = null;

    /* User */
    private User userDetails = null;
    private Intent intent = null;

    /* Confirm changes */
    private boolean confirmChanges = false;
    private boolean newPicture = false;
    private Bitmap newPictureBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cause_profile_activity);

        /* Prepare to return to MainActivity */
        intent = new Intent();
        intent.putExtra("changed", confirmChanges);
        setResult(RESULT_OK, intent);

        percentRelativeLayout3 = (android.support.percent.PercentRelativeLayout) findViewById(R.id.relative_layout_3);
        percentRelativeLayout4 = (android.support.percent.PercentRelativeLayout) findViewById(R.id.relative_layout_4);
        blurImage = (ImageView) findViewById(R.id.blur_profile_image);
        circleImage = (CircleImageView) findViewById(R.id.profile_image);
        nickname = (TextView) findViewById(R.id.userNickname);
        causes_name = (TextView) findViewById(R.id.causes_name);
        supportedNumber = (TextView) findViewById(R.id.supported_number);
        describe = (DocumentView) findViewById(R.id.describe_view);
        scrollView = (ScrollView) findViewById(R.id.scrollView);

        /* Submit Buttons */
        saveBtn = (Button)findViewById(R.id.submit_marker);
        saveBtn.setOnClickListener(callImageButtonClickListener);
        cancelBtn = (Button)findViewById(R.id.cancel_marker);
        cancelBtn.setOnClickListener(callImageButtonClickListener);
        setBtnVisibility(View.GONE);

        /* Support Buttons */
        supportBtn = (Button) findViewById(R.id.supportBtn);
        supportBtn.setOnClickListener(callImageButtonClickListener);
        noMoreSupportBtn = (Button) findViewById(R.id.noMoreSupportBtn);
        noMoreSupportBtn.setOnClickListener(callImageButtonClickListener);

        /* Get data from MainActivity */
        userDetails = (User) getIntent().getSerializableExtra("userDetails");
        Log.d(LOG, "Initial imageName: " + userDetails.getImageName());

        if(userDetails != null) {
            setProfileDetails();
            setProfilePicture(true);
        } else {
            Log.d(LOG, "Nu am primit detaliile!");
            finish();
        }

        // TODO: Moare aici cand e deschis foarte repede! (inainte sa se incarce obiectivele!)
        /* Set EditButton */
        if(userDetails.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
            ImageButton editBtn = (ImageButton) findViewById(R.id.edit_btn);
            editBtn.setVisibility(View.VISIBLE);
            editBtn.setOnClickListener(callImageButtonClickListener);
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
                    Intent i = new Intent(getApplicationContext(), EditPublicProfileActivity.class);
                    i.putExtra("userDetails", userDetails);
                    startActivityForResult(i, 1);
                    break;

                case R.id.submit_marker:
                    setBtnVisibility(View.GONE);
                    if(newPicture) {
                        newPicture = false;
                        createImageFromBitmap(newPictureBitmap);
                        removeOldImageFromFirebase();
                        uploadImageToFirebase();
                    } else {
                        intent.putExtra("changed", confirmChanges);
                        intent.putExtra("userDetails", userDetails);
                        finish();
                    }
                    break;

                case R.id.cancel_marker:
                    setBtnVisibility(View.GONE);
                    finish();
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1){

//            scrollView.fullScroll(ScrollView.FOCUS_UP);
//            scrollView.smoothScrollTo(0,0);

            if(resultCode == RESULT_OK) {
                if(data.getBooleanExtra("changed", confirmChanges)){
                    Log.d(LOG, "Au aparut schimbari!");
                    confirmChanges = true;
                    setBtnVisibility(View.VISIBLE);
                    updateLocalDetails((User) data.getSerializableExtra("userDetails"));
                } else {
                    Log.d(LOG, "Nu au aparut schimbari!");
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setProfileDetails() {

        nickname.setText(userDetails.getNickname());
        supportedNumber.setText(String.valueOf(userDetails.getSupportedCausesNumber()));

        if(userDetails.getDescribe() != null && !userDetails.getDescribe().equals("")){
            describe.setText(userDetails.getDescribe());
            percentRelativeLayout3.setVisibility(View.VISIBLE);
        } else {
            percentRelativeLayout3.setVisibility(View.GONE);
        }

    }

    private void updateLocalDetails(User editedUserDetails) {

        userDetails.setNickname(editedUserDetails.getNickname());
        userDetails.setDescribe(editedUserDetails.getDescribe());
        userDetails.setAddress(editedUserDetails.getAddress());
        userDetails.setAge(editedUserDetails.getAge());
        setProfileDetails();

        if(editedUserDetails.isChangedProfilePic()){
            editedUserDetails.setChangedProfilePic(false);
            setProfilePicture(false);
        }
    }

    private void setProfilePicture(boolean fromCreate) {
        Bitmap icon = null;

        if(!fromCreate) {
            try {
                icon = BitmapFactory.decodeStream(CauseProfileActivity.this
                        .openFileInput("draftImage_" + userDetails.getEmail()));
//                Log.d(LOG, "New picture");
                newPicture = true;
                newPictureBitmap = icon;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            if(!newPicture) {
                icon = BitmapFactory.decodeStream(CauseProfileActivity.this
                        .openFileInput("myImage_" + userDetails.getEmail()));
//                Log.d(LOG, "Old picture");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.drawable.profile);
//            Log.d(LOG, "Default picture");
        }

        // TODO: Scaleaza imaginea!
        // TODO: Var 3
        blurImage.setImageBitmap(icon);
        Blurry.with(getApplicationContext())
//                .radius(50)
                .async()
                .from(icon)
                .into(blurImage);

        circleImage.setImageBitmap(icon);

        // TODO: Var 1 - API 17 :(
//        icon = blurring(getApplicationContext(), icon, 10.5f);


        // TODO: Var 2 - Nu prea merge
//        Log.d(LOG, "width: " + icon.getWidth() + "   height: " + icon.getHeight());
//        Bitmap.createScaledBitmap(icon, icon.getWidth() / 4, icon.getHeight() / 4, true);
//        Log.d(LOG, "width: " + icon.getWidth() + "   height: " + icon.getHeight());
//        Bitmap.createScaledBitmap(icon, icon.getWidth() * 4, icon.getHeight() * 4, true);


    }

    public void setBtnVisibility(int visibility){
        saveBtn.setVisibility(visibility);
        cancelBtn.setVisibility(visibility);
    }

    public String createImageFromBitmap(Bitmap bitmap) {
        String fileName = "myImage_" + userDetails.getEmail();//no .png or .jpg needed
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }

    private void uploadImageToFirebase() {
        Uri uri = getImageUri(getApplicationContext(), newPictureBitmap);

        if(uri != null){
            uploadToFirabase(uri);
        } else {
            Toast.makeText(getApplicationContext(), "No image to upload", Toast.LENGTH_SHORT).show();
        }

    }

//    public String getImageExt(Uri uri){
//        ContentResolver cR = getContentResolver();
//        MimeTypeMap mTM = MimeTypeMap.getSingleton();
//        return mTM.getExtensionFromMimeType(cR.getType(uri));
//    }

    private void removeOldImageFromFirebase() {
        // Create a storage reference from our app
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Create a reference to the file to delete
        StorageReference desertRef = storageRef.child(FB_STORAGE_PATH +
                userDetails.getUid() + "/" + userDetails.getImageName());
        Log.d(LOG, "desertRef: " + desertRef.toString());

        // Delete the file
        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                Log.d(LOG, "Am sters cu succes imaginea!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Log.d(LOG, "Imaginea nu exista!");
            }
        });

    }

    @SuppressWarnings("VisibleForTests")
    private void uploadToFirabase(final Uri uri) {


//        uri.getLastPathSegment();

        // Create a storage reference from our app
//        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
//        Log.d(LOG, "storageRef: " + storageRef.toString());

//        final DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference(FB_DATABASE_PATH);
//
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Uploading image");
        dialog.show();

        String realPath = getRealPath(uri);
        Log.d(LOG, "Path: " + realPath);

        Uri file = Uri.fromFile(new File(realPath));
        userDetails.setImageName(file.getLastPathSegment());
        Log.d(LOG, "Name: " + userDetails.getImageName());

//        StorageReference ref = storageRef.child(FB_STORAGE_PATH +
//                System.currentTimeMillis() + "." + getImageExt(uri));

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(FB_STORAGE_PATH +
                userDetails.getUid() + "/" + userDetails.getImageName());
        Log.d(LOG, "REF: " + ref);

        ref.putFile(uri)
            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Image uploaded", Toast.LENGTH_SHORT).show();

                    userDetails.setImageURL(taskSnapshot.getDownloadUrl().toString());
                    Log.d(LOG, userDetails.getImageURL());

                    intent.putExtra("changed", confirmChanges);
                    intent.putExtra("userDetails", userDetails);
                    finish();

    //                // Save image info in o firebase database
    //                String uploadID = mDatabaseRef.push().getKey();
    //                // TODO: imageUpload <- taskSnapshot.getDownloadUrl().toString()
    //                mDatabaseRef.child(uploadID).setValue(taskSnapshot.getDownloadUrl().toString());
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
            //                        dialog.dismiss();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    intent.putExtra("changed", false);
                    intent.putExtra("userDetails", userDetails);
                    finish();
                }
            });
//                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//
//                    @Override
//                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//
//                        double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                        dialog.setMessage("Uploaded " + (int) progress + "%");
//                    }
//                });
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public  String getRealPath(Uri uri){
        String realPath = null;

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        if(cursor.moveToFirst()){
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            realPath = cursor.getString(columnIndex);
        }
        cursor.close();
        return realPath;
    }

    // TODO: Var 1
    @SuppressLint("NewApi")
    public static Bitmap blurring(Context context, Bitmap sentBitmap, float radius) {

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        final RenderScript rs = RenderScript.create(context);
        final Allocation input = Allocation.createFromBitmap(rs, sentBitmap,
                Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(radius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(bitmap);
        return bitmap;
    }
}