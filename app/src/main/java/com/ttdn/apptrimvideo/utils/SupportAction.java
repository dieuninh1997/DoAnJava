package com.ttdn.apptrimvideo.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import com.ttdn.apptrimvideo.R;

public class SupportAction {
    private Context context;

    public SupportAction(Context context) {
        this.context = context;
    }

    public void shareApplication(String linkApp) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");
        intent.putExtra("android.intent.extra.SUBJECT", context.getResources()
                .getString(R.string.app_name));
        intent.putExtra("android.intent.extra.TEXT", linkApp);
        context.startActivity(Intent.createChooser(intent, "Share App Via"));
    }


    public void sendFeedBack() {
        String[] TO = {"ninhchan190697@gmail.com"};
        Intent intentEmail = new Intent(Intent.ACTION_SEND);
        intentEmail.setData(Uri.parse("mailto:"));
        intentEmail.setType("message/rfc822");

        String subject = "Your subject to "
                + context.getResources().getString(R.string.app_name)
                + " V_"
                + getVersionName(context)
                + " ("
                + Build.MANUFACTURER + " "
                + Build.DEVICE + " AV "
                + Build.VERSION.RELEASE + ")";
        String feedbackContent = "Please describe your problem as detail as possible, and we will try to fix it as fast as we can. Thank you for your feedback!";
        intentEmail.putExtra(Intent.EXTRA_EMAIL, TO);
        intentEmail.putExtra(Intent.EXTRA_SUBJECT, subject);
        intentEmail.putExtra(Intent.EXTRA_TEXT, feedbackContent);

        try {
            context.startActivity(Intent.createChooser(intentEmail,"Send FeedBack..."));

        }catch (Exception e){
            Toast.makeText(context, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
}
