package com.treasure.posterview.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.treasure.posterview.R;
import com.treasure.posterview.adapter.PhotoAdapter;
import com.treasure.posterview.bean.LayerBean;
import com.treasure.posterview.bean.PhotoBean;
import com.treasure.posterview.helper.PhotoManager;
import com.treasure.posterview.helper.SpaceItemDecoration;
import com.treasure.posterview.ui.views.FloatingActionButton;
import com.treasure.posterview.ui.views.Layer;
import com.treasure.posterview.ui.views.Model;
import com.treasure.posterview.ui.views.PosterView;
import com.treasure.posterview.utils.GetAllPhotoTask;
import com.treasure.posterview.utils.LogUtil;
import com.treasure.posterview.utils.Tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class AlbumSelectActivity extends BaseActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.num)
    TextView selectedNum;
    @BindView(R.id.done)
    FloatingActionButton done;
    @BindView(R.id.poster_view)
    PosterView posterView;
    @BindView(R.id.poster_layout)
    RelativeLayout posterLayout;
    @BindView(R.id.operate_layout)
    RelativeLayout operateLayout;

    private List<PhotoBean> photoList;
    private ArrayList<String> selectedPhotosList = new ArrayList<>();
    private PhotoAdapter recyclerAdapter;
    private Bitmap cover;
    private List<Bitmap> bitmapList = new ArrayList<>();
    private List<Layer> layers = new ArrayList<>();
    private List<LayerBean> layerBeen = new ArrayList<>();
    private List<Integer> positionList = new ArrayList<>();
    private int photosMax = 2;
    private int type;//0:normal  1：replace

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 200:
                    //设置PosterView和posterLayout的宽高
                    setPosterViewAndLayout();
                    posterView.setModel(new Model(cover, layers, cover.getWidth()));
                    break;
                case 201:
                    posterView.getModelView().setFlag(true);
                    posterView.getModelView().getModel().setFlag(true);
                    posterView.getModelView().getModel().setLayers(layers);
                    break;
            }
        }
    };

    @Override
    protected void loadContentLayout() {
        setContentView(R.layout.activity_album_select);
    }

    @Override
    protected void initView() {
        checkStoragePermission();

        Intent intent = getIntent();
        if (intent != null) {
            type = intent.getIntExtra("type", 0);
        }

        photoList = new ArrayList<>();
        recyclerAdapter = new PhotoAdapter(this, photoList, type);
        recyclerAdapter.setMaxCount(photosMax);
        recyclerAdapter.setSelectedResId(R.drawable.bottom_black);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new SpaceItemDecoration(Tools.dip2px(this, 16), SpaceItemDecoration.TYPE_ALBUM));

        if (type == 0) {
            operateLayout.setVisibility(View.VISIBLE);
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inJustDecodeBounds = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Bitmap bitmap = Picasso.with(AlbumSelectActivity.this).load(R.mipmap.ic_template).resize(194, 166).get();
                        cover = bitmap;
                        layerBeen.add(new LayerBean(230, 188, 336, 371, 3));
                        layerBeen.add(new LayerBean(260, 200, 358, 181, 13));
                        bitmapList.clear();
                        layers.clear();

                        for (int i = 0; i < selectedPhotosList.size(); i++) {
                            bitmap = BitmapFactory.decodeFile(selectedPhotosList.get(i));
                            bitmapList.add(bitmap);
                        }

                        for (int i = 0; i < bitmapList.size(); i++) {
                            layers.add(new Layer(bitmapList.get(i), 0)
                                    .markPoint(layerBeen.get(i).getWidth(), layerBeen.get(i).getHeight(),
                                            layerBeen.get(i).getCenterX(), layerBeen.get(i).getCenterY(),
                                            layerBeen.get(i).getDegree(), 0.25f)
                                    .build());
                        }
                        handler.sendEmptyMessage(200);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }else {
            operateLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void setListener() {
        if (type == 0){
            selectedNum.setText("Selected " + selectedPhotosList.size() + "/" + photosMax);

            recyclerAdapter.setOnPhotoSelectedListener(new PhotoAdapter.OnPhotoSelectedListener() {
                @Override
                public void onPhotoSelected(PhotoBean photo, int position) {
                    selectedPhotosList.add(photo.getPath());
                    selectedNum.setText("Selected " + selectedPhotosList.size() + "/" + photosMax);
                    positionList.add(position);

                    //添加单个layer 并更新
                    addAndSetLayerList(photo.getPath());
                    //点击过快 Picasso 加载过慢会出现问题
                    showLoading("");
                    posterView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dissLoading();
                        }
                    }, 200);
                }
            });
            recyclerAdapter.setOnPhotoUnSelectedListener(new PhotoAdapter.OnPhotoUnSelectedListener() {
                @Override
                public void onPhotoUnSelected(PhotoBean photo, int position) {
                    selectedPhotosList.remove(photo.getPath());
                    selectedNum.setText("Selected " + selectedPhotosList.size() + "/" + photosMax);

                    //删除单个layer 并更新
                    removeAndSetLayerList(position);
                    showLoading("");
                    posterView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dissLoading();
                        }
                    }, 200);
                }
            });
            recyclerAdapter.setOnSelectedMaxListener(new PhotoAdapter.OnSelectedMaxListener() {
                @Override
                public void onSelectedMax() {
                    Toast.makeText(AlbumSelectActivity.this, "装不下了～～～", Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            recyclerAdapter.setOnPhotoSelectedListener(new PhotoAdapter.OnPhotoSelectedListener() {
                @Override
                public void onPhotoSelected(PhotoBean photo, int position) {
                    Intent intent = new Intent();
                    intent.putExtra("click_path",photo.getPath());
                    setResult(RESULT_OK,intent);
                    AlbumSelectActivity.this.finish();
                }
            });
        }
    }

    @OnClick({R.id.done})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.done:
                if (selectedPhotosList.size() != photosMax) {
                    Toast.makeText(AlbumSelectActivity.this, "请选 " + photosMax + " 张图片", Toast.LENGTH_SHORT).show();
                    return;
                }
                PosterActivity.start(AlbumSelectActivity.this, selectedPhotosList);
                break;
        }
    }

    private void setPosterViewAndLayout() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) posterView.getLayoutParams();
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) posterLayout.getLayoutParams();

        float scaleCover = (float) cover.getWidth() / cover.getHeight();
        if (1 <= scaleCover) {
            layoutParams.width = Tools.dip2px(AlbumSelectActivity.this, 114);
            layoutParams.height = (int) (Tools.dip2px(AlbumSelectActivity.this, 114) / scaleCover);

            layoutParams2.width = Tools.dip2px(AlbumSelectActivity.this, 120);
            layoutParams2.height = (int) (Tools.dip2px(AlbumSelectActivity.this, 120) / scaleCover);
        } else {
            layoutParams.width = (int) ((Tools.dip2px(AlbumSelectActivity.this, 114)) * scaleCover);
            layoutParams.height = Tools.dip2px(AlbumSelectActivity.this, 114);

            layoutParams2.width = (int) ((Tools.dip2px(AlbumSelectActivity.this, 120)) * scaleCover);
            layoutParams2.height = Tools.dip2px(AlbumSelectActivity.this, 120);
        }
        posterView.setLayoutParams(layoutParams);
        posterLayout.setLayoutParams(layoutParams2);
    }

    /**
     * 添加单个layer 并更新
     */
    private void addAndSetLayerList(final String path) {
        layers.clear();
        //将原来的layer 重新添加到layers   为了更新他们的位置
        for (int i = 0; i < bitmapList.size(); i++) {
            layers.add(new Layer(bitmapList.get(i), 0)
                    .markPoint(layerBeen.get(i).getWidth(), layerBeen.get(i).getHeight(),
                            layerBeen.get(i).getCenterX(), layerBeen.get(i).getCenterY(),
                            layerBeen.get(i).getDegree(), 0.25f)
                    .build());
        }

        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, op);
        final float whScale = (float) op.outWidth / op.outHeight;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bitmap = Picasso.with(AlbumSelectActivity.this).load("file://" + path).resize(180, (int) (180 / whScale)).get();
                    bitmapList.add(bitmap);
                    layers.add(new Layer(bitmap, 0)
                            .markPoint(layerBeen.get(bitmapList.size() - 1).getWidth(), layerBeen.get(bitmapList.size() - 1).getHeight(),
                                    layerBeen.get(bitmapList.size() - 1).getCenterX(), layerBeen.get(bitmapList.size() - 1).getCenterY(),
                                    layerBeen.get(bitmapList.size() - 1).getDegree(), 0.25f)
                            .build());
                    handler.sendEmptyMessage(201);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 删除单个layer 并更新
     */
    private void removeAndSetLayerList(int position) {
        //查找到 取消点击的图片 所处于  当前layers 的相对position
        int i = 0;
        for (int j = 0; j < positionList.size(); j++) {
            if (position == positionList.get(j))
                i = j;
        }
        positionList.remove(i);
        //移除单个layer
        bitmapList.remove(i);
        layers.clear();

        //将原来的layer 重新添加到layers   为了更新他们的位置
        for (int k = 0; k < bitmapList.size(); k++) {
            layers.add(new Layer(bitmapList.get(k), 0)
                    .markPoint(layerBeen.get(k).getWidth(), layerBeen.get(k).getHeight(),
                            layerBeen.get(k).getCenterX(), layerBeen.get(k).getCenterY(),
                            layerBeen.get(k).getDegree(), 0.25f)
                    .build());
        }

        posterView.getModelView().setFlag(true);
        posterView.getModelView().getModel().setFlag(true);
        posterView.getModelView().getModel().setLayers(layers);
    }

    private void loadAlbum() {
        new GetAllPhotoTask() {
            @Override
            protected void onPostExecute(List<PhotoBean> photos) {
                super.onPostExecute(photos);
                recyclerAdapter.refreshData(photos);
            }
        }.execute(new PhotoManager(this));
    }

    //请求相机权限
    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= 21 && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            loadAlbum();
        }
    }

    //动态权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkStoragePermission();
            } else {
                new AlertDialog.Builder(AlbumSelectActivity.this)
                        .setPositiveButton("确定", null)
                        .setMessage("检测到没有存储权限，请开启存储权限后，在使用产品")
                        .setIcon(R.mipmap.ic_launcher_round)
                        .create()
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onResume() {
        //刷新PostView   防止下一页面的返回   导致放缩比例失调
        if (posterView != null) {
            if (posterView.getModelView() != null) {
                if (posterView.getModelView().getModel() != null) {
                    layers.clear();
                    //将原来的layer 重新添加到layers   为了更新他们的位置
                    for (int i = 0; i < bitmapList.size(); i++) {
                        layers.add(new Layer(bitmapList.get(i), 0)
                                .markPoint(layerBeen.get(i).getWidth(), layerBeen.get(i).getHeight(),
                                        layerBeen.get(i).getCenterX(), layerBeen.get(i).getCenterY(),
                                        layerBeen.get(i).getDegree(), 0.25f)
                                .build());
                    }

                    //设置 touch flag   禁止手势 并setLayers
                    posterView.getModelView().setFlag(true);
                    posterView.getModelView().getModel().setFlag(true);
                    posterView.getModelView().getModel().setLayers(layers);
                }
            }
        }
        super.onResume();
    }
}
