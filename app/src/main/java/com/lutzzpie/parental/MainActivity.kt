package com.lutzzpie.parental

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { UltimateUI() } }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun UltimateUI() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxSize().background(Color(0xFF0F0C29))) {
        // TAB MENU
        TabRow(selectedTabIndex = tab, containerColor = Color(0xFF24243E), contentColor = Color.White) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Tools", fontSize = 12.sp) })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Live Screen", fontSize = 12.sp) })
        }

        if (tab == 0) ToolsScreen(ctx, scope)
        if (tab == 1) LiveScreenTab(ctx)
    }
}

// --- TAB 1: TOOLS (Flash, Map, Blocker) ---
@SuppressLint("MissingPermission")
@Composable
fun ToolsScreen(ctx: Context, scope: kotlinx.coroutines.CoroutineScope) {
    var isStrobing by remember { mutableStateOf(false) }
    var mapUrl by remember { mutableStateOf("https://www.openstreetmap.org/") }

    fun setFlash(on: Boolean) {
        val cm = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try { cm.setTorchMode(cm.cameraIdList[0], on) } catch (e: CameraAccessException) {}
    }

    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { p ->
        if (p[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null) mapUrl = "https://www.openstreetmap.org/?mlat=${loc.latitude}&mlon=${loc.longitude}#map=16/${loc.latitude}/${loc.longitude}"
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("HugzzBotzX Tools", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        
        Button(onClick = { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD63031))) {
            Text("Aktifkan App Blocker (Aksesibilitas)")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B894))) {
            Text("Lacak Lokasi ke Peta")
        }
        Spacer(Modifier.height(8.dp))
        AndroidView(factory = { c -> WebView(c).apply { loadUrl(mapUrl) } }, modifier = Modifier.fillMaxWidth().height(200.dp), update = { it.loadUrl(mapUrl) })
        
        Spacer(Modifier.height(16.dp))
        Button(onClick = { 
            if (!isStrobing) { isStrobing = true; scope.launch { while(isStrobing) { setFlash(true); delay(100); setFlash(false); delay(100) } } } 
            else { isStrobing = false; setFlash(false) }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if(isStrobing) Color.Red else Color(0xFF6C5CE7))) {
            Text(if(isStrobing) "HENTIKAN STROBE" else "STROBE LOKAL")
        }
    }
}

// --- TAB 2: LIVE SCREEN (HOST & VIEWER) ---
@SuppressLint("MissingPermission")
@Composable
fun LiveScreenTab(ctx: Context) {
    var mode by remember { mutableStateOf("menu") } // menu, host, viewer

    if (mode == "menu") {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { mode = "host" }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7))) {
                Text("1. Host (Bagikan Layar Ini)", fontSize = 16.sp)
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = { mode = "viewer" }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B894))) {
                Text("2. Viewer (Lihat Layar Lain)", fontSize = 16.sp)
            }
        }
    }

    if (mode == "host") {
        var status by remember { mutableStateOf("Server: OFF") }
        var ip by remember { mutableStateOf("") }
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Host Mode", color = Color.White, fontSize = 24.sp)
            Spacer(Modifier.height(16.dp))
            Text(status, color = Color(0xFFFDCB6E))
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                HostServer.start(ctx, 9999)
                ip = HostServer.getIP() ?: "0.0.0.0"
                status = "Server ON: ws://$ip:9999"
            }, modifier = Modifier.fillMaxWidth()) { Text("Mulai WebSocket Server") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { ctx.startActivity(Intent(ctx, ConsentActivity::class.java)) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD63031))) {
                Text("Mulai Share Screen (Butuh Izin)")
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
                OutlinedButton(onClick = { client.disconnect(); bmp = null; status = "idle" }) { Text("Disconnect") }
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
