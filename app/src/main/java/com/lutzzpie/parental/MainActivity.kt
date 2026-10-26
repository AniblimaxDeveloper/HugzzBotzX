package com.lutzzpie.parental

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Bundle
import android.os.Process
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
    var batteryText by remember { mutableStateOf("Membaca...") }
    var locationText by remember { mutableStateOf("Belum dilacak") }
    var usageText by remember { mutableStateOf("Butuh izin akses penggunaan") }

    // --- FUNGSI BATERAI ---
    fun getBattery() {
        val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        batteryText = "$level%"
    }

    // --- FUNGSI LOKASI ---
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) 
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null) locationText = "Lat: ${loc.latitude}\nLng: ${loc.longitude}"
            else locationText = "GPS mati / Lokasi tidak ditemukan"
        } else Toast.makeText(ctx, "Izin lokasi ditolak!", Toast.LENGTH_SHORT).show()
    }

    // --- FUNGSI SCREEN TIME (USAGE STATS) ---
    fun checkUsagePermissionAndLoad() {
        val appOps = ctx.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), ctx.packageName)
        if (mode == AppOpsManager.MODE_ALLOWED) {
            val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
            val start = calendar.timeInMillis
            val end = System.currentTimeMillis()
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
            
            if (stats != null && stats.isNotEmpty()) {
                val totalTime = stats.sumOf { it.totalTimeInForeground } / 1000 / 60 // Konversi ke Menit
                usageText = "$totalTime Menit hari ini"
            } else usageText = "Data tidak ditemukan"
        } else {
            usageText = "Izin Ditolak. Ketuk untuk buka Setelan."
        }
    }

    // Jalankan saat pertama dibuka
    LaunchedEffect(Unit) {
        getBattery()
        checkUsagePermissionAndLoad()
    }

    // --- UI TAMPILAN ---
    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(Color(0xFF0F0C29), Color(0xFF24243E)))
    )) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            Text("HugzzBotzX", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
            Text("Premium Parental Control", color = Color(0xFF00CEC9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))

            // PANEL DASHBOARD
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("System Dashboard", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    
                    // BATERAI
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Baterai", color = Color.LightGray)
                        Text(batteryText, color = Color(0xFF00B894), fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    
                    // SCREEN TIME
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Screen Time", color = Color.LightGray)
                        Text(usageText, color = Color(0xFF6C5CE7), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    if (usageText.contains("Izin Ditolak")) {
                        Button(onClick = { 
                            ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) 
                        }, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7))) {
                            Text("Buka Izin Penggunaan", fontSize = 12.sp)
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    // LOKASI GPS
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Lokasi GPS", color = Color.LightGray)
                        Text(locationText, color = Color(0xFFD63031), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // TOMBOL AKSI
            Button(
                onClick = { locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B894))
            ) { Text("Lacak Lokasi Sekarang") }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { 
                    getBattery()
                    checkUsagePermissionAndLoad()
                    Toast.makeText(ctx, "Data diperbarui!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0984E3))
            ) { Text("Refresh Data Sistem") }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}
