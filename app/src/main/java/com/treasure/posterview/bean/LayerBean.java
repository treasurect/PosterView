package com.treasure.posterview.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by treasure on 2017/11/14.
 */

public class LayerBean implements Parcelable {
    private int width;
    private int height;
    private int centerX;
    private int centerY;
    private int degree;

    public LayerBean(int width, int height, int centerX, int centerY, int degree) {
        this.width = width;
        this.height = height;
        this.centerX = centerX;
        this.centerY = centerY;
        this.degree = degree;
    }

    protected LayerBean(Parcel in) {
        width = in.readInt();
        height = in.readInt();
        centerX = in.readInt();
        centerY = in.readInt();
        degree = in.readInt();
    }

    public static final Creator<LayerBean> CREATOR = new Creator<LayerBean>() {
        @Override
        public LayerBean createFromParcel(Parcel in) {
            return new LayerBean(in);
        }

        @Override
        public LayerBean[] newArray(int size) {
            return new LayerBean[size];
        }
    };

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeInt(centerX);
        dest.writeInt(centerY);
        dest.writeInt(degree);
    }
}
