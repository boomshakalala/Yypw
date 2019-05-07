package com.hbcx.user.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.hbcx.user.R;


/**
 * Created by lmw on 2016/11/8.
 */

public class FlowLayout extends ViewGroup{
    private float mVerticalSpacing; //每个item纵向间距
    private float mHorizontalSpacing; //每个item横向间距
    public FlowLayout(Context context) {
        this(context,null);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inial(context,attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
        inial(context,attrs);
    }

    public void setHorizontalSpacing(float pixelSize) {
        mHorizontalSpacing = pixelSize;
    }
    public void setVerticalSpacing(float pixelSize) {
        mVerticalSpacing = pixelSize;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取容器设置的padding
        int mPaddingLeft = getPaddingLeft();
        int mPaddingRight = getPaddingRight();
        int mPaddingTop = getPaddingTop();
        int mPaddingBottom = getPaddingBottom();
        //获取布局要求给的宽度大小
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        //获取布局要求给的 高度的mode
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //获取布局要求给的 高度大小
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //容器存放所有子View所需的总高度
        int totalHeight = mPaddingTop + mPaddingBottom;
        //某行已使用的宽度
        int lineUsedSize = mPaddingLeft + mPaddingRight;
        //用来保存某行最后占的高度，需要取同一行中最高的子View的高度
        int lineHeight =  0;

        //循环遍历每一个子View
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if(child.getVisibility() == GONE){
                //如果子View被设置为隐藏，就跳过这个子View
                continue;
            }
            //让子View自己去测量自己，测量之后就可以调用子View的getMeasureWidth方法获取到测量宽
            measureChild(child,widthMeasureSpec,heightMeasureSpec);
            int childWidth =  child.getMeasuredWidth(); //获取子View的测量宽
            int childHeight = child.getMeasuredHeight(); //获取子View的测量高
            //获取子View的布局参数LayoutParams
            MarginLayoutParams mlps = (MarginLayoutParams) child.getLayoutParams();
            //计算这个孩子一共需要多少空间
            int spaceWidth = childWidth + mlps.leftMargin + mlps.rightMargin;
            int spaceHeight = childHeight + mlps.topMargin + mlps.bottomMargin;

            if(lineUsedSize + spaceWidth <= widthSize){
                lineUsedSize += spaceWidth + mHorizontalSpacing;
                //只要子View的高度大与之前保存的行高，就将子View的高度赋值给行高
                if(spaceHeight > lineHeight){
                    lineHeight = spaceHeight;
                }
            }else{ //这个子View不够摆放，将放到下一行
                totalHeight += lineHeight + mVerticalSpacing;
                lineUsedSize = mPaddingLeft + mPaddingRight + spaceWidth;
                lineHeight = spaceHeight;
            }
        }
        if(heightMode == MeasureSpec.EXACTLY){
            totalHeight = heightSize;
        }else{
            totalHeight += lineHeight;
        }
        setMeasuredDimension(widthSize,totalHeight);
    }

    /**
     * 给子控件布局，也就是设置子控件在容器中的位置
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //获取给容器设置的padding
        int mPaddingLeft = getPaddingLeft();
        int mPaddingRight = getPaddingRight();
        int mPaddingTop = getPaddingTop();
        //子View
        int lineX = mPaddingLeft;
        int lineY = mPaddingTop;
        int lineWidth = r - l;
        //某行已使用的宽度
        int lineUsed = mPaddingLeft + mPaddingRight;
        int lineHeight = 0;
        for (int i = 0; i < this.getChildCount(); i++) {
            View child = this.getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            int childWidth =  child.getMeasuredWidth(); //获取子View的测量宽
            int childHeight = child.getMeasuredHeight(); //获取子View的测量高
            //获取子View的布局参数LayoutParams
            MarginLayoutParams mlps = (MarginLayoutParams) child.getLayoutParams();
            //计算这个孩子一共需要多少空间
            int spaceWidth = childWidth + mlps.leftMargin + mlps.rightMargin;
            int spaceHeight = childHeight + mlps.topMargin + mlps.bottomMargin;

            if(lineUsed + spaceWidth > lineWidth){ //不够放了
                lineY += lineHeight + mVerticalSpacing;
                //变量的重新设置
                lineUsed = mPaddingLeft + mPaddingRight;
                lineX = mPaddingLeft;
                lineHeight = 0;
            }
            //设置子View在容器中的位置
            child.layout(lineX+mlps.leftMargin,lineY+mlps.topMargin,lineX+mlps.leftMargin+childWidth,lineY+mlps.topMargin+childHeight);
            if(spaceHeight > lineHeight){
                lineHeight = spaceHeight;
            }
            lineUsed += spaceWidth + mHorizontalSpacing;
            lineX += spaceWidth + mHorizontalSpacing;
        }
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs)
    {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(super.generateDefaultLayoutParams());
    }

    /**
     * 初始化自定义属性
     * @param context
     * @param attrs
     */
    private void inial(Context context,AttributeSet attrs)  {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        float horizontal = typedArray.getDimension(R.styleable.FlowLayout_HorizontalSpacing,15);
        float verticel = typedArray.getDimension(R.styleable.FlowLayout_VerticalSpacing,15);
        setHorizontalSpacing(horizontal);
        setVerticalSpacing(verticel);
        typedArray.recycle();

    }
}
