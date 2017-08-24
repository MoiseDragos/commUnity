package com.community.community;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.community.community.General.UsefulThings;
import com.community.community.General.User;
import com.community.community.PublicProfile.PublicProfileActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class NgoActivity extends AppCompatActivity {

    private String LOG = this.getClass().getSimpleName();

    private static ArrayList<PercentRelativeLayout> allPercents
            = new ArrayList<>();

    private ScrollView noCausesLayout = null;
    private ScrollView causesLayout = null;

    private PercentRelativeLayout rootLayout = null;

    private DatabaseReference mDatabase = null;

    private SparseArray<String> idsSparseArray = null;

    private int number;
    private int lastBtn = 1;
    private int unit;
    
    private Button memberBtn = null;
    private Button supporterBtn = null;
    private Button allBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ngo_activity);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        memberBtn = (Button) findViewById(R.id.membersBtn);
        memberBtn.setOnClickListener(callButtonClickListener);
        supporterBtn = (Button) findViewById(R.id.supporterBtn);
        supporterBtn.setOnClickListener(callButtonClickListener);
        allBtn = (Button) findViewById(R.id.allBtn);
        allBtn.setOnClickListener(callButtonClickListener);

        noCausesLayout = (ScrollView) findViewById(R.id.scroll_view_no_causes);
        causesLayout = (ScrollView) findViewById(R.id.scroll_view_causes);
        rootLayout = (PercentRelativeLayout) findViewById(R.id.rootLayout);

        TextView title = (TextView) findViewById(R.id.textView);

        if (UsefulThings.currentUser == null) {
            UsefulThings.currentUser = (User) savedInstanceState.getSerializable("userDetails");

            if(UsefulThings.currentUser == null) {
                Log.d(LOG, "Nu am detaliile user-ului curent!");
                finish();
            }
        }

        allBtn.setText(R.string.all);
        if(UsefulThings.currentUser.getType().equals("ngo")) {
            unit = UsefulThings.NGO_INTERMEDIATE_IDS;
            title.setText(R.string.ngo_details_title);
            memberBtn.setText(R.string.members);
            supporterBtn.setText(R.string.supporters);
            displayNgo(1);
        } else {
            unit = UsefulThings.USERS_INTERMEDIATE_IDS;
            title.setText(R.string.ngo_title);
            memberBtn.setText(R.string.member);
            supporterBtn.setText(R.string.supported);
            displayUser(1);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("userDetails", UsefulThings.currentUser);
    }

    private void displayNgo(int i) {

        resetColors();

        number = 1 - unit;

        String no_ong;
        switch (i) {
            case 1:
                memberBtn.setBackgroundColor(
                        ContextCompat.getColor(getApplicationContext(), R.color.blue1));
                no_ong = getString(R.string.no_member);
                displayDetails("Members", no_ong, false, i);
                break;
            case 2:
                supporterBtn.setBackgroundColor(
                        ContextCompat.getColor(getApplicationContext(), R.color.blue1));
                no_ong = getString(R.string.no_supporter);
                displayDetails("Supporters", no_ong, false, i);
                break;
            case 3:
                allBtn.setBackgroundColor(
                        ContextCompat.getColor(getApplicationContext(), R.color.blue1));
                no_ong = getString(R.string.no_ngo);
                displayDetails("ngo", no_ong, true, i);
                break;
            default:
                break;
        }

    }

    private void displayUser(int i) {

        number = 1 - unit;

        resetColors();
        String no_ong;
        switch (i) {
            case 1:
                memberBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue1));
                no_ong = getString(R.string.no_member_of);
                displayDetails("MemberOf", no_ong, false, i);
                break;
            case 2:
                supporterBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue1));
                no_ong = getString(R.string.no_supporter_of);
                displayDetails("SupporterOf", no_ong, false, i);
                break;
            case 3:
                allBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue1));
                no_ong = getString(R.string.no_ngo);
                displayDetails("ngo", no_ong, true, i);
                break;
            default:
                break;
        }
    }

    private void displayDetails(final String child, final String no_ong,
                                boolean isAll, final int i){

        final DatabaseReference ref;
        if(!isAll) {
            ref = mDatabase.child("users").child(UsefulThings.currentUser.getUid());
        } else {
            ref = mDatabase;
        }


        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(child)) {
                            allPercents = new ArrayList<>();

                            noCausesLayout.setVisibility(View.GONE);
                            causesLayout.setVisibility(View.VISIBLE);

                            idsSparseArray = new SparseArray<>();

                            DatabaseReference dRef = ref.child(child);
                            dRef.addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            Map<String, Object> map =
                                                    (Map<String, Object>) dataSnapshot.getValue();

                                            for (Map.Entry<String, Object> entry : map.entrySet()) {

                                                number += unit;
                                                downloadDetails(entry.getKey(), i, number);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                        } else {
                            noCausesLayout.setVisibility(View.VISIBLE);
                            causesLayout.setVisibility(View.GONE);

                            TextView noCause = (TextView) findViewById(R.id.no_causes);
                            noCause.setText(no_ong);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void downloadDetails(final String key, final int child, final int currentNumber) {

        DatabaseReference reference = mDatabase.child("users").child(key).child("ProfileSettings");
        reference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                        final String name = map.get("nickname").toString();
                        final String ownCauses = map.get("ownCauses").toString();

                        final String imageURL;
                        if(map.containsKey("imageURL")){
                            imageURL = map.get("imageURL").toString();
                        } else {
                            imageURL = null;
                        }

                        idsSparseArray.put(currentNumber, key);

                        if(UsefulThings.currentUser.getType().equals("user") || child == 3) {
                            final String allCauses;

                            if(map.containsKey("allCauses")) {
                                allCauses = map.get("allCauses").toString();
                            } else {
                                allCauses = "0";
                            }

                            DatabaseReference ref = mDatabase.child("users").child(key)
                                    .child("Supporters");
                            ref.addValueEventListener(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.getValue() != null) {
                                                Map<String, Object> supportersMap =
                                                        (Map<String, Object>) dataSnapshot.getValue();
                                                determineMembers(String.valueOf(supportersMap.size()));
                                            } else {
                                                determineMembers("0");
                                            }
                                        }

                                        private void determineMembers(final String supporters) {
                                            DatabaseReference ref = mDatabase.child("users").child(key)
                                                    .child("Members");
                                            ref.addValueEventListener(
                                                    new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if(UsefulThings.currentUser != null) {
                                                                if (dataSnapshot.getValue() != null) {
                                                                    Map<String, Object> supportersMap =
                                                                            (Map<String, Object>) dataSnapshot.getValue();
                                                                    buildLayout(name, ownCauses, allCauses,
                                                                            supporters, imageURL, null, null,
                                                                            String.valueOf(supportersMap.size()),
                                                                            child, currentNumber);
                                                                } else {
                                                                    buildLayout(name, ownCauses, allCauses,
                                                                            supporters, imageURL, null, null,
                                                                            "0", child, currentNumber);
                                                                }
                                                            }
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
                        } else {
                            final String email = map.get("email").toString();
                            String childString;

                            if(child == 2) {
                                childString = "Supporters";
                            } else {
                                childString = "Members";
                            }

                            DatabaseReference ref = mDatabase.child("users")
                                    .child(UsefulThings.currentUser.getUid())
                                    .child(childString).child(key);

                            Log.d(LOG, ref.toString());
                            ref.addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String date = String.valueOf(dataSnapshot.getValue());

                                            buildLayout(name, ownCauses, null, null,
                                                    imageURL, email, date, null,
                                                    child, currentNumber);
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

    private NgoActivity.CallButtonClickListener callButtonClickListener
            = new NgoActivity.CallButtonClickListener();
    private class CallButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.membersBtn:
                    if(lastBtn != 1) {
                        lastBtn = 1;
                        removeLayouts();
                        if (UsefulThings.currentUser.getType().equals("ngo")) {
                            displayNgo(1);
                        } else {
                            displayUser(1);
                        }
                    }
                    break;
                case R.id.supporterBtn:
                    if(lastBtn != 2) {
                        lastBtn = 2;
                        removeLayouts();
                        if (UsefulThings.currentUser.getType().equals("ngo")) {
                            displayNgo(2);
                        } else {
                            displayUser(2);
                        }
                    }
                    break;
                case R.id.allBtn:
                    if(lastBtn != 3) {
                        lastBtn = 3;
                        removeLayouts();
                        if (UsefulThings.currentUser.getType().equals("ngo")) {
                            displayNgo(3);
                        } else {
                            displayUser(3);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void buildLayout(String name, String ownCauses, String allCauses,
                             String supporters, String imageURL, String email,
                             String date, String members, int child, int currentNumber) {

        PercentRelativeLayout parent = setParentLayout(currentNumber);

        setImage(imageURL, parent, currentNumber);
        setTitle(name, parent, currentNumber);
        setVerticalLine(parent, 0, currentNumber);

        if(UsefulThings.currentUser.getType().equals("user") || child == 3){
            setColumn("Număr\nmembri", parent, 0, currentNumber);
            setColumnText(members, parent, 0, currentNumber);
            setColumn("Susținători", parent, 1, currentNumber);
            setColumnText(supporters, parent, 1, currentNumber);
            setColumn("Total\nCauze", parent, 2, currentNumber);
            setColumnText(String.valueOf(Integer.valueOf(ownCauses)
                    + Integer.valueOf(allCauses)), parent, 2, currentNumber);
            setVerticalLine(parent, 1, currentNumber);
        } else {
            setColumn("Obiective\nadăugate", parent, 0, currentNumber);
            setColumnText(ownCauses, parent, 0, currentNumber);
            setEmailText(email, parent, currentNumber);
            setHorizontalLine(parent, currentNumber);
            setDateText(date, parent, currentNumber);
        }
    }

    private void setDateText(String email, PercentRelativeLayout parent, int currentNumber) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        child.setId(currentNumber + 6);
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, currentNumber + 5);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.38f;
        info.heightPercent = 0.3f;

        if(currentNumber % 2 == 0) {
            info.leftMarginPercent = 0.21f;
        } else {
            info.leftMarginPercent = 0.61f;
        }

        child.requestLayout();

        TextView title = new TextView(getApplicationContext());
        child.addView(title);

        title.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));
        title.setGravity(Gravity.CENTER);
        title.setMaxLines(1);
        title.setText(email);
        title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
    }

    private void setHorizontalLine(PercentRelativeLayout parent, int currentNumber) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        child.setId(currentNumber + 5);
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, currentNumber + 4);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.36f;
        info.heightPercent = 0.006f;

        if(currentNumber % 2 == 0) {
            info.leftMarginPercent = 0.22f;
        } else {
            info.leftMarginPercent = 0.62f;
        }

        child.requestLayout();

        View view = new View(getApplicationContext());
        child.addView(view);

        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        view.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));
    }

    private void setEmailText(String date, PercentRelativeLayout parent, int currentNumber) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        child.setId(currentNumber + 4);
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, currentNumber + 2);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.38f;
        info.heightPercent = 0.3f;

        if(currentNumber % 2 == 0) {
            info.leftMarginPercent = 0.21f;
        } else {
            info.leftMarginPercent = 0.61f;
        }

        child.requestLayout();

        TextView title = new TextView(getApplicationContext());
        child.addView(title);

        title.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));
        title.setGravity(Gravity.CENTER);
        title.setMaxLines(1);
        title.setText(date);
        title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
    }

    private void setVerticalLine(PercentRelativeLayout parent, int i, int currentNumber) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, currentNumber + 2);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.002f;
        info.heightPercent = 1;
        info.bottomMarginPercent = 0.02f;

        if(currentNumber % 2 == 0) {
            info.leftMarginPercent = 0.198f + 0.2f * i;
        } else {
            info.leftMarginPercent = 0.598f + 0.2f * i;
        }

        child.requestLayout();

        View view = new View(getApplicationContext());
        child.addView(view);

        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        view.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));
    }

    private void setColumnText(String text, PercentRelativeLayout parent, int i, int currentNumber) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, currentNumber + 3);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();

        info.widthPercent = 0.18f;
        info.heightPercent = 0.26f;
        info.bottomMarginPercent = 0.01f;

        if(currentNumber % 2 == 0) {
            info.leftMarginPercent = 0.01f + 0.2f * i;
        } else {
            info.leftMarginPercent = 0.41f + 0.2f * i;
        }


        child.requestLayout();

        TextView title = new TextView(getApplicationContext());
        child.addView(title);

        title.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));
        title.setGravity(Gravity.CENTER);
        title.setMaxLines(1);
        title.setText(text);

        title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private void setColumn(String text, PercentRelativeLayout parent, int i, int currentNumber) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        child.setId(currentNumber + 3 + i);
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, currentNumber + 2);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();

        info.widthPercent = 0.18f;
        info.heightPercent = 0.36f;
        info.bottomMarginPercent = 0.01f;

        if(currentNumber % 2 == 0) {
            info.leftMarginPercent = 0.01f + 0.2f * i;
        } else {
            info.leftMarginPercent = 0.41f + 0.2f * i;
        }

        child.requestLayout();

        TextView textAdd = new TextView(getApplicationContext());
        child.addView(textAdd);

        textAdd.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));

        textAdd.setGravity(Gravity.CENTER);
        textAdd.setMaxLines(2);
        textAdd.setText(text);
        textAdd.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        textAdd.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
    }

    private void setTitle(String titleText, PercentRelativeLayout parent, int currentNumber) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        child.setId(currentNumber + 2);
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.58f;
        info.heightPercent = 0.35f;
        info.bottomMarginPercent = 0.01f;

        if(currentNumber % 2 == 0) {
            info.leftMarginPercent = 0.02f;
        } else {
            info.leftMarginPercent = 0.42f;
        }
        child.requestLayout();

        TextView title = new TextView(getApplicationContext());
        child.addView(title);

        title.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));
        title.setGravity(Gravity.CENTER);
        title.setMaxLines(2);
        title.setText(titleText);
        title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        title.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private void setImage(String imageURL, PercentRelativeLayout parent, int currentNumber) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.38f;
        info.heightPercent = 1;
        info.topMarginPercent = 0.02f;
        info.bottomMarginPercent = 0.02f;

        if(currentNumber % 2 == 0) {
            info.leftMarginPercent = 0.6f;
        } else {
            info.leftMarginPercent = 0.01f;
        }
        child.requestLayout();

        final ImageView image = new ImageView(getApplicationContext());
        image.setId(currentNumber + 1);
        child.addView(image);

        image.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));

        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setClickable(true);

        if(imageURL != null) {
            Glide
                    .with(getApplication())
                    .load(imageURL)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {

                            image.setImageBitmap(resource);
                            image.setOnClickListener(callImageButtonClickListener);
                        }
                    });
        } else {
            image.setImageResource(R.drawable.profile);
            image.setOnClickListener(callImageButtonClickListener);
        }
    }

    private PercentRelativeLayout setParentLayout(int currentNumber) {


        PercentRelativeLayout parent = new PercentRelativeLayout(getApplicationContext());
        parent.setId(currentNumber);
        parent.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue4));
        rootLayout.addView(parent);
        allPercents.add(parent);

        PercentRelativeLayout.LayoutParams params =
                (PercentRelativeLayout.LayoutParams) parent.getLayoutParams();

        if(currentNumber != 1) {
            params.addRule(PercentRelativeLayout.BELOW, currentNumber - unit);
        }

        PercentLayoutHelper.PercentLayoutInfo info = params.getPercentLayoutInfo();
        info.widthPercent = 1;
        info.heightPercent = 0.27f;
        info.bottomMarginPercent = 0.02f;
        info.leftMarginPercent = 0.02f;
        info.rightMarginPercent = 0.02f;
        parent.setPadding(8, 8, 8, 8);
        parent.requestLayout();

        return parent;
    }

    private CallImageButtonClickListener callImageButtonClickListener =
            new CallImageButtonClickListener();
    private class CallImageButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            for(int i = 2; i <= number + 2; i += UsefulThings.NGO_INTERMEDIATE_IDS) {
                if(view.getId() == i){
                    startProfileActivity(idsSparseArray.get(i - 1));
                    break;
                }
            }
        }

        private void startProfileActivity(final String ownerUID) {
            DatabaseReference ref = mDatabase.child("users")
                    .child(ownerUID).child("ProfileSettings");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    User mUser = new User();
                    Map mapRef = (Map) snapshot.getValue();

                    mUser.setNickname(String.valueOf(mapRef.get("nickname")));
                    mUser.setEmail(String.valueOf(mapRef.get("email")));
                    mUser.setType(String.valueOf(mapRef.get("type")));
                    mUser.setUid(ownerUID);
                    mUser.setOwnCausesNumber(
                            Integer.valueOf(String.valueOf(mapRef.get("ownCauses"))));
                    mUser.setSupportedCausesNumber(
                            Integer.valueOf(String.valueOf(mapRef.get("supportedCauses"))));

                    Object ref = mapRef.get("address");
                    if(ref != null) {
                        mUser.setAddress(String.valueOf(ref));
                    }

                    ref = mapRef.get("describe");
                    if(ref != null) {
                        mUser.setDescribe(String.valueOf(ref));
                    }

                    if(ref != null) {
                        mUser.setAge(Integer.valueOf(String.valueOf(mapRef.get("age"))));
                    }

                    Intent i = new Intent(getApplicationContext(), PublicProfileActivity.class);
                    i.putExtra("userCauseDetails", mUser);
                    startActivity(i);
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void resetColors(){
        memberBtn.setBackgroundColor(
                ContextCompat.getColor(getApplicationContext(), R.color.blue4));
        supporterBtn.setBackgroundColor(
                ContextCompat.getColor(getApplicationContext(), R.color.blue4));
        allBtn.setBackgroundColor(
                ContextCompat.getColor(getApplicationContext(), R.color.blue4));
    }

    private void removeLayouts(){
        for (PercentRelativeLayout allPercent : allPercents) {
            rootLayout.removeView(allPercent);
        }
    }
}
