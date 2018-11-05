package me.andrew.squilla

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.animation.DecelerateInterpolator
import com.almeros.android.multitouch.MoveGestureDetector


class MatrixImageView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) : AppCompatImageView(context, attrs) {

    private val standerMatrix = Matrix()
    private val workMatrix = Matrix()

    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private val movDetector = MoveGestureDetector(context, MovListener())

    private var sourceImg: Bitmap? = null
    private var containerWidth = 0
    private var containerHeight = 0

    private val imgRect = RectF()

    init {
        init()
    }

    private fun init() {
        scaleType = ScaleType.MATRIX
        setOnTouchListener { _, event ->
            {
                scaleDetector.onTouchEvent(event)
                movDetector.onTouchEvent(event)

                if (event.action == MotionEvent.ACTION_UP) {
                    checkBound()
                }
                true
            }()
        }
    }

    fun cropBitmap(): Bitmap? {
        sourceImg ?: return null
        val invMatrix = Matrix()
        workMatrix.invert(invMatrix)

        val destStart = FloatArray(2)
        val destEnd = FloatArray(2)
        val srcStart = floatArrayOf(0f, 0f)
        val srcEnd = floatArrayOf(containerWidth.toFloat(), containerHeight.toFloat())

        invMatrix.mapPoints(destStart, srcStart)
        invMatrix.mapPoints(destEnd, srcEnd)

        return Bitmap.createBitmap(sourceImg, destStart[0].toInt(), destStart[1].toInt(),
                (destEnd[0] - destStart[0]).toInt(), (destEnd[1] - destStart[1]).toInt())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (width > 0 && height > 0) {
            initMatrix(width, height)
        }
    }

    private fun initMatrix(width: Int, height: Int) {
        val rawWidth = drawable?.intrinsicWidth ?: 0
        val rawHeight = drawable?.intrinsicHeight ?: 0
        if (rawWidth == 0 || rawHeight == 0) return
        imgRect.set(0f, 0f, rawWidth.toFloat(), rawHeight.toFloat())

        val maxScale = Math.max(width / rawWidth.toFloat(), height / rawHeight.toFloat())

        standerMatrix.reset()
        standerMatrix.postTranslate((width - rawWidth) / 2f, (height - rawHeight) / 2f)
        standerMatrix.postScale(maxScale, maxScale, width / 2f, height / 2f)
        workMatrix.set(standerMatrix)
        imageMatrix = standerMatrix

        sourceImg = (drawable as? BitmapDrawable)?.bitmap
        containerWidth = width
        containerHeight = height
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            detector ?: return false

            val scaleFactor = detector.scaleFactor
            workMatrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
            imageMatrix = workMatrix
            return true
        }
    }

    private inner class MovListener : MoveGestureDetector.SimpleOnMoveGestureListener() {

        override fun onMove(detector: MoveGestureDetector?): Boolean {
            detector ?: return false

            val delta = detector.focusDelta
            workMatrix.postTranslate(delta.x, delta.y)
            imageMatrix = workMatrix
            return true
        }
    }

    private fun checkBound() {
        val currentRect = RectF()
        workMatrix.mapRect(currentRect, imgRect)
        val scale = Math.max(containerWidth / currentRect.width(), containerHeight / currentRect.height())
        if (scale > 1.01) {
            val tmpMatrix = Matrix()
            val animator = ValueAnimator.ofFloat(1f, scale)
            animator.duration = 150
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener {
                tmpMatrix.set(workMatrix)
                val s = it.animatedValue as Float
                tmpMatrix.postScale(s, s, currentRect.centerX(), currentRect.centerY())
                imageMatrix = tmpMatrix
            }
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    workMatrix.set(tmpMatrix)
                    checkTranslate()
                }
            })
            animator.start()
        } else {
            checkTranslate()
        }
    }

    private fun checkTranslate() {
        val currentRect = RectF()
        workMatrix.mapRect(currentRect, imgRect)
        var transX = when {
            currentRect.right < containerWidth -> containerWidth - currentRect.right
            currentRect.left > 0 -> -currentRect.left
            else -> 0f
        }
        var transY = when {
            currentRect.bottom < containerHeight -> containerHeight - currentRect.bottom
            currentRect.top > 0 -> -currentRect.top
            else -> 0f
        }

        if (Math.abs(transX) < 0.01 && Math.abs(transY) < 0.01) return
        val animator = ValueAnimator.ofFloat(0f, 1f)
        val tempMatrix = Matrix()
        animator.duration = 150
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener {
            tempMatrix.set(workMatrix)
            val s = it.animatedValue as Float
            tempMatrix.postTranslate(transX * s, transY * s)
            imageMatrix = tempMatrix
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                workMatrix.set(tempMatrix)
            }
        })
        animator.start()
    }
}