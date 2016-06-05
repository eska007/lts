package com.kaist.lts;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by user on 2016-05-17.
 */
public class AccessManager {
    private static AccessManager am;
    final String TAG = "[LTS][AccessManager]";

    public AccessManager(Context context) {
        Log.d(TAG, "Create");
        am = this;
        //Start ClientActivity class.
        GetSession();

        if (MainActivity.mPrefs.getBoolean("registration", false) == false) {
            Log.d(TAG, "Start Sel Registration");
            startOtherActivity(context, "com.kaist.lts.SelectRegistration");
        } else {
            startOtherActivity(context, "com.kaist.lts.ClientActivity");
        }

        if (Login.reg_id) {
            Log.d(TAG, "Start ClientActivity");
            startOtherActivity(context, "com.kaist.lts.ClientActivity");
        }
    }

    static public AccessManager getAccessManager() {
        return am;
    }

    static public void startOtherActivity(Context context, String className) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName("com.kaist.lts", className);
        context.startActivity(intent);
    }

    public ISession GetSession() {
        return Session.GetInstance();
    }

}
