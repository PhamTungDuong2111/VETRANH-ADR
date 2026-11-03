package com.example.neondraw

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

enum class BrushType {
    NEON,
    RAINBOW,
    STARS,
    BEADS,
    RIBBON,
    FIREWORKS
}

enum class SymmetryMode(val count: Int, val label: String) {
    NONE(1, "1x"),
    MIRROR_2(2, "2x"),
    MIRROR_4(4, "4x"),
    KALEIDOSCOPE_8(8, "8x")
}

enum class StencilType(val idName: String, val title: String, val category: String, val emoji: String) {
    NONE("none", "Tự do", "Tất cả", "✏️"),
    HEART("heart", "Trái tim", "Dễ thương", "❤️"),
    STAR("star", "Ngôi sao", "Dễ thương", "⭐"),
    RAINBOW_CLOUD("rainbow_cloud", "Cầu vồng", "Dễ thương", "🌈"),
    CUPCAKE("cupcake", "Bánh kem", "Dễ thương", "🧁"),
    ICE_CREAM("ice_cream", "Kem ốc quế", "Dễ thương", "🍦"),
    STRAWBERRY("strawberry", "Dâu tây", "Dễ thương", "🍓"),

    CAT("cat", "Mèo con", "Động vật", "🐱"),
    DOG("dog", "Cún con", "Động vật", "🐶"),
    BUTTERFLY("butterfly", "Con bướm", "Động vật", "🦋"),
    DINO("dino", "Khủng long", "Động vật", "🦕"),
    DOLPHIN("dolphin", "Cá heo", "Động vật", "🐬"),
    PENGUIN("penguin", "Cánh cụt", "Động vật", "🐧"),

    ROCKET("rocket", "Tên lửa", "Vũ trụ", "🚀"),
    MOON_STARS("moon_stars", "Mặt trăng", "Vũ trụ", "🌙"),
    CROWN("crown", "Vương miện", "Vũ trụ", "👑"),
    DIAMOND("diamond", "Kim cương", "Vũ trụ", "💎"),
    MUSIC("music", "Nốt nhạc", "Vũ trụ", "🎵"),

    LOTUS("lotus", "Hoa sen", "Mandala", "🪷"),
    MANDALA_SUN("mandala_sun", "Mặt trời", "Mandala", "☀️"),
    SPIDER_WEB("spider_web", "Mạng nhện", "Mandala", "🕸️")
}

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class Point(val x: Float, val y: Float)

    data class Stroke(
        val points: List<Point>,
        val color: Int,
        val strokeWidth: Float,
        val brushType: BrushType,
        val symmetryMode: SymmetryMode,
        val isRainbow: Boolean = false
    )

    private val strokes = mutableListOf<Stroke>()
    private val undoneStrokes = mutableListOf<Stroke>()

    // Current stroke being drawn
    private val currentPoints = mutableListOf<Point>()
    private var currentColor = Color.CYAN
    private var currentStrokeWidth = 24f
    private var currentBrushType = BrushType.NEON
    private var currentSymmetryMode = SymmetryMode.NONE
    private var isRainbowMode = false

    // Stencil / Guide
    private var currentStencil = StencilType.NONE
    private var isStencilGuideVisible = true
    private val stencilBounds = RectF()
    private val stencilGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4.5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.parseColor("#9900FFFF")
        maskFilter = BlurMaskFilter(6f, BlurMaskFilter.Blur.NORMAL)
    }
    private val stencilCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.0f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.parseColor("#F0FFFFFF")
    }

    // Background Photo
    private var backgroundBitmap: Bitmap? = null

    // Offscreen buffer for fast rendering
    private var canvasBitmap: Bitmap? = null
    private var drawCanvas: Canvas? = null

    // Glow and Core Paints
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.WHITE
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    var onHistoryChanged: ((canUndo: Boolean, canRedo: Boolean) -> Unit)? = null

    init {
        // Hardware acceleration can disable BlurMaskFilter on some devices; software layer guarantees glow
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setBrushColor(color: Int) {
        currentColor = color
        isRainbowMode = false
    }

    fun setRainbowMode(enabled: Boolean) {
        isRainbowMode = enabled
    }

    fun isRainbow() = isRainbowMode

    fun getBrushColor() = currentColor

    fun setBrushWidth(width: Float) {
        currentStrokeWidth = width.coerceIn(8f, 100f)
    }

    fun getBrushWidth() = currentStrokeWidth

    fun setBrushType(type: BrushType) {
        currentBrushType = type
    }

    fun getBrushType() = currentBrushType

    fun setSymmetryMode(mode: SymmetryMode) {
        currentSymmetryMode = mode
    }

    fun getSymmetryMode() = currentSymmetryMode

    fun setStencil(stencil: StencilType) {
        currentStencil = stencil
        isStencilGuideVisible = (stencil != StencilType.NONE)
        invalidate()
    }

    fun getStencil() = currentStencil

    fun toggleStencilGuideVisibility(): Boolean {
        isStencilGuideVisible = !isStencilGuideVisible
        invalidate()
        return isStencilGuideVisible
    }

    fun isStencilVisible() = isStencilGuideVisible

    fun setBackgroundPhoto(bitmap: Bitmap?) {
        backgroundBitmap = bitmap
        invalidate()
    }

    fun hasBackgroundPhoto() = backgroundBitmap != null

    fun undo() {
        if (strokes.isNotEmpty()) {
            val last = strokes.removeAt(strokes.size - 1)
            undoneStrokes.add(last)
            renderAllStrokes()
            invalidate()
            notifyHistory()
        }
    }

    fun redo() {
        if (undoneStrokes.isNotEmpty()) {
            val restored = undoneStrokes.removeAt(undoneStrokes.size - 1)
            strokes.add(restored)
            renderAllStrokes()
            invalidate()
            notifyHistory()
        }
    }

    fun canUndo() = strokes.isNotEmpty()
    fun canRedo() = undoneStrokes.isNotEmpty()

    fun clear() {
        strokes.clear()
        undoneStrokes.clear()
        currentPoints.clear()
        backgroundBitmap = null
        renderAllStrokes()
        invalidate()
        notifyHistory()
    }

    private fun notifyHistory() {
        onHistoryChanged?.invoke(canUndo(), canRedo())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            canvasBitmap?.recycle()
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            drawCanvas = Canvas(canvasBitmap!!)
            renderAllStrokes()
        }
    }

    private fun renderAllStrokes() {
        drawCanvas?.let { cv ->
            cv.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            for (stroke in strokes) {
                drawStroke(cv, stroke)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Draw black canvas
        canvas.drawColor(Color.BLACK)

        // 2. Draw user photo background if present
        backgroundBitmap?.let { bmp ->
            val src = Rect(0, 0, bmp.width, bmp.height)
            val dst = Rect(0, 0, width, height)
            canvas.drawBitmap(bmp, src, dst, null)
        }

        // 3. Draw committed strokes
        canvasBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        // 4. Draw current in-progress stroke
        if (currentPoints.size >= 2) {
            val tempStroke = Stroke(
                points = currentPoints.toList(),
                color = currentColor,
                strokeWidth = currentStrokeWidth,
                brushType = currentBrushType,
                symmetryMode = currentSymmetryMode,
                isRainbow = isRainbowMode
            )
            drawStroke(canvas, tempStroke)
        }

        // 5. Draw stencil guideline overlay
        if (currentStencil != StencilType.NONE && isStencilGuideVisible) {
            drawStencilGuide(canvas)
        }
    }

    private fun drawStroke(canvas: Canvas, stroke: Stroke) {
        val pts = stroke.points
        if (pts.isEmpty()) return

        val cx = width / 2f
        val cy = height / 2f

        val symCount = stroke.symmetryMode.count
        for (i in 0 until symCount) {
            canvas.save()
            when (stroke.symmetryMode) {
                SymmetryMode.NONE -> {}
                SymmetryMode.MIRROR_2 -> {
                    if (i == 1) {
                        canvas.translate(width.toFloat(), 0f)
                        canvas.scale(-1f, 1f)
                    }
                }
                SymmetryMode.MIRROR_4 -> {
                    val angle = i * 90f
                    canvas.rotate(angle, cx, cy)
                    if (i % 2 == 1) {
                        canvas.scale(1f, -1f, cx, cy)
                    }
                }
                SymmetryMode.KALEIDOSCOPE_8 -> {
                    val angle = i * 45f
                    canvas.rotate(angle, cx, cy)
                    if (i % 2 == 1) {
                        canvas.scale(1f, -1f, cx, cy)
                    }
                }
            }

            renderStrokeBrush(canvas, stroke)
            canvas.restore()
        }
    }

    private fun renderStrokeBrush(canvas: Canvas, stroke: Stroke) {
        val pts = stroke.points
        if (pts.size < 2) return

        when (stroke.brushType) {
            BrushType.NEON -> drawNeonPath(canvas, stroke)
            BrushType.RAINBOW -> drawRainbowPath(canvas, stroke)
            BrushType.STARS -> drawStarPath(canvas, stroke)
            BrushType.BEADS -> drawBeadsPath(canvas, stroke)
            BrushType.RIBBON -> drawRibbonPath(canvas, stroke)
            BrushType.FIREWORKS -> drawFireworksPath(canvas, stroke)
        }
    }

    private fun drawNeonPath(canvas: Canvas, stroke: Stroke) {
        val path = createSmoothedPath(stroke.points)

        // Outer Glow
        glowPaint.color = stroke.color
        glowPaint.strokeWidth = stroke.strokeWidth * 1.8f
        glowPaint.alpha = 150
        glowPaint.maskFilter = BlurMaskFilter(stroke.strokeWidth * 0.9f, BlurMaskFilter.Blur.NORMAL)
        canvas.drawPath(path, glowPaint)

        // Main colored stroke
        glowPaint.strokeWidth = stroke.strokeWidth
        glowPaint.alpha = 255
        glowPaint.maskFilter = BlurMaskFilter(stroke.strokeWidth * 0.35f, BlurMaskFilter.Blur.NORMAL)
        canvas.drawPath(path, glowPaint)

        // Inner glowing white core
        corePaint.strokeWidth = (stroke.strokeWidth * 0.35f).coerceAtLeast(3f)
        corePaint.maskFilter = null
        canvas.drawPath(path, corePaint)
    }

    private fun drawRainbowPath(canvas: Canvas, stroke: Stroke) {
        val pts = stroke.points
        for (i in 0 until pts.size - 1) {
            val p1 = pts[i]
            val p2 = pts[i + 1]
            val hue = (i * 12f) % 360f
            val segColor = Color.HSVToColor(floatArrayOf(hue, 1f, 1f))

            glowPaint.color = segColor
            glowPaint.strokeWidth = stroke.strokeWidth * 1.5f
            glowPaint.alpha = 180
            glowPaint.maskFilter = BlurMaskFilter(stroke.strokeWidth * 0.7f, BlurMaskFilter.Blur.NORMAL)
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, glowPaint)

            glowPaint.strokeWidth = stroke.strokeWidth
            glowPaint.alpha = 255
            glowPaint.maskFilter = null
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, glowPaint)

            corePaint.strokeWidth = (stroke.strokeWidth * 0.3f).coerceAtLeast(2f)
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, corePaint)
        }
    }

    private fun drawStarPath(canvas: Canvas, stroke: Stroke) {
        val pts = stroke.points
        val step = (stroke.strokeWidth * 1.5f).toInt().coerceAtLeast(20)
        var distAccum = 0f

        for (i in 0 until pts.size - 1) {
            val p1 = pts[i]
            val p2 = pts[i + 1]
            val d = hypot(p2.x - p1.x, p2.y - p1.y)
            distAccum += d
            if (distAccum >= step || i == 0) {
                distAccum = 0f
                val col = if (stroke.isRainbow) {
                    Color.HSVToColor(floatArrayOf((i * 18f) % 360f, 1f, 1f))
                } else stroke.color

                drawGlowStar(canvas, p2.x, p2.y, stroke.strokeWidth * 0.9f, col)
            }
        }
    }

    private fun drawGlowStar(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int) {
        val starPath = Path()
        val spikes = 5
        val rot = Math.PI / 2 * 3
        val step = Math.PI / spikes
        val innerRadius = radius * 0.45f

        starPath.moveTo(cx, cy - radius)
        for (i in 0 until spikes) {
            val x1 = (cx + cos(rot + i * 2 * step) * radius).toFloat()
            val y1 = (cy + sin(rot + i * 2 * step) * radius).toFloat()
            starPath.lineTo(x1, y1)
            val x2 = (cx + cos(rot + (i * 2 + 1) * step) * innerRadius).toFloat()
            val y2 = (cy + sin(rot + (i * 2 + 1) * step) * innerRadius).toFloat()
            starPath.lineTo(x2, y2)
        }
        starPath.close()

        fillPaint.color = color
        fillPaint.maskFilter = BlurMaskFilter(radius * 0.6f, BlurMaskFilter.Blur.NORMAL)
        canvas.drawPath(starPath, fillPaint)

        fillPaint.maskFilter = null
        fillPaint.color = Color.WHITE
        canvas.drawPath(starPath, fillPaint)
    }

    private fun drawBeadsPath(canvas: Canvas, stroke: Stroke) {
        val pts = stroke.points
        val step = (stroke.strokeWidth * 1.1f).coerceAtLeast(15f)
        var distAccum = 0f

        for (i in 0 until pts.size - 1) {
            val p1 = pts[i]
            val p2 = pts[i + 1]
            val d = hypot(p2.x - p1.x, p2.y - p1.y)
            distAccum += d
            if (distAccum >= step || i == 0) {
                distAccum = 0f
                val col = if (stroke.isRainbow) {
                    Color.HSVToColor(floatArrayOf((i * 15f) % 360f, 1f, 1f))
                } else stroke.color

                val r = stroke.strokeWidth * 0.5f

                // Glow
                fillPaint.color = col
                fillPaint.maskFilter = BlurMaskFilter(r * 0.8f, BlurMaskFilter.Blur.NORMAL)
                canvas.drawCircle(p2.x, p2.y, r * 1.3f, fillPaint)

                // Bead
                fillPaint.maskFilter = null
                fillPaint.color = col
                canvas.drawCircle(p2.x, p2.y, r, fillPaint)

                // White shiny center
                fillPaint.color = Color.WHITE
                canvas.drawCircle(p2.x - r * 0.25f, p2.y - r * 0.25f, r * 0.35f, fillPaint)
            }
        }
    }

    private fun drawRibbonPath(canvas: Canvas, stroke: Stroke) {
        val pts = stroke.points
        if (pts.size < 2) return

        val offset = stroke.strokeWidth * 0.5f
        val path1 = Path()
        val path2 = Path()

        for (i in 0 until pts.size - 1) {
            val p1 = pts[i]
            val p2 = pts[i + 1]
            val angle = atan2(p2.y - p1.y, p2.x - p1.x) + Math.PI / 2
            val ox = (cos(angle) * offset).toFloat()
            val oy = (sin(angle) * offset).toFloat()

            if (i == 0) {
                path1.moveTo(p1.x + ox, p1.y + oy)
                path2.moveTo(p1.x - ox, p1.y - oy)
            }
            path1.lineTo(p2.x + ox, p2.y + oy)
            path2.lineTo(p2.x - ox, p2.y - oy)
        }

        val ribbonWidth = (stroke.strokeWidth * 0.4f).coerceAtLeast(4f)

        // Glow line 1 & 2
        glowPaint.color = stroke.color
        glowPaint.strokeWidth = ribbonWidth * 2f
        glowPaint.maskFilter = BlurMaskFilter(ribbonWidth, BlurMaskFilter.Blur.NORMAL)
        canvas.drawPath(path1, glowPaint)
        canvas.drawPath(path2, glowPaint)

        // Core lines
        corePaint.strokeWidth = ribbonWidth * 0.5f
        corePaint.maskFilter = null
        canvas.drawPath(path1, corePaint)
        canvas.drawPath(path2, corePaint)
    }

    private fun drawFireworksPath(canvas: Canvas, stroke: Stroke) {
        val pts = stroke.points
        val step = (stroke.strokeWidth * 1.8f).coerceAtLeast(25f)
        var distAccum = 0f

        for (i in 0 until pts.size - 1) {
            val p1 = pts[i]
            val p2 = pts[i + 1]
            val d = hypot(p2.x - p1.x, p2.y - p1.y)
            distAccum += d
            if (distAccum >= step || i == 0) {
                distAccum = 0f
                val col = if (stroke.isRainbow) {
                    Color.HSVToColor(floatArrayOf((i * 20f) % 360f, 1f, 1f))
                } else stroke.color

                val sparks = 8
                val sparkRadius = stroke.strokeWidth * 1.4f
                glowPaint.color = col
                glowPaint.strokeWidth = 3f
                glowPaint.maskFilter = BlurMaskFilter(4f, BlurMaskFilter.Blur.NORMAL)

                for (s in 0 until sparks) {
                    val a = s * (2 * Math.PI / sparks)
                    val sx = (p2.x + cos(a) * sparkRadius).toFloat()
                    val sy = (p2.y + sin(a) * sparkRadius).toFloat()
                    canvas.drawLine(p2.x, p2.y, sx, sy, glowPaint)
                }

                // White shiny center
                fillPaint.color = Color.WHITE
                fillPaint.maskFilter = null
                canvas.drawCircle(p2.x, p2.y, stroke.strokeWidth * 0.35f, fillPaint)
            }
        }
    }

    private fun createSmoothedPath(points: List<Point>): Path {
        val path = Path()
        if (points.isEmpty()) return path
        path.moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            path.quadTo(prev.x, prev.y, (prev.x + curr.x) / 2f, (prev.y + curr.y) / 2f)
        }
        return path
    }

    private fun drawStencilGuide(canvas: Canvas) {
        if (currentStencil == StencilType.NONE || !isStencilGuideVisible) return
        val padW = width * 0.10f
        val padH = height * 0.16f
        stencilBounds.set(padW, padH, width - padW, height - padH)

        val glowColor = when (currentStencil.category) {
            "Dễ thương" -> Color.parseColor("#99FF007F")
            "Động vật" -> Color.parseColor("#9900FFFF")
            "Vũ trụ" -> Color.parseColor("#99FFE600")
            "Mandala" -> Color.parseColor("#99BC13FE")
            else -> Color.parseColor("#9900FFFF")
        }
        stencilGlowPaint.color = glowColor

        val path = TemplateShapes.createPath(currentStencil, stencilBounds)
        // Outer glowing outline
        canvas.drawPath(path, stencilGlowPaint)
        // Crisp inner white/light core line
        canvas.drawPath(path, stencilCorePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPoints.clear()
                currentPoints.add(Point(x, y))
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentPoints.add(Point(x, y))
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                currentPoints.add(Point(x, y))
                if (currentPoints.size >= 2) {
                    val completed = Stroke(
                        points = currentPoints.toList(),
                        color = currentColor,
                        strokeWidth = currentStrokeWidth,
                        brushType = currentBrushType,
                        symmetryMode = currentSymmetryMode,
                        isRainbow = isRainbowMode
                    )
                    strokes.add(completed)
                    undoneStrokes.clear()
                    drawCanvas?.let { cv ->
                        drawStroke(cv, completed)
                    }
                    notifyHistory()
                }
                currentPoints.clear()
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun exportBitmap(): Bitmap {
        val w = if (width > 0) width else 1080
        val h = if (height > 0) height else 1920
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // 1. Black background
        canvas.drawColor(Color.BLACK)

        // 2. Background image if present
        backgroundBitmap?.let { bmp ->
            val src = Rect(0, 0, bmp.width, bmp.height)
            val dst = Rect(0, 0, w, h)
            canvas.drawBitmap(bmp, src, dst, null)
        }

        // 3. Render all strokes (without stencil guide lines so artwork is clean)
        for (stroke in strokes) {
            drawStroke(canvas, stroke)
        }

        return result
    }
}
