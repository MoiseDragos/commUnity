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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.util.LruCache;
import com.community.community.General.UsefulThings;
import com.community.community.General.User;
import com.community.community.PublicProfile.PublicProfileActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class ProposalsActivity extends AppCompatActivity {

    private String LOG = this.getClass().getSimpleName();

    private static LruCache<Integer, PercentRelativeLayout> allPercents
        = new LruCache<>(UsefulThings.proposalsCacheSize);

    private PercentRelativeLayout percentLayout2 = null;

    private PercentRelativeLayout no_processing_layout = null;
    private PercentRelativeLayout no_rejected_layout = null;
    private PercentRelativeLayout rootLayout = null;

    private DatabaseReference mDatabase = null;

    private SparseArray<String> idsSparseArray = null;

    private int number;
    private boolean isReceived = true;
    private boolean firstProcessing;
    private boolean firstRejected;

    private Button receivedBtn = null;
    private Button madeBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.proposals_activity);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        percentLayout2 = (PercentRelativeLayout) findViewById(R.id.percentLayout2);

        rootLayout = (PercentRelativeLayout) findViewById(R.id.rootLayout);
        no_processing_layout = (PercentRelativeLayout) findViewById(R.id.no_processing_layout);
        no_rejected_layout = (PercentRelativeLayout) findViewById(R.id.no_rejected_layout);

        receivedBtn = (Button) findViewById(R.id.membersBtn);
        receivedBtn.setOnClickListener(callButtonClickListener);
        madeBtn = (Button) findViewById(R.id.supporterBtn);
        madeBtn.setOnClickListener(callButtonClickListener);

        if (UsefulThings.currentUser == null) {
            UsefulThings.currentUser = (User) savedInstanceState.getSerializable("userDetails");

            if(UsefulThings.currentUser == null) {
                Log.d(LOG, "Nu am detaliile user-ului curent!");
                finish();
            }
        }

        chooseWhatToShow(1);

        Button backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                onBackPressed();
            }

        });
    }

    private void chooseWhatToShow(int i) {

        firstProcessing = true;
        firstRejected = true;

        number = 1 - UsefulThings.PROPOSALS_INTERMEDIATE_IDS;

        receivedBtn.setBackgroundColor(
                ContextCompat.getColor(getApplicationContext(), R.color.blue4));
        madeBtn.setBackgroundColor(
                ContextCompat.getColor(getApplicationContext(), R.color.blue4));
        no_processing_layout.setVisibility(View.GONE);
        no_rejected_layout.setVisibility(View.GONE);

        if(i == 1) {
            isReceived = true;
            receivedBtn.setBackgroundColor(
                    ContextCompat.getColor(getApplicationContext(), R.color.blue1));
            displayAll("Received");
        } else if (i == 2) {
            isReceived = false;
            madeBtn.setBackgroundColor(
                    ContextCompat.getColor(getApplicationContext(), R.color.blue1));
            displayAll("Made");
        }

    }

    private void displayAll(String text){
        displayDetails(text, true);
        displayDetails(text + "Rejected", false);
    }

    private void displayDetails(final String text, final boolean isProcessing){

        final DatabaseReference ref = mDatabase.child("users")
                .child(UsefulThings.currentUser.getUid());
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(text.equals("Received")) {
                            removeLayouts();
                            allPercents = new LruCache<>(UsefulThings.proposalsCacheSize);
                        }

                        if (dataSnapshot.hasChild("Proposals" + text)) {

                            idsSparseArray = new SparseArray<>();

                            DatabaseReference dRef = ref.child("Proposals" + text);
                            dRef.addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            Map<String, Object> map =
                                                    (Map<String, Object>) dataSnapshot.getValue();

                                            for (Map.Entry<String, Object> entry : map.entrySet()) {

                                                downloadDetails(entry.getKey(), isProcessing);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                        } else {

                            if(isProcessing) {
                                no_processing_layout.setVisibility(View.VISIBLE);
                                PercentRelativeLayout.LayoutParams linearParams =
                                        (PercentRelativeLayout.LayoutParams) percentLayout2.getLayoutParams();
                                linearParams.addRule(PercentRelativeLayout.BELOW,
                                        R.id.no_processing_layout);
                            } else {
                                no_rejected_layout.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void downloadDetails(final String key, final boolean isProcessing) {

        DatabaseReference reference = mDatabase.child("users")
                .child(key).child("ProfileSettings");
        reference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(UsefulThings.currentUser != null) {
                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                            final String name = map.get("nickname").toString();
                            final String imageURL;

                            if (map.containsKey("imageURL")) {
                                imageURL = map.get("imageURL").toString();
                            } else {
                                imageURL = null;
                            }

                            if (UsefulThings.currentUser.getType().equals("ngo")) {
                                DatabaseReference ref = mDatabase.child("users").child(key)
                                        .child("ProfileSettings").child("ownCauses");
                                Log.d(LOG, "ref: " + ref);
                                ref.addValueEventListener(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(UsefulThings.currentUser != null) {
                                                    long ownCausesMap =
                                                            (long) dataSnapshot.getValue();
                                                    determineSupporters(String.valueOf(ownCausesMap));
                                                }
                                            }

                                            private void determineSupporters(final String supporters) {
                                                DatabaseReference ref = mDatabase.child("users").child(key)
                                                        .child("Supporters");
                                                ref.addValueEventListener(
                                                        new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                number += UsefulThings.PROPOSALS_INTERMEDIATE_IDS;
                                                                idsSparseArray.put(number, key);

                                                                if (dataSnapshot.getValue() != null) {
                                                                    Map<String, Object> supportersMap =
                                                                            (Map<String, Object>) dataSnapshot.getValue();

                                                                    buildLayout(name,
                                                                            String.valueOf(supportersMap.size()),
                                                                            supporters, imageURL, isProcessing);
                                                                } else {
                                                                    buildLayout(name, "0",
                                                                            supporters, imageURL, isProcessing);
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
                                DatabaseReference ref = mDatabase.child("users").child(key)
                                        .child("Supporters");
                                ref.addValueEventListener(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.getValue() != null) {
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

                                                                number += UsefulThings.PROPOSALS_INTERMEDIATE_IDS;
                                                                idsSparseArray.put(number, key);

                                                                if (dataSnapshot.getValue() != null) {
                                                                    Map<String, Object> supportersMap =
                                                                            (Map<String, Object>) dataSnapshot.getValue();

                                                                    buildLayout(name,
                                                                            String.valueOf(supportersMap.size()),
                                                                            supporters, imageURL, isProcessing);
//                                                                buildLayout(name, ownCauses, allCauses,
//                                                                        supporters, imageURL, null, null,
//                                                                        String.valueOf(supportersMap.size()),
//                                                                        child, currentNumber);
                                                                } else {
                                                                    buildLayout(name, "0",
                                                                            supporters, imageURL, isProcessing);
//                                                                buildLayout(name, ownCauses, allCauses,
//                                                                        supporters, imageURL, null, null,
//                                                                        "0", child, currentNumber);
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
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void removeLayouts(){

        for(int i = 1; i <= number + 1; i += UsefulThings.PROPOSALS_INTERMEDIATE_IDS){
            rootLayout.removeView(allPercents.get(i));
            allPercents.clearMemory();
        }

    }

    private ProposalsActivity.CallButtonClickListener callButtonClickListener
            = new ProposalsActivity.CallButtonClickListener();
    private class CallButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.membersBtn:
                    if(!isReceived) {
                        removeLayouts();
                        chooseWhatToShow(1);
                    }
                    break;
                case R.id.supporterBtn:
                    if(isReceived) {
                        removeLayouts();
                        chooseWhatToShow(2);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void buildLayout(String name, String firstTextField, String secondTextField,
                             String imageURL, boolean isProcessing) {

        PercentRelativeLayout parent = setParentLayout(isProcessing);

        setImage(imageURL, parent);

        setTitle(name, parent);

        setAddedObjects(parent);
        setAddedText(firstTextField, parent);

        setVerticalLine(parent);

        setSupportersObjects(parent);
        setSupportersText(secondTextField, parent);
    }

    private void setVerticalLine(PercentRelativeLayout parent) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, number + 2);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.005f;
        info.heightPercent = 1;
        info.bottomMarginPercent = 0.02f;

        if(number % 2 == 0) {
            info.leftMarginPercent = 0.297f;
        } else {
            info.leftMarginPercent = 0.697f;
        }

        child.requestLayout();

        View view = new View(getApplicationContext());
        child.addView(view);

        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        view.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));
    }

    private void setSupportersText(String secondTextField, PercentRelativeLayout parent) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, number + 4);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.26f;
        info.heightPercent = 0.26f;
        info.bottomMarginPercent = 0.01f;

        if(number % 2 == 0) {
            info.leftMarginPercent = 0.32f;
        } else {
            info.leftMarginPercent = 0.72f;
            info.rightMarginPercent = 0.02f;
        }

        child.requestLayout();

        TextView title = new TextView(getApplicationContext());
        child.addView(title);

        title.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));
        title.setGravity(Gravity.CENTER);
        title.setMaxLines(1);
        title.setText(secondTextField);
        title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        title.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private void setSupportersObjects(PercentRelativeLayout parent) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        child.setId(number + 4);
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, number + 2);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.26f;
        info.heightPercent = 0.35f;
        info.bottomMarginPercent = 0.01f;

        if(number % 2 == 0) {
            info.leftMarginPercent = 0.32f;
        } else {
            info.leftMarginPercent = 0.72f;
            info.rightMarginPercent = 0.02f;
        }

        child.requestLayout();

        TextView textAdd = new TextView(getApplicationContext());
        child.addView(textAdd);

        textAdd.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));

        textAdd.setGravity(Gravity.CENTER);
        textAdd.setMaxLines(2);
        if(UsefulThings.currentUser.getType().equals("ngo")) {
            textAdd.setText("Obiective\nsusținute");
        } else {
            textAdd.setText("Număr\nsusținători");
        }
        textAdd.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        textAdd.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
    }

    private void setAddedText(String firstTextField, PercentRelativeLayout parent) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, number + 3);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();

        info.widthPercent = 0.26f;
        info.heightPercent = 0.26f;
        info.bottomMarginPercent = 0.01f;

        if(number % 2 == 0) {
            info.leftMarginPercent = 0.02f;
        } else {
            info.leftMarginPercent = 0.42f;
            info.rightMarginPercent = 0.02f;
        }

        child.requestLayout();

        TextView title = new TextView(getApplicationContext());
        child.addView(title);

        title.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));
        title.setGravity(Gravity.CENTER);
        title.setMaxLines(1);
        title.setText(firstTextField);
        title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        title.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private void setAddedObjects(PercentRelativeLayout parent) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        child.setId(number + 3);
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, number + 2);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();

        info.widthPercent = 0.26f;
        info.heightPercent = 0.35f;
        info.bottomMarginPercent = 0.01f;

        if(number % 2 == 0) {
            info.leftMarginPercent = 0.02f;
        } else {
            info.leftMarginPercent = 0.42f;
            info.rightMarginPercent = 0.02f;
        }

        child.requestLayout();

        TextView textAdd = new TextView(getApplicationContext());
        child.addView(textAdd);

        textAdd.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));

        textAdd.setGravity(Gravity.CENTER);
        textAdd.setMaxLines(2);
        if(UsefulThings.currentUser.getType().equals("ngo")) {
            textAdd.setText("Obiective\nadăugate");
        } else {
            textAdd.setText("Număr\nmembri");
        }
        textAdd.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        textAdd.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
    }

    private void setTitle(String titleText, PercentRelativeLayout parent) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        child.setId(number + 2);
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.56f;
        info.heightPercent = 0.35f;
        info.rightMarginPercent = 0.02f;
        info.bottomMarginPercent = 0.01f;

        if(number % 2 == 0) {
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

    private void setImage(String imageURL, PercentRelativeLayout parent) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.38f;
        info.heightPercent = 1;
        info.bottomMarginPercent = 0.02f;

        if(number % 2 == 0) {
            info.leftMarginPercent = 0.6f;
        }
        child.requestLayout();

        final ImageView image = new ImageView(getApplicationContext());
        image.setId(number + 1);
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

    private PercentRelativeLayout setParentLayout(boolean isProcessing) {

        PercentRelativeLayout parent = new PercentRelativeLayout(getApplicationContext());
        parent.setId(number);
        rootLayout.addView(parent);
        allPercents.put(number, parent);

        parent.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue4));

        PercentRelativeLayout.LayoutParams params =
                (PercentRelativeLayout.LayoutParams) parent.getLayoutParams();

        if(isProcessing) {
            PercentRelativeLayout.LayoutParams mParams =
                    (PercentRelativeLayout.LayoutParams) percentLayout2.getLayoutParams();
            mParams.addRule(PercentRelativeLayout.BELOW, number);
            percentLayout2.requestLayout();

            if(firstProcessing) {
                firstProcessing = false;
                params.addRule(PercentRelativeLayout.BELOW, R.id.percentLayout1);
            } else {
                params.addRule(PercentRelativeLayout.BELOW, number -
                        UsefulThings.PROPOSALS_INTERMEDIATE_IDS);
            }
        } else {
            if(firstRejected) {
                firstRejected = false;
                params.addRule(PercentRelativeLayout.BELOW, R.id.percentLayout2);
            } else {
                params.addRule(PercentRelativeLayout.BELOW, number -
                        UsefulThings.PROPOSALS_INTERMEDIATE_IDS);
            }
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

            for(int i = 2; i <= number + 2; i += UsefulThings.PROPOSALS_INTERMEDIATE_IDS) {
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

//                   dialog.dismiss();
                    Intent i = new Intent(getApplicationContext(), PublicProfileActivity.class);
                    i.putExtra("userCauseDetails", mUser);
                    startActivity(i);
                    allPercents.clearMemory();
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
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

}
