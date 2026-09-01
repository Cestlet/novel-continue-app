package com.novel.continueapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 前台服务：用 MediaProjection + ImageReader 截取当前屏幕一帧，回调 Bitmap 后自动停止。
 */
class ScreenCaptureService : Service() {

    companion object {
        private const val TAG = "ScreenCapture"
        private const val CHANNEL_ID = "screen_capture"
        private const val NOTIFICATION_ID = 1001
        private const val CAPTURE_TIMEOUT_MS = 5000L

        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"

        /** 截图完成回调（成功传 Bitmap，失败传 null）。 */
        @Volatile var onCaptured: ((Bitmap?) -> Unit)? = null
    }

    private val handlerThread = HandlerThread("ScreenCaptureThread").apply { start() }
    private val handler by lazy { Handler(handlerThread.looper) }
    private val finished = AtomicBoolean(false)

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundWithNotification()

        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, android.app.Activity.RESULT_CANCELED)
            ?: android.app.Activity.RESULT_CANCELED
        val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(EXTRA_RESULT_DATA)
        }

        if (resultCode != android.app.Activity.RESULT_OK || resultData == null) {
            Log.w(TAG, "未获得有效的截图授权")
            finish(null)
            return START_NOT_STICKY
        }

        try {
            val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val projection = manager.getMediaProjection(resultCode, resultData)
            // Android 14+ 要求先注册回调，再创建 VirtualDisplay，否则会抛 SecurityException
            projection.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    Log.i(TAG, "MediaProjection 已停止")
                }
            }, handler)
            mediaProjection = projection
            handler.postDelayed({ finish(null) }, CAPTURE_TIMEOUT_MS)
            captureOnce(projection)
        } catch (e: Exception) {
            Log.e(TAG, "启动截图失败: ${e.message}", e)
            finish(null)
        }

        return START_NOT_STICKY
    }

    private fun captureOnce(projection: MediaProjection) {
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        imageReader = reader

        reader.setOnImageAvailableListener({ r ->
            val image = r.acquireLatestImage()
            if (image != null) {
                val bitmap = try {
                    imageToBitmap(image, width, height)
                } finally {
                    image.close()
                }
                finish(bitmap)
            }
        }, handler)

        virtualDisplay = projection.createVirtualDisplay(
            "NovelCapture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface, null, handler
        )
    }

    private fun imageToBitmap(image: Image, width: Int, height: Int): Bitmap {
        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width

        val padded = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        padded.copyPixelsFromBuffer(buffer)
        if (rowPadding == 0) return padded
        val cropped = Bitmap.createBitmap(padded, 0, 0, width, height)
        padded.recycle()
        return cropped
    }

    private fun finish(bitmap: Bitmap?) {
        if (!finished.compareAndSet(false, true)) return
        handler.removeCallbacksAndMessages(null)
        onCaptured?.invoke(bitmap)

        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        virtualDisplay = null
        imageReader = null
        mediaProjection = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerThread.quitSafely()
    }

    private fun startForegroundWithNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "截图识别", NotificationManager.IMPORTANCE_LOW)
            )
        }

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("正在截图识别文字")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }
}
