package com.community.community.PublicProfile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.community.community.General.UsefulThings;
import com.community.community.General.User;
import com.community.community.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.blurry.Blurry;

public class PublicProfileActivity extends AppCompatActivity {

    private String LOG = this.getClass().getSimpleName();

    private DatabaseReference mDatabase = null;

    /* Profile details */
    private PercentRelativeLayout describeRelativeLayout = null;
    private PercentRelativeLayout addressRelativeLayout = null;
    private PercentRelativeLayout officialAddressRelativeLayout = null;
    private PercentRelativeLayout websiteRelativeLayout = null;
    private PercentRelativeLayout donateRelativeLayout = null;
    private ImageView blurImage = null;
    private CircleImageView circleImage = null;
    private TextView nickname = null;
    private TextView email = null;
    private TextView ownNumber = null;
    private TextView supportedNumber = null;
    private DocumentView describe = null;
    private DocumentView address = null;
    private DocumentView official_address = null;
    private TextView website = null;
    private TextView donate = null;

    /* Ngo Buttons */
    private LinearLayout proposalsLayout = null;
    private Button userOngBtn = null;
    private Button acceptBtn = null;

    /* Supporter */
    private TextView supportBtn = null;

    /* Profile User */
    private User currentUserProfile = null;
    private String realtimeOwnNumber = null;
    private String realtimeSupportedNumber = null;
    private String realtimeMembersNumber = null;
    private String status = null;

    /* Confirm changes */
    private boolean isUserOng = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.public_profile_activity);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        describeRelativeLayout = (android.support.percent.PercentRelativeLayout)
                findViewById(R.id.relative_layout_describe);
        blurImage = (ImageView) findViewById(R.id.blur_profile_image);
        circleImage = (CircleImageView) findViewById(R.id.profile_image);
        nickname = (TextView) findViewById(R.id.userNickname);
        email = (TextView) findViewById(R.id.userEmail);
        ownNumber = (TextView) findViewById(R.id.own_number);
        supportedNumber = (TextView) findViewById(R.id.supported_number);
        describe = (DocumentView) findViewById(R.id.describe_view);

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

        setSmallTexts();
        boolean isMe = setUpUserDetails(savedInstanceState);


        if(UsefulThings.currentUser.getType().equals("ngo")) {
            officialAddressRelativeLayout = (PercentRelativeLayout)
                    findViewById(R.id.relative_layout_official_address);
            websiteRelativeLayout = (PercentRelativeLayout)
                    findViewById(R.id.relative_layout_site);
            donateRelativeLayout = (PercentRelativeLayout)
                    findViewById(R.id.relative_layout_donate);

            setLayoutBelowAttribute(R.id.relative_layout_donate);
            official_address = (DocumentView) findViewById(R.id.official_address_view);
            website = (TextView) findViewById(R.id.site_view);
            donate = (TextView) findViewById(R.id.donate_view);
            setUpListeners();
        } else {
            addressRelativeLayout = (android.support.percent.PercentRelativeLayout)
                    findViewById(R.id.relative_layout_address);

            setLayoutBelowAttribute(R.id.relative_layout_address);
            address = (DocumentView) findViewById(R.id.address_view);

        }

        setUpProfileDetails(isMe);

        Button backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                onBackPressed();
            }

        });
    }

    private void setUpListeners() {

        website.setClickable(true);
        website.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String url = website.getText().toString();
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }

        });

        donate.setClickable(true);
        donate.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String url = donate.getText().toString();
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Log.d(LOG, "URL: " + url);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }

        });
    }

    private void setUpProfileDetails(boolean isMe) {

        if(isMe) {

            setProfileDetails(UsefulThings.currentUser, null);
            setProfilePicture(true);

            ImageButton editBtn = (ImageButton) findViewById(R.id.edit_btn);
            editBtn.setVisibility(View.VISIBLE);
            editBtn.setOnClickListener(callButtonClickListener);

        } else {
            setProfilePicture(false);

            if (currentUserProfile.getType().equals("ngo") &&
                    UsefulThings.currentUser.getType().equals("user")) {
                isUserOng = true;
                setProfileDetails(currentUserProfile, currentUserProfile.getUid());
                verifyMember("MemberOf");
                verifySupportedBtn();
            } else if (currentUserProfile.getType().equals("user") &&
                    UsefulThings.currentUser.getType().equals("ngo")) {
                setProfileDetails(currentUserProfile, UsefulThings.currentUser.getUid());
                verifyMember("Members");
            } else {
                setProfileDetails(currentUserProfile, null);
            }
        }
    }


    private void setLayoutBelowAttribute(int id) {
        PercentRelativeLayout userOng = (PercentRelativeLayout) findViewById(R.id.userOng);
        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) userOng.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, id);

        userOng.requestLayout();
    }

    private boolean setUpUserDetails(Bundle savedInstanceState) {

        if (UsefulThings.currentUser == null) {
            UsefulThings.currentUser = (User) savedInstanceState.getSerializable("userDetails");

            if(UsefulThings.currentUser == null) {
                Log.d(LOG, "Nu am detaliile user-ului curent!");
                finish();
            }
        }

        boolean isMe = true;
        Bundle b = getIntent().getExtras();

        if(b != null) {
            currentUserProfile = (User) b.getSerializable("userCauseDetails");

            if(!currentUserProfile.getUid().equals(UsefulThings.currentUser.getUid())){
                isMe = false;
            }
        }

        return isMe;
    }

    private void setSmallTexts() {
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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("userDetails", UsefulThings.currentUser);
        outState.putSerializable("currentUserProfile", currentUserProfile);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1){
            if(resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                if(bundle != null) {
                    if(bundle.getBoolean("changed")) {
                        setProfileDetails(UsefulThings.currentUser, null);
                    }
                    if(bundle.getBoolean("newPicture")) {
                        setProfilePicture(true);
                    }
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
                    startActivityForResult(i, 1);
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
                        unsupportNGO(UsefulThings.currentUser.getUid(),
                                currentUserProfile.getUid());
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
        DatabaseReference ref = mDatabase.child("users").child(UsefulThings.currentUser.getUid());
        ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
//                            Log.d(LOG, "verifyMember");
                        if (dataSnapshot.hasChild(childText)) {
                            Map map = (Map) dataSnapshot.getValue();
                            Map<String, Object> mapChild = (Map<String, Object>) map.get(childText);

                            if (mapChild.containsKey(currentUserProfile.getUid())) {
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
                        if(UsefulThings.currentUser != null) {
                            Log.d(LOG, "mDatabase: " + mDatabase);
                            Log.d(LOG, "UsefulThings.currentUser: " + UsefulThings.currentUser.getUid());
                            DatabaseReference dRef = mDatabase.child("users")
                                    .child(UsefulThings.currentUser.getUid()).child(childText);
                            dRef.addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getValue() != null) {
                                                Map map = (Map) dataSnapshot.getValue();
                                                if (map.containsKey(currentUserProfile.getUid())) {
                                                    userOngBtn.setVisibility(View.GONE);

                                                    if (isUserOng) {
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
                    }

                    private void verifyProposalsMade(final String childText) {
                        DatabaseReference dRef = mDatabase.child("users")
                                .child(UsefulThings.currentUser.getUid()).child(childText);
                        dRef.addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.getValue() != null){
                                            Map map = (Map) dataSnapshot.getValue();
                                            if (map.containsKey(currentUserProfile.getUid())) {
//                                            if (map.containsKey(userDetails.getUid())) {
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
                                .child(UsefulThings.currentUser.getUid()).child(childText);
                        dRef.addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.getValue() != null){
                                            Map map = (Map) dataSnapshot.getValue();
                                            if (map.containsKey(currentUserProfile.getUid())) {
//                                            if (map.containsKey(userDetails.getUid())) {
                                                setBtnDetails(childText,
                                                        R.drawable.edit_text_form_gray,
                                                        R.color.blue5,
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
                                .child(UsefulThings.currentUser.getUid()).child(childText);
                        dRef.addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.getValue() != null){
                                            Map map = (Map) dataSnapshot.getValue();
                                            if (map.containsKey(currentUserProfile.getUid())) {
                                                setBtnDetails(childText,
                                                        R.drawable.edit_text_form_gray,
                                                        R.color.blue5,
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

        final PercentRelativeLayout popUpLayout =
                (PercentRelativeLayout) findViewById(R.id.popUpLayout);

        TextView textAreYouSure = (TextView) findViewById(R.id.textAreYouSure);
        switch (number) {
            case 1:
                textAreYouSure.setText("Ești sigur că vrei să accepți\nsolicitarea venită de la\n"
                        + currentUserProfile.getNickname() + "?");
                break;
            case 2:
                textAreYouSure.setText("Ești sigur că vrei să refuzi\nsolicitarea venită de la\n"
                        + currentUserProfile.getNickname() + "?");
                break;
            case 3:
                textAreYouSure.setText("Ești sigur că vrei\nsă schimbi decizia luată anterior?");
                break;
            case 4:
                textAreYouSure.setText("Ești sigur că vrei să anulezi\nsolicitarea făcută lui\n"
                        + currentUserProfile.getNickname() + "?");
                break;
            case 5:
                textAreYouSure.setText("Ești sigur că vrei să trimiți\no solicitarea către:\n"
                        + currentUserProfile.getNickname() + "?");
                break;
            case 6:
                textAreYouSure.setText("Ești sigur că vrei\nsă renunți la membrul:\n"
                        + currentUserProfile.getNickname() + "?");
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
                            setMembersCauses(currentUserProfile.getUid(), true);
                            unsupportNGO(UsefulThings.currentUser.getUid(),
                                    currentUserProfile.getUid());
                        } else {
                            setMembersCauses(UsefulThings.currentUser.getUid(), true);
                            add("Members", "MemberOf");
                            unsupportNGO(currentUserProfile.getUid(),
                                    UsefulThings.currentUser.getUid());
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
                        setMembersCauses(UsefulThings.currentUser.getUid(), false);
                        break;
                    case 7:
                        remove("MemberOf", "Members", number);
                        setMembersCauses(currentUserProfile.getUid(), false);
                        break;
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

        mDatabase.child("users").child(currentUserProfile.getUid())
                .child(childText_2).child(UsefulThings.currentUser.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void aVoid) {
                       mDatabase.child("users").child(UsefulThings.currentUser.getUid())
                               .child(childText_1).child(currentUserProfile.getUid()).removeValue()
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

        DatabaseReference ref = mDatabase.child("users").child(currentUserProfile.getUid())
                .child(childText_2).child(UsefulThings.currentUser.getUid());
        ref.setValue(String.valueOf(dateFormat.format(new Date())))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DatabaseReference dRef = mDatabase.child("users")
                                .child(UsefulThings.currentUser.getUid())
                                .child(childText_1).child(currentUserProfile.getUid());
                        dRef.setValue(String.valueOf(dateFormat.format(new Date())))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                    }
                                });
                    }
                });
    }

    private void setMembersCauses(String ongUid, final boolean addNumber) {

//        Log.d(LOG, "realtimeMembersNumber: " + realtimeMembersNumber);
//        Log.d(LOG, "realtimeOwnNumber: " + realtimeOwnNumber);

        DatabaseReference ref = mDatabase.child("users").child(ongUid)
                .child("ProfileSettings").child("membersCauses");
        if(addNumber) {
            ref.setValue(Integer.valueOf(realtimeMembersNumber)
                    + Integer.valueOf(realtimeOwnNumber))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });
        } else {
            ref.setValue(Integer.valueOf(realtimeMembersNumber)
                    - Integer.valueOf(realtimeOwnNumber))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });
        }
    }

    /* ------------ End of NGO Section ------------ */

    /* ------------ Supporter Section ------------ */

    private void verifySupportedBtn() {
        DatabaseReference ref = mDatabase.child("users")
                .child(UsefulThings.currentUser.getUid()).child("MemberOf");
        ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null) {
                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                            if (!map.containsKey(currentUserProfile.getUid())) {
                                setSupportedBtn();
                            } else {
                                supportBtn.setVisibility(View.GONE);
                            }
                        } else {
                            setSupportedBtn();
                        }
                    }

                    private void setSupportedBtn() {
                        if(UsefulThings.currentUser != null) {
                            DatabaseReference ref = mDatabase.child("users")
                                    .child(UsefulThings.currentUser.getUid()).child("SupporterOf");
                            ref.addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getValue() != null) {
                                                Map<String, Object> map =
                                                        (Map<String, Object>) dataSnapshot.getValue();

                                                if (!map.containsKey(currentUserProfile.getUid())) {
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
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void supportNGO() {

        final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        DatabaseReference ref = mDatabase.child("users")
                .child(UsefulThings.currentUser.getUid())
                .child("SupporterOf").child(currentUserProfile.getUid());
        ref.setValue(String.valueOf(dateFormat.format(new Date())))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DatabaseReference ref = mDatabase.child("users")
                                .child(currentUserProfile.getUid()).child("Supporters")
                                .child(UsefulThings.currentUser.getUid());
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

    private void unsupportNGO(final String userUid,final String ongUid) {

        mDatabase.child("users").child(userUid)
                .child("SupporterOf").child(ongUid).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mDatabase.child("users")
                                .child(ongUid).child("Supporters")
                                .child(userUid).removeValue()
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

    private void setProfileDetails(User user, String ongUid) {

        nickname.setText(user.getNickname());
        email.setText(user.getEmail());

        if(user.getDescribe() != null &&
                !user.getDescribe().equals("")){
            describe.setText(user.getDescribe());
            describeRelativeLayout.setVisibility(View.VISIBLE);
        } else {
            describeRelativeLayout.setVisibility(View.GONE);
        }

        if(user.getType().equals("ngo")) {
            if (user.getOfficial_address() != null &&
                    !user.getOfficial_address().equals("")) {
                official_address.setText(user.getOfficial_address());
                officialAddressRelativeLayout.setVisibility(View.VISIBLE);
            } else {
                officialAddressRelativeLayout.setVisibility(View.GONE);
            }

            if (user.getSite() != null &&
                    !user.getSite().equals("")) {
                website.setText(user.getSite());
                websiteRelativeLayout.setVisibility(View.VISIBLE);
            } else {
                websiteRelativeLayout.setVisibility(View.GONE);
            }

            if (user.getDonate() != null &&
                    !user.getDonate().equals("")) {
                donate.setText(user.getDonate());
                donateRelativeLayout.setVisibility(View.VISIBLE);
            } else {
                donateRelativeLayout.setVisibility(View.GONE);
            }
        } else {
            if (user.getAddress() != null &&
                    !user.getAddress().equals("")) {
                address.setText(user.getAddress());
                addressRelativeLayout.setVisibility(View.VISIBLE);
            } else {
                addressRelativeLayout.setVisibility(View.GONE);
            }
        }

        DatabaseReference ref = mDatabase.child("users").child(user.getUid())
                .child("ProfileSettings").child("ownCauses");
        ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null) {
                            realtimeOwnNumber = dataSnapshot.getValue().toString();
                            ownNumber.setText(realtimeOwnNumber);
                        } else {
                            ownNumber.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        ref = mDatabase.child("users").child(user.getUid())
                .child("ProfileSettings").child("supportedCauses");
        ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null) {
                            realtimeSupportedNumber = dataSnapshot.getValue().toString();
                            supportedNumber.setText(realtimeSupportedNumber);
                        } else {
                            supportedNumber.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        if(ongUid != null) {
            ref = mDatabase.child("users").child(ongUid)
                    .child("ProfileSettings").child("membersCauses");
            ref.addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                realtimeMembersNumber = dataSnapshot.getValue().toString();
                            } else {
                                realtimeMembersNumber = "0";
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    private void setProfilePicture(boolean isCurrentUser) {
        Bitmap icon;

        if(isCurrentUser) {
            try {
                Log.d(LOG, "myImage_");
                icon = BitmapFactory.decodeStream(PublicProfileActivity.this
                        .openFileInput("myImage_" + UsefulThings.currentUser.getEmail()));
                setProfileImages(icon);
            } catch (FileNotFoundException e) {
                Log.d(LOG, "profileImage");
                e.printStackTrace();
                icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.profile);
                setProfileImages(icon);
            }
        } else {
            DatabaseReference ref = mDatabase.child("users").child(currentUserProfile.getUid())
                    .child("ProfileSettings").child("imageURL");
            ref.addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue() != null){
                                String url = (String) dataSnapshot.getValue();
                                currentUserProfile.setImageURL(url);
                                downloadImage(url);
                            } else {
                                Bitmap icon = BitmapFactory.decodeResource(
                                        getApplicationContext().getResources(),
                                        R.drawable.profile);
                                setProfileImages(icon);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    private void setProfileImages(Bitmap icon) {
        blurImage.setImageBitmap(icon);
        Blurry.with(getApplicationContext())
//                .radius(50)
                .async()
                .from(icon)
                .into(blurImage);

        circleImage.setImageBitmap(icon);
    }

    private void downloadImage(String url) {

        Glide
                .with(this)
                .load(url)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        setProfileImages(resource);
                    }
                });
    }

    /* ------------ End of Profile Details Section ------------ */

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        finish();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG, "onDestroy");
    }
}