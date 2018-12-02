package com.ttdn.apptrimvideo.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bumptech.glide.load.engine.Resource;
import com.ttdn.apptrimvideo.R;
import com.ttdn.apptrimvideo.TrimVideoActivity;
import com.ttdn.apptrimvideo.receiver.SupportVideoReceiver;

import java.io.File;

public class NotificationUtils {
    public static void showVideoNotification(Context context, Uri uri, String filePath) {
        Log.d("TrimVideoActivity","showVideoNotification");
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constant.REFRESH_LIST_VIDEO));
        Intent intent = Utils.createIntentPlay(context, uri.toString());
        //play video intent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                1,
                intent,
                PendingIntent.FLAG_ONE_SHOT);
        Bitmap videoThumbnail = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);
        if (videoThumbnail == null) {
            videoThumbnail = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_play_circle_fill_24dp);
        }

        //share intent
        Intent intentShareVideo = new Intent(context, SupportVideoReceiver.class);
        intentShareVideo.setAction(Constant.SHARE_VIDEO_ACTION);
        intentShareVideo.setData(uri);

        PendingIntent sharePendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                intentShareVideo,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //delete intent
        Intent intentDeleteVideo = new Intent(context, SupportVideoReceiver.class);
        intentDeleteVideo.setAction(Constant.DELETE_VIDEO_ACTION);
        intentDeleteVideo.setData(uri);

        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                intentDeleteVideo,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //Trim Video
        Intent intentTrimVideo = new Intent(context, TrimVideoActivity.class);

        intentTrimVideo.putExtra(Constant.TRIM_VIDEO_URI, uri.toString());
        PendingIntent pendingTrimVideo = PendingIntent.getActivity(
                context,
                1,
                intentTrimVideo,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //
        NotificationManager notificationManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = Constant.NOTIFICATION_VIDEO_ID;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }


        //notify
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        builder.setContentTitle("Screen recording captures")
                .setContentText("Click to open "+ filePath.substring(filePath.lastIndexOf(File.separator)+1))
                .setSmallIcon(R.drawable.ic_video_camera)
                .setLargeIcon(Bitmap.createScaledBitmap(videoThumbnail, videoThumbnail.getWidth(), videoThumbnail.getHeight(), true))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[0])
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setTicker("Video in here")
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(videoThumbnail))
                .addAction(R.drawable.ic_menu_share, "Share", sharePendingIntent)
                .addAction(R.drawable.ic_content_cut_black_24dp, "Trim", pendingTrimVideo)
                .addAction(R.drawable.ic_delete_24dp, "Delete", deletePendingIntent);

        Notification notification = builder.build();

        notificationManager.cancel(notificationId);
        notificationManager.notify(notificationId, notification);

    }

}
