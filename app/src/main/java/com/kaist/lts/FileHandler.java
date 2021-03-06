package com.kaist.lts;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import org.json.simple.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;

import static com.google.android.gms.internal.zzir.runOnUiThread;

/**
 * Created by user on 2016-05-30.
 */
public class FileHandler {
    private static final String TAG = "[LTS][FileHandler]";
    private static final String SERVER_UPLOAD_URL = "http://52.78.20.109/upload.php";
    private static PowerManager.WakeLock mWakeLock;

    public static String getPath(final Context context, final Uri uri) {

        // check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }

    public static void createUploadThread(Context context, final JSONObject req, String filePath, PowerManager.WakeLock wakeLock, final String fileName) {
        final Context c = context;
        final String fp = filePath;

        mWakeLock = wakeLock;

        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    //creating new thread to handle Http Operations
                    // DEBUG
                    // Toast.makeText(c, "Upload File", Toast.LENGTH_SHORT).show();
                    uploadFile(c, fp, req, fileName);
                } catch (OutOfMemoryError e) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(c, "Insufficient Memory!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }, 3000);
    }

    public static void downloadFile(Context context, final String fileName) {
        Log.d(TAG, "Download file: " + fileName);
        new DownloadAsync(context).execute(fileName);
    }

    private static int uploadFile(final Context c, final String path, final JSONObject req, final String fileName) {
        Log.d(TAG, "uploadFile: " + path + ", saved: " + fileName);
        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        int serverResponseCode = 0;
        int defaultBufferSize = 1048576; //1 * 1024 * 1024 = 1MB
        byte[] buffer;
        File selectedFile = new File(path);

        //String[] parts = path.split("/");
        //String fileName = parts[parts.length - 1];

        if (!selectedFile.isFile()) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(c, "Src File doesn't exist:" + path, Toast.LENGTH_SHORT).show();
                }
            });
            Log.e(TAG, selectedFile + "is invalid");
            sendMsgHandler(ClientActivity.ERROR);
            return 0;
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                //AccessManager am = AccessManager.getAccessManager();
                //if (am != null) {
                Log.d(TAG, "Try to connect server via open session");
                ISession s = Session.GetInstance();

                if (s != null) {
                    connection = s.ConnectServer(SERVER_UPLOAD_URL, null, null);
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    connection.setRequestProperty(
                            "Content-Type", "multipart/form-data;boundary=" + boundary);
                    //int userMode = ProfileManager.getUserMode(AccessManager.getAccessManager().GetSession());
                    //fileName = userMode + "_" + fileName;
                    //Log.d(TAG, "Rename of file: " + fileName);
                    connection.setRequestProperty("uploaded_file", fileName);
                } else {
                    Log.e(TAG, "Error to Connect to server");
                    sendMsgHandler(ClientActivity.ERROR);
                    return 0;
                }

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable, defaultBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0) {
                    try {
                        //write the bytes read from inputstream
                        dataOutputStream.write(buffer, 0, bufferSize);
                    } catch (OutOfMemoryError e) {
                        Log.e(TAG, "Memory Insufficient!");
                    }
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, defaultBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                try {
                    serverResponseCode = connection.getResponseCode();
                } catch (OutOfMemoryError e) {
                    Log.e(TAG, "Memory Insufficient!");
                }
                String serverResponseMessage = connection.getResponseMessage();

                Log.d(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                //response code of 200 indicates the server status OK
                if (serverResponseCode == 200) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sendMsgHandler(ClientActivity.UPLOADED);

                            if (req != null)
                                ClientActivity.updateNotifyMsg(req);
                        }
                    });
                } else {
                    sendMsgHandler(ClientActivity.ERROR);
                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();

                if (mWakeLock != null && mWakeLock.isHeld()) {
                    Log.d(TAG, "Release the held wakeLock");
                    mWakeLock.release();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(c, "File Not Found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(c, "URL Error!", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(c, "Cannot Read/Write File", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            return serverResponseCode;
        }
    }

    private static void sendMsgHandler(int msg) {
        if (ClientActivity.mHandler != null) {
            ClientActivity.mHandler.sendEmptyMessage(msg);
        }
    }
}

