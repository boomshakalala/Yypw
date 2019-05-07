package cn.sinata.xldutils.view.util

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent

import cn.sinata.xldutils.view.gesture.TransformGestureDetector

/**
 * Zoomable controller that calculates transformation based on touch events.
 */
class DefaultZoomableController(private val mGestureDetector: TransformGestureDetector) : ZoomableController, TransformGestureDetector.Listener {

    private var mListener: ZoomableController.Listener? = null

    /** Returns whether the controller is enabled or not.  */
    /** Sets whether the controller is enabled or not.  */
    override var isEnabled = false
        set(enabled) {
            field = enabled
            if (!enabled) {
                reset()
            }
        }
    /** Gets whether the rotation gesture is enabled or not.  */
    /** Sets whether the rotation gesture is enabled or not.  */
    var isRotationEnabled = false
    /** Gets whether the scale gesture is enabled or not.  */
    /** Sets whether the scale gesture is enabled or not.  */
    var isScaleEnabled = true
    /** Gets whether the translations gesture is enabled or not.  */
    /** Sets whether the translation gesture is enabled or not.  */
    var isTranslationEnabled = true

    /** Gets the minimum scale factor allowed.  */
    /**
     * Sets the minimum scale factor allowed.
     *
     *
     * Note that the hierarchy performs scaling as well, which
     * is not accounted here, so the actual scale factor may differ.
     */
    var minScaleFactor = 1.0f
    /** Gets the maximum scale factor allowed.  */
    /**
     * Sets the maximum scale factor allowed.
     *
     *
     * Note that the hierarchy performs scaling as well, which
     * is not accounted here, so the actual scale factor may differ.
     */
    var maxScaleFactor = java.lang.Float.POSITIVE_INFINITY

    /** Gets the view bounds.  */
    val viewBounds = RectF()
    /** Gets the image bounds before zoomable transformation is applied.  */
    val imageBounds = RectF()
    private val mTransformedImageBounds = RectF()
    private val mPreviousTransform = Matrix()
    /**
     * Gets the zoomable transformation
     * Internal matrix is exposed for performance reasons and is not to be modified by the callers.
     */
    /**
     * Sets the zoomable transformation. Cancels the current gesture if one is happening.
     */
    override var transform = Matrix()
        set(activeTransform) {
            if (mGestureDetector.isGestureInProgress) {
                mGestureDetector.reset()
            }
            transform.set(activeTransform)
        }
    private val mActiveTransformInverse = Matrix()
    private val mTempValues = FloatArray(9)

    init {
        mGestureDetector.setListener(this)
    }

    override fun setListener(listener: ZoomableController.Listener?) {
        mListener = listener
    }

    /** Rests the controller.  */
    fun reset() {
        mGestureDetector.reset()
        mPreviousTransform.reset()
        transform.reset()
    }

    /** Sets the image bounds before zoomable transformation is applied.  */
    override fun setImageBounds(imageBounds: RectF) {
        this.imageBounds.set(imageBounds)
    }

    /** Sets the view bounds.  */
    override fun setViewBounds(viewBounds: RectF) {
        this.viewBounds.set(viewBounds)
    }

    /**
     * Maps point from the view's to the image's relative coordinate system.
     * This takes into account the zoomable transformation.
     */
    fun mapViewToImage(viewPoint: PointF): PointF {
        val points = mTempValues
        points[0] = viewPoint.x
        points[1] = viewPoint.y
        transform.invert(mActiveTransformInverse)
        mActiveTransformInverse.mapPoints(points, 0, points, 0, 1)
        mapAbsoluteToRelative(points, points, 1)
        return PointF(points[0], points[1])
    }

    /**
     * Maps point from the image's relative to the view's coordinate system.
     * This takes into account the zoomable transformation.
     */
    fun mapImageToView(imagePoint: PointF): PointF {
        val points = mTempValues
        points[0] = imagePoint.x
        points[1] = imagePoint.y
        mapRelativeToAbsolute(points, points, 1)
        transform.mapPoints(points, 0, points, 0, 1)
        return PointF(points[0], points[1])
    }

    /**
     * Maps array of 2D points from absolute to the image's relative coordinate system,
     * and writes the transformed points back into the array.
     * Points are represented by float array of [x0, y0, x1, y1, ...].

     * @param destPoints destination array (may be the same as source array)
     * *
     * @param srcPoints source array
     * *
     * @param numPoints number of points to map
     */
    private fun mapAbsoluteToRelative(destPoints: FloatArray, srcPoints: FloatArray, numPoints: Int) {
        for (i in 0..numPoints - 1) {
            destPoints[i * 2 + 0] = (srcPoints[i * 2 + 0] - imageBounds.left) / imageBounds.width()
            destPoints[i * 2 + 1] = (srcPoints[i * 2 + 1] - imageBounds.top) / imageBounds.height()
        }
    }

    /**
     * Maps array of 2D points from relative to the image's absolute coordinate system,
     * and writes the transformed points back into the array
     * Points are represented by float array of [x0, y0, x1, y1, ...].

     * @param destPoints destination array (may be the same as source array)
     * *
     * @param srcPoints source array
     * *
     * @param numPoints number of points to map
     */
    private fun mapRelativeToAbsolute(destPoints: FloatArray, srcPoints: FloatArray, numPoints: Int) {
        for (i in 0..numPoints - 1) {
            destPoints[i * 2 + 0] = srcPoints[i * 2 + 0] * imageBounds.width() + imageBounds.left
            destPoints[i * 2 + 1] = srcPoints[i * 2 + 1] * imageBounds.height() + imageBounds.top
        }
    }

    /** Notifies controller of the received touch event.   */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isEnabled) {
            return mGestureDetector.onTouchEvent(event)
        }
        return false
    }

    /**
     * Zooms to the desired scale and positions the view so that imagePoint is in the center.
     *
     *
     * It might not be possible to center imagePoint (= a corner for e.g.), in those cases the view
     * will be adjusted so that there are no black bars in it.
     * Resets any previous transform and cancels the current gesture if one is happening.

     * @param scale desired scale, will be limited to {min, max} scale factor
     * *
     * @param imagePoint 2D point in image's relative coordinate system (i.e. 0 <= x, y <= 1)
     */
    fun zoomToImagePoint(scale: Float, imagePoint: PointF) {
        var nScale = scale
        if (mGestureDetector.isGestureInProgress) {
            mGestureDetector.reset()
        }
        nScale = limit(nScale, minScaleFactor, maxScaleFactor)
        val points = mTempValues
        points[0] = imagePoint.x
        points[1] = imagePoint.y
        mapRelativeToAbsolute(points, points, 1)
        transform.setScale(nScale, nScale, points[0], points[1])
        transform.postTranslate(
                viewBounds.centerX() - points[0],
                viewBounds.centerY() - points[1])
        limitTranslation()
    }

    /* TransformGestureDetector.Listener methods  */

    override fun onGestureBegin(detector: TransformGestureDetector) {
        mPreviousTransform.set(transform)
    }

    override fun onGestureUpdate(detector: TransformGestureDetector) {
        transform.set(mPreviousTransform)
        if (isRotationEnabled) {
            val angle = detector.rotation * (180 / Math.PI).toFloat()
            transform.postRotate(angle, detector.pivotX, detector.pivotY)
        }
        if (isScaleEnabled) {
            val scale = detector.scale
            transform.postScale(scale, scale, detector.pivotX, detector.pivotY)
        }
        limitScale(detector.pivotX, detector.pivotY)
        if (isTranslationEnabled) {
            transform.postTranslate(detector.translationX, detector.translationY)
        }
        if (limitTranslation()) {
            mGestureDetector.restartGesture()
        }
        if (mListener != null) {
            mListener!!.onTransformChanged(transform)
        }
    }

    override fun onGestureEnd(detector: TransformGestureDetector) {
        mPreviousTransform.set(transform)
    }

    /** Gets the current scale factor.  */
    override val scaleFactor: Float
        get() {
            transform.getValues(mTempValues)
            return mTempValues[Matrix.MSCALE_X]
        }

    private fun limitScale(pivotX: Float, pivotY: Float) {
        val currentScale = scaleFactor
        val targetScale = limit(currentScale, minScaleFactor, maxScaleFactor)
        if (targetScale != currentScale) {
            val scale = targetScale / currentScale
            transform.postScale(scale, scale, pivotX, pivotY)
        }
    }

    /**
     * Keeps the view inside the image if possible, if not (i.e. image is smaller than view)
     * centers the image.
     * @return whether adjustments were needed or not
     */
    private fun limitTranslation(): Boolean {
        val bounds = mTransformedImageBounds
        bounds.set(imageBounds)
        transform.mapRect(bounds)

        val offsetLeft = getOffset(bounds.left, bounds.width(), viewBounds.width())
        val offsetTop = getOffset(bounds.top, bounds.height(), viewBounds.height())
        if (offsetLeft != bounds.left || offsetTop != bounds.top) {
            transform.postTranslate(offsetLeft - bounds.left, offsetTop - bounds.top)
            return true
        }
        return false
    }

    private fun getOffset(offset: Float, imageDimension: Float, viewDimension: Float): Float {
        val diff = viewDimension - imageDimension
        return if (diff > 0) diff / 2 else limit(offset, diff, 0f)
    }

    private fun limit(value: Float, min: Float, max: Float): Float {
        return Math.min(Math.max(min, value), max)
    }

    companion object {

        fun newInstance(): DefaultZoomableController {
            return DefaultZoomableController(TransformGestureDetector.newInstance())
        }
    }

}