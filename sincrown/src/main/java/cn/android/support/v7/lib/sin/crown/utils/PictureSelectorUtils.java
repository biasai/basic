package cn.android.support.v7.lib.sin.crown.utils;

import android.app.Activity;
import android.content.Intent;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.util.List;

import cn.android.support.v7.lib.sin.crown.R;

import static android.app.Activity.RESULT_OK;

/**
 * 需要SD卡权限。
 * PictureSelector 图片选择器工具类。
 * Created by 彭治铭 on 2018/4/2.
 */

public class PictureSelectorUtils {
    private static PictureSelectorUtils pictureSelectorUtils;

    public static PictureSelectorUtils getInstance() {
        if (pictureSelectorUtils == null) {
            pictureSelectorUtils = new PictureSelectorUtils();
        }
        return pictureSelectorUtils;
    }

    /**
     * 打開圖片選擇器【视频，图片都有。图片可以裁剪(多张图片也可以裁剪)。】
     *
     * @param activity
     */
    public void openPictureSelector(Activity activity) {
        // 进入相册 以下是例子：用不到的 api 可以不写
        PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofAll())//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .theme(R.style.picture_default_style)//主题样式(不设置为默认样式) 也可参考 demo values/styles 下 例如：R.style.picture.white.style
                .maxSelectNum(9)// 最大图片选择数量 int
                .minSelectNum(0)// 最小选择数量 int
                .imageSpanCount(4)// 每行显示个数 int
                .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                .previewImage(true)// 是否可预览图片 true or false
                .previewVideo(true)// 是否可预览视频 true or false
                .enablePreviewAudio(true) // 是否可播放音频 true or false
                .isCamera(true)// 是否显示拍照按钮 true or false
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认 true
                .sizeMultiplier(0.5f)// glide 加载图片大小 0~1 之间 如设置 .glideOverride()无效
                .setOutputCameraPath("/CustomPath")// 自定义拍照保存路径,可不填
                .enableCrop(true)// 是否裁剪 true or false
                .compress(false)// 是否压缩 true or false
//                .glideOverride()// int glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
//                .withAspectRatio()// int 裁剪比例 如 16:9 3:2 3:4 1:1 可自定义
                .hideBottomControls(true)// 是否显示 uCrop 工具栏，默认不显示 true or false
                .isGif(true)// 是否显示 gif 图片 true or false
                .freeStyleCropEnabled(true)// 裁剪框是否可拖拽 true or false
                .circleDimmedLayer(false)// 是否圆形裁剪 true or false
                .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为 false   true or false
                .showCropGrid(true)// 是否显示裁剪矩形网格 圆形裁剪时建议设为 false    true or false
                .openClickSound(true)// 是否开启点击声音 true or false
                .selectionMedia(null)// 是否传入已选图片 List<LocalMedia> list
                .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中) true or false
                .cropCompressQuality(90)// 裁剪压缩质量 默认 90 int
//                .cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效 int
//                .rotateEnabled() // 裁剪是否可旋转图片 true or false
//                .scaleEnabled()// 裁剪是否可放大缩小图片 true or false
//                .videoQuality()// 视频录制质量 0 or 1 int
                .videoSecond(15)// 显示多少秒以内的视频 or 音频也可适用 int
                .recordVideoSecond(10)//视频秒数录制 默认 60s int
                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调 onActivityResult code
    }

    /**
     * 打開圖片選擇器【只有图片，没有视频，没有裁剪。选中图片就直接返回(最多选9张图片)】
     *
     * @param activity
     */
    public void openGallery(Activity activity) {
        PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofImage())//打开图片选择器。 .openCamera()是打开相机，就是系统相机。这个版本会报错，无法打开相机。我们也不需要。
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }

    /**
     * 回調，在Activity中onActivityResult(int requestCode, int resultCode, Intent data)中調用。
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @return
     */
    public List<LocalMedia> getActivityResult(int requestCode, int resultCode, Intent data) {
        List<LocalMedia> selectList = null;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片选择结果回调
                    selectList = PictureSelector.obtainMultipleResult(data);
                    // 例如 LocalMedia 里面返回三种 path
                    // 1.media.getPath(); 为原图 path
                    // 2.media.getCutPath();为裁剪后 path，需判断 media.isCut();是否为 true
                    // 3.media.getCompressPath();为压缩后 path，需判断 media.isCompressed();是否为 true
                    // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                    break;
            }
        }
        return selectList;
    }


}
