package com.kaist.lts;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.simple.JSONObject;

/**
 * Created by Administrator on 2016-06-01.
 */
public class Login extends AppCompatActivity {
    // TODO: Decide the type of Login class (activity? or..)
    private MessageHandler mainHandler = null;
    static final int RESULT_LOGIN = 0;
    static final String TAG = "[LTS][Login]";

    private EditText idEt;
    private EditText pwEt;
    private Button okBtn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        idEt = (EditText) findViewById(R.id.login_id);
        pwEt = (EditText) findViewById(R.id.login_pw);
        okBtn = (Button) findViewById(R.id.login_ok);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "OK Clicked");
                final String id = idEt.getText().toString();
                final String pw = pwEt.getText().toString();

                if (id.isEmpty() || pw.isEmpty()) {
                    // TODO: Show error popup!
                    Log.e(TAG, "missing id or pw");
                    return;
                }

                final MessageHandler mainHandler = new MessageHandler();
                Runnable run = new Runnable() {
                    /* UI Main thread doesn't allow any direct network connection */
                    @Override
                    public void run() {
                        boolean ret = log_in(Session.GetInstance(), generateJson(id, pw));
                        //boolean ret = log_in(Session.GetInstance(), generateJson("test_id", "1234"));
                        Message msg = mainHandler.obtainMessage();
                        msg.what = RESULT_LOGIN;
                        msg.arg1 = ret ? MessageHandler.SUCCESS:MessageHandler.FAILED;
                        mainHandler.sendMessage(msg); // send a result to main UI thread
                    }
                };
                Thread bg = new Thread(run);
                bg.start();
            }
        });
    }

    class MessageHandler extends Handler {
        final static int SUCCESS = 1;
        final static int FAILED = 0;
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RESULT_LOGIN:
                    if (msg.arg1 == SUCCESS) {
                        Log.d(TAG, "Login Success");
                        // TODO: GOTO CLIENT ACTIVITY
                        startActivity(new Intent(getApplicationContext(), ClientActivity.class));
                        finish();
                    } else {
                        Log.d(TAG, "Login Failed");
                        // TODO: Show failed popup
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private JSONObject generateJson(String id, String password) {
        JSONObject profiles = new JSONObject();
        profiles.put("id", id);
        profiles.put("password", password);
        return profiles;
    }

    private boolean log_in(ISession cs, JSONObject profiles) {
        profiles.put("command", "LOGIN");
        ISession.RetVal ret = cs.Send(profiles);
        Log.d(TAG, "register result : " + ret);
        return (ret == ISession.RetVal.RET_OK);
    }
}
