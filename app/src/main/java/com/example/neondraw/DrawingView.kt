package com.example.neondraw

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val path = Path()
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
        isDither = true
        color = Color.CYAN
        strokeWidth = 20f
        maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
    }

    private val bitmapPaint = Paint(Paint.DITHER_FLAG)
    private var extraBitmap: Bitmap? = null
    private var extraCanvas: Canvas? = null

    init {
        // BlurMaskFilter requires software layer for consistent neon glow rendering
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setStrokeColor(color: Int) {
        paint.color = color
        invalidate()
    }

    fun setStrokeWidth(width: Float) {
        val w = if (width < 4f) 4f else width
        paint.strokeWidth = w
        paint.maskFilter = BlurMaskFilter(w / 2f, BlurMaskFilter.Blur.NORMAL)
        invalidate()
    }

    fun clear() {
        extraBitmap?.eraseColor(Color.TRANSPARENT)
        path.reset()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            extraBitmap?.recycle()
            extraBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            extraCanvas = Canvas(extraBitmap!!)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        extraBitmap?.let { canvas.drawBitmap(it, 0f, 0f, bitmapPaint) }
        canvas.drawPath(path, paint)
    }

    private var mX = 0f
    private var mY = 0f
    private val touchTolerance = 4f

    private fun touchStart(x: Float, y: Float) {
        path.reset()
        path.moveTo(x, y)
        mX = x
        mY = y
    }

    private fun touchMove(x: Float, y: Float) {
        val dx = abs(x - mX)
        val dy = abs(y - mY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            path.quadTo(mX, mY, (x + mX) / 2f, (y + mY) / 2f)
            mX = x
            mY = y
        }
    }

    private fun touchUp() {
        path.lineTo(mX, mY)
        extraCanvas?.drawPath(path, paint)
        path.reset()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart(x, y)
            MotionEvent.ACTION_MOVE -> touchMove(x, y)
            MotionEvent.ACTION_UP -> touchUp()
        }
        invalidate()
        return true
    }
}
