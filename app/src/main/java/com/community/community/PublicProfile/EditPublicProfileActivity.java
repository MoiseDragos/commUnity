package com.community.community.PublicProfile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.community.community.General.User;
import com.community.community.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditPublicProfileActivity extends AppCompatActivity {

    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    /* Profile details */
    private EditText nickname = null;
    private EditText describe = null;
    private EditText address = null;
    private TextView ageTextView = null;
    private SeekBar ageSeekBar = null;

    /* User */
    private User userDetails = null;
    private Boolean changed;
    private int progress;

    /* Image Detail */
    private CircleImageView profileImage = null;
    private CircleImageView changeProfileImage = null;
    private Bitmap changedImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_public_profile_activity);

        profileImage = (CircleImageView) findViewById(R.id.profile_image);
        nickname = (EditText) findViewById(R.id.edit_nickname);
        describe = (EditText) findViewById(R.id.edit_describe);
        address = (EditText) findViewById(R.id.edit_address);
        ageTextView = (TextView) findViewById(R.id.edit_age);
        ageSeekBar = (SeekBar) findViewById(R.id.seek_bar);

        changeProfileImage = (CircleImageView) findViewById(R.id.change_profile_image);
        changeProfileImage.setOnClickListener(callImageButtonClickListener);

        Button submitBtn = (Button) findViewById(R.id.submit_changes);
        submitBtn.setOnClickListener(callImageButtonClickListener);

        Button cancelBtn = (Button) findViewById(R.id.cancel_changes);
        cancelBtn.setOnClickListener(callImageButtonClickListener);

        /* Get data from PublicProfileActivity */
        userDetails = (User) getIntent().getSerializableExtra("userDetails");

        if (userDetails != null) {
            setInitialProfileDetails();
        } else {
            Log.d(LOG, "Nu am primit detaliile!");
        }
    }

    private void setInitialProfileDetails() {
        changed = false;

        Bitmap icon;
        try {
            icon = BitmapFactory.decodeStream(EditPublicProfileActivity.this
                    .openFileInput("myImage_" + userDetails.getEmail()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.drawable.profile);
        }
        profileImage.setImageBitmap(icon);

        nickname.setText(userDetails.getNickname(), TextView.BufferType.EDITABLE);

        if (userDetails.getDescribe() != null) {
            describe.setText(userDetails.getDescribe());
        }

        if (userDetails.getAddress() != null) {
            address.setText(userDetails.getAddress());
        }

        ageSeekBar.setProgress(userDetails.getAge());
        progress = ageSeekBar.getProgress();
        ageTextView.setText(String.valueOf(progress + 10));

        ageSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue + 10;
                ageTextView.setText(String.valueOf(progress));
//                Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
//                Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ageTextView.setText(String.valueOf(progress));
//                Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private EditPublicProfileActivity.CallImageButtonClickListener callImageButtonClickListener = new EditPublicProfileActivity.CallImageButtonClickListener();
    public class CallImageButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.submit_changes:
                    if(updateUserDetails()) {
                        if(userDetails.isChangedProfilePic()){
                            createImageFromBitmap(changedImage);
                        }

                        Intent i = new Intent();
                        i.putExtra("changed", changed);
                        i.putExtra("userDetails", userDetails);
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
//                        Log.d(LOG, "API: " + Build.VERSION.SDK_INT);
                        checkPermission();
                    }
                    Intent i = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, 1);
                    break;
                default:
                    break;
            }
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

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case 123: {
//
//
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //Peform your task here if any
//
//                } else {
//
//                    checkPermission();
//                }
//                return;
//            }
//        }
//    }

    private boolean updateUserDetails() {

        String nick = verifyString(nickname.getText().toString(), 3, 24, 1);
        if(nick == null) {
            return false;
        }

        //TODO: Fara nickname-uri duplicate!

        if(!userDetails.getNickname().equals(nick)) {
            Log.d(LOG, "nick: " + nick);
            changed = true;
            userDetails.setNickname(nick);
        }

        String des = verifyString(describe.getText().toString(), 0, 1300, 2);
        if(des == null) {
            return false;
        }

        if((userDetails.getDescribe() == null && !des.equals("")) ||
                (userDetails.getDescribe() != null && !userDetails.getDescribe().equals(des))) {
            Log.d(LOG, "des: " + des);
            changed = true;
            userDetails.setDescribe(des);
        }

        String adr = verifyString(address.getText().toString(), 0, 100, 3);
        if(adr == null) {
            return false;
        }

        if((userDetails.getAddress() == null && !adr.equals("")) ||
                (userDetails.getAddress() != null && !userDetails.getAddress().equals(adr))) {
            Log.d(LOG, "adr: " + adr);
            changed = true;
            userDetails.setAddress(adr);
        }

        if(progress != userDetails.getAge()) {
            Log.d(LOG, "age: " + progress);
            changed = true;
            userDetails.setAge(progress);
        }

//        Bitmap icon;
//        try {
//            icon = BitmapFactory.decodeStream(EditPublicProfileActivity.this
//                    .openFileInput("myImage_" + userDetails.getEmail()));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
//                    R.drawable.profile);
//        }

        //TODO: Aceeasi imagine
        if(changedImage != null) {// && !equals(icon, changedImage)){//!icon.sameAs(changedImage)){
//            Log.d(LOG, "Icon: " + String.valueOf(icon.getHeight()));
//            Log.d(LOG, "ChangedImage: " + String.valueOf(changedImage.getHeight()));
            Log.d(LOG, "Image!");
            changed = true;
            userDetails.setChangedProfilePic(true);
        }
//        if(userDetails.getProfileImage() == null || !userDetails.getProfileImage().sameAs(changedImage)){
//            Log.d(LOG, "Image!");
//            changed = true;
//            if(userDetails.getProfileImage() != null)
//                Log.d(LOG, "InitImage: " + userDetails.getProfileImage().toString());
//            userDetails.setProfileImage(changedImage);
//        }
        return true;
    }

//    public boolean equals(Bitmap bitmap1, Bitmap bitmap2) {
//        ByteBuffer buffer1 = ByteBuffer.allocate(bitmap1.getHeight() * bitmap1.getRowBytes());
//        bitmap1.copyPixelsToBuffer(buffer1);
//
//        ByteBuffer buffer2 = ByteBuffer.allocate(bitmap2.getHeight() * bitmap2.getRowBytes());
//        bitmap2.copyPixelsToBuffer(buffer2);
//
//        return Arrays.equals(buffer1.array(), buffer2.array());
//    }

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
//                Log.d(LOG, LOG + ": " + imageUri);
                try {
                    Bitmap bitmap = getThumbnail(imageUri);

                    if(bitmap != null) {
                        changeProfileImage.setImageBitmap(bitmap);
                        changedImage = bitmap;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // TODO: remove duplicate functions like getThumbnail, getPower...., createImageFro....

    public Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException{
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

    public String createImageFromBitmap(Bitmap bitmap) {
        String fileName = "draftImage_" + userDetails.getEmail();//no .png or .jpg needed
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
}


