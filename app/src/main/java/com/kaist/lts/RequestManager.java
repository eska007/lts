package com.kaist.lts;

import android.util.Log;

import org.json.simple.JSONObject;

/**
 * Created by Administrator on 2016-06-02.
 */
public class RequestManager {
    static final String TAG = "[LTS][RequestManager]";

    public class LANGUAGE {
        public static final int KOREAN = 0;
        public static final int ENGLISH = 1;
        public static final int CHINESE = 2;
        public static final int JAPANESE = 3;
    }

    public static void addNewRequest(ISession cs) {

        JSONObject input = new JSONObject();
        input.put("command", "ADD_NEW_REQUEST");

        // TODO: Read and put the input fields...
        input.put("subject", "1st request");
        input.put("target_language", LANGUAGE.ENGLISH);
        input.put("source_doc_path", "./uploads/aaa.txt");
        // ...

        // get session, and register profiles
        JSONObject output = new JSONObject();
        ISession.RetVal ret = cs.SendRequest(input, output);
        Log.d(TAG, "ADD_NEW_REQUEST result : " + ret + ", output : " + output.toString());
    }

    public static void getNewRequest(ISession cs) { // Polling by background thread

        JSONObject input = new JSONObject();
        input.put("command", "GET_NEW_REQUEST");

        // get session, and register profiles
        JSONObject output = new JSONObject();
        ISession.RetVal ret = cs.SendRequest(input, output);
        Log.d(TAG, "ADD_NEW_REQUEST result : " + ret + ", output : " + output.toString());
    }
}
