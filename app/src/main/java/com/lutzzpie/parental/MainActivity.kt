package com.lutzzpie.parental

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
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
    
    // State Variables
    var battery by remember { mutableStateOf("...") }
    var storage by remember { mutableStateOf("...") }
    var network by remember { mutableStateOf("...") }
    var deviceInfo by remember { mutableStateOf("...") }
    var locationText by remember { mutableStateOf("Belum dilacak") }
    var usageText by remember { mutableStateOf("Butuh izin akses") }
    var appCount by remember { mutableStateOf("...") }

    // --- LOGIKA SISTEM ---
    fun updateAllData() {
        // 1. Baterai
        val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        battery = "${bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)}%"

        // 2. Storage
        val stat = StatFs(Environment.getDataDirectory().path)
        val freeBytes = stat.availableBytes
        val totalBytes = stat.totalBytes
        val freeGB = String.format("%.1f", freeBytes / (1024.0 * 1024.0 * 1024.0))
        val totalGB = String.format("%.1f", totalBytes / (1024.0 * 1024.0 * 1024.0))
        storage = "$freeGB GB / $totalGB GB"

        // 3. Network
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)
        network = if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) "WiFi Aktif" else "Data Seluler"

        // 4. Device Info
        deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"

        // 5. App Count (Persiapan App Blocker)
        appCount = "${ctx.packageManager.getInstalledPackages(0).size} Aplikasi"

        // 6. Screen Time
        val appOps = ctx.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), ctx.packageName)
        if (mode == AppOpsManager.MODE_ALLOWED) {
            val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, calendar.timeInMillis, System.currentTimeMillis())
            if (stats != null && stats.isNotEmpty()) {
                usageText = "${stats.sumOf { it.totalTimeInForeground } / 1000 / 60} Menit hari ini"
            }
        } else usageText = "Izin Ditolak"
    }

    // --- IZIN LOKASI ---
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null) locationText = "Lat: ${loc.latitude}\nLng: ${loc.longitude}" else locationText = "GPS tidak aktif"
        } else Toast.makeText(ctx, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) { updateAllData() }

    // --- UI DASHBOARD ---
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
            Spacer(Modifier.height(24.dp))

            // PANEL 1: DEVICE STATUS
            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Device Status", color = Color.White, fontWeight = FontWeight.Bold)
                    StatusRow("Device", deviceInfo)
                    StatusRow("Baterai", battery)
                    StatusRow("Storage", storage)
                    StatusRow("Jaringan", network)
                    StatusRow("Total Apps", appCount)
                }
            }

            Spacer(Modifier.height(16.dp))

            // PANEL 2: PARENTAL CONTROL
            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Parental Control", color = Color.White, fontWeight = FontWeight.Bold)
                    StatusRow("Screen Time", usageText)
                    if (usageText == "Izin Ditolak") {
                        Button(onClick = { ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                            modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7))) {
                            Text("Izinkan Pemantauan", fontSize = 12.sp)
                        }
                    }
                    StatusRow("Lokasi GPS", locationText)
                }
            }

            Spacer(Modifier.height(24.dp))

            // TOMBOL AKSI
            Button(onClick = { locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) },
                modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B894))) {
                Text("Update Lokasi GPS")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { updateAllData(); Toast.makeText(ctx, "Data di-refresh!", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0984E3))) {
                Text("Refresh System Data")
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

// Komponen UI untuk baris data agar rapi
@Composable
fun StatusRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.LightGray, fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
}
