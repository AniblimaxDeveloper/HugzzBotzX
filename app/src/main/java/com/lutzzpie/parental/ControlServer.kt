package com.lutzzpie.parental

import android.content.Context
import android.hardware.camera2.CameraManager
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

object ControlServer {
    var server: WebSocketServer? = null
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null

    fun start(ctx: Context, port: Int) {
        cameraManager = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager?.cameraIdList?.firstOrNull()
        
        server = object : WebSocketServer(InetSocketAddress(port)) {
            override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {}
            override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {}
            override fun onError(conn: WebSocket?, ex: Exception) {}
            override fun onStart() {}
            override fun onMessage(conn: WebSocket, message: String) {
                try {
                    if (cameraId != null) {
                        when (message) {
                            "FLASH_ON" -> cameraManager?.setTorchMode(cameraId!!, true)
                            "FLASH_OFF" -> cameraManager?.setTorchMode(cameraId!!, false)
                        }
                    }
                } catch (e: Exception) {}
            }
        }
        server?.start()
    }
    fun stop() { server?.stop(1000) }
    fun getIP(): String? {
        return try {
            java.net.NetworkInterface.getNetworkInterfaces().toList()
                .flatMap { it.inetAddresses.toList() }
                .firstOrNull { !it.isLoopbackAddress && it is java.net.Inet4Address }?.hostAddress
        } catch (e: Exception) { null }
    }
}
