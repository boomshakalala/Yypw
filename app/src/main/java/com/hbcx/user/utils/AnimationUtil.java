package com.hbcx.user.utils;

import android.animation.ObjectAnimator;
import android.view.View;


/**
 * 动画工具类
 *
 * @author ZYJ
 *         create at 2016/6/12 0012 16:45
 */
public class AnimationUtil {

    public static ObjectAnimator hiddenOrShowDriverInfo(View view, float start, float end) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationY", start, end).setDuration(300);
        objectAnimator.start();
        return objectAnimator;
    }

    public static ObjectAnimator beatingPoint(View view) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationY", 0, -30, 0).setDuration(600);
        objectAnimator.start();
        return objectAnimator;
    }
}
