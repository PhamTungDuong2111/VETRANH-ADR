package com.example.neondraw

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var drawingView: DrawingView
    private lateinit var btnClear: Button
    private lateinit var btnColor: Button
    private lateinit var seekSize: SeekBar
    private lateinit var fabClear: FloatingActionButton

    // Vibrant neon palette
    private val colors = listOf(
        Color.parseColor("#00FFFF"), // Neon Cyan
        Color.parseColor("#FF007F"), // Neon Pink / Rose
        Color.parseColor("#39FF14"), // Neon Green
        Color.parseColor("#FFE600"), // Neon Yellow
        Color.parseColor("#BC13FE"), // Neon Purple
        Color.parseColor("#FF5F1F"), // Neon Orange
        Color.parseColor("#FFFFFF")  // Pure White
    )
    private var colorIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawingView)
        btnClear = findViewById(R.id.btnClear)
        btnColor = findViewById(R.id.btnColor)
        seekSize = findViewById(R.id.seekSize)
        fabClear = findViewById(R.id.fabClear)

        // Set initial brush width & color
        drawingView.setStrokeWidth(seekSize.progress.toFloat().coerceAtLeast(8f))
        drawingView.setStrokeColor(colors[colorIndex])
        btnColor.setTextColor(colors[colorIndex])

        btnClear.setOnClickListener { drawingView.clear() }
        fabClear.setOnClickListener { drawingView.clear() }

        btnColor.setOnClickListener {
            colorIndex = (colorIndex + 1) % colors.size
            val currentColor = colors[colorIndex]
            drawingView.setStrokeColor(currentColor)
            btnColor.setTextColor(currentColor)
        }

        seekSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                drawingView.setStrokeWidth(progress.toFloat().coerceAtLeast(8f))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}
