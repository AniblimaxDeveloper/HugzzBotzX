package com.lutzzpie.parental

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { FullSystemUI() } }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun FullSystemUI() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isStrobing by remember { mutableStateOf(false) }
    var mapUrl by remember { mutableStateOf("https://www.openstreetmap.org/") }
    var serverStatus by remember { mutableStateOf("Server: OFF") }

    // --- FUNGSI FLASH LOKAL ---
    fun setFlash(on: Boolean) {
        val cm = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try { cm.setTorchMode(cm.cameraIdList[0], on) } catch (e: CameraAccessException) {}
    }

    // --- FUNGSI LOKASI & MAP ---
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null) mapUrl = "https://www.openstreetmap.org/?mlat=${loc.latitude}&mlon=${loc.longitude}#map=16/${loc.latitude}/${loc.longitude}"
            else Toast.makeText(ctx, "GPS tidak aktif", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0C29))) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))
            Text("HugzzBotzX", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
            Text("Ultimate Controller by Lutzzpie", color = Color(0xFF00CEC9), fontSize = 12.sp)
            Spacer(Modifier.height(16.dp))

            // 1. REMOTE FLASH SERVER
            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Remote Flash (WebSocket)", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(serverStatus, color = Color(0xFFFDCB6E), fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val ip = ControlServer.getIP()
                        ControlServer.start(ctx, 9999)
                        serverStatus = "Server ON di $ip:9999"
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7))) {
                        Text("Mulai Terima Perintah (Host)")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 2. APP BLOCKER
            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("App Blocker (TikTok, YouTube, IG)", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { 
                        ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) 
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD63031))) {
                        Text("Aktifkan Izin Blocker")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 3. LIVE MAP
            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Live Location Map", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)) }, 
                        modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B894))) {
                        Text("Dapatkan Peta Lokasi")
                    }
                    Spacer(Modifier.height(8.dp))
                    AndroidView(
                        factory = { c -> WebView(c).apply { loadUrl(mapUrl) } },
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                        update = { wv -> wv.loadUrl(mapUrl) }
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))

            // 4. LOKAL STROBE
            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Local Strobe Test", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        if (!isStrobing) {
                            isStrobing = true
                            scope.launch {
                                while (isStrobing) {
                                    setFlash(true); delay(100); setFlash(false); delay(100)
                                }
                            }
                        } else {
                            isStrobing = false; setFlash(false)
                        }
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if(isStrobing) Color.Red else Color(0xFF0984E3))) {
                        Text(if(isStrobing) "HENTIKAN STROBE" else "MULAI STROBE LOKAL")
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
