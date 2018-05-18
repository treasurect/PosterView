package com.treasure.posterview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.treasure.posterview.R;
import com.treasure.posterview.utils.ColorFilter;

import java.util.List;

/**
 * Created by treasure on 2017/11/2.
 */

public class FilterMenuAdapter extends RecyclerView.Adapter<FilterMenuAdapter.ViewHolder> {
    private Context context;
    private List<float[]> filters;
    private LayoutInflater inflater;

    public FilterMenuAdapter(Context context, List<float[]> filters) {
        this.context = context;
        this.filters = filters;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {
        return filters.size() + 1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = inflater.inflate(R.layout.item_fliter_image, null);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.img.setImageResource(R.mipmap.ic_filter_img);
        if (position != 0) {
            ColorFilter.imageViewColorFilter(holder.img, filters.get(position - 1));
        } else {
            holder.img.setColorFilter(null);
        }
        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onSelectListener != null){
                    if (position != 0) {
                        onSelectListener.onSelect(filters.get(position - 1));
                    } else {
                        onSelectListener.onSelect(null);
                    }
                }
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;

        public ViewHolder(View view) {
            super(view);
            img = (ImageView) view.findViewById(R.id.fliter_item_image);
        }
    }
    public interface OnSelect {
        void onSelect(float[] filter);
    }
    private OnSelect onSelectListener = null;

    public void setOnSelect(OnSelect onSelectListener) {
        this.onSelectListener = onSelectListener;
    }
}
