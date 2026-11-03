package com.example.neondraw

import android.graphics.Path
import android.graphics.RectF
import kotlin.math.*

object TemplateShapes {

    /**
     * Generates a fully detailed vector path for a given stencil,
     * scaled and centered inside [bounds].
     */
    fun createPath(type: StencilType, bounds: RectF): Path {
        val path = Path()
        if (type == StencilType.NONE) return path

        val cx = bounds.centerX()
        val cy = bounds.centerY()
        val w = bounds.width()
        val h = bounds.height()
        val s = min(w, h) * 0.46f // base radius / scale

        when (type) {
            StencilType.NONE -> {}

            // ==========================================
            // --- DỄ THƯƠNG (25 MẪU) ---
            // ==========================================

            StencilType.HEART -> {
                val r = s * 0.85f
                path.moveTo(cx, cy + r * 0.85f)
                path.cubicTo(cx - r * 1.35f, cy - r * 0.2f, cx - r * 0.8f, cy - r * 1.25f, cx, cy - r * 0.45f)
                path.cubicTo(cx + r * 0.8f, cy - r * 1.25f, cx + r * 1.35f, cy - r * 0.2f, cx, cy + r * 0.85f)

                val inR = r * 0.5f
                path.moveTo(cx, cy + inR * 0.8f)
                path.cubicTo(cx - inR * 1.35f, cy - inR * 0.2f, cx - inR * 0.8f, cy - inR * 1.25f, cx, cy - inR * 0.45f)
                path.cubicTo(cx + inR * 0.8f, cy - inR * 1.25f, cx + inR * 1.35f, cy - inR * 0.2f, cx, cy + inR * 0.8f)

                addStar(path, cx + r * 0.85f, cy - r * 0.65f, r * 0.2f)
                addStar(path, cx - r * 0.9f, cy + r * 0.2f, r * 0.14f)
            }

            StencilType.STAR -> {
                addStar(path, cx, cy, s * 0.95f)
                addStar(path, cx, cy, s * 0.52f)
                path.addCircle(cx - s * 0.16f, cy - s * 0.05f, s * 0.05f, Path.Direction.CW)
                path.addCircle(cx + s * 0.16f, cy - s * 0.05f, s * 0.05f, Path.Direction.CW)
                addArc(path, cx - s * 0.14f, cy - s * 0.02f, cx + s * 0.14f, cy + s * 0.16f, 0f, 180f)
            }

            StencilType.RAINBOW_CLOUD -> {
                val r = s * 0.85f
                val arcs = 4
                for (i in 0 until arcs) {
                    val stepR = r * (1f - i * 0.16f)
                    addArc(path, cx - stepR, cy - stepR * 0.8f, cx + stepR, cy + stepR * 0.8f, 180f, 180f)
                }
                addCloud(path, cx - r * 0.65f, cy + r * 0.25f, r * 0.38f)
                addCloud(path, cx + r * 0.65f, cy + r * 0.25f, r * 0.38f)
            }

            StencilType.CUPCAKE -> {
                val r = s * 0.8f
                path.moveTo(cx - r * 0.65f, cy + r * 0.05f)
                path.lineTo(cx - r * 0.45f, cy + r * 0.95f)
                path.lineTo(cx + r * 0.45f, cy + r * 0.95f)
                path.lineTo(cx + r * 0.65f, cy + r * 0.05f)
                path.close()
                path.moveTo(cx - r * 0.25f, cy + r * 0.08f)
                path.lineTo(cx - r * 0.18f, cy + r * 0.92f)
                path.moveTo(cx + r * 0.25f, cy + r * 0.08f)
                path.lineTo(cx + r * 0.18f, cy + r * 0.92f)
                path.moveTo(cx - r * 0.75f, cy + r * 0.05f)
                path.cubicTo(cx - r * 0.9f, cy - r * 0.35f, cx - r * 0.3f, cy - r * 0.45f, cx, cy - r * 0.55f)
                path.cubicTo(cx + r * 0.3f, cy - r * 0.45f, cx + r * 0.9f, cy - r * 0.35f, cx + r * 0.75f, cy + r * 0.05f)
                path.moveTo(cx - r * 0.45f, cy - r * 0.4f)
                path.cubicTo(cx - r * 0.4f, cy - r * 0.8f, cx + r * 0.4f, cy - r * 0.8f, cx + r * 0.45f, cy - r * 0.4f)
                path.addCircle(cx, cy - r * 0.92f, r * 0.15f, Path.Direction.CW)
                path.moveTo(cx, cy - r * 1.05f)
                path.quadTo(cx + r * 0.2f, cy - r * 1.35f, cx + r * 0.35f, cy - r * 1.25f)
            }

            StencilType.ICE_CREAM -> {
                val r = s * 0.85f
                path.moveTo(cx, cy + r * 1.05f)
                path.lineTo(cx - r * 0.5f, cy)
                path.lineTo(cx + r * 0.5f, cy)
                path.close()
                path.moveTo(cx - r * 0.35f, cy + r * 0.3f)
                path.lineTo(cx + r * 0.15f, cy + r * 0.8f)
                path.moveTo(cx + r * 0.35f, cy + r * 0.3f)
                path.lineTo(cx - r * 0.15f, cy + r * 0.8f)
                path.addCircle(cx, cy - r * 0.25f, r * 0.45f, Path.Direction.CW)
                path.addCircle(cx, cy - r * 0.65f, r * 0.35f, Path.Direction.CW)
                path.addCircle(cx, cy - r * 1.05f, r * 0.14f, Path.Direction.CW)
            }

            StencilType.STRAWBERRY -> {
                val r = s * 0.85f
                path.moveTo(cx, cy + r * 0.95f)
                path.cubicTo(cx - r * 1.1f, cy + r * 0.4f, cx - r * 0.95f, cy - r * 0.65f, cx, cy - r * 0.55f)
                path.cubicTo(cx + r * 0.95f, cy - r * 0.65f, cx + r * 1.1f, cy + r * 0.4f, cx, cy + r * 0.95f)
                path.moveTo(cx, cy - r * 0.55f)
                path.lineTo(cx - r * 0.4f, cy - r * 0.85f)
                path.lineTo(cx - r * 0.15f, cy - r * 0.65f)
                path.lineTo(cx, cy - r * 0.95f)
                path.lineTo(cx + r * 0.15f, cy - r * 0.65f)
                path.lineTo(cx + r * 0.4f, cy - r * 0.85f)
                path.lineTo(cx, cy - r * 0.55f)
                path.moveTo(cx, cy - r * 0.95f)
                path.quadTo(cx + r * 0.15f, cy - r * 1.25f, cx + r * 0.3f, cy - r * 1.2f)
                val dotPositions = listOf(
                    Pair(-0.35f, -0.1f), Pair(0f, -0.2f), Pair(0.35f, -0.1f),
                    Pair(-0.25f, 0.25f), Pair(0.25f, 0.25f), Pair(0f, 0.55f)
                )
                dotPositions.forEach { (dx, dy) ->
                    path.addCircle(cx + r * dx, cy + r * dy, r * 0.05f, Path.Direction.CW)
                }
            }

            StencilType.CHERRY -> {
                val r = s * 0.8f
                // 2 cherries
                path.addCircle(cx - r * 0.45f, cy + r * 0.45f, r * 0.42f, Path.Direction.CW)
                path.addCircle(cx + r * 0.45f, cy + r * 0.35f, r * 0.42f, Path.Direction.CW)
                // Left stem
                path.moveTo(cx - r * 0.45f, cy + r * 0.05f)
                path.cubicTo(cx - r * 0.3f, cy - r * 0.5f, cx - r * 0.1f, cy - r * 0.85f, cx, cy - r * 0.95f)
                // Right stem
                path.moveTo(cx + r * 0.45f, cy - r * 0.05f)
                path.cubicTo(cx + r * 0.3f, cy - r * 0.5f, cx + r * 0.1f, cy - r * 0.85f, cx, cy - r * 0.95f)
                // Leaf at junction
                addLeaf(path, cx, cy - r * 0.95f, cx + r * 0.65f, cy - r * 1.15f, r * 0.25f)
            }

            StencilType.DONUT -> {
                val r = s * 0.88f
                path.addCircle(cx, cy, r, Path.Direction.CW)
                path.addCircle(cx, cy, r * 0.38f, Path.Direction.CW)
                // Wavy frosting outline
                val points = 16
                for (i in 0 until points) {
                    val a = i * 2 * Math.PI / points
                    val fr = r * (0.75f + 0.12f * sin(i * 3.0).toFloat())
                    val px = (cx + cos(a) * fr).toFloat()
                    val py = (cy + sin(a) * fr).toFloat()
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                // Sprinkles
                for (i in 0 until 8) {
                    val a = i * 2 * Math.PI / 8 + 0.2
                    val px = (cx + cos(a) * r * 0.58f).toFloat()
                    val py = (cy + sin(a) * r * 0.58f).toFloat()
                    path.moveTo(px - r * 0.04f, py)
                    path.lineTo(px + r * 0.04f, py)
                }
            }

            StencilType.WATERMELON -> {
                val r = s * 0.9f
                // Sliced slice
                path.moveTo(cx, cy - r * 0.6f)
                path.lineTo(cx - r * 0.85f, cy + r * 0.65f)
                path.quadTo(cx, cy + r * 0.95f, cx + r * 0.85f, cy + r * 0.65f)
                path.close()
                // Rind inner arc
                path.moveTo(cx - r * 0.72f, cy + r * 0.55f)
                path.quadTo(cx, cy + r * 0.8f, cx + r * 0.72f, cy + r * 0.55f)
                // Seeds
                val seeds = listOf(Pair(-0.25f, 0.1f), Pair(0.25f, 0.1f), Pair(0f, -0.1f), Pair(-0.1f, 0.35f), Pair(0.1f, 0.35f))
                seeds.forEach { (dx, dy) ->
                    path.addCircle(cx + r * dx, cy + r * dy, r * 0.04f, Path.Direction.CW)
                }
            }

            StencilType.AVOCADO -> {
                val r = s * 0.85f
                // Pear-shaped avocado body
                path.moveTo(cx, cy - r * 0.95f)
                path.cubicTo(cx - r * 0.6f, cy - r * 0.95f, cx - r * 0.5f, cy - r * 0.2f, cx - r * 0.85f, cy + r * 0.35f)
                path.cubicTo(cx - r * 0.95f, cy + r * 0.85f, cx - r * 0.45f, cy + r * 1.05f, cx, cy + r * 1.05f)
                path.cubicTo(cx + r * 0.45f, cy + r * 1.05f, cx + r * 0.95f, cy + r * 0.85f, cx + r * 0.85f, cy + r * 0.35f)
                path.cubicTo(cx + r * 0.5f, cy - r * 0.2f, cx + r * 0.6f, cy - r * 0.95f, cx, cy - r * 0.95f)
                // Inner flesh ring
                path.addCircle(cx, cy + r * 0.45f, r * 0.45f, Path.Direction.CW)
                // Pit (seed)
                path.addCircle(cx, cy + r * 0.45f, r * 0.28f, Path.Direction.CW)
            }

            StencilType.APPLE -> {
                val r = s * 0.82f
                path.moveTo(cx, cy - r * 0.65f)
                path.cubicTo(cx - r * 0.5f, cy - r * 0.95f, cx - r * 1.05f, cy - r * 0.4f, cx - r * 0.95f, cy + r * 0.2f)
                path.cubicTo(cx - r * 0.9f, cy + r * 0.85f, cx - r * 0.3f, cy + r * 0.95f, cx, cy + r * 0.75f)
                path.cubicTo(cx + r * 0.3f, cy + r * 0.95f, cx + r * 0.9f, cy + r * 0.85f, cx + r * 0.95f, cy + r * 0.2f)
                path.cubicTo(cx + r * 1.05f, cy - r * 0.4f, cx + r * 0.5f, cy - r * 0.95f, cx, cy - r * 0.65f)
                // Stem
                path.moveTo(cx, cy - r * 0.65f)
                path.quadTo(cx - r * 0.1f, cy - r * 1.15f, cx - r * 0.2f, cy - r * 1.2f)
                // Leaf
                addLeaf(path, cx, cy - r * 0.85f, cx + r * 0.5f, cy - r * 1.1f, r * 0.22f)
            }

            StencilType.BANANA -> {
                val r = s * 0.88f
                path.moveTo(cx - r * 0.85f, cy - r * 0.55f)
                path.quadTo(cx, cy + r * 0.95f, cx + r * 0.85f, cy - r * 0.45f)
                path.quadTo(cx, cy + r * 0.6f, cx - r * 0.85f, cy - r * 0.55f)
                // Inner ridge line
                path.moveTo(cx - r * 0.75f, cy - r * 0.45f)
                path.quadTo(cx, cy + r * 0.75f, cx + r * 0.75f, cy - r * 0.35f)
                // Stem tips
                path.moveTo(cx - r * 0.85f, cy - r * 0.55f)
                path.lineTo(cx - r * 0.95f, cy - r * 0.65f)
                path.moveTo(cx + r * 0.85f, cy - r * 0.45f)
                path.lineTo(cx + r * 0.92f, cy - r * 0.52f)
            }

            StencilType.CANDY -> {
                val r = s * 0.78f
                // Center wrapped oval
                addOval(path, cx - r * 0.55f, cy - r * 0.35f, cx + r * 0.55f, cy + r * 0.35f)
                // Stripes on wrapper
                path.moveTo(cx - r * 0.2f, cy - r * 0.32f)
                path.lineTo(cx - r * 0.1f, cy + r * 0.32f)
                path.moveTo(cx + r * 0.1f, cy - r * 0.32f)
                path.lineTo(cx + r * 0.2f, cy + r * 0.32f)
                // Left twist
                path.moveTo(cx - r * 0.55f, cy)
                path.lineTo(cx - r * 1.0f, cy - r * 0.45f)
                path.lineTo(cx - r * 1.0f, cy + r * 0.45f)
                path.close()
                // Right twist
                path.moveTo(cx + r * 0.55f, cy)
                path.lineTo(cx + r * 1.0f, cy - r * 0.45f)
                path.lineTo(cx + r * 1.0f, cy + r * 0.45f)
                path.close()
            }

            StencilType.LOLLIPOP -> {
                val r = s * 0.85f
                // Candy spiral circle
                path.addCircle(cx, cy - r * 0.25f, r * 0.58f, Path.Direction.CW)
                path.addCircle(cx, cy - r * 0.25f, r * 0.38f, Path.Direction.CW)
                path.addCircle(cx, cy - r * 0.25f, r * 0.18f, Path.Direction.CW)
                // Stick
                path.moveTo(cx, cy + r * 0.33f)
                path.lineTo(cx, cy + r * 1.15f)
                // Bow tie on stick
                path.moveTo(cx, cy + r * 0.45f)
                path.lineTo(cx - r * 0.25f, cy + r * 0.35f)
                path.lineTo(cx - r * 0.25f, cy + r * 0.55f)
                path.close()
                path.moveTo(cx, cy + r * 0.45f)
                path.lineTo(cx + r * 0.25f, cy + r * 0.35f)
                path.lineTo(cx + r * 0.25f, cy + r * 0.55f)
                path.close()
            }

            StencilType.MILK_TEA -> {
                val r = s * 0.82f
                // Cup
                path.moveTo(cx - r * 0.5f, cy - r * 0.45f)
                path.lineTo(cx - r * 0.38f, cy + r * 0.95f)
                path.lineTo(cx + r * 0.38f, cy + r * 0.95f)
                path.lineTo(cx + r * 0.5f, cy - r * 0.45f)
                path.close()
                // Domed lid
                addArc(path, cx - r * 0.52f, cy - r * 0.75f, cx + r * 0.52f, cy - r * 0.45f, 180f, 180f)
                // Straw
                path.moveTo(cx + r * 0.05f, cy - r * 0.6f)
                path.lineTo(cx + r * 0.2f, cy - r * 1.15f)
                // Tapioca pearls
                val boba = listOf(Pair(-0.2f, 0.75f), Pair(0f, 0.75f), Pair(0.2f, 0.75f), Pair(-0.1f, 0.85f), Pair(0.1f, 0.85f))
                boba.forEach { (dx, dy) ->
                    path.addCircle(cx + r * dx, cy + r * dy, r * 0.06f, Path.Direction.CW)
                }
            }

            StencilType.HAMBURGER -> {
                val r = s * 0.82f
                // Top bun (dome)
                addArc(path, cx - r * 0.75f, cy - r * 0.65f, cx + r * 0.75f, cy - r * 0.15f, 180f, 180f)
                path.lineTo(cx - r * 0.75f, cy - r * 0.15f)
                // Sesame seeds on top bun
                path.addCircle(cx - r * 0.3f, cy - r * 0.35f, r * 0.03f, Path.Direction.CW)
                path.addCircle(cx, cy - r * 0.45f, r * 0.03f, Path.Direction.CW)
                path.addCircle(cx + r * 0.3f, cy - r * 0.35f, r * 0.03f, Path.Direction.CW)
                // Lettuce (wavy)
                path.moveTo(cx - r * 0.8f, cy - r * 0.05f)
                for (i in 0 until 6) {
                    val x1 = cx - r * 0.8f + r * (i + 1) * 0.26f
                    val y1 = cy - r * 0.05f + (if (i % 2 == 0) r * 0.08f else -r * 0.05f)
                    path.lineTo(x1, y1)
                }
                // Meat patty
                addOval(path, cx - r * 0.75f, cy + r * 0.08f, cx + r * 0.75f, cy + r * 0.28f)
                // Bottom bun
                path.moveTo(cx - r * 0.7f, cy + r * 0.35f)
                path.lineTo(cx + r * 0.7f, cy + r * 0.35f)
                path.quadTo(cx, cy + r * 0.65f, cx - r * 0.7f, cy + r * 0.35f)
            }

            StencilType.PIZZA -> {
                val r = s * 0.88f
                // Slice triangle
                path.moveTo(cx, cy + r * 0.85f)
                path.lineTo(cx - r * 0.75f, cy - r * 0.65f)
                path.quadTo(cx, cy - r * 0.85f, cx + r * 0.75f, cy - r * 0.65f)
                path.close()
                // Crust line
                path.moveTo(cx - r * 0.68f, cy - r * 0.52f)
                path.quadTo(cx, cy - r * 0.72f, cx + r * 0.68f, cy - r * 0.52f)
                // Pepperoni slices
                path.addCircle(cx, cy - r * 0.2f, r * 0.12f, Path.Direction.CW)
                path.addCircle(cx - r * 0.25f, cy + r * 0.15f, r * 0.11f, Path.Direction.CW)
                path.addCircle(cx + r * 0.25f, cy + r * 0.15f, r * 0.11f, Path.Direction.CW)
            }

            StencilType.BOW_TIE -> {
                val r = s * 0.85f
                // Center knot
                path.addCircle(cx, cy, r * 0.18f, Path.Direction.CW)
                // Left wing
                path.moveTo(cx - r * 0.18f, cy)
                path.lineTo(cx - r * 0.85f, cy - r * 0.55f)
                path.lineTo(cx - r * 0.85f, cy + r * 0.55f)
                path.close()
                // Right wing
                path.moveTo(cx + r * 0.18f, cy)
                path.lineTo(cx + r * 0.85f, cy - r * 0.55f)
                path.lineTo(cx + r * 0.85f, cy + r * 0.55f)
                path.close()
                // Ribbon tails
                path.moveTo(cx - r * 0.1f, cy + r * 0.15f)
                path.lineTo(cx - r * 0.45f, cy + r * 0.95f)
                path.lineTo(cx - r * 0.2f, cy + r * 0.9f)
                path.lineTo(cx, cy + r * 0.18f)
                path.moveTo(cx + r * 0.1f, cy + r * 0.15f)
                path.lineTo(cx + r * 0.45f, cy + r * 0.95f)
                path.lineTo(cx + r * 0.2f, cy + r * 0.9f)
                path.lineTo(cx, cy + r * 0.18f)
            }

            StencilType.BALLOON -> {
                val r = s * 0.8f
                // Main middle balloon
                addOval(path, cx - r * 0.45f, cy - r * 0.95f, cx + r * 0.45f, cy + r * 0.15f)
                // Left balloon
                addOval(path, cx - r * 0.85f, cy - r * 0.75f, cx - r * 0.15f, cy + r * 0.25f)
                // Right balloon
                addOval(path, cx + r * 0.15f, cy - r * 0.75f, cx + r * 0.85f, cy + r * 0.25f)
                // Knot & string
                path.moveTo(cx, cy + r * 0.15f)
                path.lineTo(cx - r * 0.08f, cy + r * 0.25f)
                path.lineTo(cx + r * 0.08f, cy + r * 0.25f)
                path.close()
                path.moveTo(cx, cy + r * 0.25f)
                path.cubicTo(cx - r * 0.2f, cy + r * 0.5f, cx + r * 0.2f, cy + r * 0.8f, cx, cy + r * 1.15f)
            }

            StencilType.SUNGLASSES -> {
                val r = s * 0.82f
                // Left heart lens
                path.moveTo(cx - r * 0.45f, cy + r * 0.4f)
                path.cubicTo(cx - r * 0.9f, cy - r * 0.1f, cx - r * 0.75f, cy - r * 0.55f, cx - r * 0.45f, cy - r * 0.25f)
                path.cubicTo(cx - r * 0.15f, cy - r * 0.55f, cx, cy - r * 0.1f, cx - r * 0.45f, cy + r * 0.4f)
                // Right heart lens
                path.moveTo(cx + r * 0.45f, cy + r * 0.4f)
                path.cubicTo(cx, cy - r * 0.1f, cx + r * 0.15f, cy - r * 0.55f, cx + r * 0.45f, cy - r * 0.25f)
                path.cubicTo(cx + r * 0.75f, cy - r * 0.55f, cx + r * 0.9f, cy - r * 0.1f, cx + r * 0.45f, cy + r * 0.4f)
                // Bridge
                path.moveTo(cx - r * 0.2f, cy - r * 0.15f)
                path.lineTo(cx + r * 0.2f, cy - r * 0.15f)
                // Left arm
                path.moveTo(cx - r * 0.85f, cy - r * 0.25f)
                path.lineTo(cx - r * 1.0f, cy - r * 0.45f)
                // Right arm
                path.moveTo(cx + r * 0.85f, cy - r * 0.25f)
                path.lineTo(cx + r * 1.0f, cy - r * 0.45f)
            }

            StencilType.COFFEE_CUP -> {
                val r = s * 0.8f
                // Cup body
                path.moveTo(cx - r * 0.55f, cy - r * 0.25f)
                path.lineTo(cx - r * 0.45f, cy + r * 0.55f)
                path.quadTo(cx, cy + r * 0.65f, cx + r * 0.45f, cy + r * 0.55f)
                path.lineTo(cx + r * 0.55f, cy - r * 0.25f)
                path.close()
                // Handle
                addArc(path, cx + r * 0.45f, cy - r * 0.15f, cx + r * 0.85f, cy + r * 0.35f, -90f, 180f)
                // Saucer
                addOval(path, cx - r * 0.75f, cy + r * 0.55f, cx + r * 0.75f, cy + r * 0.75f)
                // Steam curves
                path.moveTo(cx - r * 0.2f, cy - r * 0.4f)
                path.cubicTo(cx - r * 0.3f, cy - r * 0.6f, cx - r * 0.1f, cy - r * 0.8f, cx - r * 0.2f, cy - r * 1.05f)
                path.moveTo(cx + r * 0.15f, cy - r * 0.4f)
                path.cubicTo(cx + r * 0.05f, cy - r * 0.6f, cx + r * 0.25f, cy - r * 0.8f, cx + r * 0.15f, cy - r * 1.05f)
            }

            StencilType.GIFT_BOX -> {
                val r = s * 0.8f
                // Box base
                path.addRect(cx - r * 0.65f, cy - r * 0.25f, cx + r * 0.65f, cy + r * 0.85f, Path.Direction.CW)
                // Lid
                path.addRect(cx - r * 0.75f, cy - r * 0.45f, cx + r * 0.75f, cy - r * 0.25f, Path.Direction.CW)
                // Vertical ribbon
                path.moveTo(cx - r * 0.1f, cy - r * 0.45f)
                path.lineTo(cx - r * 0.1f, cy + r * 0.85f)
                path.moveTo(cx + r * 0.1f, cy - r * 0.45f)
                path.lineTo(cx + r * 0.1f, cy + r * 0.85f)
                // Bow loops on top
                addOval(path, cx - r * 0.4f, cy - r * 0.85f, cx, cy - r * 0.45f)
                addOval(path, cx, cy - r * 0.85f, cx + r * 0.4f, cy - r * 0.45f)
            }

            StencilType.UMBRELLA -> {
                val r = s * 0.85f
                // Umbrella canopy dome
                path.moveTo(cx - r * 0.85f, cy)
                path.cubicTo(cx - r * 0.85f, cy - r * 0.85f, cx + r * 0.85f, cy - r * 0.85f, cx + r * 0.85f, cy)
                // Scallop bottom
                for (i in 0 until 4) {
                    val p1 = cx + r * 0.85f - r * i * 0.425f
                    val p2 = cx + r * 0.85f - r * (i + 1) * 0.425f
                    path.quadTo((p1 + p2) / 2, cy - r * 0.12f, p2, cy)
                }
                // Handle shaft
                path.moveTo(cx, cy - r * 0.8f)
                path.lineTo(cx, cy + r * 0.75f)
                // J hook handle
                addArc(path, cx - r * 0.25f, cy + r * 0.55f, cx + r * 0.05f, cy + r * 0.95f, 0f, 180f)
                // Tip
                path.moveTo(cx, cy - r * 0.8f)
                path.lineTo(cx, cy - r * 0.98f)
            }

            StencilType.BELL -> {
                val r = s * 0.82f
                // Top loop
                addArc(path, cx - r * 0.18f, cy - r * 0.95f, cx + r * 0.18f, cy - r * 0.65f, 180f, 180f)
                // Bell body
                path.moveTo(cx - r * 0.25f, cy - r * 0.65f)
                path.cubicTo(cx - r * 0.35f, cy - r * 0.2f, cx - r * 0.75f, cy + r * 0.45f, cx - r * 0.75f, cy + r * 0.6f)
                path.lineTo(cx + r * 0.75f, cy + r * 0.6f)
                path.cubicTo(cx + r * 0.75f, cy + r * 0.45f, cx + r * 0.35f, cy - r * 0.2f, cx + r * 0.25f, cy - r * 0.65f)
                path.close()
                // Clapper
                path.addCircle(cx, cy + r * 0.8f, r * 0.16f, Path.Direction.CW)
            }

            StencilType.FLOWER_TULIP -> {
                val r = s * 0.85f
                // Tulip petal cups
                path.moveTo(cx, cy + r * 0.35f)
                path.cubicTo(cx - r * 0.65f, cy + r * 0.35f, cx - r * 0.65f, cy - r * 0.55f, cx - r * 0.45f, cy - r * 0.75f)
                path.lineTo(cx - r * 0.15f, cy - r * 0.4f)
                path.lineTo(cx, cy - r * 0.85f)
                path.lineTo(cx + r * 0.15f, cy - r * 0.4f)
                path.lineTo(cx + r * 0.45f, cy - r * 0.75f)
                path.cubicTo(cx + r * 0.65f, cy - r * 0.55f, cx + r * 0.65f, cy + r * 0.35f, cx, cy + r * 0.35f)
                // Stem
                path.moveTo(cx, cy + r * 0.35f)
                path.lineTo(cx, cy + r * 1.05f)
                // Leaves
                addLeaf(path, cx, cy + r * 0.95f, cx - r * 0.55f, cy + r * 0.45f, r * 0.25f)
                addLeaf(path, cx, cy + r * 0.85f, cx + r * 0.55f, cy + r * 0.35f, r * 0.25f)
            }

            // ==========================================
            // --- ĐỘNG VẬT (25 MẪU) ---
            // ==========================================

            StencilType.CAT -> {
                val r = s * 0.85f
                path.addCircle(cx, cy + r * 0.32f, r * 0.52f, Path.Direction.CW)
                path.addCircle(cx, cy - r * 0.35f, r * 0.42f, Path.Direction.CW)
                path.moveTo(cx - r * 0.38f, cy - r * 0.5f)
                path.lineTo(cx - r * 0.48f, cy - r * 0.95f)
                path.lineTo(cx - r * 0.12f, cy - r * 0.72f)
                path.close()
                path.moveTo(cx + r * 0.38f, cy - r * 0.5f)
                path.lineTo(cx + r * 0.48f, cy - r * 0.95f)
                path.lineTo(cx + r * 0.12f, cy - r * 0.72f)
                path.close()
                path.addCircle(cx - r * 0.16f, cy - r * 0.4f, r * 0.05f, Path.Direction.CW)
                path.addCircle(cx + r * 0.16f, cy - r * 0.4f, r * 0.05f, Path.Direction.CW)
                path.moveTo(cx - r * 0.06f, cy - r * 0.28f)
                path.lineTo(cx + r * 0.06f, cy - r * 0.28f)
                path.lineTo(cx, cy - r * 0.22f)
                path.close()
                path.moveTo(cx, cy - r * 0.22f)
                path.quadTo(cx - r * 0.08f, cy - r * 0.14f, cx - r * 0.12f, cy - r * 0.18f)
                path.moveTo(cx, cy - r * 0.22f)
                path.quadTo(cx + r * 0.08f, cy - r * 0.14f, cx + r * 0.12f, cy - r * 0.18f)
                path.moveTo(cx - r * 0.22f, cy - r * 0.25f); path.lineTo(cx - r * 0.55f, cy - r * 0.32f)
                path.moveTo(cx - r * 0.22f, cy - r * 0.2f); path.lineTo(cx - r * 0.55f, cy - r * 0.18f)
                path.moveTo(cx + r * 0.22f, cy - r * 0.25f); path.lineTo(cx + r * 0.55f, cy - r * 0.32f)
                path.moveTo(cx + r * 0.22f, cy - r * 0.2f); path.lineTo(cx + r * 0.55f, cy - r * 0.18f)
                path.moveTo(cx + r * 0.45f, cy + r * 0.55f)
                path.cubicTo(cx + r * 0.9f, cy + r * 0.55f, cx + r * 0.95f, cy - r * 0.1f, cx + r * 0.75f, cy - r * 0.25f)
            }

            StencilType.DOG -> {
                val r = s * 0.85f
                path.addCircle(cx, cy - r * 0.25f, r * 0.5f, Path.Direction.CW)
                path.addCircle(cx, cy + r * 0.45f, r * 0.45f, Path.Direction.CW)
                // Left floppy ear
                path.moveTo(cx - r * 0.4f, cy - r * 0.45f)
                path.cubicTo(cx - r * 0.85f, cy - r * 0.45f, cx - r * 0.85f, cy + r * 0.15f, cx - r * 0.45f, cy + r * 0.05f)
                // Right floppy ear
                path.moveTo(cx + r * 0.4f, cy - r * 0.45f)
                path.cubicTo(cx + r * 0.85f, cy - r * 0.45f, cx + r * 0.85f, cy + r * 0.15f, cx + r * 0.45f, cy + r * 0.05f)
                path.addCircle(cx - r * 0.18f, cy - r * 0.3f, r * 0.06f, Path.Direction.CW)
                path.addCircle(cx + r * 0.18f, cy - r * 0.3f, r * 0.06f, Path.Direction.CW)
                addOval(path, cx - r * 0.15f, cy - r * 0.15f, cx + r * 0.15f, cy)
                path.moveTo(cx, cy)
                path.lineTo(cx, cy + r * 0.1f)
                addArc(path, cx - r * 0.14f, cy + r * 0.02f, cx + r * 0.14f, cy + r * 0.18f, 0f, 180f)
                // Tail
                path.moveTo(cx + r * 0.35f, cy + r * 0.65f)
                path.quadTo(cx + r * 0.85f, cy + r * 0.45f, cx + r * 0.85f, cy + r * 0.25f)
            }

            StencilType.BUTTERFLY -> {
                val r = s * 0.88f
                // Center body
                addOval(path, cx - r * 0.08f, cy - r * 0.45f, cx + r * 0.08f, cy + r * 0.45f)
                path.addCircle(cx, cy - r * 0.55f, r * 0.12f, Path.Direction.CW)
                // Antennae
                path.moveTo(cx, cy - r * 0.65f)
                path.cubicTo(cx - r * 0.15f, cy - r * 0.95f, cx - r * 0.4f, cy - r * 0.85f, cx - r * 0.35f, cy - r * 0.75f)
                path.moveTo(cx, cy - r * 0.65f)
                path.cubicTo(cx + r * 0.15f, cy - r * 0.95f, cx + r * 0.4f, cy - r * 0.85f, cx + r * 0.35f, cy - r * 0.75f)
                // Top wings
                path.moveTo(cx - r * 0.08f, cy - r * 0.2f)
                path.cubicTo(cx - r * 0.65f, cy - r * 0.95f, cx - r * 0.98f, cy - r * 0.45f, cx - r * 0.65f, cy)
                path.lineTo(cx - r * 0.08f, cy)
                path.moveTo(cx + r * 0.08f, cy - r * 0.2f)
                path.cubicTo(cx + r * 0.65f, cy - r * 0.95f, cx + r * 0.98f, cy - r * 0.45f, cx + r * 0.65f, cy)
                path.lineTo(cx + r * 0.08f, cy)
                // Bottom wings
                path.moveTo(cx - r * 0.08f, cy)
                path.cubicTo(cx - r * 0.85f, cy + r * 0.2f, cx - r * 0.6f, cy + r * 0.85f, cx - r * 0.08f, cy + r * 0.45f)
                path.moveTo(cx + r * 0.08f, cy)
                path.cubicTo(cx + r * 0.85f, cy + r * 0.2f, cx + r * 0.6f, cy + r * 0.85f, cx + r * 0.08f, cy + r * 0.45f)
                // Wing spots
                path.addCircle(cx - r * 0.45f, cy - r * 0.35f, r * 0.12f, Path.Direction.CW)
                path.addCircle(cx + r * 0.45f, cy - r * 0.35f, r * 0.12f, Path.Direction.CW)
            }

            StencilType.DINO -> {
                val r = s * 0.85f
                // Dino body
                path.moveTo(cx - r * 0.85f, cy + r * 0.15f)
                path.cubicTo(cx - r * 0.85f, cy - r * 0.65f, cx - r * 0.25f, cy - r * 0.75f, cx, cy - r * 0.75f)
                path.lineTo(cx + r * 0.35f, cy - r * 0.75f)
                path.quadTo(cx + r * 0.55f, cy - r * 0.75f, cx + r * 0.55f, cy - r * 0.55f)
                path.lineTo(cx + r * 0.25f, cy - r * 0.4f)
                path.cubicTo(cx + r * 0.25f, cy - r * 0.1f, cx + r * 0.55f, cy + r * 0.1f, cx + r * 0.55f, cy + r * 0.55f)
                // Legs
                path.lineTo(cx + r * 0.4f, cy + r * 0.95f)
                path.lineTo(cx + r * 0.25f, cy + r * 0.95f)
                path.lineTo(cx + r * 0.25f, cy + r * 0.65f)
                path.lineTo(cx - r * 0.15f, cy + r * 0.65f)
                path.lineTo(cx - r * 0.15f, cy + r * 0.95f)
                path.lineTo(cx - r * 0.3f, cy + r * 0.95f)
                path.lineTo(cx - r * 0.35f, cy + r * 0.6f)
                // Tail
                path.cubicTo(cx - r * 0.65f, cy + r * 0.65f, cx - r * 1.05f, cy + r * 0.35f, cx - r * 0.85f, cy + r * 0.15f)
                // Eye & smile
                path.addCircle(cx + r * 0.4f, cy - r * 0.65f, r * 0.05f, Path.Direction.CW)
            }

            StencilType.DOLPHIN -> {
                val r = s * 0.85f
                path.moveTo(cx - r * 0.85f, cy + r * 0.45f)
                path.cubicTo(cx - r * 0.45f, cy - r * 0.85f, cx + r * 0.45f, cy - r * 0.85f, cx + r * 0.75f, cy - r * 0.15f)
                path.lineTo(cx + r * 0.92f, cy - r * 0.1f)
                path.quadTo(cx + r * 0.75f, cy + r * 0.05f, cx + r * 0.65f, cy + r * 0.1f)
                path.cubicTo(cx + r * 0.35f, cy + r * 0.4f, cx - r * 0.2f, cy + r * 0.4f, cx - r * 0.85f, cy + r * 0.45f)
                // Tail flukes
                path.lineTo(cx - r * 1.05f, cy + r * 0.25f)
                path.lineTo(cx - r * 0.92f, cy + r * 0.48f)
                path.lineTo(cx - r * 1.05f, cy + r * 0.7f)
                path.close()
                // Dorsal fin
                path.moveTo(cx + r * 0.1f, cy - r * 0.75f)
                path.cubicTo(cx + r * 0.05f, cy - r * 1.05f, cx - r * 0.15f, cy - r * 1.0f, cx - r * 0.2f, cy - r * 0.7f)
                // Flipper
                path.moveTo(cx + r * 0.3f, cy + r * 0.1f)
                path.cubicTo(cx + r * 0.25f, cy + r * 0.45f, cx + r * 0.05f, cy + r * 0.4f, cx + r * 0.15f, cy + r * 0.15f)
                path.addCircle(cx + r * 0.62f, cy - r * 0.22f, r * 0.04f, Path.Direction.CW)
            }

            StencilType.PENGUIN -> {
                val r = s * 0.85f
                addOval(path, cx - r * 0.5f, cy - r * 0.85f, cx + r * 0.5f, cy + r * 0.75f)
                addOval(path, cx - r * 0.3f, cy - r * 0.35f, cx + r * 0.3f, cy + r * 0.65f)
                // Flippers
                path.moveTo(cx - r * 0.48f, cy - r * 0.2f)
                path.cubicTo(cx - r * 0.85f, cy + r * 0.05f, cx - r * 0.75f, cy + r * 0.45f, cx - r * 0.4f, cy + r * 0.35f)
                path.moveTo(cx + r * 0.48f, cy - r * 0.2f)
                path.cubicTo(cx + r * 0.85f, cy + r * 0.05f, cx + r * 0.75f, cy + r * 0.45f, cx + r * 0.4f, cy + r * 0.35f)
                // Eyes & beak
                path.addCircle(cx - r * 0.15f, cy - r * 0.5f, r * 0.05f, Path.Direction.CW)
                path.addCircle(cx + r * 0.15f, cy - r * 0.5f, r * 0.05f, Path.Direction.CW)
                path.moveTo(cx - r * 0.08f, cy - r * 0.42f)
                path.lineTo(cx + r * 0.08f, cy - r * 0.42f)
                path.lineTo(cx, cy - r * 0.3f)
                path.close()
                // Feet
                path.moveTo(cx - r * 0.3f, cy + r * 0.75f); path.lineTo(cx - r * 0.15f, cy + r * 0.95f); path.lineTo(cx, cy + r * 0.75f)
                path.moveTo(cx, cy + r * 0.75f); path.lineTo(cx + r * 0.15f, cy + r * 0.95f); path.lineTo(cx + r * 0.3f, cy + r * 0.75f)
            }

            StencilType.RABBIT -> {
                val r = s * 0.85f
                path.addCircle(cx, cy + r * 0.35f, r * 0.48f, Path.Direction.CW)
                path.addCircle(cx, cy - r * 0.22f, r * 0.38f, Path.Direction.CW)
                // Long ears
                addOval(path, cx - r * 0.32f, cy - r * 1.05f, cx - r * 0.08f, cy - r * 0.45f)
                addOval(path, cx + r * 0.08f, cy - r * 1.05f, cx + r * 0.32f, cy - r * 0.45f)
                // Inner ears
                addOval(path, cx - r * 0.26f, cy - r * 0.95f, cx - r * 0.14f, cy - r * 0.55f)
                addOval(path, cx + r * 0.14f, cy - r * 0.95f, cx + r * 0.26f, cy - r * 0.55f)
                // Eyes & nose
                path.addCircle(cx - r * 0.15f, cy - r * 0.25f, r * 0.05f, Path.Direction.CW)
                path.addCircle(cx + r * 0.15f, cy - r * 0.25f, r * 0.05f, Path.Direction.CW)
                path.addCircle(cx, cy - r * 0.12f, r * 0.04f, Path.Direction.CW)
                // Whiskers
                path.moveTo(cx - r * 0.15f, cy - r * 0.1f); path.lineTo(cx - r * 0.45f, cy - r * 0.12f)
                path.moveTo(cx + r * 0.15f, cy - r * 0.1f); path.lineTo(cx + r * 0.45f, cy - r * 0.12f)
            }

            StencilType.PANDA -> {
                val r = s * 0.85f
                // Head
                path.addCircle(cx, cy, r * 0.65f, Path.Direction.CW)
                // Ears
                path.addCircle(cx - r * 0.52f, cy - r * 0.52f, r * 0.22f, Path.Direction.CW)
                path.addCircle(cx + r * 0.52f, cy - r * 0.52f, r * 0.22f, Path.Direction.CW)
                // Eye patches
                addOval(path, cx - r * 0.35f, cy - r * 0.2f, cx - r * 0.1f, cy + r * 0.1f)
                addOval(path, cx + r * 0.1f, cy - r * 0.2f, cx + r * 0.35f, cy + r * 0.1f)
                path.addCircle(cx - r * 0.22f, cy - r * 0.05f, r * 0.05f, Path.Direction.CW)
                path.addCircle(cx + r * 0.22f, cy - r * 0.05f, r * 0.05f, Path.Direction.CW)
                // Nose & mouth
                addOval(path, cx - r * 0.1f, cy + r * 0.18f, cx + r * 0.1f, cy + r * 0.28f)
                addArc(path, cx - r * 0.12f, cy + r * 0.25f, cx + r * 0.12f, cy + r * 0.4f, 0f, 180f)
            }

            StencilType.BEAR -> {
                val r = s * 0.85f
                path.addCircle(cx, cy + r * 0.05f, r * 0.62f, Path.Direction.CW)
                path.addCircle(cx - r * 0.52f, cy - r * 0.48f, r * 0.2f, Path.Direction.CW)
                path.addCircle(cx + r * 0.52f, cy - r * 0.48f, r * 0.2f, Path.Direction.CW)
                path.addCircle(cx - r * 0.2f, cy - r * 0.08f, r * 0.05f, Path.Direction.CW)
                path.addCircle(cx + r * 0.2f, cy - r * 0.08f, r * 0.05f, Path.Direction.CW)
                // Snout
                addOval(path, cx - r * 0.25f, cy + r * 0.12f, cx + r * 0.25f, cy + r * 0.48f)
                addOval(path, cx - r * 0.1f, cy + r * 0.18f, cx + r * 0.1f, cy + r * 0.3f)
                addArc(path, cx - r * 0.12f, cy + r * 0.28f, cx + r * 0.12f, cy + r * 0.42f, 0f, 180f)
            }

            StencilType.LION -> {
                val r = s * 0.85f
                // Mane sun-like petals
                for (i in 0 until 12) {
                    val a = i * 2 * Math.PI / 12
                    val px = (cx + cos(a) * r * 0.72f).toFloat()
                    val py = (cy + sin(a) * r * 0.72f).toFloat()
                    path.addCircle(px, py, r * 0.22f, Path.Direction.CW)
                }
                // Center face
                path.addCircle(cx, cy, r * 0.5f, Path.Direction.CW)
                path.addCircle(cx - r * 0.18f, cy - r * 0.12f, r * 0.05f, Path.Direction.CW)
                path.addCircle(cx + r * 0.18f, cy - r * 0.12f, r * 0.05f, Path.Direction.CW)
                path.moveTo(cx - r * 0.1f, cy + r * 0.08f)
                path.lineTo(cx + r * 0.1f, cy + r * 0.08f)
                path.lineTo(cx, cy + r * 0.2f)
                path.close()
                addArc(path, cx - r * 0.14f, cy + r * 0.15f, cx + r * 0.14f, cy + r * 0.32f, 0f, 180f)
            }

            StencilType.TIGER -> {
                val r = s * 0.85f
                path.addCircle(cx, cy, r * 0.62f, Path.Direction.CW)
                path.addCircle(cx - r * 0.52f, cy - r * 0.45f, r * 0.18f, Path.Direction.CW)
                path.addCircle(cx + r * 0.52f, cy - r * 0.45f, r * 0.18f, Path.Direction.CW)
                // Forehead stripes
                path.moveTo(cx, cy - r * 0.55f); path.lineTo(cx, cy - r * 0.35f)
                path.moveTo(cx - r * 0.18f, cy - r * 0.45f); path.lineTo(cx - r * 0.08f, cy - r * 0.35f)
                path.moveTo(cx + r * 0.18f, cy - r * 0.45f); path.lineTo(cx + r * 0.08f, cy - r * 0.35f)
                // Eyes & nose
                path.addCircle(cx - r * 0.2f, cy - r * 0.05f, r * 0.05f, Path.Direction.CW)
                path.addCircle(cx + r * 0.2f, cy - r * 0.05f, r * 0.05f, Path.Direction.CW)
                path.moveTo(cx - r * 0.1f, cy + r * 0.12f); path.lineTo(cx + r * 0.1f, cy + r * 0.12f); path.lineTo(cx, cy + r * 0.24f); path.close()
            }

            StencilType.MONKEY -> {
                val r = s * 0.85f
                path.addCircle(cx, cy, r * 0.55f, Path.Direction.CW)
                // Big ears
                path.addCircle(cx - r * 0.6f, cy, r * 0.25f, Path.Direction.CW)
                path.addCircle(cx + r * 0.6f, cy, r * 0.25f, Path.Direction.CW)
                // Heart-shaped face
                path.addCircle(cx - r * 0.18f, cy - r * 0.08f, r * 0.22f, Path.Direction.CW)
                path.addCircle(cx + r * 0.18f, cy - r * 0.08f, r * 0.22f, Path.Direction.CW)
                path.addCircle(cx - r * 0.18f, cy - r * 0.08f, r * 0.05f, Path.Direction.CW)
                path.addCircle(cx + r * 0.18f, cy - r * 0.08f, r * 0.05f, Path.Direction.CW)
                addOval(path, cx - r * 0.3f, cy + r * 0.1f, cx + r * 0.3f, cy + r * 0.45f)
                addArc(path, cx - r * 0.16f, cy + r * 0.2f, cx + r * 0.16f, cy + r * 0.38f, 0f, 180f)
            }

            StencilType.ELEPHANT -> {
                val r = s * 0.85f
                // Head
                path.addCircle(cx, cy - r * 0.15f, r * 0.45f, Path.Direction.CW)
                // Big ears
                addOval(path, cx - r * 0.95f, cy - r * 0.45f, cx - r * 0.35f, cy + r * 0.25f)
                addOval(path, cx + r * 0.35f, cy - r * 0.45f, cx + r * 0.95f, cy + r * 0.25f)
                // Trunk curled up
                path.moveTo(cx - r * 0.12f, cy + r * 0.1f)
                path.cubicTo(cx - r * 0.15f, cy + r * 0.75f, cx + r * 0.25f, cy + r * 0.95f, cx + r * 0.35f, cy + r * 0.65f)
                path.cubicTo(cx + r * 0.35f, cy + r * 0.45f, cx + r * 0.1f, cy + r * 0.45f, cx + r * 0.1f, cy + r * 0.1f)
                path.addCircle(cx - r * 0.2f, cy - r * 0.15f, r * 0.05f, Path.Direction.CW)
                path.addCircle(cx + r * 0.2f, cy - r * 0.15f, r * 0.05f, Path.Direction.CW)
            }

            StencilType.GIRAFFE -> {
                val r = s * 0.85f
                // Head
                addOval(path, cx - r * 0.25f, cy - r * 0.85f, cx + r * 0.25f, cy - r * 0.35f)
                // Horns (ossicones)
                path.moveTo(cx - r * 0.15f, cy - r * 0.82f); path.lineTo(cx - r * 0.18f, cy - r * 1.05f)
                path.addCircle(cx - r * 0.18f, cy - r * 1.05f, r * 0.06f, Path.Direction.CW)
                path.moveTo(cx + r * 0.15f, cy - r * 0.82f); path.lineTo(cx + r * 0.18f, cy - r * 1.05f)
                path.addCircle(cx + r * 0.18f, cy - r * 1.05f, r * 0.06f, Path.Direction.CW)
                // Long neck
                path.moveTo(cx - r * 0.15f, cy - r * 0.38f); path.lineTo(cx - r * 0.25f, cy + r * 1.05f)
                path.moveTo(cx + r * 0.15f, cy - r * 0.38f); path.lineTo(cx + r * 0.25f, cy + r * 1.05f)
                // Spots on neck
                path.addCircle(cx, cy + r * 0.15f, r * 0.1f, Path.Direction.CW)
                path.addCircle(cx - r * 0.05f, cy + r * 0.55f, r * 0.11f, Path.Direction.CW)
                path.addCircle(cx + r * 0.05f, cy + r * 0.85f, r * 0.11f, Path.Direction.CW)
            }

            StencilType.FOX -> {
                val r = s * 0.85f
                // Triangular head
                path.moveTo(cx, cy + r * 0.45f)
                path.lineTo(cx - r * 0.65f, cy - r * 0.15f)
                path.lineTo(cx - r * 0.55f, cy - r * 0.95f)
                path.lineTo(cx - r * 0.15f, cy - r * 0.35f)
                path.lineTo(cx + r * 0.15f, cy - r * 0.35f)
                path.lineTo(cx + r * 0.55f, cy - r * 0.95f)
                path.lineTo(cx + r * 0.65f, cy - r * 0.15f)
                path.close()
                // Nose
                path.addCircle(cx, cy + r * 0.4f, r * 0.07f, Path.Direction.CW)
                // Eyes
                path.moveTo(cx - r * 0.3f, cy - r * 0.05f); path.lineTo(cx - r * 0.15f, cy)
                path.moveTo(cx + r * 0.3f, cy - r * 0.05f); path.lineTo(cx + r * 0.15f, cy)
            }

            StencilType.OWL -> {
                val r = s * 0.85f
                // Body & ear tufts
                path.moveTo(cx - r * 0.5f, cy - r * 0.75f)
                path.lineTo(cx - r * 0.35f, cy - r * 0.45f)
                path.lineTo(cx + r * 0.35f, cy - r * 0.45f)
                path.lineTo(cx + r * 0.5f, cy - r * 0.75f)
                path.cubicTo(cx + r * 0.85f, cy + r * 0.2f, cx + r * 0.45f, cy + r * 0.85f, cx, cy + r * 0.85f)
                path.cubicTo(cx - r * 0.45f, cy + r * 0.85f, cx - r * 0.85f, cy + r * 0.2f, cx - r * 0.5f, cy - r * 0.75f)
                // Big eyes
                path.addCircle(cx - r * 0.25f, cy - r * 0.15f, r * 0.2f, Path.Direction.CW)
                path.addCircle(cx + r * 0.25f, cy - r * 0.15f, r * 0.2f, Path.Direction.CW)
                path.addCircle(cx - r * 0.25f, cy - r * 0.15f, r * 0.08f, Path.Direction.CW)
                path.addCircle(cx + r * 0.25f, cy - r * 0.15f, r * 0.08f, Path.Direction.CW)
                // Beak
                path.moveTo(cx - r * 0.08f, cy - r * 0.05f); path.lineTo(cx + r * 0.08f, cy - r * 0.05f); path.lineTo(cx, cy + r * 0.1f); path.close()
            }

            StencilType.FROG -> {
                val r = s * 0.85f
                // Big eyes on top
                path.addCircle(cx - r * 0.42f, cy - r * 0.45f, r * 0.24f, Path.Direction.CW)
                path.addCircle(cx + r * 0.42f, cy - r * 0.45f, r * 0.24f, Path.Direction.CW)
                path.addCircle(cx - r * 0.42f, cy - r * 0.45f, r * 0.08f, Path.Direction.CW)
                path.addCircle(cx + r * 0.42f, cy - r * 0.45f, r * 0.08f, Path.Direction.CW)
                // Wide oval face
                addOval(path, cx - r * 0.75f, cy - r * 0.35f, cx + r * 0.75f, cy + r * 0.45f)
                // Wide frog smile
                addArc(path, cx - r * 0.45f, cy - r * 0.1f, cx + r * 0.45f, cy + r * 0.35f, 0f, 180f)
                // Rosy cheeks
                path.addCircle(cx - r * 0.48f, cy + r * 0.1f, r * 0.06f, Path.Direction.CW)
                path.addCircle(cx + r * 0.48f, cy + r * 0.1f, r * 0.06f, Path.Direction.CW)
            }

            StencilType.CHICK -> {
                val r = s * 0.85f
                // Round body
                path.addCircle(cx, cy + r * 0.15f, r * 0.58f, Path.Direction.CW)
                // Cute head fluff
                path.moveTo(cx, cy - r * 0.43f); path.lineTo(cx - r * 0.08f, cy - r * 0.65f); path.lineTo(cx, cy - r * 0.48f)
                path.lineTo(cx + r * 0.08f, cy - r * 0.65f); path.lineTo(cx, cy - r * 0.43f)
                // Big eyes
                path.addCircle(cx - r * 0.2f, cy, r * 0.06f, Path.Direction.CW)
                path.addCircle(cx + r * 0.2f, cy, r * 0.06f, Path.Direction.CW)
                // Beak
                path.moveTo(cx - r * 0.08f, cy + r * 0.08f); path.lineTo(cx + r * 0.08f, cy + r * 0.08f); path.lineTo(cx, cy + r * 0.22f); path.close()
                // Wings
                addArc(path, cx - r * 0.65f, cy, cx - r * 0.35f, cy + r * 0.45f, 90f, 180f)
                addArc(path, cx + r * 0.35f, cy, cx + r * 0.65f, cy + r * 0.45f, -90f, 180f)
            }

            StencilType.DUCK -> {
                val r = s * 0.85f
                // Floating body
                path.moveTo(cx - r * 0.65f, cy + r * 0.15f)
                path.quadTo(cx - r * 0.85f, cy + r * 0.05f, cx - r * 0.95f, cy - r * 0.15f)
                path.quadTo(cx - r * 0.75f, cy + r * 0.55f, cx + r * 0.15f, cy + r * 0.55f)
                path.quadTo(cx + r * 0.65f, cy + r * 0.55f, cx + r * 0.55f, cy + r * 0.15f)
                path.close()
                // Head
                path.addCircle(cx + r * 0.35f, cy - r * 0.25f, r * 0.28f, Path.Direction.CW)
                // Duck bill
                path.moveTo(cx + r * 0.55f, cy - r * 0.28f)
                path.lineTo(cx + r * 0.95f, cy - r * 0.2f)
                path.lineTo(cx + r * 0.55f, cy - r * 0.12f)
                path.close()
                path.addCircle(cx + r * 0.42f, cy - r * 0.32f, r * 0.04f, Path.Direction.CW)
            }

            StencilType.FISH -> {
                val r = s * 0.85f
                // Fish body outline
                path.moveTo(cx + r * 0.85f, cy)
                path.cubicTo(cx + r * 0.35f, cy - r * 0.65f, cx - r * 0.45f, cy - r * 0.45f, cx - r * 0.65f, cy)
                path.cubicTo(cx - r * 0.45f, cy + r * 0.45f, cx + r * 0.35f, cy + r * 0.65f, cx + r * 0.85f, cy)
                // Tail fin
                path.moveTo(cx - r * 0.65f, cy)
                path.lineTo(cx - r * 1.05f, cy - r * 0.45f)
                path.quadTo(cx - r * 0.85f, cy, cx - r * 1.05f, cy + r * 0.45f)
                path.close()
                // Eye & gill
                path.addCircle(cx + r * 0.55f, cy - r * 0.08f, r * 0.06f, Path.Direction.CW)
                addArc(path, cx + r * 0.25f, cy - r * 0.3f, cx + r * 0.45f, cy + r * 0.3f, -90f, 180f)
            }

            StencilType.OCTOPUS -> {
                val r = s * 0.85f
                // Dome head
                addArc(path, cx - r * 0.55f, cy - r * 0.85f, cx + r * 0.55f, cy + r * 0.15f, 180f, 180f)
                path.lineTo(cx + r * 0.55f, cy)
                path.lineTo(cx - r * 0.55f, cy)
                // Eyes
                path.addCircle(cx - r * 0.2f, cy - r * 0.25f, r * 0.06f, Path.Direction.CW)
                path.addCircle(cx + r * 0.2f, cy - r * 0.25f, r * 0.06f, Path.Direction.CW)
                // 5 Curly tentacles
                for (i in 0 until 5) {
                    val tx = cx - r * 0.48f + r * i * 0.24f
                    path.moveTo(tx, cy)
                    path.cubicTo(tx - r * 0.15f, cy + r * 0.5f, tx + r * 0.15f, cy + r * 0.75f, tx - r * 0.05f, cy + r * 0.95f)
                }
            }

            StencilType.WHALE -> {
                val r = s * 0.85f
                path.moveTo(cx + r * 0.85f, cy + r * 0.15f)
                path.cubicTo(cx + r * 0.85f, cy - r * 0.55f, cx - r * 0.15f, cy - r * 0.55f, cx - r * 0.55f, cy)
                path.cubicTo(cx - r * 0.75f, cy + r * 0.25f, cx - r * 0.95f, cy + r * 0.05f, cx - r * 1.05f, cy - r * 0.15f)
                path.lineTo(cx - r * 0.95f, cy + r * 0.25f)
                path.lineTo(cx - r * 1.05f, cy + r * 0.45f)
                path.cubicTo(cx - r * 0.75f, cy + r * 0.35f, cx - r * 0.25f, cy + r * 0.65f, cx + r * 0.55f, cy + r * 0.55f)
                path.close()
                // Eye & spout
                path.addCircle(cx + r * 0.65f, cy - r * 0.05f, r * 0.04f, Path.Direction.CW)
                path.moveTo(cx + r * 0.2f, cy - r * 0.45f); path.lineTo(cx + r * 0.2f, cy - r * 0.85f)
                path.moveTo(cx + r * 0.2f, cy - r * 0.7f); path.lineTo(cx + r * 0.05f, cy - r * 0.95f)
                path.moveTo(cx + r * 0.2f, cy - r * 0.7f); path.lineTo(cx + r * 0.35f, cy - r * 0.95f)
            }

            StencilType.CRAB -> {
                val r = s * 0.85f
                // Oval shell
                addOval(path, cx - r * 0.55f, cy - r * 0.15f, cx + r * 0.55f, cy + r * 0.45f)
                // Eyes on stalks
                path.moveTo(cx - r * 0.2f, cy - r * 0.15f); path.lineTo(cx - r * 0.2f, cy - r * 0.38f)
                path.addCircle(cx - r * 0.2f, cy - r * 0.42f, r * 0.08f, Path.Direction.CW)
                path.moveTo(cx + r * 0.2f, cy - r * 0.15f); path.lineTo(cx + r * 0.2f, cy - r * 0.38f)
                path.addCircle(cx + r * 0.2f, cy - r * 0.42f, r * 0.08f, Path.Direction.CW)
                // Claws
                path.moveTo(cx - r * 0.45f, cy); path.cubicTo(cx - r * 0.95f, cy - r * 0.15f, cx - r * 0.85f, cy - r * 0.75f, cx - r * 0.55f, cy - r * 0.55f)
                path.moveTo(cx + r * 0.45f, cy); path.cubicTo(cx + r * 0.95f, cy - r * 0.15f, cx + r * 0.85f, cy - r * 0.75f, cx + r * 0.55f, cy - r * 0.55f)
            }

            StencilType.SNAIL -> {
                val r = s * 0.85f
                // Shell concentric spiral
                path.addCircle(cx - r * 0.15f, cy - r * 0.15f, r * 0.52f, Path.Direction.CW)
                path.addCircle(cx - r * 0.15f, cy - r * 0.15f, r * 0.34f, Path.Direction.CW)
                path.addCircle(cx - r * 0.15f, cy - r * 0.15f, r * 0.16f, Path.Direction.CW)
                // Body
                path.moveTo(cx - r * 0.85f, cy + r * 0.45f)
                path.lineTo(cx + r * 0.75f, cy + r * 0.45f)
                path.cubicTo(cx + r * 0.95f, cy + r * 0.45f, cx + r * 0.85f, cy - r * 0.05f, cx + r * 0.65f, cy)
                path.lineTo(cx + r * 0.35f, cy + r * 0.25f)
                // Tentacles (antennae)
                path.moveTo(cx + r * 0.65f, cy); path.lineTo(cx + r * 0.75f, cy - r * 0.35f)
                path.addCircle(cx + r * 0.75f, cy - r * 0.38f, r * 0.04f, Path.Direction.CW)
                path.moveTo(cx + r * 0.58f, cy); path.lineTo(cx + r * 0.62f, cy - r * 0.32f)
                path.addCircle(cx + r * 0.62f, cy - r * 0.35f, r * 0.04f, Path.Direction.CW)
            }

            StencilType.BEE -> {
                val r = s * 0.85f
                // Striped oval body
                addOval(path, cx - r * 0.45f, cy - r * 0.25f, cx + r * 0.45f, cy + r * 0.45f)
                // Stripes
                path.moveTo(cx - r * 0.15f, cy - r * 0.22f); path.lineTo(cx - r * 0.15f, cy + r * 0.42f)
                path.moveTo(cx + r * 0.15f, cy - r * 0.22f); path.lineTo(cx + r * 0.15f, cy + r * 0.42f)
                // Stinger
                path.moveTo(cx - r * 0.45f, cy + r * 0.1f); path.lineTo(cx - r * 0.65f, cy + r * 0.1f); path.lineTo(cx - r * 0.45f, cy + r * 0.18f); path.close()
                // Wings
                addOval(path, cx - r * 0.25f, cy - r * 0.85f, cx + r * 0.1f, cy - r * 0.15f)
                addOval(path, cx + r * 0.05f, cy - r * 0.85f, cx + r * 0.4f, cy - r * 0.15f)
            }

            // ==========================================
            // --- VŨ TRỤ & PHÉP THUẬT (25 MẪU) ---
            // ==========================================

            StencilType.ROCKET -> {
                val r = s * 0.85f
                path.moveTo(cx, cy - r * 1.15f)
                path.cubicTo(cx - r * 0.5f, cy - r * 0.65f, cx - r * 0.45f, cy + r * 0.35f, cx - r * 0.45f, cy + r * 0.45f)
                path.lineTo(cx + r * 0.45f, cy + r * 0.45f)
                path.cubicTo(cx + r * 0.45f, cy + r * 0.35f, cx + r * 0.5f, cy - r * 0.65f, cx, cy - r * 1.15f)
                // Left wing
                path.moveTo(cx - r * 0.45f, cy + r * 0.05f)
                path.lineTo(cx - r * 0.85f, cy + r * 0.55f)
                path.lineTo(cx - r * 0.45f, cy + r * 0.45f)
                // Right wing
                path.moveTo(cx + r * 0.45f, cy + r * 0.05f)
                path.lineTo(cx + r * 0.85f, cy + r * 0.55f)
                path.lineTo(cx + r * 0.45f, cy + r * 0.45f)
                // Window & exhaust flame
                path.addCircle(cx, cy - r * 0.25f, r * 0.2f, Path.Direction.CW)
                path.moveTo(cx - r * 0.25f, cy + r * 0.45f)
                path.lineTo(cx, cy + r * 1.15f)
                path.lineTo(cx + r * 0.25f, cy + r * 0.45f)
            }

            StencilType.MOON_STARS -> {
                val r = s * 0.85f
                // Crescent moon
                path.moveTo(cx - r * 0.1f, cy - r * 0.95f)
                path.cubicTo(cx - r * 0.95f, cy - r * 0.6f, cx - r * 0.95f, cy + r * 0.6f, cx - r * 0.1f, cy + r * 0.95f)
                path.cubicTo(cx - r * 0.55f, cy + r * 0.45f, cx - r * 0.55f, cy - r * 0.45f, cx - r * 0.1f, cy - r * 0.95f)
                // Sparkle stars
                addStar(path, cx + r * 0.45f, cy - r * 0.45f, r * 0.32f)
                addStar(path, cx + r * 0.65f, cy + r * 0.35f, r * 0.22f)
                addStar(path, cx + r * 0.15f, cy + r * 0.15f, r * 0.14f)
            }

            StencilType.CROWN -> {
                val r = s * 0.82f
                // 5 peaks crown
                path.moveTo(cx - r * 0.75f, cy + r * 0.55f)
                path.lineTo(cx - r * 0.85f, cy - r * 0.45f)
                path.lineTo(cx - r * 0.42f, cy + r * 0.05f)
                path.lineTo(cx, cy - r * 0.75f)
                path.lineTo(cx + r * 0.42f, cy + r * 0.05f)
                path.lineTo(cx + r * 0.85f, cy - r * 0.45f)
                path.lineTo(cx + r * 0.75f, cy + r * 0.55f)
                path.close()
                // Jewels on tips
                path.addCircle(cx - r * 0.85f, cy - r * 0.45f, r * 0.08f, Path.Direction.CW)
                path.addCircle(cx, cy - r * 0.75f, r * 0.08f, Path.Direction.CW)
                path.addCircle(cx + r * 0.85f, cy - r * 0.45f, r * 0.08f, Path.Direction.CW)
                // Base band
                path.moveTo(cx - r * 0.75f, cy + r * 0.38f); path.lineTo(cx + r * 0.75f, cy + r * 0.38f)
            }

            StencilType.DIAMOND -> {
                val r = s * 0.85f
                path.moveTo(cx, cy + r * 0.95f)
                path.lineTo(cx - r * 0.85f, cy - r * 0.25f)
                path.lineTo(cx - r * 0.45f, cy - r * 0.85f)
                path.lineTo(cx + r * 0.45f, cy - r * 0.85f)
                path.lineTo(cx + r * 0.85f, cy - r * 0.25f)
                path.close()
                // Facets
                path.moveTo(cx - r * 0.85f, cy - r * 0.25f); path.lineTo(cx + r * 0.85f, cy - r * 0.25f)
                path.moveTo(cx - r * 0.45f, cy - r * 0.85f); path.lineTo(cx - r * 0.25f, cy - r * 0.25f); path.lineTo(cx, cy + r * 0.95f)
                path.moveTo(cx + r * 0.45f, cy - r * 0.85f); path.lineTo(cx + r * 0.25f, cy - r * 0.25f); path.lineTo(cx, cy + r * 0.95f)
            }

            StencilType.MUSIC -> {
                val r = s * 0.82f
                // Note heads
                path.addCircle(cx - r * 0.4f, cy + r * 0.45f, r * 0.22f, Path.Direction.CW)
                path.addCircle(cx + r * 0.35f, cy + r * 0.25f, r * 0.22f, Path.Direction.CW)
                // Stems
                path.moveTo(cx - r * 0.2f, cy + r * 0.45f); path.lineTo(cx - r * 0.2f, cy - r * 0.55f)
                path.moveTo(cx + r * 0.55f, cy + r * 0.25f); path.lineTo(cx + r * 0.55f, cy - r * 0.75f)
                // Connecting beam
                path.moveTo(cx - r * 0.2f, cy - r * 0.45f); path.lineTo(cx + r * 0.55f, cy - r * 0.65f)
                path.lineTo(cx + r * 0.55f, cy - r * 0.78f); path.lineTo(cx - r * 0.2f, cy - r * 0.58f); path.close()
            }

            StencilType.SATURN -> {
                val r = s * 0.85f
                // Planet sphere
                path.addCircle(cx, cy, r * 0.55f, Path.Direction.CW)
                // Tilted ring
                addOval(path, cx - r * 0.98f, cy - r * 0.32f, cx + r * 0.98f, cy + r * 0.32f)
                addOval(path, cx - r * 0.78f, cy - r * 0.22f, cx + r * 0.78f, cy + r * 0.22f)
            }

            StencilType.UFO -> {
                val r = s * 0.82f
                // Glass cockpit dome
                addArc(path, cx - r * 0.45f, cy - r * 0.65f, cx + r * 0.45f, cy - r * 0.05f, 180f, 180f)
                // Saucer disc
                addOval(path, cx - r * 0.95f, cy - r * 0.15f, cx + r * 0.95f, cy + r * 0.25f)
                // Porthole lights
                for (i in -2..2) {
                    path.addCircle(cx + r * i * 0.32f, cy + r * 0.05f, r * 0.06f, Path.Direction.CW)
                }
                // Tractor beam
                path.moveTo(cx - r * 0.35f, cy + r * 0.25f); path.lineTo(cx - r * 0.65f, cy + r * 0.95f)
                path.moveTo(cx + r * 0.35f, cy + r * 0.25f); path.lineTo(cx + r * 0.65f, cy + r * 0.95f)
            }

            StencilType.ASTRONAUT -> {
                val r = s * 0.85f
                // Helmet
                addOval(path, cx - r * 0.55f, cy - r * 0.85f, cx + r * 0.55f, cy - r * 0.05f)
                // Visor
                addOval(path, cx - r * 0.4f, cy - r * 0.7f, cx + r * 0.4f, cy - r * 0.2f)
                // Torso & backpack
                path.addRect(cx - r * 0.45f, cy - r * 0.05f, cx + r * 0.45f, cy + r * 0.65f, Path.Direction.CW)
                path.moveTo(cx - r * 0.45f, cy + r * 0.65f); path.lineTo(cx - r * 0.3f, cy + r * 1.05f)
                path.moveTo(cx + r * 0.45f, cy + r * 0.65f); path.lineTo(cx + r * 0.3f, cy + r * 1.05f)
            }

            StencilType.METEOR -> {
                val r = s * 0.85f
                // Meteor rock
                path.addCircle(cx - r * 0.45f, cy + r * 0.45f, r * 0.35f, Path.Direction.CW)
                // Crater dots
                path.addCircle(cx - r * 0.5f, cy + r * 0.38f, r * 0.08f, Path.Direction.CW)
                path.addCircle(cx - r * 0.35f, cy + r * 0.52f, r * 0.06f, Path.Direction.CW)
                // Flame tail
                path.moveTo(cx - r * 0.35f, cy + r * 0.2f)
                path.lineTo(cx + r * 0.95f, cy - r * 0.85f)
                path.lineTo(cx - r * 0.15f, cy + r * 0.4f)
                path.moveTo(cx - r * 0.25f, cy + r * 0.3f)
                path.lineTo(cx + r * 0.75f, cy - r * 0.55f)
            }

            StencilType.TELESCOPE -> {
                val r = s * 0.82f
                // Barrel tube angled
                path.moveTo(cx - r * 0.65f, cy + r * 0.15f); path.lineTo(cx + r * 0.65f, cy - r * 0.65f)
                path.lineTo(cx + r * 0.75f, cy - r * 0.45f); path.lineTo(cx - r * 0.55f, cy + r * 0.35f); path.close()
                // Lens flare star at end
                addStar(path, cx + r * 0.85f, cy - r * 0.75f, r * 0.2f)
                // Tripod legs
                path.moveTo(cx, cy + r * 0.05f); path.lineTo(cx, cy + r * 0.95f)
                path.moveTo(cx, cy + r * 0.05f); path.lineTo(cx - r * 0.55f, cy + r * 0.95f)
                path.moveTo(cx, cy + r * 0.05f); path.lineTo(cx + r * 0.55f, cy + r * 0.95f)
            }

            StencilType.EARTH -> {
                val r = s * 0.85f
                path.addCircle(cx, cy, r * 0.75f, Path.Direction.CW)
                // Equator & meridian lines
                path.moveTo(cx - r * 0.75f, cy); path.lineTo(cx + r * 0.75f, cy)
                addOval(path, cx - r * 0.35f, cy - r * 0.75f, cx + r * 0.35f, cy + r * 0.75f)
                // Cute mini moon orbiting
                path.addCircle(cx + r * 0.85f, cy - r * 0.65f, r * 0.12f, Path.Direction.CW)
            }

            StencilType.COMET -> {
                val r = s * 0.85f
                path.addCircle(cx + r * 0.45f, cy - r * 0.45f, r * 0.32f, Path.Direction.CW)
                // 3 sweeping tail curves
                path.moveTo(cx + r * 0.25f, cy - r * 0.35f)
                path.cubicTo(cx, cy, cx - r * 0.45f, cy + r * 0.35f, cx - r * 0.95f, cy + r * 0.75f)
                path.moveTo(cx + r * 0.45f, cy - r * 0.15f)
                path.cubicTo(cx + r * 0.1f, cy + r * 0.15f, cx - r * 0.25f, cy + r * 0.55f, cx - r * 0.75f, cy + r * 0.95f)
                path.moveTo(cx + r * 0.35f, cy - r * 0.25f)
                path.cubicTo(cx - r * 0.1f, cy + r * 0.05f, cx - r * 0.5f, cy + r * 0.35f, cx - r * 0.95f, cy + r * 0.55f)
            }

            StencilType.LIGHTNING -> {
                val r = s * 0.88f
                // Sharp zig-zag bolt
                path.moveTo(cx + r * 0.25f, cy - r * 0.95f)
                path.lineTo(cx - r * 0.45f, cy - r * 0.05f)
                path.lineTo(cx - r * 0.05f, cy - r * 0.05f)
                path.lineTo(cx - r * 0.35f, cy + r * 0.95f)
                path.lineTo(cx + r * 0.45f, cy + r * 0.05f)
                path.lineTo(cx + r * 0.05f, cy + r * 0.05f)
                path.close()
            }

            StencilType.GALAXY_SPIRAL -> {
                val r = s * 0.88f
                // Archimedean spiral arms
                for (arm in 0 until 2) {
                    val baseA = arm * Math.PI
                    for (step in 1..40) {
                        val angle = baseA + step * 0.18
                        val rad = r * (step / 40f) * 0.92f
                        val nx = (cx + cos(angle) * rad).toFloat()
                        val ny = (cy + sin(angle) * rad).toFloat()
                        if (step == 1) path.moveTo(nx, ny) else path.lineTo(nx, ny)
                    }
                }
                path.addCircle(cx, cy, r * 0.15f, Path.Direction.CW)
            }

            StencilType.MAGIC_WAND -> {
                val r = s * 0.85f
                // Wand stick
                path.moveTo(cx - r * 0.75f, cy + r * 0.75f); path.lineTo(cx + r * 0.35f, cy - r * 0.35f)
                path.lineTo(cx + r * 0.45f, cy - r * 0.25f); path.lineTo(cx - r * 0.65f, cy + r * 0.85f); path.close()
                // Star at tip
                addStar(path, cx + r * 0.55f, cy - r * 0.55f, r * 0.35f)
                // Sparkles around
                addStar(path, cx + r * 0.85f, cy - r * 0.2f, r * 0.14f)
                addStar(path, cx + r * 0.2f, cy - r * 0.85f, r * 0.14f)
            }

            StencilType.CRYSTAL_BALL -> {
                val r = s * 0.82f
                // Sphere
                path.addCircle(cx, cy - r * 0.15f, r * 0.58f, Path.Direction.CW)
                // Star inside ball
                addStar(path, cx, cy - r * 0.15f, r * 0.22f)
                // Stand base
                path.moveTo(cx - r * 0.45f, cy + r * 0.42f)
                path.lineTo(cx + r * 0.45f, cy + r * 0.42f)
                path.lineTo(cx + r * 0.65f, cy + r * 0.85f)
                path.lineTo(cx - r * 0.65f, cy + r * 0.85f)
                path.close()
            }

            StencilType.SHIELD -> {
                val r = s * 0.85f
                path.moveTo(cx, cy - r * 0.85f)
                path.lineTo(cx + r * 0.65f, cy - r * 0.85f)
                path.lineTo(cx + r * 0.65f, cy + r * 0.15f)
                path.cubicTo(cx + r * 0.65f, cy + r * 0.65f, cx, cy + r * 0.95f, cx, cy + r * 0.95f)
                path.cubicTo(cx, cy + r * 0.95f, cx - r * 0.65f, cy + r * 0.65f, cx - r * 0.65f, cy + r * 0.15f)
                path.lineTo(cx - r * 0.65f, cy - r * 0.85f)
                path.close()
                // Emblem star in center
                addStar(path, cx, cy - r * 0.1f, r * 0.3f)
            }

            StencilType.SWORD -> {
                val r = s * 0.85f
                // Blade
                path.moveTo(cx, cy - r * 1.15f)
                path.lineTo(cx - r * 0.12f, cy - r * 0.95f)
                path.lineTo(cx - r * 0.12f, cy + r * 0.35f)
                path.lineTo(cx + r * 0.12f, cy + r * 0.35f)
                path.lineTo(cx + r * 0.12f, cy - r * 0.95f)
                path.close()
                // Center fuller groove
                path.moveTo(cx, cy - r * 0.95f); path.lineTo(cx, cy + r * 0.3f)
                // Crossguard
                addOval(path, cx - r * 0.45f, cy + r * 0.35f, cx + r * 0.45f, cy + r * 0.48f)
                // Handle & pommel
                path.addRect(cx - r * 0.08f, cy + r * 0.48f, cx + r * 0.08f, cy + r * 0.85f, Path.Direction.CW)
                path.addCircle(cx, cy + r * 0.92f, r * 0.1f, Path.Direction.CW)
            }

            StencilType.CASTLE -> {
                val r = s * 0.85f
                // Main base
                path.addRect(cx - r * 0.55f, cy - r * 0.15f, cx + r * 0.55f, cy + r * 0.85f, Path.Direction.CW)
                // Left tower
                path.addRect(cx - r * 0.85f, cy - r * 0.45f, cx - r * 0.55f, cy + r * 0.85f, Path.Direction.CW)
                path.moveTo(cx - r * 0.85f, cy - r * 0.45f); path.lineTo(cx - r * 0.7f, cy - r * 0.85f); path.lineTo(cx - r * 0.55f, cy - r * 0.45f); path.close()
                // Right tower
                path.addRect(cx + r * 0.55f, cy - r * 0.45f, cx + r * 0.85f, cy + r * 0.85f, Path.Direction.CW)
                path.moveTo(cx + r * 0.55f, cy - r * 0.45f); path.lineTo(cx + r * 0.7f, cy - r * 0.85f); path.lineTo(cx + r * 0.85f, cy - r * 0.45f); path.close()
                // Center door
                addArc(path, cx - r * 0.18f, cy + r * 0.45f, cx + r * 0.18f, cy + r * 0.85f, 180f, 180f)
            }

            StencilType.FIRE_FLAME -> {
                val r = s * 0.85f
                // Outer flame
                path.moveTo(cx, cy + r * 0.95f)
                path.cubicTo(cx - r * 0.85f, cy + r * 0.75f, cx - r * 0.85f, cy - r * 0.15f, cx - r * 0.2f, cy - r * 0.55f)
                path.cubicTo(cx - r * 0.1f, cy - r * 0.75f, cx - r * 0.1f, cy - r * 1.05f, cx, cy - r * 1.15f)
                path.cubicTo(cx + r * 0.1f, cy - r * 0.75f, cx + r * 0.65f, cy - r * 0.55f, cx + r * 0.45f, cy - r * 0.15f)
                path.cubicTo(cx + r * 0.85f, cy + r * 0.15f, cx + r * 0.85f, cy + r * 0.75f, cx, cy + r * 0.95f)
                // Inner core flame
                path.moveTo(cx, cy + r * 0.85f)
                path.cubicTo(cx - r * 0.35f, cy + r * 0.65f, cx - r * 0.35f, cy + r * 0.15f, cx, cy - r * 0.45f)
                path.cubicTo(cx + r * 0.35f, cy + r * 0.15f, cx + r * 0.35f, cy + r * 0.65f, cx, cy + r * 0.85f)
            }

            StencilType.SNOWFLAKE -> {
                val r = s * 0.88f
                // 6 spokes with chevrons
                for (i in 0 until 6) {
                    val a = i * Math.PI / 3
                    val x2 = (cx + cos(a) * r * 0.95f).toFloat()
                    val y2 = (cy + sin(a) * r * 0.95f).toFloat()
                    path.moveTo(cx, cy); path.lineTo(x2, y2)
                    // Chevrons
                    val c1 = 0.55f
                    val cx1 = (cx + cos(a) * r * c1).toFloat()
                    val cy1 = (cy + sin(a) * r * c1).toFloat()
                    val bAngle1 = a + Math.PI / 4
                    val bAngle2 = a - Math.PI / 4
                    path.moveTo(cx1, cy1); path.lineTo((cx1 + cos(bAngle1) * r * 0.2f).toFloat(), (cy1 + sin(bAngle1) * r * 0.2f).toFloat())
                    path.moveTo(cx1, cy1); path.lineTo((cx1 + cos(bAngle2) * r * 0.2f).toFloat(), (cy1 + sin(bAngle2) * r * 0.2f).toFloat())
                }
            }

            StencilType.HOURGLASS -> {
                val r = s * 0.82f
                // Top & bottom caps
                path.addRect(cx - r * 0.65f, cy - r * 0.95f, cx + r * 0.65f, cy - r * 0.8f, Path.Direction.CW)
                path.addRect(cx - r * 0.65f, cy + r * 0.8f, cx + r * 0.65f, cy + r * 0.95f, Path.Direction.CW)
                // Glass bulb contour
                path.moveTo(cx - r * 0.55f, cy - r * 0.8f)
                path.lineTo(cx + r * 0.55f, cy - r * 0.8f)
                path.cubicTo(cx + r * 0.45f, cy - r * 0.2f, cx + r * 0.15f, cy, cx, cy)
                path.cubicTo(cx - r * 0.15f, cy, cx - r * 0.45f, cy - r * 0.2f, cx - r * 0.55f, cy - r * 0.8f)
                path.moveTo(cx, cy)
                path.cubicTo(cx + r * 0.15f, cy, cx + r * 0.45f, cy + r * 0.2f, cx + r * 0.55f, cy + r * 0.8f)
                path.lineTo(cx - r * 0.55f, cy + r * 0.8f)
                path.cubicTo(cx - r * 0.45f, cy + r * 0.2f, cx - r * 0.15f, cy, cx, cy)
            }

            StencilType.COMPASS -> {
                val r = s * 0.85f
                path.addCircle(cx, cy, r * 0.85f, Path.Direction.CW)
                path.addCircle(cx, cy, r * 0.68f, Path.Direction.CW)
                // 4-Point compass star
                path.moveTo(cx, cy - r * 0.65f); path.lineTo(cx + r * 0.12f, cy); path.lineTo(cx, cy + r * 0.65f); path.lineTo(cx - r * 0.12f, cy); path.close()
                path.moveTo(cx - r * 0.65f, cy); path.lineTo(cx, cy - r * 0.12f); path.lineTo(cx + r * 0.65f, cy); path.lineTo(cx, cy + r * 0.12f); path.close()
            }

            StencilType.KEY_MAGIC -> {
                val r = s * 0.85f
                // Top decorative heart loop
                path.addCircle(cx, cy - r * 0.55f, r * 0.35f, Path.Direction.CW)
                path.addCircle(cx, cy - r * 0.55f, r * 0.18f, Path.Direction.CW)
                // Shaft
                path.moveTo(cx, cy - r * 0.2f); path.lineTo(cx, cy + r * 0.95f)
                // Key teeth
                path.moveTo(cx, cy + r * 0.65f); path.lineTo(cx + r * 0.25f, cy + r * 0.65f)
                path.moveTo(cx, cy + r * 0.85f); path.lineTo(cx + r * 0.35f, cy + r * 0.85f); path.lineTo(cx + r * 0.35f, cy + r * 0.75f)
            }

            StencilType.POTION -> {
                val r = s * 0.85f
                // Cork stopper
                path.addRect(cx - r * 0.18f, cy - r * 0.95f, cx + r * 0.18f, cy - r * 0.75f, Path.Direction.CW)
                // Neck
                path.addRect(cx - r * 0.14f, cy - r * 0.75f, cx + r * 0.14f, cy - r * 0.35f, Path.Direction.CW)
                // Round flask body
                path.moveTo(cx - r * 0.14f, cy - r * 0.35f)
                path.cubicTo(cx - r * 0.75f, cy - r * 0.2f, cx - r * 0.75f, cy + r * 0.85f, cx, cy + r * 0.85f)
                path.cubicTo(cx + r * 0.75f, cy + r * 0.85f, cx + r * 0.75f, cy - r * 0.2f, cx + r * 0.14f, cy - r * 0.35f)
                // Bubbles
                path.addCircle(cx - r * 0.2f, cy + r * 0.35f, r * 0.08f, Path.Direction.CW)
                path.addCircle(cx + r * 0.18f, cy + r * 0.45f, r * 0.06f, Path.Direction.CW)
                path.addCircle(cx, cy + r * 0.15f, r * 0.05f, Path.Direction.CW)
            }

            // ==========================================
            // --- MANDALA & HỌA TIẾT (25 MẪU) ---
            // ==========================================

            StencilType.LOTUS -> {
                val r = s * 0.85f
                // Center petal
                path.moveTo(cx, cy + r * 0.55f)
                path.cubicTo(cx - r * 0.35f, cy, cx - r * 0.35f, cy - r * 0.65f, cx, cy - r * 0.95f)
                path.cubicTo(cx + r * 0.35f, cy - r * 0.65f, cx + r * 0.35f, cy, cx, cy + r * 0.55f)
                // Left petals
                path.moveTo(cx, cy + r * 0.55f)
                path.cubicTo(cx - r * 0.65f, cy + r * 0.2f, cx - r * 0.85f, cy - r * 0.35f, cx - r * 0.65f, cy - r * 0.65f)
                path.cubicTo(cx - r * 0.35f, cy - r * 0.2f, cx, cy + r * 0.1f, cx, cy + r * 0.55f)
                // Right petals
                path.moveTo(cx, cy + r * 0.55f)
                path.cubicTo(cx + r * 0.65f, cy + r * 0.2f, cx + r * 0.85f, cy - r * 0.35f, cx + r * 0.65f, cy - r * 0.65f)
                path.cubicTo(cx + r * 0.35f, cy - r * 0.2f, cx, cy + r * 0.1f, cx, cy + r * 0.55f)
                // Base curve
                path.moveTo(cx - r * 0.55f, cy + r * 0.55f)
                path.quadTo(cx, cy + r * 0.85f, cx + r * 0.55f, cy + r * 0.55f)
            }

            StencilType.MANDALA_SUN -> {
                val r = s * 0.88f
                path.addCircle(cx, cy, r * 0.3f, Path.Direction.CW)
                path.addCircle(cx, cy, r * 0.48f, Path.Direction.CW)
                val rays = 12
                for (i in 0 until rays) {
                    val a = i * (2 * Math.PI / rays)
                    val x1 = (cx + cos(a) * r * 0.48f).toFloat()
                    val y1 = (cy + sin(a) * r * 0.48f).toFloat()
                    val x2 = (cx + cos(a) * r * 0.95f).toFloat()
                    val y2 = (cy + sin(a) * r * 0.95f).toFloat()
                    path.moveTo(x1, y1); path.lineTo(x2, y2)
                    addStar(path, x2, y2, r * 0.1f)
                }
            }

            StencilType.SPIDER_WEB -> {
                val maxR = s * 0.95f
                val spokes = 8
                for (i in 0 until spokes) {
                    val a = i * (2 * Math.PI / spokes)
                    path.moveTo(cx, cy); path.lineTo((cx + cos(a) * maxR).toFloat(), (cy + sin(a) * maxR).toFloat())
                }
                for (ring in 1..4) {
                    val curR = maxR * (ring / 4f)
                    for (i in 0 until spokes) {
                        val a1 = i * (2 * Math.PI / spokes)
                        val a2 = (i + 1) * (2 * Math.PI / spokes)
                        val p1x = (cx + cos(a1) * curR).toFloat()
                        val p1y = (cy + sin(a1) * curR).toFloat()
                        val p2x = (cx + cos(a2) * curR).toFloat()
                        val p2y = (cy + sin(a2) * curR).toFloat()
                        if (i == 0) path.moveTo(p1x, p1y)
                        val midA = (a1 + a2) / 2
                        path.quadTo(
                            (cx + cos(midA) * (curR * 0.88f)).toFloat(),
                            (cy + sin(midA) * (curR * 0.88f)).toFloat(),
                            p2x, p2y
                        )
                    }
                }
                path.moveTo(cx + maxR * 0.5f, cy + maxR * 0.25f); path.lineTo(cx + maxR * 0.5f, cy + maxR * 0.65f)
                path.addCircle(cx + maxR * 0.5f, cy + maxR * 0.65f, maxR * 0.08f, Path.Direction.CW)
            }

            StencilType.MANDALA_FLOWER_8 -> {
                val r = s * 0.88f
                path.addCircle(cx, cy, r * 0.2f, Path.Direction.CW)
                for (i in 0 until 8) {
                    val a = i * 2 * Math.PI / 8
                    addPetal(path, cx, cy, r * 0.85f, r * 0.35f, a)
                }
                path.addCircle(cx, cy, r * 0.85f, Path.Direction.CW)
            }

            StencilType.MANDALA_STAR_12 -> {
                val r = s * 0.88f
                path.addCircle(cx, cy, r * 0.3f, Path.Direction.CW)
                addStar(path, cx, cy, r * 0.95f, spikes = 12, innerRatio = 0.6f)
                addStar(path, cx, cy, r * 0.65f, spikes = 12, innerRatio = 0.45f)
            }

            StencilType.MANDALA_ROSE -> {
                val r = s * 0.85f
                for (ring in 1..4) {
                    val cr = r * (ring / 4f)
                    val petals = ring * 4
                    for (i in 0 until petals) {
                        val a = i * 2 * Math.PI / petals
                        val px = (cx + cos(a) * cr * 0.7f).toFloat()
                        val py = (cy + sin(a) * cr * 0.7f).toFloat()
                        addArc(path, px - cr * 0.4f, py - cr * 0.4f, px + cr * 0.4f, py + cr * 0.4f, (a * 180 / Math.PI).toFloat(), 180f)
                    }
                }
            }

            StencilType.MANDALA_HEART_RING -> {
                val r = s * 0.85f
                path.addCircle(cx, cy, r * 0.25f, Path.Direction.CW)
                val count = 8
                for (i in 0 until count) {
                    val a = i * 2 * Math.PI / count
                    val hx = (cx + cos(a) * r * 0.62f).toFloat()
                    val hy = (cy + sin(a) * r * 0.62f).toFloat()
                    addHeartShape(path, hx, hy, r * 0.25f, (a + Math.PI / 2).toFloat())
                }
            }

            StencilType.MANDALA_KALEIDO_6 -> {
                val r = s * 0.88f
                for (i in 0 until 6) {
                    val a = i * Math.PI / 3
                    path.moveTo(cx, cy)
                    path.lineTo((cx + cos(a) * r * 0.95f).toFloat(), (cy + sin(a) * r * 0.95f).toFloat())
                    val midX = (cx + cos(a) * r * 0.5f).toFloat()
                    val midY = (cy + sin(a) * r * 0.5f).toFloat()
                    path.addCircle(midX, midY, r * 0.18f, Path.Direction.CW)
                }
                path.addCircle(cx, cy, r * 0.95f, Path.Direction.CW)
            }

            StencilType.MANDALA_SUNFLOWER -> {
                val r = s * 0.88f
                // Seed center lattice
                path.addCircle(cx, cy, r * 0.45f, Path.Direction.CW)
                for (i in 0 until 16) {
                    val a = i * 2 * Math.PI / 16
                    addPetal(path, cx, cy, r * 0.92f, r * 0.2f, a)
                }
            }

            StencilType.MANDALA_PEACOCK -> {
                val r = s * 0.85f
                // Peacock fan array
                for (i in -4..4) {
                    val a = -Math.PI / 2 + i * (Math.PI / 10)
                    val ex = (cx + cos(a) * r * 0.9f).toFloat()
                    val ey = (cy + sin(a) * r * 0.9f).toFloat()
                    path.moveTo(cx, cy + r * 0.35f); path.lineTo(ex, ey)
                    path.addCircle(ex, ey, r * 0.12f, Path.Direction.CW)
                    path.addCircle(ex, ey, r * 0.05f, Path.Direction.CW)
                }
                // Bird body in front
                path.addCircle(cx, cy + r * 0.4f, r * 0.22f, Path.Direction.CW)
            }

            StencilType.MANDALA_GEOMETRIC -> {
                val r = s * 0.88f
                // Overlapping squares rotated 45 deg
                addSquare(path, cx, cy, r * 0.88f, 0f)
                addSquare(path, cx, cy, r * 0.88f, 45f)
                addSquare(path, cx, cy, r * 0.55f, 0f)
                addSquare(path, cx, cy, r * 0.55f, 45f)
                path.addCircle(cx, cy, r * 0.25f, Path.Direction.CW)
            }

            StencilType.MANDALA_INFINITY -> {
                val r = s * 0.85f
                // Figure 8 infinity loop
                for (t in 0..100) {
                    val a = t * 2 * Math.PI / 100
                    val scale = r * 0.85f
                    val denom = 1 + sin(a) * sin(a)
                    val ix = (cx + scale * cos(a) / denom).toFloat()
                    val iy = (cy + scale * sin(a) * cos(a) / denom).toFloat()
                    if (t == 0) path.moveTo(ix, iy) else path.lineTo(ix, iy)
                }
                path.close()
                // Starburst around center
                addStar(path, cx, cy, r * 0.22f)
            }

            StencilType.MANDALA_YIN_YANG -> {
                val r = s * 0.85f
                path.addCircle(cx, cy, r, Path.Direction.CW)
                // S curve
                addArc(path, cx - r * 0.5f, cy - r, cx + r * 0.5f, cy, 90f, 180f)
                addArc(path, cx - r * 0.5f, cy, cx + r * 0.5f, cy + r, -90f, 180f)
                // Small circles
                path.addCircle(cx, cy - r * 0.5f, r * 0.15f, Path.Direction.CW)
                path.addCircle(cx, cy + r * 0.5f, r * 0.15f, Path.Direction.CW)
            }

            StencilType.MANDALA_CELTIC_KNOT -> {
                val r = s * 0.85f
                // Triquetra (3 overlapping arcs)
                for (i in 0 until 3) {
                    val a = -Math.PI / 2 + i * 2 * Math.PI / 3
                    val ax = (cx + cos(a) * r * 0.35f).toFloat()
                    val ay = (cy + sin(a) * r * 0.35f).toFloat()
                    path.addCircle(ax, ay, r * 0.52f, Path.Direction.CW)
                }
                path.addCircle(cx, cy, r * 0.48f, Path.Direction.CW)
            }

            StencilType.MANDALA_CHAKRA -> {
                val r = s * 0.88f
                path.addCircle(cx, cy, r * 0.3f, Path.Direction.CW)
                for (i in 0 until 16) {
                    val a = i * 2 * Math.PI / 16
                    addPetal(path, cx, cy, r * 0.88f, r * 0.18f, a)
                }
                path.addCircle(cx, cy, r * 0.9f, Path.Direction.CW)
            }

            StencilType.MANDALA_LEAF_WREATH -> {
                val r = s * 0.85f
                path.addCircle(cx, cy, r * 0.65f, Path.Direction.CW)
                val leaves = 12
                for (i in 0 until leaves) {
                    val a = i * 2 * Math.PI / leaves
                    val lx = (cx + cos(a) * r * 0.65f).toFloat()
                    val ly = (cy + sin(a) * r * 0.65f).toFloat()
                    addLeaf(path, lx, ly, (lx + cos(a + 0.4) * r * 0.25f).toFloat(), (ly + sin(a + 0.4) * r * 0.25f).toFloat(), r * 0.12f)
                }
            }

            StencilType.MANDALA_WAVE -> {
                val r = s * 0.88f
                for (ring in 1..3) {
                    val cr = r * (ring / 3f)
                    val waves = ring * 6
                    for (i in 0 until waves) {
                        val a1 = i * 2 * Math.PI / waves
                        val a2 = (i + 1) * 2 * Math.PI / waves
                        val p1x = (cx + cos(a1) * cr).toFloat()
                        val p1y = (cy + sin(a1) * cr).toFloat()
                        val p2x = (cx + cos(a2) * cr).toFloat()
                        val p2y = (cy + sin(a2) * cr).toFloat()
                        if (i == 0) path.moveTo(p1x, p1y)
                        path.quadTo((p1x + p2x) / 2 + cr * 0.1f, (p1y + p2y) / 2 - cr * 0.1f, p2x, p2y)
                    }
                }
            }

            StencilType.MANDALA_HEXAGON -> {
                val r = s * 0.88f
                addPolygon(path, cx, cy, r * 0.92f, 6)
                addPolygon(path, cx, cy, r * 0.68f, 6)
                addPolygon(path, cx, cy, r * 0.44f, 6)
                addPolygon(path, cx, cy, r * 0.2f, 6)
                for (i in 0 until 6) {
                    val a = i * Math.PI / 3
                    path.moveTo(cx, cy); path.lineTo((cx + cos(a) * r * 0.92f).toFloat(), (cy + sin(a) * r * 0.92f).toFloat())
                }
            }

            StencilType.MANDALA_STAR_OF_DAVID -> {
                val r = s * 0.88f
                // 2 Equilateral triangles rotated 60 deg
                addPolygon(path, cx, cy, r * 0.9f, 3, -Math.PI / 2)
                addPolygon(path, cx, cy, r * 0.9f, 3, Math.PI / 2)
                path.addCircle(cx, cy, r * 0.92f, Path.Direction.CW)
                path.addCircle(cx, cy, r * 0.52f, Path.Direction.CW)
            }

            StencilType.MANDALA_DIAMOND_GRID -> {
                val r = s * 0.88f
                for (i in -3..3) {
                    val offset = i * r * 0.28f
                    path.moveTo(cx + offset, cy - r * 0.85f); path.lineTo(cx + offset, cy + r * 0.85f)
                    path.moveTo(cx - r * 0.85f, cy + offset); path.lineTo(cx + r * 0.85f, cy + offset)
                }
                path.addCircle(cx, cy, r * 0.92f, Path.Direction.CW)
            }

            StencilType.MANDALA_SPIRAL_SHELL -> {
                val r = s * 0.88f
                for (i in 1..80) {
                    val a = i * 0.18
                    val rad = r * (i / 80f) * 0.95f
                    val nx = (cx + cos(a) * rad).toFloat()
                    val ny = (cy + sin(a) * rad).toFloat()
                    if (i == 1) path.moveTo(nx, ny) else path.lineTo(nx, ny)
                }
                path.addCircle(cx, cy, r * 0.95f, Path.Direction.CW)
            }

            StencilType.MANDALA_BUTTERFLY_WING -> {
                val r = s * 0.88f
                for (i in 0 until 4) {
                    val a = i * Math.PI / 2 + Math.PI / 4
                    val wx = (cx + cos(a) * r * 0.55f).toFloat()
                    val wy = (cy + sin(a) * r * 0.55f).toFloat()
                    addPetal(path, cx, cy, r * 0.9f, r * 0.35f, a)
                    path.addCircle(wx, wy, r * 0.15f, Path.Direction.CW)
                }
                path.addCircle(cx, cy, r * 0.3f, Path.Direction.CW)
            }

            StencilType.MANDALA_SUN_BURST -> {
                val r = s * 0.88f
                path.addCircle(cx, cy, r * 0.35f, Path.Direction.CW)
                for (i in 0 until 24) {
                    val a = i * 2 * Math.PI / 24
                    val len = if (i % 2 == 0) r * 0.95f else r * 0.65f
                    path.moveTo((cx + cos(a) * r * 0.35f).toFloat(), (cy + sin(a) * r * 0.35f).toFloat())
                    path.lineTo((cx + cos(a) * len).toFloat(), (cy + sin(a) * len).toFloat())
                }
            }

            StencilType.MANDALA_FLOWER_OF_LIFE -> {
                val r = s * 0.85f
                val cr = r * 0.45f
                // Center circle
                path.addCircle(cx, cy, cr, Path.Direction.CW)
                // 6 Surrounding intersecting circles
                for (i in 0 until 6) {
                    val a = i * Math.PI / 3
                    path.addCircle((cx + cos(a) * cr).toFloat(), (cy + sin(a) * cr).toFloat(), cr, Path.Direction.CW)
                }
                path.addCircle(cx, cy, r * 0.92f, Path.Direction.CW)
            }

            StencilType.MANDALA_ZEN_CIRCLE -> {
                val r = s * 0.85f
                // Open Zen Enso calligraphic circle
                addArc(path, cx - r * 0.85f, cy - r * 0.85f, cx + r * 0.85f, cy + r * 0.85f, 25f, 320f)
                addArc(path, cx - r * 0.7f, cy - r * 0.7f, cx + r * 0.7f, cy + r * 0.7f, 35f, 300f)
                // Small serene dot
                path.addCircle(cx, cy, r * 0.08f, Path.Direction.CW)
            }
        }

        return path
    }

    // ==========================================
    // --- VECTOR HELPER PRIMITIVES ---
    // ==========================================

    private fun addStar(path: Path, cx: Float, cy: Float, radius: Float, spikes: Int = 5, innerRatio: Float = 0.45f) {
        val rot = Math.PI / 2 * 3
        val step = Math.PI / spikes
        val innerRadius = radius * innerRatio
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

    private fun addCloud(path: Path, cx: Float, cy: Float, radius: Float) {
        path.addCircle(cx - radius * 0.5f, cy, radius * 0.6f, Path.Direction.CW)
        path.addCircle(cx, cy - radius * 0.35f, radius * 0.75f, Path.Direction.CW)
        path.addCircle(cx + radius * 0.5f, cy, radius * 0.6f, Path.Direction.CW)
    }

    private fun addOval(path: Path, left: Float, top: Float, right: Float, bottom: Float) {
        path.addOval(RectF(left, top, right, bottom), Path.Direction.CW)
    }

    private fun addArc(path: Path, left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float) {
        path.arcTo(RectF(left, top, right, bottom), startAngle, sweepAngle, false)
    }

    private fun addLeaf(path: Path, x1: Float, y1: Float, x2: Float, y2: Float, bend: Float) {
        val mx = (x1 + x2) / 2
        val my = (y1 + y2) / 2
        val dx = x2 - x1
        val dy = y2 - y1
        val len = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
        val nx = -dy / len * bend
        val ny = dx / len * bend
        path.moveTo(x1, y1)
        path.quadTo(mx + nx, my + ny, x2, y2)
        path.quadTo(mx - nx, my - ny, x1, y1)
    }

    private fun addPetal(path: Path, cx: Float, cy: Float, length: Float, width: Float, angle: Double) {
        val tipX = (cx + cos(angle) * length).toFloat()
        val tipY = (cy + sin(angle) * length).toFloat()
        val midR = length * 0.5f
        val perpAngle = angle + Math.PI / 2
        val p1x = (cx + cos(angle) * midR + cos(perpAngle) * width * 0.5f).toFloat()
        val p1y = (cy + sin(angle) * midR + sin(perpAngle) * width * 0.5f).toFloat()
        val p2x = (cx + cos(angle) * midR - cos(perpAngle) * width * 0.5f).toFloat()
        val p2y = (cy + sin(angle) * midR - sin(perpAngle) * width * 0.5f).toFloat()
        path.moveTo(cx, cy)
        path.quadTo(p1x, p1y, tipX, tipY)
        path.quadTo(p2x, p2y, cx, cy)
    }

    private fun addPolygon(path: Path, cx: Float, cy: Float, radius: Float, sides: Int, startAngle: Double = -Math.PI / 2) {
        val step = 2 * Math.PI / sides
        for (i in 0 until sides) {
            val a = startAngle + i * step
            val px = (cx + cos(a) * radius).toFloat()
            val py = (cy + sin(a) * radius).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
    }

    private fun addSquare(path: Path, cx: Float, cy: Float, size: Float, rotDegrees: Float) {
        val half = size / 2
        val rad = Math.toRadians(rotDegrees.toDouble())
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()
        val pts = listOf(
            Pair(-half, -half),
            Pair(half, -half),
            Pair(half, half),
            Pair(-half, half)
        )
        pts.forEachIndexed { i, (px, py) ->
            val rx = cx + px * cosA - py * sinA
            val ry = cy + px * sinA + py * cosA
            if (i == 0) path.moveTo(rx, ry) else path.lineTo(rx, ry)
        }
        path.close()
    }

    private fun addHeartShape(path: Path, cx: Float, cy: Float, radius: Float, rotRad: Float = 0f) {
        val r = radius * 0.6f
        val pts = mutableListOf<Pair<Float, Float>>()
        for (i in 0..30) {
            val t = i * 2 * Math.PI / 30
            val hx = (16 * sin(t).pow(3)).toFloat() / 16f * r
            val hy = -(13 * cos(t) - 5 * cos(2 * t) - 2 * cos(3 * t) - cos(4 * t)).toFloat() / 16f * r
            val rx = (cx + hx * cos(rotRad) - hy * sin(rotRad))
            val ry = (cy + hx * sin(rotRad) + hy * cos(rotRad))
            pts.add(Pair(rx, ry))
        }
        pts.forEachIndexed { idx, (px, py) ->
            if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
    }
}
