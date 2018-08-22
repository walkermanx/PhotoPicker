package com.walkermanx.photopicker;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.walkermanx.photopicker.entity.Photo;
import com.walkermanx.photopicker.event.OnItemCheckListener;
import com.walkermanx.photopicker.fragment.ImagePagerFragment;
import com.walkermanx.photopicker.fragment.PhotoPickerFragment;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.widget.Toast.LENGTH_LONG;
import static com.walkermanx.photopicker.PhotoPicker.DEFAULT_COLUMN_NUMBER;
import static com.walkermanx.photopicker.PhotoPicker.DEFAULT_MAX_COUNT;
import static com.walkermanx.photopicker.PhotoPicker.EXTRA_CROP_STATUSBARCOLOR;
import static com.walkermanx.photopicker.PhotoPicker.EXTRA_CROP_TOOLBARCOLOR;
import static com.walkermanx.photopicker.PhotoPicker.EXTRA_CROP_TOOLBAR_WIDGET_COLOR;
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
import static com.walkermanx.photopicker.PhotoPicker.EXTRA_TITLE_MARGIN_START;
import static com.walkermanx.photopicker.PhotoPicker.KEY_SELECTED_PHOTOS;

public class PhotoPickerActivity extends AppCompatActivity {

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
    private LinearLayout linear_view;
    private int cropX;
    private int cropY;
    public int toolbarColor;
    private int statusbarColor;
    private int toolbarWidgetColor;
    private int titleMarginStart;


    private int getResId(int attrVal) {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attrVal, typedValue, false);
        return typedValue.data;
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);
        boolean openCamera = getIntent().getBooleanExtra(EXTRA_OPEN_CAMERA, false);

        isCrop = getIntent().getBooleanExtra(EXTRA_OPEN_CROP, false);
        cropX = getIntent().getIntExtra(EXTRA_CROP_X, 1);
        cropY = getIntent().getIntExtra(EXTRA_CROP_Y, 1);

        int toolbarWidgetColorVal;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbarWidgetColorVal = getResId(android.R.attr.colorControlNormal);
            toolbarColor = getIntent().getIntExtra(EXTRA_CROP_TOOLBARCOLOR, getResId(android.R.attr.colorPrimary));
            statusbarColor = getIntent().getIntExtra(EXTRA_CROP_STATUSBARCOLOR, getResId(android.R.attr.colorPrimaryDark));
//            toolbarWidgetColor = getIntent().getIntExtra(EXTRA_CROP_TOOLBAR_WIDGET_COLOR, getResId(android.R.attr.colorControlNormal));
        } else {
            toolbarWidgetColorVal = getResId(R.attr.colorControlNormal);
            toolbarColor = getIntent().getIntExtra(EXTRA_CROP_TOOLBARCOLOR, getResId(R.attr.colorPrimary));
            statusbarColor = getIntent().getIntExtra(EXTRA_CROP_STATUSBARCOLOR, getResId(R.attr.colorPrimaryDark));
//            toolbarWidgetColor = getIntent().getIntExtra(EXTRA_CROP_TOOLBAR_WIDGET_COLOR, getResId(R.attr.colorControlNormal));
        }

        toolbarWidgetColor = getIntent().getIntExtra(EXTRA_CROP_TOOLBAR_WIDGET_COLOR, toolbarWidgetColorVal);
        titleMarginStart = getIntent().getIntExtra(EXTRA_TITLE_MARGIN_START, R.dimen.__picker_toolbar_title_margin_start);
        boolean showGif = getIntent().getBooleanExtra(EXTRA_SHOW_GIF, false);
        boolean previewEnabled = getIntent().getBooleanExtra(EXTRA_PREVIEW_ENABLED, true);

        setShowGif(showGif);

        setContentView(R.layout.__picker_activity_photo_picker);
        linear_view = findViewById(R.id.linear_view);
        //状态栏颜色设置
        setStatusBarColor(ContextCompat.getColor(this, statusbarColor));
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            actionBar.setElevation(25);
        }
        /**
         * 等待toolbar绘制完成后给其着色  当toolbarWidgetColor !=toolbarWidgetColorVal 时 则判断为通过代码设置了tint着色 则为toolbar 执行 applyTint方法为其着色
         */
        if (toolbarWidgetColor != toolbarWidgetColorVal) {
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
            getSupportFragmentManager().executePendingTransactions();
        }


        pickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {
            @Override
            public boolean onItemCheck(int position, Photo photo, final int selectedItemCount) {

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
        });


        if (savedInstanceState == null) {
            if (openCamera) {
                linear_view.setVisibility(View.GONE);
                pickerFragment.activityCamera();
            }
        }
    }

    /**
     * Sets status-bar color for L devices.
     *
     * @param color - status-bar color
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStatusBarColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Window window = getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(color);
            }
        }
    }

    /**
     * 给toolbar着色
     *
     * @param mToolbar
     */
    private void applyTint(Toolbar mToolbar, @ColorInt int mToolbarWidgetColor) {
        Drawable drawable = mToolbar.getNavigationIcon();
        if (drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(mToolbarWidgetColor, PorterDuff.Mode.SRC_ATOP);
            mToolbar.setNavigationIcon(drawable);
        }

        drawable = mToolbar.getOverflowIcon();
        if (drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(mToolbarWidgetColor, PorterDuff.Mode.SRC_ATOP);
            mToolbar.setOverflowIcon(drawable);
        }


        drawable = mToolbar.getLogo();
        if (drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(mToolbarWidgetColor, PorterDuff.Mode.SRC_ATOP);
            mToolbar.setLogo(drawable);
        }

        for (int i = 0; i < mToolbar.getChildCount(); i++) {
            View v = mToolbar.getChildAt(i);
            if (v instanceof ActionMenuView) {
                for (int j = 0; j < ((ActionMenuView) v).getChildCount(); j++) {
                    View v2 = ((ActionMenuView) v).getChildAt(j);
                    if (v2 instanceof ActionMenuItemView) {
                        ((ActionMenuItemView) v2).setTextColor(mToolbarWidgetColor);
                    }
                }
            }
        }
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
        options.setAllowedGestures(3, 3, 3);

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
                .replace(R.id.container, this.imagePagerFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (maxCount <= 1 && isCrop)
            return false;

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
