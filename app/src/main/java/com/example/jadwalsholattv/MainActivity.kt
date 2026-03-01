package com.example.jadwalsholattv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherDashboardScreen()
        }
    }
}

@Composable
fun WeatherDashboardScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111317))
            .padding(28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(25.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0x34525252), Color(0x803DB9AF), Color(0x8027867E))
                    )
                )
                .border(1.dp, Color(0x80A6B6B8), RoundedCornerShape(25.dp))
                .padding(30.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxSize()) {
                LeftPanel(modifier = Modifier.weight(1f))
                RightPanel(modifier = Modifier.weight(1.08f))
            }
        }
    }
}

@Composable
private fun LeftPanel(modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp), modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(105.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x33D9D9D9))
                .border(1.dp, Color(0x80B8C2C3), RoundedCornerShape(20.dp))
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color(0x33FFFFFF))
                ) {
                    Text(
                        text = "🔔",
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Informasi running text yang memiliki tingkat kepanjangan yang berbeda beda",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x33D9D9D9))
                .border(1.dp, Color(0x80B8C2C3), RoundedCornerShape(20.dp))
                .padding(30.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WeatherIcon(Modifier.size(130.dp), true)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("25", color = Color.White, fontSize = 55.sp, fontWeight = FontWeight.Bold)
                        Text("Party Cloud", color = Color.White, fontSize = 36.sp * (20f / 36f), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Monday", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                        Text("29 Agustus 2026", color = Color.White.copy(alpha = 0.6f), fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(34.dp)) {
                    Metric("💧", "Humidty", "80%")
                    Metric("💨", "Wind Speed", "13 Km/h")
                }
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.White.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.height(26.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "12 : 00",
                        style = TextStyle(
                            fontSize = 95.sp,
                            fontWeight = FontWeight.Black,
                            brush = Brush.linearGradient(listOf(Color(0xFF6A777A), Color(0xFF435256)))
                        )
                    )
                    Text(" PM", color = Color(0xFF8F9A9D), fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(Color(0x33D9D9D9))
                            .border(1.dp, Color(0x80B8C2C3), RoundedCornerShape(25.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌍", fontSize = 36.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Jawa Barat, Bandung, Kab. Bandung",
                    color = Color(0xFF9EA7A9),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RightPanel(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x33D9D9D9))
            .border(1.dp, Color(0x80B8C2C3), RoundedCornerShape(20.dp))
            .padding(26.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.weight(1f)) {
                PrayerTile("Terbit", "04 : 00", true, Modifier.weight(1f))
                PrayerTile("Subuh", "04 : 30", false, Modifier.weight(1f))
                PrayerTile("Dzuhur", "12 : 00", null, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.weight(1f)) {
                PrayerTile("Ashar", "15 : 30", false, Modifier.weight(1f))
                PrayerTile("Maghrib", "18 : 17", true, Modifier.weight(1f))
                PrayerTile("Isya", "19 : 20", false, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Metric(icon: String, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0x33D9D9D9))
                .border(1.dp, Color(0x80B8C2C3), RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, color = Color(0xFFD2D8D9), fontWeight = FontWeight.SemiBold, fontSize = 30.sp * (14f / 30f))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp * (14f / 28f))
        }
    }
}

@Composable
private fun PrayerTile(
    title: String,
    time: String,
    cloudy: Boolean?,
    modifier: Modifier = Modifier,
    isEmpty: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(15.dp))
            .background(Color(0x33D9D9D9))
            .border(1.dp, Color(0x80B8C2C3), RoundedCornerShape(15.dp))
            .padding(18.dp)
    ) {
        if (!isEmpty) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(8.dp))
                WeatherIcon(Modifier.size(74.dp), cloudy)
                Spacer(modifier = Modifier.weight(1f))
                Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                Text(time, color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
        }
    }
}

@Composable
private fun WeatherIcon(modifier: Modifier = Modifier, cloudy: Boolean?) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x66FFE5A3), Color.Transparent),
                    center = center,
                    radius = size.minDimension / 2f
                ),
                radius = size.minDimension / 2f
            )
        }

        Canvas(modifier = Modifier.size(58.dp)) {
            drawCircle(
                brush = Brush.verticalGradient(listOf(Color(0xFFFFE7A5), Color(0xFFFFA81C))),
                radius = size.minDimension / 2f,
                center = center
            )
        }

        if (cloudy != null) {
            Canvas(modifier = Modifier
                .size(84.dp)
                .offset(y = 14.dp)) {
                val cloud = if (cloudy) Color(0xFFF5F5F5) else Color(0xFFECECEC)
                drawCircle(cloud, radius = size.minDimension * 0.2f, center = Offset(size.width * 0.3f, size.height * 0.55f))
                drawCircle(cloud, radius = size.minDimension * 0.24f, center = Offset(size.width * 0.48f, size.height * 0.45f))
                drawCircle(cloud, radius = size.minDimension * 0.2f, center = Offset(size.width * 0.66f, size.height * 0.57f))
                drawRoundRect(
                    cloud,
                    topLeft = Offset(size.width * 0.16f, size.height * 0.56f),
                    size = Size(size.width * 0.58f, size.height * 0.2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
                )
            }
            if (cloudy) {
                Canvas(modifier = Modifier.size(86.dp)) {
                    val star = Color(0xFFFFD370)
                    drawCircle(star, radius = 3f, center = Offset(size.width * 0.24f, size.height * 0.22f))
                    drawCircle(star, radius = 4f, center = Offset(size.width * 0.68f, size.height * 0.2f))
                    drawCircle(star, radius = 2.8f, center = Offset(size.width * 0.74f, size.height * 0.38f))
                }
            }
        }
    }
}

@Preview(widthDp = 1280, heightDp = 720)
@Composable
private fun WeatherDashboardPreview() {
    WeatherDashboardScreen()
}
