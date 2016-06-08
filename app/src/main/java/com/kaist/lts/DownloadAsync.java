package com.kaist.lts;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by user on 2016-06-07.
 */
public class DownloadAsync extends AsyncTask<String, String, String> {
    static private final String DOWNLOAD_FOLDER_URL = "http://52.78.20.109/uploads/";
    static private final String DOWNLOAD_LOCAL_PATH = "/storage/emulated/0/Download/";
    private final String TAG = "[LTS][DownloadAsync]";
    private final int DEFAULT_BUFF_SIZE = 1024;

    private Context mContext;
    private ProgressDialog mDlg;

    public DownloadAsync(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {

        //Create progress dialog
        mDlg = new ProgressDialog(mContext);
        mDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDlg.setMessage("Start");
        mDlg.setCancelable(false);
        mDlg.show();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        int count;

        try {
            Thread.sleep(100);

            //Connect the web-server with vaild filepath
            URL url = new URL(DOWNLOAD_FOLDER_URL + params[0].toString());
            URLConnection con = url.openConnection();
            con.connect();

            //Measure the file length
            int fileSize = con.getContentLength();
            Log.d(TAG, "Download file size: " + fileSize);

            //Make the input&output stream
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(DOWNLOAD_LOCAL_PATH + params[0].toString());

            byte data[] = new byte[DEFAULT_BUFF_SIZE];

            //Update the downloading progress
            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress("Downloading...", Integer.toString((int) ((total * 100) / fileSize)));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            publishProgress("Complete", Integer.toString(100));

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*    @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }*/
    @Override
    protected void onProgressUpdate(String... progress) {

        if (progress[0].equals("Downloading...")) {
            mDlg.setMessage(progress[0]);
            mDlg.setProgress(Integer.parseInt(progress[1]));
        } else if (progress[0].equals("Finish")) {
            mDlg.setMax(Integer.parseInt(progress[1]));
        }
    }

    @Override
    protected void onPostExecute(String s) {
        if (mDlg != null) mDlg.dismiss();
        //super.onPostExecute(s);
    }
}
