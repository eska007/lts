package com.kaist.lts;

import org.json.simple.JSONObject;

/**
 * Created by Administrator on 2016-05-30.
 */
public interface ISession {
    enum RetVal {
        RET_OK,
        RET_FAIL,
        RET_NETWORK_ERROR,
        RET_DATABASE_ERROR,
        RET_DATABASE_DUPLICATED_ERROR,
        RET_PARAM_ERROR;
    }

    public RetVal Send(JSONObject profiles);
}

interface SubscribeCb {
    void onNotiication();
}

interface IRequesterSession extends ISession {
    public boolean Request(JSONObject obj);
    public boolean Subscribe(SubscribeCb callback);
}

interface ITranslatorSession extends ISession {
    public boolean Request(JSONObject obj);
    public boolean Subscribe(SubscribeCb callback);
}

interface IReviewerSession extends ISession {
    public boolean Request(JSONObject obj);
    public boolean Subscribe(SubscribeCb callback);
}