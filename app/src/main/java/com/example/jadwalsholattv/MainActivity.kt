package com.example.jadwalsholattv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.jadwalsholattv.ui.theme.JadwalSholatTVTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JadwalSholatTVTheme {
                PrayerDashboardScreen()
            }
        }
    }
}

private val outerFrameBrush = Brush.horizontalGradient(
    listOf(Color(0xFF0F1115), Color(0xFF173C3A), Color(0xFF0F1115))
)

private val panelBrush = Brush.verticalGradient(
    listOf(Color(0xFF4A5658), Color(0xFF3E4A4D))
)

private val tileBrush = Brush.verticalGradient(
    listOf(Color(0xFF6F7C7D), Color(0xFF677476))
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PrayerDashboardScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121417))
            .padding(26.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(outerFrameBrush)
            .border(1.dp, Color(0xFF365857), RoundedCornerShape(24.dp))
            .padding(32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            LeftPanel(modifier = Modifier.weight(1.05f))
            RightPanel(modifier = Modifier.weight(1.25f))
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun LeftPanel(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(106.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(panelBrush)
                .border(1.dp, Color(0xFF8B9B9B), RoundedCornerShape(18.dp))
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🔔",
                    fontSize = 28.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x558E999A))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Informasi running text yang memiliki tingkat kepanjangan yang berbeda beda",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFEAF1F1), fontSize = 20.sp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(panelBrush)
                .border(1.dp, Color(0xFF9EAEAE), RoundedCornerShape(20.dp))
                .padding(26.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "☁️", fontSize = 86.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "25", fontSize = 68.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(text = "Party Cloud", fontSize = 40.sp, color = Color(0xFFF2F5F5), fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Monday", fontSize = 56.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = "29 Agustus 2026", fontSize = 32.sp, color = Color(0xFFD2DBDB))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    MiniInfo(label = "Humidity", value = "80%")
                    MiniInfo(label = "Wind Speed", value = "13 Km/h")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFA5B4B4)))
                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "12:00", fontSize = 98.sp, color = Color(0xFFE7EFEF), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "PM", fontSize = 54.sp, color = Color(0xFFCCD6D6), fontWeight = FontWeight.Medium)
                    }

                    Text(
                        text = "🕋",
                        fontSize = 54.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x5594A5A5))
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Jawa Barat, Bandung, Kab. Bandung",
                    fontSize = 36.sp,
                    color = Color(0xFFC6D0D0),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun MiniInfo(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "◌",
            fontSize = 28.sp,
            color = Color.White,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x558D9A9A))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = label, color = Color(0xFFDDE7E7), fontSize = 26.sp, fontWeight = FontWeight.Medium)
            Text(text = value, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun RightPanel(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(panelBrush)
            .border(1.dp, Color(0xFF9EAEAE), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                WeatherTile(Modifier.weight(1f).fillMaxSize(), "🌙☁️")
                WeatherTile(Modifier.weight(1f).fillMaxSize(), "🌤️")
                WeatherTile(Modifier.weight(1f).fillMaxSize(), "☀️")
            }
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                WeatherTile(Modifier.weight(1f).fillMaxSize(), "⛅")
                WeatherTile(Modifier.weight(1f).fillMaxSize(), "🌙☁️")
                WeatherTile(Modifier.weight(1f).fillMaxSize(), "")
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun WeatherTile(modifier: Modifier, icon: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(tileBrush)
            .border(1.dp, Color(0xFFAFBEBE), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (icon.isNotEmpty()) {
            Text(text = icon, fontSize = 70.sp)
        }
    }
}

@Preview(showBackground = true, device = Devices.TV_1080p)
@Composable
fun PrayerDashboardPreview() {
    JadwalSholatTVTheme {
        PrayerDashboardScreen()
    }
}
