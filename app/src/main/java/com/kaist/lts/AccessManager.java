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
        startClientActivity(context);
    }

    static public AccessManager getAccessManager() {
        return am;
    }

    public void startClientActivity(Context context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName("com.kaist.lts", "com.kaist.lts.ClientActivity");
        context.startActivity(intent);
    }

    public ISession GetSession() {
        return Session.GetInstance();
    }

}
