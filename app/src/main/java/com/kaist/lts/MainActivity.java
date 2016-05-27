package com.kaist.lts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "[LTS][MainActivity]";
    static final String PACKAGE_NAME = "com.kaist.lts";
    static private AccessManager am;
    static private AccessToken accessToken;
    static private AccessTokenTracker accessTokenTracker;

    private Context mContext;
    private SharedPreferences mPrefs;
    private CallbackManager callbackManager;
    private LoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;//getApplicationContext();
        mPrefs = getSharedPreferences("lts", MODE_PRIVATE);

        createAccessTokenTracker();
        if (accessToken != null) {
            //am.startClientActivity(mContext);
            am = new AccessManager(mContext);
            setContentView(R.layout.activity_main);
            facebookLogin();
            return;
        }

        //Display Intro page at 1st lunch app.
        boolean startStatus = mPrefs.getBoolean("startup", false);
        Log.d(TAG, "Bootup status: " + startStatus);
        if (!startStatus) {
            Intent intent = new Intent();
            intent.setClassName("com.kaist.lts", "com.kaist.lts.Intro");
            mContext.startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.activity_main);
            facebookLogin();
            getAppHashKey();
        }
    }

    private void createAccessTokenTracker() {
        Log.d(TAG, "createAccessTokenTracker");

        FacebookSdk.sdkInitialize(getApplicationContext());
        //if (accessTokenTracker != null && accessToken != null) return;
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
                accessToken = currentAccessToken;
            }
        };
        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken != null) {
            Log.d(TAG, "Facebook login status");
            Log.d(TAG, "Facebook login ID : " + accessToken.getUserId());
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyUp");

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            moveTaskToBack(true);
            finish();
            //android.os.Process.killProcess(android.os.Process.myPid());
        }
        return super.onKeyUp(keyCode, event);
    }

    private void getAppHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(PACKAGE_NAME, PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d(TAG, Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void facebookLogin() {
        callbackManager = CallbackManager.Factory.create();

        Button button = (Button) findViewById(R.id.button);
        button.setVisibility(View.VISIBLE);
        loginButton = (LoginButton) findViewById(R.id.facebook_login);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Success!");
                am = new AccessManager(mContext);
                finish();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "fail!");
                // App code
                Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d(TAG, "Exception!");
                // App code
                Toast.makeText(getApplicationContext(), "Exception!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
