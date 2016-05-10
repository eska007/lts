package com.kaist.lts;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";
    CallbackManager callbackManager;
    LoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        facebookLogin();
    }

    private void facebookLogin() {
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.facebook_login);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Success!");
                //App code
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "fail!");
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }
}
