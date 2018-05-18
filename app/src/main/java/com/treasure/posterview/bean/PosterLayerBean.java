package com.treasure.posterview.bean;

import java.io.Serializable;

/**
 * Created by treasure on 2017/11/2.
 */

public class PosterLayerBean implements Serializable {
    private float x;
    private float y;
    private float scale;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}
