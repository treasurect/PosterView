package com.treasure.posterview.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;
import com.treasure.posterview.R;
import com.treasure.posterview.utils.Tools;

import butterknife.BindView;
import butterknife.OnClick;

public class ShareActivity extends BaseActivity {

    public static void start(Context context, String path) {
        Intent intent = new Intent(context, ShareActivity.class);
        intent.putExtra("path", path);
        context.startActivity(intent);
    }

    @BindView(R.id.image)
    ImageView resultImg;

    private String imagePath;

    @Override
    protected void loadContentLayout() {
        setContentView(R.layout.activity_share);
    }


    @Override
    protected void initView() {
        Intent intent = getIntent();
        if (intent != null) {
            imagePath = intent.getStringExtra("path");
        }

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) resultImg.getLayoutParams();
        int width = getResources().getDisplayMetrics().widthPixels - Tools.dip2px(this, 64);
        layoutParams.width = width;
        layoutParams.height = width * 350 / 296;
        resultImg.setLayoutParams(layoutParams);

        if (!Tools.isNull(imagePath)) {
            Picasso.with(ShareActivity.this)
                    .load("file:///" + imagePath)
                    .into(resultImg);
        }
    }

    @Override
    protected void setListener() {

    }

    @OnClick({R.id.done})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.done:
                onBackPressed();
                break;
        }
    }
}
