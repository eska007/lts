package com.kaist.lts;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created by Administrator on 2016-05-30.
 */
public interface ISession {
    RetVal Send(JSONObject data);

    RetVal SendRequest(JSONObject input, JSONObject output);

    HttpURLConnection ConnectServer(String addr, String property_key, String property_val) throws IOException;
    enum RetVal {
        RET_OK,
        RET_FAIL,
        RET_NETWORK_ERROR,
        RET_DATABASE_ERROR,
        RET_DATABASE_DUPLICATED_ERROR,
        RET_PARAM_ERROR
    }
}