package com.walkermanx.photopicker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.walkermanx.BaseActivity;
import com.walkermanx.photopicker.entity.Photo;
import com.walkermanx.photopicker.event.OnItemCheckListener;
import com.walkermanx.photopicker.fragment.ImagePagerFragment;
import com.walkermanx.photopicker.fragment.PhotoPickerFragment;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.widget.Toast.LENGTH_LONG;
import static com.walkermanx.photopicker.PhotoPicker.DEFAULT_COLUMN_NUMBER;
import static com.walkermanx.photopicker.PhotoPicker.DEFAULT_MAX_COUNT;
import static com.walkermanx.photopicker.PhotoPicker.EXTRA_CROP_X;
import static com.walkermanx.photopicker.PhotoPicker.EXTRA_CROP_Y;
import static com.walkermanx.photopicker.PhotoPicker.EXTRA_GRID_COLUMN;
import static com.walkermanx.photopicker.PhotoPicker.EXTRA_MAX_COUNT;
import static com.walkermanx.photopicker.PhotoPicker.EXTRA_OPEN_CAMERA;
import static com.walkermanx.photopicker.PhotoPicker.EXTRA_OPEN_CROP;
import static com.walkermanx.photopicker.PhotoPicker.EXTRA_ORIGINAL_PHOTOS;
import static com.walkermanx.photopicker.PhotoPicker.EXTRA_PREVIEW_ENABLED;
import static com.walkermanx.photopicker.PhotoPicker.EXTRA_SHOW_CAMERA;
import static com.walkermanx.photopicker.PhotoPicker.EXTRA_SHOW_GIF;
import static com.walkermanx.photopicker.PhotoPicker.KEY_SELECTED_PHOTOS;

public class PhotoPickerActivity extends BaseActivity implements OnItemCheckListener, ImagePagerFragment.CallBack {

    private PhotoPickerFragment pickerFragment;
    private ImagePagerFragment imagePagerFragment;
    private MenuItem menuDoneItem;
    private Toolbar mToolbar;

    private int maxCount = DEFAULT_MAX_COUNT;

    /**
     * to prevent multiple calls to inflate menu
     */

    // modify PhotoPickerActivity.java

    private boolean menuIsInflated = false;

    private boolean showGif = false;
    private int columnNumber = DEFAULT_COLUMN_NUMBER;
    private ArrayList<String> originalPhotos = null;
    private boolean isCrop = false;
    private RelativeLayout linear_view;
    private int cropX;
    private int cropY;
    public static int currentPosition;
    private final String KEY_CURRENT_POSITION = "KEY_CURRENT_POSITION";
    boolean showCamera;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_POSITION, currentPosition);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS | Window.FEATURE_ACTIVITY_TRANSITIONS);
        setContentView(R.layout.__picker_activity_photo_picker);
        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION, 0);
        }

        showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);
        boolean openCamera = getIntent().getBooleanExtra(EXTRA_OPEN_CAMERA, false);

        isCrop = getIntent().getBooleanExtra(EXTRA_OPEN_CROP, false);
        cropX = getIntent().getIntExtra(EXTRA_CROP_X, 1);
        cropY = getIntent().getIntExtra(EXTRA_CROP_Y, 1);


        boolean showGif = getIntent().getBooleanExtra(EXTRA_SHOW_GIF, false);
        boolean previewEnabled = getIntent().getBooleanExtra(EXTRA_PREVIEW_ENABLED, true);

        setShowGif(showGif);

        linear_view = findViewById(R.id.linear_view);

        mToolbar = findViewById(R.id.toolbar);
        // Set all of the Toolbar coloring
        mToolbar.setBackgroundColor(ContextCompat.getColor(this, toolbarColor));
        mToolbar.setTitleTextColor(ContextCompat.getColor(this, toolbarWidgetColor));
        mToolbar.setContentInsetStartWithNavigation(getResources().getDimensionPixelSize(titleMarginStart));
        mToolbar.setTitleMarginStart(getResources().getDimensionPixelSize(titleMarginStart));
        setSupportActionBar(mToolbar);
        setTitle(R.string.__picker_title);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            actionBar.setElevation(25);
//        }
        /**
         * 等待toolbar绘制完成后给其着色  当toolbarWidgetColor !=toolbarWidgetColorVal 时 则判断为通过代码设置了tint着色 则为toolbar 执行 applyTint方法为其着色
         */
        if (isManual()) {
            mToolbar.post(new Runnable() {
                @Override
                public void run() {
                    applyTint(mToolbar, ContextCompat.getColor(mToolbar.getContext(), toolbarWidgetColor));
                }
            });
        }

        maxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
        columnNumber = getIntent().getIntExtra(EXTRA_GRID_COLUMN, DEFAULT_COLUMN_NUMBER);
        originalPhotos = getIntent().getStringArrayListExtra(EXTRA_ORIGINAL_PHOTOS);

        pickerFragment = (PhotoPickerFragment) getSupportFragmentManager().findFragmentByTag("tag");
        if (pickerFragment == null) {
            pickerFragment = PhotoPickerFragment
                    .newInstance(showCamera, showGif, previewEnabled, columnNumber, maxCount, originalPhotos, isCrop, openCamera);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, pickerFragment, "tag")
                    .commit();
//            getSupportFragmentManager().executePendingTransactions();
        }

        if (openCamera) {
            if (savedInstanceState == null)
                getSupportFragmentManager().executePendingTransactions();
            linear_view.setVisibility(View.GONE);
            pickerFragment.activityCamera();
        }

    }

    @Override
    public void onScrollToPosition(int curPos) {
        currentPosition = showCamera ? curPos + 1 : curPos;
    }

    @Override
    public boolean onItemCheck(int position, Photo photo, int selectedItemCount) {
        if (menuDoneItem != null)
            menuDoneItem.setEnabled(selectedItemCount > 0);

        if (maxCount <= 1) {
            if (isCrop) {
                openCropActivity(photo.getPath());
                return false;
            }
            List<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
            if (!photos.contains(photo.getPath())) {
                photos.clear();
                pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
            }
            return true;
        }


        if (selectedItemCount > maxCount) {
            Toast.makeText(getActivity(), getString(R.string.__picker_over_max_count_tips, maxCount),
                    LENGTH_LONG).show();
            return false;
        }
        if (!isCrop) {
            menuDoneItem.setTitle(getString(R.string.__picker_done_with_count, selectedItemCount, maxCount));
        }
        return true;
    }

    public void openCropActivity(String path) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setHideBottomControls(true);
        options.setFreeStyleCropEnabled(false);
        options.setCompressionQuality(90);
        options.setToolbarColor(ContextCompat.getColor(this, toolbarColor));
        options.setStatusBarColor(ContextCompat.getColor(this, statusbarColor));
        options.setToolbarWidgetColor(ContextCompat.getColor(this, toolbarWidgetColor));
        options.setAllowedGestures(UCropActivity.ALL, UCropActivity.ALL, UCropActivity.ALL);

        UCrop.of(Uri.fromFile(new File(path)), Uri.fromFile(new File(getActivity().getCacheDir(), imageFileName)))
                .withAspectRatio(cropX, cropY)
                .withOptions(options)
                .start(PhotoPickerActivity.this);
    }

    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it complete.
     */
    @Override
    public void onBackPressed() {
        if (imagePagerFragment != null && imagePagerFragment.isVisible()) {
            imagePagerFragment.runExitAnimation(new Runnable() {
                public void run() {
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportFragmentManager().popBackStack();
                    }
                }
            });
        } else {
            super.onBackPressed();
        }
    }


    public void addImagePagerFragment(ImagePagerFragment imagePagerFragment) {
        this.imagePagerFragment = imagePagerFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, imagePagerFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (maxCount <= 1 && isCrop)
            return true;

        if (!menuIsInflated) {
            getMenuInflater().inflate(R.menu.__picker_menu_picker, menu);
            menuDoneItem = menu.findItem(R.id.done);
            if (originalPhotos != null && originalPhotos.size() > 0 && !isCrop) {
                menuDoneItem.setEnabled(true);
                menuDoneItem.setTitle(getString(R.string.__picker_done_with_count, originalPhotos.size(), maxCount));
            } else {
                menuDoneItem.setEnabled(false);
            }
            menuIsInflated = true;
            return true;
        }
        return false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.done && !isCrop) {
            Intent intent = new Intent();
            ArrayList<String> selectedPhotos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
            intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, selectedPhotos);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void PhotoCamera(String path) {
        Intent intent = new Intent();
        intent.putExtra(PhotoPicker.KEY_CAMEAR_PATH, path);
        setResult(RESULT_OK, intent);
        finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        pickerFragment.onActivityResult(requestCode, resultCode, data);
    }


    public PhotoPickerActivity getActivity() {
        return this;
    }

    public boolean isShowGif() {
        return showGif;
    }

    public void setShowGif(boolean showGif) {
        this.showGif = showGif;
    }
}
