package com.walkermanx;

import android.annotation.TargetApi;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.walkermanx.photopicker.R;

import static com.walkermanx.Constants.EXTRA_STATUSBARCOLOR;
import static com.walkermanx.Constants.EXTRA_TITLE_MARGIN_START;
import static com.walkermanx.Constants.EXTRA_TOOLBARCOLOR;
import static com.walkermanx.Constants.EXTRA_TOOLBAR_WIDGET_COLOR;


/**
 * Created by 5Mall<zhangwei> on 2018/8/22
 * Email:zhangwei@qingsongchou.com
 * 描述：
 */
public class BaseActivity extends AppCompatActivity {

    public int toolbarColor;
    protected int statusbarColor;
    public int toolbarWidgetColor;
    protected int titleMarginStart;
    private boolean isManual;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        titleMarginStart = getIntent().getIntExtra(EXTRA_TITLE_MARGIN_START, R.dimen.__picker_toolbar_title_margin_start);

        int toolbarWidgetColorVal;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbarWidgetColorVal = getResId(android.R.attr.colorControlNormal);
            toolbarColor = getIntent().getIntExtra(EXTRA_TOOLBARCOLOR, getResId(android.R.attr.colorPrimary));
            statusbarColor = getIntent().getIntExtra(EXTRA_STATUSBARCOLOR, getResId(android.R.attr.colorPrimaryDark));
        } else {
            toolbarWidgetColorVal = getResId(R.attr.colorControlNormal);
            toolbarColor = getIntent().getIntExtra(EXTRA_TOOLBARCOLOR, getResId(R.attr.colorPrimary));
            statusbarColor = getIntent().getIntExtra(EXTRA_STATUSBARCOLOR, getResId(R.attr.colorPrimaryDark));
        }

        toolbarWidgetColor = getIntent().getIntExtra(EXTRA_TOOLBAR_WIDGET_COLOR, -1);

        if (toolbarWidgetColor == -1) { //若无手动设置该属性 则从主题中取值
            toolbarWidgetColor = toolbarWidgetColorVal;
        } else {
            //判断为用户同过代码手动设置该数值
            isManual = true;
        }

        //状态栏颜色设置
        setStatusBarColor(ContextCompat.getColor(this, statusbarColor));
    }


    protected int getResId(int attrVal) {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attrVal, typedValue, false);
        return typedValue.data;
    }

    /**
     * 判断为用户是否通过过代码手动设置tint着色
     */
    protected boolean isManual() {
        return isManual;
    }


    /**
     * Sets status-bar color for L devices.
     *
     * @param color - status-bar color
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void setStatusBarColor(@ColorInt int color) {
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
    protected void applyTint(Toolbar mToolbar, @ColorInt int mToolbarWidgetColor) {
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
}
