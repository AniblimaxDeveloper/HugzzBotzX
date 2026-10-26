package com.lutzzpie.parental

import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PremiumParentalUI()
            }
        }
    }
}

@Composable
fun PremiumParentalUI() {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. VIDEO BACKGROUND ANIME
        AndroidView(
            factory = { context ->
                VideoView(context).apply {
                    val videoUri = Uri.parse("android.resource://${context.packageName}/raw/anime_bg")
                    setVideoURI(videoUri)
                    setOnPreparedListener { it.isLooping = true; start() }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. OVERLAY GELAP AGAR TEKS TERBACA
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.9f))
                )
            )
        )

        // 3. KONTEN UI PREMIUM
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            
            // LOGO / TITLE
            Text(
                text = "HugzzBotzX",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Premium Parental Control",
                color = Color(0xFF00CEC9),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Created by Lutzzpie",
                color = Color.LightGray,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(48.dp))

            // PANEL CONTROLLER (GLASSMORPHISM EFFECT)
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
                        onClick = { /* Logika Screen Time */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7))
                    ) {
                        Text("Screen Time & App Blocker")
                    }

                    Button(
                        onClick = { /* Logika GPS */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B894))
                    ) {
                        Text("Location Tracking")
                    }

                    Button(
                        onClick = { /* Logika Aksesibilitas */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD63031))
                    ) {
                        Text("App Monitoring (Consent Required)")
                    }
                }
            }
            Spacer(Modifier.weight(1f))
        }
    }
}
