package com.example.neondraw

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var btnUndo: ImageButton
    private lateinit var btnRedo: ImageButton
    private lateinit var btnSymmetry: LinearLayout
    private lateinit var tvSymmetry: TextView
    private lateinit var btnStencil: LinearLayout
    private lateinit var tvStencilBadge: TextView
    private lateinit var btnToggleStencilGuide: ImageButton
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
    private lateinit var brushFireworks: TextView
    private lateinit var brushChips: List<TextView>

    private lateinit var layoutColors: LinearLayout

    // 12 Vibrant Neon Colors (Classic Glow Doodle palette)
    private val neonColors = listOf(
        Color.parseColor("#00FFFF"), // Neon Cyan
        Color.parseColor("#FF007F"), // Neon Pink
        Color.parseColor("#39FF14"), // Lime Green
        Color.parseColor("#FFE600"), // Electric Yellow
        Color.parseColor("#FF5F1F"), // Neon Orange
        Color.parseColor("#BC13FE"), // Electric Purple
        Color.parseColor("#007FFF"), // Sky Azure
        Color.parseColor("#FF1493"), // Deep Rose
        Color.parseColor("#00FF80"), // Mint Glow
        Color.parseColor("#FF3131"), // Neon Red
        Color.parseColor("#9D00FF"), // Neon Violet
        Color.parseColor("#FFFFFF")  // Pure Glowing White
    )

    private var selectedColorView: View? = null

    // Photo picker
    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { loadBackgroundFromUri(it) }
    }

    // Permission request for Android 9 and below
    private val requestWritePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            executeSaveArtwork()
        } else {
            Toast.makeText(this, "Cần cấp quyền ghi bộ nhớ để lưu ảnh vào Thư viện!", Toast.LENGTH_LONG).show()
        }
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
        tvStencilBadge = findViewById(R.id.tvStencilBadge)
        btnToggleStencilGuide = findViewById(R.id.btnToggleStencilGuide)
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
        brushFireworks = findViewById(R.id.brushFireworks)
        brushChips = listOf(brushNeon, brushRainbow, brushStars, brushBeads, brushRibbon, brushFireworks)

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
        btnStencil.setOnClickListener { showTemplateGalleryDialog() }

        // Toggle Stencil Guide Visibility
        btnToggleStencilGuide.setOnClickListener {
            val isVisible = drawingView.toggleStencilGuideVisibility()
            btnToggleStencilGuide.setImageResource(
                if (isVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
            )
            Toast.makeText(
                this,
                if (isVisible) "Đã hiện nét khung mẫu vẽ" else "Đã ẩn nét khung mẫu vẽ",
                Toast.LENGTH_SHORT
            ).show()
        }

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
        btnSave.setOnClickListener { checkAndSaveArtwork() }

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
                R.id.brushFireworks -> Pair(BrushType.FIREWORKS, brushFireworks)
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

        // 1. Rainbow Mode Button (dynamic rainbow colors along any stroke)
        val btnRainbowColor = ImageButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(88, 88).apply {
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

    private fun showTemplateGalleryDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_templates)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val btnClose = dialog.findViewById<ImageButton>(R.id.btnDialogClose)
        val btnRemove = dialog.findViewById<Button>(R.id.btnRemoveStencil)
        val rvTemplates = dialog.findViewById<RecyclerView>(R.id.rvTemplates)

        val chipAll = dialog.findViewById<TextView>(R.id.chipCatAll)
        val chipCute = dialog.findViewById<TextView>(R.id.chipCatCute)
        val chipAnimal = dialog.findViewById<TextView>(R.id.chipCatAnimal)
        val chipMagic = dialog.findViewById<TextView>(R.id.chipCatMagic)
        val chipMandala = dialog.findViewById<TextView>(R.id.chipCatMandala)
        val categoryChips = listOf(chipAll, chipCute, chipAnimal, chipMagic, chipMandala)

        val allTemplates = StencilType.values().filter { it != StencilType.NONE }

        rvTemplates.layoutManager = GridLayoutManager(this, 3)
        val adapter = TemplateAdapter(allTemplates, drawingView.getStencil()) { selected ->
            drawingView.setStencil(selected)
            tvStencilBadge.text = selected.title
            btnToggleStencilGuide.visibility = View.VISIBLE
            btnToggleStencilGuide.setImageResource(R.drawable.ic_visibility)
            Toast.makeText(this, "Đã chọn: ${selected.emoji} ${selected.title}! Hãy vẽ theo khung nhé ✨", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        rvTemplates.adapter = adapter

        fun filterCategory(category: String, activeChip: TextView) {
            categoryChips.forEach { chip ->
                val isSelected = (chip == activeChip)
                chip.setTextColor(if (isSelected) Color.parseColor("#00FFFF") else Color.WHITE)
                chip.setBackgroundResource(R.drawable.bg_glass_pill)
            }
            val filtered = if (category == "Tất cả") allTemplates else allTemplates.filter { it.category == category }
            adapter.updateList(filtered, drawingView.getStencil())
        }

        chipAll.setOnClickListener { filterCategory("Tất cả", chipAll) }
        chipCute.setOnClickListener { filterCategory("Dễ thương", chipCute) }
        chipAnimal.setOnClickListener { filterCategory("Động vật", chipAnimal) }
        chipMagic.setOnClickListener { filterCategory("Vũ trụ", chipMagic) }
        chipMandala.setOnClickListener { filterCategory("Mandala", chipMandala) }

        btnRemove.setOnClickListener {
            drawingView.setStencil(StencilType.NONE)
            tvStencilBadge.text = "Mẫu vẽ"
            btnToggleStencilGuide.visibility = View.GONE
            Toast.makeText(this, "Đã chuyển sang chế độ Vẽ tự do", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
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

    private fun checkAndSaveArtwork() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestWritePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                return
            }
        }
        executeSaveArtwork()
    }

    private fun executeSaveArtwork() {
        try {
            val bitmap = drawingView.exportBitmap()
            val filename = "NeonDraw_${System.currentTimeMillis()}.png"
            var savedUri: Uri? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/NeonDraw/")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    contentResolver.openOutputStream(uri)?.use { out: OutputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.flush()
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                    savedUri = uri
                }
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val neonDir = File(picturesDir, "NeonDraw")
                if (!neonDir.exists()) neonDir.mkdirs()

                val file = File(neonDir, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                }

                MediaScannerConnection.scanFile(this, arrayOf(file.absolutePath), arrayOf("image/png")) { _, scanUri ->
                    savedUri = scanUri
                }
                savedUri = Uri.fromFile(file)
            }

            if (savedUri != null) {
                showSaveSuccessDialog(bitmap, savedUri!!)
            } else {
                Toast.makeText(this, "🎉 Đã lưu thành công vào Bộ sưu tập (Pictures/NeonDraw)", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi khi lưu ảnh: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showSaveSuccessDialog(bitmap: Bitmap, imageUri: Uri) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_save_success)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val imgPreview = dialog.findViewById<ImageView>(R.id.imgSavePreview)
        val btnOpen = dialog.findViewById<Button>(R.id.btnOpenGallery)
        val btnShareSaved = dialog.findViewById<Button>(R.id.btnShareSaved)
        val btnContinue = dialog.findViewById<Button>(R.id.btnContinueDrawing)

        imgPreview.setImageBitmap(bitmap)

        btnOpen.setOnClickListener {
            try {
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(imageUri, "image/png")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(viewIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "Vui lòng mở ứng dụng Bộ sưu tập để xem ảnh!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        btnShareSaved.setOnClickListener {
            dialog.dismiss()
            shareArtwork()
        }

        btnContinue.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun shareArtwork() {
        try {
            val bitmap = drawingView.exportBitmap()
            val cachePath = File(cacheDir, "images")
            if (!cachePath.exists()) cachePath.mkdirs()
            val file = File(cachePath, "neon_artwork_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
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
