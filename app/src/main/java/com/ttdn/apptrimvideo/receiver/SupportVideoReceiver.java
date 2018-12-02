package com.ttdn.apptrimvideo.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.ttdn.apptrimvideo.utils.Constant;
import com.ttdn.apptrimvideo.utils.Utils;

public class SupportVideoReceiver extends BroadcastReceiver {

    public static final String TAG = "apptrimvideo";

    @Override
    public void onReceive(Context context, Intent intent) {
        Uri uri = intent.getData();
        String action = intent.getAction();
        switch (action){
            case Constant.SHARE_VIDEO_ACTION:
                context.startActivity(Utils.createIntentShare(context,uri.toString()));
                break;
            case Constant.DELETE_VIDEO_ACTION:
                Utils.deleteVideo(context,uri.toString());
                cancelNotification(context, Constant.NOTIFICATION_VIDEO_ID);
                break;
        }

    }

    private void cancelNotification(Context context, int notificationVideoId) {
        NotificationManager manager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationVideoId);
    }
}
