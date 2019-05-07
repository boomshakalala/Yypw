package cn.sinata.xldutils.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Paint.Style
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

/**
 * 裁剪视图
 * @author sinata
 */
class ClipImageBorderView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {

    enum class Mode {
        Circle, Rect
    }

    private var mode = Mode.Rect//默认矩形模式
    /**
     * 水平方向与View的边距
     */
    private var mHorizontalPadding: Int = 0
    /**
     * 垂直方向与View的边距
     */
    private var mVerticalPadding: Int = 0
    /**
     * 绘制的矩形的宽度
     */
    private var mWidth: Int = 0
    /**
     * 边框的颜色，默认为白色
     */
    private val mBorderColor = Color.parseColor("#FFFFFF")
    /**
     * 边框的宽度 单位dp
     */
    private var mBorderWidth = 1

    private val mPaint: Paint
    private var scale = 1f

    init {

        mBorderWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, mBorderWidth.toFloat(), resources
                .displayMetrics).toInt()
        mPaint = Paint()
        mPaint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 计算矩形区域的宽度
        mWidth = width - 2 * mHorizontalPadding
        // 计算距离屏幕垂直边界 的边距
        mVerticalPadding = (height - (mWidth / scale).toInt()) / 2
        mPaint.color = Color.parseColor("#aa000000")
        mPaint.style = Style.FILL

        if (mode == Mode.Circle) {
            drawLiftUp(canvas)
            drawRightUp(canvas)
            drawLiftDown(canvas)
            drawRightDown(canvas)
            // 绘制外边框
            mPaint.color = mBorderColor
            mPaint.strokeWidth = mBorderWidth.toFloat()
            mPaint.style = Style.STROKE
            canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), (mWidth / 2).toFloat(), mPaint)
        } else {
            // 绘制左边1
            canvas.drawRect(0f, 0f, mHorizontalPadding.toFloat(), height.toFloat(), mPaint)
            // 绘制右边2
            canvas.drawRect((width - mHorizontalPadding).toFloat(), 0f, width.toFloat(),
                    height.toFloat(), mPaint)
            // 绘制上边3
            canvas.drawRect(mHorizontalPadding.toFloat(), 0f, (width - mHorizontalPadding).toFloat(),
                    mVerticalPadding.toFloat(), mPaint)
            // 绘制下边4
            canvas.drawRect(mHorizontalPadding.toFloat(), (height - mVerticalPadding).toFloat(),
                    (width - mHorizontalPadding).toFloat(), height.toFloat(), mPaint)
            // 绘制外边框
            mPaint.color = mBorderColor
            mPaint.strokeWidth = mBorderWidth.toFloat()
            mPaint.style = Style.STROKE
            canvas.drawRect(mHorizontalPadding.toFloat(), mVerticalPadding.toFloat(), (width - mHorizontalPadding).toFloat(), (height - mVerticalPadding).toFloat(), mPaint)
        }
    }

    fun setHorizontalPadding(mHorizontalPadding: Int) {
        this.mHorizontalPadding = mHorizontalPadding

    }

    /**
     * 设置宽高比
     * @param scale
     */
    fun setImageScale(scale: Float) {
        this.scale = scale
    }

    /**
     * 设置裁剪显示区域模式圆或矩形。默认矩形。
     * @param mode
     */
    fun setMode(mode: Mode) {
        this.mode = mode
    }

    private fun drawLiftUp(canvas: Canvas) {

        val path = Path()
        path.moveTo((width / 2).toFloat(), mVerticalPadding.toFloat())
        path.lineTo((width / 2).toFloat(), 0f)
        path.lineTo(0f, 0f)
        path.lineTo(0f, (height / 2).toFloat())
        path.lineTo(mHorizontalPadding.toFloat(), (height / 2).toFloat())
        path.arcTo(RectF(
                mHorizontalPadding.toFloat(),
                mVerticalPadding.toFloat(),
                (width - mHorizontalPadding).toFloat(),
                (height - mVerticalPadding).toFloat()),
                180f,
                90f)
        path.close()
        canvas.drawPath(path, mPaint)

    }

    private fun drawLiftDown(canvas: Canvas) {
        val path = Path()
        path.moveTo(mHorizontalPadding.toFloat(), (height / 2).toFloat())
        path.lineTo(0f, (height / 2).toFloat())
        path.lineTo(0f, height.toFloat())
        path.lineTo((width / 2).toFloat(), height.toFloat())
        path.lineTo((width / 2).toFloat(), (height - mVerticalPadding).toFloat())
        path.arcTo(RectF(
                mHorizontalPadding.toFloat(),
                mVerticalPadding.toFloat(),
                (width - mHorizontalPadding).toFloat(),
                (height - mVerticalPadding).toFloat()),
                90f,
                90f)
        path.close()
        canvas.drawPath(path, mPaint)
    }

    private fun drawRightDown(canvas: Canvas) {
        val path = Path()
        path.moveTo((width / 2).toFloat(), (height - mVerticalPadding).toFloat())
        path.lineTo((width / 2).toFloat(), height.toFloat())
        path.lineTo(width.toFloat(), height.toFloat())
        path.lineTo(width.toFloat(), (height / 2).toFloat())
        path.lineTo((width - mHorizontalPadding).toFloat(), (height / 2).toFloat())
        path.arcTo(RectF(
                mHorizontalPadding.toFloat(),
                mVerticalPadding.toFloat(),
                (width - mHorizontalPadding).toFloat(),
                (height - mVerticalPadding).toFloat()),
                0f,
                90f)
        path.close()
        canvas.drawPath(path, mPaint)
    }

    private fun drawRightUp(canvas: Canvas) {
        val path = Path()
        path.moveTo((width - mHorizontalPadding).toFloat(), (height / 2).toFloat())
        path.lineTo(width.toFloat(), (height / 2).toFloat())
        path.lineTo(width.toFloat(), 0f)
        path.lineTo((width / 2).toFloat(), 0f)
        path.lineTo((width / 2).toFloat(), mVerticalPadding.toFloat())
        path.arcTo(RectF(
                mHorizontalPadding.toFloat(),
                mVerticalPadding.toFloat(),
                (width - mHorizontalPadding).toFloat(),
                (height - mVerticalPadding).toFloat()),
                270f,
                90f)
        path.close()
        canvas.drawPath(path, mPaint)
    }
}
