package com.treasure.posterview.ui.views;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import com.treasure.posterview.helper.LayerFocusChange;
import com.treasure.posterview.helper.OnLayerSelectListener;
import com.treasure.posterview.utils.BitmapUtils;
import com.treasure.posterview.utils.LayerUtils;

/**
 * @author Martin
 *         created at 2016/8/6 0006 2:04
 */
public class Layer {

    private static final String Tag = "Layer";
    private Bitmap layer;//提供绘图的原图
    private Bitmap drawLayer;//绘制的图
    private Bitmap filterLayer;

    private Path layerPath;//layer实际的形状
    public RectF layerRectF;//layerPath外矩形

    private Paint layerPaint;
    private Paint framePaint;
    private static final int frameColor = Color.parseColor("#FF3E96");

    private PaintFlagsDrawFilter drawFilter;

    private float x, y, moveLayerCenterX, moveLayerCenterY;//新加移动中心点坐标，不再使用外边框中心点作为移动的中心点了  后两个参数修改后已弃用
    private int width, height;

    private int degree = 0;//=ayer在cover中的旋转角度，tip：这里的旋转角度以在cover标注的layer的中心点的旋转角度为基准
    private float scale = 1;//以在视图中的宽度计算出来的缩放比例。
    private float pre_scale = 1;//记录scale的初始值

    /**
     * 这些范围都必须在caculateDrawLayer之后计算，
     * 是以最优计算的出来的值为准
     */
    private static final float MAX_SCALE = 2;//缩放的最大值
    private static final float MIN_SCALE = 1;//缩放的最小值
    private static final float MAX_MOVE_EX = 0.1f;//平移最多预留的距离的比值

    public boolean isTouched = false;//点击选中判断
    private boolean isSelect = false;//点击选中判断
    private boolean isPreSelect = false;//预览选中的状态
    private boolean isCanDrawFrame = true;

    /**
     * 根据上方默认的比例计算出来的值
     */
    private float max_scale, min_scale;
    private float normalX, normalY;
    private int move_dis_width, move_dis_height;

    private boolean isFlip; //用于layer翻转的判断
    private float layerCenterX, layerCenterY;
    private float layerMoveDisX = 0, layerMoveDisY = 0;


    public Layer(Bitmap layer, int degree) {
        this.layer = layer;
        this.degree = degree;
    }

    /**
     * 标记坐标点
     *
     * @param x
     * @param y
     * @return
     */
    public Layer markPoint(float x, float y) {
        if (null == layerPath) {
            layerPath = new Path();
            layerPath.moveTo(x, y);
        } else {
            layerPath.lineTo(x, y);
        }
        return this;
    }

    /**
     * 根据参数 确定4个顶点坐标（偏移角度）
     */
    public Layer markPoint(int width, int height, int centerX, int centerY, int degree, float scale) {
        layerCenterX = (float) centerX * scale;
        layerCenterY = (float) centerY * scale;
        float width2 = width * scale;
        float height2 = height * scale;

        float[] srcPoint = new float[10];
        float[] dstPoint = new float[10];
        srcPoint[0] = layerCenterX - width2 / 2;
        srcPoint[1] = layerCenterY - height2 / 2;
        srcPoint[2] = layerCenterX + width2 / 2;
        srcPoint[3] = layerCenterY - height2 / 2;
        srcPoint[4] = layerCenterX + width2 / 2;
        srcPoint[5] = layerCenterY + height2 / 2;
        srcPoint[6] = layerCenterX - width2 / 2;
        srcPoint[7] = layerCenterY + height2 / 2;
        Matrix matrix = new Matrix();
        matrix.setRotate(degree, layerCenterX, layerCenterY);
        matrix.mapPoints(dstPoint, srcPoint);

        layerPath = new Path();
        layerPath.moveTo(dstPoint[0], dstPoint[1]);
        layerPath.lineTo(dstPoint[2], dstPoint[3]);
        layerPath.lineTo(dstPoint[4], dstPoint[5]);
        layerPath.lineTo(dstPoint[6], dstPoint[7]);
        return this;
    }

    public Layer build() {
        layerPath.close();
        width = layer.getWidth();
        height = layer.getHeight();

        layerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        layerPaint.setAntiAlias(true);
        layerPaint.setFilterBitmap(true);

        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setColor(frameColor);
        framePaint.setAntiAlias(true);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(3);

        drawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        return this;
    }

    public Layer build(Path path) {
        layerPath = path;

        width = layer.getWidth();
        height = layer.getHeight();

        layerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        layerPaint.setAntiAlias(true);
        layerPaint.setFilterBitmap(true);

        drawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        return this;
    }


    /**
     * 计算出 真实起点坐标，和结束坐标，可以确定出显示矩形框
     *
     * @param coverScale 遮盖层图片的缩放比例
     * @return
     */
    public void caculateDrawLayer(float coverScale) {
        if (layerRectF == null) {
            Matrix scaleMatrix = new Matrix();
            scaleMatrix.postScale(coverScale, coverScale);
            layerPath.transform(scaleMatrix);
            layerRectF = new RectF();
            layerPath.computeBounds(layerRectF, true);
            moveLayerCenterX = layerRectF.centerX();
            moveLayerCenterY = layerRectF.centerY();
        }
        // 计算最优缩放并更新Layer的坐标
        scale = LayerUtils.calculateFitScale(width, height, (int) layerRectF.width(), (int) layerRectF.height());
        pre_scale = scale;
        if (filterLayer != null) {
            drawLayer = BitmapUtils.scaleBitmap(filterLayer, scale, scale);
        } else {
            drawLayer = BitmapUtils.scaleBitmap(layer, scale, scale);
        }

        width = drawLayer.getWidth();
        height = drawLayer.getHeight();
        setLayerX(layerRectF.left - ((width - layerRectF.width()) / 2));
        setLayerY(layerRectF.top - ((height - layerRectF.height()) / 2));

        max_scale = MAX_SCALE * scale;
        min_scale = MIN_SCALE * scale;
        //计算预留的值
        normalX = x;
        normalY = y;
        move_dis_width = (int) (drawLayer.getWidth() * (1 - MAX_MOVE_EX));
        move_dis_height = (int) (drawLayer.getHeight() * (1 - MAX_MOVE_EX));

    }

    public void draw(Canvas canvas) {
        if (null != drawLayer && !drawLayer.isRecycled()) {
            int a = canvas.save(Canvas.ALL_SAVE_FLAG);

            if (isTouched && !isSelect && !isPreSelect) {
                layerPaint.setAlpha(125);//如果是点击状态 50%的透明度
            } else {
                canvas.clipPath(layerPath);
                layerPaint.setAlpha(255);
            }
            canvas.translate(x, y);
            //craves旋转，中心点选取在每个layer的中心
            if (layerRectF != null) {
                canvas.rotate(degree, layerRectF.width() * (scale / pre_scale) / 2, layerRectF.height() * (scale / pre_scale) / 2);
            }
            canvas.drawBitmap(drawLayer, 0, 0, layerPaint);
            canvas.setDrawFilter(drawFilter);
            canvas.restoreToCount(a);
            if (isInTouch || isSelect || isPreSelect) {
                if (isCanDrawFrame)
                    canvas.drawPath(layerPath, framePaint);
            }
        }
    }


    private boolean isInTouch = false;
    private boolean isMultTouch = false;

    private PointF firstPointF = new PointF();
    private PointF centerPointF = new PointF();
    private PointF secondPointF = new PointF();
    private float lastX, lastY;

    private float originalDis; // 初始的两个手指按下的触摸点的距离
    private float lastDegrees;
    private float lastScale;

    private long firstTime;

    private static final int MaxMenuPadding = 20;

    /**
     * 触摸事件处理
     *
     * @param event
     * @return
     */
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(0);
        float y = event.getY(0);
        float scale = getScale();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isTouched = isSelect = isPreSelect = false;
                focusChange.releaseFocus(this);
                if (onLayerSelectListener != null)
                    onLayerSelectListener.dismiss(this);
                if (isTouchInLayer(x, y)) {
                    lastDegrees = 0;
                    lastX = x;
                    lastY = y;
                    firstPointF.set(x, y);//默认取得位置是单指的位置
                    isInTouch = true;
                    firstTime = event.getEventTime();
//                    Log.i(Tag, "  firstTime getDownTime   " + firstTime);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN://双指操作
                float x1 = event.getX(1);
                float y1 = event.getY(1);
                if (isInTouch) {
                    isTouched = true;
                    focusChange.requseFocus(this);
                    isMultTouch = true;
                    originalDis = LayerUtils.distance(event);
                    lastScale = 1;
                    secondPointF.set(x1, y1);
                    centerPointF = LayerUtils.middle(event);
                    lastDegrees = LayerUtils.getDegrees(firstPointF, secondPointF, centerPointF);
                    //  Log.i(Tag, x1 + "  isMultTouch Layer   " + isMultTouch);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isInTouch) {
                    if (Math.abs(x - firstPointF.x) > 5 || Math.abs(y - firstPointF.y) > 5) {
                        isTouched = true;
                        focusChange.requseFocus(this);
                    }
                    if (isMultTouch) {
                        //做的旋转，缩放操作
                        secondPointF.set(event.getX(1), event.getY(1));
                        float newSpin = LayerUtils.getDegrees(firstPointF, secondPointF, centerPointF);
                        degree += (int) ((lastDegrees - newSpin) * 0.7f);
                        lastDegrees = newSpin;
                        Log.i(Tag, "  rotate     " + degree);
                        float thisScale = LayerUtils.distance(event) / originalDis;
                        scaleLayer(thisScale / lastScale, thisScale / lastScale, false);
                        lastScale = thisScale;

                    } else {//平移操作
                        moveLayer(event.getX(0) - lastX, event.getY(0) - lastY);
                        lastX = x;
                        lastY = y;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                focusChange.releaseFocus(this);
                if (isTouchInLayer(x, y)) {
                    if (event.getEventTime() - firstTime < 200) {
                        isSelect = true;
                        focusChange.requseFocus(this);
                        if (onLayerSelectListener != null)
                            onLayerSelectListener.onSelected(this);
                    } else {
                        isSelect = false;
                        if (onLayerSelectListener != null)
                            onLayerSelectListener.dismiss(this);
                    }
//                    Log.i(Tag, event.getEventTime() + "  isSelect getDown   " + isSelect);
                }
                //单指移动 越界回调 的逻辑处理： 手指不断移动中，如果越界，回调到上一状态
//                if (isInTouch) {
//                    float canMoveX = (drawLayer.getWidth() - layerRectF.width()) / 2;
//                    float canMoveY = (drawLayer.getHeight() - layerRectF.height()) / 2;
//                    float moveX = event.getX() - firstPointF.x;
//                    float moveY = event.getY() - firstPointF.y;
//                    layerMoveDisX += moveX;
//                    layerMoveDisY += moveY;
////                    if ((degree % 360 >= 0 && degree % 360 < 90)||(degree % 360 > 270 && degree % 360 <= 360)){
//                        if (Math.abs(layerMoveDisX) > canMoveX || Math.abs(layerMoveDisY) > canMoveY) {
//                            moveLayer(-moveX, -moveY);
//                            layerMoveDisX -= moveX;
//                            layerMoveDisY -= moveY;
//                        }
////                    }else{
////                        if (Math.abs(layerMoveDisX) > canMoveY || Math.abs(layerMoveDisY) > canMoveX) {
////                            moveLayer(-moveX, -moveY);
////                            layerMoveDisX -= moveX;
////                            layerMoveDisY -= moveY;
////                        }
////                    }
//                }
                isMultTouch = isTouched = isInTouch = false;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //双指伸缩 越界回调 的逻辑处理： 手指伸缩中，如果越界，回调到中心位置（可调整）
//                if (isInTouch) {
//                    float canMoveX = (drawLayer.getWidth() - layerRectF.width()) / 2;
//                    float canMoveY = (drawLayer.getHeight() - layerRectF.height()) / 2;
//                    if (Math.abs(layerMoveDisX) > canMoveX || Math.abs(layerMoveDisY) > canMoveY) {
//                        moveLayer(-layerMoveDisX, -layerMoveDisY);
//                        layerMoveDisX = 0;
//                        layerMoveDisY = 0;
//                    }
//                }
                isMultTouch = false;
                isInTouch = false;
                break;
        }
        return isInTouch;
    }

    public boolean isTouchInLayer(float x, float y) {
        return layerRectF.contains(x, y);
    }

    /**
     * 缩放图层   //加入第三个参数，主要是为了 解决 翻转和放缩的冲突
     *
     * @param toScaleX
     */
    public void scaleLayer(float toScaleX, float toScaleY, boolean isFlipOrNot) {
        float scaleX = 1;
        float scaleY = 1;
        if (isFlipOrNot) {
            scaleX = scale * toScaleX;
            scaleY = toScaleY * scale;
        } else {
            scale = scale * toScaleX;
        }
        if (Math.abs(scale) > max_scale)
            scale = max_scale;
        if (Math.abs(scale) < min_scale)
            scale = min_scale;
        Bitmap scaleLayer = null;
        if (isFlipOrNot) {
            if (filterLayer != null) {
                scaleLayer = BitmapUtils.scaleBitmap(filterLayer, scaleX, scaleY);
            } else {
                scaleLayer = BitmapUtils.scaleBitmap(layer, scaleX, scaleY);
            }
        } else {
            if (filterLayer != null) {
                if (isFlip) {
                    scaleLayer = BitmapUtils.scaleBitmap(filterLayer, -scale, scale);
                } else {
                    scaleLayer = BitmapUtils.scaleBitmap(filterLayer, scale, scale);
                }
            } else {
                if (isFlip) {
                    scaleLayer = BitmapUtils.scaleBitmap(layer, -scale, scale);
                } else {
                    scaleLayer = BitmapUtils.scaleBitmap(layer, scale, scale);
                }
            }
        }
        BitmapUtils.destroyBitmap(drawLayer);
        drawLayer = scaleLayer;
        // 更新Layer的坐标
        setLayerX(x - (drawLayer.getWidth() - width) / 2);
        setLayerY(y - (drawLayer.getHeight() - height) / 2);
        width = drawLayer.getWidth();
        height = drawLayer.getHeight();
//        Log.e("~~~~~~~", "x:" + x + "y:" + y);
    }

    protected RectF scaleRect(float scale, RectF layerRectF) {
        return new RectF(layerRectF.left * 1.0f * scale, layerRectF.top * 1.0f * scale, layerRectF.right * 1.0f * scale, layerRectF.bottom * 1.0f * scale);
    }

    /**
     * 移动图层
     *
     * @param disX
     * @param disY
     */
    protected void moveLayer(float disX, float disY) {
        x += disX;
        y += disY;

        moveLayerCenterX += disX;
        moveLayerCenterY += disY;

        if (x >= move_dis_width + normalX)
            x = move_dis_width + normalX;
        if (x <= normalX - move_dis_width)
            x = normalX - move_dis_width;

        if (y >= move_dis_height + normalY)
            y = move_dis_height + normalY;
        if (y <= normalY - move_dis_height)
            y = normalY - move_dis_height;

    }

    protected void setLayerX(float x) {
        this.x = x;
    }

    protected void setLayerY(float y) {
        this.y = y;

    }

    public float getLayerX() {
        return x;
    }

    public float getLayerY() {
        return y;
    }

    /**
     * 计算出菜单弹出的最优点
     *
     * @return
     */
    public MenuPoint getFrontMenuPoint(int height, int menuHeight) {
        PointF point = new PointF();
        MenuPoint menuPoint = null;
        if (height - MaxMenuPadding < layerRectF.bottom + menuHeight) {
            point.set(layerRectF.left, layerRectF.top);
            menuPoint = new MenuPoint(1, point);
        } else {
            point.set(layerRectF.left, layerRectF.bottom);
            menuPoint = new MenuPoint(0, point);
        }
        return menuPoint;
    }

    /**
     * 定义菜单坐标对象，
     * direction对应方向，0代表上，1代表下
     */
    public static class MenuPoint {
        int direction;
        PointF point;

        public MenuPoint(int direction, PointF point) {
            this.direction = direction;
            this.point = point;
        }
    }

    public void resetLayer(Bitmap layer, Bitmap filterLayer) {
        this.layer = layer;
        this.filterLayer = filterLayer;
        width = layer.getWidth();
        height = layer.getHeight();
        isPreSelect = isMultTouch = isInTouch = isTouched = isSelect = false;
    }

    /**
     * 设置滤镜图片
     *
     * @param filterLayer
     */
    public void setFilterLayer(Bitmap filterLayer) {
        this.filterLayer = filterLayer;
        //因为当前绘制的图片使用的是原图（或者滤镜图）缩放过后的图，所以当有滤镜图存在的同时需要将滤镜图进行缩放
        scaleLayer(1, 1, false);
    }

    /**
     * 去除滤镜图
     */
    public void clearFilter() {
        BitmapUtils.destroyBitmap(filterLayer);//方法里有致空操作
        filterLayer = null;
        scaleLayer(1, 1, false);
    }


    /**
     * 清除layer内存
     */
    public void destroyLayer() {
        BitmapUtils.destroyBitmap(layer);
        BitmapUtils.destroyBitmap(drawLayer);
        BitmapUtils.destroyBitmap(filterLayer);
    }

    public boolean isCanDrawFrame() {
        return isCanDrawFrame;
    }

    public void setCanDrawFrame(boolean canDrawFrame) {
        isCanDrawFrame = canDrawFrame;
    }

    public boolean isPreSelect() {
        return isPreSelect;
    }

    public void setPreSelect(boolean preSelect) {
        isPreSelect = preSelect;
        if (isPreSelect) {
            focusChange.preSelect(this);
        } else {
            focusChange.releasePreSelect(this);
        }
    }

    public Bitmap getLayer() {
        return layer;
    }

    public Bitmap getFilterLayer() {
        return filterLayer;
    }

    public void setLayer(Bitmap layer) {
        this.layer = layer;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    OnLayerSelectListener onLayerSelectListener;

    public void setOnLayerSelectListener(OnLayerSelectListener onLayerSelectListener) {
        this.onLayerSelectListener = onLayerSelectListener;
    }

    private LayerFocusChange focusChange;

    public void setLayerFocusChange(LayerFocusChange focusChange) {
        this.focusChange = focusChange;
    }

    public void releaseAllFocus() {
        isPreSelect = isMultTouch = isInTouch = isTouched = isSelect = false;
        onLayerSelectListener.dismiss(this);
        focusChange.releaseFocus(this);
    }

    public void setFlip() {
        if (!isFlip) {
            scaleLayer(-1, 1, true);
            isFlip = true;
        } else {
            scaleLayer(1, 1, true);
            isFlip = false;
        }
    }
}
