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
import com.community.community.General.UsefulThings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class ProposalsActivity extends AppCompatActivity {

    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    private PercentRelativeLayout no_processing_layout = null;
    private PercentRelativeLayout no_rejected_layout = null;

    private PercentRelativeLayout root_processing_layout = null;
    private PercentRelativeLayout root_rejected_layout = null;

    private DatabaseReference mDatabase = null;

    private SparseArray<String> keysSparseArray = null;
    private SparseArray<String> idsSparseArray = null;

    private String uid = null;
    private String type = null;

    private int number = -1;
    private boolean isReceived = true;

    private Button receivedBtn = null;
    private Button madeBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.proposals_activity);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        root_processing_layout = (PercentRelativeLayout) findViewById(R.id.root_processing_layout);
        root_rejected_layout = (PercentRelativeLayout) findViewById(R.id.root_rejected_layout);
        no_processing_layout = (PercentRelativeLayout) findViewById(R.id.no_processing_layout);
        no_rejected_layout = (PercentRelativeLayout) findViewById(R.id.no_rejected_layout);

        receivedBtn = (Button) findViewById(R.id.membersBtn);
        receivedBtn.setOnClickListener(callButtonClickListener);
        madeBtn = (Button) findViewById(R.id.supporterBtn);
        madeBtn.setOnClickListener(callButtonClickListener);

        Intent intent = getIntent();
        if(intent != null){
            uid = intent.getStringExtra("uid");
            type = intent.getStringExtra("type");
        }

        chooseWhatToShow(1);
    }

    private void chooseWhatToShow(int i) {

        receivedBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue4));
        madeBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue4));
        no_processing_layout.setVisibility(View.GONE);
        no_rejected_layout.setVisibility(View.GONE);
        root_processing_layout.setVisibility(View.GONE);
        root_rejected_layout.setVisibility(View.GONE);

        String text;
        if(i == 1) {
            isReceived = true;
            receivedBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue1));
            text = "Received";
            displayAll(text);
        } else if (i == 2) {
            isReceived = false;
            madeBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue1));
            text = "Made";
            displayAll(text);
        }

    }

    private void displayAll(String text){
        displayDetails(text, true);
        displayDetails(text + "Rejected", false);
    }

    private void displayDetails(final String text, final boolean isProcessing){

        final DatabaseReference ref = mDatabase.child("users").child(uid);
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("Proposals" + text)) {

                            if(isProcessing) {
                                root_processing_layout.setVisibility(View.VISIBLE);
                            } else {
                                root_rejected_layout.setVisibility(View.VISIBLE);
                            }

                            keysSparseArray = new SparseArray<>();
                            idsSparseArray = new SparseArray<>();

                            DatabaseReference dRef = ref.child("Proposals" + text);
                            dRef.addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                            if(isProcessing) {
                                                for (Map.Entry<String, Object> entry : map.entrySet()) {

                                                    downloadDetails(entry.getKey(), root_processing_layout);
                                                }
                                            } else {
                                                for (Map.Entry<String, Object> entry : map.entrySet()) {

                                                    downloadDetails(entry.getKey(), root_rejected_layout);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                        } else {
                            if(isProcessing) {
                                no_processing_layout.setVisibility(View.VISIBLE);
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

    private void downloadDetails(final String key, final PercentRelativeLayout layout) {

        DatabaseReference reference = mDatabase.child("users").child(key).child("ProfileSettings");
        reference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                        String name = map.get("name").toString();
                        String allCauses = map.get("allCauses").toString();
                        String imageURL = map.get("imageURL").toString();
                        String supporters = map.get("supporters").toString();

                        number += UsefulThings.CAUSE_INTERMEDIATE_IDS;
                        keysSparseArray.put(number, key);
                        idsSparseArray.put(number, uid);

                        buildLayout(name, allCauses, supporters, imageURL, layout);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private ProposalsActivity.CallButtonClickListener callButtonClickListener
            = new ProposalsActivity.CallButtonClickListener();
    private class CallButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.membersBtn:
                    if(!isReceived)
                        chooseWhatToShow(1);
                    break;
                case R.id.supporterBtn:
                    if(isReceived)
                        chooseWhatToShow(2);
                    break;
                default:
                    break;
            }
        }
    }

    private void buildLayout(String name, String allCauses, String supporters,
                             String imageURL, PercentRelativeLayout layout) {

        PercentRelativeLayout parent = setParentLayout(layout);

        setImage(imageURL, parent);

        setTitle(name, parent);

        setAddedObjects(parent);
        setAddedText(allCauses, parent);

        setVerticalLine(parent);

        setSupportersObjects(parent);
        setSupportersText(supporters, parent);
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
            info.leftMarginPercent = 0.697f;
        } else {
            info.leftMarginPercent = 0.397f;
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

        info.widthPercent = 0.26f;
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

    private PercentRelativeLayout setParentLayout(PercentRelativeLayout layout) {

        PercentRelativeLayout parent = new PercentRelativeLayout(getApplicationContext());
        parent.setId(number);
        parent.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue4));
        layout.addView(parent);

        PercentRelativeLayout.LayoutParams params = (PercentRelativeLayout.LayoutParams) parent.getLayoutParams();

        if(number != -1) {
            params.addRule(PercentRelativeLayout.BELOW, number - UsefulThings.NGO_INTERMEDIATE_IDS);
        } else {
            params.addRule(PercentRelativeLayout.BELOW, R.id.linearLayout1);
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
}
