package com.treasure.posterview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.treasure.posterview.R;
import com.treasure.posterview.bean.PhotoBean;
import com.treasure.posterview.utils.Tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by treasure on 2017/12/21.
 */

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private final String TAG = PhotoAdapter.class.getSimpleName();

    private List<PhotoBean> mData;
    private ArrayList<PhotoBean> mSelectedPhotos;
    private Set<Integer> mSelectedPhotoPositions;

    private int mMaxCount = 3;
    private int widthPixels;
    private Context context;
    private int type;

    private int mSelectedResId;

    public PhotoAdapter(Context context, List<PhotoBean> mData, int type) {
        this.mData = mData;
        this.context = context;
        this.type = type;
        mSelectedPhotos = new ArrayList<>();
        mSelectedPhotoPositions = new HashSet<>();
        widthPixels = context.getResources().getDisplayMetrics().widthPixels;
    }

    public List<PhotoBean> getData() {
        return mData;
    }

    public void setData(List<PhotoBean> data) {
        mData = data;
    }

    public int getMaxCount() {
        return mMaxCount;
    }

    public void setMaxCount(int maxCount) {
        mMaxCount = maxCount;
    }

    public ArrayList<PhotoBean> getSelectedPhotos() {
        return mSelectedPhotos;
    }

    public ArrayList<String> getSelectedPhotoPaths() {
        ArrayList<String> paths = new ArrayList<>();
        for (PhotoBean photo : mSelectedPhotos) {
            paths.add(photo.getPath());
        }

        return paths;
    }

    public void refreshData(List<PhotoBean> dataNew) {
        mData = dataNew;
        mSelectedPhotos.clear();
        notifyDataSetChanged();
    }

    public void reset() {
        mSelectedPhotos.clear();
        mSelectedPhotoPositions.clear();

        notifyDataSetChanged();
    }

    @Override
    public PhotoAdapter.PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_image, parent, false);
        return new PhotoAdapter.PhotoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PhotoAdapter.PhotoViewHolder holder, final int position) {
        int width = (widthPixels - Tools.dip2px(context, 72)) / 3;
//        holder.mShadow.setBackgroundResource(mSelectedResId);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) holder.layout.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = width;
        holder.layout.setLayoutParams(layoutParams);
        holder.mIvPhoto.setLayoutParams(layoutParams);
        //解决View复用时的问题

        final String path = mData.get(position).getPath();
        Picasso.with(holder.itemView.getContext())
                .load("file:///" + path)
                .into(holder.mIvPhoto);

        if (type == 0) {
            holder.check.setVisibility(View.VISIBLE);
            if (!mSelectedPhotoPositions.contains(position)) {
                holder.check.setImageResource(R.mipmap.ic_checkbox_un);
            } else if (mSelectedPhotoPositions.contains(position)) {
                holder.check.setImageResource(R.mipmap.ic_checkbox_selected);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getAdapterPosition();
                    if (mSelectedPhotoPositions.contains(pos)) {
                        mSelectedPhotoPositions.remove(pos);
                        mSelectedPhotos.remove(mData.get(pos));
                        if (mOnPhotoUnSelectedListener != null) {
                            mOnPhotoUnSelectedListener.onPhotoUnSelected(mData.get(pos), pos);
                        }

                        holder.check.setImageResource(R.mipmap.ic_checkbox_un);

                    } else {
                        if (mSelectedPhotoPositions.size() >= mMaxCount) {
                            if (mOnSelectedMaxListener != null) {
                                mOnSelectedMaxListener.onSelectedMax();
                            }
                        } else {
                            mSelectedPhotoPositions.add(pos);
                            mSelectedPhotos.add(mData.get(pos));
                            if (mOnPhotoSelectedListener != null) {
                                mOnPhotoSelectedListener.onPhotoSelected(mData.get(pos), pos);
                            }
                            holder.check.setImageResource(R.mipmap.ic_checkbox_selected);
                        }
                    }
                }
            });
        } else {
            holder.check.setVisibility(View.GONE);
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnPhotoSelectedListener != null) {
                        mOnPhotoSelectedListener.onPhotoSelected(mData.get(position), position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void setSelectedResId(int selectedResId) {
        mSelectedResId = selectedResId;
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView mIvPhoto;
        private FrameLayout layout;
        private ImageView check;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            mIvPhoto = (ImageView) itemView.findViewById(R.id.album_item_image);
            layout = (FrameLayout) itemView.findViewById(R.id.album_item_layout);
            check = (ImageView) itemView.findViewById(R.id.album_item_check);
        }
    }

    private PhotoAdapter.OnPhotoSelectedListener mOnPhotoSelectedListener;
    private PhotoAdapter.OnPhotoUnSelectedListener mOnPhotoUnSelectedListener;
    private PhotoAdapter.OnSelectedMaxListener mOnSelectedMaxListener;

    public interface OnPhotoSelectedListener {
        void onPhotoSelected(PhotoBean photo, int position);
    }

    public interface OnPhotoUnSelectedListener {
        void onPhotoUnSelected(PhotoBean photo, int position);
    }

    public interface OnSelectedMaxListener {
        void onSelectedMax();
    }


    public void setOnSelectedMaxListener(PhotoAdapter.OnSelectedMaxListener onSelectedMaxListener) {
        mOnSelectedMaxListener = onSelectedMaxListener;
    }

    public void setOnPhotoSelectedListener(PhotoAdapter.OnPhotoSelectedListener onPhotoSelectedListener) {
        mOnPhotoSelectedListener = onPhotoSelectedListener;
    }

    public void setOnPhotoUnSelectedListener(PhotoAdapter.OnPhotoUnSelectedListener onPhotoUnSelectedListener) {
        mOnPhotoUnSelectedListener = onPhotoUnSelectedListener;
    }
}
