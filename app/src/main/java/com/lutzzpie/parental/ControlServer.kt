package com.lutzzpie.parental

import android.content.Context
import android.hardware.camera2.CameraManager
import android.util.Base64
import com.lutzzpie.parental.service.ControlAccessibilityService
import okhttp3.*
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.json.JSONObject
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

object HostServer {
    var server: WebSocketServer? = null
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var appCtx: Context? = null

    fun start(ctx: Context, port: Int) {
        appCtx = ctx.applicationContext
        cameraManager = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager?.cameraIdList?.firstOrNull()

        server = object : WebSocketServer(InetSocketAddress(port)) {
            override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {}
            override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {}
            override fun onError(conn: WebSocket?, ex: Exception) {}
            override fun onStart() {}
            override fun onMessage(conn: WebSocket, message: String) {
                val obj = runCatching { JSONObject(message) }.getOrNull() ?: return
                when (obj.optString("t")) {
                    "flash" -> {
                        try { if (cameraId != null) cameraManager?.setTorchMode(cameraId!!, obj.optBoolean("on")) } catch (e: Exception) {}
                    }
                    "tap" -> {
                        val x = obj.optDouble("x", 0.0).toFloat()
                        val y = obj.optDouble("y", 0.0).toFloat()
                        val dm = appCtx?.resources?.displayMetrics
                        if (dm != null) ControlAccessibilityService.instance?.performTap(x * dm.widthPixels, y * dm.heightPixels)
                    }
                    "key" -> ControlAccessibilityService.instance?.performGlobalKey(obj.optInt("code", 0))
                }
            }
        }
        server?.start()
    }
    fun stop() { server?.stop(1000) }
    fun broadcast(msg: String) { server?.connections?.filter { it.isOpen }?.forEach { it.send(msg) } }
    fun getIP(): String? = try {
        java.net.NetworkInterface.getNetworkInterfaces().toList().flatMap { it.inetAddresses.toList() }
            .firstOrNull { !it.isLoopbackAddress && it is java.net.Inet4Address }?.hostAddress
    } catch (e: Exception) { null }
}

class ViewerClient(private val onFrame: (android.graphics.Bitmap) -> Unit, private val onState: (String) -> Unit) {
    private var ws: WebSocket? = null
    private val client = OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build()
    
    fun connect(host: String, port: Int) {
        onState("connecting")
        val req = Request.Builder().url("ws://$host:$port").build()
        ws = client.newWebSocket(req, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) { onState("connected") }
            override fun onMessage(webSocket: WebSocket, text: String) {
                val obj = runCatching { JSONObject(text) }.getOrNull() ?: return
                if (obj.optString("t") == "frame") {
                    val bytes = Base64.decode(obj.optString("d"), Base64.DEFAULT)
                    val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bmp != null) onFrame(bmp)
                }
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, r: Response?) { onState("error: ${t.message}") }
        })
    }
    fun tap(x: Float, y: Float) { ws?.send(JSONObject().put("t", "tap").put("x", x).put("y", y).toString()) }
    fun key(code: Int) { ws?.send(JSONObject().put("t", "key").put("code", code).toString()) }
    fun setFlash(on: Boolean) { ws?.send(JSONObject().put("t", "flash").put("on", on).toString()) }
    fun disconnect() { ws?.close(1000, "bye"); ws = null }
}
