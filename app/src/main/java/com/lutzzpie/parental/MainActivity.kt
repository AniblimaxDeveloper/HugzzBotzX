package com.lutzzpie.parental

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    var locationText by remember { mutableStateOf("Tekan tombol untuk melacak") }

    // Pengelola Izin Lokasi
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            // Jika diizinkan, ambil lokasi
            val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) 
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null) {
                locationText = "Lat: ${loc.latitude}\nLng: ${loc.longitude}"
            } else {
                locationText = "Lokasi tidak ditemukan. Pastikan GPS aktif."
            }
        } else {
            Toast.makeText(ctx, "Izin lokasi ditolak!", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(Color(0xFF0F0C29), Color(0xFF24243E)))
    )) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            Text("HugzzBotzX", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black)
            Text("Premium Parental Control", color = Color(0xFF00CEC9), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(48.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("System Dashboard", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    
                    Button(
                        onClick = { /* Nanti kita isi fitur Screen Time */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7))
                    ) { Text("Screen Time & App Blocker") }

                    // TOMBOL LOKASI YANG SUDAH BERFUNGSI
                    Button(
                        onClick = { 
                            locationPermissionLauncher.launch(arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ))
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B894))
                    ) { Text("Dapatkan Lokasi GPS") }

                    Text(locationText, color = Color.White, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.weight(1f))
        }
    }
}
