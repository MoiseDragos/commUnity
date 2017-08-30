package com.community.community.PublicProfile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.community.community.General.BackPressedActivity;
import com.community.community.General.UsefulThings;
import com.community.community.General.User;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditPublicProfileActivity extends AppCompatActivity {

    private String LOG = this.getClass().getSimpleName();

    /* Profile details */
    private EditText nickname = null;
    private EditText describe = null;
    private EditText address = null;
    private EditText official_address = null;
    private EditText website = null;
    private EditText donate = null;
    private TextView ageTextView = null;
    private SeekBar ageSeekBar = null;

    private Button submitBtn = null;
    private Button cancelBtn = null;

    boolean greenButton = false;

    /* User */
    private int progress;
    private boolean changed;
    private String oldNickname = null;

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

        changeProfileImage = (CircleImageView) findViewById(R.id.change_profile_image);
        changeProfileImage.setOnClickListener(callImageButtonClickListener);

        submitBtn = (Button) findViewById(R.id.submit_changes);
        submitBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.edit_text_form_gray));
        submitBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.blue5));
        submitBtn.setEnabled(false);
        submitBtn.setOnClickListener(callImageButtonClickListener);

        cancelBtn = (Button) findViewById(R.id.cancel_changes);
        cancelBtn.setOnClickListener(callImageButtonClickListener);

        if(UsefulThings.currentUser == null) {
            UsefulThings.currentUser = (User) savedInstanceState.getSerializable("currentUser");

            if(UsefulThings.currentUser == null) {
                Log.d(LOG, "Nu am primit detaliile!");
                finish();
            }
        }

        if(UsefulThings.currentUser.getType().equals("ngo")) {
            LinearLayout ngoLayout = (LinearLayout) findViewById(R.id.ngoLayout);
            ngoLayout.setVisibility(View.VISIBLE);

            official_address = (EditText) findViewById(R.id.edit_official_address);
            website = (EditText) findViewById(R.id.edit_site);
            donate = (EditText) findViewById(R.id.edit_donate);

            setListeners(true);
            setInitialProfileDetails(true);
        } else {
            LinearLayout userLayout = (LinearLayout) findViewById(R.id.userLayout);
            userLayout.setVisibility(View.VISIBLE);

            address = (EditText) findViewById(R.id.edit_address);
            ageTextView = (TextView) findViewById(R.id.edit_age);
            ageSeekBar = (SeekBar) findViewById(R.id.seek_bar);
            setListeners(false);
            setInitialProfileDetails(false);
        }

    }

    private void setListeners(boolean ngo) {

        final String initNickname = UsefulThings.currentUser.getNickname();
        final String initDescribe;

        if(UsefulThings.currentUser.getDescribe() != null) {
            initDescribe = UsefulThings.currentUser.getDescribe();
        } else {
            initDescribe = "";
        }

        nickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!initNickname.equals(s.toString())) {
                    changeSubmitBtnDetails();
                }
            }
        });

        describe.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!initDescribe.equals(s.toString())) {
                    changeSubmitBtnDetails();
                }
            }
        });

        if(ngo) {
            final String initOfficialAddress;
            if(UsefulThings.currentUser.getOfficial_address() != null) {
                initOfficialAddress = UsefulThings.currentUser.getOfficial_address();
            } else {
                initOfficialAddress = "";
            }


            official_address.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(!initOfficialAddress.equals(s.toString())) {
                        changeSubmitBtnDetails();
                    }
                }
            });

            final String initWebsite;
            if(UsefulThings.currentUser.getSite() != null) {
                initWebsite = UsefulThings.currentUser.getSite();
            } else {
                initWebsite = "";
            }


            website.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(!initWebsite.equals(s.toString())) {
                        changeSubmitBtnDetails();
                    }
                }
            });

            final String initDonate;
            if(UsefulThings.currentUser.getDonate() != null) {
                initDonate = UsefulThings.currentUser.getDonate();
            } else {
                initDonate = "";
            }


            donate.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(!initDonate.equals(s.toString())) {
                        changeSubmitBtnDetails();
                    }
                }
            });

        } else {
            final String initAddress;
            if(UsefulThings.currentUser.getAddress() != null) {
                initAddress = UsefulThings.currentUser.getAddress();
            } else {
                initAddress = "";
            }


            address.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(!initAddress.equals(s.toString())) {
                        changeSubmitBtnDetails();
                    }
                }
            });
        }

    }

    private void changeSubmitBtnDetails() {
        submitBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.edit_text_form_green));
        submitBtn.setTextColor(ContextCompat.getColor(getApplicationContext(),
                R.color.white));
        greenButton = true;
        submitBtn.setEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("currentUser", UsefulThings.currentUser);
    }

    private void setInitialProfileDetails(boolean ngo) {
        changed = false;

        Bitmap icon;
        try {
            icon = BitmapFactory.decodeStream(EditPublicProfileActivity.this
                    .openFileInput("myImage_" + UsefulThings.currentUser.getEmail()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.drawable.profile);
        }
        profileImage.setImageBitmap(icon);

        nickname.setText(UsefulThings.currentUser.getNickname(), TextView.BufferType.EDITABLE);

        if (UsefulThings.currentUser.getDescribe() != null) {
            describe.setText(UsefulThings.currentUser.getDescribe());
        }

        if(ngo) {
            if (UsefulThings.currentUser.getOfficial_address() != null) {
                official_address.setText(UsefulThings.currentUser.getOfficial_address());
            }

            if (UsefulThings.currentUser.getSite() != null) {
                website.setText(UsefulThings.currentUser.getSite());
            }

            if (UsefulThings.currentUser.getDonate() != null) {
                donate.setText(UsefulThings.currentUser.getDonate());
            }
        } else {
            if (UsefulThings.currentUser.getAddress() != null) {
                address.setText(UsefulThings.currentUser.getAddress());
            }

            ageSeekBar.setProgress(UsefulThings.currentUser.getAge());
            progress = ageSeekBar.getProgress();
            ageTextView.setText(String.valueOf(progress + 10));

            ageSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                    progress = progressValue + 10;
                    ageTextView.setText(String.valueOf(progress));
                    changeSubmitBtnDetails();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    ageTextView.setText(String.valueOf(progress));
                }
            });

        }

    }

    private EditPublicProfileActivity.CallImageButtonClickListener callImageButtonClickListener =
            new EditPublicProfileActivity.CallImageButtonClickListener();
    private class CallImageButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.submit_changes:
                    User user = verifyUserDetails();
                    if(user != null && changed) {
                        if(user.getNickname() != null) {
                            verifyDuplicatedNicknames(user);
                        } else {
                            applyChanges(user);
                        }
                    }
                    break;

                case R.id.cancel_changes:
                    if(greenButton) {
                        Intent i = new Intent(getApplicationContext(), BackPressedActivity.class);
                        i.putExtra("edit", "edit");
                        startActivityForResult(i, 100);
                    } else {
                        Intent intent = new Intent();
                        intent.putExtra("changed", false);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    break;

                case R.id.change_profile_image:
                    if(Build.VERSION.SDK_INT > 22){
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

        private void applyChanges(User user) {
            Intent i = new Intent();
            i.putExtra("changed", true);

            if(changedImage != null) {
                createImageFromBitmap(changedImage);
                new asyncImageUpload().execute();
                i.putExtra("newPicture", true);
            } else {
                i.putExtra("newPicture", false);
            }

            updateUserDetails(user);

            setResult(RESULT_OK, i);
            finish();
        }

        private class asyncImageUpload extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                removeOldImageFromFirebase();
                uploadImageToFirebase();

                return "Executed";
            }

        }

        private User verifyUserDetails() {

            User draftUser = new User();

            final String nick = verifyString(nickname.getText().toString(), 3, 24, 1);
            if(nick == null) {
                changed = false;
                return null;
            }

            if(!UsefulThings.currentUser.getNickname().equals(nick)) {
                changed = true;
                draftUser.setNickname(nick);
            }

            String des = verifyString(describe.getText().toString(), 0, 1300, 2);
            if(des == null) {
                changed = false;
                return null;
            }

            if((UsefulThings.currentUser.getDescribe() == null && !des.equals("")) ||
                    (UsefulThings.currentUser.getDescribe() != null
                            && !UsefulThings.currentUser.getDescribe().equals(des))) {
                changed = true;
                draftUser.setDescribe(des);
            }

            if(changedImage != null) {
                changed = true;
            }

            if(UsefulThings.currentUser.getType().equals("ngo")) {
                String off_adr = verifyString(official_address.getText().toString(), 0, 100, 4);
                if(off_adr == null) {
                    changed = false;
                    return null;
                }

                if((UsefulThings.currentUser.getOfficial_address() == null && !off_adr.equals("")) ||
                        (UsefulThings.currentUser.getOfficial_address() != null
                                && !UsefulThings.currentUser.getOfficial_address().equals(off_adr))) {
                    changed = true;
                    draftUser.setOfficial_address(off_adr);
                }

                String site = verifyString(website.getText().toString(), 0, 100, 5);
                if(site == null) {
                    changed = false;
                    return null;
                }

                if((UsefulThings.currentUser.getSite() == null && !site.equals("")) ||
                        (UsefulThings.currentUser.getSite() != null
                                && !UsefulThings.currentUser.getSite().equals(site))) {
                    changed = true;
                    draftUser.setSite(site);
                }

                String don = verifyString(donate.getText().toString(), 0, 100, 6);
                if(don == null) {
                    changed = false;
                    return null;
                }

                if((UsefulThings.currentUser.getDonate() == null && !don.equals("")) ||
                        (UsefulThings.currentUser.getDonate() != null
                                && !UsefulThings.currentUser.getDonate().equals(don))) {
                    changed = true;
                    draftUser.setDonate(don);
                }
            } else {
                String adr = verifyString(address.getText().toString(), 0, 100, 3);
                if(adr == null) {
                    changed = false;
                    return null;
                }

                if((UsefulThings.currentUser.getAddress() == null && !adr.equals("")) ||
                        (UsefulThings.currentUser.getAddress() != null
                                && !UsefulThings.currentUser.getAddress().equals(adr))) {
                    changed = true;
                    draftUser.setAddress(adr);
                }

                if(progress != UsefulThings.currentUser.getAge()) {
                    changed = true;
                    draftUser.setAge(progress);
                }
            }

            return draftUser;
        }

        private void verifyDuplicatedNicknames(final User user) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child("nicknames");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() != null) {
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                        if(map.containsKey(user.getNickname().replace(".", "-"))){
                            Toast.makeText(getApplicationContext(),
                                    "Nickname este folosit de către alt user!",
                                    Toast.LENGTH_SHORT).show();
                            changed = false;
                        } else {
                            applyChanges(user);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
        }
    }

    private void updateUserDetails(User draftUser) {
        if(draftUser.getNickname() != null) {
            oldNickname = UsefulThings.currentUser.getNickname();
            UsefulThings.currentUser.setNickname(draftUser.getNickname());
        }

        if(draftUser.getDescribe() != null) {
            UsefulThings.currentUser.setDescribe(draftUser.getDescribe());
        }

        String currentUserType = UsefulThings.currentUser.getType();

        if(currentUserType.equals("ngo")) {
            if (draftUser.getOfficial_address() != null) {
                UsefulThings.currentUser.setOfficial_address(draftUser.getOfficial_address());
            }

            if (draftUser.getSite() != null) {
                UsefulThings.currentUser.setSite(draftUser.getSite());
            }

            if (draftUser.getDonate() != null) {
                UsefulThings.currentUser.setDonate(draftUser.getDonate());
            }
        } else {
            if (draftUser.getAddress() != null) {
                UsefulThings.currentUser.setAddress(draftUser.getAddress());
            }

            if (draftUser.getAge() != 0) {
                UsefulThings.currentUser.setAge(draftUser.getAge());
            }
        }

        new asyncUpload(draftUser, currentUserType).execute();
    }

    private class asyncUpload extends AsyncTask<String, Void, String> {

        private User draftUser;
        private String currentUserType;

        asyncUpload(User draftUser, String currentUserType){
            this.draftUser = draftUser;
            this.currentUserType = currentUserType;
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(LOG, UsefulThings.currentUser.getUid());

            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(UsefulThings.currentUser.getUid())
                    .child("ProfileSettings");

            if(draftUser.getNickname() != null) {
                FirebaseDatabase.getInstance().getReference().child("nicknames")
                        .child(oldNickname.replace(".", "-")).removeValue();
                String nick = UsefulThings.currentUser.getNickname().replace(".", "-");
                FirebaseDatabase.getInstance().getReference().child("nicknames").child(nick)
                        .setValue(UsefulThings.currentUser.getEmail() + "~"
                                + UsefulThings.currentUser.getUid());
                rootRef.child("nickname").setValue(draftUser.getNickname());
            }

            if(draftUser.getDescribe() != null) {
                rootRef.child("describe").setValue(draftUser.getDescribe());
            }

            if(currentUserType.equals("ngo")){
                if(draftUser.getOfficial_address() != null) {
                    rootRef.child("official_address").setValue(draftUser.getOfficial_address());
                }
                if(draftUser.getDonate() != null) {
                    rootRef.child("donate").setValue(draftUser.getDonate());
                }
                if(draftUser.getSite() != null) {
                    rootRef.child("site").setValue(draftUser.getSite());
                }
            } else {
                if(draftUser.getAddress() != null) {
                    rootRef.child("address").setValue(draftUser.getAddress());
                }

                if(draftUser.getAge() != 0) {
                    rootRef.child("age").setValue(draftUser.getAge());
                }
            }

            return "Executed";
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

    private String verifyString(String str, int min, int max, int forToast) {
        str = str.replaceAll("\\s+$", "");
        str = str.replaceAll("^\\s+", "");
        str = str.replace("\n", "").replace("\r", "");

        int len = str.length();
        if(len < min) {
            switch (forToast) {
                case 1:
                    Toast.makeText(getApplicationContext(),
                            "Nickname prea scurt!", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(),
                            "Descriere prea scurtă!", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(getApplicationContext(),
                            "Adresă prea scurtă!", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    Toast.makeText(getApplicationContext(),
                            "Adresa oficială prea scurtă!", Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    Toast.makeText(getApplicationContext(),
                            "Website prea scurt!", Toast.LENGTH_SHORT).show();
                    break;
                case 6:
                    Toast.makeText(getApplicationContext(),
                            "Link donație prea scurt!", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
            return null;
        }

        if(len > max) {

            switch (forToast) {
                case 1:
                    Toast.makeText(getApplicationContext(),
                            "Nickname prea lung!", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(),
                            "Descriere prea lungă!", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(getApplicationContext(),
                            "Adresă prea lungă!", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    Toast.makeText(getApplicationContext(),
                            "Adresa oficială prea lungă!", Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    Toast.makeText(getApplicationContext(),
                            "Website prea lung!", Toast.LENGTH_SHORT).show();
                    break;
                case 6:
                    Toast.makeText(getApplicationContext(),
                            "Link donație prea lung!", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
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
                try {
                    Bitmap bitmap = UsefulThings.getThumbnail(imageUri,
                            EditPublicProfileActivity.this);

                    if(bitmap != null) {
                        changeProfileImage.setImageBitmap(bitmap);
                        changedImage = bitmap;
                        changeSubmitBtnDetails();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == 100) {
            if(resultCode == Activity.RESULT_OK){
                Bundle b = data.getExtras();
                if(b.getBoolean("result")) {
                    Intent intent = new Intent();
                    intent.putExtra("changed", false);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }
    }

    /* ---------- Image Upload Section ---------- */

    public String createImageFromBitmap(Bitmap bitmap) {
        String fileName = "myImage_" + UsefulThings.currentUser.getEmail();

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
        StorageReference desertRef = storageRef.child(UsefulThings.FB_STORAGE_USERS_PATH +
                UsefulThings.currentUser.getUid() + "/" + UsefulThings.currentUser.getImageName());
        Log.d(LOG, "getImageName(): " + UsefulThings.currentUser.getImageName());

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
                Log.d(LOG, "Imaginea nu exista!");
            }
        });

    }

    private void uploadImageToFirebase() {
        Uri uri = UsefulThings.getCompressedImageUri(getApplicationContext(), changedImage);

        if(uri != null){
            uploadToFirebase(uri);
        } else {
            Toast.makeText(getApplicationContext(),
                    "No image to upload", Toast.LENGTH_SHORT).show();
        }

    }

    @SuppressWarnings("VisibleForTests")
    private void uploadToFirebase(final Uri uri) {


        String realPath = UsefulThings.getRealPath(uri, EditPublicProfileActivity.this);

        final Uri file = Uri.fromFile(new File(realPath));
        UsefulThings.currentUser.setImageName(file.getLastPathSegment());

        Log.d(LOG, "Segment: " + file.getLastPathSegment());

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child(UsefulThings.FB_STORAGE_USERS_PATH +
                        UsefulThings.currentUser.getUid() + "/" +
                        UsefulThings.currentUser.getImageName());
        Log.d(LOG, "ImageName" + UsefulThings.currentUser.getImageName());

        ref.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        String url = taskSnapshot.getDownloadUrl().toString();
                        Log.d(LOG, "taskSnapshot URL: " + url);
                        UsefulThings.currentUser.setImageURL(url);
                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(UsefulThings.currentUser.getUid())
                                .child("ProfileSettings").child("imageURL").setValue(url);

                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(UsefulThings.currentUser.getUid())
                                .child("ProfileSettings").child("imageName")
                                .setValue(file.getLastPathSegment());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if(greenButton) {
            Intent i = new Intent(getApplicationContext(), BackPressedActivity.class);
            i.putExtra("edit", "edit");
            startActivityForResult(i, 100);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(UsefulThings.mNetworkStateIntentReceiver,
                UsefulThings.mNetworkStateChangedFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(UsefulThings.mNetworkStateIntentReceiver);
    }

    /* ---------- End of Image Upload Section ---------- */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG, "onDestroy");

        profileImage = null;
        nickname = null;
        describe = null;
        address = null;
        ageTextView = null;
        ageSeekBar = null;
        official_address = null;
        website = null;
        donate = null;

        changeProfileImage = null;

        submitBtn = null;
        cancelBtn = null;
        callImageButtonClickListener = null;
        profileImage = null;
        changeProfileImage = null;
        changedImage = null;
    }
}


