package com.treasure.posterview.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by treasure on 2017/11/3.
 */

public class ScaleImageView extends ImageView {
    private int initWidth = 0;
    private int initHeight = 0;
    private int type; //  宽高比 type=1: 1:1  type = 2: 1:1.4    type = 3: 1:0.7  type = 4: other

    public ScaleImageView(Context context) {
        this(context, null);
    }

    public ScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setInitSize(int initWidth, int type) {
        this.initWidth = initWidth;
        this.type = type;
    }

    public void setInitSize(int initWidth, int initHeight,int type) {
        this.initWidth = initWidth;
        this.initHeight = initHeight;
        this.type = type;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (initWidth > 0) {
            switch (type) {
                case 1:
                    setMeasuredDimension(initWidth, initWidth);
                    break;
                case 2:
                    setMeasuredDimension(initWidth, (int) (initWidth * 1.4f));
                    break;
                case 3:
                    setMeasuredDimension(initWidth, (int) (initWidth * 0.7f));
                    break;
                case 4:
                    setMeasuredDimension(initWidth,initHeight);
                    break;
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
