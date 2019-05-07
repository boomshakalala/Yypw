package com.hbcx.user.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.LinearLayout;


/**
 * Created by lmw on 2016/11/25.
 */

public class ViewPagerIndicator {
    private LinearLayout container;
    private int count;
    private Context context;
    private int normalRes;
    private int selectedRes;

    public ViewPagerIndicator(Context context, LinearLayout container,int normalRes,int selectedRes, int count) {
        this.context = context;
        this.container = container;
        this.count = count;
        this.normalRes = normalRes;
        this.selectedRes = selectedRes;
    }

    public void setupWithBanner(ViewPager banner) {
        if (count < 2)
            return;
        container.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView indicator = new ImageView(context);
            indicator.setPadding(10,0,0,0);
            if (i == 0) {
                indicator.setImageResource(selectedRes);
            } else
                indicator.setImageResource(normalRes);
            container.addView(indicator);
        }
        banner.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                for (int i = 0; i < count; i++) {
                    ImageView imageView =  (ImageView) container.getChildAt(i);
                    if (i == position % count) {
                        imageView.setImageResource(selectedRes);
                    } else
                        imageView.setImageResource(normalRes);
                }
            }
        });
    }
}
