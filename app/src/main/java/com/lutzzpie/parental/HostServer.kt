package com.lutzzpie.parental

import android.content.Context
import android.hardware.camera2.CameraManager
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.json.JSONObject
import java.net.InetSocketAddress

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
                        if (dm != null) AppBlockerService.instance?.performTap(x * dm.widthPixels, y * dm.heightPixels)
                    }
                    "key" -> AppBlockerService.instance?.performGlobalKey(obj.optInt("code", 0))
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
