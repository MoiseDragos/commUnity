package com.community.community.PublicProfile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bluejamesbond.text.DocumentView;
import com.community.community.General.User;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.blurry.Blurry;

//TODO: upload details cand ies din aplicatie (nu salveaza cand vin din 'nickname')
public class PublicProfileActivity extends AppCompatActivity {

    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    public static final String FB_STORAGE_PATH = "images/users/";

    private DatabaseReference mDatabase = null;

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

    /* Submit */
    private Button saveBtn = null;
    private Button cancelBtn = null;
    private LinearLayout submit_layout = null;

    /* Ngo Buttons */
    private LinearLayout proposalsLayout = null;
    private Button userOngBtn = null;
    private Button acceptBtn = null;

    /* Supporter */
    private TextView supportBtn = null;

    /* User */
    private User userDetails = null;
    private String currentUserUid = null;
    private String currentUserType = null;
    private String status = null;
    private Intent intent = null;

    /* Confirm changes */
    private boolean confirmChanges = false;
    private boolean newPicture = false;
    private boolean isUserOng = false;
    private Bitmap newPictureBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.public_profile_activity);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        /* Prepare to return to MainActivity */
        intent = new Intent();
        intent.putExtra("changed", confirmChanges);
        setResult(RESULT_OK, intent);

        percentRelativeLayout1 = (android.support.percent.PercentRelativeLayout)
                findViewById(R.id.relative_layout_1);
        percentRelativeLayout2 = (android.support.percent.PercentRelativeLayout)
                findViewById(R.id.relative_layout_2);
        blurImage = (ImageView) findViewById(R.id.blur_profile_image);
        circleImage = (CircleImageView) findViewById(R.id.profile_image);
        nickname = (TextView) findViewById(R.id.userNickname);
        email = (TextView) findViewById(R.id.userEmail);
        ownNumber = (TextView) findViewById(R.id.own_number);
        supportedNumber = (TextView) findViewById(R.id.supported_number);
        describe = (DocumentView) findViewById(R.id.describe_view);
        address = (DocumentView) findViewById(R.id.address_view);

        /* Submit */
        saveBtn = (Button)findViewById(R.id.submit_marker);
        saveBtn.setOnClickListener(callButtonClickListener);
        cancelBtn = (Button)findViewById(R.id.cancel_marker);
        cancelBtn.setOnClickListener(callButtonClickListener);

        submit_layout = (LinearLayout) findViewById(R.id.submit_layout);

        /* Ngo */
        proposalsLayout = (LinearLayout) findViewById(R.id.proposalsLayout);

        acceptBtn = (Button) findViewById(R.id.acceptBtn);
        acceptBtn.setOnClickListener(callButtonClickListener);

        Button rejectBtn = (Button) findViewById(R.id.rejectBtn);
        rejectBtn.setOnClickListener(callButtonClickListener);

        userOngBtn = (Button) findViewById(R.id.userOngBtn);
        userOngBtn.setOnClickListener(callButtonClickListener);

        /* Supporter */
        supportBtn = (TextView) findViewById(R.id.supportBtn);
        supportBtn.setOnClickListener(callButtonClickListener);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        if(dm.heightPixels < 801) {
            TextView tv1 = (TextView) findViewById(R.id.textView1);
            tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

            TextView tv2 = (TextView) findViewById(R.id.textView2);
            tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

            email.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            ownNumber.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            supportedNumber.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        }
//        Toast.makeText(getApplicationContext(), String.valueOf(dm.heightPixels), Toast.LENGTH_LONG).show();

        /* Get data from MainActivity */
        Intent intent = getIntent();

        if(intent != null){
            userDetails = (User) intent.getSerializableExtra("userDetails");
            currentUserUid = intent.getStringExtra("currentUserUid");
            currentUserType = intent.getStringExtra("type");
        }

        if(userDetails != null) {
            setProfileDetails();
            setProfilePicture(true);

            //TODO: Moare aici cand e deschis foarte repede! (inainte sa se incarce obiectivele!)
            /* Set EditButton */
            if(userDetails.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                ImageButton editBtn = (ImageButton) findViewById(R.id.edit_btn);
                editBtn.setVisibility(View.VISIBLE);
                editBtn.setOnClickListener(callButtonClickListener);
            } else {
                if(currentUserType.equals("ngo") && userDetails.getType().equals("user")){
                    verifyMember("Members");
                } else if (currentUserType.equals("user") && userDetails.getType().equals("ngo")) {
                    isUserOng = true;
                    verifyMember("MemberOf");
                    verifySupportedBtn();
                    //TODO: Set supportedBtn
                }
            }
        } else {
            Log.d(LOG, "Nu am primit detaliile!");
            finish();
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1){
            if(resultCode == RESULT_OK) {
                if(data.getBooleanExtra("changed", confirmChanges)){
                    Log.d(LOG, "Au aparut schimbari!");
                    confirmChanges = true;
                    submit_layout.setVisibility(View.VISIBLE);
                    updateLocalDetails((User) data.getSerializableExtra("userDetails"));
                } else {
                    Log.d(LOG, "Nu au aparut schimbari!");
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private PublicProfileActivity.CallImageButtonClickListener
            callButtonClickListener = new PublicProfileActivity.CallImageButtonClickListener();
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
                    submit_layout.setVisibility(View.GONE);
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
                    submit_layout.setVisibility(View.GONE);
                    finish();
                    break;

                case R.id.userOngBtn:
                    Log.d(LOG, "Status: " + status);
                    switch (status) {
                        case "Members":
                            manageProposal(6);
                            break;
                        case "MemberOf":
                            manageProposal(7);
                            break;
                        case "ProposalsMade":
                            manageProposal(4);
                            break;
                        case "ProposalsMadeRejected":
                            if(isUserOng){
                                Toast.makeText(getApplicationContext(),
                                        "Nu puteți formula altă solicitare acestui ONG",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Nu puteți formula altă solicitare acestui user",
                                        Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case "ProposalsReceivedRejected":
                            manageProposal(3);
                            break;
                        case "NoProposal":
                            manageProposal(5);
                            break;
                        default:
                            break;
                    }
                    break;

                case R.id.supportBtn:
                    if(supportBtn.getText().equals(getString(R.string.support_NGO))){
                        supportNGO();
                    } else {
                        unsupportNGO();
                    }
                    break;

                case R.id.acceptBtn:
                    manageProposal(1);
                    break;

                case R.id.rejectBtn:
                    manageProposal(2);
                    break;

                default:
                    break;
            }
        }
    }

    /* ------------ NGO Section ------------ */
    private void verifyMember(final String childText) {
        DatabaseReference ref = mDatabase.child("users").child(currentUserUid);
        ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        Log.d(LOG, "verifyMember" + childText);
                        if (dataSnapshot.hasChild(childText)) {
                            Map map = (Map) dataSnapshot.getValue();
                            Map<String, Object> mapChild = (Map<String, Object>) map.get(childText);

                            if (mapChild.containsKey(userDetails.getUid())) {
                                setBtnDetails(childText, R.drawable.edit_text_form_red,
                                        R.color.white, R.string.remove_from_ngo,
                                        R.string.remove_user_from_ngo);
                            } else {
                                verifyProposalsReceived("ProposalsReceived");
                            }
                        } else {
                            verifyProposalsReceived("ProposalsReceived");
                        }
                    }

                    private void verifyProposalsReceived(final String childText) {
                        DatabaseReference dRef = mDatabase.child("users")
                                .child(currentUserUid).child(childText);
                        dRef.addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.getValue() != null){
                                            Map map = (Map) dataSnapshot.getValue();
                                            if (map.containsKey(userDetails.getUid())) {
                                                userOngBtn.setVisibility(View.GONE);

                                                if(isUserOng){
                                                    acceptBtn.setText(R.string.accept_ngo);
                                                } else {
                                                    acceptBtn.setText(R.string.accept_user);
                                                }
                                                proposalsLayout.setVisibility(View.VISIBLE);
                                            } else {
                                                verifyProposalsMade("ProposalsMade");
                                            }
                                        } else {
                                            verifyProposalsMade("ProposalsMade");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }

                    private void verifyProposalsMade(final String childText) {
                        DatabaseReference dRef = mDatabase.child("users")
                                .child(currentUserUid).child(childText);
                        dRef.addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.getValue() != null){
                                            Map map = (Map) dataSnapshot.getValue();
                                            if (map.containsKey(userDetails.getUid())) {
                                                setBtnDetails(childText,
                                                        R.drawable.edit_text_form_blue,
                                                        R.color.white,
                                                        R.string.remove_proposal_to_ngo,
                                                        R.string.remove_proposal_to_user);
                                            } else {
                                                verifyProposalsMadeRejected("ProposalsMadeRejected");
                                            }
                                        } else {
                                            verifyProposalsMadeRejected("ProposalsMadeRejected");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }

                    private void verifyProposalsMadeRejected(final String childText) {

                        DatabaseReference dRef = mDatabase.child("users")
                                .child(currentUserUid).child(childText);
                        dRef.addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.getValue() != null){
                                            Map map = (Map) dataSnapshot.getValue();
                                            if (map.containsKey(userDetails.getUid())) {
                                                setBtnDetails(childText,
                                                        R.drawable.edit_text_form_gray,
                                                        R.color.black,
                                                        R.string.rejected_proposal_from_ngo,
                                                        R.string.rejected_proposal_from_user);
                                            } else {
                                                verifyProposalsReceivedRejected("ProposalsReceivedRejected");
                                            }
                                        } else {
                                            verifyProposalsReceivedRejected("ProposalsReceivedRejected");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }

                    private void verifyProposalsReceivedRejected(final String childText) {

                        DatabaseReference dRef = mDatabase.child("users")
                                .child(currentUserUid).child(childText);
                        dRef.addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.getValue() != null){
                                            Map map = (Map) dataSnapshot.getValue();
                                            if (map.containsKey(userDetails.getUid())) {
                                                setBtnDetails(childText,
                                                        R.drawable.edit_text_form_gray,
                                                        R.color.black,
                                                        R.string.reject_ngo,
                                                        R.string.reject_user);
                                            } else {
                                                setBtnDetails("NoProposal",
                                                        R.drawable.edit_text_form_green,
                                                        R.color.white,
                                                        R.string.add_to_ngo,
                                                        R.string.add_user_in_ngo);
                                            }
                                        } else {
                                            setBtnDetails("NoProposal",
                                                    R.drawable.edit_text_form_green,
                                                    R.color.white,
                                                    R.string.add_to_ngo,
                                                    R.string.add_user_in_ngo);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }

                    private void setBtnDetails(String childText, int background, int color,
                                               int text_1, int text_2) {

                        proposalsLayout.setVisibility(View.GONE);
                        status = childText;
                        userOngBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(),
                                background));
                        userOngBtn.setTextColor(ContextCompat.getColor(getApplicationContext(),
                                color));
                        if(isUserOng) {
                            userOngBtn.setText(text_1);
                        } else {
                            userOngBtn.setText(text_2);
                        }
                        userOngBtn.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void manageProposal(final int number){

        final PercentRelativeLayout popUpLayout = (PercentRelativeLayout) findViewById(R.id.popUpLayout);

        TextView textAreYouSure = (TextView) findViewById(R.id.textAreYouSure);
        switch (number) {
            case 1:
                textAreYouSure.setText("Ești sigur că vrei să accepți\nsolicitarea venită de la\n"
                        + userDetails.getNickname() + "?");
                break;
            case 2:
                textAreYouSure.setText("Ești sigur că vrei să refuzi\nsolicitarea venită de la\n"
                        + userDetails.getNickname() + "?");
                break;
            case 3:
                textAreYouSure.setText("Ești sigur că vrei\nsă schimbi decizia luată anterior?");
                break;
            case 4:
                textAreYouSure.setText("Ești sigur că vrei să anulezi\nsolicitarea făcută lui\n"
                        + userDetails.getNickname() + "?");
                break;
            case 5:
                textAreYouSure.setText("Ești sigur că vrei să trimiți\no solicitarea către:\n"
                        + userDetails.getNickname() + "?");
                break;
            case 6:
                textAreYouSure.setText("Ești sigur că vrei\nsă renunți la membrul:\n"
                        + userDetails.getNickname() + "?");
                break;
            case 7:
                textAreYouSure.setText("Ești sigur că vrei\nsă renunți calitatea de membru?");
                break;
            default:
                break;
        }

        Button im_sure = (Button) findViewById(R.id.im_sure);
        im_sure.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                switch (number) {
                    case 1:
                        if (isUserOng) {
                            add("MemberOf", "Members");
                            getOngSupportedNumber(userDetails.getUid(), true);
//                            unsupportNGO();
                        } else {
                            add("Members", "MemberOf");
                            getOngSupportedNumber(currentUserUid, true);
//                            unsupportNGO();
                        }
                        remove("ProposalsReceived", "ProposalsMade", number);
                        break;
                    case 2:
                        add("ProposalsReceivedRejected", "ProposalsMadeRejected");
                        remove("ProposalsReceived", "ProposalsMade", number);
                        break;
                    case 3:
                        add("ProposalsReceived", "ProposalsMade");
                        remove("ProposalsReceivedRejected", "ProposalsMadeRejected", number);
                        break;
                    case 4:
                        remove("ProposalsMade", "ProposalsReceived", number);
                        break;
                    case 5:
                        add("ProposalsMade", "ProposalsReceived");
                        Toast.makeText(getApplicationContext(),
                                "Solicitarea a fost trimisă!",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 6:
                        remove("Members", "MemberOf", number);
                        getOngSupportedNumber(currentUserUid, false);
                        break;
                    case 7:
                        remove("MemberOf", "Members", number);
                        getOngSupportedNumber(userDetails.getUid(), false);
                    default:
                        break;
                }
                popUpLayout.setVisibility(View.GONE);
            }
        });

        Button im_not_sure = (Button) findViewById(R.id.im_not_sure);
        im_not_sure.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                popUpLayout.setVisibility(View.GONE);
            }

        });

        popUpLayout.setVisibility(View.VISIBLE);
    }

    private void remove(final String childText_1, final String childText_2, final int number) {

        mDatabase.child("users").child(currentUserUid)
                .child(childText_1).child(userDetails.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void aVoid) {
                       mDatabase.child("users").child(userDetails.getUid())
                               .child(childText_2).child(currentUserUid).removeValue()
                               .addOnSuccessListener(new OnSuccessListener<Void>() {
                                   @Override
                                   public void onSuccess(Void aVoid) {
                                       switch(number){
                                           case 1:
                                               if (isUserOng) {
                                                   Toast.makeText(getApplicationContext(),
                                                           "Felicitări! Ai devenit membru!",
                                                           Toast.LENGTH_SHORT).show();
                                               } else {
                                                   Toast.makeText(getApplicationContext(),
                                                           "Felicitări! Ai un nou membru!",
                                                           Toast.LENGTH_SHORT).show();
                                               }
                                               break;
                                           case 2:
                                               Toast.makeText(getApplicationContext(),
                                                       "Solicitarea a fost refuzată!",
                                                       Toast.LENGTH_SHORT).show();
                                               break;
                                           case 3:
                                               Toast.makeText(getApplicationContext(),
                                                       "Decizia a fost schimbată!",
                                                       Toast.LENGTH_SHORT).show();
                                               break;
                                           case 4:
                                               Toast.makeText(getApplicationContext(),
                                                       "Solicitarea a fost anulată!",
                                                       Toast.LENGTH_SHORT).show();
                                               break;
                                           case 6:
                                               Toast.makeText(getApplicationContext(),
                                                       "Membrul a fost înlăturat!",
                                                       Toast.LENGTH_SHORT).show();
                                               break;
                                           case 7:
                                               Toast.makeText(getApplicationContext(),
                                                       "Nu mai ești membru!",
                                                       Toast.LENGTH_SHORT).show();
                                               break;
                                           default:
                                               break;
                                       }
                                   }
                               });
                   }
               });
    }

    private void add(final String childText_1, final String childText_2) {

        final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        DatabaseReference ref = mDatabase.child("users").child(currentUserUid)
                .child(childText_1).child(userDetails.getUid());
        ref.setValue(String.valueOf(dateFormat.format(new Date())))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DatabaseReference dRef = mDatabase.child("users").child(userDetails.getUid())
                                .child(childText_2).child(currentUserUid);
                        dRef.setValue(String.valueOf(dateFormat.format(new Date())))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //TODO: Toast
                                    }
                                });
                    }
                });
    }

    //TODO: update number de fiecare data cand un user isi modifica ownCauses (nu aici, in alta activitate)
    private void getOngSupportedNumber(String ongUid, final boolean addNumber) {

        Log.d(LOG, "UID: " + ongUid);
        final DatabaseReference ref = mDatabase.child("users").child(ongUid)
                .child("ProfileSettings").child("membersCauses");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    String number = String.valueOf(dataSnapshot.getValue());
                    Log.d(LOG, "NgoNumber: " + number);

                    if (isUserOng) {
                        Log.d(LOG, "A");
                        getUserSupportedNumber(Integer.valueOf(number), currentUserUid, ref, addNumber);
                    } else {
                        Log.d(LOG, "B");
                        getUserSupportedNumber(Integer.valueOf(number), userDetails.getUid(), ref, addNumber);
                    }
                } else {
                    Log.d(LOG, "C");
                    getUserSupportedNumber(0, currentUserUid, ref, addNumber);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

            private void getUserSupportedNumber(final int number, String userUid,
                                                final DatabaseReference reference,
                                                final boolean addNumber) {

                Log.d(LOG, "Ajung aici!  " + userUid);
                final DatabaseReference ref = mDatabase.child("users").child(userUid)
                        .child("ProfileSettings").child("ownCauses");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(LOG, "UserNumber: " + dataSnapshot.getValue());
                        if(addNumber) {
                            reference.setValue(number
                                    + Integer.valueOf(String.valueOf(dataSnapshot.getValue())))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    });
                        } else {
                            reference.setValue(number
                                    - Integer.valueOf(String.valueOf(dataSnapshot.getValue())))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        });
    }

    /* ------------ End of NGO Section ------------ */

    /* ------------ Supporter Section ------------ */

    private void verifySupportedBtn() {
        DatabaseReference ref = mDatabase.child("users")
                .child(currentUserUid).child("MemberOf");
        ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null) {
                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                            if (!map.containsKey(userDetails.getUid())) {
                                setSupportedBtn();
                            } else {
                                supportBtn.setVisibility(View.GONE);
                            }
                        } else {
                            setSupportedBtn();
                        }
                    }

                    private void setSupportedBtn() {
                        DatabaseReference ref = mDatabase.child("users")
                                .child(currentUserUid).child("SupporterOf");
                        ref.addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.getValue() != null){
                                            Map<String, Object> map =
                                                    (Map<String, Object>) dataSnapshot.getValue();

                                            if (!map.containsKey(userDetails.getUid())) {
                                                supportBtn.setText(R.string.support_NGO);
                                                supportBtn.setBackground(ContextCompat.getDrawable(
                                                        getApplicationContext(),
                                                        R.drawable.edit_text_form_green));
                                            } else {
                                                supportBtn.setText(R.string.no_support_NGO);
                                                supportBtn.setBackground(ContextCompat.getDrawable(
                                                        getApplicationContext(),
                                                        R.drawable.edit_text_form_red));
                                            }
                                        } else {
                                            supportBtn.setText(R.string.support_NGO);
                                            supportBtn.setBackground(ContextCompat.getDrawable(
                                                    getApplicationContext(),
                                                    R.drawable.edit_text_form_green));
                                        }
                                        supportBtn.setVisibility(View.VISIBLE);
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

    private void supportNGO() {

        final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        DatabaseReference ref = mDatabase.child("users")
                .child(userDetails.getUid()).child("Supporters").child(currentUserUid);
        ref.setValue(String.valueOf(dateFormat.format(new Date())))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DatabaseReference ref = mDatabase.child("users")
                                .child(currentUserUid).child("SupporterOf").child(userDetails.getUid());
                        ref.setValue(String.valueOf(dateFormat.format(new Date())))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        supportBtn.setText(R.string.no_support_NGO);
                                        supportBtn.setBackground(ContextCompat.getDrawable(
                                                getApplicationContext(),
                                                R.drawable.edit_text_form_red));
                                    }
                                });
                    }
                });
    }

    private void unsupportNGO() {

        mDatabase.child("users").child(userDetails.getUid())
                .child("Supporters").child(currentUserUid).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mDatabase.child("users")
                                .child(currentUserUid).child("SupporterOf")
                                .child(userDetails.getUid()).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        supportBtn.setText(R.string.support_NGO);
                                        supportBtn.setBackground(ContextCompat.getDrawable(
                                                getApplicationContext(),
                                                R.drawable.edit_text_form_green));
                                    }
                                });
                    }
                });
    }

    /* ------------ End of Supporter Section ------------ */

    /* ------------ Profile Details Section ------------ */

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

    private Uri getCompressedImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private String getRealPath(Uri uri){
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

    /* ------------ End of Profile Details Section ------------ */

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}