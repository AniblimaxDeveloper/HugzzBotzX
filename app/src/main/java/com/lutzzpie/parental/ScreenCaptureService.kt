package com.lutzzpie.parental

import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Base64
import android.util.DisplayMetrics
import androidx.core.app.NotificationCompat
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class ScreenCaptureService : Service() {
    private var projection: MediaProjection? = null
    private var vd: VirtualDisplay? = null
    private var reader: ImageReader? = null
    private var lastSent = 0L
    private var dm: DisplayMetrics? = null

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startFg()
        val code = intent?.getIntExtra("code", Activity.RESULT_CANCELED) ?: Activity.RESULT_CANCELED
        val data = intent?.getParcelableExtra<Intent>("data")
        if (code != Activity.RESULT_OK || data == null) { stopSelf(); return START_NOT_STICKY }
        
        dm = resources.displayMetrics
        projection = getSystemService(MediaProjectionManager::class.java).getMediaProjection(code, data)
        projection?.registerCallback(object : MediaProjection.Callback() { override fun onStop() { stopSelf() } }, null)
        
        val w = dm!!.widthPixels; val h = dm!!.heightPixels
        reader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 2)
        vd = projection?.createVirtualDisplay("cap", w, h, dm!!.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader?.surface, null, null)
        
        reader?.setOnImageAvailableListener({ r ->
            val image = r.acquireLatestImage() ?: return@setOnImageAvailableListener
            val now = System.currentTimeMillis()
            if (now - lastSent < 100) { image.close(); return@setOnImageAvailableListener }
            lastSent = now
            try {
                val plane = image.planes[0]; val buf = plane.buffer
                val pixelStride = plane.pixelStride; val rowStride = plane.rowStride
                val rowPadding = rowStride - pixelStride * w
                val bmp = Bitmap.createBitmap(w + rowPadding / pixelStride, h, Bitmap.Config.ARGB_8888)
                bmp.copyPixelsFromBuffer(buf)
                val cropped = Bitmap.createBitmap(bmp, 0, 0, w, h)
                val scaled = Bitmap.createScaledBitmap(cropped, 540, (h * (540f / w)).toInt(), true)
                
                val bos = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 30, bos)
                val msg = JSONObject().put("t", "frame").put("w", scaled.width).put("h", scaled.height).put("d", Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP)).toString()
                HostServer.broadcast(msg)
                bmp.recycle(); cropped.recycle(); scaled.recycle()
            } catch (_: Throwable) {} finally { image.close() }
        }, null)
        return START_STICKY
    }

    private fun startFg() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("cap", "Screen", NotificationManager.IMPORTANCE_LOW))
        }
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val notif = NotificationCompat.Builder(this, "cap").setContentTitle("HugzzBotzX").setContentText("Live Screen Active").setSmallIcon(android.R.drawable.ic_menu_camera).setOngoing(true).setContentIntent(pi).build()
        if (Build.VERSION.SDK_INT >= 29) startForeground(1001, notif, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION) else startForeground(1001, notif)
    }
    override fun onDestroy() { reader?.close(); vd?.release(); projection?.stop(); super.onDestroy() }
}
