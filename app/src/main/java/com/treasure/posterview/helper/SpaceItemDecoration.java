package com.treasure.posterview.helper;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by treasure on 2017/11/11.
 */

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private static final String TAG = SpaceItemDecoration.class.getName();
    private int space;
    private int type;
    public static final int TYPE_MAIN = 1;
    public static final int TYPE_ALBUM = 2;

    public SpaceItemDecoration(int space, int type) {
        this.space = space;
        this.type = type;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int position = parent.getChildAdapterPosition(view); // item position
        switch (type) {
            case TYPE_MAIN:
                if (position == 0){
                    outRect.top = 0;
                }
                else if (position == 1 || position == 2){
                    outRect.top = space/2;
                }else {
                    outRect.top = -space/2;
                }
                outRect.left = 0;
                outRect.right = space;
                break;
            case TYPE_ALBUM:
                outRect.top = space;
                if (position % 3 == 0 || position % 3 == 1) {
                    outRect.left = 0;
                    outRect.right = space;
                } else if (position % 3 == 2) {
                    outRect.left = 0;
                    outRect.right = 0;
                }
                break;
            default:
                outRect.left = 0;
                outRect.right = space;
                break;
        }
    }
}
