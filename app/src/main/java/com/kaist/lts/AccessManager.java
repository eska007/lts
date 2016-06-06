package com.kaist.lts;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by user on 2016-05-17.
 */
public class AccessManager {
    static final String LTS_PACKAGE_NAME = "com.kaist.lts";
    static final String INTRO_CLASS_NAME = "com.kaist.lts.Intro";
    static final String CLIENT_CLASS_NAME = "com.kaist.lts.ClientActivity";
    static final String REGIST_CLASS_NAME = "com.kaist.lts.Registration";
    static final String SELREG_CLASS_NAME = "com.kaist.lts.SelectRegistration";
    static final String LOGIN_CLASS_NAME = "com.kaist.lts.Login";
    private static AccessManager am;
    final String TAG = "[LTS][AccessManager]";

    public AccessManager(Context context) {
        Log.d(TAG, "Create");
        am = this;
        //Start ClientActivity class.
        GetSession();

        if (MainActivity.mPrefs.getBoolean("registration", false) == false) {
            Log.d(TAG, "Start Sel Registration");
            startOtherActivity(context, SELREG_CLASS_NAME);
        } else {
            Log.d(TAG, "Start ClientActivity");
            startOtherActivity(context, CLIENT_CLASS_NAME);
        }

        if (Login.reg_id) {
            Log.d(TAG, "Start ClientActivity");
            startOtherActivity(context, CLIENT_CLASS_NAME);
        }
    }

    static public AccessManager getAccessManager() {
        return am;
    }

    static public void startOtherActivity(Context context, String className) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(LTS_PACKAGE_NAME, className);
        context.startActivity(intent);
    }

    public ISession GetSession() {
        return Session.GetInstance();
    }

}
