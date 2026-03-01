package com.example.jadwalsholattv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WeatherDashboardScreen() }
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
                        colors = listOf(Color(0x33525252), Color(0x553DB9AF), Color(0x5527867E))
                    )
                )
                .border(1.dp, Color(0x809FB0B3), RoundedCornerShape(25.dp))
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
                .border(1.dp, Color(0x66C2CCCD), RoundedCornerShape(20.dp))
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color(0x33FFFFFF))
                        .border(1.dp, Color(0x66C2CCCD), RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
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
                .border(1.dp, Color(0x66C2CCCD), RoundedCornerShape(20.dp))
                .padding(30.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_weather_night_cloudy),
                        contentDescription = null,
                        modifier = Modifier.size(130.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("25", color = Color.White, fontSize = 55.sp, fontWeight = FontWeight.Bold)
                        Text("Party Cloud", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Monday", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                        Text("29 Agustus 2026", color = Color.White.copy(alpha = 0.6f), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(34.dp)) {
                    Metric(imageVector = Icons.Outlined.WaterDrop, label = "Humidty", value = "80%")
                    Metric(imageVector = Icons.Outlined.Air, label = "Wind Speed", value = "13 Km/h")
                }

                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.White.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "12 : 00",
                        style = TextStyle(
                            fontSize = 95.sp,
                            fontWeight = FontWeight.Black,
                            brush = Brush.linearGradient(listOf(Color(0xFF6A777A), Color(0xFF435256)))
                        )
                    )
                    Text(" PM", color = Color(0xFFAAB3B5), fontSize = 46.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(Color(0x33D9D9D9))
                            .border(1.dp, Color(0x66C2CCCD), RoundedCornerShape(25.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
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
            .border(1.dp, Color(0x66C2CCCD), RoundedCornerShape(20.dp))
            .padding(26.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.weight(1f)) {
                IconTile(R.drawable.ic_weather_night_cloudy, Modifier.weight(1f))
                IconTile(R.drawable.ic_weather_partly, Modifier.weight(1f))
                IconTile(R.drawable.ic_weather_sun, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.weight(1f)) {
                IconTile(R.drawable.ic_weather_cloudy_day, Modifier.weight(1f))
                IconTile(R.drawable.ic_weather_night_cloudy, Modifier.weight(1f))
                IconTile(iconRes = null, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Metric(imageVector: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0x33D9D9D9))
                .border(1.dp, Color(0x66C2CCCD), RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = imageVector, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, color = Color(0xFFD2D8D9), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 30.sp * (14f / 30f))
        }
    }
}

@Composable
private fun IconTile(iconRes: Int?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(15.dp))
            .background(Color(0x33D9D9D9))
            .border(1.dp, Color(0x66C2CCCD), RoundedCornerShape(15.dp)),
        contentAlignment = Alignment.Center
    ) {
        iconRes?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = null,
                modifier = Modifier.size(108.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Preview(widthDp = 1280, heightDp = 720)
@Composable
private fun WeatherDashboardPreview() {
    WeatherDashboardScreen()
}
