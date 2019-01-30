package com.walkermanx.PhotoPickerDemo;

import android.content.Context;
import android.net.Uri;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.walkermanx.photopicker.utils.AndroidLifecycleUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by donglua on 15/5/31.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private ArrayList<String> photoPaths;
    private LayoutInflater inflater;

    private Context mContext;
    private RequestOptions options;

    final static int TYPE_ADD = 1;
    final static int TYPE_PHOTO = 2;

    final static int MAX = 9;

    public PhotoAdapter(Context mContext, ArrayList<String> photoPaths) {
        this.photoPaths = photoPaths;
        this.mContext = mContext;
        this.inflater = LayoutInflater.from(mContext);
        this.options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.__picker_ic_photo_black_48dp)
                .error(R.drawable.__picker_ic_broken_image_black_48dp);
    }


    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        switch (viewType) {
            case TYPE_ADD:
                itemView = inflater.inflate(R.layout.item_add, parent, false);
                break;
            case TYPE_PHOTO:
                itemView = inflater.inflate(R.layout.__picker_item_photo, parent, false);
                break;
        }
        return new PhotoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, final int position) {

        if (getItemViewType(position) == TYPE_PHOTO) {
            String path = photoPaths.get(position);

            Uri uri = path.startsWith("http") ? Uri.parse(path) : Uri.fromFile(new File(path));

            ViewCompat.setTransitionName(holder.ivPhoto, photoPaths.get(position));

            boolean canLoadImage = AndroidLifecycleUtils.canLoadImage(holder.ivPhoto.getContext());

            if (canLoadImage) {
                Glide.with(mContext)
                        .load(uri)
                        .thumbnail(0.1f)
                        .apply(options)
                        .into(holder.ivPhoto);
            }
        }
    }

    @Override
    public int getItemCount() {
        int count = photoPaths.size() + 1;
        if (count > MAX) {
            count = MAX;
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == photoPaths.size() && position != MAX) ? TYPE_ADD : TYPE_PHOTO;
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private View vSelected;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            vSelected = itemView.findViewById(R.id.v_selected);
            if (vSelected != null) vSelected.setVisibility(View.GONE);
        }
    }

}
