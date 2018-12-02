package com.ttdn.apptrimvideo.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ttdn.apptrimvideo.R;
import com.ttdn.apptrimvideo.utils.SupportAction;

import butterknife.Bind;
import butterknife.ButterKnife;


public class SupportFragment extends Fragment {
    @Bind(R.id.txtAppName)
    TextView txtAppName;

    @Bind(R.id.btnFeedback)
    TextView btnFeedback;

    @Bind(R.id.btnShareApp)
    TextView btnShareApp;

    public SupportFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_support, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txtAppName.setText("Video Trimmer" + " Version " + SupportAction.getVersionName(getContext()));
        btnFeedback.setOnClickListener(onClickListener);
        btnShareApp.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            SupportAction supportAction = new SupportAction(getActivity());
            if (id == btnFeedback.getId()) {
                supportAction.sendFeedBack();
            }else if(id==btnShareApp.getId()){
                String link="https://www.facebook.com/";
                supportAction.shareApplication(link);
            }
        }
    };
}
