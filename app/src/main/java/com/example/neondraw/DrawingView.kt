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
    private val stencilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f
        color = Color.parseColor("#6600FFFF")
        pathEffect = DashPathEffect(floatArrayOf(16f, 12f), 0f)
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
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f
        val minDim = min(w, h)
        val path = Path()

        when (currentStencil) {
            StencilType.NONE -> return

            StencilType.HEART -> {
                val s = minDim * 0.35f
                path.moveTo(cx, cy + s * 0.85f)
                path.cubicTo(cx - s * 1.3f, cy - s * 0.2f, cx - s * 0.7f, cy - s * 1.2f, cx, cy - s * 0.4f)
                path.cubicTo(cx + s * 0.7f, cy - s * 1.2f, cx + s * 1.3f, cy - s * 0.2f, cx, cy + s * 0.85f)
            }

            StencilType.STAR -> {
                val radius = minDim * 0.35f
                val innerRadius = radius * 0.45f
                val spikes = 5
                val rot = Math.PI / 2 * 3
                val step = Math.PI / spikes
                path.moveTo(cx, cy - radius)
                for (i in 0 until spikes) {
                    val x1 = (cx + cos(rot + i * 2 * step) * radius).toFloat()
                    val y1 = (cy + sin(rot + i * 2 * step) * radius).toFloat()
                    path.lineTo(x1, y1)
                    val x2 = (cx + cos(rot + (i * 2 + 1) * step) * innerRadius).toFloat()
                    val y2 = (cy + sin(rot + (i * 2 + 1) * step) * innerRadius).toFloat()
                    path.lineTo(x2, y2)
                }
                path.close()
            }

            StencilType.RAINBOW_CLOUD -> {
                val s = minDim * 0.3f
                // 3 rainbow arcs
                val arcRect1 = RectF(cx - s * 1.2f, cy - s * 0.8f, cx + s * 1.2f, cy + s * 0.8f)
                path.arcTo(arcRect1, 180f, 180f, true)
                val arcRect2 = RectF(cx - s * 0.9f, cy - s * 0.5f, cx + s * 0.9f, cy + s * 0.7f)
                path.arcTo(arcRect2, 180f, 180f, true)
                // Cloud left
                path.addCircle(cx - s * 0.9f, cy + s * 0.3f, s * 0.35f, Path.Direction.CW)
                // Cloud right
                path.addCircle(cx + s * 0.9f, cy + s * 0.3f, s * 0.35f, Path.Direction.CW)
            }

            StencilType.CUPCAKE -> {
                val s = minDim * 0.25f
                // Base cup
                path.moveTo(cx - s * 0.8f, cy)
                path.lineTo(cx - s * 0.5f, cy + s * 1.1f)
                path.lineTo(cx + s * 0.5f, cy + s * 1.1f)
                path.lineTo(cx + s * 0.8f, cy)
                path.close()
                // Fluffy frosting
                path.moveTo(cx - s * 0.8f, cy)
                path.cubicTo(cx - s * 1.1f, cy - s * 0.6f, cx - s * 0.4f, cy - s * 1.0f, cx, cy - s * 1.1f)
                path.cubicTo(cx + s * 0.4f, cy - s * 1.0f, cx + s * 1.1f, cy - s * 0.6f, cx + s * 0.8f, cy)
                // Cherry
                path.addCircle(cx, cy - s * 1.25f, s * 0.18f, Path.Direction.CW)
            }

            StencilType.ICE_CREAM -> {
                val s = minDim * 0.28f
                // Cone
                path.moveTo(cx, cy + s * 1.2f)
                path.lineTo(cx - s * 0.6f, cy - s * 0.1f)
                path.lineTo(cx + s * 0.6f, cy - s * 0.1f)
                path.close()
                // Scoop 1
                path.addCircle(cx, cy - s * 0.4f, s * 0.5f, Path.Direction.CW)
                // Cherry
                path.addCircle(cx, cy - s * 1.0f, s * 0.15f, Path.Direction.CW)
            }

            StencilType.STRAWBERRY -> {
                val s = minDim * 0.3f
                path.moveTo(cx, cy + s * 0.9f)
                path.cubicTo(cx - s * 1.1f, cy + s * 0.2f, cx - s * 0.8f, cy - s * 0.8f, cx, cy - s * 0.7f)
                path.cubicTo(cx + s * 0.8f, cy - s * 0.8f, cx + s * 1.1f, cy + s * 0.2f, cx, cy + s * 0.9f)
                // Leaf crown
                path.moveTo(cx - s * 0.5f, cy - s * 0.7f)
                path.lineTo(cx, cy - s * 1.05f)
                path.lineTo(cx + s * 0.5f, cy - s * 0.7f)
            }

            StencilType.CAT -> {
                val r = minDim * 0.25f
                // Head
                path.addCircle(cx, cy, r, Path.Direction.CW)
                // Ears
                path.moveTo(cx - r * 0.8f, cy - r * 0.5f)
                path.lineTo(cx - r * 1.1f, cy - r * 1.35f)
                path.lineTo(cx - r * 0.2f, cy - r * 0.95f)
                path.moveTo(cx + r * 0.8f, cy - r * 0.5f)
                path.lineTo(cx + r * 1.1f, cy - r * 1.35f)
                path.lineTo(cx + r * 0.2f, cy - r * 0.95f)
                // Whiskers
                path.moveTo(cx - r * 0.4f, cy + r * 0.1f)
                path.lineTo(cx - r * 1.2f, cy - r * 0.05f)
                path.moveTo(cx - r * 0.4f, cy + r * 0.3f)
                path.lineTo(cx - r * 1.2f, cy + r * 0.4f)
                path.moveTo(cx + r * 0.4f, cy + r * 0.1f)
                path.lineTo(cx + r * 1.2f, cy - r * 0.05f)
                path.moveTo(cx + r * 0.4f, cy + r * 0.3f)
                path.lineTo(cx + r * 1.2f, cy + r * 0.4f)
            }

            StencilType.DOG -> {
                val r = minDim * 0.24f
                path.addCircle(cx, cy, r, Path.Direction.CW)
                // Floppy Ears
                path.moveTo(cx - r * 0.7f, cy - r * 0.6f)
                path.cubicTo(cx - r * 1.4f, cy - r * 0.2f, cx - r * 1.3f, cy + r * 0.9f, cx - r * 0.7f, cy + r * 0.6f)
                path.moveTo(cx + r * 0.7f, cy - r * 0.6f)
                path.cubicTo(cx + r * 1.4f, cy - r * 0.2f, cx + r * 1.3f, cy + r * 0.9f, cx + r * 0.7f, cy + r * 0.6f)
                // Snout
                path.addCircle(cx, cy + r * 0.25f, r * 0.35f, Path.Direction.CW)
            }

            StencilType.BUTTERFLY -> {
                val s = minDim * 0.25f
                // Left wings
                path.moveTo(cx, cy)
                path.cubicTo(cx - s * 1.6f, cy - s * 1.6f, cx - s * 1.8f, cy, cx, cy + s * 0.2f)
                path.cubicTo(cx - s * 1.3f, cy + s * 1.3f, cx - s * 0.8f, cy + s * 1.6f, cx, cy + s * 0.8f)
                // Right wings
                path.moveTo(cx, cy)
                path.cubicTo(cx + s * 1.6f, cy - s * 1.6f, cx + s * 1.8f, cy, cx, cy + s * 0.2f)
                path.cubicTo(cx + s * 1.3f, cy + s * 1.3f, cx + s * 0.8f, cy + s * 1.6f, cx, cy + s * 0.8f)
                // Antennae
                path.moveTo(cx, cy - s * 0.3f)
                path.quadTo(cx - s * 0.4f, cy - s * 0.9f, cx - s * 0.6f, cy - s * 0.9f)
                path.moveTo(cx, cy - s * 0.3f)
                path.quadTo(cx + s * 0.4f, cy - s * 0.9f, cx + s * 0.6f, cy - s * 0.9f)
            }

            StencilType.DINO -> {
                val s = minDim * 0.28f
                // Cute T-rex silhouette outline
                path.moveTo(cx - s * 0.8f, cy + s * 0.8f) // tail tip
                path.quadTo(cx - s * 0.4f, cy + s * 0.3f, cx - s * 0.2f, cy - s * 0.2f) // back
                path.lineTo(cx - s * 0.1f, cy - s * 0.8f) // neck
                path.lineTo(cx + s * 0.5f, cy - s * 0.8f) // head top
                path.lineTo(cx + s * 0.6f, cy - s * 0.5f) // snout
                path.lineTo(cx + s * 0.2f, cy - s * 0.4f) // mouth
                path.lineTo(cx + s * 0.1f, cy - s * 0.1f) // chest
                path.lineTo(cx + s * 0.3f, cy + s * 0.1f) // tiny arm
                path.lineTo(cx + s * 0.15f, cy + s * 0.2f)
                path.lineTo(cx + s * 0.3f, cy + s * 0.8f) // leg front
                path.lineTo(cx, cy + s * 0.8f) // belly
                path.close()
            }

            StencilType.DOLPHIN -> {
                val s = minDim * 0.3f
                path.moveTo(cx - s * 1.1f, cy + s * 0.6f) // tail
                path.cubicTo(cx - s * 0.5f, cy - s * 0.5f, cx + s * 0.4f, cy - s * 0.8f, cx + s * 1.0f, cy - s * 0.2f) // arched back
                path.lineTo(cx + s * 1.2f, cy - s * 0.1f) // snout
                path.cubicTo(cx + s * 0.8f, cy + s * 0.2f, cx + s * 0.2f, cy + s * 0.3f, cx - s * 0.3f, cy + s * 0.2f) // belly
                path.lineTo(cx - s * 1.1f, cy + s * 0.6f) // tail
                // Dorsal fin
                path.moveTo(cx, cy - s * 0.65f)
                path.lineTo(cx - s * 0.2f, cy - s * 0.95f)
                path.lineTo(cx + s * 0.2f, cy - s * 0.6f)
            }

            StencilType.PENGUIN -> {
                val s = minDim * 0.25f
                // Oval body
                val bodyRect = RectF(cx - s * 0.7f, cy - s * 0.8f, cx + s * 0.7f, cy + s * 1.0f)
                path.addOval(bodyRect, Path.Direction.CW)
                // Wings
                path.moveTo(cx - s * 0.7f, cy - s * 0.2f)
                path.quadTo(cx - s * 1.2f, cy + s * 0.3f, cx - s * 0.6f, cy + s * 0.6f)
                path.moveTo(cx + s * 0.7f, cy - s * 0.2f)
                path.quadTo(cx + s * 1.2f, cy + s * 0.3f, cx + s * 0.6f, cy + s * 0.6f)
                // Beak
                path.moveTo(cx - s * 0.2f, cy - s * 0.3f)
                path.lineTo(cx, cy - s * 0.1f)
                path.lineTo(cx + s * 0.2f, cy - s * 0.3f)
            }

            StencilType.ROCKET -> {
                val s = minDim * 0.28f
                // Fuselage
                path.moveTo(cx, cy - s * 1.2f)
                path.cubicTo(cx + s * 0.6f, cy - s * 0.5f, cx + s * 0.6f, cy + s * 0.5f, cx + s * 0.4f, cy + s * 0.8f)
                path.lineTo(cx - s * 0.4f, cy + s * 0.8f)
                path.cubicTo(cx - s * 0.6f, cy + s * 0.5f, cx - s * 0.6f, cy - s * 0.5f, cx, cy - s * 1.2f)
                // Fins
                path.moveTo(cx - s * 0.4f, cy + s * 0.4f)
                path.lineTo(cx - s * 0.85f, cy + s * 0.9f)
                path.lineTo(cx - s * 0.4f, cy + s * 0.8f)
                path.moveTo(cx + s * 0.4f, cy + s * 0.4f)
                path.lineTo(cx + s * 0.85f, cy + s * 0.9f)
                path.lineTo(cx + s * 0.4f, cy + s * 0.8f)
                // Window
                path.addCircle(cx, cy - s * 0.2f, s * 0.22f, Path.Direction.CW)
            }

            StencilType.MOON_STARS -> {
                val r = minDim * 0.32f
                // Crescent Moon
                path.moveTo(cx, cy - r)
                path.cubicTo(cx - r * 1.2f, cy - r * 0.7f, cx - r * 1.2f, cy + r * 0.7f, cx, cy + r)
                path.cubicTo(cx - r * 0.4f, cy + r * 0.6f, cx - r * 0.4f, cy - r * 0.6f, cx, cy - r)
                // Twinkle stars beside moon
                drawMiniStar(path, cx + r * 0.5f, cy - r * 0.4f, r * 0.25f)
                drawMiniStar(path, cx + r * 0.7f, cy + r * 0.3f, r * 0.18f)
            }

            StencilType.CROWN -> {
                val s = minDim * 0.32f
                path.moveTo(cx - s, cy + s * 0.5f)
                path.lineTo(cx - s, cy - s * 0.2f)
                path.lineTo(cx - s * 0.5f, cy + s * 0.1f)
                path.lineTo(cx, cy - s * 0.6f)
                path.lineTo(cx + s * 0.5f, cy + s * 0.1f)
                path.lineTo(cx + s, cy - s * 0.2f)
                path.lineTo(cx + s, cy + s * 0.5f)
                path.close()
                // Gem jewels
                path.addCircle(cx - s, cy - s * 0.2f, s * 0.08f, Path.Direction.CW)
                path.addCircle(cx, cy - s * 0.6f, s * 0.1f, Path.Direction.CW)
                path.addCircle(cx + s, cy - s * 0.2f, s * 0.08f, Path.Direction.CW)
            }

            StencilType.DIAMOND -> {
                val s = minDim * 0.3f
                // Table top
                path.moveTo(cx - s * 0.8f, cy - s * 0.4f)
                path.lineTo(cx + s * 0.8f, cy - s * 0.4f)
                path.lineTo(cx, cy + s * 0.9f)
                path.close()
                // Top facets
                path.lineTo(cx - s * 0.4f, cy - s * 0.4f)
                path.lineTo(cx - s * 0.6f, cy - s * 0.75f)
                path.lineTo(cx + s * 0.6f, cy - s * 0.75f)
                path.lineTo(cx + s * 0.4f, cy - s * 0.4f)
            }

            StencilType.MUSIC -> {
                val s = minDim * 0.3f
                // Treble / Eighth note pair
                path.addCircle(cx - s * 0.4f, cy + s * 0.6f, s * 0.22f, Path.Direction.CW)
                path.addCircle(cx + s * 0.5f, cy + s * 0.4f, s * 0.22f, Path.Direction.CW)
                path.moveTo(cx - s * 0.2f, cy + s * 0.6f)
                path.lineTo(cx - s * 0.2f, cy - s * 0.6f)
                path.lineTo(cx + s * 0.7f, cy - s * 0.8f)
                path.lineTo(cx + s * 0.7f, cy + s * 0.4f)
            }

            StencilType.LOTUS -> {
                val s = minDim * 0.32f
                // Center petal
                path.moveTo(cx, cy + s * 0.5f)
                path.cubicTo(cx - s * 0.4f, cy - s * 0.2f, cx - s * 0.3f, cy - s * 0.8f, cx, cy - s * 1.0f)
                path.cubicTo(cx + s * 0.3f, cy - s * 0.8f, cx + s * 0.4f, cy - s * 0.2f, cx, cy + s * 0.5f)
                // Left petal
                path.moveTo(cx, cy + s * 0.5f)
                path.cubicTo(cx - s * 0.7f, cy + s * 0.2f, cx - s * 0.9f, cy - s * 0.4f, cx - s * 0.6f, cy - s * 0.7f)
                path.cubicTo(cx - s * 0.3f, cy - s * 0.3f, cx - s * 0.1f, cy + s * 0.2f, cx, cy + s * 0.5f)
                // Right petal
                path.moveTo(cx, cy + s * 0.5f)
                path.cubicTo(cx + s * 0.7f, cy + s * 0.2f, cx + s * 0.9f, cy - s * 0.4f, cx + s * 0.6f, cy - s * 0.7f)
                path.cubicTo(cx + s * 0.3f, cy - s * 0.3f, cx + s * 0.1f, cy + s * 0.2f, cx, cy + s * 0.5f)
            }

            StencilType.MANDALA_SUN -> {
                val r = minDim * 0.15f
                path.addCircle(cx, cy, r, Path.Direction.CW)
                val rays = 12
                for (i in 0 until rays) {
                    val angle = i * (2 * Math.PI / rays)
                    val x1 = (cx + cos(angle) * r).toFloat()
                    val y1 = (cy + sin(angle) * r).toFloat()
                    val x2 = (cx + cos(angle) * r * 2.2f).toFloat()
                    val y2 = (cy + sin(angle) * r * 2.2f).toFloat()
                    path.moveTo(x1, y1)
                    path.lineTo(x2, y2)
                    path.addCircle(x2, y2, minDim * 0.02f, Path.Direction.CW)
                }
            }

            StencilType.SPIDER_WEB -> {
                val maxR = minDim * 0.36f
                val spokes = 8
                // Radial lines
                for (i in 0 until spokes) {
                    val a = i * (2 * Math.PI / spokes)
                    path.moveTo(cx, cy)
                    path.lineTo((cx + cos(a) * maxR).toFloat(), (cy + sin(a) * maxR).toFloat())
                }
                // Concentric web rings
                for (ring in 1..4) {
                    val r = maxR * (ring / 4f)
                    for (i in 0 until spokes) {
                        val a1 = i * (2 * Math.PI / spokes)
                        val a2 = (i + 1) * (2 * Math.PI / spokes)
                        val p1x = (cx + cos(a1) * r).toFloat()
                        val p1y = (cy + sin(a1) * r).toFloat()
                        val p2x = (cx + cos(a2) * r).toFloat()
                        val p2y = (cy + sin(a2) * r).toFloat()
                        if (i == 0) path.moveTo(p1x, p1y)
                        path.quadTo(
                            (cx + cos((a1 + a2) / 2) * (r * 0.85f)).toFloat(),
                            (cy + sin((a1 + a2) / 2) * (r * 0.85f)).toFloat(),
                            p2x, p2y
                        )
                    }
                }
            }
        }

        canvas.drawPath(path, stencilPaint)
    }

    private fun drawMiniStar(path: Path, cx: Float, cy: Float, radius: Float) {
        val spikes = 4
        val rot = Math.PI / 2 * 3
        val step = Math.PI / spikes
        val innerRadius = radius * 0.3f
        path.moveTo(cx, cy - radius)
        for (i in 0 until spikes) {
            val x1 = (cx + cos(rot + i * 2 * step) * radius).toFloat()
            val y1 = (cy + sin(rot + i * 2 * step) * radius).toFloat()
            path.lineTo(x1, y1)
            val x2 = (cx + cos(rot + (i * 2 + 1) * step) * innerRadius).toFloat()
            val y2 = (cy + sin(rot + (i * 2 + 1) * step) * innerRadius).toFloat()
            path.lineTo(x2, y2)
        }
        path.close()
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
