package com.kaist.lts;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by user on 2016-05-17.
 */
public class AccessManager {
    static final String LTS_PACKAGE_NAME = "com.kaist.lts";
    static final String MAIN_CLASS_NAME = "com.kaist.lts.MainActivity";
    static final String INTRO_CLASS_NAME = "com.kaist.lts.Intro";
    static final String CLIENT_CLASS_NAME = "com.kaist.lts.ClientActivity";
    static final String REGIST_CLASS_NAME = "com.kaist.lts.Registration";
    static final String SELREG_CLASS_NAME = "com.kaist.lts.SelectRegistration";
    static final String LOGIN_CLASS_NAME = "com.kaist.lts.Login";
    static final String SELWKR_CLASS_NAME = "com.kaist.lts.SelectWorkerActivity";
    static final String WORKLISTV_CLASS_NAME = "com.kaist.lts.WorklistViewer";

    private static AccessManager am;
    private static ISession sc;
    private static ProfileManager pm;
    final String TAG = "[LTS][AccessManager]";

    public AccessManager(Context context) {
        Log.d(TAG, "Create");
        am = this;

        //Create Profile Manager
        pm = new ProfileManager();

        //Start connect Session
        sc = getConnectSession();

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

    public ProfileManager getProfileManager() {
        if (pm != null) {
            return pm;
        }
        return new ProfileManager();
    }

    public ISession getConnectSession() {
        if (sc != null) {
            return sc;
        }
        return Session.GetInstance();
    }
}
