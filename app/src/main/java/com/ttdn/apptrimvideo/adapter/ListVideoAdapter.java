package com.ttdn.apptrimvideo.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ttdn.apptrimvideo.R;
import com.ttdn.apptrimvideo.TrimVideoActivity;
import com.ttdn.apptrimvideo.database.DBHelper;
import com.ttdn.apptrimvideo.database.VideoItem;
import com.ttdn.apptrimvideo.utils.Constant;
import com.ttdn.apptrimvideo.utils.Utils;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

public class ListVideoAdapter extends RecyclerView.Adapter<ListVideoAdapter.ViewHolder> {
    private Context context;
    private List<VideoItem> listVideosData;
    private DateFormat dateFormat = DateFormat.getDateTimeInstance();

    private String[] project = new String[]{
            MediaStore.Video.VideoColumns.DISPLAY_NAME,
            MediaStore.Video.VideoColumns.DURATION,
            MediaStore.Video.VideoColumns.RESOLUTION,
            MediaStore.Video.VideoColumns.SIZE
    };


    public ListVideoAdapter(Context ctx, List<VideoItem> data) {
        context = ctx;
        listVideosData = data;
        Log.d("VideoFragment","data="+data+" context="+ctx);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_video_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoItem item = listVideosData.get(position);
        Log.d("VideoFragment", "item="+item);
        getVideoInfo(holder, item.videoLink);
    }

    private void getVideoInfo(ViewHolder holder, String filePath) {
        Uri uri = Uri.parse(filePath);
        Cursor cursor = MediaStore.Video.query(context.getContentResolver(), uri, project);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                Glide.with(context).load(filePath).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.getVideoThumbnail());
                holder.getTxtRecordTime().setText(cursor.getString(cursor.getColumnIndex(project[0])));
                holder.getTxtVideoDuration().setText(convertDuration(cursor.getLong(cursor.getColumnIndex(project[1]))));
                holder.getTxtVideoResolution().setText(cursor.getString(cursor.getColumnIndex(project[2])));
                holder.getTxtVideoSize().setText(convertByteToMB(cursor.getLong(cursor.getColumnIndex(project[3]))));
            }
            Log.d("VideoFragment", "holder="+holder);
            cursor.close();
        } else {
            DBHelper dbHelper = new DBHelper(context);
            dbHelper.open();
            if (dbHelper.deleteVideoLink(filePath) != 0) {
                Log.i("VideoFragment", "Deleted empty link");
                listVideosData.remove(holder.getAdapterPosition());
            }
            dbHelper.close();
        }

    }

    private String convertByteToMB(long time) {
        return String.format(Locale.US, "%.2f", time / 1024.0f / 1024.0f) + "MB";
    }

    private String convertDuration(long duration) {//ms
        long hour, minute, second;
        second = duration / 1000;
        minute = second / 60;
        hour = minute / 60;
        minute = (second - hour * 3600) / 60;
        second = second - minute * 60 - hour * 3600;
        return String.format(Locale.US, "%02d:%02d:%02d", hour, minute, second);
    }

    @Override
    public int getItemCount() {
        return  listVideosData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private FrameLayout videoPreview;
        private ImageView videoThumbnail;
        private ImageButton btnShare;
        private ImageButton btnDelete;
        private ImageButton btnTrimVideo;
        private TextView txtRecordTime;
        private TextView txtVideoDuration;
        private TextView txtVideoResolution;
        private TextView txtVideoSize;

        public TextView getTxtRecordTime() {
            return txtRecordTime;
        }

        public TextView getTxtVideoDuration() {
            return txtVideoDuration;
        }

        public TextView getTxtVideoResolution() {
            return txtVideoResolution;
        }

        public TextView getTxtVideoSize() {
            return txtVideoSize;
        }

        public ImageView getVideoThumbnail() {
            return videoThumbnail;
        }

        public ViewHolder(View itemView) {
            super(itemView);

            videoThumbnail = (ImageView) itemView.findViewById(R.id.videoThumbnail);
            //videoPreview = (FrameLayout) itemView.findViewById(R.id.videoPreview);

            btnShare = (ImageButton) itemView.findViewById(R.id.btnShareVideo);
            btnDelete = (ImageButton) itemView.findViewById(R.id.btnDeleteVideo);
            btnTrimVideo = (ImageButton) itemView.findViewById(R.id.btnTrimVideo);
            txtRecordTime = (TextView) itemView.findViewById(R.id.txtRecordTime);
            txtVideoDuration = (TextView) itemView.findViewById(R.id.txtVideoDuration);
            txtVideoResolution = (TextView) itemView.findViewById(R.id.txtVideoResolution);
            txtVideoSize = (TextView) itemView.findViewById(R.id.txtVideoSize);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String videoLink = listVideosData.get(getAdapterPosition()).videoLink;
                    Intent intentPlay = Utils.createIntentPlay(context, videoLink);
                    context.startActivity(intentPlay);

                }
            });

            btnShare.setOnClickListener(onClickListener);
            btnDelete.setOnClickListener(onClickListener);
            btnTrimVideo.setOnClickListener(onClickListener);

        }

        private View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                final String videoLink = listVideosData.get(getAdapterPosition()).videoLink;
                if (id == btnShare.getId()) {
                    Intent intentPlay = Utils.createIntentShare(context, videoLink);
                    context.startActivity(intentPlay);
                } else if (id == btnDelete.getId()) {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(context,
                                    R.style.MyAlertDialogAppCompatStyle);
                    builder.setTitle(context.getResources().getString(R.string.delete_video))
                            .setMessage(context.getResources().getString(R.string.delete_message)
                                    + txtRecordTime.getText().toString() + "?")
                            .setPositiveButton(context.getResources().getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Utils.deleteVideo(context, videoLink);
                                        }
                                    }).setNegativeButton(context.getResources().getString(R.string.cancel), null);
                    builder.show();
                } else if (id == btnTrimVideo.getId()) {
                    Intent intent = new Intent(context, TrimVideoActivity.class);
                    intent.putExtra(Constant.TRIM_VIDEO_URI, videoLink);
                    context.startActivity(intent);
                }
            }
        };
    }
}
