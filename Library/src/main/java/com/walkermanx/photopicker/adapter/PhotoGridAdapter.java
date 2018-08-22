package com.walkermanx.photopicker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.walkermanx.photopicker.R;
import com.walkermanx.photopicker.entity.Photo;
import com.walkermanx.photopicker.entity.PhotoDirectory;
import com.walkermanx.photopicker.event.OnItemCheckListener;
import com.walkermanx.photopicker.event.OnPhotoClickListener;
import com.walkermanx.photopicker.utils.AndroidLifecycleUtils;
import com.walkermanx.photopicker.utils.MediaStoreHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by donglua on 15/5/31.
 */
public class PhotoGridAdapter extends SelectableAdapter<PhotoGridAdapter.PhotoViewHolder> {

    private LayoutInflater inflater;
    private RequestManager glide;
    private RequestOptions options;

    private OnItemCheckListener onItemCheckListener = null;
    private OnPhotoClickListener onPhotoClickListener = null;
    private View.OnClickListener onCameraClickListener = null;


    public final static int ITEM_TYPE_CAMERA = 100;
    public final static int ITEM_TYPE_PHOTO = 101;
    private final static int COL_NUMBER_DEFAULT = 3;

    private boolean hasCamera = true;
    private boolean previewEnable = true;
    private boolean hideSelectFrame = false;

    private int imageSize;
    private int columnNumber = COL_NUMBER_DEFAULT;


    public PhotoGridAdapter(Context context, RequestManager requestManager, List<PhotoDirectory> photoDirectories) {
        this.photoDirectories = photoDirectories;
        this.glide = requestManager;
        inflater = LayoutInflater.from(context);
        setColumnNumber(context, columnNumber);
        this.options = new RequestOptions()
                .centerCrop()
                .dontAnimate()
                .override(imageSize, imageSize)
                .placeholder(R.drawable.__picker_ic_photo_black_48dp)
                .error(R.drawable.__picker_ic_broken_image_black_48dp)
                .priority(Priority.HIGH);
    }

    public PhotoGridAdapter(Context context, RequestManager requestManager, List<PhotoDirectory> photoDirectories, ArrayList<String> orginalPhotos, int colNum, boolean hideSelectFrame) {
        this(context, requestManager, photoDirectories);
        setColumnNumber(context, colNum);
        selectedPhotos = new ArrayList<>();
        if (orginalPhotos != null) selectedPhotos.addAll(orginalPhotos);

        this.hideSelectFrame = hideSelectFrame;
    }

    private void setColumnNumber(Context context, int columnNumber) {
        this.columnNumber = columnNumber;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        imageSize = widthPixels / columnNumber;
    }

    @Override
    public int getItemViewType(int position) {
        return (showCamera() && position == 0) ? ITEM_TYPE_CAMERA : ITEM_TYPE_PHOTO;
    }


    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = inflater.inflate(R.layout.__picker_item_photo, parent, false);
        final PhotoViewHolder holder = new PhotoViewHolder(itemView);
        if (viewType == ITEM_TYPE_CAMERA) {
            holder.vSelected.setVisibility(View.GONE);
            holder.ivPhoto.setScaleType(ImageView.ScaleType.CENTER);

            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onCameraClickListener != null) {
                        onCameraClickListener.onClick(view);
                    }
                }
            });
        } else {
            holder.vSelected.setVisibility(hideSelectFrame ? View.GONE : View.VISIBLE);
        }
        return holder;
    }


    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, int position) {

        if (getItemViewType(position) == ITEM_TYPE_PHOTO) {

            List<Photo> photos = getCurrentPhotos();
            final Photo photo;

            if (showCamera()) {
                photo = photos.get(position - 1);
            } else {
                photo = photos.get(position);
            }

            boolean canLoadImage = AndroidLifecycleUtils.canLoadImage(holder.ivPhoto.getContext());

            if (canLoadImage) {
                glide
                        .load(new File(photo.getPath()))
//                        .centerCrop()
//                        .dontAnimate()
                        .thumbnail(0.5f)
//                        .override(imageSize, imageSize)
//                        .placeholder(R.drawable.__picker_ic_photo_black_48dp)
//                        .error(R.drawable.__picker_ic_broken_image_black_48dp)
                        .apply(options)
                        .into(holder.ivPhoto);
            }

            if (!hideSelectFrame) {
                final boolean isChecked = isSelected(photo);
                holder.vSelected.setSelected(isChecked);
                holder.ivPhoto.setSelected(isChecked);
                holder.vSelected.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = holder.getAdapterPosition();
                        boolean isEnable = true;

                        if (onItemCheckListener != null) {
                            isEnable = onItemCheckListener.onItemCheck(pos, photo,
                                    getSelectedPhotos().size() + (isSelected(photo) ? -1 : 1));
                        }
                        if (isEnable) {
                            toggleSelection(photo);
                            notifyItemChanged(pos);
                        }
                    }
                });
            }

            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onPhotoClickListener != null) {
                        int pos = holder.getAdapterPosition();
                        if (previewEnable) {
                            onPhotoClickListener.onClick(view, pos, showCamera());
                        } else {
                            if (hideSelectFrame) {
                                if (onItemCheckListener != null) {
                                    onItemCheckListener.onItemCheck(pos, photo, 0);
                                }
                            } else {
                                holder.vSelected.performClick();
                            }
                        }
                    }
                }
            });

        } else {
            holder.ivPhoto.setImageResource(R.drawable.__picker_camera);
        }
    }


    @Override
    public int getItemCount() {
        int photosCount =
                photoDirectories.size() == 0 ? 0 : getCurrentPhotos().size();
        if (showCamera()) {
            return photosCount + 1;
        }
        return photosCount;
    }


    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private View vSelected;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            ivPhoto = (ImageView) itemView.findViewById(R.id.iv_photo);
            vSelected = itemView.findViewById(R.id.v_selected);
        }
    }


    public void setOnItemCheckListener(OnItemCheckListener onItemCheckListener) {
        this.onItemCheckListener = onItemCheckListener;
    }


    public void setOnPhotoClickListener(OnPhotoClickListener onPhotoClickListener) {
        this.onPhotoClickListener = onPhotoClickListener;
    }


    public void setOnCameraClickListener(View.OnClickListener onCameraClickListener) {
        this.onCameraClickListener = onCameraClickListener;
    }


    public ArrayList<String> getSelectedPhotoPaths() {
        ArrayList<String> selectedPhotoPaths = new ArrayList<>(getSelectedItemCount());

        for (String photo : selectedPhotos) {
            selectedPhotoPaths.add(photo);
        }

        return selectedPhotoPaths;
    }


    public void setShowCamera(boolean hasCamera) {
        this.hasCamera = hasCamera;
    }

    public void setPreviewEnable(boolean previewEnable) {
        this.previewEnable = previewEnable;
    }

    public boolean showCamera() {
        return (hasCamera && currentDirectoryIndex == MediaStoreHelper.INDEX_ALL_PHOTOS);
    }

    @Override
    public void onViewRecycled(PhotoViewHolder holder) {
//        Glide.clear(holder.ivPhoto);
        glide.clear(holder.ivPhoto);
        super.onViewRecycled(holder);
    }
}
