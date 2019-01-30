package com.walkermanx.photopicker.event;
import android.graphics.drawable.Drawable;

/**
 * Created by 5Mall<zhangwei> on 2019/1/30
 * Email:zhangwei@qingsongchou.com
 * 描述：
 */
public interface RequestListener {
    void onLoaded(Drawable resource, int position);
}
