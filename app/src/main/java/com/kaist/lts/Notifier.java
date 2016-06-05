package com.kaist.lts;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
        RESULT_OF_REVIEW("GET_RESULT_OF_WORK");   // by Translator or Requester, Invoke after ReviewRequest,  RETURN: ISession.RetVal
        private String cmd;
        Command(String str) {
            cmd = str;
        }
        public String get() {
            return cmd;
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
                                .setTicker("LTS Notification Ticker")
                                .setContentIntent(intent)
                                .setContentTitle(mCmd.get())
                                .setVibrate(new long [] {0, 1000})
                                .setAutoCancel(true);

                        String id;
                        if (output.get("new_request") != null)
                            id = (String) output.get("new_request");
                        else if (output.get("id") != null)
                            id = (String) output.get("id");
                        else
                            id = output.toString();
                        builder.setContentText("Request ID : " + id);
                        nm.notify(0, builder.build());
                        break;
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
