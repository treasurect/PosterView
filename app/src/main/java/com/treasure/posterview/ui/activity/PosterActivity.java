package com.treasure.posterview.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;
import com.treasure.posterview.R;
import com.treasure.posterview.adapter.FilterMenuAdapter;
import com.treasure.posterview.bean.LayerBean;
import com.treasure.posterview.helper.OnLayerSelectListener;
import com.treasure.posterview.ui.views.Layer;
import com.treasure.posterview.ui.views.Model;
import com.treasure.posterview.ui.views.PosterView;
import com.treasure.posterview.utils.ColorFilter;
import com.treasure.posterview.utils.Tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class PosterActivity extends BaseActivity {

    public static void start(Context context, ArrayList<String> paths) {
        Intent intent = new Intent(context, PosterActivity.class);
        intent.putExtra("paths", paths);
        context.startActivity(intent);
    }

    @BindView(R.id.poster_view)
    PosterView posterView;
    @BindView(R.id.poster_layout)
    RelativeLayout posterLayout;

    private List<float[]> filters = new ArrayList<>();
    private ArrayList<String> selectedPhotoList;
    private List<Bitmap> bitmapList = new ArrayList<>();
    private List<Layer> layers = new ArrayList<>();
    private FilterMenuAdapter menuAdapter;
    private Layer selectedLayer;
    private int widthPixels;
    private Bitmap bitmap, cover;
    private ImageSaveTask imageSaveTask;
    private boolean isShowFilter;
    private String posterPath;
    private int heightPixels;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 200:
                    //设置 postView和posterLayout的宽高
                    setPostViewAndLayout();
                    posterView.setModel(new Model(cover, layers, cover.getWidth()));
                    posterView.getModelView().setOnLayerSelectListener(new OnLayerSelectListener() {
                        @Override
                        public void onSelected(Layer layer) {
                            selectedLayer = layer;
                            posterView.showAdjustMenu(layer);
                            posterView.dissFilterMenu();
                            isShowFilter = false;
                        }

                        @Override
                        public void dismiss(Layer layer) {

                            if (selectedLayer == layer) {//怕有冲突加额外的判断
                                selectedLayer = null;
                                posterView.dissAdjustMenu();
                                posterView.dissFilterMenu();
                                isShowFilter = false;
                            }
                        }
                    });
                    dissLoading();
                    break;
            }
        }
    };

    @Override
    protected void loadContentLayout() {
        setContentView(R.layout.activity_poster);
    }

    @Override
    protected void initView() {
        showLoading();
        widthPixels = getResources().getDisplayMetrics().widthPixels;
        heightPixels = getResources().getDisplayMetrics().heightPixels;

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getExtras() != null) {
                selectedPhotoList = (ArrayList<String>) getIntent().getExtras().get("paths");
            }
        }
        initFilterMenu();//初始化 用于渲染的 recyclerView
        initAdjustMenu();
        addViewToPaint();
    }

    @Override
    protected void setListener() {

    }


    private void initFilterMenu() {
        View filter_menu = getLayoutInflater().inflate(R.layout.layout_menu_filter, null);
        RecyclerView poster_recyclerView = (RecyclerView) filter_menu.findViewById(R.id.poster_recyclerView);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        inItFilters();
        menuAdapter = new FilterMenuAdapter(this, filters);
        menuAdapter.setOnSelect(new FilterMenuAdapter.OnSelect() {
            @Override
            public void onSelect(float[] filter) {
                if (selectedLayer != null) {

                    if (filter != null) {
                        posterView.dissFilterMenu();
                        isShowFilter = false;
                        Bitmap bitmap = ColorFilter.setColorMatrix(selectedLayer.getLayer(), filter, false);
                        selectedLayer.setFilterLayer(bitmap);
                        posterView.getModelView().invalidate();
                    } else {
                        selectedLayer.clearFilter();
                        posterView.getModelView().invalidate();
                    }
                }
            }
        });
        poster_recyclerView.setLayoutManager(linearLayoutManager);
        poster_recyclerView.setAdapter(menuAdapter);

        posterView.addMenuInit(filter_menu, widthPixels - Tools.dip2px(getApplicationContext(), 100.0f),
                Tools.dip2px(getApplicationContext(), 60));
    }

    private void inItFilters() {
        filters.add(ColorFilter.colormatrix_heibai);
        filters.add(ColorFilter.colormatrix_fugu);
        filters.add(ColorFilter.colormatrix_gete);
        filters.add(ColorFilter.colormatrix_chuan_tong);
        filters.add(ColorFilter.colormatrix_danya);
        filters.add(ColorFilter.colormatrix_guangyun);
        filters.add(ColorFilter.colormatrix_fanse);
        filters.add(ColorFilter.colormatrix_hepian);
        filters.add(ColorFilter.colormatrix_huajiu);
        filters.add(ColorFilter.colormatrix_jiao_pian);
        filters.add(ColorFilter.colormatrix_landiao);
        filters.add(ColorFilter.colormatrix_langman);
        filters.add(ColorFilter.colormatrix_ruise);
        filters.add(ColorFilter.colormatrix_menghuan);
        filters.add(ColorFilter.colormatrix_qingning);
        filters.add(ColorFilter.colormatrix_yese);
    }

    private void initAdjustMenu() {
        View adjust_menu = getLayoutInflater().inflate(R.layout.layout_menu_adjust, null);
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ImageView menu_replace = (ImageView) adjust_menu.findViewById(R.id.adjust_menu_replace);
        ImageView menu_rotate = (ImageView) adjust_menu.findViewById(R.id.adjust_menu_rotate);
        ImageView menu_flip = (ImageView) adjust_menu.findViewById(R.id.adjust_menu_flip);
        ImageView menu_filter = (ImageView) adjust_menu.findViewById(R.id.adjust_menu_filter);
        menu_replace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                posterView.dissFilterMenu();
                posterView.dissAdjustMenu();
                isShowFilter = false;

                bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cover);
                Bitmap filterLayer = selectedLayer.getFilterLayer();

                selectedLayer.resetLayer(bitmap, filterLayer);
                selectedLayer.caculateDrawLayer(posterView.getModelView().getWidth() * 1.0f / cover.getWidth());
                posterView.release();
                posterView.invalidate();
            }
        });
        menu_rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                posterView.dissFilterMenu();
                posterView.dissAdjustMenu();
                isShowFilter = false;

                selectedLayer.setDegree((selectedLayer.getDegree() + 90) % 360);
                posterView.getModelView().invalidate();
            }
        });
        menu_flip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                posterView.dissAdjustMenu();
                posterView.dissFilterMenu();
                isShowFilter = false;
                selectedLayer.setFlip();
                posterView.getModelView().invalidate();
            }
        });
        menu_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                posterView.dissAdjustMenu();
                if (!isShowFilter) {
                    posterView.showFilterMenu(selectedLayer);
                    isShowFilter = true;
                } else {
                    posterView.dissFilterMenu();
                    isShowFilter = false;
                }
            }
        });

        posterView.addAdjustInit(adjust_menu, layoutParams);
    }

    @OnClick({R.id.download})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.download:
                if (imageSaveTask != null && !imageSaveTask.isCancelled()) {
                    imageSaveTask.cancel(true);
                    imageSaveTask = null;
                }
                imageSaveTask = new ImageSaveTask();
                imageSaveTask.execute();
                break;
        }
    }


    private void addViewToPaint() {
        //子线程添加Layer
        new Thread(new Runnable() {
            @Override
            public void run() {
//                Glide.with(PosterActivity.this).load(path).asBitmap().override(width,height).into(new SimpleTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//
//                    }
//                });
                try {
                    Bitmap bmp = Picasso.with(PosterActivity.this).load(R.mipmap.ic_template).resize(389, 333).get();
                    cover = bmp;
                    for (int i = 0; i < selectedPhotoList.size(); i++) {
                        Bitmap bitmap = Picasso.with(PosterActivity.this).load("file://" + selectedPhotoList.get(i)).get();
                        bitmapList.add(bitmap);
                    }
                    List<LayerBean> layerBeen = new ArrayList<>();
                    layerBeen.add(new LayerBean(230, 188, 336, 371, 3));
                    layerBeen.add(new LayerBean(260, 200, 358, 181, 13));
                    for (int j = 0; j < bitmapList.size(); j++) {
                        layers.add(new Layer(bitmapList.get(j), 0)
                                .markPoint(layerBeen.get(j).getWidth(), layerBeen.get(j).getHeight(),
                                        layerBeen.get(j).getCenterX(), layerBeen.get(j).getCenterY(),
                                        layerBeen.get(j).getDegree(), 0.5f)
                                .build());
                    }
                    handler.sendEmptyMessage(200);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void setPostViewAndLayout() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) posterView.getLayoutParams();
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) posterLayout.getLayoutParams();

        float scaleScreen = (float) widthPixels / heightPixels;
        float scaleCover = (float) cover.getWidth() / cover.getHeight();
        if (scaleScreen <= scaleCover) {
            layoutParams.width = widthPixels - Tools.dip2px(PosterActivity.this, 22);
            layoutParams.height = (int) ((widthPixels - Tools.dip2px(PosterActivity.this, 22)) / scaleCover);

            layoutParams2.width = widthPixels - Tools.dip2px(PosterActivity.this, 16);
            layoutParams2.height = (int) ((widthPixels - Tools.dip2px(PosterActivity.this, 16)) / scaleCover);
        } else {
            layoutParams.width = (int) ((heightPixels - Tools.dip2px(PosterActivity.this, 68)) * scaleCover);
            layoutParams.height = heightPixels - Tools.dip2px(PosterActivity.this, 68);

            layoutParams2.width = (int) ((heightPixels - Tools.dip2px(PosterActivity.this, 62)) * scaleCover);
            layoutParams2.height = heightPixels - Tools.dip2px(PosterActivity.this, 62);
        }
        posterView.setLayoutParams(layoutParams);
        posterLayout.setLayoutParams(layoutParams2);
    }

    @Override
    protected void onDestroy() {
        if (imageSaveTask != null && !imageSaveTask.isCancelled()) {
            imageSaveTask.cancel(true);
            imageSaveTask = null;
        }
        super.onDestroy();
    }

    private class ImageSaveTask extends AsyncTask<Void, Void, Boolean> {
        private Bitmap bitmap;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading("保存中...");
            posterLayout.setDrawingCacheEnabled(true);
            bitmap = posterLayout.getDrawingCache();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            posterPath = Tools.saveBitmapToSD(PosterActivity.this, bitmap, System.currentTimeMillis() + "_poster", 0);
            if (!posterPath.equals("Download Failed!")) {
                return true;
            } else {
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            dissLoading();
            posterLayout.setDrawingCacheEnabled(false);
            if (aBoolean) {
                ShareActivity.start(PosterActivity.this, posterPath);
            }
        }
    }
}
