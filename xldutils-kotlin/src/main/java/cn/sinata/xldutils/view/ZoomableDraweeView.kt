package cn.sinata.xldutils.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Animatable
import android.util.AttributeSet
import android.view.MotionEvent
import cn.sinata.xldutils.view.util.DefaultZoomableController
import cn.sinata.xldutils.view.util.ZoomableController
import com.facebook.common.internal.Preconditions
import com.facebook.drawee.controller.AbstractDraweeController
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.GenericDraweeView

class ZoomableDraweeView : GenericDraweeView, ZoomableController.Listener {

    private val mImageBounds = RectF()
    private val mViewBounds = RectF()
    private val mControllerListener = object : BaseControllerListener<Any>() {
        override fun onFinalImageSet(
                id: String?,
                imageInfo: Any?,
                animatable: Animatable?) {
            this@ZoomableDraweeView.onFinalImageSet()
        }
        override fun onRelease(id: String?) {
            this@ZoomableDraweeView.onRelease()
        }
    }

    private var mHugeImageController: DraweeController? = null
    private var mZoomableController = DefaultZoomableController.newInstance()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        mZoomableController.setListener(this)
    }

    fun setZoomableController(zoomableController: ZoomableController) {
        Preconditions.checkNotNull(zoomableController)
        mZoomableController.setListener(null)
        mZoomableController = zoomableController as DefaultZoomableController
        mZoomableController.setListener(this)
    }

    override fun setController(controller: DraweeController?) {
        setControllers(controller, null)
    }

    private fun setControllersInternal(
            controller: DraweeController?,
            hugeImageController: DraweeController?) {
        removeControllerListener(getController())
        addControllerListener(controller)
        mHugeImageController = hugeImageController
        super.setController(controller)
    }

    /**
     * Sets the controllers for the normal and huge image.

     *
     *  IMPORTANT: in order to avoid a flicker when switching to the huge image, the huge image
     * controller should have the normal-image-uri set as its low-res-uri.

     * @param controller controller to be initially used
     * *
     * @param hugeImageController controller to be used after the client starts zooming-in
     */
    fun setControllers(
            controller: DraweeController?,
            hugeImageController: DraweeController?) {
        setControllersInternal(null, null)
        mZoomableController.isEnabled = false
        setControllersInternal(controller, hugeImageController)
    }

    private fun maybeSetHugeImageController() {
        if (mHugeImageController != null && mZoomableController.scaleFactor > HUGE_IMAGE_SCALE_FACTOR_THRESHOLD) {
            setControllersInternal(mHugeImageController, null)
        }
    }

    private fun removeControllerListener(controller: DraweeController?) {
        if (controller!=null) {
            (controller as? AbstractDraweeController<*, *>)?.removeControllerListener(mControllerListener)
        }
    }

    private fun addControllerListener(controller: DraweeController?) {
        if(controller!=null) {
            (controller as? AbstractDraweeController<*, *>)?.addControllerListener(mControllerListener)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val saveCount = canvas.save()
        canvas.concat(mZoomableController.transform)
        super.onDraw(canvas)
        canvas.restoreToCount(saveCount)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if (mZoomableController.onTouchEvent(event)) {
//            if (mZoomableController.scaleFactor > 1.0f) {
//                parent.requestDisallowInterceptTouchEvent(true)
//            }
//            return true
//        }
        return super.onTouchEvent(event)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        updateZoomableControllerBounds()
    }

    private fun onFinalImageSet() {
        if (!mZoomableController.isEnabled) {
            updateZoomableControllerBounds()
            mZoomableController.isEnabled = true
        }
    }

    private fun onRelease() {
        mZoomableController.isEnabled = false
    }

    override fun onTransformChanged(transform: Matrix) {
        maybeSetHugeImageController()
        invalidate()
    }

    private fun updateZoomableControllerBounds() {
        hierarchy.getActualImageBounds(mImageBounds)
        mViewBounds.set(0f, 0f, width.toFloat(), height.toFloat())
        mZoomableController.setImageBounds(mImageBounds)
        mZoomableController.setViewBounds(mViewBounds)
    }

    companion object {
        private val HUGE_IMAGE_SCALE_FACTOR_THRESHOLD = 1.1f
    }
}