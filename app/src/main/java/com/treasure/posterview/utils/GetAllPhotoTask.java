package com.treasure.posterview.utils;

import android.os.AsyncTask;

import com.treasure.posterview.bean.PhotoBean;
import com.treasure.posterview.helper.PhotoManager;

import java.util.List;

/**
 * Created by treasure on 2017/11/5.
 */

public class GetAllPhotoTask extends AsyncTask<PhotoManager, Integer, List<PhotoBean>> {
    private static final String TAG = "GetAllPhotoTask";

    @Override
    protected List<PhotoBean> doInBackground(PhotoManager... params) {
        return params[0].getAllPhoto();
    }
}
