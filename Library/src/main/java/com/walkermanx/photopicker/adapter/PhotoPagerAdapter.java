package com.walkermanx.photopicker.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.walkermanx.photopicker.R;
import com.walkermanx.photopicker.event.PhotoOnLongClickManager;
import com.walkermanx.photopicker.utils.AndroidLifecycleUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by donglua on 15/6/21.
 */

// modify PhotoPickerFragment.java add setOnLongClickListener

public class PhotoPagerAdapter extends PagerAdapter {

    private List<String> paths;
    private ArrayList<String> longData;

    private RequestManager mGlide;
    private RequestOptions options;
    private SparseArray<View> pagers = new SparseArray<>();


    public PhotoPagerAdapter(Fragment fragment, List<String> paths, ArrayList<String> longData) {
        this.paths = paths;
        this.mGlide = Glide.with(fragment);
        this.longData = longData;
        this.options = new RequestOptions()
                .dontAnimate()
                .dontTransform()
                .fitCenter()
                .override(800, 800)
                .placeholder(R.drawable.__picker_ic_photo_black_48dp)
                .error(R.drawable.__picker_ic_broken_image_black_48dp)
                .priority(Priority.HIGH);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        final Context context = container.getContext();

        ImageView imageView = (ImageView) pagers.get(position);
        if (imageView == null) {
            imageView = (ImageView) LayoutInflater.from(context)
                    .inflate(R.layout.__picker_picker_item_pager, container, false);
//        final ImageView imageView = itemView.findViewById(R.id.iv_pager);
        }


        final String path = paths.get(position);
        final Uri uri;
        if (path.startsWith("http")) {
            uri = Uri.parse(path);
        } else {
            uri = Uri.fromFile(new File(path));
        }

        // Just like we do when binding views at the grid, we set the transition name to be the string
        // value of the image res.
        ViewCompat.setTransitionName(imageView, path);

        boolean canLoadImage = AndroidLifecycleUtils.canLoadImage(context);

        if (canLoadImage) {
            mGlide.load(uri)
                    .thumbnail(0.1f)
//                    .dontAnimate()
//                    .dontTransform()
//                    .override(800, 800)
//                    .placeholder(R.drawable.__picker_ic_photo_black_48dp)
//                    .error(R.drawable.__picker_ic_broken_image_black_48dp)
                    .apply(options)
                    .into(imageView);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context instanceof Activity) {
                    if (!((Activity) context).isFinishing()) {
                        ((Activity) context).onBackPressed();
                    }
                }
            }
        });

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (longData != null && longData.size() > 0) {
                    OnLongDialog(context, path);
                }
                return true;
            }
        });

        container.addView(imageView);

        return imageView;
    }

    private void OnLongDialog(Context context, final String path) {
        final AlertDialog albumDialog = new AlertDialog.Builder(context).create();
        albumDialog.setCanceledOnTouchOutside(true);
        albumDialog.setCancelable(true);
        View v = LayoutInflater.from(context).inflate(
                R.layout.__picker_dialog_photo_pager, null);
        albumDialog.show();
//        ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(280, ViewGroup.LayoutParams.MATCH_PARENT);
        albumDialog.setContentView(v);
        albumDialog.getWindow().setGravity(Gravity.CENTER);
        albumDialog.getWindow().setBackgroundDrawableResource(R.drawable.__picker_bg_dialog);
        ListView dialog_lv = v.findViewById(R.id.dialog_lv);

        PhotoDialogAdapter photoDialogAdapter = new PhotoDialogAdapter(context, longData);
        dialog_lv.setAdapter(photoDialogAdapter);
        dialog_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                albumDialog.dismiss();
                PhotoOnLongClickManager photoOnLongClickManager = PhotoOnLongClickManager.getInstance();
                photoOnLongClickManager.setOnLongClick(i, path);
            }
        });
    }

    public void setLongData(ArrayList<String> longData) {
        this.longData = longData;
    }

    @Override
    public int getCount() {
        return paths.size();
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View v = (View) object;
        container.removeView(v);
//        Glide.clear((View) object);
        mGlide.clear(v);
        pagers.append(position, v);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }


}
