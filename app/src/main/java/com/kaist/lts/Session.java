package com.kaist.lts;

import android.util.Log;

import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by user on 2016-05-19.
 */
public class Session implements ISession{
    static final String TAG = "[LTS][Session]";
    private static ISession instance = null;
    boolean is_connected;
    String cookieString;

    private Session() {
        is_connected = false;
        cookieString = null;

        HttpURLConnection con;
        try {
            con = ConnectServer(null, null);
            // Get Cookie to keep the session
            if (con != null) {
                String new_cookie = con.getHeaderField("Set-Cookie");
                if (new_cookie != null) {
                    Log.d(TAG, "Got new cookie:" + new_cookie);
                    Log.d(TAG, "Old cookie:" + cookieString);
                    cookieString = new_cookie;
                }
                is_connected = true;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.d(TAG, "SessionCreated");
    }

    public static synchronized ISession GetInstance() { // Singleton
        if (instance == null) {
            instance = new Session();
        }
        Log.d(TAG, "Created");
        return instance;
    }

    private HttpURLConnection ConnectServer(String property_key, String property_val) throws IOException {
        URL url = new URL("http://52.78.20.109/main2.php");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        if (cookieString != null) {
            con.setRequestProperty("cookie", cookieString);
            Log.d(TAG, "Reuse cookie:" + cookieString);
        }
        con.setDefaultUseCaches(false);
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        if (property_key != null && property_val != null) {
            con.setRequestProperty(property_key, property_val);
            return con;
        }
        return null;
    }

    private RetVal ConvertResult(String arg)
    {
        Log.d(TAG, "ConvertResult : " + arg);
        switch(arg) {
            case "0":	return ISession.RetVal.RET_OK;
            case "2":	return ISession.RetVal.RET_NETWORK_ERROR;
            case "3":	return ISession.RetVal.RET_DATABASE_ERROR;
            case "4":	return ISession.RetVal.RET_DATABASE_DUPLICATED_ERROR;
            case "5":	return ISession.RetVal.RET_PARAM_ERROR;
            default:
                break;
        }

        return ISession.RetVal.RET_FAIL;
    }

    @Override
    public RetVal Send(JSONObject profiles) {

        try{
            //HttpURLConnection con = ConnectServer("content-type", "application/x-www-form-urlencoded");
            HttpURLConnection con = ConnectServer("Content-Type", "application/json");

            // Send data
            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
            Log.d(TAG, "json:" + profiles.toString());
            osw.write(profiles.toString());
            osw.flush();
            osw.close();

            // Receive data
            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String line;
            String result = new String();
            while((line = rd.readLine()) != null){
                Log.d(TAG, "return : " + line);
                result += line;
            }
            return ConvertResult(result);
	        /*
        	try {
	        	JSONParser jsonParser = new JSONParser();
	        	JSONArray jsonArray = (JSONArray) jsonParser.parse(result);
	        	Log.d(TAG, "return : "+jsonArray.get(0));
        	} catch (ParseException e) {
    			e.printStackTrace();
    		}
    		*/

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ISession.RetVal.RET_FAIL;
    }
}
