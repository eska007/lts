package com.kaist.lts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.simple.JSONObject;

import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "[LTS][MainActivity]";
    static final String PACKAGE_NAME = "com.kaist.lts";
    static protected SharedPreferences mPrefs;
    static private AccessManager am;
    static private AccessTokenTracker accessTokenTracker;
    static private AccessToken accessToken;
    static private ProfileTracker profileTracker;
    static private Profile profile;
    private Context mContext;
    private CallbackManager callbackManager;
    private LoginButton facebookLoginButton;
    private MessageHandler handler;

    public static void callFacebookLogout() {
        LoginManager mLoginManager = LoginManager.getInstance();
        mLoginManager.logOut();

        Login.status = false;
    }

    static public void setSharedPref(String key, boolean ret) {
        if (mPrefs == null)
            return;

        SharedPreferences.Editor prefEditor = mPrefs.edit();
        prefEditor.putBoolean(key, ret);
        prefEditor.apply();
    }

    static public void setSharedPref(String key, String val) {
        if (mPrefs == null)
            return;

        SharedPreferences.Editor prefEditor = mPrefs.edit();
        prefEditor.putString(key, val);
        prefEditor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;//getApplicationContext();
        mPrefs = getSharedPreferences("lts", MODE_PRIVATE);

        FacebookSdk.sdkInitialize(mContext);
        setContentView(R.layout.activity_main);

        //Check facebook login status
        if (checkFbLoginStatus()) {
            return;
        }

        //Display Intro page at 1st lunch app.
        boolean startStatus = mPrefs.getBoolean("startup", false);
        Log.d(TAG, "Bootup status: " + startStatus);

        if (!startStatus) {
            AccessManager.startOtherActivity(mContext, AccessManager.INTRO_CLASS_NAME);
            finish();
        } else {
            facebookLogin();
            getAppHashKey();
        }

        Button Registbutton = (Button) findViewById(R.id.button_Registration);
        Registbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(mContext, SelectRegistration.class));
                AccessManager.startOtherActivity(mContext, AccessManager.SELREG_CLASS_NAME);
            }
        });

        Button loginButton = (Button) findViewById(R.id.buttonLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(mContext, Login.class));
                AccessManager.startOtherActivity(mContext, AccessManager.LOGIN_CLASS_NAME);
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

    private boolean checkFbLoginStatus() {
        Log.d(TAG, "checkFbLoginStatus");
        accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            Log.d(TAG, "Load the Client info");
            createAccessManager();
            setContentView(R.layout.activity_main);
            facebookLogin();
            return true;
        }
        return false;
    }

    private boolean getFacebookTracker() {
        Log.d(TAG, "createFacebookTracker");

        //if (accessTokenTracker != null && accessToken != null) return;
        if (accessTokenTracker == null) {
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
        }
        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();

/*        if (accessToken != null) {
            Log.d(TAG, "Facebook user ID : " + accessToken.getUserId());
        }*/
        if (profileTracker == null) {
            profileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                    this.stopTracking();
                    Profile.setCurrentProfile(currentProfile);
                    profile = currentProfile;
                }
            };
        }
        profile = Profile.getCurrentProfile();
        profileTracker.startTracking();

        if (accessToken != null && profile != null) {
            Login.id = profile.getId();
            Login.given_name = profile.getLastName();
            Login.sur_name = profile.getFirstName();

            handler = new MessageHandler();
            Login.status = true;
            Login.reg_id = Login.tryTocheckID(Login.id, handler);

            Log.d(TAG, "Facebook ID: " + Login.id + ", Name: " + Login.given_name + Login.sur_name);
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
       boolean ret = getFacebookTracker();
        Log.d(TAG, "Login Status: " + Login.status + ", Registration: " + mPrefs.getBoolean("registration", false));

        //1. 페북 & 직접 로그 아웃 상태
        if (!Login.status) {
            super.onResume();
        }

        //2. 페북 로그인 후, 가입 전 종료
        if (ret && !mPrefs.getBoolean("registration", false)) {
            AccessManager.startOtherActivity(mContext, AccessManager.SELREG_CLASS_NAME);
        } else {
            checkFbLoginStatus();
        }
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
                Log.d(TAG, "hash-Key:  " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
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
                getFacebookTracker();
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
            return;
        }

        if (!MainActivity.mPrefs.getBoolean("registration", false)) {
            Log.d(TAG, "Start Sel Registration");
            AccessManager.startOtherActivity(mContext, "com.kaist.lts.SelectRegistration");
        } else {
            AccessManager.startOtherActivity(mContext, "com.kaist.lts.ClientActivity");
        }

    }

    class MessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Login.RESULT_CHECK_ID:
                    if (msg.arg1 == Login.SUCCESS) {
                        Log.d(TAG, "checkID Success");
                        Login.reg_id = true;
                        //Login.status = true;
                        setSharedPref("registration", true);

                        AccessManager.startOtherActivity(mContext, AccessManager.CLIENT_CLASS_NAME);
                    } else {
                        Log.d(TAG, "checkID Failed");
                        Login.reg_id = false;
                        //Login.status = false;
                        setSharedPref("registration", false);

                        AccessManager.startOtherActivity(mContext, AccessManager.SELREG_CLASS_NAME);
                    }
                    finish();
                    break;
                default:
                    break;
            }
        }
    }
}
