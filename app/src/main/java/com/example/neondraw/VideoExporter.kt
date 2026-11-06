package com.example.neondraw

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer

object VideoExporter {
    private const val TAG = "VideoExporter"
    private const val MIME_TYPE = "video/avc" // H.264
    private const val FRAME_RATE = 30
    private const val I_FRAME_INTERVAL = 1

    suspend fun exportDrawingTimelapse(
        context: Context,
        drawingView: DrawingView,
        durationSeconds: Int = 5,
        onProgress: (Int) -> Unit
    ): Uri? = withContext(Dispatchers.IO) {
        val width = 720
        val height = 1280
        val totalFrames = durationSeconds * FRAME_RATE
        val bitRate = 4_000_000

        val tempFile = File(context.cacheDir, "timelapse_${System.currentTimeMillis()}.mp4")

        var mediaCodec: MediaCodec? = null
        var mediaMuxer: MediaMuxer? = null
        var trackIndex = -1
        var muxerStarted = false

        try {
            val format = MediaFormat.createVideoFormat(MIME_TYPE, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
            }

            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE)
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mediaCodec.start()

            mediaMuxer = MediaMuxer(tempFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val bufferInfo = MediaCodec.BufferInfo()
            val argbPixels = IntArray(width * height)
            val yuvBuffer = ByteArray(width * height * 3 / 2)

            for (frame in 0 until totalFrames) {
                val progress = (frame + 1).toFloat() / totalFrames
                val bitmap = drawingView.renderProgressToBitmap(progress, width, height)

                // Scale bitmap to frame
                bitmap.getPixels(argbPixels, 0, width, 0, 0, width, height)
                bitmap.recycle()

                encodeYUV420SP(yuvBuffer, argbPixels, width, height)

                // Send to encoder
                var inputIndex = mediaCodec.dequeueInputBuffer(10000)
                while (inputIndex < 0) {
                    drainEncoder(mediaCodec, mediaMuxer, bufferInfo, { trackIndex = it }, { muxerStarted = it }, muxerStarted)
                    inputIndex = mediaCodec.dequeueInputBuffer(10000)
                }

                val inputBuffer = mediaCodec.getInputBuffer(inputIndex)
                inputBuffer?.clear()
                inputBuffer?.put(yuvBuffer)
                val ptsUsec = (frame * 1_000_000L) / FRAME_RATE
                mediaCodec.queueInputBuffer(inputIndex, 0, yuvBuffer.size, ptsUsec, 0)

                drainEncoder(mediaCodec, mediaMuxer, bufferInfo, { trackIndex = it }, { muxerStarted = it }, muxerStarted)

                withContext(Dispatchers.Main) {
                    val percent = ((frame + 1) * 90) / totalFrames
                    onProgress(percent)
                }
            }

            // End of stream
            var inputIndex = mediaCodec.dequeueInputBuffer(10000)
            while (inputIndex < 0) {
                drainEncoder(mediaCodec, mediaMuxer, bufferInfo, { trackIndex = it }, { muxerStarted = it }, muxerStarted)
                inputIndex = mediaCodec.dequeueInputBuffer(10000)
            }
            val ptsUsec = (totalFrames * 1_000_000L) / FRAME_RATE
            mediaCodec.queueInputBuffer(inputIndex, 0, 0, ptsUsec, MediaCodec.BUFFER_FLAG_END_OF_STREAM)

            // Drain remaining
            drainAll(mediaCodec, mediaMuxer, bufferInfo, { trackIndex = it }, { muxerStarted = it }, muxerStarted)

            mediaCodec.stop()
            mediaCodec.release()
            mediaCodec = null

            if (muxerStarted) {
                mediaMuxer.stop()
                mediaMuxer.release()
                mediaMuxer = null
            }

            withContext(Dispatchers.Main) {
                onProgress(95)
            }

            // Save video to MediaStore / Public storage
            val savedUri = saveVideoToGallery(context, tempFile)
            tempFile.delete()

            withContext(Dispatchers.Main) {
                onProgress(100)
            }
            return@withContext savedUri
        } catch (e: Exception) {
            Log.e(TAG, "Video export failed", e)
            try {
                mediaCodec?.release()
                mediaMuxer?.release()
                tempFile.delete()
            } catch (_: Exception) {}
            return@withContext null
        }
    }

    private fun drainEncoder(
        codec: MediaCodec,
        muxer: MediaMuxer?,
        bufferInfo: MediaCodec.BufferInfo,
        setTrack: (Int) -> Unit,
        setStarted: (Boolean) -> Unit,
        muxerStarted: Boolean
    ) {
        var runningMuxerStarted = muxerStarted
        while (true) {
            val outputIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
            if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break
            } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (runningMuxerStarted) {
                    throw RuntimeException("Format changed twice")
                }
                val newFormat = codec.outputFormat
                val track = muxer?.addTrack(newFormat) ?: 0
                setTrack(track)
                muxer?.start()
                runningMuxerStarted = true
                setStarted(true)
            } else if (outputIndex >= 0) {
                val outputBuffer = codec.getOutputBuffer(outputIndex)
                if (outputBuffer != null && runningMuxerStarted && bufferInfo.size > 0) {
                    outputBuffer.position(bufferInfo.offset)
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                    muxer?.writeSampleData(0, outputBuffer, bufferInfo)
                }
                codec.releaseOutputBuffer(outputIndex, false)
            }
        }
    }

    private fun drainAll(
        codec: MediaCodec,
        muxer: MediaMuxer?,
        bufferInfo: MediaCodec.BufferInfo,
        setTrack: (Int) -> Unit,
        setStarted: (Boolean) -> Unit,
        muxerStarted: Boolean
    ) {
        var runningMuxerStarted = muxerStarted
        while (true) {
            val outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
            if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break
            } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                val newFormat = codec.outputFormat
                val track = muxer?.addTrack(newFormat) ?: 0
                setTrack(track)
                muxer?.start()
                runningMuxerStarted = true
                setStarted(true)
            } else if (outputIndex >= 0) {
                val outputBuffer = codec.getOutputBuffer(outputIndex)
                if (outputBuffer != null && runningMuxerStarted && bufferInfo.size > 0) {
                    outputBuffer.position(bufferInfo.offset)
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                    muxer?.writeSampleData(0, outputBuffer, bufferInfo)
                }
                codec.releaseOutputBuffer(outputIndex, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break
                }
            }
        }
    }

    private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize

        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                val c = argb[index++]
                val r = (c shr 16) and 0xff
                val g = (c shr 8) and 0xff
                val b = c and 0xff

                var y = (66 * r + 129 * g + 25 * b + 128 shr 8) + 16
                var u = (-38 * r - 74 * g + 112 * b + 128 shr 8) + 128
                var v = (112 * r - 94 * g - 18 * b + 128 shr 8) + 128

                yuv420sp[yIndex++] = y.coerceIn(0, 255).toByte()
                if (j % 2 == 0 && i % 2 == 0) {
                    yuv420sp[uvIndex++] = u.coerceIn(0, 255).toByte()
                    yuv420sp[uvIndex++] = v.coerceIn(0, 255).toByte()
                }
            }
        }
    }

    private fun saveVideoToGallery(context: Context, videoFile: File): Uri? {
        val fileName = "NeonDraw_${System.currentTimeMillis()}.mp4"
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/NeonDraw")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return null

        try {
            resolver.openOutputStream(uri)?.use { out ->
                FileInputStream(videoFile).use { input ->
                    input.copyTo(out)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            return uri
        } catch (e: Exception) {
            Log.e(TAG, "Error saving video to MediaStore: ${e.message}")
            try { resolver.delete(uri, null, null) } catch (_: Exception) {}
            return null
        }
    }
}

