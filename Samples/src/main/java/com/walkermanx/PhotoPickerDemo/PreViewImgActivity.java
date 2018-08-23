package com.walkermanx.PhotoPickerDemo;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

import com.walkermanx.photopicker.PhotoPreview;
import com.walkermanx.photopicker.event.PhotoOnLongClick;
import com.walkermanx.photopicker.event.PhotoOnLongClickManager;

/**
 * Created by q805699513 on 2017/1/23.
 */

public class PreViewImgActivity extends AppCompatActivity {

    private GridView img_grid;
    ArrayList<String> imgData = new ArrayList<>();
    private ArrayList<String> onLongClickListData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_img);
        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setContentInsetStartWithNavigation(getResources().getDimensionPixelSize(R.dimen.__picker_toolbar_title_margin_start));
        mToolbar.setTitleMarginStart(getResources().getDimensionPixelSize(R.dimen.__picker_toolbar_title_margin_start));
        setSupportActionBar(mToolbar);
        setTitle("网图预览");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
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

        onLongClickListData.add("分享");
        onLongClickListData.add("保存");
        onLongClickListData.add("取消");
        //图片长按后的item点击事件回调
        PhotoOnLongClickManager photoOnLongClickManager = PhotoOnLongClickManager.getInstance();
        photoOnLongClickManager.setOnLongClickListener(new PhotoOnLongClick() {
            @Override
            public void sendOnLongClick(int position, String path) {
                Toast.makeText(PreViewImgActivity.this, "你点击了：" + onLongClickListData.get(position) + "，图片路径：" + path, Toast.LENGTH_LONG).show();
            }
        });
        imgData.add("https://images-cn.ssl-images-amazon.com/images/I/61PI88GEqTL.jpg");
        imgData.add("https://images-cn.ssl-images-amazon.com/images/I/61m2kJWam5L.jpg");
        imgData.add("https://images-cn.ssl-images-amazon.com/images/I/61dQGK0xeuL.jpg");
        imgData.add("https://images-cn.ssl-images-amazon.com/images/I/71YuPpF6jKL.jpg");
        imgData.add("https://images-cn.ssl-images-amazon.com/images/I/615YYN4q7gL.jpg");
        imgData.add("https://images-cn.ssl-images-amazon.com/images/I/61Nriqwg2LL.jpg");
        imgData.add("https://images-cn.ssl-images-amazon.com/images/I/615b9gM9GgL.jpg");
        imgData.add("https://images-cn.ssl-images-amazon.com/images/I/41fi2pEYkuL.jpg");
        img_grid = (GridView) findViewById(R.id.img_grid);
        PreViewGridAdapter gridAdapter = new PreViewGridAdapter(PreViewImgActivity.this, imgData);
        img_grid.setAdapter(gridAdapter);
        img_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                PhotoPreview.builder()
                        .setPhotos(imgData)
                        .setCurrentItem(position)
                        .setShowToolbar(false)
                        .setOnLongClickListData(onLongClickListData)
                        .start(PreViewImgActivity.this);
            }
        });
    }

//    @Override
//    public void sendOnLongClick(int position, String path) {
//        Toast.makeText(PreViewImgActivity.this, "你点击了：" + onLongClickListData.get(position) + "，图片路径：" + path, Toast.LENGTH_LONG).show();
//
//    }
}
