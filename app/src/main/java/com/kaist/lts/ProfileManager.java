package com.kaist.lts;

import android.util.Log;

import org.json.simple.JSONObject;

/**
 * Created by Administrator on 2016-06-02.
 */
public class ProfileManager {
    static final String TAG = "[LTS][ProfileManager]";
    static int user_mode;

    public static int getUserMode(ISession cs) {
        // Fill profiles
        JSONObject profiles = new JSONObject();
        profiles.put("command", "GET_USER_MODE");

        // get session, and register profiles
        JSONObject output = new JSONObject();
        ISession.RetVal ret = cs.SendRequest(profiles, output);

        Log.d(TAG, "getUserMode result : " + output.toString());
        if (ret != ISession.RetVal.RET_OK)
            return -1;

        String mode = (String) output.get("user_mode");

        user_mode = Integer.parseInt(mode);
        switch (user_mode) {
            case 0:
                return USER_MODE.REQUESTER;
            case 1:
                return USER_MODE.TRANSLATOR;
            case 2:
                return USER_MODE.REVIEWER;
            default:
                break;
        }
        return -1;
    }

    public static JSONObject getMemberInfo(ISession cs, String member_id) {
        JSONObject input = new JSONObject();
        input.put("command", "GET_MEMBER_INFO");
        input.put("id", member_id);

        JSONObject output = new JSONObject();
        ISession.RetVal ret = cs.SendRequest(input, output);
        if (ret != ISession.RetVal.RET_OK) {
            return null;
        }

        Log.d(TAG, "GET_MEMBER_INFO result : " + ret + ", output : " + output.toString());
        return output;
    }

    public static JSONObject getMyInfo(ISession cs) {
        JSONObject input = new JSONObject();
        input.put("command", "GET_MEMBER_INFO");

        JSONObject output = new JSONObject();
        ISession.RetVal ret = cs.SendRequest(input, output);
        if (ret != ISession.RetVal.RET_OK) {
            return null;
        }

        Log.d(TAG, "GET_MEMBER_INFO result : " + ret + ", output : " + output.toString());
        return output;
    }

    public class USER_MODE {
        public static final int REQUESTER = 0;
        public static final int TRANSLATOR = 1;
        public static final int REVIEWER = 2;
    }
}
