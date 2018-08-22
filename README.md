# [PhotoPicker:](https://github.com/walkermanx/PhotoPicker) Android图片选择器

[![PhotoPicker](https://img.shields.io/badge/PhotoPicker-1.0.1-brightgreen.svg?style=plastic)](http://walkermanx.github.io)
[![简书个人页](https://img.shields.io/badge/%E7%AE%80%E4%B9%A6-5Mall-orange.svg?style=plastic&colorA=f2715f&colorB=10b23c)](http://www.jianshu.com/u/b9cbfe0a7f35)


一款Android图片选择器，支持直接拍照、拍照并裁剪、单选裁剪、图片多选、图片放大预览、裁剪比例设置等，满足APP图片拍照、选择等模块需求。

ps:该项目是在[q805699513](https://github.com/q805699513/PhotoPicker)同学仓库的基础上优化修改而来 在此特别感谢 因为项目自定义的需要 所以在其源码的基础上进行了一些优化以及修改以便满足项目需求

## 主要优化如下：

**1.** 更新了glide到最新的4.8.0

**2.** 更新了uCrop依赖到2.2.2

**3.** 更新项目gradle到3.1.4

**4.** 修复原有照片选取页面通过代码设置 ToolBar背景、StatusBar色以及ToolBar文本颜色无效的bug

**5.** 在原有ToolBar StatusBar 以及ToolBar文本颜色可通过代码设置的基础上 新增通过读取theme设置来设置三者的属性数值

**6.** 单选照片且需要剪裁照片时 隐藏照片选取页面照片选择框以及顶部右侧文本

## 引入：

**1.** 在项目的根目录中找到 __`build.gradle`__ 文件 （*不是module的`build.gradle `文件* ），加入如下代码：

   ```groovy
   allprojects {
       repositories {
           maven { url 'https://jitpack.io' }
       }
   }
   ```

**2.** 在你的项目module的`build.gradle `文件中引入依赖：

   ```groovy

    implementation 'com.github.walkermanx:PhotoPicker:1.0.1'

   ```

## 使用：
### 拍照并裁剪
```java
PhotoPicker.builder()
    //直接拍照
    .setOpenCamera(true)
    //拍照后裁剪
    .setCrop(true)
    //设置裁剪比例(X,Y)
    //.setCropXY(1, 1)
    //设置裁剪界面标题栏颜色，设置裁剪界面状态栏颜色
    //.setCropColors(R.color.colorPrimary, R.color.colorPrimaryDark)
    .start(MainActivity.this);
```

### 单选并裁剪
```java
 PhotoPicker.builder()
     //设置图片选择数量
     .setPhotoCount(1)
     //取消选择时点击图片浏览
     .setPreviewEnabled(false)
     //开启裁剪
     .setCrop(true)
     //设置裁剪比例(X,Y)
     .setCropXY(1, 1)
     //设置裁剪界面标题栏颜色，设置裁剪界面状态栏颜色
     .setCropColors(R.color.colorPrimary, R.color.colorPrimaryDark)
     .start(MainActivity.this);
```

### 图片多选
```java
 private ArrayList<String> selectedPhotos = new ArrayList<>();
 PhotoPicker.builder()
        //设置选择个数
        .setPhotoCount(9)
        //选择界面第一个显示拍照按钮
        .setShowCamera(true)
        //选择时点击图片放大浏览
        .setPreviewEnabled(true)
        //附带已经选中过的图片
        .setSelected(selectedPhotos)
        .start(MainActivity.this);    
        
        //多选返回图片后对ArrayList<String> selectedPhotos里的数据操作可达到删除所选择的图片。
```

### 大图浏览
```java
  //多选选中图片后点击进入大图浏览界面以及标题栏显示删除按钮
   PhotoPreview.builder()
         //附带已经选中过的图片
         .setPhotos(selectedPhotos)
         //设置要浏览图片的第position张
         .setCurrentItem(position)
         .start(MainActivity.this);
  
  
 //图片浏览,全屏模式
 ArrayList<String> imgData = new ArrayList<>();
 PhotoPreview.builder()
        //设置浏览的图片数据
        .setPhotos(imgData)
        //设置点击后浏览的是第几张图
        .setCurrentItem(position)
        //浏览时不要标题栏  
        //setShowDeleteButton浏览时显示删除按钮.
        .setShowToolbar(false)
        //开启浏览时长按后显示PopuWindow,分享、保存、取消 等，可以自定义。
        .setOnLongClickListData(onLongClickListData)
        .start(PreViewImgActivity.this);   
 ```
        
### 大图浏览长按显示PopuWindow
[使用参考类](https://github.com/walkermanx/PhotoPicker/blob/master/Samples/src/main/java/com/walkermanx/PhotoPickerDemo/PreViewImgActivity.java)
//放大预览后可长按图片进行下载、分享、取消（示例）等（自定义）操作,已将选择事件回调回Activity（可根据需求自定义）
.setOnLongClickListData(onLongClickListData)
### 
```java
        private ArrayList<String> onLongClickListData = new ArrayList<>();
        //activity或者fragment里图片浏览时使用
        onLongClickListData.add("分享");
        onLongClickListData.add("保存");
        onLongClickListData.add("取消");
        //图片长按后的item点击事件回调
        PhotoOnLongClickManager photoOnLongClickManager = PhotoOnLongClickManager.getInstance();
        photoOnLongClickManager.setOnLongClickListener(new PhotoOnLongClick() {
            @Override
            public void sendOnLongClick(int position, String path) {
            //自己实现分享或者保存等自定义操作
                Toast.makeText(PreViewImgActivity.this, "你点击了：" + onLongClickListData.get(position) + "，图片路径：" + path, Toast.LENGTH_LONG).show();
            }
        });
        
      //图片浏览API设置
      //.setOnLongClickListData(onLongClickListData)
 
 ```
 
### 图片返回 
```java
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
        //拍照功能或者裁剪后返回
        if (resultCode == RESULT_OK && requestCode == PhotoPicker.CROP_CODE) {
            iv_crop.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            Glide.with(getApplicationContext()).load(Uri.fromFile(new File(data.getStringExtra(PhotoPicker.KEY_CAMEAR_PATH)))).into(iv_crop);
        }
    }
```

### manifest //设置权限以及注册Activity
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    >
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.CAMERA" />
  <application
    ...
    >
    ...
       <!--@style/PhotoTheme是图片选择界面样式，文档有提供，不要漏掉添加到values/styles里-->
       <activity
            android:name="com.walkermanx.photopicker.PhotoPickerActivity"
            android:screenOrientation="portrait"
            android:theme="@style/PhotoTheme" />

        <activity
            android:name="com.walkermanx.photopicker.PhotoPagerActivity"
            android:screenOrientation="portrait"/>

        <activity
            android:name="com.yalantis.ucrop.UCropActivity"
            android:screenOrientation="portrait"/>
    
  </application>
</manifest>
```
### 自定义设置样式 设置图片选择界面样式(@style/PhotoTheme样式) 请通过color引用设置颜色值

values/styles.xml
```xml
    <style name="PhotoTheme" parent="Theme.AppCompat.Light.NoActionBar">
        <!--toolBar背景-->
        <item name="colorPrimary">@color/colorPrimary</item>
        <!--status bar 背景-->
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <!--toolBar 文本/ icon 着色-->
        <item name="colorControlNormal">@color/colorControlNormal</item>
    </style>

```

### Proguard

```
# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

如果你的 target API 低于 Android API 27，请添加：
```pro
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder
VideoDecoder 使用 API 27 的一些接口，这可能导致 proguard 发出警告，尽管这些 API 在旧版 Android 设备上根本不会被调用。

如果你使用 DexGuard 你可能还需要添加：

# for DexGuard only
-keepresourcexmlelements manifest/application/meta-data@value=GlideModule [详细配置请查看glide官网文档](https://muyangmin.github.io/glide-docs-cn/doc/download-setup.html)


# nineoldandroids
-keep interface com.nineoldandroids.view.** { *; }
-dontwarn com.nineoldandroids.**
-keep class com.nineoldandroids.** { *; }
# support-v7-appcompat
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }
-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}
# support-design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }
# ucrop
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }
-ignorewarnings
```

---

## Thanks
* [uCrop](https://github.com/Yalantis/uCrop)
* [Glide](https://github.com/bumptech/glide)
* [PhotoPicker](https://github.com/donglua/PhotoPicker)
* [q805699513](https://github.com/q805699513/PhotoPicker)

## License
```text

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```



