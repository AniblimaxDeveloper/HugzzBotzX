package com.lutzzpie.parental

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { UltimateUI() } }
    }
}

@Composable
fun UltimateUI() {
    var tab by remember { mutableStateOf(0) }
    Column(Modifier.fillMaxSize().background(Color(0xFF0F0C29))) {
        TabRow(selectedTabIndex = tab, containerColor = Color(0xFF24243E), contentColor = Color.White) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Live", fontSize = 12.sp) })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Blocker", fontSize = 12.sp) })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Strobe", fontSize = 12.sp) })
        }

        when (tab) {
            0 -> LiveScreenTab()
            1 -> BlockerTab()
            2 -> StrobeTab()
        }
    }
}

// --- TAB 1: LIVE SCREEN ---
@Composable
fun LiveScreenTab() {
    val ctx = LocalContext.current
    var mode by remember { mutableStateOf("menu") }

    if (mode == "menu") {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Live Screen", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            Button(onClick = { mode = "host" }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7))) {
                Text("Host (Bagikan Layar Ini)")
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = { mode = "viewer" }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B894))) {
                Text("Viewer (Pantau Layar Lain)")
            }
        }
    }

    if (mode == "host") {
        var status by remember { mutableStateOf("Server: OFF") }
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Host Mode", color = Color.White, fontSize = 24.sp)
            Spacer(Modifier.height(16.dp))
            Text(status, color = Color(0xFFFDCB6E))
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                HostServer.start(ctx, 9999)
                status = "ON: ws://${HostServer.getIP()}:9999"
            }, modifier = Modifier.fillMaxWidth()) { Text("Mulai WebSocket Server") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { ctx.startActivity(Intent(ctx, ConsentActivity::class.java)) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD63031))) {
                Text("Mulai Share Screen")
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = { mode = "menu"; HostServer.stop() }) { Text("Kembali") }
        }
    }

    if (mode == "viewer") {
        var hostIp by remember { mutableStateOf("192.168.1.10") }
        var bmp by remember { mutableStateOf<Bitmap?>(null) }
        var status by remember { mutableStateOf("idle") }
        val client = remember { ViewerClient({ bmp = it }, { status = it }) }

        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Viewer Mode", color = Color.White, fontSize = 24.sp)
            OutlinedTextField(hostIp, { hostIp = it }, label = { Text("IP Host") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { client.connect(hostIp, 9999) }) { Text("Connect") }
                OutlinedButton(onClick = { client.disconnect(); bmp = null; status = "idle" }) { Text("Stop") }
                Button(onClick = { client.setFlash(true) }) { Text("Flash ON") }
                Button(onClick = { client.setFlash(false) }) { Text("Flash OFF") }
            }
            Text("Status: $status", color = Color.White)
            
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                bmp?.let { val img = remember(it) { it.asImageBitmap() }
                    Canvas(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { o -> client.tap(o.x / size.width, o.y / size.height) } }) {
                        drawImage(img, dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt()))
                    }
                } ?: Text("Menunggu layar...", color = Color.Gray)
            }
            OutlinedButton(onClick = { mode = "menu"; client.disconnect() }, modifier = Modifier.fillMaxWidth()) { Text("Kembali") }
        }
    }
}

// --- TAB 2: APP BLOCKER ---
@Composable
fun BlockerTab() {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("App Blocker", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("Memblokir TikTok, YouTube, & Instagram secara otomatis.", color = Color.LightGray, fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD63031))) {
            Text("Aktifkan Izin Blocker (Aksesibilitas)")
        }
    }
}

// --- TAB 3: FLASH STROBE ---
@SuppressLint("MissingPermission")
@Composable
fun StrobeTab() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var isStrobing by remember { mutableStateOf(false) }

    fun setFlash(on: Boolean) {
        val cm = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try { cm.setTorchMode(cm.cameraIdList[0], on) } catch (e: CameraAccessException) {}
    }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Flash Strobe", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { 
            if (!isStrobing) { 
                isStrobing = true
                scope.launch { while(isStrobing) { setFlash(true); delay(100); setFlash(false); delay(100) } } 
            } else { 
                isStrobing = false; setFlash(false) 
            }
        }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = if(isStrobing) Color.Red else Color(0xFF6C5CE7))) {
            Text(if(isStrobing) "HENTIKAN STROBE" else "MULAI KEDAP-KEDIP", fontSize = 18.sp)
        }
    }
}
