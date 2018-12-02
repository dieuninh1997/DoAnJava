package com.ttdn.apptrimvideo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.ttdn.apptrimvideo.utils.AppSettings;
import com.ttdn.apptrimvideo.utils.Constant;
import com.ttdn.apptrimvideo.utils.TrimVideoUtils;
import com.ttdn.apptrimvideo.utils.Utils;
import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TrimVideoActivity extends BaseAppCompatActivity {

    @Bind(R.id.rangeSeekBar)
    RangeSeekBar<Integer> rangeSeekBar;

    @Bind(R.id.videoView)
    VideoView videoView;

    @Bind(R.id.txtStartTimeVideo)
    TextView txtStartTimeVideo;

    @Bind(R.id.txtEndTimeVideo)
    TextView txtEndTimeVideo;

    private Uri uri;
    private File fileSource = null;
    private File fileDes = null;
    private int startTime, endTime, duration;
    private AppSettings appSettings;
    private DateFormat fileFormat;
    private final int REQUEST_ADD_VIDEO = 1;
    private static final int REQUEST_OVERLAY_PERMISSION = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showArrowBack();
        setContentView(R.layout.activity_trim_video);

        ButterKnife.bind(this);

        appSettings = new AppSettings(getApplication());
        fileFormat = new SimpleDateFormat(appSettings.getFileNameFormat().concat("'_trim.mp4'"), Locale.US);

        getVideoUri();
        if (uri != null)
            initVideoView();
    }

    private void initVideoView() {
        fileSource = getFileSource(uri);
        if (fileSource != null) {
            File directory = new File(appSettings.getVideoDirectory());
            if (!directory.mkdirs()) {
                Log.i("TrimVideoActivity", "Can't create folder");
            } else {
                directory = Environment.getExternalStorageDirectory();
            }

            fileDes = new File(directory, fileFormat.format(new Date()));
        }
        videoView.setVideoURI(uri);


        rangeSeekBar.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                if (startTime != rangeSeekBar.getSelectedMinValue()) {
                    startTime = rangeSeekBar.getSelectedMinValue();

                    txtStartTimeVideo.setText(String.valueOf(convertDuration(startTime)));
                    videoView.seekTo(startTime * 1000);
                }
                if (endTime != rangeSeekBar.getSelectedMaxValue()) {
                    endTime = rangeSeekBar.getSelectedMaxValue();

                    txtEndTimeVideo.setText(String.valueOf(convertDuration(endTime)));
                    videoView.seekTo(endTime * 1000);
                }


            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                duration = videoView.getDuration();
                startTime = 0;
                endTime = duration / 1000;
                txtStartTimeVideo.setText(String.valueOf(convertDuration(startTime)));
                txtEndTimeVideo.setText(String.valueOf(convertDuration(endTime)));

                rangeSeekBar.setRangeValues(startTime, endTime);
                rangeSeekBar.setSelectedMinValue(startTime);
                rangeSeekBar.setSelectedMaxValue(endTime);

                videoView.seekTo(startTime);
            }
        });


        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    return false;
                } else {
                    videoView.start();
                    return false;
                }
            }
        });
    }

    private File getFileSource(Uri uri) {
        File file = null;
        Cursor cursor = MediaStore.Video.query(
                getContentResolver(),
                uri,
                new String[]{MediaStore.Video.VideoColumns.DATA});

        if (cursor != null) {
            if (cursor.moveToFirst())
                file = new File(cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA)));
        }
        return file;
    }

    private void getVideoUri() {
        Intent intent = getIntent();//
        String path = intent.getStringExtra(Constant.TRIM_VIDEO_URI);//
        if (!TextUtils.isEmpty(path)) {
            uri = Uri.parse(path);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trim_video, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_trim_video) {
            //permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(getApplicationContext())) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getApplicationContext().getPackageName()));
                    startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
                }
            }

            //trim
            if (fileSource != null && fileDes != null) {
                new AsyncTask<Void, Void, Void>() {
                    ProgressDialog progDailog = new ProgressDialog(TrimVideoActivity.this);

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progDailog.setMessage("Trimming...");
                        progDailog.setIndeterminate(false);
                        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progDailog.setCancelable(true);
                        progDailog.show();
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        TrimVideoUtils.startTrimVideo(
                                fileSource,
                                fileDes,
                                startTime,
                                endTime);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        if (fileDes.exists()) {
                            Utils.scanVideoFile(getApplication(), fileDes.getAbsolutePath());
                        }

                        progDailog.dismiss();
                        Toast.makeText(getApplicationContext(), "Trim video finish", Toast.LENGTH_SHORT).show();
                    }
                }.execute();
            }
        } else if (id == R.id.action_add_video) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.setType("video/mp4");
            startActivityForResult(intent, REQUEST_ADD_VIDEO);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_VIDEO) {
            if (resultCode == RESULT_OK) {
                uri = data.getData();
                if (uri != null) {
                    initVideoView();

                }
            }
        }
    }


    private String convertDuration(int duration) {//ms
        int hour, minute, second;
        second = duration;
        minute = second / 60;
        hour = minute / 60;
        minute = (second - hour * 3600) / 60;
        second = second - minute * 60 - hour * 3600;
        if (hour > 0)
            return String.format(Locale.US, "%02d:%02d:%02d", hour, minute, second);
        return String.format(Locale.US, "%02d:%02d", minute, second);
    }

}
