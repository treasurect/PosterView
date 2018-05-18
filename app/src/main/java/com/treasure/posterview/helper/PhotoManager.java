package com.treasure.posterview.helper;

/**
 * Created by treasure on 2017/11/5.
 */

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;


import com.treasure.posterview.bean.PhotoBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Photo manager
 * Created by Flying SnowBean on 2015/11/19.
 */
public class PhotoManager {
    private final String TAG = PhotoManager.class.getSimpleName();
    private ContentResolver mContentResolver;
    private Context mContext;
    private List<String> mBucketIds;

    public PhotoManager(Context context) {
        this.mContext = context;
        mContentResolver = context.getContentResolver();
        mBucketIds = new ArrayList<>();
    }

//    public List<AlbumBean> getAlbum() {
//        mBucketIds.clear();
//
//        List<AlbumBean> data = new ArrayList<>();
//        String projects[] = new String[]{
//                MediaStore.Images.Media.BUCKET_ID,
//                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
//        };
//        Cursor cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                , projects
//                , null
//                , null
//                , MediaStore.Images.Media.DATE_MODIFIED);
//
//        if (cursor != null && cursor.moveToFirst()) {
//            do {
//                AlbumBean albumBean = new AlbumBean();
//
//                String buckedId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
//
//                if (mBucketIds.contains(buckedId)) continue;
//
//                mBucketIds.add(buckedId);
//
//                String buckedName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
//                String coverPath = getFrontCoverData(buckedId);
//
//                albumBean.setId(buckedId);
//                albumBean.setName(buckedName);
//                albumBean.setCoverPath(coverPath);
//
//                data.add(albumBean);
//
//
//            } while (cursor.moveToNext());
//
//            cursor.close();
//        }
//
//        return data;
//
//    }

    public List<PhotoBean> getPhoto(String buckedId) {
        List<PhotoBean> photos = new ArrayList<>();

        Cursor cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                , new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.DATE_MODIFIED}
                , MediaStore.Images.Media.BUCKET_ID + "=?"
                , new String[]{buckedId}
                , MediaStore.Images.Media.DATE_MODIFIED);
        if (cursor != null && cursor.moveToFirst()) {
            do {

                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                Long dataAdded = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                Long dataModified = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));


                PhotoBean photo = new PhotoBean(path,dataAdded,dataModified);

                photos.add(photo);

            } while (cursor.moveToNext());
            cursor.close();
        }

        return photos;
    }


    private String getFrontCoverData(String bucketId) {
        String path = "empty";
        Cursor cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media.DATA}, MediaStore.Images.Media.BUCKET_ID + "=?", new String[]{bucketId}, MediaStore.Images.Media.DATE_MODIFIED);
        if (cursor != null && cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }
        return path;
    }


    public List<PhotoBean> getAllPhoto() {
        List<PhotoBean> photos = new ArrayList<>();

        Cursor cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                , new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.DATE_MODIFIED}
                , null
                , null
                , MediaStore.Images.Media.DATE_MODIFIED);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                Long dataAdded = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                Long dataModified = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));

                //过滤掉gif图    原因：gif在裁剪出问题
                if (path.toUpperCase().endsWith(".gif") || path.toUpperCase().endsWith(".GIF"))
                    continue;
                PhotoBean photo = new PhotoBean(path, dataAdded, dataModified);

                photos.add(photo);

            } while (cursor.moveToNext());
            cursor.close();
        }

        Collections.sort(photos, new Comparator<PhotoBean>() {
            @Override
            public int compare(PhotoBean lhs, PhotoBean rhs) {
                long l = lhs.getDataModified();
                long r = rhs.getDataModified();
                return  l > r ? -1 : (l == r ? 0 : 1);
            }
        });

        return photos;
    }


}
