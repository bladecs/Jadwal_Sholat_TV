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
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.jadwalsholattv.ui.theme.JadwalSholatTVTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JadwalSholatTVTheme {
                JadwalSholatScreen()
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

private val outlineBrush = Brush.horizontalGradient(listOf(Color(0xFFD4AF37), Color(0xFF675724)))
private val outline2Brush = Brush.horizontalGradient(listOf(Color(0xFFD4AF37), Color(0xFF726F1B)))
private val sixCardBrush = Brush.verticalGradient(listOf(Color(0xFF00FFE1), Color(0xFF006FFF)))
private val mainCardBrush = Brush.linearGradient(
    listOf(Color(0xFF005E73), Color(0xFFA6A600), Color(0xFF00D948))
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun JadwalSholatScreen() {
    val sholatTimes = listOf(
        SholatTime("Subuh", "04:30", passed = true),
        SholatTime("Syuruq", "05:47", passed = true),
        SholatTime("Dzuhur", "12:15", passed = true),
        SholatTime("Ashar", "15:45", isCurrent = true),
        SholatTime("Maghrib", "18:15"),
        SholatTime("Isya", "19:30")
    )

    val currentDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())
    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001B26))
            .padding(12.dp)
            .border(2.dp, outlineBrush, RoundedCornerShape(28.dp))
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x6600AFAF))
            .border(2.dp, outline2Brush, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GlassCard(
                    modifier = Modifier.weight(3f).height(110.dp),
                    backgroundBrush = Brush.verticalGradient(listOf(Color(0xB000AFAF), Color(0x6600AFAF))),
                    title = "JADWAL SHOLAT",
                    subtitle = currentDate
                )
                GlassCard(
                    modifier = Modifier.weight(2f).height(110.dp),
                    backgroundBrush = Brush.verticalGradient(listOf(Color(0xB000AFAF), Color(0x6600AFAF))),
                    title = "JAKARTA",
                    subtitle = currentTime,
                    alignEnd = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MainPrayerCard(
                    modifier = Modifier.weight(3f).fillMaxSize(),
                    nextPrayer = "MAGHRIB",
                    remaining = "01:45:30",
                    nextTime = "18:15 WIB"
                )

                Column(
                    modifier = Modifier.weight(2f).fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (row in 0 until 2) {
                        Row(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (col in 0 until 3) {
                                val item = sholatTimes[row * 3 + col]
                                PrayerCard(sholat = item, modifier = Modifier.weight(1f).fillMaxSize())
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            BottomInfoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp),
                leftText = "© Jadwal Sholat TV 2024",
                rightText = "Sumber: Kemenag RI"
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun GlassCard(
    modifier: Modifier,
    backgroundBrush: Brush,
    title: String,
    subtitle: String,
    alignEnd: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(2.dp, outline2Brush, RoundedCornerShape(18.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFFE8FFFE),
                        fontSize = 20.sp
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainPrayerCard(modifier: Modifier, nextPrayer: String, remaining: String, nextTime: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, outline2Brush, RoundedCornerShape(20.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize().background(mainCardBrush).padding(24.dp)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SHOLAT BERIKUTNYA",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = nextPrayer,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 62.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = remaining,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = nextTime,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFEFFFFD)
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PrayerCard(sholat: SholatTime, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, outlineBrush, RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(sixCardBrush)
                .padding(vertical = 10.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = sholat.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = sholat.time,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (sholat.isCurrent) {
                    Text(
                        text = "SEKARANG",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFFFF5C2),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BottomInfoCard(modifier: Modifier, leftText: String, rightText: String) {
    val shape: Shape = GenericShape { size, _ ->
        moveTo(size.width * 0.04f, 0f)
        lineTo(size.width * 0.96f, 0f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0x6600AFAF))
            .border(2.dp, outlineBrush, shape)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = leftText, color = Color.White, fontSize = 16.sp)
            Text(text = rightText, color = Color.White, fontSize = 16.sp)
        }
    }
}

@Preview(showBackground = true, device = Devices.TV_1080p)
@Composable
fun JadwalSholatPreview() {
    JadwalSholatTVTheme {
        JadwalSholatScreen()
    }
}
