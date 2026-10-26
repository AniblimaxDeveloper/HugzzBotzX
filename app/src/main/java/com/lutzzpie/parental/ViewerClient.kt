package com.lutzzpie.parental

import android.util.Base64
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

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
