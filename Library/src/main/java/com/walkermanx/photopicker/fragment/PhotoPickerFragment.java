package com.walkermanx.photopicker.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.walkermanx.BaseActivity;
import com.walkermanx.photopicker.PhotoPicker;
import com.walkermanx.photopicker.PhotoPickerActivity;
import com.walkermanx.photopicker.R;
import com.walkermanx.photopicker.adapter.PhotoGridAdapter;
import com.walkermanx.photopicker.adapter.PopupDirectoryListAdapter;
import com.walkermanx.photopicker.entity.Photo;
import com.walkermanx.photopicker.entity.PhotoDirectory;
import com.walkermanx.photopicker.event.OnPhotoClickListener;
import com.walkermanx.photopicker.utils.AndroidLifecycleUtils;
import com.walkermanx.photopicker.utils.ImageCaptureManager;
import com.walkermanx.photopicker.utils.MediaStoreHelper;
import com.walkermanx.photopicker.utils.PermissionsConstant;
import com.walkermanx.photopicker.utils.PermissionsUtils;
import com.yalantis.ucrop.UCrop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by donglua on 15/5/31.
 */

// modify PhotoPickerFragment.java add Crop

public class PhotoPickerFragment extends Fragment {

    private ImageCaptureManager captureManager;
    private PhotoGridAdapter photoGridAdapter;

    private PopupDirectoryListAdapter listAdapter;
    //所有photos的路径
    private List<PhotoDirectory> directories;
    //传入的已选照片
    private ArrayList<String> originalPhotos;

    private int SCROLL_THRESHOLD = 30;
    int column;
    int maxCount;

    private final static String EXTRA_CAMERA = "camera";
    private final static String EXTRA_COLUMN = "column";
    private final static String EXTRA_COUNT = "count";
    private final static String EXTRA_GIF = "gif";
    private final static String EXTRA_ORIGIN = "origin";
    private final static String EXTRA_CROP = "Crop";
    private final static String EXTRA_OPEN_CAMERA = "openCamera";


    private ListPopupWindow listPopupWindow;
    private RequestManager mGlideRequestManager;
    private boolean isCrop;
    private boolean isOpenCamera;
    RecyclerView recyclerView;
    int itemHeight;

    public static PhotoPickerFragment newInstance(boolean showCamera, boolean showGif,
                                                  boolean previewEnable, int column, int maxCount, ArrayList<String> originalPhotos, boolean isCrop, boolean openCamera) {
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_CAMERA, showCamera);
        args.putBoolean(EXTRA_GIF, showGif);
        args.putBoolean(PhotoPicker.EXTRA_PREVIEW_ENABLED, previewEnable);
        args.putInt(EXTRA_COLUMN, column);
        args.putInt(EXTRA_COUNT, maxCount);
        args.putStringArrayList(EXTRA_ORIGIN, originalPhotos);
        args.putBoolean(EXTRA_CROP, isCrop);
        args.putBoolean(EXTRA_OPEN_CAMERA, openCamera);

        PhotoPickerFragment fragment = new PhotoPickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        captureManager = new ImageCaptureManager(getActivity());

        itemHeight = getResources().getDimensionPixelOffset(R.dimen.__picker_item_directory_height);
        mGlideRequestManager = Glide.with(this);

        directories = new ArrayList<>();
        originalPhotos = getArguments().getStringArrayList(EXTRA_ORIGIN);

        maxCount = getArguments().getInt(EXTRA_COUNT, PhotoPicker.DEFAULT_MAX_COUNT);
        column = getArguments().getInt(EXTRA_COLUMN, PhotoPicker.DEFAULT_COLUMN_NUMBER);
        isCrop = getArguments().getBoolean(EXTRA_CROP, false);
        isOpenCamera = getArguments().getBoolean(EXTRA_OPEN_CAMERA, false);

        boolean showCamera = getArguments().getBoolean(EXTRA_CAMERA, true);
        boolean previewEnable = getArguments().getBoolean(PhotoPicker.EXTRA_PREVIEW_ENABLED, true);

        photoGridAdapter = new PhotoGridAdapter(getActivity(), mGlideRequestManager, directories, originalPhotos, column, maxCount <= 1 && isCrop);
        photoGridAdapter.setShowCamera(showCamera);
        photoGridAdapter.setPreviewEnable(previewEnable);

        Bundle mediaStoreArgs = new Bundle();

        boolean showGif = getArguments().getBoolean(EXTRA_GIF);
        mediaStoreArgs.putBoolean(PhotoPicker.EXTRA_SHOW_GIF, showGif);
        MediaStoreHelper.getPhotoDirs(getActivity(), mediaStoreArgs,
                new MediaStoreHelper.PhotosResultCallback() {
                    @Override
                    public void onResultCallback(List<PhotoDirectory> dirs) {
                        if (dirs.size() <= 1 && !directories.isEmpty())
                            return;

                        directories.clear();
                        directories.addAll(dirs);
                        photoGridAdapter.notifyDataSetChanged();
                        listAdapter.notifyDataSetChanged();
                        if (listPopupWindow.isShowing())
                            adjustHeight();
                    }
                });


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.__picker_fragment_photo_picker, container, false);

        listAdapter = new PopupDirectoryListAdapter(mGlideRequestManager, directories);
        recyclerView = rootView.findViewById(R.id.rv_photos);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(column, OrientationHelper.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(photoGridAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        final Button btSwitchDirectory = rootView.findViewById(R.id.button);
        //设置底部Bar背景色 默认和PhotoPickerActivity 中 Toolbar使用相同背景色
        View bottomBar = rootView.findViewById(R.id.bottomBar);
        if (getActivity() instanceof BaseActivity) {
            bottomBar.setBackgroundColor(ContextCompat.getColor(getContext(), ((BaseActivity) getActivity()).toolbarColor));
            btSwitchDirectory.setTextColor(ContextCompat.getColor(getContext(), ((BaseActivity) getActivity()).toolbarWidgetColor));
        }

        listPopupWindow = new ListPopupWindow(getActivity());
        listPopupWindow.setWidth(ListPopupWindow.MATCH_PARENT);
        listPopupWindow.setAnchorView(bottomBar);
        listPopupWindow.setAdapter(listAdapter);
        listPopupWindow.setModal(true);
        listPopupWindow.setDropDownGravity(Gravity.TOP);
        listPopupWindow.setVerticalOffset(-2);
        listPopupWindow.setBackgroundDrawable(new ColorDrawable(0));

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listPopupWindow.dismiss();

                PhotoDirectory directory = directories.get(position);

                btSwitchDirectory.setText(directory.getName());

                photoGridAdapter.setCurrentDirectoryIndex(position);
                photoGridAdapter.notifyDataSetChanged();
            }
        });

        photoGridAdapter.setOnPhotoClickListener(new OnPhotoClickListener() {
            @Override
            public void onClick(View v, int position, boolean showCamera) {
                final int index = showCamera ? position - 1 : position;

                List<String> photos = photoGridAdapter.getCurrentPhotoPaths();

                int[] screenLocation = new int[2];
                v.getLocationOnScreen(screenLocation);
                ImagePagerFragment imagePagerFragment =
                        ImagePagerFragment.newInstance(photos, index, screenLocation, v.getWidth(),
                                v.getHeight());

                ((PhotoPickerActivity) getActivity()).addImagePagerFragment(imagePagerFragment);
            }
        });

        photoGridAdapter.setOnCameraClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!PermissionsUtils.checkCameraPermission(PhotoPickerFragment.this)) return;
                if (!PermissionsUtils.checkWriteStoragePermission(PhotoPickerFragment.this)) return;
                openCamera();
            }
        });

        btSwitchDirectory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listPopupWindow.isShowing()) {
                    listPopupWindow.dismiss();
                } else if (!getActivity().isFinishing()) {
                    adjustHeight();
                    listPopupWindow.show();
                    listPopupWindow.getListView().setBackgroundResource(android.R.color.white);
                }
            }
        });


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Log.d(">>> Picker >>>", "dy = " + dy);
                if (Math.abs(dy) > SCROLL_THRESHOLD) {
                    mGlideRequestManager.pauseRequests();
                } else {
                    resumeRequestsIfNotDestroyed();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    resumeRequestsIfNotDestroyed();
                }
            }
        });

        return rootView;
    }


    public void activityCamera() {
        if (!PermissionsUtils.checkCameraPermission(PhotoPickerFragment.this)) return;
        if (!PermissionsUtils.checkWriteStoragePermission(PhotoPickerFragment.this)) return;
        openCamera();
    }

    public void openCamera() {
        try {
            Intent intent = captureManager.dispatchTakePictureIntent();
            startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ActivityNotFoundException e) {
            // TODO No Activity Found to handle Intent
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("photolog", "isOpenCamera=" + isOpenCamera + ",requestCode=" + requestCode + ",resultCode=" + resultCode);
        if (isOpenCamera && requestCode == ImageCaptureManager.REQUEST_TAKE_PHOTO && resultCode == RESULT_CANCELED) {
            getActivity().finish();
        }
        if (isOpenCamera && requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_CANCELED) {
            getActivity().finish();
        }
        if (requestCode == ImageCaptureManager.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            if (captureManager == null) {
                FragmentActivity activity = getActivity();
                captureManager = new ImageCaptureManager(activity);
            }
            captureManager.galleryAddPic();
            if (isCrop) {
                String path = captureManager.getCurrentPhotoPath();
                ((PhotoPickerActivity) getActivity()).openCropActivity(path);
                return;
            }
            //如果是拍照不裁剪
            if (isOpenCamera) {
                String path = captureManager.getCurrentPhotoPath();
                Log.d("photolog", "path=" + path);
                ((PhotoPickerActivity) getActivity()).PhotoCamera(path);
                return;
            }
            captureManager.galleryAddPic();
            if (directories.size() > 0) {
                String path = captureManager.getCurrentPhotoPath();
                PhotoDirectory directory = directories.get(MediaStoreHelper.INDEX_ALL_PHOTOS);
                directory.getPhotos().add(MediaStoreHelper.INDEX_ALL_PHOTOS, new Photo(path.hashCode(), path));
                directory.setCoverPath(path);
                photoGridAdapter.notifyDataSetChanged();
            }
        }

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            Log.d("photolog", "resultUri=" + resultUri.getPath());
            ((PhotoPickerActivity) getActivity()).PhotoCamera(resultUri.getPath());

        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(getActivity(), "crop fail:", Toast.LENGTH_SHORT).show();
            Log.d("photolog", "cropError=" + cropError.getMessage());
            getActivity().finish();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case PermissionsConstant.REQUEST_CAMERA:
                case PermissionsConstant.REQUEST_EXTERNAL_WRITE:
                    if (PermissionsUtils.checkWriteStoragePermission(this) &&
                            PermissionsUtils.checkCameraPermission(this)) {
                        openCamera();
                    }
                    break;
            }
        }
    }

    public PhotoGridAdapter getPhotoGridAdapter() {
        return photoGridAdapter;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        captureManager.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        captureManager.onRestoreInstanceState(savedInstanceState);
        super.onViewStateRestored(savedInstanceState);
    }

    public ArrayList<String> getSelectedPhotoPaths() {
        return photoGridAdapter.getSelectedPhotoPaths();
    }

    public void adjustHeight() {
        if (listAdapter == null && recyclerView.getMeasuredHeight() == 0) return;
        int count = listAdapter.getCount();
        int COUNT_MAX = recyclerView.getHeight() / itemHeight - 1;
        count = count < COUNT_MAX ? count : COUNT_MAX;
        int newHeight = count * itemHeight;
        if (listPopupWindow != null && newHeight != listPopupWindow.getHeight()) {
            listPopupWindow.setHeight(count * itemHeight);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (directories == null) {
            return;
        }

        for (PhotoDirectory directory : directories) {
            directory.getPhotoPaths().clear();
            directory.getPhotos().clear();
            directory.setPhotos(null);
        }
        directories.clear();
        directories = null;
    }

    private void resumeRequestsIfNotDestroyed() {
        if (!AndroidLifecycleUtils.canLoadImage(this)) {
            return;
        }

        mGlideRequestManager.resumeRequests();
    }


}
