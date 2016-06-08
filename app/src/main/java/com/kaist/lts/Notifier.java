package com.kaist.lts;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import org.json.simple.JSONObject;

/**
 * Created by Administrator on 2016-06-03.
 */
public class Notifier {
    static final String TAG = "[LTS][Notifier]";

    private Command mCmd;
    private int mRequestID = -1;
    Context mContext;

    public enum Command{
        NEW_REQUEST("GET_NEW_REQUEST"), // by Translator / Reviewer, Invoke whenever,  RETURN: Integer / RequestID
        LIST_OF_CANDIDATES("GET_LIST_OF_CANDIDATES"), // by Requester / Translator,  Invoke after adding new request, RETURN: String / Series of Member IDs ";ID1;ID2;ID3;..."
        RESULT_OF_BID("GET_RESULT_OF_BID"),   // by Translator or Reviewer, Invoke after bid,   RETURN: String / Selected Member ID
        RESULT_OF_WORK("GET_RESULT_OF_WORK");   // by Translator or Requester, Invoke after ReviewRequest,  RETURN: ISession.RetVal
        private String cmd;

        Command(String str) {
            cmd = str;
        }
        public String get() {
            return cmd;
        }

        boolean parseOutput(int request_id, JSONObject output, NotificationCompat.Builder builder) {
            switch(this) {
                case NEW_REQUEST:{
                    String id;
                    if (output.get("new_request") != null)
                        id = (String) output.get("new_request");
                    else if (output.get("id") != null)
                        id = (String) output.get("id");
                    else
                        id = output.toString();

                    Log.e(TAG, "New Request is delivered: "+id);
                    Bundle bd = new Bundle();
                    bd.putString("request_id", id);
                    builder.setExtras(bd);
                    builder.setTicker("신규 의뢰가 도착했습니다. ID:" + id);
                    builder.setContentTitle("신규 의뢰가 도착했습니다. ID:" + id);
                    return true;
                }

                case LIST_OF_CANDIDATES: {
                    String key = "translator_candidate_list";
                    String candidates = (String) output.get(key);
                    if (candidates == null || candidates.isEmpty()) {
                        key = "reviewer_candidate_list";
                        candidates = (String)output.get(key);
                        if (candidates == null || candidates.isEmpty()) {
                            return false;
                        }
                    }
                    Bundle bd = new Bundle();
                    bd.putStringArray(key, candidates.split(";"));
                    bd.putInt("request_id", request_id);
                    builder.setExtras(bd);
                    builder.setTicker("List of candidates");
                    builder.setContentText("지원자 리스트 : " + candidates.replace(';', ','));
                    if (key.equals("translator_candidate_list")) {
                        builder.setTicker("의뢰ID(" + request_id + ") 번역지원자 모집완료");
                        builder.setContentTitle("의뢰ID(" + request_id + ") 번역지원자 모집완료");
                    }else {
                        builder.setTicker("의뢰ID(" + request_id + ") 감수지원자 모집완료");
                        builder.setContentTitle("의뢰ID(" + request_id + ") 감수지원자 모집완료");
                    }
                    return true;
                }

                case RESULT_OF_BID: {
                    String key = "translator_id";
                    String employee = (String) output.get(key);
                    if (employee == null || employee.isEmpty()) {
                        key = "reviewer_id";
                        employee = (String)output.get(key);
                        if (employee == null || employee.isEmpty()) {
                            return false;
                        }
                    }
                    Bundle bd = new Bundle();
                    bd.putString(key, employee);
                    bd.putInt("request_id", request_id);
                    builder.setExtras(bd);
                    if (key.equals("translator_id")) {
                        builder.setTicker("의뢰ID(" + request_id + ") 번역가 채택 완료");
                        builder.setContentTitle("의뢰ID(" + request_id + ") 번역가 채택 완료");
                    }else {
                        builder.setTicker("의뢰ID(" + request_id + ") 감수자 채택 완료");
                        builder.setContentTitle("의뢰ID(" + request_id + ") 감수자 채택 완료");
                    }
                    builder.setContentText("채택된 지원자ID: " + employee);
                    return true;
                }

                case RESULT_OF_WORK: {
                    Bundle bd = new Bundle();
                    bd.putString("request_id", Integer.toString(request_id));
                    builder.setExtras(bd);
                    builder.setContentText("Request ID : " + Integer.toString(request_id));
                    return true;
                }

                default:
                    break;
            }

            return false;
        }
    }

    private Notifier() { // Prevent invalid construction
    }

    public Notifier(Command cmd, int req_id, Context context) {
        mCmd = cmd;
        mRequestID = req_id;
        mContext = context;
        StartMonitoring();
    }

    public Notifier(Command cmd, Context context) {
        mCmd = cmd;
        mContext = context;
        StartMonitoring();
    }

    private void StartMonitoring() {
        Runnable run = new Runnable() {
            /* UI Main thread doesn't allow any direct network connection */
            @Override
            public void run() {
                JSONObject input = new JSONObject();
                input.put("command", mCmd.get());
                if (mRequestID != -1)
                    input.put("id", mRequestID);
                JSONObject output = new JSONObject();

                while(true) {
                    ISession ss = Session.GetInstance();
                    ISession.RetVal ret = ss.SendRequest(input, output);
                    if (ret == ISession.RetVal.RET_OK) {
                        // TODO: Send 'output' and request_id(if exist) to caller through callback.
                        PendingIntent intent = PendingIntent.getActivity(mContext, 0,
                                new Intent(mContext, ClientActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                                .setSmallIcon(R.drawable.ic_stat_new_message)
                                .setContentIntent(intent)
                                .setVibrate(new long [] {0, 1000})
                                .setAutoCancel(true);
                        if (true == mCmd.parseOutput(mRequestID, output, builder)) {
                            nm.notify(0, builder.build());
                            if (mCmd != Command.NEW_REQUEST) // NEW_REQUEST SHOULD BE CHECKED FOREVER
                                break;
                        }
                    }

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "Notified : " + output.toString());
            }
        };
        Thread bg = new Thread(run);
        bg.start();
    }
}
