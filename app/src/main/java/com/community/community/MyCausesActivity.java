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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.community.community.CauseProfile.CauseProfileActivity;
import com.community.community.General.UsefulThings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

//TODO: Memory problem!
public class MyCausesActivity extends AppCompatActivity {

    // TODO: remove
    private String LOG = this.getClass().getSimpleName();

    private ScrollView allLayout = null;
    private TextView title = null;

    private PercentRelativeLayout rootLayout = null;

    private DatabaseReference mDatabase = null;
    private String uid = null;
    private String type = null;
    private int number = -1;

    SparseArray<String> hmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_causes_activity);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        if(intent != null){
            uid = intent.getStringExtra("uid");
            type = intent.getStringExtra("type");
            title = (TextView) findViewById(R.id.textView);
        }

        displayCauses();
    }

    private void displayCauses() {

        final DatabaseReference ref = mDatabase.child("users").child(uid);
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("MyCauses")) {

                            if(type.equals("ngo")) {
                                title.setText(R.string.ngoCauses);
                            } else {
                                title.setText(R.string.myCauses);
                            }

                            allLayout = (ScrollView) findViewById(R.id.scroll_view_causes);
                            allLayout.setVisibility(View.VISIBLE);
                            rootLayout = (PercentRelativeLayout) findViewById(R.id.rootLayout);
                            rootLayout.setVisibility(View.VISIBLE);

                            hmap = new SparseArray<>();

                            DatabaseReference dRef = ref.child("MyCauses");
                            dRef.addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                                            for (Map.Entry<String, Object> entry : map.entrySet()) {

                                                Map<String, Object> data = (Map<String, Object>) entry.getValue();

                                                Log.d(LOG, "KEY:" + entry.getKey());

                                                /* Data */
                                                Map<String, Object> a = (Map<String, Object>) data.get("Info");
                                                String name = a.get("name").toString();
                                                String date = a.get("date").toString();
                                                String description = a.get("description").toString();
                                                String supportedBy = String.valueOf(Integer.valueOf(a.get("supportedBy").toString()) + 1);

                                                /* Image */
                                                a = (Map<String, Object>) data.get("Images");
                                                String imageURL = a.get("profileThumbnailURL").toString();

                                                number += UsefulThings.INTERMEDIATE_IDS;
                                                hmap.put(number, entry.getKey());

                                                buildLayout(name, date, description, supportedBy, imageURL);
                                            }
                                        }

                                        private void buildLayout(final String name, String date, final String description,
                                                                String supportedBy, String imageURL) {

                                            PercentRelativeLayout parent = setParentLayout();

                                            setImage(imageURL, parent);
                                            setTitle(name, parent);
                                            setSupportedImage(parent);
                                            setSupportedText(supportedBy, parent);
                                            setDate(date, parent);
                                            setDescription(description, parent);
                                        }

                                        private void setDescription(String description, PercentRelativeLayout parent) {

                                            PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
//                                            child.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue2));
                                            parent.addView(child);

                                            PercentRelativeLayout.LayoutParams childParams = (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
                                            childParams.addRule(PercentRelativeLayout.BELOW, number + UsefulThings.INTERMEDIATE_IDS - 2);
                                            PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
                                            info.widthPercent = 0.56f;
                                            info.heightPercent = 0.34f;
                                            info.rightMarginPercent = 0.02f;

                                            if(number % 2 == 1) {
                                                info.leftMarginPercent = 0.02f;
                                            } else {
                                                info.leftMarginPercent = 0.42f;
                                            }

                                            child.requestLayout();

                                            TextView title = new TextView(getApplicationContext());
                                            title.setId(number + 4);
                                            child.addView(title);

                                            title.setLayoutParams(new PercentRelativeLayout.LayoutParams
                                                    (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                                                            PercentRelativeLayout.LayoutParams.MATCH_PARENT));

                                            title.setMaxLines(2);
                                            title.setText(description);
                                            title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                        }

                                        private void setDate(String date, PercentRelativeLayout parent) {

                                            PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
//                                            child.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue2));
                                            parent.addView(child);

                                            PercentRelativeLayout.LayoutParams childParams = (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
                                            childParams.addRule(PercentRelativeLayout.BELOW, number + UsefulThings.INTERMEDIATE_IDS - 3);
                                            PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
                                            info.widthPercent = 0.56f;
                                            info.heightPercent = 0.25f;
                                            info.bottomMarginPercent = 0.025f;

                                            TextView title = new TextView(getApplicationContext());
                                            child.addView(title);

                                            if(number%2 == 1) {
                                                info.leftMarginPercent = 0.39f;
                                                title.setGravity(Gravity.CENTER_VERTICAL);
                                            } else {
                                                info.leftMarginPercent = 0.42f;
                                                title.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                                            }

                                            child.requestLayout();

                                            title.setLayoutParams(new PercentRelativeLayout.LayoutParams
                                                    (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                                                            PercentRelativeLayout.LayoutParams.MATCH_PARENT));

                                            title.setText(date);
                                            title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                        }

                                        private void setSupportedText(String supportedBy, PercentRelativeLayout parent) {

                                            PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
                                            parent.addView(child);

                                            PercentRelativeLayout.LayoutParams childParams = (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
                                            childParams.addRule(PercentRelativeLayout.BELOW, number + UsefulThings.INTERMEDIATE_IDS - 3);
                                            PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
                                            info.widthPercent = 0.08f;
                                            info.heightPercent = 0.25f;
                                            info.bottomMarginPercent = 0.025f;

                                            if(number%2 == 1) {
                                                info.leftMarginPercent = 0.09f;
                                            } else {
                                                info.leftMarginPercent = 0.49f;
                                            }

                                            child.requestLayout();

                                            TextView title = new TextView(getApplicationContext());
                                            child.addView(title);

                                            title.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.edittext_login_form_green));
                                            title.setLayoutParams(new PercentRelativeLayout.LayoutParams
                                                    (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                                                            PercentRelativeLayout.LayoutParams.MATCH_PARENT));
                                            title.setGravity(Gravity.CENTER);
                                            title.setText(supportedBy);
                                            title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                        }

                                        private void setSupportedImage(PercentRelativeLayout parent) {

                                            PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
                                            child.setId(number + 3);
                                            parent.addView(child);

                                            PercentRelativeLayout.LayoutParams childParams = (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
                                            childParams.addRule(PercentRelativeLayout.BELOW, number + UsefulThings.INTERMEDIATE_IDS - 3);
                                            PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
                                            info.widthPercent = 0.1f;
                                            info.heightPercent = 0.25f;
                                            info.bottomMarginPercent = 0.025f;

                                            if(number%2 == 1) {
                                                info.leftMarginPercent = 0.02f;
                                            } else {
                                                info.leftMarginPercent = 0.42f;
                                            }

                                            child.requestLayout();

                                            ImageView image = new ImageView(getApplicationContext());
                                            child.addView(image);

                                            image.setLayoutParams(new PercentRelativeLayout.LayoutParams
                                                    (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                                                            PercentRelativeLayout.LayoutParams.MATCH_PARENT));

                                            image.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.edittext_login_form_green));
                                            image.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.follow_man));
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
                                            info.bottomMarginPercent = 0.015f;
                                            child.requestLayout();

                                            if(number%2 == 1) {
                                                childParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                                            }

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

                                            if(number != 1) {
                                                params.addRule(PercentRelativeLayout.BELOW, number - UsefulThings.INTERMEDIATE_IDS);
                                            }

                                            PercentLayoutHelper.PercentLayoutInfo info = params.getPercentLayoutInfo();
                                            info.widthPercent = 1;
                                            info.heightPercent = 0.20f;
                                            info.bottomMarginPercent = 0.02f;
                                            info.leftMarginPercent = 0.02f;
                                            info.rightMarginPercent = 0.02f;
                                            parent.setPadding(8, 8, 8, 8);
                                            parent.requestLayout();

                                            return parent;
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        } else {
                            PercentRelativeLayout.LayoutParams params = (PercentRelativeLayout.LayoutParams) title.getLayoutParams();
                            PercentLayoutHelper.PercentLayoutInfo info = params.getPercentLayoutInfo();
                            info.heightPercent = 0.20f;
                            title.requestLayout();

                            if(type.equals("ngo")) {
                                title.setText(R.string.ngoCauses);
                            } else {
                                title.setText(R.string.myCauses);
                            }

                            allLayout = (ScrollView) findViewById(R.id.scroll_view_no_causes);
                            allLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private MyCausesActivity.CallImageButtonClickListener callImageButtonClickListener = new MyCausesActivity.CallImageButtonClickListener();
    private class CallImageButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            for(int i = UsefulThings.INTERMEDIATE_IDS; i <= number + 1; i += UsefulThings.INTERMEDIATE_IDS) {
                if(view.getId() == i){
                    Log.d(LOG, "Accesez cauza cu numarul: " + i/UsefulThings.INTERMEDIATE_IDS);
                    Log.d(LOG, "ID: " + hmap.get(i - 1));
                    Intent intent = new Intent(getApplicationContext(), CauseProfileActivity.class);
                    intent.putExtra("ownerUID", uid);
                    intent.putExtra("causeId", hmap.get(i - 1));
                    startActivity(intent);
                    break;
                }
            }
        }
    }

}
