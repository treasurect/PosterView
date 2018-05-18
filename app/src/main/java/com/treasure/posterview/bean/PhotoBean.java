package com.treasure.posterview.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by treasure on 2017/11/5.
 */

public class PhotoBean implements Parcelable {
    private String path;
    private long dataAdded;
    private long dataModified;

    public PhotoBean(String path, long dataAdded, long dataModified) {
        this.path = path;
        this.dataAdded = dataAdded;
        this.dataModified = dataModified;
    }

    protected PhotoBean(Parcel in) {
        path = in.readString();
        dataAdded = in.readLong();
        dataModified = in.readLong();
    }

    public static final Creator<PhotoBean> CREATOR = new Creator<PhotoBean>() {
        @Override
        public PhotoBean createFromParcel(Parcel in) {
            return new PhotoBean(in);
        }

        @Override
        public PhotoBean[] newArray(int size) {
            return new PhotoBean[size];
        }
    };

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDataAdded() {
        return dataAdded;
    }

    public void setDataAdded(long dataAdded) {
        this.dataAdded = dataAdded;
    }

    public long getDataModified() {
        return dataModified;
    }

    public void setDataModified(long dataModified) {
        this.dataModified = dataModified;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(path);
        dest.writeLong(dataAdded);
        dest.writeLong(dataModified);
    }
}

