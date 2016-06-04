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

    public static int addNewRequest(ISession cs) {
        JSONObject input = new JSONObject();
        input.put("command", "ADD_NEW_REQUEST");

        // TODO: Read and put the input fields...
        input.put("subject", "1st request");
        input.put("target_language", LANGUAGE.ENGLISH);
        input.put("source_doc_path", "./uploads/aaa.txt");
        // ...

        JSONObject output = new JSONObject();
        ISession.RetVal ret = cs.SendRequest(input, output);
        Log.d(TAG, "ADD_NEW_REQUEST result : " + ret + ", output : " + output.toString());
        if (ret != ISession.RetVal.RET_OK)
            return -1;

        return (int)(output.get("id")); // Return request_id, it's positive value if it's valid.
        // TODO: Try "new Notifier(Notifier.Command.GET_LIST_OF_CANDIDATES,this.getApplicationContext());" after this function,
    }

    public static JSONObject getRequestInfo(ISession cs, int request_id /*request_id will be provided by Notifier(NEW_TRANSLATION_REQUEST) */) {
        JSONObject input = new JSONObject();
        input.put("command", "GET_REQUEST_INFO");
        input.put("id", request_id);

        JSONObject output = new JSONObject();
        ISession.RetVal ret = cs.SendRequest(input, output);
        Log.d(TAG, "GET_REQUEST_INFO : " + ret + ", output : " + output.toString());
        return output;
    }

    public static boolean bid(ISession cs, int request_id) {
        // Server know the user-id and user-mode of this caller.
        JSONObject input = new JSONObject();
        input.put("command", "BID");
        input.put("id",  request_id);

        JSONObject output = new JSONObject();
        ISession.RetVal ret = cs.SendRequest(input, output);
        if (ret != ISession.RetVal.RET_OK)
            return false;

        Log.d(TAG, "BID result : " + ret + ", output : " + output.toString());

        // TODO: Try "new Notifier(Notifier.Command.GET_RESULT_OF_BID,this.getApplicationContext());" after this function,
        return true;
    }

    public static boolean employ(ISession cs, int request_id, String member_id) {
        JSONObject input = new JSONObject();
        input.put("command", "EMPLOY");
        input.put("id",  request_id);
        input.put("member_id",  member_id);

        JSONObject output = new JSONObject();
        ISession.RetVal ret = cs.SendRequest(input, output);
        if (ret != ISession.RetVal.RET_OK)
            return false;
        Log.d(TAG, "EMPLOY result : " + ret + ", output : " + output.toString());
        return true;
    }

    public static String getResultOfBid(ISession cs, int request_id) {
        JSONObject input = new JSONObject();
        input.put("command", "GET_RESULT_OF_BID");
        input.put("id", request_id);

        JSONObject output = new JSONObject();
        ISession.RetVal ret = cs.SendRequest(input, output);
        Log.d(TAG, "GET_RESULT_OF_BID result : " + ret + ", output : " + output.toString());

        if (ret != ISession.RetVal.RET_OK || output == null || output.size() == 0)
            return null;

        if (output.get("translator_id") != null)
            return (String)output.get("translator_id");
        else if (output.get("reviewer_id") != null)
            return (String)output.get("reviewer_id");

        Log.e(TAG, "There is no meaningful info");
        return null;
    }

}
