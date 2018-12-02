package com.ttdn.apptrimvideo.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ttdn.apptrimvideo.R;
import com.ttdn.apptrimvideo.SimpleDirectoryChooserActivity;
import com.ttdn.apptrimvideo.utils.AppSettings;
import com.ttdn.apptrimvideo.utils.Constant;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SettingsFragment extends Fragment {
    //Video settings

    //file name format
    @Bind(R.id.btnFileNameFormat)
    LinearLayout btnFileNameFormat;
    @Bind(R.id.txtFileNameFormat)
    TextView txtFileNameFormat;

    //choose directory
    @Bind(R.id.btnChooseDirectory)
    LinearLayout btnChooseDirectory;
    @Bind(R.id.txtDirectoryPath)
    TextView txtDirectoryPath;

    private AppSettings appSettings;
    private Resources resources;


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appSettings = new AppSettings(getActivity().getApplication());
        resources = getResources();

        setTextFileNameFormat();

        txtDirectoryPath.setText(appSettings.getVideoDirectory());
    }



    @Nullable
    @OnClick({
            R.id.btnFileNameFormat,
            R.id.btnChooseDirectory
    })
    public void onClick(View v) {
        int id = v.getId();
        if (id == btnChooseDirectory.getId()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    return;
                }
            }
            getContext().getExternalFilesDir(null);
            Intent chooserIntent = new Intent(getActivity(), SimpleDirectoryChooserActivity.class);
            chooserIntent.putExtra(SimpleDirectoryChooserActivity.INIT_DIRECTORY_EXTRA, appSettings.getVideoDirectory());
            startActivityForResult(chooserIntent, Constant.REQUEST_DIRECTORY);

        } else if (id == btnFileNameFormat.getId()) {
            showDialogFileNameFormat();
        }
    }

    private AlertDialog.Builder newBuilder() {
        return new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogAppCompatStyle);
    }

    private void showDialogFileNameFormat() {
        AlertDialog.Builder builder = newBuilder();
        final String arrays[] = resources.getStringArray(R.array.array_file_name_format);
        int position = 0;
        String fileNameFormat = appSettings.getFileNameFormat();
        for (int i = 0; i < arrays.length; i++) {
            if (arrays[i].equals(fileNameFormat)) {
                position = i;
                break;
            }
        }

        builder.setTitle(resources.getString(R.string.file_name_format))
                .setSingleChoiceItems(
                        arrays,
                        position,
                        (dialog, which)->{
                            appSettings.setFileNameFormat(arrays[which]);
                            setTextFileNameFormat();
                            dialog.dismiss();
                        })
                .setNegativeButton(resources.getString(R.string.cancel), null)
                .show();
    }

    private void setTextFileNameFormat() {
        txtFileNameFormat.setText(appSettings.getFileNameFormat());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Constant.REQUEST_DIRECTORY){
            if(requestCode==SimpleDirectoryChooserActivity.RESULT_CODE_DIRECTORY_SELECTED){
                String newPath = data.getStringExtra(SimpleDirectoryChooserActivity.RESULT_DIRECTORY_EXTRA);
                if (!newPath.equals(appSettings.getVideoDirectory())) {
                    appSettings.setVideoDirectory(newPath);
                    txtDirectoryPath.setText(appSettings.getVideoDirectory());
                }
            }
        }
    }
}
