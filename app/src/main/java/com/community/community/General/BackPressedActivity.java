package com.community.community.General;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.community.community.R;

public class BackPressedActivity extends AppCompatActivity {

    private Button im_sure = null;
    private Button im_not_sure = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.back_pressed_activity);

        im_sure = (Button) findViewById(R.id.im_sure);
        im_sure.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", true);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }

        });

        im_not_sure = (Button) findViewById(R.id.im_not_sure);
        im_not_sure.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", false);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }

        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", false);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        im_sure = null;
        im_not_sure = null;
    }
}
