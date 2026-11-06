package com.example.neondraw

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
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
import androidx.lifecycle.lifecycleScope
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var btnUndo: ImageButton
    private lateinit var btnRedo: ImageButton
    private lateinit var btnSymmetry: LinearLayout
    private lateinit var tvSymmetry: TextView
    private lateinit var btnLanguage: LinearLayout
    private lateinit var tvLangFlag: TextView
    private lateinit var tvLangCode: TextView
    private lateinit var btnStencil: LinearLayout
    private lateinit var tvStencilBadge: TextView
    private lateinit var btnToggleStencilGuide: ImageButton
    private lateinit var btnPhoto: ImageButton
    private lateinit var btnMusicPill: LinearLayout
    private lateinit var tvMusicTrackName: TextView
    private lateinit var ivMusicIcon: ImageView
    private lateinit var btnQuickMute: ImageView
    private lateinit var btnVideo: View
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
            Toast.makeText(this, getString(R.string.toast_permission_needed), Toast.LENGTH_LONG).show()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        LocaleHelper.init(newBase)
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = AudioManager.STREAM_MUSIC
        LocaleHelper.init(this)
        setContentView(R.layout.activity_main)

        MusicManager.init(this)

        initViews()
        setupTopBar()
        setupLanguageButton()
        setupBrushChips()
        setupBrushSize()
        setupColorPalette()
        setupHistoryListener()
    }

    override fun onResume() {
        super.onResume()
        MusicManager.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        MusicManager.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicManager.release()
    }

    private fun initViews() {
        drawingView = findViewById(R.id.drawingView)
        btnUndo = findViewById(R.id.btnUndo)
        btnRedo = findViewById(R.id.btnRedo)
        btnSymmetry = findViewById(R.id.btnSymmetry)
        tvSymmetry = findViewById(R.id.tvSymmetry)
        btnLanguage = findViewById(R.id.btnLanguage)
        tvLangFlag = findViewById(R.id.tvLangFlag)
        tvLangCode = findViewById(R.id.tvLangCode)
        btnStencil = findViewById(R.id.btnStencil)
        tvStencilBadge = findViewById(R.id.tvStencilBadge)
        btnToggleStencilGuide = findViewById(R.id.btnToggleStencilGuide)
        btnPhoto = findViewById(R.id.btnPhoto)
        btnMusicPill = findViewById(R.id.btnMusicPill)
        tvMusicTrackName = findViewById(R.id.tvMusicTrackName)
        ivMusicIcon = findViewById(R.id.ivMusicIcon)
        btnQuickMute = findViewById(R.id.btnQuickMute)
        btnVideo = findViewById(R.id.btnVideo)
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

    private fun setupLanguageButton() {
        updateLanguageButtonDisplay()
        btnLanguage.setOnClickListener {
            showLanguageDialog()
        }
    }

    private fun updateLanguageButtonDisplay() {
        val isVi = LocaleHelper.isVietnamese()
        tvLangFlag.text = if (isVi) "🇻🇳" else "🇬🇧"
        tvLangCode.text = if (isVi) "VI" else "EN"
    }

    private fun showLanguageDialog() {
        val currentLang = LocaleHelper.getLanguage(this)
        val items = arrayOf(
            getString(R.string.lang_english),
            getString(R.string.lang_vietnamese)
        )
        val checkedItem = if (currentLang == LocaleHelper.LANG_VI) 1 else 0

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_language_title))
            .setSingleChoiceItems(items, checkedItem) { dialog, which ->
                val targetLang = if (which == 1) LocaleHelper.LANG_VI else LocaleHelper.LANG_EN
                if (targetLang != currentLang) {
                    LocaleHelper.setLanguage(this, targetLang)
                    dialog.dismiss()
                    recreate()
                } else {
                    dialog.dismiss()
                }
            }
            .setNegativeButton(getString(R.string.dialog_clear_cancel), null)
            .show()
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
            Toast.makeText(this, "${getString(R.string.symmetry)}: ${next.label}", Toast.LENGTH_SHORT).show()
        }

        // Stencil / Pattern Templates
        btnStencil.setOnClickListener { showTemplateGalleryDialog() }

        // Toggle Stencil Guide Visibility
        btnToggleStencilGuide.setOnClickListener {
            val isVisible = drawingView.toggleStencilGuideVisibility()
            btnToggleStencilGuide.setImageResource(
                if (isVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
            )
        }

        // Import Photo Background
        btnPhoto.setOnClickListener {
            if (drawingView.hasBackgroundPhoto()) {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.photo_bg))
                    .setPositiveButton(getString(R.string.dialog_clear_cancel), null)
                    .setNegativeButton(getString(R.string.clear)) { _, _ -> drawingView.setBackgroundPhoto(null) }
                    .setNeutralButton(getString(R.string.photo_bg)) { _, _ -> pickPhotoLauncher.launch("image/*") }
                    .show()
            } else {
                pickPhotoLauncher.launch("image/*")
            }
        }

        // Music Controller (Pill + Quick Mute)
        MusicManager.onStateChanged = { track, isMuted ->
            updateMusicBarUI(track, isMuted)
        }
        updateMusicBarUI(MusicManager.getCurrentTrack(), MusicManager.isMuted())

        btnMusicPill.setOnClickListener { showMusicDialog() }
        btnQuickMute.setOnClickListener {
            val isMuted = MusicManager.toggleMute(this)
            val msg = if (isMuted) getString(R.string.music_off) else getString(R.string.music_on)
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Video Step-by-Step Drawing
        btnVideo.setOnClickListener { showTimelapseDialog() }

        // Share Artwork
        btnShare.setOnClickListener { shareArtwork() }

        // Save Artwork to Gallery
        btnSave.setOnClickListener { checkAndSaveArtwork() }

        // Clear Canvas
        btnClear.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_clear_title))
                .setMessage(getString(R.string.dialog_clear_msg))
                .setPositiveButton(getString(R.string.dialog_clear_confirm)) { _, _ ->
                    drawingView.clear()
                    Toast.makeText(this, getString(R.string.toast_canvas_cleared), Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(getString(R.string.dialog_clear_cancel), null)
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
                tvBrushSize.text = getString(R.string.size_label, size)
                updatePreviewDot(drawingView.getBrushColor(), size)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        tvBrushSize.text = getString(R.string.size_label, seekBrushSize.progress)
        updatePreviewDot(drawingView.getBrushColor(), seekBrushSize.progress)
    }

    private fun updatePreviewDot(color: Int, size: Int) {
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            setStroke(2, Color.WHITE)
        }
        viewBrushPreview.background = drawable
        val param = viewBrushPreview.layoutParams
        param.width = (size * 1.2f).toInt().coerceIn(24, 72)
        param.height = param.width
        viewBrushPreview.layoutParams = param
    }

    private fun setupColorPalette() {
        layoutColors.removeAllViews()
        val btnSize = (38 * resources.displayMetrics.density).toInt()
        val margin = (4 * resources.displayMetrics.density).toInt()

        for ((idx, color) in neonColors.withIndex()) {
            val colorBtn = View(this).apply {
                val params = LinearLayout.LayoutParams(btnSize, btnSize).apply {
                    setMargins(margin, 0, margin, 0)
                }
                layoutParams = params
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
            Toast.makeText(this, getString(R.string.toast_stencil_selected, selected.emoji, selected.title), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        rvTemplates.adapter = adapter

        fun filterCategory(categoryIndex: Int, activeChip: TextView) {
            categoryChips.forEach { chip ->
                val isSelected = (chip == activeChip)
                chip.setTextColor(if (isSelected) Color.parseColor("#00FFFF") else Color.WHITE)
                chip.setBackgroundResource(R.drawable.bg_glass_pill)
            }
            val filtered = when (categoryIndex) {
                1 -> allTemplates.filter { it.categoryVi == "Dễ thương" }
                2 -> allTemplates.filter { it.categoryVi == "Động vật" }
                3 -> allTemplates.filter { it.categoryVi == "Vũ trụ" }
                4 -> allTemplates.filter { it.categoryVi == "Mandala" }
                else -> allTemplates
            }
            adapter.updateList(filtered, drawingView.getStencil())
        }

        chipAll.setOnClickListener { filterCategory(0, chipAll) }
        chipCute.setOnClickListener { filterCategory(1, chipCute) }
        chipAnimal.setOnClickListener { filterCategory(2, chipAnimal) }
        chipMagic.setOnClickListener { filterCategory(3, chipMagic) }
        chipMandala.setOnClickListener { filterCategory(4, chipMandala) }

        btnRemove.setOnClickListener {
            drawingView.setStencil(StencilType.NONE)
            tvStencilBadge.text = getString(R.string.stencil_badge)
            btnToggleStencilGuide.visibility = View.GONE
            Toast.makeText(this, getString(R.string.toast_freehand_mode), Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, getString(R.string.toast_photo_loaded), Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.toast_photo_error, e.message), Toast.LENGTH_SHORT).show()
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
                val imageFile = File(neonDir, filename)

                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                }

                MediaScannerConnection.scanFile(
                    this,
                    arrayOf(imageFile.absolutePath),
                    arrayOf("image/png")
                ) { _, uri ->
                    savedUri = uri
                }
                savedUri = Uri.fromFile(imageFile)
            }

            val finalUri = savedUri
            if (finalUri != null) {
                Toast.makeText(this, getString(R.string.toast_save_success), Toast.LENGTH_SHORT).show()
                showSaveSuccessDialog(bitmap, finalUri)
            } else {
                Toast.makeText(this, getString(R.string.toast_save_error, "URI null"), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.toast_save_error, e.message), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, getString(R.string.save_success_location), Toast.LENGTH_SHORT).show()
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
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.toast_save_error, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMusicBarUI(track: MusicTrack, isMuted: Boolean) {
        if (isMuted) {
            tvMusicTrackName.text = getString(R.string.music_off)
            tvMusicTrackName.setTextColor(Color.parseColor("#FF5F1F"))
            ivMusicIcon.setColorFilter(Color.parseColor("#FF5F1F"))
            btnQuickMute.setImageResource(R.drawable.ic_volume_off)
            btnQuickMute.setColorFilter(Color.parseColor("#FF5F1F"))
        } else {
            tvMusicTrackName.text = "${track.title} (${track.fileName})"
            tvMusicTrackName.setTextColor(Color.parseColor("#00FFFF"))
            ivMusicIcon.setColorFilter(Color.parseColor("#00FFFF"))
            btnQuickMute.setImageResource(R.drawable.ic_volume_up)
            btnQuickMute.setColorFilter(Color.parseColor("#39FF14"))
        }
    }

    private fun showMusicDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_music)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val switchMusic = dialog.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchMusicToggle)
        val sbVolume = dialog.findViewById<SeekBar>(R.id.sbVolume)
        val tvVolumePercent = dialog.findViewById<TextView>(R.id.tvVolumePercent)
        val container = dialog.findViewById<LinearLayout>(R.id.layoutTracksContainer)
        val btnClose = dialog.findViewById<Button>(R.id.btnCloseMusic)

        val maxVol = MusicManager.getSystemMaxMediaVolume(this).coerceAtLeast(1)
        val curVol = MusicManager.getSystemMediaVolume(this)
        sbVolume.max = maxVol
        sbVolume.progress = curVol
        tvVolumePercent.text = "${(curVol * 100 / maxVol)}%"

        fun renderTracks() {
            container.removeAllViews()
            val currentId = MusicManager.getCurrentTrackId()
            val isMuted = MusicManager.isMuted()

            for (track in MusicManager.TRACKS) {
                val isSelected = track.id == currentId && !isMuted

                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        (50 * resources.displayMetrics.density).toInt()
                    ).apply {
                        setMargins(0, (4 * resources.displayMetrics.density).toInt(), 0, (4 * resources.displayMetrics.density).toInt())
                    }
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    setPadding(
                        (14 * resources.displayMetrics.density).toInt(),
                        0,
                        (14 * resources.displayMetrics.density).toInt(),
                        0
                    )
                    background = ContextCompat.getDrawable(
                        this@MainActivity,
                        if (isSelected) R.drawable.bg_neon_card else R.drawable.bg_glass_pill
                    )

                    val tvIcon = TextView(this@MainActivity).apply {
                        text = if (isSelected) "▶️" else "🎵"
                        textSize = 15f
                    }
                    val tvName = TextView(this@MainActivity).apply {
                        text = "${track.title} (${track.fileName})"
                        textSize = 14f
                        setTextColor(if (isSelected) Color.parseColor("#00FFFF") else Color.WHITE)
                        setTypeface(typeface, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                            marginStart = (10 * resources.displayMetrics.density).toInt()
                        }
                    }

                    val tvStatus = TextView(this@MainActivity).apply {
                        text = if (isSelected) getString(R.string.music_on) else ""
                        textSize = 11f
                        setTextColor(Color.parseColor("#39FF14"))
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                    }

                    addView(tvIcon)
                    addView(tvName)
                    addView(tvStatus)

                    setOnClickListener {
                        MusicManager.playTrack(this@MainActivity, track.id)
                        switchMusic.isChecked = true
                        renderTracks()
                    }
                }
                container.addView(row)
            }
        }

        renderTracks()

        switchMusic.setOnCheckedChangeListener { _, isChecked ->
            MusicManager.setMute(this, !isChecked)
            renderTracks()
        }

        sbVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val pct = (progress * 100 / maxVol)
                    tvVolumePercent.text = "$pct%"
                    MusicManager.setSystemMediaVolume(this@MainActivity, progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Listen for phone volume buttons pressed
        val volumeReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val updatedVol = MusicManager.getSystemMediaVolume(this@MainActivity)
                sbVolume.progress = updatedVol
                tvVolumePercent.text = "${(updatedVol * 100 / maxVol)}%"
            }
        }
        val filter = android.content.IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        try {
            registerReceiver(volumeReceiver, filter)
        } catch (_: Exception) {}

        dialog.setOnDismissListener {
            try {
                unregisterReceiver(volumeReceiver)
            } catch (_: Exception) {}
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showTimelapseDialog() {
        val totalSteps = drawingView.getTotalSteps().coerceAtLeast(1)

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_timelapse)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.94).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val ivPreview = dialog.findViewById<ImageView>(R.id.ivTimelapsePreview)
        val ivCenterPlay = dialog.findViewById<ImageView>(R.id.ivCenterPlay)
        val sbProgress = dialog.findViewById<SeekBar>(R.id.sbTimelapseProgress)
        val tvProgressText = dialog.findViewById<TextView>(R.id.tvTimelapseProgressText)
        val tvStepIndicator = dialog.findViewById<TextView>(R.id.tvStepIndicator)
        val btnPrevStep = dialog.findViewById<ImageButton>(R.id.btnPrevStep)
        val btnPlayPause = dialog.findViewById<ImageButton>(R.id.btnPlayPause)
        val btnNextStep = dialog.findViewById<ImageButton>(R.id.btnNextStep)
        val btnReplay = dialog.findViewById<ImageButton>(R.id.btnReplay)
        val btnSpeed1x = dialog.findViewById<TextView>(R.id.btnSpeed1x)
        val btnSpeed2x = dialog.findViewById<TextView>(R.id.btnSpeed2x)
        val btnSpeed4x = dialog.findViewById<TextView>(R.id.btnSpeed4x)
        val btnExport = dialog.findViewById<Button>(R.id.btnExportMp4)
        val pbExport = dialog.findViewById<ProgressBar>(R.id.pbExportVideo)
        val tvExportStatus = dialog.findViewById<TextView>(R.id.tvExportStatus)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnCloseTimelapse)

        var speed = 1.0f
        var currentProgress = 0.05f
        var isPlaying = true
        var currentBitmap: Bitmap? = null

        fun updateStepUI() {
            val currentStep = (currentProgress * totalSteps).toInt().coerceIn(1, totalSteps)
            tvStepIndicator.text = getString(R.string.step_counter, currentStep, totalSteps)
            tvProgressText.text = "${(currentProgress * 100).toInt()}%"
            sbProgress.progress = (currentProgress * 1000).toInt()

            val previewBmp = drawingView.renderProgressToBitmap(currentProgress, 540, 960)
            currentBitmap?.recycle()
            currentBitmap = previewBmp
            ivPreview.setImageBitmap(previewBmp)
        }

        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val updateFrameRunnable = object : Runnable {
            override fun run() {
                if (!isPlaying) return

                val step = (0.012f * speed)
                currentProgress += step
                if (currentProgress >= 1f) {
                    currentProgress = 1f
                    isPlaying = false
                    btnPlayPause.setImageResource(R.drawable.ic_play)
                    ivCenterPlay.visibility = View.VISIBLE
                }

                updateStepUI()

                if (isPlaying) {
                    handler.postDelayed(this, 33)
                }
            }
        }

        fun updateSpeedButtons(activeSpeed: Float) {
            speed = activeSpeed
            btnSpeed1x.setTextColor(if (activeSpeed == 1f) Color.parseColor("#00FFFF") else Color.WHITE)
            btnSpeed2x.setTextColor(if (activeSpeed == 2f) Color.parseColor("#00FFFF") else Color.WHITE)
            btnSpeed4x.setTextColor(if (activeSpeed == 4f) Color.parseColor("#00FFFF") else Color.WHITE)
        }

        btnSpeed1x.setOnClickListener { updateSpeedButtons(1.0f) }
        btnSpeed2x.setOnClickListener { updateSpeedButtons(2.0f) }
        btnSpeed4x.setOnClickListener { updateSpeedButtons(4.0f) }

        // Previous step
        btnPrevStep.setOnClickListener {
            isPlaying = false
            handler.removeCallbacks(updateFrameRunnable)
            btnPlayPause.setImageResource(R.drawable.ic_play)
            ivCenterPlay.visibility = View.VISIBLE

            val stepDelta = 1f / totalSteps
            currentProgress = (currentProgress - stepDelta).coerceAtLeast(0.02f)
            updateStepUI()
        }

        // Next step
        btnNextStep.setOnClickListener {
            isPlaying = false
            handler.removeCallbacks(updateFrameRunnable)
            btnPlayPause.setImageResource(R.drawable.ic_play)

            val stepDelta = 1f / totalSteps
            currentProgress = (currentProgress + stepDelta).coerceAtMost(1f)
            if (currentProgress >= 1f) {
                ivCenterPlay.visibility = View.VISIBLE
            }
            updateStepUI()
        }

        btnPlayPause.setOnClickListener {
            if (isPlaying) {
                isPlaying = false
                handler.removeCallbacks(updateFrameRunnable)
                btnPlayPause.setImageResource(R.drawable.ic_play)
                ivCenterPlay.visibility = View.VISIBLE
            } else {
                if (currentProgress >= 1f) {
                    currentProgress = 0.02f
                }
                isPlaying = true
                btnPlayPause.setImageResource(R.drawable.ic_pause)
                ivCenterPlay.visibility = View.GONE
                handler.post(updateFrameRunnable)
            }
        }

        ivCenterPlay.setOnClickListener {
            btnPlayPause.performClick()
        }

        btnReplay.setOnClickListener {
            currentProgress = 0.02f
            isPlaying = true
            btnPlayPause.setImageResource(R.drawable.ic_pause)
            ivCenterPlay.visibility = View.GONE
            handler.removeCallbacks(updateFrameRunnable)
            handler.post(updateFrameRunnable)
        }

        sbProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentProgress = progress / 1000f
                    updateStepUI()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (isPlaying) {
                    isPlaying = false
                    handler.removeCallbacks(updateFrameRunnable)
                    btnPlayPause.setImageResource(R.drawable.ic_play)
                }
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Export MP4 Video
        btnExport.setOnClickListener {
            isPlaying = false
            handler.removeCallbacks(updateFrameRunnable)
            btnPlayPause.setImageResource(R.drawable.ic_play)

            btnExport.isEnabled = false
            pbExport.visibility = View.VISIBLE
            tvExportStatus.visibility = View.VISIBLE
            tvExportStatus.text = getString(R.string.exporting_video, 0)

            lifecycleScope.launchWhenStarted {
                val videoUri = VideoExporter.exportDrawingTimelapse(
                    this@MainActivity,
                    drawingView,
                    durationSeconds = 6
                ) { percent ->
                    pbExport.progress = percent
                    tvExportStatus.text = getString(R.string.exporting_video, percent)
                }

                btnExport.isEnabled = true
                pbExport.visibility = View.GONE
                tvExportStatus.visibility = View.GONE

                if (videoUri != null) {
                    Toast.makeText(this@MainActivity, getString(R.string.video_export_success), Toast.LENGTH_LONG).show()
                    showVideoSuccessDialog(videoUri)
                } else {
                    Toast.makeText(this@MainActivity, getString(R.string.video_export_error), Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.setOnDismissListener {
            isPlaying = false
            handler.removeCallbacks(updateFrameRunnable)
            currentBitmap?.recycle()
            currentBitmap = null
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        // Start playback immediately
        btnPlayPause.setImageResource(R.drawable.ic_pause)
        handler.post(updateFrameRunnable)

        dialog.show()
    }

    private fun showVideoSuccessDialog(videoUri: Uri) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.video_replay_title))
            .setMessage(getString(R.string.video_export_success))
            .setPositiveButton(getString(R.string.btn_view_photo)) { _, _ ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(videoUri, "video/mp4")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton(getString(R.string.share)) { _, _ ->
                try {
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "video/mp4"
                        putExtra(Intent.EXTRA_STREAM, videoUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(share, getString(R.string.share)))
                } catch (e: Exception) {
                    Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.btn_continue_drawing), null)
            .show()
    }
}
