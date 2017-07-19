package com.community.community.PublicProfile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
//import android.renderscript.Allocation;
//import android.renderscript.Element;
//import android.renderscript.RenderScript;
//import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluejamesbond.text.DocumentView;
import com.community.community.General.User;
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

public class PublicProfileActivity extends AppCompatActivity {

    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    public static final String FB_STORAGE_PATH = "images/users/";

    /* Profile details */
    private android.support.percent.PercentRelativeLayout percentRelativeLayout1 = null;
    private android.support.percent.PercentRelativeLayout percentRelativeLayout2 = null;
    private ImageView blurImage = null;
    private CircleImageView circleImage = null;
    private TextView nickname = null;
    private TextView email = null;
    private TextView ownNumber = null;
    private TextView supportedNumber = null;
    private DocumentView describe = null;
    private DocumentView address = null;

    /* Submit Buttons*/
    private Button saveBtn = null;
    private Button cancelBtn = null;

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
        setContentView(R.layout.public_profile_activity);

        /* Prepare to return to MainActivity */
        intent = new Intent();
        intent.putExtra("changed", confirmChanges);
        setResult(RESULT_OK, intent);

        percentRelativeLayout1 = (android.support.percent.PercentRelativeLayout) findViewById(R.id.relative_layout_1);
        percentRelativeLayout2 = (android.support.percent.PercentRelativeLayout) findViewById(R.id.relative_layout_2);
        blurImage = (ImageView) findViewById(R.id.blur_profile_image);
        circleImage = (CircleImageView) findViewById(R.id.profile_image);
        nickname = (TextView) findViewById(R.id.userNickname);
        email = (TextView) findViewById(R.id.userEmail);
        ownNumber = (TextView) findViewById(R.id.own_number);
        supportedNumber = (TextView) findViewById(R.id.supported_number);
        describe = (DocumentView) findViewById(R.id.describe_view);
        address = (DocumentView) findViewById(R.id.address_view);
//        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);

        /* Submit Button */
        saveBtn = (Button)findViewById(R.id.submit_marker);
        saveBtn.setOnClickListener(callImageButtonClickListener);
        cancelBtn = (Button)findViewById(R.id.cancel_marker);
        cancelBtn.setOnClickListener(callImageButtonClickListener);
        setBtnVisibility(View.GONE);

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

        //TODO: Moare aici cand e deschis foarte repede! (inainte sa se incarce obiectivele!)
        /* Set EditButton */
        if(userDetails.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
            ImageButton editBtn = (ImageButton) findViewById(R.id.edit_btn);
            editBtn.setVisibility(View.VISIBLE);
            editBtn.setOnClickListener(callImageButtonClickListener);
        }

    }

    private PublicProfileActivity.CallImageButtonClickListener callImageButtonClickListener = new PublicProfileActivity.CallImageButtonClickListener();
    private class CallImageButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

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
        email.setText(userDetails.getEmail());
        ownNumber.setText(String.valueOf(userDetails.getOwnCausesNumber()));
        supportedNumber.setText(String.valueOf(userDetails.getSupportedCausesNumber()));

        if(userDetails.getDescribe() != null && !userDetails.getDescribe().equals("")){
            describe.setText(userDetails.getDescribe());
            percentRelativeLayout1.setVisibility(View.VISIBLE);
        } else {
            percentRelativeLayout1.setVisibility(View.GONE);
        }

        if(userDetails.getAddress() != null && !userDetails.getAddress().equals("")){
            address.setText(userDetails.getAddress());
            percentRelativeLayout2.setVisibility(View.VISIBLE);
        } else {
            percentRelativeLayout2.setVisibility(View.GONE);
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
                icon = BitmapFactory.decodeStream(PublicProfileActivity.this
                        .openFileInput("draftImage_" + userDetails.getEmail()));
                newPicture = true;
                newPictureBitmap = icon;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            if(!newPicture) {
                icon = BitmapFactory.decodeStream(PublicProfileActivity.this
                        .openFileInput("myImage_" + userDetails.getEmail()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.drawable.profile);
        }

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

    private void uploadImageToFirebase() {
        Uri uri = getCompressedImageUri(getApplicationContext(), newPictureBitmap);

        if(uri != null){
            uploadToFirebase(uri);
        } else {
            Toast.makeText(getApplicationContext(), "No image to upload", Toast.LENGTH_SHORT).show();
        }

    }

    @SuppressWarnings("VisibleForTests")
    private void uploadToFirebase(final Uri uri) {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Uploading image");
        dialog.show();

        String realPath = getRealPath(uri);
        Log.d(LOG, "Path: " + realPath);

        Uri file = Uri.fromFile(new File(realPath));
        userDetails.setImageName(file.getLastPathSegment());
        Log.d(LOG, "Name: " + userDetails.getImageName());

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

    public Uri getCompressedImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
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

//    // TODO: Var 1
//    @SuppressLint("NewApi")
//    public static Bitmap blurring(Context context, Bitmap sentBitmap, float radius) {
//
//        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
//
//        final RenderScript rs = RenderScript.create(context);
//        final Allocation input = Allocation.createFromBitmap(rs, sentBitmap,
//                Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
//        final Allocation output = Allocation.createTyped(rs, input.getType());
//        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
//        script.setRadius(radius);
//        script.setInput(input);
//        script.forEach(output);
//        output.copyTo(bitmap);
//        return bitmap;
//    }
}
//        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("users")
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .child("ProfileSettings");
//
//        dRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//
//                Map<String, Object> data = (Map<String,Object>) snapshot.getValue();
//
//                nickname.setText(data.get("nickname").toString());
//                email.setText(data.get("email").toString());
//                ownNumber.setText(data.get("ownCauses").toString());
//                supportedNumber.setText(data.get("supportedCauses").toString());
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                //handle databaseError
//            }
//        });
