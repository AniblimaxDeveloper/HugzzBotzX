package com.lutzzpie.parental

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ConsentActivity : ComponentActivity() {
    private lateinit var mpm: MediaProjectionManager
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == RESULT_OK && res.data != null) {
            startForegroundService(Intent(this, ScreenCaptureService::class.java).apply { putExtra("code", res.resultCode); putExtra("data", res.data) })
        } else Toast.makeText(this, "Izin ditolak", Toast.LENGTH_SHORT).show()
        finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mpm = getSystemService(MediaProjectionManager::class.java)
        setContent {
            MaterialTheme {
                Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Persetujuan Live Screen", fontSize = 22.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Layar Anda akan dibagikan. Notifikasi akan selalu tampil. Anda bisa menghentikannya kapan saja.", fontSize = 14.sp)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { launcher.launch(mpm.createScreenCaptureIntent()) }, modifier = Modifier.fillMaxWidth()) { Text("Setujui & Mulai") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { finish() }, modifier = Modifier.fillMaxWidth()) { Text("Batal") }
                }
            }
        }
    }
}
