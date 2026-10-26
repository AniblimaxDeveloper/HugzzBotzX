package com.lutzzpie.parental

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Panel50Fitur() } }
    }
}

data class Fitur(val nama: String, val warna: Color, val aksi: (Context) -> Unit)

@SuppressLint("MissingPermission")
@Composable
fun Panel50Fitur() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var isStrobing by remember { mutableStateOf(false) }
    
    val cm = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val daftarFitur = listOf(
        // --- HARDWARE & MEDIA ---
        Fitur("Flash ON", Color(0xFFFDCB6E)) { try { cm.setTorchMode(cm.cameraIdList[0], true) } catch (e: Exception) {} },
        Fitur("Flash OFF", Color(0xFF636E72)) { try { cm.setTorchMode(cm.cameraIdList[0], false) } catch (e: Exception) {} },
        Fitur(if(isStrobing) "Stop Strobe" else "Strobe", Color(0xFF00B894)) { 
            if (!isStrobing) {
                isStrobing = true
                scope.launch { while(isStrobing) { try { cm.setTorchMode(cm.cameraIdList[0], true); delay(100); cm.setTorchMode(cm.cameraIdList[0], false); delay(100) } catch (e: Exception) {} } }
            } else { isStrobing = false; try { cm.setTorchMode(cm.cameraIdList[0], false) } catch(e: Exception){} }
        },
        Fitur("Volume Max", Color(0xFF6C5CE7)) { am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0) },
        Fitur("Mute HP", Color(0xFFE17055)) { am.ringerMode = AudioManager.RINGER_MODE_SILENT },
        Fitur("Getar", Color(0xFF00CEC9)) { am.ringerMode = AudioManager.RINGER_MODE_VIBRATE },
        Fitur("Normal Mode", Color(0xFF0984E3)) { am.ringerMode = AudioManager.RINGER_MODE_NORMAL },
        
        // --- AKSESIBILITAS (KONTROL) ---
        Fitur("Kunci Layar", Color(0xFFD63031)) { AppBlockerService.instance?.lockScreen() },
        Fitur("Tombol Back", Color(0xFFB2BEC3)) { AppBlockerService.instance?.goBack() },
        Fitur("Tombol Home", Color(0xFFB2BEC3)) { AppBlockerService.instance?.goHome() },
        Fitur("Set. Aksesbilitas", Color(0xFFD63031)) { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
        Fitur("Set. Aplikasi", Color(0xFF0984E3)) { ctx.startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS)) },

        // --- SETELAN SISTEM (SHORTCUT) ---
        Fitur("Set. WiFi", Color(0xFF00B894)) { ctx.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) },
        Fitur("Set. Bluetooth", Color(0xFF0984E3)) { ctx.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) },
        Fitur("Set. Lokasi", Color(0xFFE17055)) { ctx.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
        Fitur("Set. Suara", Color(0xFF6C5CE7)) { ctx.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS)) },
        Fitur("Set. Layar", Color(0xFFFDCB6E)) { ctx.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS)) },
        Fitur("Set. Baterai", Color(0xFF00CEC9)) { ctx.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)) },
        Fitur("Set. Keamanan", Color(0xFFD63031)) { ctx.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS)) },
        Fitur("Set. Bahasa", Color(0xFF6C5CE7)) { ctx.startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS)) },
        Fitur("Set. Keyboard", Color(0xFF00B894)) { ctx.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) },
        Fitur("Set. Hotspot", Color(0xFFE17055)) { ctx.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) },
        Fitur("Set. NFC", Color(0xFF636E72)) { ctx.startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) },
        Fitur("Set. Pesawat", Color(0xFF0984E3)) { ctx.startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)) },
        Fitur("Set. Tgl/Waktu", Color(0xFF00CEC9)) { ctx.startActivity(Intent(Settings.ACTION_DATE_SETTINGS)) },
        Fitur("Set. Penyimpanan", Color(0xFFD63031)) { ctx.startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)) },
        Fitur("Set. Memori", Color(0xFFFDCB6E)) { ctx.startActivity(Intent(Settings.ACTION_MEMORY_CARD_SETTINGS)) },
        Fitur("Set. VPN", Color(0xFF00B894)) { ctx.startActivity(Intent(Settings.ACTION_VPN_SETTINGS)) },
        Fitur("Set. Sync", Color(0xFFE17055)) { ctx.startActivity(Intent(Settings.ACTION_SYNC_SETTINGS)) },
        Fitur("Set. Privasi", Color(0xFF636E72)) { ctx.startActivity(Intent(Settings.ACTION_PRIVACY_SETTINGS)) },
        Fitur("Set. Notifikasi", Color(0xFF0984E3)) { ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
        Fitur("Set. Developer", Color(0xFF6C5CE7)) { ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)) },
        Fitur("Set. Data", Color(0xFFFDCB6E)) { ctx.startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)) },
        Fitur("Set. Info HP", Color(0xFF636E72)) { ctx.startActivity(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)) },

        // --- INFO SISTEM (TOAST) ---
        Fitur("Baterai %", Color(0xFF00CEC9)) { 
            val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
            Toast.makeText(ctx, "Baterai: ${bm.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)}%", Toast.LENGTH_SHORT).show() 
        },
        Fitur("Storage Free", Color(0xFF6C5CE7)) { 
            val stat = StatFs(Environment.getDataDirectory().path)
            Toast.makeText(ctx, "${stat.availableBytes / 1024 / 1024 / 1024} GB Free", Toast.LENGTH_SHORT).show() 
        },
        Fitur("Info Android", Color(0xFFFDCB6E)) { Toast.makeText(ctx, "Android ${Build.VERSION.RELEASE}", Toast.LENGTH_SHORT).show() },
        Fitur("Info Model", Color(0xFFD63031)) { Toast.makeText(ctx, "${Build.MANUFACTURER} ${Build.MODEL}", Toast.LENGTH_SHORT).show() },
        Fitur("Info SDK", Color(0xFF00CEC9)) { Toast.makeText(ctx, "SDK: ${Build.VERSION.SDK_INT}", Toast.LENGTH_SHORT).show() },
        Fitur("Info CPU", Color(0xFF0984E3)) { Toast.makeText(ctx, Build.SUPPORTED_ABIS[0], Toast.LENGTH_SHORT).show() },
        Fitur("Info Kernel", Color(0xFF636E72)) { Toast.makeText(ctx, System.getProperty("os.version"), Toast.LENGTH_SHORT).show() },
        Fitur("Total Aplikasi", Color(0xFFE17055)) { Toast.makeText(ctx, "${ctx.packageManager.getInstalledPackages(0).size} Aplikasi", Toast.LENGTH_SHORT).show() },
        
        // --- LAINNYA ---
        Fitur("Buka ZArchiver", Color(0xFF0984E3)) { try { ctx.startActivity(ctx.packageManager.getLaunchIntentForPackage("ru.zdevs.zarchiver")) } catch(e: Exception) {} },
        Fitur("Buka Setelan", Color(0xFF00B894)) { ctx.startActivity(Intent(Settings.ACTION_SETTINGS)) },
        Fitur("Cek Root", Color(0xFFD63031)) { 
            val root = java.io.File("/system/app/Superuser.apk").exists() || java.io.File("/sbin/su").exists()
            Toast.makeText(ctx, if(root) "Root Terdeteksi" else "Aman (No Root)", Toast.LENGTH_SHORT).show() 
        },
        Fitur("Refresh UI", Color(0xFFE17055)) { Toast.makeText(ctx, "Aplikasi Segar", Toast.LENGTH_SHORT).show() }
    )

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0C29))) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("HugzzBotzX", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text("50+ System Controls", color = Color(0xFF00CEC9), fontSize = 12.sp)
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(daftarFitur) { fitur ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = fitur.warna.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(12.dp),
                    onClick = { fitur.aksi(ctx) },
                    modifier = Modifier.aspectRatio(1f)
                ) {
                    Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
                        Text(fitur.nama, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}
