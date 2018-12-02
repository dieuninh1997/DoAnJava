package com.ttdn.apptrimvideo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;

public class AppSettings {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    //Video settings
    public final static String VIDEO_SIZE = "VIDEO_SIZE";
    public final static String VIDEO_ORIENTATION = "VIDEO_ORIENTATION";
    public final static String VIDEO_BIT_RATE = "VIDEO_BIT_RATE";
    public final static String VIDEO_FRAME_RATE = "VIDEO_FRAME_RATE";
    public final static String VIDEO_DIRECTORY = "VIDEO_DIRECTORY";
    public final static String RECORD_SOUND = "RECORD_SOUND";
    public final static String QUALITY_SOUND = "QUALITY_SOUND";
    public final static String SHOW_NOTIFICATION = "SHOW_NOTIFICATION";



    private static final String FILE_NAME_FORMAT = "FILE_NAME_FORMAT";


    public final static String pathDefault =
            Environment.getExternalStorageDirectory()
                    + File.separator + "AppTrimVideo";

    public AppSettings(Context context) {
        preferences = context.getSharedPreferences("app_trim_video",
                Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    //Video size
    public void setVideoSize(String size) {
        editor.putString(VIDEO_SIZE, size);
        editor.commit();
    }

    public String getVideoSize() {
        return preferences.getString(VIDEO_SIZE, "1280x720");//default HD
    }


    //Video directory
    public void setVideoDirectory(String path) {
        editor.putString(VIDEO_DIRECTORY, path);
        editor.commit();
    }

    public String getVideoDirectory() {
        return preferences.getString(VIDEO_DIRECTORY, pathDefault);
    }


    //Video orientation
    public void setVideoOrientation(int orientation) {
        editor.putInt(VIDEO_ORIENTATION, orientation);
        editor.commit();
    }

    public int getVideoOrientation() {
        return preferences.getInt(VIDEO_ORIENTATION, 0);//0Auto - 1Portrait-2Landscape
    }


    //video bitrate
    public void setVideoBitRate(int videoBitRate) {
        editor.putInt(VIDEO_BIT_RATE, videoBitRate);
        editor.commit();
    }

    public int getVideoBitRate() {
        return preferences.getInt(VIDEO_BIT_RATE, 8);//Default 6Mbp
    }


    //video frame rate
    public void setVideoFrameRate(int videoFrameRate) {
        editor.putInt(VIDEO_FRAME_RATE, videoFrameRate);
        editor.commit();
    }
    public int getVideoFrameRate() {
        return preferences.getInt(VIDEO_FRAME_RATE, 30);//Default 30FPS
    }


    // record sound
    public void setRecordSound(boolean hasSound) {
        editor.putBoolean(RECORD_SOUND, hasSound);
        editor.commit();
    }

    public boolean getRecordSound() {
        return preferences.getBoolean(RECORD_SOUND, false);
    }


    //quality sound
    public void setQualitySound(int bitrate) {
        editor.putInt(QUALITY_SOUND, bitrate);
        editor.commit();
    }

    public int getQualitySound() {
        return preferences.getInt(QUALITY_SOUND, 128);//128Kbps
    }


    //file name format
    public void setFileNameFormat(String format) {
        editor.putString(FILE_NAME_FORMAT, format);
        editor.apply();
    }

    public String getFileNameFormat() {
        return preferences.getString(FILE_NAME_FORMAT, "yyyy_MM_dd_HH_mm_ss");
    }

    public void setShowNotification(boolean isShow) {
        editor.putBoolean(SHOW_NOTIFICATION, isShow);
        editor.commit();
    }

    public boolean getShowNotification() {
        return preferences.getBoolean(SHOW_NOTIFICATION, true);
    }
}
