package com.kaist.lts;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.simple.JSONObject;

/**
 * Created by Administrator on 2016-06-01.
 */
public class Login extends AppCompatActivity {
    static final String TAG = "[LTS][Login]";
    final static int RESULT_LOGIN = 0;
    final static int RESULT_CHECK_ID = 1;
    final static int SUCCESS = 1;
    final static int FAILED = 0;
    static MessageHandler mHandler = null;
    static boolean reg_id = false;
    static boolean status = false;
    static String id;
    static String given_name;
    static String sur_name;
    // TODO: Decide the type of Login class (activity? or..)
    private MessageHandler mainHandler = null;
    private EditText idEt;
    private EditText pwEt;
    private Button okBtn;

    static public void tryToLogin(final String id, final String pw) {

        if (mHandler == null) {
            Log.d(TAG, "Handler is null");
            return;
        }

        Runnable run = new Runnable() {
            /* UI Main thread doesn't allow any direct network connection */
            @Override
            public void run() {
                boolean ret = log_in(Session.GetInstance(), generateJson(id, pw));
                //boolean ret = log_in(Session.GetInstance(), generateJson("test_id", "1234"));
                Message msg = mHandler.obtainMessage();
                msg.what = RESULT_LOGIN;
                msg.arg1 = ret ? SUCCESS : FAILED;
                mHandler.sendMessage(msg); // send a result to main UI thread
            }
        };
        Thread bg = new Thread(run);
        bg.start();
    }

    static public boolean tryTocheckID(final String id, final Handler handler) {

        if (id == null || handler == null || id.isEmpty()) {
            return false;
        }

        Runnable run = new Runnable() {
            /* UI Main thread doesn't allow any direct network connection */
            @Override
            public void run() {
                boolean ret = check_id(Session.GetInstance(), generateJson(id));
                //boolean ret = log_in(Session.GetInstance(), generateJson("test_id", "1234"));
                Message msg = new Message(); //msg = mHandler.obtainMessage();
                msg.what = RESULT_CHECK_ID;
                msg.arg1 = ret ? SUCCESS : FAILED;
                handler.sendMessage(msg); // send a result to main UI thread
            }
        };
        Thread bg = new Thread(run);
        bg.start();
        return true;
    }

    static private JSONObject generateJson(String id, String password) {
        JSONObject profiles = new JSONObject();
        profiles.put("id", id);
        profiles.put("password", password);
        return profiles;
    }

    static private JSONObject generateJson(String id) {
        JSONObject profiles = new JSONObject();
        profiles.put("id", id);
        return profiles;
    }

    static private boolean log_in(ISession cs, JSONObject profiles) {
        profiles.put("command", "LOGIN");
        ISession.RetVal ret = cs.Send(profiles);
        Log.d(TAG, "login result : " + ret);
        return (ret == ISession.RetVal.RET_OK);
    }

    static private boolean check_id(ISession cs, JSONObject profiles) {
        profiles.put("command", "CHECKID");
        ISession.RetVal ret = cs.Send(profiles);
        Log.d(TAG, "checkid result : " + ret);
        return (ret == ISession.RetVal.RET_OK);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        idEt = (EditText) findViewById(R.id.login_id);
        pwEt = (EditText) findViewById(R.id.login_pw);
        okBtn = (Button) findViewById(R.id.login_ok);
        mHandler = new MessageHandler();

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "OK Clicked");
                final String id = idEt.getText().toString();
                final String pw = pwEt.getText().toString();

                if (id.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Missing user's ID", Toast.LENGTH_SHORT);
                    return;
                } else if (pw.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Missing user's PASSWORD", Toast.LENGTH_SHORT);
                }

                tryToLogin(id, pw);
            }
        });
    }

    class MessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RESULT_LOGIN:
                    if (msg.arg1 == SUCCESS) {
                        Log.d(TAG, "Login Success");
                        Login.status = true;
                        MainActivity.setSharedPref("registration", true);

                        //Start client Activity
                        AccessManager.startOtherActivity(getApplicationContext(), AccessManager.CLIENT_CLASS_NAME);
                        finish();
                    } else {
                        Log.d(TAG, "Login Failed");
                        Login.status = false;
                        MainActivity.setSharedPref("registration", false);
                    }
                    break;

                default:
                    break;
            }
        }
    }
}
