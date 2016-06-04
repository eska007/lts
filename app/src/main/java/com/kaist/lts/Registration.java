package com.kaist.lts;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.simple.JSONObject;

public class Registration extends AppCompatActivity {

    private EditText idEt;
    private EditText passwordEt;
    private EditText repasswordEt;
    private EditText nameEt;
    private EditText surnameEt;
    private Button manButton;
    private Button womanButton;
    private EditText emailEt;
    private EditText phoneEt;
    private EditText countryEt;
    private EditText addressEt;
    private Button registrationButton;
    private MessageHandler mainHandler = null;
    static final String TAG = "[LTS][Registration]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        idEt = (EditText) findViewById(R.id.et_id);
        passwordEt = (EditText) findViewById(R.id.et_password);
        repasswordEt = (EditText) findViewById(R.id.et_repassword);
        nameEt = (EditText) findViewById(R.id.et_names);
        surnameEt = (EditText) findViewById(R.id.et_surname);
        manButton = (Button) findViewById(R.id.btn_man);
        womanButton = (Button) findViewById(R.id.btn_woman);
        emailEt = (EditText) findViewById(R.id.et_email);
        phoneEt = (EditText) findViewById(R.id.et_phone);
        countryEt = (EditText) findViewById(R.id.et_country);
        addressEt = (EditText) findViewById(R.id.et_address);
        registrationButton = (Button) findViewById(R.id.btn_registeration);

        mainHandler = new MessageHandler();

        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked");
                final String id = idEt.getText().toString();
                final String password = passwordEt.getText().toString();
                final String name = nameEt.getText().toString();
                final String surname = surnameEt.getText().toString();
                final String phone = phoneEt.getText().toString();
                final String email = emailEt.getText().toString();
                final String country = countryEt.getText().toString();
                final String address = addressEt.getText().toString();

                if (id.isEmpty() || password.isEmpty() || name.isEmpty() || surname.isEmpty()
                        || phone.isEmpty() || email.isEmpty() || country.isEmpty() || address.isEmpty()) {
                    // TODO: Show error popup!
                    Log.e(TAG, "some missing profiles");
                    return;
                }

                // Request Registration
                Runnable run = new Runnable() {
                    /* UI Main thread doesn't allow any direct network connection */
                    @Override
                    public void run() {
                        boolean ret = register(Session.GetInstance(),
                                generateJson(id, password, name, surname, phone, email, country, address));
                        /* boolean ret = register(Session.GetInstance(),
                            generateJson("test_id", "1234", "name","surname","01000011", "ys@naver.com", "korea", "suwon"));*/
                        Message msg = mainHandler.obtainMessage();
                        msg.what = MessageHandler.RESULT_REGISTRATION;
                        msg.arg1 = ret ? MessageHandler.SUCCESS:MessageHandler.FAILED;
                        mainHandler.sendMessage(msg);
                    }
                };
                Thread bg = new Thread(run);
                bg.start();
            }
        });
    }

    class MessageHandler extends Handler {
        final static int RESULT_REGISTRATION = 0;
        final static int SUCCESS = 1;
        final static int FAILED = 0;
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RESULT_REGISTRATION:
                    if (msg.arg1 == SUCCESS) {
                        // TODO: goto main view and wait for login by user?
                    } else {
                        // TODO: Show error popup (ask user to adjust input again)
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private JSONObject generateJson(String id, String password, String name, String surname,
                                    String phone, String email, String country, String address) {
        JSONObject profiles = new JSONObject();
        profiles.put("id", id);
        profiles.put("password", password);
        profiles.put("family_name", name);
        profiles.put("first_name", surname);
        profiles.put("email", email);
        profiles.put("phone", phone);
        profiles.put("country", country);
        profiles.put("address", address);
        return profiles;
    }

    private boolean register(ISession cs, JSONObject profiles) {
        profiles.put("command", "REGISTER");
        ISession.RetVal ret = cs.Send(profiles);

        Log.d(TAG, "register result : " + ret);
        return (ret == ISession.RetVal.RET_OK);
    }
}