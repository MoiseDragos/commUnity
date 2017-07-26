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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class NgoActivity extends AppCompatActivity {

    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    private ScrollView allLayout = null;

    private PercentRelativeLayout rootLayout = null;
    private PercentRelativeLayout requests = null;
    private PercentRelativeLayout all = null;

    private TextView title = null;

    private DatabaseReference mDatabase = null;

    private SparseArray<String> keysSparseArray = null;
    private SparseArray<String> idsSparseArray = null;

    private String uid = null;
    private String type = null;

    private int number = -1;
    private int lastBtn = 1;

    private Button memberBtn = null;
    private Button supporterBtn = null;
    private Button requestsBtn = null;
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

        requests = (PercentRelativeLayout) findViewById(R.id.requestsLayout);
        all = (PercentRelativeLayout) findViewById(R.id.allLayout);
        title = (TextView) findViewById(R.id.textView);

        Intent intent = getIntent();
        if(intent != null){
            uid = intent.getStringExtra("uid");
            type = intent.getStringExtra("type");
        }


        if(type.equals("ngo")) {
            setUpButtons(true);
            displayNgo(1);
        } else {
            setUpButtons(false);
            displayUser(1);
        }
    }

    private void setUpButtons(boolean isNgo){

        if(isNgo){
            title.setText(R.string.ngo_details_title);
            memberBtn.setText(R.string.member);
            supporterBtn.setText(R.string.supporters);

            requests.setVisibility(View.GONE);
            all.setVisibility(View.GONE);
        } else {
            title.setText(R.string.ngo_title);
            memberBtn.setText(R.string.member);
            supporterBtn.setText(R.string.supported);

            requests.setVisibility(View.VISIBLE);
            all.setVisibility(View.VISIBLE);

            requestsBtn = (Button) findViewById(R.id.requestsBtn);
            requestsBtn.setText(R.string.requests);
            requestsBtn.setOnClickListener(callButtonClickListener);
            allBtn = (Button) findViewById(R.id.allBtn);
            allBtn.setText(R.string.all);
            allBtn.setOnClickListener(callButtonClickListener);
        }
    }

    private void displayNgo(int i) {

        memberBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue4));
        supporterBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue4));
        String no_ong;
        if(i == 1) {
            memberBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue1));
            no_ong = getString(R.string.no_member);
            displayDetails("Members", no_ong, false);
        } else if (i == 2) {
            supporterBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue1));
            no_ong = getString(R.string.no_supporter);
            displayDetails("Supporters", no_ong, false);
        }

    }

    private void displayUser(int i) {

        resetColors();
        String no_ong;
        switch (i) {
            case 1:
                memberBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue1));
                no_ong = getString(R.string.no_member_of);
                displayDetails("MemberOf", no_ong, false);
                break;
            case 2:
                supporterBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue1));
                no_ong = getString(R.string.no_supporter_of);
                displayDetails("SupporterOf", no_ong, false);
                break;
            case 3:
                requestsBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue1));
                no_ong = getString(R.string.no_request_of);
                displayDetails("SupporterOf", no_ong, false);
                break;
            case 4:
                allBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue1));
                no_ong = getString(R.string.no_ngo);
                displayDetails("ngo", no_ong, true);
                break;
            default:
                break;
        }
    }

    private void displayDetails(final String child, final String no_ong, boolean isAll){

        final DatabaseReference ref;
        if(!isAll) {
            ref = mDatabase.child("users").child(uid);
        } else {
            ref = mDatabase;
        }

        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(child)) {

                            allLayout = (ScrollView) findViewById(R.id.scroll_view_causes);
                            allLayout.setVisibility(View.VISIBLE);

                            rootLayout = (PercentRelativeLayout) findViewById(R.id.rootLayout);
                            rootLayout.setVisibility(View.VISIBLE);

                            keysSparseArray = new SparseArray<>();
                            idsSparseArray = new SparseArray<>();

                            DatabaseReference dRef = ref.child(child);
                            dRef.addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                                            for (Map.Entry<String, Object> entry : map.entrySet()) {

                                                downloadDetails(entry.getKey());
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                        } else {
                            allLayout = (ScrollView) findViewById(R.id.scroll_view_no_causes);
                            allLayout.setVisibility(View.VISIBLE);

                            TextView noCause = (TextView) findViewById(R.id.no_causes);
                            noCause.setText(no_ong);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void downloadDetails(final String key) {

        DatabaseReference reference = mDatabase.child("users").child(key).child("ProfileSettings");
        reference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                        final String name = map.get("name").toString();
                        final String allCauses = map.get("allCauses").toString();
                        final String imageURL = map.get("imageURL").toString();
                        final String[] supporters = {null};
                        final String[] email = {null};
                        final String[] date = {null};

                        number += UsefulThings.CAUSE_INTERMEDIATE_IDS;
                        keysSparseArray.put(number, key);
                        idsSparseArray.put(number, uid);

                        if(type.equals("ngo")){
                            email[0] = map.get("name").toString();
                            DatabaseReference ref = mDatabase.child("users").child(key).child("MemberOf").child(uid);
                            ref.addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            date[0] = String.valueOf(dataSnapshot.getValue());
                                            buildLayout(name, allCauses, supporters[0], imageURL, email[0], date[0]);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        } else {
                            supporters[0] = map.get("supporters").toString();
                            buildLayout(name, allCauses, supporters[0], imageURL, email[0], date[0]);

                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private NgoActivity.CallButtonClickListener callButtonClickListener = new NgoActivity.CallButtonClickListener();
    private class CallButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.membersBtn:
                    if(lastBtn != 1) {
                        lastBtn = 1;
                        if (type.equals("ngo")) {
                            displayNgo(1);
                        } else {
                            displayUser(1);
                        }
                    }
                    break;
                case R.id.supporterBtn:
                    if(lastBtn != 2) {
                        lastBtn = 2;
                        if (type.equals("ngo")) {
                            displayNgo(2);
                        } else {
                            displayUser(2);
                        }
                    }
                    break;
                case R.id.requestsBtn:
                    if(lastBtn != 3) {
                        lastBtn = 3;
                        displayUser(3);
                    }
                    break;
                case R.id.allBtn:
                    if(lastBtn != 4) {
                        lastBtn = 4;
                        displayUser(4);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void buildLayout(String name, String allCauses, String supporters, String imageURL, String email, String date) {

        PercentRelativeLayout parent = setParentLayout();

        setImage(imageURL, parent);

        setTitle(name, parent);

        setAddedObjects(parent);
        setAddedText(allCauses, parent);

        setVerticalLine(parent);

        if(type.equals("user")){
            setSupportersObjects(parent);
            setSupportersText(supporters, parent);
        } else {
            setHorizontalLine(parent);
            setEmailText(email, parent);
            setDateText(date, parent);
        }
    }

    private void setDateText(String email, PercentRelativeLayout parent) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams = (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, number + UsefulThings.NGO_INTERMEDIATE_IDS - 3);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.32f;
        info.topMarginPercent = 0.02f;
        info.heightPercent = 0.35f;
        info.bottomMarginPercent = 0.01f;

        if(number%2 == 1) {
            info.leftMarginPercent = 0.26f;
        } else {
            info.leftMarginPercent = 0.66f;
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
        title.setText(email);
        title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
    }

    private void setEmailText(String date, PercentRelativeLayout parent) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        child.setId(number + 5);
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams = (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, number + UsefulThings.NGO_INTERMEDIATE_IDS - 3);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.32f;
        info.heightPercent = 0.29f;
        info.bottomMarginPercent = 0.02f;

        if(number%2 == 1) {
            info.leftMarginPercent = 0.26f;
        } else {
            info.leftMarginPercent = 0.66f;
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
        title.setText(date);
        title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
    }

    private void setHorizontalLine(PercentRelativeLayout parent) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams = (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, number + UsefulThings.NGO_INTERMEDIATE_IDS - 3);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.32f;
        info.heightPercent = 0.35f;
        info.topMarginPercent = 0.02f;
        info.bottomMarginPercent = 0.02f;

        if(number%2 == 1) {
            info.leftMarginPercent = 0.66f;
            info.rightMarginPercent = 0.02f;
        } else {
            info.leftMarginPercent = 0.26f;
        }

        child.requestLayout();

        View view = new View(getApplicationContext());
        child.addView(view);

        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        view.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));
    }

    private void setVerticalLine(PercentRelativeLayout parent) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams = (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, number + UsefulThings.NGO_INTERMEDIATE_IDS - 3);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.01f;
        info.heightPercent = 1;
        info.bottomMarginPercent = 0.02f;

        if(number%2 == 1) {
            if(type.equals("ngo")){
                info.leftMarginPercent = 0.637f;
            } else {
                info.leftMarginPercent = 0.697f;
            }
        } else {
            if(type.equals("ngo")){
                info.leftMarginPercent = 0.337f;
            } else {
                info.leftMarginPercent = 0.397f;
            }
        }

        child.requestLayout();

        View view = new View(getApplicationContext());
        child.addView(view);

        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        view.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));
    }

    private void setSupportersText(String supporters, PercentRelativeLayout parent) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams = (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, number + UsefulThings.NGO_INTERMEDIATE_IDS - 3);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.26f;
        info.heightPercent = 0.26f;
        info.bottomMarginPercent = 0.01f;

        if(number%2 == 1) {
            info.leftMarginPercent = 0.02f;
        } else {
            info.leftMarginPercent = 0.42f;
            info.rightMarginPercent = 0.02f;
        }

        child.requestLayout();

        TextView title = new TextView(getApplicationContext());
        child.addView(title);

        title.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.edit_text_form_green));
        title.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));
        title.setGravity(Gravity.CENTER);
        title.setMaxLines(1);
        title.setText(supporters);
        title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        title.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private void setSupportersObjects(PercentRelativeLayout parent) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        child.setId(number + 3);
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams = (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, number + UsefulThings.NGO_INTERMEDIATE_IDS - 3);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.26f;
        info.heightPercent = 0.35f;
        info.bottomMarginPercent = 0.01f;

        if(number%2 == 1) {
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
        textAdd.setText(R.string.supporters);
        textAdd.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        textAdd.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
    }

    private void setAddedText(String addedObj, PercentRelativeLayout parent) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams = (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, number + UsefulThings.NGO_INTERMEDIATE_IDS - 3);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();

        if(type.equals("ngo")) {
            info.widthPercent = 0.20f;
        } else {
            info.widthPercent = 0.26f;
        }

        info.heightPercent = 0.26f;
        info.bottomMarginPercent = 0.01f;

        if(number%2 == 1) {
            info.leftMarginPercent = 0.02f;
        } else {
            info.leftMarginPercent = 0.42f;
            info.rightMarginPercent = 0.02f;
        }

        child.requestLayout();

        TextView title = new TextView(getApplicationContext());
        child.addView(title);

        title.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.edit_text_form_green));
        title.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));
        title.setGravity(Gravity.CENTER);
        title.setMaxLines(1);
        title.setText(addedObj);
        title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        title.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private void setAddedObjects(PercentRelativeLayout parent) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        child.setId(number + 3);
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams = (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, number + UsefulThings.NGO_INTERMEDIATE_IDS - 3);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();

        if(type.equals("ngo")) {
            info.widthPercent = 0.20f;
        } else {
            info.widthPercent = 0.26f;
        }
        info.heightPercent = 0.35f;
        info.bottomMarginPercent = 0.01f;

        if(number%2 == 1) {
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
        textAdd.setText("Obiective\nadăugate");
        textAdd.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        textAdd.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
    }

    private void setTitle(String titleText, PercentRelativeLayout parent) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        child.setId(number + 2);
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams = (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.56f;
        info.heightPercent = 0.35f;
        info.rightMarginPercent = 0.02f;
        info.bottomMarginPercent = 0.01f;

        if(number%2 == 1) {
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

        PercentRelativeLayout.LayoutParams childParams = (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.38f;
        info.heightPercent = 1;
        info.bottomMarginPercent = 0.02f;

        if(number%2 == 1) {
//            childParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            info.leftMarginPercent = 0.6f;
        }
        child.requestLayout();

        final ImageView image = new ImageView(getApplicationContext());
        image.setId(number + 1);
//                                            Log.d(LOG, "ID: " + (i+1));
        child.addView(image);

        image.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));

        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setClickable(true);

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
    }

    private PercentRelativeLayout setParentLayout() {

        PercentRelativeLayout parent = new PercentRelativeLayout(getApplicationContext());
        parent.setId(number);
        parent.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue4));
        rootLayout.addView(parent);

        PercentRelativeLayout.LayoutParams params = (PercentRelativeLayout.LayoutParams) parent.getLayoutParams();

        if(number != -1) {
            params.addRule(PercentRelativeLayout.BELOW, number - UsefulThings.NGO_INTERMEDIATE_IDS);
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

    private CallImageButtonClickListener callImageButtonClickListener = new CallImageButtonClickListener();
    private class CallImageButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            for(int i = UsefulThings.NGO_INTERMEDIATE_IDS; i <= number + 1; i += UsefulThings.NGO_INTERMEDIATE_IDS) {
                if(view.getId() == i){
                    Log.d(LOG, "Accesez cauza cu numarul: " + i/UsefulThings.NGO_INTERMEDIATE_IDS);
                    Log.d(LOG, "ID: " + idsSparseArray.get(i - 1));
//                    Intent intent = new Intent(getApplicationContext(), PublicProfileActivity.class);
//                    intent.putExtra("ownerUID", idsSparseArray.get(i - 1));
//                    intent.putExtra("causeId", keysSparseArray.get(i - 1));
//                    startActivity(intent);
                    break;
                }
            }
        }
    }

    private void resetColors(){
        memberBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue4));
        supporterBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue4));
        requestsBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue4));
        allBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue4));
    }
}
