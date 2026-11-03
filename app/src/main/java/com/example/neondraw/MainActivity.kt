package com.example.neondraw

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var btnUndo: ImageButton
    private lateinit var btnRedo: ImageButton
    private lateinit var btnSymmetry: LinearLayout
    private lateinit var tvSymmetry: TextView
    private lateinit var btnStencil: ImageButton
    private lateinit var btnPhoto: ImageButton
    private lateinit var btnShare: ImageButton
    private lateinit var btnSave: ImageButton
    private lateinit var btnClear: ImageButton

    private lateinit var seekBrushSize: SeekBar
    private lateinit var tvBrushSize: TextView
    private lateinit var viewBrushPreview: View

    private lateinit var brushNeon: TextView
    private lateinit var brushRainbow: TextView
    private lateinit var brushStars: TextView
    private lateinit var brushBeads: TextView
    private lateinit var brushRibbon: TextView
    private lateinit var brushChips: List<TextView>

    private lateinit var layoutColors: LinearLayout

    // Vibrant Neon Palette (similar to iOS Neon Draw app)
    private val neonColors = listOf(
        Color.parseColor("#00FFFF"), // Neon Cyan
        Color.parseColor("#FF007F"), // Neon Pink / Rose
        Color.parseColor("#39FF14"), // Neon Green
        Color.parseColor("#FFE600"), // Neon Yellow
        Color.parseColor("#FF5F1F"), // Electric Orange
        Color.parseColor("#BC13FE"), // Electric Purple
        Color.parseColor("#007FFF"), // Deep Azure Blue
        Color.parseColor("#FF1493"), // Deep Pink
        Color.parseColor("#00FF80"), // Mint Neon
        Color.parseColor("#FFFFFF")  // Pure Glowing White
    )

    private var selectedColorView: View? = null

    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { loadBackgroundFromUri(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupTopBar()
        setupBrushChips()
        setupBrushSize()
        setupColorPalette()
        setupHistoryListener()
    }

    private fun initViews() {
        drawingView = findViewById(R.id.drawingView)
        btnUndo = findViewById(R.id.btnUndo)
        btnRedo = findViewById(R.id.btnRedo)
        btnSymmetry = findViewById(R.id.btnSymmetry)
        tvSymmetry = findViewById(R.id.tvSymmetry)
        btnStencil = findViewById(R.id.btnStencil)
        btnPhoto = findViewById(R.id.btnPhoto)
        btnShare = findViewById(R.id.btnShare)
        btnSave = findViewById(R.id.btnSave)
        btnClear = findViewById(R.id.btnClear)

        seekBrushSize = findViewById(R.id.seekBrushSize)
        tvBrushSize = findViewById(R.id.tvBrushSize)
        viewBrushPreview = findViewById(R.id.viewBrushPreview)

        brushNeon = findViewById(R.id.brushNeon)
        brushRainbow = findViewById(R.id.brushRainbow)
        brushStars = findViewById(R.id.brushStars)
        brushBeads = findViewById(R.id.brushBeads)
        brushRibbon = findViewById(R.id.brushRibbon)
        brushChips = listOf(brushNeon, brushRainbow, brushStars, brushBeads, brushRibbon)

        layoutColors = findViewById(R.id.layoutColors)
    }

    private fun setupTopBar() {
        btnUndo.setOnClickListener { drawingView.undo() }
        btnRedo.setOnClickListener { drawingView.redo() }

        // Symmetry mode cycler: 1x -> 2x -> 4x -> 8x -> 1x
        btnSymmetry.setOnClickListener {
            val next = when (drawingView.getSymmetryMode()) {
                SymmetryMode.NONE -> SymmetryMode.MIRROR_2
                SymmetryMode.MIRROR_2 -> SymmetryMode.MIRROR_4
                SymmetryMode.MIRROR_4 -> SymmetryMode.KALEIDOSCOPE_8
                SymmetryMode.KALEIDOSCOPE_8 -> SymmetryMode.NONE
            }
            drawingView.setSymmetryMode(next)
            tvSymmetry.text = next.label
            val color = if (next == SymmetryMode.NONE) Color.WHITE else Color.parseColor("#00FFFF")
            tvSymmetry.setTextColor(color)
            Toast.makeText(this, "Chế độ đối xứng: ${next.label}", Toast.LENGTH_SHORT).show()
        }

        // Stencil / Pattern Templates
        btnStencil.setOnClickListener { showStencilDialog() }

        // Import Photo Background
        btnPhoto.setOnClickListener {
            if (drawingView.hasBackgroundPhoto()) {
                AlertDialog.Builder(this)
                    .setTitle("Ảnh nền")
                    .setMessage("Bạn muốn đổi ảnh nền khác hay xóa ảnh nền hiện tại?")
                    .setPositiveButton("Đổi ảnh") { _, _ -> pickPhotoLauncher.launch("image/*") }
                    .setNegativeButton("Xóa ảnh") { _, _ -> drawingView.setBackgroundPhoto(null) }
                    .setNeutralButton("Hủy", null)
                    .show()
            } else {
                pickPhotoLauncher.launch("image/*")
            }
        }

        // Share Artwork
        btnShare.setOnClickListener { shareArtwork() }

        // Save Artwork to Gallery
        btnSave.setOnClickListener { saveArtworkToGallery() }

        // Clear Canvas
        btnClear.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Xóa bản vẽ")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ bản vẽ hiện tại?")
                .setPositiveButton("Xóa") { _, _ -> drawingView.clear() }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    private fun setupBrushChips() {
        val clickListener = View.OnClickListener { v ->
            val (type, chip) = when (v.id) {
                R.id.brushRainbow -> Pair(BrushType.RAINBOW, brushRainbow)
                R.id.brushStars -> Pair(BrushType.STARS, brushStars)
                R.id.brushBeads -> Pair(BrushType.BEADS, brushBeads)
                R.id.brushRibbon -> Pair(BrushType.RIBBON, brushRibbon)
                else -> Pair(BrushType.NEON, brushNeon)
            }
            drawingView.setBrushType(type)
            updateBrushChipsSelection(chip)
        }

        brushChips.forEach { it.setOnClickListener(clickListener) }
        updateBrushChipsSelection(brushNeon)
    }

    private fun updateBrushChipsSelection(selectedChip: TextView) {
        for (chip in brushChips) {
            val isSelected = (chip == selectedChip)
            chip.setTextColor(if (isSelected) Color.parseColor("#00FFFF") else Color.WHITE)
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 48f
                setColor(if (isSelected) Color.parseColor("#4000FFFF") else Color.parseColor("#26FFFFFF"))
                setStroke(2, if (isSelected) Color.parseColor("#00FFFF") else Color.parseColor("#4DFFFFFF"))
            }
            chip.background = drawable
        }
    }

    private fun setupBrushSize() {
        seekBrushSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val size = progress.coerceAtLeast(8)
                drawingView.setBrushWidth(size.toFloat())
                tvBrushSize.text = "$size px"
                updatePreviewDot(drawingView.getBrushColor(), size)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        updatePreviewDot(drawingView.getBrushColor(), seekBrushSize.progress)
    }

    private fun updatePreviewDot(color: Int, size: Int) {
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            setStroke(2, Color.WHITE)
        }
        viewBrushPreview.background = drawable
        val displaySize = (size / 2).coerceIn(12, 32)
        val params = viewBrushPreview.layoutParams
        params.width = displaySize
        params.height = displaySize
        viewBrushPreview.layoutParams = params
    }

    private fun setupColorPalette() {
        layoutColors.removeAllViews()

        // 1. Rainbow Mode Button (dynamic rainbow brush for any stroke)
        val btnRainbowColor = ImageButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(90, 90).apply {
                marginEnd = 16
            }
            setImageResource(R.drawable.ic_rainbow)
            setColorFilter(Color.WHITE)
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                colors = intArrayOf(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
                setStroke(4, Color.TRANSPARENT)
            }
            background = bg
            setOnClickListener {
                drawingView.setRainbowMode(true)
                highlightColorSelection(this)
                Toast.makeText(this@MainActivity, "Chế độ màu cầu vồng 🌈", Toast.LENGTH_SHORT).show()
            }
        }
        layoutColors.addView(btnRainbowColor)

        // 2. Individual Neon Colors
        for ((idx, color) in neonColors.withIndex()) {
            val colorBtn = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(80, 80).apply {
                    marginEnd = 16
                }
                val bg = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                    setStroke(if (idx == 0) 6 else 2, if (idx == 0) Color.WHITE else Color.parseColor("#44FFFFFF"))
                }
                background = bg
                setOnClickListener {
                    drawingView.setBrushColor(color)
                    highlightColorSelection(this)
                    updatePreviewDot(color, seekBrushSize.progress)
                    seekBrushSize.thumbTintList = android.content.res.ColorStateList.valueOf(color)
                    seekBrushSize.progressTintList = android.content.res.ColorStateList.valueOf(color)
                }
            }
            if (idx == 0) selectedColorView = colorBtn
            layoutColors.addView(colorBtn)
        }
    }

    private fun highlightColorSelection(view: View) {
        selectedColorView?.let { prev ->
            val bg = prev.background as? GradientDrawable
            bg?.setStroke(2, Color.parseColor("#44FFFFFF"))
        }
        val currentBg = view.background as? GradientDrawable
        currentBg?.setStroke(6, Color.WHITE)
        selectedColorView = view
    }

    private fun setupHistoryListener() {
        drawingView.onHistoryChanged = { canUndo, canRedo ->
            btnUndo.isEnabled = canUndo
            btnUndo.alpha = if (canUndo) 1.0f else 0.35f

            btnRedo.isEnabled = canRedo
            btnRedo.alpha = if (canRedo) 1.0f else 0.35f
        }
        btnUndo.isEnabled = false
        btnUndo.alpha = 0.35f
        btnRedo.isEnabled = false
        btnRedo.alpha = 0.35f
    }

    private fun showStencilDialog() {
        val stencilNames = arrayOf(
            "Tự do (Không mẫu)",
            "❤️ Trái tim (Heart)",
            "⭐ Ngôi sao (Star)",
            "🦋 Con bướm (Butterfly)",
            "🐱 Mèo con (Cat)",
            "👑 Vương miện (Crown)",
            "🌸 Bông hoa (Flower)"
        )
        val stencils = arrayOf(
            StencilType.NONE,
            StencilType.HEART,
            StencilType.STAR,
            StencilType.BUTTERFLY,
            StencilType.CAT,
            StencilType.CROWN,
            StencilType.FLOWER
        )

        AlertDialog.Builder(this)
            .setTitle("Chọn mẫu vẽ neon")
            .setItems(stencilNames) { _, which ->
                drawingView.setStencil(stencils[which])
                Toast.makeText(this, "Đã chọn: ${stencilNames[which]}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Đóng", null)
            .show()
    }

    private fun loadBackgroundFromUri(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { stream ->
                val original = BitmapFactory.decodeStream(stream)
                original?.let {
                    drawingView.setBackgroundPhoto(it)
                    Toast.makeText(this, "Đã thêm ảnh nền! Hãy vẽ neon lên ảnh ✨", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể tải ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveArtworkToGallery() {
        try {
            val bitmap = drawingView.exportBitmap()
            val filename = "NeonDraw_${System.currentTimeMillis()}.png"
            var outputStream: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NeonDraw")
                }
                val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                imageUri?.let { outputStream = contentResolver.openOutputStream(it) }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/NeonDraw"
                val file = File(imagesDir)
                if (!file.exists()) file.mkdirs()
                val imageFile = File(imagesDir, filename)
                outputStream = FileOutputStream(imageFile)
            }

            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                Toast.makeText(this, "🎉 Đã lưu tranh vào Thư viện ảnh!", Toast.LENGTH_LONG).show()
            } ?: run {
                Toast.makeText(this, "Lỗi khi lưu ảnh!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi lưu: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareArtwork() {
        try {
            val bitmap = drawingView.exportBitmap()
            val cachePath = File(cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "neon_artwork_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val contentUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ tranh Neon Art"))
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi chia sẻ: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
