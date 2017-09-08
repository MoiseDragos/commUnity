package com.community.community;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.util.LruCache;
import com.community.community.BeforeLogin.LoginActivity;
import com.community.community.General.BackPressedActivity;
import com.community.community.General.UsefulThings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AdminActivity extends AppCompatActivity {

    private String LOG = this.getClass().getSimpleName();

    private static LruCache<Integer, PercentRelativeLayout> allPercents
        = new LruCache<>(UsefulThings.PROPOSALS_CACHE_SIZE);

    private PercentRelativeLayout no_processing_layout = null;
    private PercentRelativeLayout rootLayout = null;
    private ScrollView scrollView = null;

    private DatabaseReference mDatabase = null;

    private SparseArray<String> idsSparseArray = null;
    private SparseArray<String> nameSparseArray = null;

    private int number;
    private boolean accept;

    private Button acceptBtn = null;
    private Button addBtn = null;
    private Button sendBtn = null;

    private EditText email = null;
    private EditText address = null;
    private EditText site = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity);

        acceptBtn = (Button) findViewById(R.id.acceptBtn);
        acceptBtn.setOnClickListener(callButtonClickListener);
        addBtn = (Button) findViewById(R.id.addBtn);
        addBtn.setOnClickListener(callButtonClickListener);
        sendBtn = (Button) findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(callButtonClickListener);

        ImageButton logoutBtn = (ImageButton) findViewById(R.id.logout);
        logoutBtn.setOnClickListener(callButtonClickListener);

        rootLayout = (PercentRelativeLayout) findViewById(R.id.rootLayout);
        no_processing_layout = (PercentRelativeLayout) findViewById(R.id.no_processing_layout);
        scrollView = (ScrollView) findViewById(R.id.scrollView);

        email = (EditText) findViewById(R.id.email);
        address = (EditText) findViewById(R.id.official_address);
        site = (EditText) findViewById(R.id.site);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        chooseWhatToShow(1);
    }

    private void chooseWhatToShow(int i) {

        acceptBtn.setBackgroundColor(
                ContextCompat.getColor(getApplicationContext(), R.color.blue4));
        addBtn.setBackgroundColor(
                ContextCompat.getColor(getApplicationContext(), R.color.blue4));

        if(i == 1) {
            accept = true;
            scrollView.setVisibility(View.GONE);
            acceptBtn.setBackgroundColor(
                    ContextCompat.getColor(getApplicationContext(), R.color.blue1));

            DatabaseReference ref = mDatabase.child("applyForNgo");
            Log.d(LOG, "ref: " + ref);
//            ref.addListenerForSingleValueEvent(
            ref.addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(LOG, "C");
                            if(dataSnapshot.getValue() != null) {
                                Log.d(LOG, "A");
                                no_processing_layout.setVisibility(View.GONE);

                                removeLayouts();
                                number = 1 - UsefulThings.ADMIN_INTERMEDIATE_IDS;
                                allPercents = new LruCache<>(UsefulThings.PROPOSALS_CACHE_SIZE);
                                idsSparseArray = new SparseArray<>();
                                nameSparseArray = new SparseArray<>();

                                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                                for (Map.Entry<String, Object> entry : map.entrySet()) {
                                    downloadDetails(entry.getKey(), String.valueOf(entry.getValue()));
                                }

                            } else {
                                Log.d(LOG, "B");

                                removeLayouts();
                                no_processing_layout.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

        } else if (i == 2) {
            accept = false;

            removeLayouts();
            no_processing_layout.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);

            addBtn.setBackgroundColor(
                    ContextCompat.getColor(getApplicationContext(), R.color.blue1));
        }

    }

    /* ------------- Accept Section ------------- */

    private void downloadDetails(final String key, final String date) {

        DatabaseReference reference = mDatabase.child("users")
                .child(key).child("ProfileSettings");
        reference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if(UsefulThings.currentUser != null) {
                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                            final String name = map.get("nickname").toString();
                            final String imageURL;

                            if (map.containsKey("imageURL")) {
                                imageURL = map.get("imageURL").toString();
                            } else {
                                imageURL = null;
                            }

                            number += UsefulThings.ADMIN_INTERMEDIATE_IDS;
                            Log.d(LOG, "Number: " + number);
                            idsSparseArray.put(number, key);
                            nameSparseArray.put(number, name);

                            buildLayout(name, imageURL, date, number);

//                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void buildLayout(String name, String imageURL, String date, int number) {

        PercentRelativeLayout parent = setParentLayout(number);

        setImage(imageURL, parent, number);

        setTitle(name, parent, number);


        String[] parts = date.split("~");

        Log.d(LOG, "LEN: " + parts.length);
        if(parts.length == 1) {
            setDate(parent, date, number);
            setAcceptBtn(parent, number);
            setRejectBtn(parent, number);
        } else {
            setDate(parent, parts[1], number);
            setChangeDecision(parent, number);
        }

    }

    private void setChangeDecision(PercentRelativeLayout parent, int number) {
        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, number + 3);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();

        info.widthPercent = 0.56f;
        info.heightPercent = 0.38f;
        info.bottomMarginPercent = 0.02f;
        info.leftMarginPercent = 0.42f;

        child.requestLayout();

        Button btn = new Button(getApplicationContext());
        btn.setId(number + 6);
        child.addView(btn);
        btn.setOnClickListener(callImageButtonClickListener);
        btn.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));
        btn.setGravity(Gravity.CENTER);
        btn.setMaxLines(1);
        btn.setText(R.string.changeDecision);
        btn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.blue5));
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setBackground(ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.edit_text_form_gray));
    }

    private void setRejectBtn(PercentRelativeLayout parent, int number) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        parent.addView(child);

        child.setOnClickListener(callImageButtonClickListener);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, number + 3);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();

        info.widthPercent = 0.26f;
        info.heightPercent = 0.38f;
        info.bottomMarginPercent = 0.02f;
        info.leftMarginPercent = 0.72f;

        child.requestLayout();

        Button btn = new Button(getApplicationContext());
        btn.setId(number + 5);
        child.addView(btn);
        btn.setOnClickListener(callImageButtonClickListener);
        btn.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));
        btn.setGravity(Gravity.CENTER);
        btn.setMaxLines(1);
        btn.setText(R.string.reject);
        btn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setBackground(ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.edit_text_form_red));

    }

    private void setAcceptBtn(PercentRelativeLayout parent, int number) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, number + 3);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();

        info.widthPercent = 0.26f;
        info.heightPercent = 0.38f;
        info.bottomMarginPercent = 0.02f;
        info.leftMarginPercent = 0.42f;

        child.requestLayout();

        Button btn = new Button(getApplicationContext());
        btn.setId(number + 4);
        child.addView(btn);
        btn.setOnClickListener(callImageButtonClickListener);
        btn.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));
        btn.setGravity(Gravity.CENTER);
        btn.setMaxLines(1);
        btn.setText(R.string.accept);
        btn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setBackground(ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.edit_text_form_green));

    }

    private void setDate(PercentRelativeLayout parent, String date, int number) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        child.setId(number + 3);
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        childParams.addRule(PercentRelativeLayout.BELOW, number + 2);
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();

        info.widthPercent = 0.56f;
        info.heightPercent = 0.23f;
        info.bottomMarginPercent = 0.01f;
        info.leftMarginPercent = 0.42f;
        info.rightMarginPercent = 0.02f;

        child.requestLayout();

        TextView textAdd = new TextView(getApplicationContext());
        child.addView(textAdd);

        textAdd.setLayoutParams(new PercentRelativeLayout.LayoutParams
                (PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                        PercentRelativeLayout.LayoutParams.MATCH_PARENT));

        textAdd.setGravity(Gravity.CENTER);
        textAdd.setMaxLines(1);
        textAdd.setText(date);
        textAdd.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        textAdd.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
    }

    private void setTitle(String titleText, PercentRelativeLayout parent, int number) {

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
        info.leftMarginPercent = 0.42f;

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

    private void setImage(String imageURL, PercentRelativeLayout parent, int number) {

        PercentRelativeLayout child = new PercentRelativeLayout(getApplicationContext());
        parent.addView(child);

        PercentRelativeLayout.LayoutParams childParams =
                (PercentRelativeLayout.LayoutParams) child.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo info = childParams.getPercentLayoutInfo();
        info.widthPercent = 0.38f;
        info.heightPercent = 1;
        info.bottomMarginPercent = 0.02f;
        info.leftMarginPercent = 0.01f;

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
                        }
                    });
        } else {
            image.setImageResource(R.drawable.profile);
        }
    }

    private PercentRelativeLayout setParentLayout(int number) {

        PercentRelativeLayout parent = new PercentRelativeLayout(getApplicationContext());
        parent.setId(number);
        rootLayout.addView(parent);
        allPercents.put(number, parent);

        parent.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue4));

        PercentRelativeLayout.LayoutParams params =
                (PercentRelativeLayout.LayoutParams) parent.getLayoutParams();

        if(number != 1) {
            params.addRule(PercentRelativeLayout.BELOW, number -
                    UsefulThings.ADMIN_INTERMEDIATE_IDS);
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

    /* ------------- End of Accept Section ------------- */

    private void removeLayouts(){

        if(allPercents != null && rootLayout != null) {
            for (int i = 1; i <= number + 1; i += UsefulThings.ADMIN_INTERMEDIATE_IDS) {
                rootLayout.removeView(allPercents.get(i));
                allPercents.clearMemory();
            }
        }

    }

    private AdminActivity.CallButtonClickListener callButtonClickListener
            = new AdminActivity.CallButtonClickListener();
    private class CallButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            Log.d(LOG, "Accept: " + accept);

            switch (view.getId()){
                case R.id.acceptBtn:
                    if(!accept) {
                        removeLayouts();
                        chooseWhatToShow(1);
                    }
                    break;
                case R.id.addBtn:
                    if(accept) {
                        removeLayouts();
                        chooseWhatToShow(2);
                    }
                    break;
                case R.id.logout:
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(),
                            LoginActivity.class));
                    finish();
                    break;
                case R.id.sendBtn:
                    verify_email(email.getText().toString());
                    break;
                default:
                    break;
            }
        }
    }

    private CallImageButtonClickListener callImageButtonClickListener =
            new CallImageButtonClickListener();
    private class CallImageButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            Log.d(LOG, String.valueOf(view.getId()));

            for(int i = 5; i <= number + 5; i += UsefulThings.ADMIN_INTERMEDIATE_IDS) {
                if(view.getId() == i) {
                    areYouSure(1, i - 4);
                    break;
                }
            }

            for(int i = 6; i <= number + 6; i += UsefulThings.ADMIN_INTERMEDIATE_IDS) {
                if(view.getId() == i) {
                    areYouSure(2, i - 5);
                    break;
                }
            }

            for(int i = 7; i <= number + 7; i += UsefulThings.ADMIN_INTERMEDIATE_IDS) {
                if(view.getId() == i) {
                    areYouSure(3, i - 6);
                    break;
                }
            }
        }

        private void areYouSure(final int ok, final int i) {

            TextView textAreYouSure = (TextView) findViewById(R.id.textAreYouSure);
            switch (ok) {
                case 1:
                    textAreYouSure.setText("Ești sigur că vrei\nsă accepți ca utilizatorul\n"
                            + nameSparseArray.get(i) + "\nsă devină ONG?");
                    break;
                case 2:
                    textAreYouSure.setText("Ești sigur că vrei\nsă refuzi ca utilizatorul\n"
                            + nameSparseArray.get(i) + "\nsă devină ONG?");
                    break;
                case 3:
                    textAreYouSure.setText("Ești sigur că vrei\nsă schimbi decizia\n" +
                            "cu privire la utilizatorul\n" + nameSparseArray.get(i) + "?");
                    break;
                default:
                    break;
            }

            final PercentRelativeLayout popUpLayout = (PercentRelativeLayout)
                    findViewById(R.id.popUpLayout);
            popUpLayout.setVisibility(View.VISIBLE);

            Button im_sure = (Button) findViewById(R.id.im_sure);
            im_sure.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    switch (ok) {
                        case 1:
                            acceptNGO(i);
                            popUpLayout.setVisibility(View.GONE);
                            break;
                        case 2:
                            rejectNGO(i);
                            popUpLayout.setVisibility(View.GONE);
                            break;
                        case 3:
                            changeNGO(i);
                            popUpLayout.setVisibility(View.GONE);
                            break;
                        default:
                            break;
                    }
                }

            });

            Button im_not_sure = (Button) findViewById(R.id.im_not_sure);
            im_not_sure.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    popUpLayout.setVisibility(View.GONE);
                }

            });
        }

        private void changeNGO(final int i) {

            String current_key = idsSparseArray.get(i);

            final DatabaseReference ref = mDatabase.child("applyForNgo").child(current_key);

            ref.addListenerForSingleValueEvent(
                    new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String date = String.valueOf(dataSnapshot.getValue());
                            String[] parts = date.split("~");

                            ref.setValue(parts[2]);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


        }

        private void rejectNGO(final int i) {

            String current_key = idsSparseArray.get(i);

            final DatabaseReference ref = mDatabase.child("applyForNgo").child(current_key);

            ref.addListenerForSingleValueEvent(
                    new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String date = String.valueOf(dataSnapshot.getValue());

                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                            ref.setValue("reject~" + String.valueOf(dateFormat.format(new Date())) +
                                "~" + date);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


        }

        private void acceptNGO(int i) {
            String current_key = idsSparseArray.get(i);

            updateSupporterOf(current_key);
            updateType(current_key);
            updateNGO(current_key);
            removeApply(current_key);
        }

        private void removeApply(String current_key) {
            mDatabase.child("applyForNgo").child(current_key).removeValue();
        }

        private void updateNGO(final String current_key) {
            DatabaseReference ref = mDatabase.child("users").child(current_key)
                    .child("ProfileSettings").child("email");

            ref.addListenerForSingleValueEvent(
                    new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mDatabase.child("ngo").child(current_key)
                                    .setValue(String.valueOf(dataSnapshot.getValue()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

        private void updateType(String current_key) {
            mDatabase.child("users").child(current_key).child("ProfileSettings")
                    .child("type").setValue("ngo");
        }

        private void updateSupporterOf(final String current_key) {

            final DatabaseReference ref = mDatabase.child("users").child(current_key)
                    .child("SupporterOf");
            ref.addListenerForSingleValueEvent(
                    new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue() != null) {
                                Map<String, Object> map = (Map<String, Object>)
                                        dataSnapshot.getValue();

                                for (Map.Entry<String, Object> entry : map.entrySet()) {
                                    updateSupporters(entry.getKey());
                                }
                                ref.removeValue();
                            }
                        }

                        private void updateSupporters(String key) {
                            mDatabase.child("users").child(key).child("Supporters")
                                    .child(current_key).removeValue();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

        }
    }

    /* ------------- Add Section ------------- */

    private void sendEmail(String em, String pass) {

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{em});
        i.putExtra(Intent.EXTRA_SUBJECT, "Înregistrare CommUnity");
        i.putExtra(Intent.EXTRA_TEXT   , "Bună ziua,\n\nÎn urma solicitării primite de la " +
                "dumneavoastră, v-am înregistrat în comunitate. În același timp v-am trimis " +
                "un email pentru activarea contului.\n\nUser: " + em + "\nParolă: " + pass +
                "\n\nVă rugăm să vă schimbați această parolă cât mai curând posibil.\n\n" +
                "Cu stimă,\nEchipa CommUnity\n\n\nDacă nu ați făcut nicio solicitare către " +
                "noi, ne cerem scuze și avem rugămintea să ignorați acest email.");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
            createUser(pass);
            resetFields();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(AdminActivity.this, "There are no email clients installed.",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void createUser(String pass) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final String add = address.getText().toString();
        final String si = site.getText().toString();

        mAuth.createUserWithEmailAndPassword(email.getText().toString(), pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        sendBtn.setEnabled(false);

                        if(task.isSuccessful()) {
                            hideVirtualKeyboard();
                            Toast.makeText(AdminActivity.this,
                                    "Înregistrare reușită", Toast.LENGTH_SHORT).show();

                            final FirebaseUser user = mAuth.getCurrentUser();
                            if(user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(AdminActivity.this,
                                                new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        sendBtn.setEnabled(true);

                                                        mAuth.signOut();

                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(getApplicationContext(),
                                                                    "Am trimis un email pentru activare",
                                                                    Toast.LENGTH_LONG).show();
                                                        } else {
                                                            Toast.makeText(getApplicationContext(),
                                                                    "Nu am putut trimite email de activare",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                        createNGODetails(user.getUid(), add, si);
                                                        resetFields();
                                                    }

                                                    private void createNGODetails(String uid,
                                                                                  String add,
                                                                                  String site) {

                                                        mDatabase.child("users")
                                                                .child(uid).child("NGODetails")
                                                                .child("address").setValue(add);

                                                        mDatabase.child("users")
                                                                .child(uid).child("NGODetails")
                                                                .child("site").setValue(site);

                                                    }
                                                });
                            }
                        }
                        progressDialog.dismiss();

                    }
                });
    }

    private void resetFields() {
        email.setText(null);
        address.setText(null);
        site.setText(null);
    }

    private void hideVirtualKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private String generatePassword(int length) {

        String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * str.length());
            salt.append(str.charAt(index));
        }

        return salt.toString();
    }

    private String verifyString(String str, int min, int max, int forToast) {
        str = str.replaceAll("\\s+$", "");
        str = str.replaceAll("^\\s+", "");
        str = str.replace("\n", "").replace("\r", "");

        int len = str.length();
        if(len < min) {
            if(forToast == 1) {
                Toast.makeText(getApplicationContext(), "Adresa este prea scurtă!",
                        Toast.LENGTH_SHORT).show();
            } else if(forToast == 2){
                Toast.makeText(getApplicationContext(), "Site-ul este prea scurt!",
                        Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        if(len > max) {
            if(forToast == 1) {
                Toast.makeText(getApplicationContext(), "Adresa este prea lungă!",
                        Toast.LENGTH_SHORT).show();
            } else if(forToast == 2){
                Toast.makeText(getApplicationContext(), "Site-ul este prea lung!",
                        Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        return str;
    }

    private void verify_email(String em) {

        if(TextUtils.isEmpty(em)) {
            Toast.makeText(this, "Adăugați un email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(em.length() < 3) {
            Toast.makeText(this, "Email prea scurt", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().fetchProvidersForEmail(em)
                .addOnCompleteListener(this, new OnCompleteListener<ProviderQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<ProviderQueryResult> task) {

                        if(task.isSuccessful()){
                            List<String> taskList = task.getResult().getProviders();

                            if(!taskList.isEmpty()){
                                Toast.makeText(AdminActivity.this,
                                        "Contul existentă", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if(verifyString(address.getText().toString(), 5, 100, 1) == null){
                                return;
                            }

                            if(verifyString(site.getText().toString(), 4, 100, 2) == null){
                                return;
                            }

                            String pass = generatePassword(6);
                            String em = email.getText().toString();
                            sendEmail(em, pass);
                        }
                    }
                });

    }

    /* ------------- End of Add Section ------------- */

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), BackPressedActivity.class);
        startActivityForResult(i, 100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(UsefulThings.mNetworkStateIntentReceiver == null ||
                UsefulThings.mNetworkStateChangedFilter == null) {
            UsefulThings.initNetworkListener();
        }
        registerReceiver(UsefulThings.mNetworkStateIntentReceiver,
                UsefulThings.mNetworkStateChangedFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(UsefulThings.mNetworkStateIntentReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 100) {
            if(resultCode == Activity.RESULT_OK){
                Bundle b = data.getExtras();
                if(b.getBoolean("result")) {
                    finish();
                }
            }
        }
    }

}
