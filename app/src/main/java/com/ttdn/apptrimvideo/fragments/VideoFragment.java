package com.ttdn.apptrimvideo.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ttdn.apptrimvideo.R;
import com.ttdn.apptrimvideo.adapter.ListVideoAdapter;
import com.ttdn.apptrimvideo.database.DBHelper;
import com.ttdn.apptrimvideo.database.VideoItem;
import com.ttdn.apptrimvideo.utils.Constant;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class VideoFragment extends Fragment {

    private static final int REQUEST_OVERLAY_PERMISSION = 10;
    private static final String TAG = "VideoFragment";

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    @Bind(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    private List<VideoItem> listVideosData;
    private LoadVideoHandler loadVideoHandler;

    private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.REFRESH_LIST_VIDEO)) {
                setRecyclerView();
            }
        }
    };

    private void setRecyclerView() {
        cancelTask();
        loadVideoHandler = new LoadVideoHandler();
        loadVideoHandler.execute();
    }

    private void cancelTask() {
        if (loadVideoHandler != null && !loadVideoHandler.isCancelled()) {
            loadVideoHandler.cancel(true);
        }
    }

    public VideoFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configRecyclerView();
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        swipeRefresh.setOnRefreshListener(() -> {
            setRecyclerView();
        });

        setRecyclerView();
    }

    private void configRecyclerView() {
//        GridLayoutManager layoutManager = new GridLayoutManager(getActivity().getApplication(), 1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplication());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelTask();
    }

    @Override
    public void onDetach() {
//        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(updateFabReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(refreshReceiver);
        super.onDetach();

    }

    private class LoadVideoHandler extends AsyncTask<Void, Void, Void> {

        private boolean checkFileExist(Uri uri) {
            Cursor cursor = MediaStore.Video.query(getActivity().getContentResolver(), uri, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            return false;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (listVideosData == null) {
                listVideosData = new ArrayList<>();
            } else {
                listVideosData.clear();
            }
            swipeRefresh.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefresh.setRefreshing(true);
                }
            });
        }

        @Override
        protected Void doInBackground(Void... params) {
            DBHelper dbHelper = new DBHelper(getActivity().getApplication());
            dbHelper.open();
            Cursor cursor = dbHelper.getAllVideoLink();
            if (cursor != null && cursor.getCount() > 0) {
                int videoLinkColumn = cursor.getColumnIndex(DBHelper.VIDEO_LINK);

                Log.d(TAG, cursor.getCount()+" videoLinkCol="+videoLinkColumn);


                String filePath;
                while (cursor.moveToNext()) {
                    filePath = cursor.getString(videoLinkColumn);

                    Log.d(TAG, "filePath="+filePath);

                    if (checkFileExist(Uri.parse(filePath))) {
                        Log.d(TAG, "doInBackground: file exists");
                        VideoItem videoItem = new VideoItem();
                        videoItem.videoLink = filePath;
                        listVideosData.add(videoItem);
                    } else {
//                        if (dbHelper.deleteVideoLink(filePath) != 0) {
//                            Log.i(TAG, "Deleted empty link");
//                        }
                    }
                }
                cursor.close();
                Log.d(TAG, "listVideosData"+listVideosData);
            }
            dbHelper.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "onPostExecute --> listVideosData"+listVideosData);
            ListVideoAdapter adapter = new ListVideoAdapter(getActivity(), listVideosData);
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);

            swipeRefresh.post(()->{
                    swipeRefresh.setRefreshing(false);
            });
        }
    }
}
