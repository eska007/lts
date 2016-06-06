package com.kaist.lts;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class SelectRegistration extends AppCompatActivity implements View.OnClickListener {
    static private Context mContext;
    final String TAG = "[LTS][SelectReg]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_registration);

        Button btCustomer = (Button) findViewById(R.id.btn_customer);
        btCustomer.setOnClickListener(this);
        Button btReviewer = (Button) findViewById(R.id.btn_reviewer);
        btReviewer.setOnClickListener(this);
        Button btTransltor = (Button) findViewById(R.id.btn_translator);
        btTransltor.setOnClickListener(this);

        /*new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getApplicationContext(), Registration.class));
                AccessManager.startOtherActivity(mContext, AccessManager.REGIST_CLASS_NAME);

            }
        });
        button = (Button) findViewById(R.id.btn_translator);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getApplicationContext(), Registration.class));
                AccessManager.startOtherActivity(mContext, AccessManager.REGIST_CLASS_NAME);
            }
        });
        button = (Button) findViewById(R.id.btn_reviewer);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getApplicationContext(), Registration.class));
            }
        });*/
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyUp");

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            moveTaskToBack(true);
            if (!Login.reg_id) {
                Log.d(TAG, "Stop to regist with facebook-ID");
                SharedPreferences.Editor prefEditor = MainActivity.mPrefs.edit();
                prefEditor.putBoolean("registration", false);
                prefEditor.apply();
            }
            finish();
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        AccessManager.startOtherActivity(mContext, AccessManager.REGIST_CLASS_NAME);
    }
}
