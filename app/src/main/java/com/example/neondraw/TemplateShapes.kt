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

            StencilType.HEART -> {
                // Double romantic glowing heart with sparkles
                val r = s * 0.85f
                path.moveTo(cx, cy + r * 0.85f)
                path.cubicTo(cx - r * 1.35f, cy - r * 0.2f, cx - r * 0.8f, cy - r * 1.25f, cx, cy - r * 0.45f)
                path.cubicTo(cx + r * 0.8f, cy - r * 1.25f, cx + r * 1.35f, cy - r * 0.2f, cx, cy + r * 0.85f)

                // Inner baby heart
                val inR = r * 0.5f
                path.moveTo(cx, cy + inR * 0.8f)
                path.cubicTo(cx - inR * 1.35f, cy - inR * 0.2f, cx - inR * 0.8f, cy - inR * 1.25f, cx, cy - inR * 0.45f)
                path.cubicTo(cx + inR * 0.8f, cy - inR * 1.25f, cx + inR * 1.35f, cy - inR * 0.2f, cx, cy + inR * 0.8f)

                // Little sparkle stars near top right
                addStar(path, cx + r * 0.85f, cy - r * 0.65f, r * 0.2f)
                addStar(path, cx - r * 0.9f, cy + r * 0.2f, r * 0.14f)
            }

            StencilType.STAR -> {
                // 5-Point star with inner cute smiling face and eye sparkles
                addStar(path, cx, cy, s * 0.95f)
                // Inner 5-point star
                addStar(path, cx, cy, s * 0.52f)
                // Cute eyes
                path.addCircle(cx - s * 0.16f, cy - s * 0.05f, s * 0.05f, Path.Direction.CW)
                path.addCircle(cx + s * 0.16f, cy - s * 0.05f, s * 0.05f, Path.Direction.CW)
                // Smile arc
                val smileRect = RectF(cx - s * 0.14f, cy - s * 0.02f, cx + s * 0.14f, cy + s * 0.16f)
                path.arcTo(smileRect, 0f, 180f, true)
            }

            StencilType.RAINBOW_CLOUD -> {
                val r = s * 0.85f
                // 4 Rainbow arches
                val arcs = 4
                for (i in 0 until arcs) {
                    val stepR = r * (1f - i * 0.16f)
                    val arcRect = RectF(cx - stepR, cy - stepR * 0.8f, cx + stepR, cy + stepR * 0.8f)
                    path.arcTo(arcRect, 180f, 180f, false)
                }
                // Left cloud
                addCloud(path, cx - r * 0.65f, cy + r * 0.25f, r * 0.38f)
                // Right cloud
                addCloud(path, cx + r * 0.65f, cy + r * 0.25f, r * 0.38f)
            }

            StencilType.CUPCAKE -> {
                val r = s * 0.8f
                // Wrapper base
                path.moveTo(cx - r * 0.65f, cy + r * 0.05f)
                path.lineTo(cx - r * 0.45f, cy + r * 0.95f)
                path.lineTo(cx + r * 0.45f, cy + r * 0.95f)
                path.lineTo(cx + r * 0.65f, cy + r * 0.05f)
                path.close()
                // Vertical lines on wrapper
                path.moveTo(cx - r * 0.25f, cy + r * 0.08f)
                path.lineTo(cx - r * 0.18f, cy + r * 0.92f)
                path.moveTo(cx + r * 0.25f, cy + r * 0.08f)
                path.lineTo(cx + r * 0.18f, cy + r * 0.92f)
                // Swirly frosting layers
                path.moveTo(cx - r * 0.75f, cy + r * 0.05f)
                path.cubicTo(cx - r * 0.9f, cy - r * 0.35f, cx - r * 0.3f, cy - r * 0.45f, cx, cy - r * 0.55f)
                path.cubicTo(cx + r * 0.3f, cy - r * 0.45f, cx + r * 0.9f, cy - r * 0.35f, cx + r * 0.75f, cy + r * 0.05f)
                // Top swirl
                path.moveTo(cx - r * 0.45f, cy - r * 0.4f)
                path.cubicTo(cx - r * 0.4f, cy - r * 0.8f, cx + r * 0.4f, cy - r * 0.8f, cx + r * 0.45f, cy - r * 0.4f)
                // Cherry on top
                path.addCircle(cx, cy - r * 0.92f, r * 0.15f, Path.Direction.CW)
                // Cherry stem
                path.moveTo(cx, cy - r * 1.05f)
                path.quadTo(cx + r * 0.2f, cy - r * 1.35f, cx + r * 0.35f, cy - r * 1.25f)
            }

            StencilType.ICE_CREAM -> {
                val r = s * 0.85f
                // Waffle Cone
                path.moveTo(cx - r * 0.5f, cy)
                path.lineTo(cx, cy + r * 1.05f)
                path.lineTo(cx + r * 0.5f, cy)
                path.close()
                // Waffle lines
                path.moveTo(cx - r * 0.35f, cy + r * 0.3f)
                path.lineTo(cx + r * 0.2f, cy + r * 0.65f)
                path.moveTo(cx + r * 0.35f, cy + r * 0.3f)
                path.lineTo(cx - r * 0.2f, cy + r * 0.65f)
                // Bottom scoop with wavy edge
                path.addCircle(cx, cy - r * 0.2f, r * 0.45f, Path.Direction.CW)
                // Top scoop
                path.addCircle(cx, cy - r * 0.65f, r * 0.35f, Path.Direction.CW)
                // Cherry on top
                path.addCircle(cx, cy - r * 1.1f, r * 0.14f, Path.Direction.CW)
                path.moveTo(cx, cy - r * 1.22f)
                path.quadTo(cx + r * 0.15f, cy - r * 1.45f, cx + r * 0.25f, cy - r * 1.38f)
            }

            StencilType.STRAWBERRY -> {
                val r = s * 0.85f
                // Heart-shaped strawberry body
                path.moveTo(cx, cy + r * 0.95f)
                path.cubicTo(cx - r * 1.05f, cy + r * 0.4f, cx - r * 0.85f, cy - r * 0.6f, cx, cy - r * 0.5f)
                path.cubicTo(cx + r * 0.85f, cy - r * 0.6f, cx + r * 1.05f, cy + r * 0.4f, cx, cy + r * 0.95f)
                // Leaf crown
                path.moveTo(cx - r * 0.6f, cy - r * 0.55f)
                path.lineTo(cx - r * 0.3f, cy - r * 0.85f)
                path.lineTo(cx, cy - r * 0.55f)
                path.lineTo(cx + r * 0.3f, cy - r * 0.85f)
                path.lineTo(cx + r * 0.6f, cy - r * 0.55f)
                // Stem
                path.moveTo(cx, cy - r * 0.55f)
                path.quadTo(cx - r * 0.1f, cy - r * 1.15f, cx + r * 0.1f, cy - r * 1.2f)
                // Cute seeds
                path.addCircle(cx - r * 0.35f, cy - r * 0.1f, r * 0.04f, Path.Direction.CW)
                path.addCircle(cx + r * 0.35f, cy - r * 0.1f, r * 0.04f, Path.Direction.CW)
                path.addCircle(cx, cy + r * 0.15f, r * 0.04f, Path.Direction.CW)
                path.addCircle(cx - r * 0.2f, cy + r * 0.45f, r * 0.04f, Path.Direction.CW)
                path.addCircle(cx + r * 0.2f, cy + r * 0.45f, r * 0.04f, Path.Direction.CW)
            }

            StencilType.CAT -> {
                val r = s * 0.65f
                // Head
                path.addCircle(cx, cy + r * 0.1f, r * 0.85f, Path.Direction.CW)
                // Left Ear
                path.moveTo(cx - r * 0.7f, cy - r * 0.35f)
                path.lineTo(cx - r * 0.95f, cy - r * 1.25f)
                path.lineTo(cx - r * 0.15f, cy - r * 0.75f)
                // Right Ear
                path.moveTo(cx + r * 0.7f, cy - r * 0.35f)
                path.lineTo(cx + r * 0.95f, cy - r * 1.25f)
                path.lineTo(cx + r * 0.15f, cy - r * 0.75f)
                // Inner ears
                path.moveTo(cx - r * 0.6f, cy - r * 0.4f)
                path.lineTo(cx - r * 0.8f, cy - r * 1.05f)
                path.lineTo(cx - r * 0.25f, cy - r * 0.65f)
                path.moveTo(cx + r * 0.6f, cy - r * 0.4f)
                path.lineTo(cx + r * 0.8f, cy - r * 1.05f)
                path.lineTo(cx + r * 0.25f, cy - r * 0.65f)
                // Big Anime Eyes
                path.addCircle(cx - r * 0.35f, cy, r * 0.18f, Path.Direction.CW)
                path.addCircle(cx + r * 0.35f, cy, r * 0.18f, Path.Direction.CW)
                path.addCircle(cx - r * 0.38f, cy - r * 0.05f, r * 0.06f, Path.Direction.CW)
                path.addCircle(cx + r * 0.32f, cy - r * 0.05f, r * 0.06f, Path.Direction.CW)
                // Triangle Nose
                path.moveTo(cx - r * 0.08f, cy + r * 0.22f)
                path.lineTo(cx + r * 0.08f, cy + r * 0.22f)
                path.lineTo(cx, cy + r * 0.32f)
                path.close()
                // Cat mouth
                path.moveTo(cx, cy + r * 0.32f)
                path.quadTo(cx - r * 0.18f, cy + r * 0.48f, cx - r * 0.25f, cy + r * 0.42f)
                path.moveTo(cx, cy + r * 0.32f)
                path.quadTo(cx + r * 0.18f, cy + r * 0.48f, cx + r * 0.25f, cy + r * 0.42f)
                // Whiskers
                path.moveTo(cx - r * 0.45f, cy + r * 0.25f)
                path.lineTo(cx - r * 1.15f, cy + r * 0.15f)
                path.moveTo(cx - r * 0.45f, cy + r * 0.35f)
                path.lineTo(cx - r * 1.15f, cy + r * 0.45f)
                path.moveTo(cx + r * 0.45f, cy + r * 0.25f)
                path.lineTo(cx + r * 1.15f, cy + r * 0.15f)
                path.moveTo(cx + r * 0.45f, cy + r * 0.35f)
                path.lineTo(cx + r * 1.15f, cy + r * 0.45f)
            }

            StencilType.DOG -> {
                val r = s * 0.75f
                // Head outline
                path.addCircle(cx, cy + r * 0.05f, r * 0.75f, Path.Direction.CW)
                // Floppy Left Ear
                path.moveTo(cx - r * 0.55f, cy - r * 0.4f)
                path.cubicTo(cx - r * 1.35f, cy - r * 0.1f, cx - r * 1.25f, cy + r * 0.8f, cx - r * 0.65f, cy + r * 0.5f)
                // Floppy Right Ear
                path.moveTo(cx + r * 0.55f, cy - r * 0.4f)
                path.cubicTo(cx + r * 1.35f, cy - r * 0.1f, cx + r * 1.25f, cy + r * 0.8f, cx + r * 0.65f, cy + r * 0.5f)
                // Eyes
                path.addCircle(cx - r * 0.28f, cy - r * 0.08f, r * 0.14f, Path.Direction.CW)
                path.addCircle(cx + r * 0.28f, cy - r * 0.08f, r * 0.14f, Path.Direction.CW)
                // Snout circle
                path.addCircle(cx, cy + r * 0.3f, r * 0.32f, Path.Direction.CW)
                // Nose
                path.addCircle(cx, cy + r * 0.2f, r * 0.12f, Path.Direction.CW)
                // Mouth & Tongue
                path.moveTo(cx, cy + r * 0.32f)
                path.lineTo(cx, cy + r * 0.45f)
                val tongueRect = RectF(cx - r * 0.12f, cy + r * 0.4f, cx + r * 0.12f, cy + r * 0.65f)
                path.arcTo(tongueRect, 0f, 180f, false)
            }

            StencilType.BUTTERFLY -> {
                val r = s * 0.85f
                // Body
                path.moveTo(cx, cy - r * 0.65f)
                path.lineTo(cx, cy + r * 0.65f)
                path.addCircle(cx, cy - r * 0.72f, r * 0.1f, Path.Direction.CW)
                // Antennae
                path.moveTo(cx, cy - r * 0.78f)
                path.quadTo(cx - r * 0.35f, cy - r * 1.15f, cx - r * 0.5f, cy - r * 1.05f)
                path.moveTo(cx, cy - r * 0.78f)
                path.quadTo(cx + r * 0.35f, cy - r * 1.15f, cx + r * 0.5f, cy - r * 1.05f)
                // Upper Left Wing
                path.moveTo(cx, cy - r * 0.2f)
                path.cubicTo(cx - r * 1.45f, cy - r * 1.1f, cx - r * 1.55f, cy + r * 0.1f, cx, cy + r * 0.1f)
                // Lower Left Wing
                path.moveTo(cx, cy + r * 0.1f)
                path.cubicTo(cx - r * 1.15f, cy + r * 0.95f, cx - r * 0.4f, cy + r * 1.1f, cx, cy + r * 0.5f)
                // Upper Right Wing
                path.moveTo(cx, cy - r * 0.2f)
                path.cubicTo(cx + r * 1.45f, cy - r * 1.1f, cx + r * 1.55f, cy + r * 0.1f, cx, cy + r * 0.1f)
                // Lower Right Wing
                path.moveTo(cx, cy + r * 0.1f)
                path.cubicTo(cx + r * 1.15f, cy + r * 0.95f, cx + r * 0.4f, cy + r * 1.1f, cx, cy + r * 0.5f)
                // Inner Wing Circles / Patterns
                path.addCircle(cx - r * 0.65f, cy - r * 0.35f, r * 0.22f, Path.Direction.CW)
                path.addCircle(cx + r * 0.65f, cy - r * 0.35f, r * 0.22f, Path.Direction.CW)
                path.addCircle(cx - r * 0.45f, cy + r * 0.5f, r * 0.14f, Path.Direction.CW)
                path.addCircle(cx + r * 0.45f, cy + r * 0.5f, r * 0.14f, Path.Direction.CW)
            }

            StencilType.DINO -> {
                val r = s * 0.85f
                // Cute Baby Dino Outline
                path.moveTo(cx - r * 0.85f, cy + r * 0.85f) // Tail tip
                path.quadTo(cx - r * 0.4f, cy + r * 0.25f, cx - r * 0.25f, cy - r * 0.3f) // Back
                path.lineTo(cx - r * 0.1f, cy - r * 0.85f) // Neck
                path.lineTo(cx + r * 0.65f, cy - r * 0.85f) // Head top
                path.lineTo(cx + r * 0.75f, cy - r * 0.5f) // Snout
                path.lineTo(cx + r * 0.25f, cy - r * 0.4f) // Mouth
                path.lineTo(cx + r * 0.15f, cy - r * 0.05f) // Chest
                path.lineTo(cx + r * 0.35f, cy - r * 0.02f) // Tiny Arm
                path.lineTo(cx + r * 0.18f, cy + r * 0.15f)
                path.lineTo(cx + r * 0.35f, cy + r * 0.85f) // Front Leg
                path.lineTo(cx + r * 0.1f, cy + r * 0.85f)
                path.lineTo(cx, cy + r * 0.55f) // Belly
                path.lineTo(cx - r * 0.15f, cy + r * 0.85f) // Back Leg
                path.lineTo(cx - r * 0.35f, cy + r * 0.85f)
                path.close()
                // Eye
                path.addCircle(cx + r * 0.35f, cy - r * 0.65f, r * 0.08f, Path.Direction.CW)
                // Cute back spikes
                path.moveTo(cx - r * 0.25f, cy - r * 0.35f); path.lineTo(cx - r * 0.35f, cy - r * 0.5f); path.lineTo(cx - r * 0.18f, cy - r * 0.55f)
                path.moveTo(cx - r * 0.15f, cy - r * 0.6f); path.lineTo(cx - r * 0.25f, cy - r * 0.78f); path.lineTo(cx - r * 0.08f, cy - r * 0.8f)
            }

            StencilType.DOLPHIN -> {
                val r = s * 0.85f
                // Jumping dolphin
                path.moveTo(cx - r * 1.15f, cy + r * 0.6f) // Tail fluke top
                path.lineTo(cx - r * 0.95f, cy + r * 0.4f)
                path.lineTo(cx - r * 1.15f, cy + r * 0.25f) // Tail fluke bottom
                path.cubicTo(cx - r * 0.5f, cy - r * 0.4f, cx + r * 0.3f, cy - r * 0.85f, cx + r * 0.9f, cy - r * 0.25f) // Arched back
                path.lineTo(cx + r * 1.25f, cy - r * 0.15f) // Snout
                path.cubicTo(cx + r * 0.85f, cy + r * 0.25f, cx + r * 0.2f, cy + r * 0.4f, cx - r * 0.35f, cy + r * 0.3f) // Belly
                path.lineTo(cx - r * 0.95f, cy + r * 0.4f)
                // Dorsal Fin
                path.moveTo(cx + r * 0.05f, cy - r * 0.68f)
                path.lineTo(cx - r * 0.2f, cy - r * 1.05f)
                path.lineTo(cx + r * 0.25f, cy - r * 0.6f)
                // Eye
                path.addCircle(cx + r * 0.75f, cy - r * 0.2f, r * 0.06f, Path.Direction.CW)
                // Ocean waves below
                path.moveTo(cx - r, cy + r * 0.85f)
                path.quadTo(cx - r * 0.5f, cy + r * 0.65f, cx, cy + r * 0.85f)
                path.quadTo(cx + r * 0.5f, cy + r * 0.65f, cx + r, cy + r * 0.85f)
            }

            StencilType.PENGUIN -> {
                val r = s * 0.8f
                // Oval Body
                val bodyRect = RectF(cx - r * 0.75f, cy - r * 0.75f, cx + r * 0.75f, cy + r * 0.95f)
                path.addOval(bodyRect, Path.Direction.CW)
                // Belly Patch
                val bellyRect = RectF(cx - r * 0.48f, cy - r * 0.35f, cx + r * 0.48f, cy + r * 0.85f)
                path.addOval(bellyRect, Path.Direction.CW)
                // Left Wing
                path.moveTo(cx - r * 0.72f, cy - r * 0.15f)
                path.cubicTo(cx - r * 1.2f, cy + r * 0.2f, cx - r * 1.1f, cy + r * 0.6f, cx - r * 0.62f, cy + r * 0.55f)
                // Right Wing
                path.moveTo(cx + r * 0.72f, cy - r * 0.15f)
                path.cubicTo(cx + r * 1.2f, cy + r * 0.2f, cx + r * 1.1f, cy + r * 0.6f, cx + r * 0.62f, cy + r * 0.55f)
                // Eyes
                path.addCircle(cx - r * 0.25f, cy - r * 0.4f, r * 0.1f, Path.Direction.CW)
                path.addCircle(cx + r * 0.25f, cy - r * 0.4f, r * 0.1f, Path.Direction.CW)
                // Triangle Beak
                path.moveTo(cx - r * 0.12f, cy - r * 0.25f)
                path.lineTo(cx + r * 0.12f, cy - r * 0.25f)
                path.lineTo(cx, cy - r * 0.1f)
                path.close()
                // Feet
                path.addOval(RectF(cx - r * 0.5f, cy + r * 0.88f, cx - r * 0.1f, cy + r * 1.08f), Path.Direction.CW)
                path.addOval(RectF(cx + r * 0.1f, cy + r * 0.88f, cx + r * 0.5f, cy + r * 1.08f), Path.Direction.CW)
            }

            StencilType.ROCKET -> {
                val r = s * 0.85f
                // Rocket Body
                path.moveTo(cx, cy - r * 1.25f) // Nose cone tip
                path.cubicTo(cx + r * 0.65f, cy - r * 0.5f, cx + r * 0.65f, cy + r * 0.45f, cx + r * 0.42f, cy + r * 0.8f)
                path.lineTo(cx - r * 0.42f, cy + r * 0.8f)
                path.cubicTo(cx - r * 0.65f, cy + r * 0.45f, cx - r * 0.65f, cy - r * 0.5f, cx, cy - r * 1.25f)
                // Nose cone stripe
                path.moveTo(cx - r * 0.35f, cy - r * 0.65f)
                path.lineTo(cx + r * 0.35f, cy - r * 0.65f)
                // Left Wing
                path.moveTo(cx - r * 0.42f, cy + r * 0.35f)
                path.lineTo(cx - r * 0.88f, cy + r * 0.95f)
                path.lineTo(cx - r * 0.42f, cy + r * 0.8f)
                // Right Wing
                path.moveTo(cx + r * 0.42f, cy + r * 0.35f)
                path.lineTo(cx + r * 0.88f, cy + r * 0.95f)
                path.lineTo(cx + r * 0.42f, cy + r * 0.8f)
                // Circular Porthole Window
                path.addCircle(cx, cy - r * 0.1f, r * 0.26f, Path.Direction.CW)
                path.addCircle(cx, cy - r * 0.1f, r * 0.16f, Path.Direction.CW)
                // Flame exhaust
                path.moveTo(cx - r * 0.25f, cy + r * 0.8f)
                path.lineTo(cx, cy + r * 1.35f)
                path.lineTo(cx + r * 0.25f, cy + r * 0.8f)
            }

            StencilType.MOON_STARS -> {
                val r = s * 0.85f
                // Crescent Moon with cute closed eye smile
                path.moveTo(cx, cy - r)
                path.cubicTo(cx - r * 1.25f, cy - r * 0.6f, cx - r * 1.25f, cy + r * 0.6f, cx, cy + r)
                path.cubicTo(cx - r * 0.35f, cy + r * 0.55f, cx - r * 0.35f, cy - r * 0.55f, cx, cy - r)
                // Sleeping closed eye arc
                val eyeRect = RectF(cx - r * 0.85f, cy - r * 0.15f, cx - r * 0.55f, cy + r * 0.1f)
                path.arcTo(eyeRect, 0f, 180f, true)
                // Twinkling stars
                addStar(path, cx + r * 0.55f, cy - r * 0.45f, r * 0.28f)
                addStar(path, cx + r * 0.78f, cy + r * 0.25f, r * 0.22f)
                addStar(path, cx + r * 0.3f, cy + r * 0.65f, r * 0.16f)
            }

            StencilType.CROWN -> {
                val r = s * 0.85f
                // Royal Crown outline with 5 peaks
                path.moveTo(cx - r * 0.95f, cy + r * 0.55f)
                path.lineTo(cx - r * 0.95f, cy - r * 0.15f) // Peak 1
                path.lineTo(cx - r * 0.5f, cy + r * 0.18f)
                path.lineTo(cx - r * 0.45f, cy - r * 0.55f) // Peak 2
                path.lineTo(cx, cy + r * 0.05f)
                path.lineTo(cx, cy - r * 0.8f) // Center Tall Peak 3
                path.lineTo(cx, cy + r * 0.05f)
                path.lineTo(cx + r * 0.45f, cy - r * 0.55f) // Peak 4
                path.lineTo(cx + r * 0.5f, cy + r * 0.18f)
                path.lineTo(cx + r * 0.95f, cy - r * 0.15f) // Peak 5
                path.lineTo(cx + r * 0.95f, cy + r * 0.55f)
                path.close()
                // Base headband
                path.moveTo(cx - r * 0.95f, cy + r * 0.38f)
                path.lineTo(cx + r * 0.95f, cy + r * 0.38f)
                // Gem circles on peaks
                path.addCircle(cx - r * 0.95f, cy - r * 0.15f, r * 0.08f, Path.Direction.CW)
                path.addCircle(cx - r * 0.45f, cy - r * 0.55f, r * 0.09f, Path.Direction.CW)
                path.addCircle(cx, cy - r * 0.8f, r * 0.11f, Path.Direction.CW)
                path.addCircle(cx + r * 0.45f, cy - r * 0.55f, r * 0.09f, Path.Direction.CW)
                path.addCircle(cx + r * 0.95f, cy - r * 0.15f, r * 0.08f, Path.Direction.CW)
                // Center diamond in headband
                path.addCircle(cx, cy + r * 0.46f, r * 0.07f, Path.Direction.CW)
            }

            StencilType.DIAMOND -> {
                val r = s * 0.85f
                // Top table facet
                path.moveTo(cx - r * 0.55f, cy - r * 0.75f)
                path.lineTo(cx + r * 0.55f, cy - r * 0.75f)
                path.lineTo(cx + r * 0.95f, cy - r * 0.25f)
                path.lineTo(cx, cy + r * 0.95f) // Culet point
                path.lineTo(cx - r * 0.95f, cy - r * 0.25f)
                path.close()
                // Girdle line
                path.moveTo(cx - r * 0.95f, cy - r * 0.25f)
                path.lineTo(cx + r * 0.95f, cy - r * 0.25f)
                // Upper star facets
                path.lineTo(cx + r * 0.35f, cy - r * 0.25f)
                path.lineTo(cx + r * 0.55f, cy - r * 0.75f)
                path.moveTo(cx - r * 0.95f, cy - r * 0.25f)
                path.lineTo(cx - r * 0.35f, cy - r * 0.25f)
                path.lineTo(cx - r * 0.55f, cy - r * 0.75f)
                // Lower pavilion facets
                path.moveTo(cx - r * 0.35f, cy - r * 0.25f)
                path.lineTo(cx, cy + r * 0.95f)
                path.lineTo(cx + r * 0.35f, cy - r * 0.25f)
            }

            StencilType.MUSIC -> {
                val r = s * 0.85f
                // Double eighth notes connected with beam
                path.addOval(RectF(cx - r * 0.65f, cy + r * 0.35f, cx - r * 0.25f, cy + r * 0.75f), Path.Direction.CW)
                path.addOval(RectF(cx + r * 0.25f, cy + r * 0.15f, cx + r * 0.65f, cy + r * 0.55f), Path.Direction.CW)
                // Stems
                path.moveTo(cx - r * 0.25f, cy + r * 0.55f)
                path.lineTo(cx - r * 0.25f, cy - r * 0.65f)
                path.lineTo(cx + r * 0.65f, cy - r * 0.85f)
                path.lineTo(cx + r * 0.65f, cy + r * 0.35f)
                // Secondary beam
                path.moveTo(cx - r * 0.25f, cy - r * 0.45f)
                path.lineTo(cx + r * 0.65f, cy - r * 0.65f)
                // Floating sparkle notes
                addStar(path, cx - r * 0.6f, cy - r * 0.45f, r * 0.18f)
                addStar(path, cx + r * 0.8f, cy - r * 0.25f, r * 0.16f)
            }

            StencilType.LOTUS -> {
                val r = s * 0.85f
                // Center petal
                path.moveTo(cx, cy + r * 0.65f)
                path.cubicTo(cx - r * 0.45f, cy, cx - r * 0.3f, cy - r * 0.9f, cx, cy - r * 1.15f)
                path.cubicTo(cx + r * 0.3f, cy - r * 0.9f, cx + r * 0.45f, cy, cx, cy + r * 0.65f)
                // Left inner petal
                path.moveTo(cx, cy + r * 0.65f)
                path.cubicTo(cx - r * 0.65f, cy + r * 0.2f, cx - r * 0.85f, cy - r * 0.5f, cx - r * 0.65f, cy - r * 0.85f)
                path.cubicTo(cx - r * 0.35f, cy - r * 0.4f, cx - r * 0.1f, cy + r * 0.2f, cx, cy + r * 0.65f)
                // Right inner petal
                path.moveTo(cx, cy + r * 0.65f)
                path.cubicTo(cx + r * 0.65f, cy + r * 0.2f, cx + r * 0.85f, cy - r * 0.5f, cx + r * 0.65f, cy - r * 0.85f)
                path.cubicTo(cx + r * 0.35f, cy - r * 0.4f, cx + r * 0.1f, cy + r * 0.2f, cx, cy + r * 0.65f)
                // Base leaves
                path.moveTo(cx - r * 0.95f, cy + r * 0.65f)
                path.quadTo(cx, cy + r * 0.95f, cx + r * 0.95f, cy + r * 0.65f)
            }

            StencilType.MANDALA_SUN -> {
                val r = s * 0.85f
                // Inner center circle
                path.addCircle(cx, cy, r * 0.3f, Path.Direction.CW)
                path.addCircle(cx, cy, r * 0.48f, Path.Direction.CW)
                // 12 Sun rays with diamond tips
                val rays = 12
                for (i in 0 until rays) {
                    val a = i * (2 * Math.PI / rays)
                    val x1 = (cx + cos(a) * r * 0.48f).toFloat()
                    val y1 = (cy + sin(a) * r * 0.48f).toFloat()
                    val x2 = (cx + cos(a) * r * 0.95f).toFloat()
                    val y2 = (cy + sin(a) * r * 0.95f).toFloat()
                    path.moveTo(x1, y1)
                    path.lineTo(x2, y2)
                    addStar(path, x2, y2, r * 0.1f)
                }
            }

            StencilType.SPIDER_WEB -> {
                val maxR = s * 0.95f
                val spokes = 8
                // Radial lines
                for (i in 0 until spokes) {
                    val a = i * (2 * Math.PI / spokes)
                    path.moveTo(cx, cy)
                    path.lineTo((cx + cos(a) * maxR).toFloat(), (cy + sin(a) * maxR).toFloat())
                }
                // 4 Concentric web rings
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
                // Cute hanging spider
                path.moveTo(cx + maxR * 0.5f, cy + maxR * 0.25f)
                path.lineTo(cx + maxR * 0.5f, cy + maxR * 0.65f)
                path.addCircle(cx + maxR * 0.5f, cy + maxR * 0.65f, maxR * 0.08f, Path.Direction.CW)
            }
        }

        return path
    }

    private fun addStar(path: Path, cx: Float, cy: Float, radius: Float) {
        val spikes = 5
        val rot = Math.PI / 2 * 3
        val step = Math.PI / spikes
        val innerRadius = radius * 0.45f
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
}
