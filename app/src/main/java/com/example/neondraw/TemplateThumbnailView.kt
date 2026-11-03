package com.example.neondraw

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class TemplateThumbnailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var stencilType = StencilType.NONE
    private val boundsRect = RectF()

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f
        color = Color.parseColor("#9900FFFF")
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        maskFilter = BlurMaskFilter(3.5f, BlurMaskFilter.Blur.NORMAL)
    }

    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.6f
        color = Color.WHITE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setStencil(type: StencilType, color: Int = Color.parseColor("#00FFFF")) {
        stencilType = type
        glowPaint.color = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (stencilType == StencilType.NONE) return

        val pad = width * 0.12f
        boundsRect.set(pad, pad, width - pad, height - pad)

        val path = TemplateShapes.createPath(stencilType, boundsRect)
        canvas.drawPath(path, glowPaint)
        canvas.drawPath(path, corePaint)
    }
}

