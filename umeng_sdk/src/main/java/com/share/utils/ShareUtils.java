package com.share.utils;

import android.app.Activity;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.ShareContent;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

/**
 *
 */
public class ShareUtils {

    public static void share(Activity context, String content, String title, String tagUrl){
        final SHARE_MEDIA[] displayList = new SHARE_MEDIA[]{
                        SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.SINA,
                        SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE
                };
        try {
            int icon = context.getResources().getIdentifier("ic_launcher", "mipmap", context.getPackageName());
            UMImage image = new UMImage(context, icon);
            UMWeb web = new UMWeb(tagUrl,title,content,image);
            ShareContent shareContent = new ShareContent();
            shareContent.mMedia = web;
            new ShareAction(context).setDisplayList( displayList )
                    .setShareContent(shareContent)
                    .open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void share(Activity context, String content, String title, String tagUrl, UMImage image, UMShareListener listener){
        final SHARE_MEDIA[] displayList = new SHARE_MEDIA[]{
                        SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.SINA,
                        SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE
                };
        UMWeb web = new UMWeb(tagUrl,title,content,image);
        ShareContent shareContent = new ShareContent();
        shareContent.mMedia = web;
        new ShareAction(context).setDisplayList( displayList )
                .setShareContent(shareContent)
                .setCallback(listener)
                .open();
    }

    public static void share(Activity context, SHARE_MEDIA platform,String content, String title, String tagUrl,UMShareListener listener){
        share( context,  platform, content,  title,  tagUrl, null,listener);
    }
    public static void share(Activity context, SHARE_MEDIA platform,String content, String title, String tagUrl, UMImage image,UMShareListener listener){
        try {
            int icon = context.getResources().getIdentifier("ic_launcher","mipmap",context.getPackageName());
            if (image == null) {

                image = new UMImage(context, icon);
            }
            UMWeb web = new UMWeb(tagUrl,title,content,image);
            ShareContent shareContent = new ShareContent();
            shareContent.mMedia = web;
            new ShareAction(context).setPlatform(platform)
                    .setShareContent(shareContent)
                    .setCallback(listener)
                    .share();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
