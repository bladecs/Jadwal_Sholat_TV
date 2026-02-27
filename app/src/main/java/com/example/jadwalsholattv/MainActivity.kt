package com.example.jadwalsholattv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.jadwalsholattv.ui.theme.JadwalSholatTVTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JadwalSholatTVTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0A192F))
                ) {
                    JadwalSholatScreen()
                }
            }
        }
    }
}

data class SholatTime(
    val name: String,
    val time: String,
    val isCurrent: Boolean = false,
    val passed: Boolean = false
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun JadwalSholatScreen() {
    val sholatTimes = listOf(
        SholatTime("Subuh", "04:30", passed = true),
        SholatTime("Dzuhur", "12:15", passed = true),
        SholatTime("Ashar", "15:45", isCurrent = true),
        SholatTime("Maghrib", "18:15"),
        SholatTime("Isya", "19:30")
    )

    val currentDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        .format(Date())
    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        .format(Date())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp)
    ) {
        // Header Section - Lebih ringkas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "JADWAL SHOLAT",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        color = Color(0xFF64FFDA)
                    )
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "JAKARTA",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 32.sp,
                        color = Color(0xFF64FFDA),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Main Prayer Cards - Horizontal Scroll (bukan vertical)
        Text(
            text = "WAKTU SHOLAT HARI INI",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TvLazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            pivotOffsets = PivotOffsets(parentFraction = 0.07f)
        ) {
            items(sholatTimes) { sholat ->
                PrayerCard(sholat = sholat)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Current Prayer Highlight - Lebih besar di tengah
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(MaterialTheme.shapes.large)
                .background(Color(0xFF112240))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SHOLAT BERIKUTNYA",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 18.sp,
                            color = Color(0xFF64FFDA),
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "MAGHRIB",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 48.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "SISAKAN",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 18.sp,
                            color = Color(0xFF8892B0)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "01:45:30",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 48.sp,
                            color = Color(0xFF64FFDA),
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text(
                        text = "18:15 WIB",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    )
                }
            }
        }

        // Bottom Info Bar
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "© Jadwal Sholat TV 2024",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    color = Color(0xFF8892B0)
                )
            )

            Text(
                text = "Sumber: Kemenag RI",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    color = Color(0xFF8892B0)
                )
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PrayerCard(sholat: SholatTime) {
    val cardColor = when {
        sholat.isCurrent -> Color(0xFF64FFDA).copy(alpha = 0.15f)
        sholat.passed -> Color(0xFF2D3748)
        else -> Color(0xFF112240)
    }

    val textColor = when {
        sholat.isCurrent -> Color(0xFF64FFDA)
        sholat.passed -> Color(0xFF718096)
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .width(200.dp)
            .height(140.dp)
            .shadow(elevation = if (sholat.isCurrent) 8.dp else 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(cardColor)
            .border(
                width = if (sholat.isCurrent) 2.dp else 0.dp,
                color = if (sholat.isCurrent) Color(0xFF64FFDA) else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = sholat.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = sholat.time,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 32.sp,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            )

            if (sholat.isCurrent) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "● SEDANG BERLANGSUNG",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        color = Color(0xFF64FFDA),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TV_1080p)
@Composable
fun JadwalSholatPreview() {
    JadwalSholatTVTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A192F))
        ) {
            JadwalSholatScreen()
        }
    }
}