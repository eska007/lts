package com.kaist.lts;

import android.os.StrictMode;
import android.util.Log;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

/**
 * Created by user on 2016-05-19.
 */
public class Session implements ISession{
    final static String TAG = "[LTS][Session]";
    final static String MAIN_PAGE = "http://52.78.20.109/main2.php";

    private static ISession instance;
    String currentCookie;
    boolean isConnected = false;

    private Session() {

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        HttpURLConnection con;
        try {
            con = ConnectServer(MAIN_PAGE, null, null);
            // Get Cookie to keep the session
            if (con != null) {
                String newCookie = con.getHeaderField("Set-Cookie");
                if (newCookie != null) {
                    Log.d(TAG, "new cookie: " + newCookie);
                    currentCookie = newCookie;
                }
                isConnected = true;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.d(TAG, "Session cookie: " + currentCookie + ", connect: " + isConnected);
    }

    public static synchronized ISession GetInstance() { // Singleton
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    @Override
    public HttpURLConnection ConnectServer(String addr, String property_key, String property_val) throws IOException {
        URL url = new URL(addr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        if (con == null) {
            throw new IOException("HttpURLConnection.openConnection failed");
		}

        con.setDoOutput(true);
        if (currentCookie != null) {
            con.setRequestProperty("cookie", currentCookie);
            Log.d(TAG, "Reuse cookie:" + currentCookie);
        }
        con.setDefaultUseCaches(false);
        con.setDoInput(true);
        con.setRequestMethod("POST");
        if (property_key != null && property_val != null) {
            con.setRequestProperty(property_key, property_val);
        }
        return con;
    }

    private RetVal ConvertResult(String arg)
    {
        Log.d(TAG, "ConvertResult : " + arg);
        switch(arg) {
            case "0":	return ISession.RetVal.RET_OK;
            case "-1":	return ISession.RetVal.RET_FAIL;
            case "-2":	return ISession.RetVal.RET_NETWORK_ERROR;
            case "-3":	return ISession.RetVal.RET_DATABASE_ERROR;
            case "-4":	return ISession.RetVal.RET_DATABASE_DUPLICATED_ERROR;
            case "-5":	return ISession.RetVal.RET_PARAM_ERROR;
            default:
                break;
        }

        return ISession.RetVal.RET_FAIL;
    }

    @Override
    public RetVal Send(JSONObject data) {
        try{
            //HttpURLConnection con = ConnectServer("content-type", "application/x-www-form-urlencoded");
            HttpURLConnection con = ConnectServer(MAIN_PAGE, "Content-Type", "application/json");

            // Send data
            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
            Log.d(TAG, "json:" + data.toString());
            osw.write(data.toString());
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

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ISession.RetVal.RET_FAIL;
    }

    public RetVal SendRequest(JSONObject input, JSONObject output) {
        if (input == null || output == null) {
            return ISession.RetVal.RET_PARAM_ERROR;
        }

        String received_data = null;
        try{
            //HttpURLConnection con = ConnectServer("content-type", "application/x-www-form-urlencoded");
            HttpURLConnection con = ConnectServer(MAIN_PAGE, "Content-Type", "application/json");

            // Send data
            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
            System.out.println("json:" + input.toString());
            osw.write(input.toString());
            osw.flush();
            osw.close();

            // Receive data
            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String line;
            received_data = new String();
            while((line = rd.readLine()) != null){
                System.out.println("return1 : " + line);
                received_data += line;
            }

            // Convert received binary to JSON format
            JSONParser jsonParser = new JSONParser();
            JSONObject obj = (JSONObject) jsonParser.parse(received_data);
            //// deep copy the json data
            Iterator<Object> itr = obj.keySet().iterator();
            while(itr.hasNext()) {
                Object key = itr.next();
                Object val = obj.get(key);
                output.put(key, val);
            }

            Log.d(TAG, "return2 : " + output.toString());
            return ISession.RetVal.RET_OK;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            // If it's not in logged-in state, received_data may not be json format.
            e.printStackTrace();
            return ConvertResult(received_data);
        }
        return ISession.RetVal.RET_FAIL;
    }
}
