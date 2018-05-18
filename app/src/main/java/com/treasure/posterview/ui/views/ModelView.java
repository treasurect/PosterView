package com.treasure.posterview.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.treasure.posterview.helper.OnLayerSelectListener;
import com.treasure.posterview.bean.PosterLayerBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin on 2016/7/23 0023.
 */
public class ModelView extends View {

    private Model model;
    private boolean isFirstDraw = true;
    private float viewRatio = 1080 * 1.0f / 720;
    private Bitmap result;
    private List<PosterLayerBean> layer_list;
    private boolean flag;
    private float modelScale;

    public ModelView(Context context) {
        super(context);
        layer_list = new ArrayList<>();
    }

    public ModelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        layer_list = new ArrayList<>();
    }

    public void setModel(Model model) {
        this.model = model;
        this.model.bindView(this);
        invalidate();
    }

    public Model getModel() {
        return model;
    }

//    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        if (viewRatio != 0) {
//            setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
//            int childWidthSize = getMeasuredWidth();
//            int childHeightSize = getMeasuredHeight();
//            int scale = childHeightSize / widthMeasureSpec;
//            if (viewRatio > scale) {
//                widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
//                heightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeightSize, MeasureSpec.EXACTLY);
//            } else {
//                widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
//                heightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeightSize, MeasureSpec.EXACTLY);
//            }
//            //按比例修改宽高
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        }
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null != model) {
            if (getWidth() != 0) {
                if (isFirstDraw) {
                    model.setDrawWidth(getWidth());
//                    LogUtil.d("~~~~~~~~~~~getWidth():~`"+getWidth());
                    isFirstDraw = false;
                }
                model.draw(canvas);
            }
        }
    }

    public void destoryLayers() {
        if (null != model) {
            model.destroyLayer();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!flag){
            if (null != model) {
                boolean b = model.onTouchEvent(event);
                invalidate();
                return b;
            }
        }

        return super.onTouchEvent(event);
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public Bitmap getResult() {
        if (model != null){
            model.releaseAllFocus();//去除所有焦点，并刷新视图
        }
        this.invalidate();
        result = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        this.draw(canvas);
        return result;
    }

    public void setOnLayerSelectListener(OnLayerSelectListener onLayerSelectListener) {
        if (null != model) {
            model.setOnLayerSelectListener(onLayerSelectListener);
        }
    }

    public List<PosterLayerBean> getLayer_list() {
        return layer_list;
    }

    public void setLayer_list(List<PosterLayerBean> layer_list) {
        this.layer_list = layer_list;
    }

    public void setModelScale(float modelScale) {
        this.modelScale = modelScale;
    }
    public void release() {
        if (model != null) {
            model.releaseAllFocus();//去除所有焦点，并刷新视图
        }
    }
}
