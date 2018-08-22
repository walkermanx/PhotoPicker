package com.walkermanx.PhotoPickerDemo;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Created by 5Mall<zhangwei> on 2018/8/20
 * Email:zhangwei@qingsongchou.com
 * 描述：
 */
@GlideModule
public final class MyAppGlideModule extends AppGlideModule {
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
