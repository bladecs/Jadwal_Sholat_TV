package com.example.jadwalsholattv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.jadwalsholattv.ui.theme.JadwalSholatTVTheme
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

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

data class SholatTime(val name: String, val time: LocalTime)

private val prayerSchedule = listOf(
    SholatTime("Subuh", LocalTime.of(4, 30)),
    SholatTime("Syuruq", LocalTime.of(5, 47)),
    SholatTime("Dzuhur", LocalTime.of(12, 15)),
    SholatTime("Ashar", LocalTime.of(15, 45)),
    SholatTime("Maghrib", LocalTime.of(18, 15)),
    SholatTime("Isya", LocalTime.of(19, 30))
)

private val frameBrush = Brush.linearGradient(
    listOf(Color(0xFFD6B25E), Color(0xFF7F5F1A), Color(0xFFD6B25E)),
    start = Offset(0f, 0f),
    end = Offset(1200f, 1200f)
)

private val backgroundBrush = Brush.radialGradient(
    colors = listOf(Color(0xFF0A3A43), Color(0xFF041A26), Color(0xFF021017)),
    radius = 1800f
)

private val cardBrush = Brush.verticalGradient(listOf(Color(0xAA0C5562), Color(0xAA0A2639)))
private val highlightCard = Brush.linearGradient(listOf(Color(0xCC2C8F7A), Color(0xCC26607D)))

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun JadwalSholatScreen() {
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000)
        }
    }

    val locale = Locale("id", "ID")
    val gregorianDate = now.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", locale))
    val hijriDate = HijrahDate.from(LocalDate.now()).format(DateTimeFormatter.ofPattern("dd MMMM yyyy", locale))

    val nextPrayer = prayerSchedule.firstOrNull { it.time.isAfter(now.toLocalTime()) } ?: prayerSchedule.first()
    val isTomorrow = nextPrayer.time <= now.toLocalTime()
    val targetDateTime = if (isTomorrow) now.toLocalDate().plusDays(1).atTime(nextPrayer.time) else now.toLocalDate().atTime(nextPrayer.time)
    val remaining = Duration.between(now, targetDateTime).coerceAtLeast(Duration.ZERO)

    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(14.dp)
            .border(2.dp, frameBrush, RoundedCornerShape(30.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderSection(gregorianDate = gregorianDate, hijriDate = hijriDate, now = now)
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MainPrayerCard(
                    modifier = Modifier.weight(3f).fillMaxSize().scale(pulseScale),
                    nextPrayer = nextPrayer.name,
                    remaining = remaining.formatHms(),
                    nextTime = nextPrayer.time.format(DateTimeFormatter.ofPattern("HH:mm"))
                )

                Column(
                    modifier = Modifier.weight(2f).fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    prayerSchedule.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { prayer ->
                                PrayerCard(
                                    sholat = prayer,
                                    modifier = Modifier.weight(1f).fillMaxSize(),
                                    isCurrent = prayer.name == nextPrayer.name
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            RunningTextTicker(
                text = "Assalamu'alaikum. Matikan suara ponsel saat di masjid • Perbanyak shalawat di hari Jumat • Sumber jadwal: Kemenag RI"
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun HeaderSection(gregorianDate: String, hijriDate: String, now: LocalDateTime) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        InfoCard(
            modifier = Modifier.weight(3f).height(120.dp),
            title = "JADWAL SHOLAT",
            subtitle = gregorianDate,
            tertiary = "Hijriah: $hijriDate"
        )

        InfoCard(
            modifier = Modifier.weight(2f).height(120.dp),
            title = "JAM DIGITAL",
            subtitle = now.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            tertiary = "WIB",
            alignEnd = true,
            animated = true
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun InfoCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    tertiary: String,
    alignEnd: Boolean = false,
    animated: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, frameBrush, RoundedCornerShape(20.dp))
            .background(cardBrush)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color(0xFFFFF6DA)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (animated) {
                AnimatedContent(targetState = subtitle, label = "clock") { value ->
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                }
            } else {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color.White)
                )
            }
            Text(
                text = tertiary,
                style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFD7F6FF))
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun MainPrayerCard(modifier: Modifier, nextPrayer: String, remaining: String, nextTime: String) {
    val glow = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glow.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .border(1.8.dp, frameBrush, RoundedCornerShape(22.dp))
            .background(highlightCard)
            .padding(22.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "COUNTDOWN SHOLAT BERIKUTNYA",
                style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFFFFF6DA), fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = nextPrayer.uppercase(),
                style = MaterialTheme.typography.displayMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 58.sp
                )
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x3394FFE5))
                        .border(1.dp, Color(0x88E5FFFA), RoundedCornerShape(50))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = remaining,
                        modifier = Modifier.alpha(glowAlpha),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Text(
                    text = "$nextTime WIB",
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color(0xFFEFFFFD), fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PrayerCard(sholat: SholatTime, modifier: Modifier = Modifier, isCurrent: Boolean) {
    Crossfade(targetState = isCurrent, label = "prayerState") { active ->
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(18.dp))
                .border(1.4.dp, frameBrush, RoundedCornerShape(18.dp))
                .background(if (active) Brush.verticalGradient(listOf(Color(0xCC2D9872), Color(0xCC1B4B65))) else cardBrush)
                .padding(vertical = 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = sholat.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = sholat.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.headlineMedium.copy(color = Color(0xFFFFF7DC), fontWeight = FontWeight.Bold)
                )
                if (active) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.width(7.dp).height(7.dp).clip(CircleShape).background(Color(0xFFB1FFCA)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "BERIKUTNYA", style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFC2FFDA)))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun RunningTextTicker(text: String) {
    val transition = rememberInfiniteTransition(label = "ticker")
    val offsetX by transition.animateFloat(
        initialValue = 1f,
        targetValue = -1.2f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 18000, easing = FastOutSlowInEasing))
            ,label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(cardBrush)
            .border(1.5.dp, frameBrush, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterStart)
                .padding(horizontal = 16.dp)
                .alpha(0.95f)
                .then(Modifier.padding(start = (offsetX * 900).dp)),
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
            maxLines = 1,
            overflow = TextOverflow.Visible
        )
    }
}

private fun Duration.formatHms(): String {
    val total = seconds.coerceAtLeast(0)
    val hours = total / 3600
    val minutes = (total % 3600) / 60
    val secs = total % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}

@Preview(showBackground = true, device = Devices.TV_1080p)
@Composable
fun JadwalSholatPreview() {
    JadwalSholatTVTheme {
        JadwalSholatScreen()
    }
}
