package com.lutzzpie.parental

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.provider.Settings
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme { PremiumParentalUI() }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun PremiumParentalUI() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State Sistem
    var battery by remember { mutableStateOf("...") }
    var storage by remember { mutableStateOf("...") }
    var network by remember { mutableStateOf("...") }
    var usageText by remember { mutableStateOf("...") }

    // State Flash
    var isFlashOn by remember { mutableStateOf(false) }
    var isStrobing by remember { mutableStateOf(false) }

    // Fungsi Hardware Flash
    fun setFlashlight(on: Boolean) {
        val cameraManager = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, on)
            isFlashOn = on
        } catch (e: CameraAccessException) {
            Toast.makeText(ctx, "Kamera/Flash tidak tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi Strobe (Kedap-kedip)
    fun startStrobe() {
        if (!ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA).equals(PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(ctx, "Izin kamera dibutuhkan untuk senter!", Toast.LENGTH_SHORT).show()
            return
        }
        isStrobing = true
        scope.launch {
            while (isStrobing) {
                setFlashlight(true)
                delay(100) // Nyala 100ms
                setFlashlight(false)
                delay(100) // Mati 100ms
            }
        }
    }

    fun stopStrobe() {
        isStrobing = false
        setFlashlight(false)
    }

    // Fungsi update data
    fun updateAllData() {
        val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        battery = "${bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)}%"
        
        val stat = StatFs(Environment.getDataDirectory().path)
        storage = "${String.format("%.1f", stat.availableBytes / (1024.0 * 1024.0 * 1024.0))} GB Free"

        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        network = if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) "WiFi" else "Data Seluler"

        val appOps = ctx.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), ctx.packageName)
        if (mode == AppOpsManager.MODE_ALLOWED) {
            val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, calendar.timeInMillis, System.currentTimeMillis())
            if (stats != null && stats.isNotEmpty()) usageText = "${stats.sumOf { it.totalTimeInForeground } / 1000 / 60} Menit"
        } else usageText = "Izin Ditolak"
    }

    LaunchedEffect(Unit) { updateAllData() }

    // --- UI TAMPILAN ---
    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(Color(0xFF0F0C29), Color(0xFF24243E)))
    )) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(30.dp))
            Text("HugzzBotzX", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
            Text("Live System Controller", color = Color(0xFF00CEC9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            // PANEL 1: FLASH CONTROLLER
            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Flash & Strobe Control", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(if (isFlashOn) "Status: NYALA" else "Status: MATI", color = if (isFlashOn) Color.Green else Color.LightGray, fontSize = 12.sp)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { 
                                if (isStrobing) stopStrobe() else { setFlashlight(true); isStrobing = false } 
                            },
                            modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDCB6E))
                        ) { Text("Flash ON", color = Color.Black, fontSize = 12.sp) }

                        Button(
                            onClick = { 
                                if (!isStrobing) stopStrobe() 
                            },
                            modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF636E72))
                        ) { Text("Flash OFF", fontSize = 12.sp) }
                    }

                    Button(
                        onClick = { 
                            if (isStrobing) { stopStrobe(); Toast.makeText(ctx, "Strobe Dihentikan", Toast.LENGTH_SHORT).show() } 
                            else { startStrobe(); Toast.makeText(ctx, "Strobe Dimulai!", Toast.LENGTH_SHORT).show() } 
                        },
                        modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (isStrobing) Color(0xFFD63031) else Color(0xFF6C5CE7))
                    ) { Text(if (isStrobing) "STOP STROBE" else "MULAI KEDAP-KEDIP (STROBE)") }
                }
            }

            Spacer(Modifier.height(16.dp))

            // PANEL 2: SYSTEM STATUS
            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("System Status", color = Color.White, fontWeight = FontWeight.Bold)
                    StatusRow("Baterai", battery)
                    StatusRow("Storage", storage)
                    StatusRow("Jaringan", network)
                    StatusRow("Screen Time", usageText)
                    if (usageText == "Izin Ditolak") {
                        Button(onClick = { ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                            modifier = Modifier.fillMaxWidth().height(35.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7))) {
                            Text("Izinkan Pemantauan", fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(onClick = { updateAllData(); Toast.makeText(ctx, "Data di-refresh!", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0984E3))) {
                Text("Refresh System Data")
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun StatusRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.LightGray, fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
}
