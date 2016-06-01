package com.kaist.lts;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by user on 2016-05-17.
 */
public class AccessManager {
    static final String TAG = "[LTS][AccessManager]";
    Context mContext;
    public AccessManager(Context context) {
        Log.d(TAG, "Create");
        mContext = context;
        //Start ClientActivity class.
        GetSession();
        startClientActivity(context);
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
