package com.kaist.lts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewDebug;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.server.converter.StringToIntConverter;

import org.json.simple.JSONObject;

import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "[LTS][MainActivity]";
    static final String PACKAGE_NAME = "com.kaist.lts";
    static private AccessManager am;
    static private AccessTokenTracker accessTokenTracker;
    static private AccessToken accessToken;
    static private ProfileTracker profileTracker;
    static private Profile profile;

    private Context mContext;
    private SharedPreferences mPrefs;
    private CallbackManager callbackManager;
    private LoginButton facebookLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;//getApplicationContext();
        mPrefs = getSharedPreferences("lts", MODE_PRIVATE);

        FacebookSdk.sdkInitialize(getApplicationContext());
        accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken != null) {
            createAccessManager();
            setContentView(R.layout.activity_main);
            facebookLogin();
            return;
        }

        //Display Intro page at 1st lunch app.
        boolean startStatus = mPrefs.getBoolean("startup", false);
        Log.d(TAG, "Bootup status: " + startStatus);
        if (!startStatus) {
            startIntroActivity();
        } else {
            setContentView(R.layout.activity_main);
            facebookLogin();
            getAppHashKey();
        }

        Button Registbutton = (Button) findViewById(R.id.button_Registration);
        Registbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, SelectRegistration.class));
                finish();
            }
        });

        Button loginButton = (Button) findViewById(R.id.buttonLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });

        Button testButton = (Button) findViewById(R.id.buttonTest);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        ISession ss = Session.GetInstance();
                        JSONObject input = new JSONObject();
                        JSONObject output = new JSONObject();
                        input.put("command", "GET_USER_MODE");
                        ISession.RetVal ret = ss.SendRequest(input, output);
                        Log.d(TAG, "Ret : "+ret);
                    }
                };
                Thread bg = new Thread(run);
                bg.start();
            }
        });
    }

    private void startIntroActivity() {
        Intent intent = new Intent();
        intent.setClassName("com.kaist.lts", "com.kaist.lts.Intro");
        mContext.startActivity(intent);
        finish();
    }

    private void createFacebookTracker() {
        Log.d(TAG, "createFacebookTracker");

        //if (accessTokenTracker != null && accessToken != null) return;
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
                this.stopTracking();
                accessToken = currentAccessToken;
            }
        };
        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();

/*        if (accessToken != null) {
            Log.d(TAG, "Facebook user ID : " + accessToken.getUserId());
        }*/

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                this.stopTracking();
                Profile.setCurrentProfile(currentProfile);
                profile = currentProfile;
            }
        };
        profile = Profile.getCurrentProfile();
        profileTracker.startTracking();

        if (profile != null) {
            Log.d(TAG, "Facebook login ID: " + profile.getId());
            Log.d(TAG, "Facebook Name: " + profile.getLastName() + profile.getFirstName());
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
        releaseTracker();
    }

    private void releaseTracker() {
        if (accessTokenTracker != null) {
            accessTokenTracker.stopTracking();
        }
        if (profileTracker != null) {
            profileTracker.stopTracking();
        }
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

        facebookLoginButton = (LoginButton) findViewById(R.id.facebook_login);
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Success!");

                createFacebookTracker();
                createAccessManager();
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

    private void createAccessManager() {
        if (am == null) {
            am = new AccessManager(mContext);
        } else {
            am.startClientActivity(mContext);
        }
    }
}
