package com.ttdn.apptrimvideo.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ttdn.apptrimvideo.database.DBHelper;

public class Utils {
    private Context context;
    public static final String TAG = "Utils";

    public Utils(Context context) {
        this.context = context;
    }


    public static void deleteVideo(final Context context, final String path) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                Uri uri = Uri.parse(path);
                int deleteRow = context.getContentResolver().delete(uri, null, null);
                if (deleteRow == 1) {
                    DBHelper dbHelper = new DBHelper(context);
                    dbHelper.open();
                    if (dbHelper.deleteVideoLink(path) != 0) {
                        Log.i(TAG, "delete video link success");
                    }
                    dbHelper.close();
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constant.REFRESH_LIST_VIDEO));
                }
                return null;
            }
        }.execute();
    }


    public static Intent createIntentShare(Context context, String path) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/mp4");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent createIntentPlay(Context context, String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path), "video/*");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static void scanVideoFile(final Context context, final String outputFile) {
        MediaScannerConnection.scanFile(context,
                new String[]{outputFile},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        DBHelper dbHelper = new DBHelper(context);
                        dbHelper.open();
                        ContentValues values = new ContentValues();
                        values.put(DBHelper.VIDEO_LINK, uri.toString());
                        if (dbHelper.insertVideoLink(values) != 0) {
                            Log.i("TrimVideoActivity", "Insert video OK");
                        }
                        dbHelper.close();
                        NotificationUtils.showVideoNotification(context, uri, outputFile);
                    }
                });
    }


}
