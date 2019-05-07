package cn.sinata.xldutils.view.gesture

import android.view.MotionEvent

/**
 * Component that detects translation, scale and rotation based on touch events.
 *
 *
 * This class notifies its listeners whenever a gesture begins, updates or ends.
 * The instance of this detector is passed to the listeners, so it can be queried
 * for pivot, translation, scale or rotation.
 */
class TransformGestureDetector(private val mDetector: MultiPointerGestureDetector) : MultiPointerGestureDetector.Listener {

    /** The listener for receiving notifications when gestures occur.  */
    interface Listener {
        /** Responds to the beginning of a gesture.  */
        fun onGestureBegin(detector: TransformGestureDetector)

        /** Responds to the update of a gesture in progress.  */
        fun onGestureUpdate(detector: TransformGestureDetector)

        /** Responds to the end of a gesture.  */
        fun onGestureEnd(detector: TransformGestureDetector)
    }

    private var mListener: Listener? = null

    init {
        mDetector.setListener(this)
    }

    /**
     * Sets the listener.
     * @param listener listener to set
     */
    fun setListener(listener: Listener) {
        mListener = listener
    }

    /**
     * Resets the component to the initial state.
     */
    fun reset() {
        mDetector.reset()
    }

    /**
     * Handles the given motion event.
     * @param event event to handle
     * *
     * @return whether or not the event was handled
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        return mDetector.onTouchEvent(event)
    }

    override fun onGestureBegin(detector: MultiPointerGestureDetector) {
        if (mListener != null) {
            mListener!!.onGestureBegin(this)
        }
    }

    override fun onGestureUpdate(detector: MultiPointerGestureDetector) {
        if (mListener != null) {
            mListener!!.onGestureUpdate(this)
        }
    }

    override fun onGestureEnd(detector: MultiPointerGestureDetector) {
        if (mListener != null) {
            mListener!!.onGestureEnd(this)
        }
    }

    private fun calcAverage(arr: FloatArray, len: Int): Float {
        val sum = (0..len - 1)
                .map { arr[it] }
                .sum()
        return if (len > 0) sum / len else 0f
    }

    /** Restarts the current gesture  */
    fun restartGesture() {
        mDetector.restartGesture()
    }

    /** Gets whether gesture is in progress or not  */
    val isGestureInProgress: Boolean
        get() = mDetector.isGestureInProgress

    /** Gets the X coordinate of the pivot point  */
    val pivotX: Float
        get() = calcAverage(mDetector.startX, mDetector.count)

    /** Gets the Y coordinate of the pivot point  */
    val pivotY: Float
        get() = calcAverage(mDetector.startY, mDetector.count)

    /** Gets the X component of the translation  */
    val translationX: Float
        get() = calcAverage(mDetector.currentX, mDetector.count) - calcAverage(mDetector.startX, mDetector.count)

    /** Gets the Y component of the translation  */
    val translationY: Float
        get() = calcAverage(mDetector.currentY, mDetector.count) - calcAverage(mDetector.startY, mDetector.count)

    /** Gets the scale  */
    val scale: Float
        get() {
            if (mDetector.count < 2) {
                return 1f
            } else {
                val startDeltaX = mDetector.startX[1] - mDetector.startX[0]
                val startDeltaY = mDetector.startY[1] - mDetector.startY[0]
                val currentDeltaX = mDetector.currentX[1] - mDetector.currentX[0]
                val currentDeltaY = mDetector.currentY[1] - mDetector.currentY[0]
                val startDist = Math.hypot(startDeltaX.toDouble(), startDeltaY.toDouble()).toFloat()
                val currentDist = Math.hypot(currentDeltaX.toDouble(), currentDeltaY.toDouble()).toFloat()
                return currentDist / startDist
            }
        }

    /** Gets the rotation in radians  */
    val rotation: Float
        get() {
            if (mDetector.count < 2) {
                return 0f
            } else {
                val startDeltaX = mDetector.startX[1] - mDetector.startX[0]
                val startDeltaY = mDetector.startY[1] - mDetector.startY[0]
                val currentDeltaX = mDetector.currentX[1] - mDetector.currentX[0]
                val currentDeltaY = mDetector.currentY[1] - mDetector.currentY[0]
                val startAngle = Math.atan2(startDeltaY.toDouble(), startDeltaX.toDouble()).toFloat()
                val currentAngle = Math.atan2(currentDeltaY.toDouble(), currentDeltaX.toDouble()).toFloat()
                return currentAngle - startAngle
            }
        }

    companion object {

        /** Factory method that creates a new instance of TransformGestureDetector  */
        fun newInstance(): TransformGestureDetector {
            return TransformGestureDetector(MultiPointerGestureDetector.newInstance())
        }
    }
}