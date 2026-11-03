package com.example.neondraw

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var drawingView: DrawingView
    private lateinit var btnClear: Button
    private lateinit var btnColor: Button
    private lateinit var seekSize: SeekBar

    // A simple palette of neon colors
    private val colors = listOf(
        Color.CYAN,
        Color.MAGENTA,
        Color.YELLOW,
        Color.WHITE,
        Color.parseColor("#FF00FF"), // magenta-like
        Color.parseColor("#00FFFF")  // cyan-like
    )
    private var colorIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawingView)
        btnClear = findViewById(R.id.btnClear)
        btnColor = findViewById(R.id.btnColor)
        seekSize = findViewById(R.id.seekSize)

        // 初始化属性
        drawingView.setStrokeWidth(seekSize.progress.toFloat())
        drawingView.setStrokeColor(colors[colorIndex])

        btnClear.setOnClickListener { drawingView.clear() }
        btnColor.setOnClickListener { 
            colorIndex = (colorIndex + 1) % colors.size
            drawingView.setStrokeColor(colors[colorIndex])
        }
        seekSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                drawingView.setStrokeWidth(progress.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}
