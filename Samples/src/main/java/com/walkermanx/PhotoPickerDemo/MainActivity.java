package com.walkermanx.PhotoPickerDemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.walkermanx.photopicker.PhotoPicker;
import com.walkermanx.photopicker.PhotoPreview;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private PhotoAdapter photoAdapter;

    private ArrayList<String> selectedPhotos = new ArrayList<>();
    private ArrayList<String> onLongClickListData = new ArrayList<>();

    private ImageView iv_crop;
    private RecyclerView recyclerView;
    private int curPos;
    public int returnPos = -1;
    private SharedElementCallback sharedElementCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //添加长按事件
//        PhotoOnLongClickManager photoOnLongClickManager = PhotoOnLongClickManager.getInstance();
//        photoOnLongClickManager.setOnLongClickListener(this);
//        onLongClickListData.add("分享");
//        onLongClickListData.add("保存");

        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setContentInsetStartWithNavigation(getResources().getDimensionPixelSize(R.dimen.__picker_toolbar_title_margin_start));
        mToolbar.setTitleMarginStart(getResources().getDimensionPixelSize(R.dimen.__picker_toolbar_title_margin_start));
        setSupportActionBar(mToolbar);
        setTitle(R.string.app_name);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                actionBar.setElevation(25);
            }
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        iv_crop = findViewById(R.id.iv_crop);

        recyclerView = findViewById(R.id.recycler_view);
        photoAdapter = new PhotoAdapter(this, selectedPhotos);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(4, OrientationHelper.VERTICAL));
        recyclerView.setAdapter(photoAdapter);

        findViewById(R.id.button_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhotoPicker.builder()
                        .setOpenCamera(true)
                        .setCrop(true)
//                        .setCropColors(R.color.colorPrimary, R.color.colorPrimaryDark)
                        .start(MainActivity.this);
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setOpenCamera(true)
                        .start(MainActivity.this);
            }
        });


        findViewById(R.id.button_one_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setPhotoCount(1)
                        .setPreviewEnabled(false)
                        .setCrop(true)
                        .setCropXY(1, 1)
//                        .setThemeColors(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorControlNormal)
//                        .setToolbarTitleMarginStart(R.dimen.__picker_toolbar_title_margin_start)
                        .start(MainActivity.this);
            }
        });

        findViewById(R.id.button_grid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PreViewImgActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.multiselect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setPhotoCount(9)
                        .setShowCamera(true)
                        .setPreviewEnabled(true)
                        .setSelected(selectedPhotos)
                        .start(MainActivity.this);

            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (photoAdapter.getItemViewType(position) == PhotoAdapter.TYPE_ADD) {
                            PhotoPicker.builder()
                                    .setPhotoCount(PhotoAdapter.MAX)
                                    .setShowCamera(true)
                                    .setPreviewEnabled(true)
                                    .setSelected(selectedPhotos)
                                    .start(MainActivity.this);
                        } else {
                            transitionJump(view.findViewById(R.id.iv_photo), position);
                        }
                    }
                }));
    }

    private void transitionJump(View view, int position) {
        this.curPos = position;
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, view, ViewCompat.getTransitionName(view));
        if (sharedElementCallback == null) {
            sharedElementCallback = new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    RecyclerView.ViewHolder selectedViewHolder = recyclerView.findViewHolderForAdapterPosition(returnPos == -1 ? curPos : returnPos);
                    if (selectedViewHolder == null || selectedViewHolder.itemView == null) {
                        return;
                    }
                    returnPos = -1;
                    sharedElements.put(names.get(0), selectedViewHolder.itemView.findViewById(R.id.iv_photo));
                }
            };
        }
        ActivityCompat.setExitSharedElementCallback(MainActivity.this, sharedElementCallback);

        PhotoPreview.builder()
                .setPhotos(selectedPhotos)
                .setCurrentItem(position)
                //设置主题色系 toolBar背景色 statusBar颜色 以及toolBar 文本/overflow Icon着色
//              .setThemeColors(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorControlNormal)
                //设置toolBar标题栏于NavigationIcon的边距
//              .setToolbarTitleMarginStart(R.dimen.__picker_toolbar_title_margin_start)
                .start(MainActivity.this, options.toBundle());
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        returnPos = data.getIntExtra(PhotoPicker.KEY_SELECTED_INDEX, returnPos);
        if (returnPos != -1) {
            supportStartPostponedEnterTransition();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //选择返回
        if (resultCode == RESULT_OK &&
                (requestCode == PhotoPicker.REQUEST_CODE || requestCode == PhotoPreview.REQUEST_CODE)) {
            iv_crop.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            List<String> photos = null;
            if (data != null) {
                photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
            }
            selectedPhotos.clear();
            if (photos != null) {
                selectedPhotos.addAll(photos);
            }
            photoAdapter.notifyDataSetChanged();
        }
        //拍照功能或者裁剪功能返回
        if (resultCode == RESULT_OK && requestCode == PhotoPicker.CROP_CODE) {
            iv_crop.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            Glide.with(getApplicationContext()).load(Uri.fromFile(new File(data.getStringExtra(PhotoPicker.KEY_CAMEAR_PATH)))).into(iv_crop);
        }
    }

//    @Override
//    public void sendOnLongClick(int position, String path) {
//        //点击图片url为path，自己根据需要实现下载和分享等。
//        Toast.makeText(MainActivity.this, "你点击了：" + onLongClickListData.get(position) + path, Toast.LENGTH_SHORT).show();
//    }
}
