package com.example.jadwalsholattv

import android.content.ContentResolver
import android.graphics.BlurMaskFilter
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.jadwalsholattv.data.FirebasePairingManager
import com.example.jadwalsholattv.data.PairingUiState
import com.example.jadwalsholattv.data.DailyPrayerSchedule
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.io.path.Path
import kotlin.math.roundToInt
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import android.icu.util.IslamicCalendar as HijriCalendar
import java.util.Calendar

val gradientColors = listOf(Color(0xFF01A671), Color(0xFF00A78A),Color(0xFF005066))

val IslamicGold = Color(0xFFCFA84C)
val IslamicTeal = Color(0xFF00C896)
val DeepCard    = Color(0xFF04111E)

private enum class CountdownPhase {
    BEFORE_PRAYER,
    ADZAN_HOLD,
    IQOMAH
}

private enum class CenterPage {
    HOME,
    LEARNING,
    HADITH
}

private data class PrayerTime(
    val name: String,
    val time: LocalDateTime,
    val displayTime: String
)

private data class CountdownState(
    val nextPrayer: PrayerTime?,
    val phase: CountdownPhase,
    val remainingSeconds: Long,
    val label: String
)

class MainActivity : ComponentActivity(){
    private lateinit var pairingManager: FirebasePairingManager
    private val pairingUiState: MutableState<PairingUiState> = mutableStateOf(PairingUiState())

    override fun onCreate(savedInstanceState : Bundle?){
        super.onCreate(savedInstanceState)
        pairingManager = FirebasePairingManager(this)
        lifecycleScope.launch {
            runCatching {
                pairingManager.initializeAndPublishPairingCode()
            }.onSuccess { initialState ->
                pairingUiState.value = initialState
                pairingManager.observeDevice(
                    onChanged = { state -> pairingUiState.value = state },
                    onError = { message ->
                        pairingUiState.value = pairingUiState.value.copy(errorMessage = message)
                    }
                )
            }.onFailure { error ->
                Log.e("MainActivity", "Firebase init error", error)
                // Coba muat dari cache lokal sebagai fallback
                val cached = pairingManager.loadCachedState()
                pairingUiState.value = cached
                    ?: pairingUiState.value.copy(errorMessage = error.message)
                // Tetap pasang listener agar langsung update saat internet kembali
                pairingManager.observeDevice(
                    onChanged = { state -> pairingUiState.value = state },
                    onError = { message ->
                        if (pairingUiState.value.settings.mosqueName == "Belum diatur") {
                            pairingUiState.value = pairingUiState.value.copy(errorMessage = message)
                        }
                    }
                )
            }
        }
        setContent{
            JadwalSholatTV(pairingUiState = pairingUiState.value)
        }
    }

    override fun onDestroy() {
        pairingManager.stopObserving()
        super.onDestroy()
    }
}

@Composable
fun JadwalSholatTV(pairingUiState: PairingUiState) {
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var centerPage by remember { mutableStateOf(CenterPage.HOME) }
    var learningPhotoIndex by remember { mutableStateOf(0) }
    val pairingStatusText = when {
        pairingUiState.paired -> "Terhubung"
        !pairingUiState.pairingCode.isNullOrBlank() -> pairingUiState.pairingCode ?: "Belum Pairing"
        else -> "Belum Pairing"
    }
    val shortDeviceId = if (pairingUiState.deviceId.length > 12) {
        pairingUiState.deviceId.take(12) + "..."
    } else {
        pairingUiState.deviceId
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            nowMillis = System.currentTimeMillis()
        }
    }

    val pageOrder = listOf(CenterPage.HOME, CenterPage.HADITH, CenterPage.LEARNING)
    LaunchedEffect("auto_cycle") {
        while (true) {
            delay(30_000L)
            val wasLearning = centerPage == CenterPage.LEARNING
            val idx = pageOrder.indexOf(centerPage)
            centerPage = pageOrder[(idx + 1) % pageOrder.size]
            if (wasLearning) learningPhotoIndex = (learningPhotoIndex + 1) % LEARNING_SLIDES.size
        }
    }

    val zoneId = runCatching { ZoneId.of(pairingUiState.settings.timezone) }
        .getOrElse { ZoneId.systemDefault() }
    val now = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDateTime()
    val dayName = now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("id", "ID"))
    val dateText = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    val timeText = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    val hijriText = remember(dateText) {
        try {
            val hijriCal = HijriCalendar().apply { timeInMillis = nowMillis }
            val day = hijriCal.get(Calendar.DAY_OF_MONTH)
            val monthIdx = hijriCal.get(Calendar.MONTH)
            val year = hijriCal.get(Calendar.YEAR)
            val monthNames = listOf("Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", "Jumadil Awal", "Jumadil Akhir", "Rajab", "Sya'ban", "Ramadhan", "Syawal", "Dzulqaidah", "Dzulhijjah")
            "$day ${monthNames.getOrElse(monthIdx) { "-" }} $year H"
        } catch (e: Exception) {
            ""
        }
    }
    val arabicDayName = remember(dateText) {
        try {
            val hijriCal = HijriCalendar().apply { timeInMillis = nowMillis }
            val dow = hijriCal.get(Calendar.DAY_OF_WEEK)
            listOf("Al-Ahad", "Al-Itsnain", "Ats-Tsalatsaa'", "Al-Arba'aa", "Al-Khomiis", "Al-Jumu'ah", "As-Sabtu")
                .getOrElse(dow - 1) { "-" }
        } catch (e: Exception) { "-" }
    }

    val countdownState = remember(
        nowMillis,
        pairingUiState.todaySchedule,
        pairingUiState.tomorrowSchedule
    ) {
        computeCountdownState(
            now = now,
            todaySchedule = pairingUiState.todaySchedule,
            tomorrowSchedule = pairingUiState.tomorrowSchedule
        )
    }

    val nextPrayerName = countdownState.nextPrayer?.name ?: "-"
    val nextPrayerTime = countdownState.nextPrayer?.displayTime ?: "--:--"
    val countdownLabel = countdownState.label

    val remainingSeconds = countdownState.remainingSeconds.coerceAtLeast(0L)
    val isLongCountdown = remainingSeconds >= 3600L
    val countdownDigits = if (isLongCountdown) {
        val hours = (remainingSeconds / 3600L).toInt().coerceAtMost(99)
        val mins  = ((remainingSeconds % 3600L) / 60L).toInt()
        String.format("%02d%02d", hours, mins)
    } else {
        val mins = (remainingSeconds / 60L).toInt().coerceAtMost(99)
        val secs = (remainingSeconds % 60L).toInt()
        String.format("%02d%02d", mins, secs)
    }
    val countdownUnitLabel = if (isLongCountdown) "jam : mnt" else "mnt : dtk"

    val todayPrayerList = listOf(
        "Subuh" to (pairingUiState.todaySchedule?.fajr ?: "--:--"),
        "Syuruk" to (pairingUiState.todaySchedule?.syuruk ?: "--:--"),
        "Dzuhur" to (pairingUiState.todaySchedule?.dzuhur ?: "--:--"),
        "Ashar" to (pairingUiState.todaySchedule?.ashar ?: "--:--"),
        "Maghrib" to (pairingUiState.todaySchedule?.maghrib ?: "--:--"),
        "Isya" to (pairingUiState.todaySchedule?.isya ?: "--:--")
    )

    // Gunakan Box untuk menumpuk elemen (Z-axis)
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. Lapisan Paling Bawah: Video Background
        VideoBackground()

        // 2. Lapisan Tengah: Overlay Hitam
        // Menggunakan Box kosong dengan background lebih ringan daripada menggunakan Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.62f))
        )

        // 3. Lapisan Atas: Konten Utama
        AnimatedContent(
            targetState = centerPage,
            transitionSpec = {
                (slideInHorizontally { it } + fadeIn(tween(450))) togetherWith
                (slideOutHorizontally { -it } + fadeOut(tween(450)))
            },
            modifier = Modifier.fillMaxSize(),
            label = "page_slide"
        ) { page ->
        when (page) {
            CenterPage.LEARNING -> Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    PembelajaranPage(currentPhotoIndex = learningPhotoIndex)
                }
                Spacer(modifier = Modifier.height(6.dp))
                PageIndicator(currentPage = page, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF011520).copy(alpha = 0.72f)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape
                ) {
                    RunningTickerText(
                        text = pairingUiState.settings.runningText.trim().ifEmpty {
                            "Info: Sholat berjamaah dimulai 10 menit setelah adzan. Mohon merapatkan shaf dan menonaktifkan nada dering."
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(36.dp),
                        textColor = Color.White,
                        speed = pairingUiState.settings.runningTextSpeed,
                        brightness = pairingUiState.settings.runningTextBrightness
                    )
                }
            }
            CenterPage.HADITH -> Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    DailyHadithPage(
                        hadithText = pairingUiState.settings.hadithText,
                        hadithSource = pairingUiState.settings.hadithSource
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                PageIndicator(currentPage = page, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF011520).copy(alpha = 0.72f)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape
                ) {
                    RunningTickerText(
                        text = pairingUiState.settings.runningText.trim().ifEmpty {
                            "Info: Sholat berjamaah dimulai 10 menit setelah adzan. Mohon merapatkan shaf dan menonaktifkan nada dering."
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(36.dp),
                        textColor = Color.White,
                        speed = pairingUiState.settings.runningTextSpeed,
                        brightness = pairingUiState.settings.runningTextBrightness
                    )
                }
            }
            else ->
            HomePageContent(
                pairingUiState = pairingUiState,
                pairingStatusText = pairingStatusText,
                timeText = timeText,
                dayName = dayName,
                dateText = dateText,
                hijriText = hijriText,
                arabicDayName = arabicDayName,
                todayPrayerList = todayPrayerList,
                nextPrayerName = nextPrayerName,
                nextPrayerTime = nextPrayerTime,
                countdownState = countdownState,
                countdownDigits = countdownDigits,
                countdownLabel = countdownLabel,
                countdownUnitLabel = countdownUnitLabel,
                currentPage = page
            )
        } // when
        } // AnimatedContent lambda

        // ═══ POPUP ADZAN (overlay di atas semua layer) ═══
        if (countdownState.phase == CountdownPhase.ADZAN_HOLD) {
            AdzanPopup(
                prayerName = nextPrayerName,
                countdownDigits = countdownDigits,
                countdownUnitLabel = countdownUnitLabel
            )
        }

    }
}

@Composable
private fun AdzanPopup(prayerName: String, countdownDigits: String, countdownUnitLabel: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "adzan_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.93f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lingkaran pulsing dengan gradien
            Box(
                modifier = Modifier
                    .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
                    .size(160.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00A78A).copy(alpha = glowAlpha),
                                Color(0xFF005066).copy(alpha = glowAlpha * 0.4f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "☪",
                    style = LocalTextStyle.current.copy(fontSize = 70.sp),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "A D Z A N",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = LocalTextStyle.current.copy(fontSize = 60.sp)
            )
            Text(
                text = prayerName.uppercase(),
                color = Color(0xFF00E8C0),
                fontWeight = FontWeight.Bold,
                style = LocalTextStyle.current.copy(fontSize = 32.sp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Countdown menuju iqomah
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 30.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Menuju Iqomah",
                    color = Color.White.copy(alpha = 0.7f),
                    style = LocalTextStyle.current.copy(fontSize = 13.sp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${countdownDigits[0]}${countdownDigits[1]}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = LocalTextStyle.current.copy(fontSize = 50.sp)
                    )
                    Text(
                        text = "  :  ",
                        color = Color(0xFFFF9900),
                        fontWeight = FontWeight.Bold,
                        style = LocalTextStyle.current.copy(fontSize = 36.sp)
                    )
                    Text(
                        text = "${countdownDigits[2]}${countdownDigits[3]}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = LocalTextStyle.current.copy(fontSize = 50.sp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val unitParts = countdownUnitLabel.split(" : ")
                    val u1 = unitParts.getOrElse(0) { "MNT" }.uppercase()
                    val u2 = unitParts.getOrElse(1) { "DTK" }.uppercase()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(u1, color = Color.White, fontWeight = FontWeight.Bold,
                            style = LocalTextStyle.current.copy(fontSize = 10.sp, letterSpacing = 0.5.sp))
                    }
                    Text(" : ", color = Color(0xFFFF9900), fontWeight = FontWeight.Bold,
                        style = LocalTextStyle.current.copy(fontSize = 10.sp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(u2, color = Color.White, fontWeight = FontWeight.Bold,
                            style = LocalTextStyle.current.copy(fontSize = 10.sp, letterSpacing = 0.5.sp))
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoBackground() {
    val context = LocalContext.current
    val videoUri = remember(context) {
        Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(context.packageName)
            .appendPath(R.raw.video_bck.toString())
            .build()
    }
    val player = remember(context, videoUri) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            addListener(
                object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("VideoBackground", "Cannot play video_bck.mp4", error)
                    }
                }
            )
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    AndroidView(
        factory = { viewContext ->
            (LayoutInflater.from(viewContext)
                .inflate(R.layout.view_video_background, null, false) as PlayerView).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                this.player = player
            }
        },
        update = { playerView ->
            playerView.player = player
            player.playWhenReady = true
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun RunningTickerText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Black,
    speed: Float = 5f,
    brightness: Float = 80f
) {
    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    var textWidthPx by remember { mutableFloatStateOf(0f) }

    val safeSpeed = speed.coerceIn(1f, 10f)
    val durationMillis = (12000f * (5f / safeSpeed)).roundToInt().coerceIn(4000, 30000)
    val brightnessAlpha = (brightness / 100f).coerceIn(0.2f, 1f)

    val transition = rememberInfiniteTransition(label = "tickerTransition")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tickerProgress"
    )

    val travelDistance = (containerWidthPx + textWidthPx).coerceAtLeast(0f)
    val xOffset = if (travelDistance > 0f) containerWidthPx - (progress * travelDistance) else 0f
    val alpha = when {
        progress < 0.08f -> progress / 0.08f
        progress > 0.92f -> (1f - progress) / 0.08f
        else -> 1f
    }.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { containerWidthPx = it.width.toFloat() },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = textColor.copy(alpha = alpha * brightnessAlpha),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            style = LocalTextStyle.current.copy(fontSize = 22.sp),
            modifier = Modifier
                .onSizeChanged { textWidthPx = it.width.toFloat() }
                .offset { androidx.compose.ui.unit.IntOffset(xOffset.roundToInt(), 0) }
                .graphicsLayer { this.alpha = alpha }
        )
    }
}

@Composable
private fun NavigationBar(
    selectedPage: CenterPage,
    onNavigateHome: () -> Unit,
    onNavigateLearning: () -> Unit,
    onNavigateHadith: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color = Color.White.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationPill(
                text = "Home",
                selected = selectedPage == CenterPage.HOME,
                onClick = onNavigateHome
            )
            Spacer(modifier = Modifier.width(10.dp))
            NavigationPill(
                text = "Hadist",
                selected = selectedPage == CenterPage.HADITH,
                onClick = onNavigateHadith
            )
            Spacer(modifier = Modifier.width(10.dp))
            NavigationPill(
                text = "Pembelajaran",
                selected = selectedPage == CenterPage.LEARNING,
                onClick = onNavigateLearning
            )
        }
    }
}

@Composable
private fun NavigationPill(
    text: String,
    selected: Boolean,
    width: Dp = 150.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(80.dp)
            )
            .width(width)
            .height(34.dp)
            .clip(RoundedCornerShape(80.dp))
            .background(
                color = if (selected) Color(0xFF00A78A) else Color.White
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Black,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            style = LocalTextStyle.current.copy(fontSize = 12.sp)
        )
    }
}

@Composable
private fun PrayerTimeBox(time: String, label: String) {
    val (hour, minute) = splitTime(time)
    Box(
        modifier = Modifier
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .width(65.dp)
            .height(90.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = Color(0xFF00A78A)
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column (
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text(
                text = hour,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = LocalTextStyle.current.copy(fontSize = 25.sp)
            )
            Text(
                text = minute,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9900),
                style = LocalTextStyle.current.copy(fontSize = 25.sp)
            )
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = LocalTextStyle.current.copy(fontSize = 15.sp)
            )
        }
    }
}

private const val ADZAN_HOLD_MINUTES = 2L
private const val IQOMAH_DURATION_MINUTES = 3L

private fun computeCountdownState(
    now: LocalDateTime,
    todaySchedule: DailyPrayerSchedule?,
    tomorrowSchedule: DailyPrayerSchedule?
): CountdownState {
    val todayDate = now.toLocalDate()
    val totalWindowMinutes = ADZAN_HOLD_MINUTES + IQOMAH_DURATION_MINUTES
    val prayers = buildPrayerList(todaySchedule, todayDate) +
        buildPrayerList(tomorrowSchedule, todayDate.plusDays(1))

    val nextPrayer = prayers.firstOrNull { now.isBefore(it.time.plusMinutes(totalWindowMinutes)) }
    if (nextPrayer == null) {
        return CountdownState(
            nextPrayer = null,
            phase = CountdownPhase.BEFORE_PRAYER,
            remainingSeconds = 0,
            label = "-"
        )
    }

    val prayerTime = nextPrayer.time
    val iqomahTime = prayerTime.plusMinutes(ADZAN_HOLD_MINUTES)
    val iqomahEndTime = iqomahTime.plusMinutes(IQOMAH_DURATION_MINUTES)

    return when {
        now.isBefore(prayerTime) -> CountdownState(
            nextPrayer = nextPrayer,
            phase = CountdownPhase.BEFORE_PRAYER,
            remainingSeconds = Duration.between(now, prayerTime).seconds,
            label = nextPrayer.name
        )
        now.isBefore(iqomahTime) -> CountdownState(
            nextPrayer = nextPrayer,
            phase = CountdownPhase.ADZAN_HOLD,
            remainingSeconds = Duration.between(now, iqomahTime).seconds,
            label = "Adzan"
        )
        now.isBefore(iqomahEndTime) -> CountdownState(
            nextPrayer = nextPrayer,
            phase = CountdownPhase.IQOMAH,
            remainingSeconds = Duration.between(now, iqomahEndTime).seconds,
            label = "Iqomah"
        )
        else -> CountdownState(
            nextPrayer = nextPrayer,
            phase = CountdownPhase.BEFORE_PRAYER,
            remainingSeconds = 0,
            label = nextPrayer.name
        )
    }
}

private fun buildPrayerList(
    schedule: DailyPrayerSchedule?,
    date: LocalDate
): List<PrayerTime> {
    if (schedule == null) return emptyList()
    val items = listOf(
        "Subuh" to schedule.fajr,
        "Dzuhur" to schedule.dzuhur,
        "Ashar" to schedule.ashar,
        "Maghrib" to schedule.maghrib,
        "Isya" to schedule.isya
    )
    return items.mapNotNull { (name, timeText) ->
        val parsed = parsePrayerTime(date, timeText)
        parsed?.let { PrayerTime(name = name, time = it, displayTime = timeText) }
    }.sortedBy { it.time }
}

private fun parsePrayerTime(date: LocalDate, timeText: String): LocalDateTime? {
    val parts = timeText.trim().split(":")
    if (parts.size < 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return runCatching { LocalDateTime.of(date, LocalTime.of(hour, minute)) }.getOrNull()
}

private fun splitTime(timeText: String): Pair<String, String> {
    val parts = timeText.trim().split(":")
    if (parts.size < 2) return "--" to "--"
    val hour = parts[0].padStart(2, '0')
    val minute = parts[1].padStart(2, '0')
    return hour to minute
}

@Composable
private fun PageIndicator(currentPage: CenterPage, modifier: Modifier = Modifier) {
    val pages = listOf(CenterPage.HOME, CenterPage.HADITH, CenterPage.LEARNING)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        pages.forEach { page ->
            val isActive = page == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (isActive) 22.dp else 6.dp, 6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (isActive) Color(0xFF00A78A)
                        else Color.White.copy(alpha = 0.35f)
                    )
            )
        }
    }
}

@Composable
private fun DailyHadithPage(hadithText: String, hadithSource: String) {
    val displayText = hadithText.ifBlank {
        "Barangsiapa yang beriman kepada Allah dan Hari Akhir, maka hendaknya ia berkata baik atau diam."
    }
    val displaySource = hadithSource.ifBlank { "HR. Bukhari & Muslim" }

    val infiniteTransition = rememberInfiniteTransition(label = "hadith_anim")
    val glowPulse by infiniteTransition.animateFloat(
        0.4f, 1.0f,
        infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "hadith_glow"
    )
    val rotateDeco by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(30000, easing = LinearEasing)),
        label = "hadith_rotate"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2; val cy = size.height / 2
            val r = size.minDimension * 0.44f
            for (i in 0 until 8) {
                val angle = (rotateDeco + i * 45f) * PI.toFloat() / 180f
                drawLine(
                    color = IslamicGold.copy(0.04f),
                    start = Offset(cx, cy),
                    end = Offset(cx + r * cos(angle.toDouble()).toFloat(), cy + r * sin(angle.toDouble()).toFloat()),
                    strokeWidth = 1.dp.toPx()
                )
            }
            for (ratio in listOf(0.35f, 0.65f, 0.95f)) {
                drawCircle(
                    color = IslamicGold.copy(0.04f),
                    radius = r * ratio, center = Offset(cx, cy),
                    style = Stroke(1.dp.toPx())
                )
            }
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(IslamicTeal.copy(glowPulse * 0.07f), Color.Transparent),
                    center = Offset(cx, cy), radius = r
                ),
                radius = r, center = Offset(cx, cy)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .clip(RoundedCornerShape(28.dp))
                .background(DeepCard.copy(0.92f))
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, IslamicGold.copy(0.7f), Color.Transparent)
                        ),
                        topLeft = Offset.Zero,
                        size = Size(size.width, 2.dp.toPx())
                    )
                    drawRect(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, IslamicGold.copy(0.7f), Color.Transparent)
                        ),
                        topLeft = Offset(0f, size.height - 2.dp.toPx()),
                        size = Size(size.width, 2.dp.toPx())
                    )
                }
                .padding(horizontal = 32.dp, vertical = 24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        Modifier.weight(1f).height(1.dp).background(
                            Brush.horizontalGradient(listOf(Color.Transparent, IslamicGold.copy(0.6f)))
                        )
                    )
                    Text("  ☪  ", color = IslamicGold, style = LocalTextStyle.current.copy(fontSize = 20.sp))
                    Box(
                        Modifier.weight(1f).height(1.dp).background(
                            Brush.horizontalGradient(listOf(IslamicGold.copy(0.6f), Color.Transparent))
                        )
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "HADIST HARIAN",
                    color = IslamicGold,
                    fontWeight = FontWeight.Bold,
                    style = LocalTextStyle.current.copy(fontSize = 14.sp, letterSpacing = 4.sp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "“",
                    color = IslamicTeal.copy(glowPulse * 0.8f + 0.2f),
                    fontWeight = FontWeight.Bold,
                    style = LocalTextStyle.current.copy(fontSize = 52.sp),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Text(
                    displayText,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = LocalTextStyle.current.copy(fontSize = 16.sp, lineHeight = 27.sp)
                )
                Text(
                    "”",
                    color = IslamicTeal.copy(glowPulse * 0.8f + 0.2f),
                    fontWeight = FontWeight.Bold,
                    style = LocalTextStyle.current.copy(fontSize = 52.sp),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(IslamicGold.copy(0.45f), IslamicTeal.copy(0.45f))
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Text(
                        "— $displaySource",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomePageContent(
    pairingUiState: PairingUiState,
    pairingStatusText: String,
    timeText: String,
    dayName: String,
    dateText: String,
    hijriText: String,
    arabicDayName: String,
    todayPrayerList: List<Pair<String, String>>,
    nextPrayerName: String,
    nextPrayerTime: String,
    countdownState: CountdownState,
    countdownDigits: String,
    countdownLabel: String,
    countdownUnitLabel: String,
    currentPage: CenterPage
) {
    val infiniteTransition = rememberInfiniteTransition(label = "home_anim")
    val shimmerOffset by infiniteTransition.animateFloat(
        -0.4f, 1.4f,
        infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "shimmer"
    )
    val centerGlow by infiniteTransition.animateFloat(
        0.2f, 0.9f,
        infiniteRepeatable(tween(1600), RepeatMode.Reverse),
        label = "center_glow"
    )
    val clockPulse by infiniteTransition.animateFloat(
        0.5f, 1.0f,
        infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "clock_pulse"
    )
    val runningTextValue = pairingUiState.settings.runningText.trim().ifEmpty {
        "Info: Sholat berjamaah dimulai 10 menit setelah adzan. Mohon merapatkan shaf dan menonaktifkan nada dering."
    }

    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp).fillMaxSize()) {

        // ── TOP BAR ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Mosque Card
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DeepCard.copy(alpha = 0.88f))
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            color = IslamicGold.copy(alpha = 0.75f),
                            topLeft = Offset.Zero,
                            size = Size(size.width, 3.dp.toPx())
                        )
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        "☪  ${pairingUiState.settings.mosqueName}",
                        fontWeight = FontWeight.Bold,
                        color = IslamicGold,
                        style = LocalTextStyle.current.copy(fontSize = 13.sp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${pairingUiState.settings.city}, ${pairingUiState.settings.province}",
                        color = Color.White.copy(alpha = 0.65f),
                        style = LocalTextStyle.current.copy(fontSize = 11.sp)
                    )
                    Spacer(Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (pairingUiState.paired) IslamicTeal.copy(0.25f)
                                else Color(0xFFFF9900).copy(0.25f)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            pairingStatusText,
                            color = if (pairingUiState.paired) IslamicTeal else Color(0xFFFF9900),
                            fontWeight = FontWeight.Bold,
                            style = LocalTextStyle.current.copy(fontSize = 10.sp)
                        )
                    }
                }
            }

            // CENTER: Logo kiri | Jam | Logo kanan
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo kiri
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_polman),
                                contentDescription = "Logo Kampus",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    // Jam realtime (center sejati)
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(200.dp, 62.dp)
                                .background(
                                    Brush.radialGradient(
                                        listOf(IslamicTeal.copy(alpha = clockPulse * 0.12f), Color.Transparent)
                                    )
                                )
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                timeText.substring(0, 5),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = LocalTextStyle.current.copy(fontSize = 54.sp)
                            )
                            Text(
                                timeText.substring(5),
                                color = IslamicTeal,
                                fontWeight = FontWeight.Bold,
                                style = LocalTextStyle.current.copy(fontSize = 24.sp),
                                modifier = Modifier.padding(bottom = 7.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    // Logo kanan
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_polman),
                                contentDescription = "Logo Kampus",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(4.dp).background(IslamicGold, CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text(dayName, color = Color.White.copy(0.8f), style = LocalTextStyle.current.copy(fontSize = 13.sp))
                    Spacer(Modifier.width(6.dp))
                    Box(Modifier.size(4.dp).background(IslamicGold, CircleShape))
                }
            }

            // RIGHT: Date Card
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DeepCard.copy(alpha = 0.88f))
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            color = IslamicGold.copy(alpha = 0.75f),
                            topLeft = Offset.Zero,
                            size = Size(size.width, 3.dp.toPx())
                        )
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(dateText, fontWeight = FontWeight.Bold, color = Color.White, style = LocalTextStyle.current.copy(fontSize = 15.sp))
                    Text("Masehi", color = Color.White.copy(0.5f), style = LocalTextStyle.current.copy(fontSize = 11.sp))
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(IslamicGold.copy(0.4f)))
                    Spacer(Modifier.height(4.dp))
                    Text(hijriText, fontWeight = FontWeight.Bold, color = IslamicGold, style = LocalTextStyle.current.copy(fontSize = 12.sp))
                    Text("$arabicDayName • Hijriah", color = Color.White.copy(0.55f), style = LocalTextStyle.current.copy(fontSize = 10.sp))
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── MAIN CONTENT ─────────────────────────────────────────
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // LEFT: Prayer List
            Box(
                modifier = Modifier
                    .width(210.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DeepCard.copy(0.88f))
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(listOf(IslamicGold, IslamicGold.copy(0.1f))),
                            topLeft = Offset.Zero,
                            size = Size(3.dp.toPx(), size.height)
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 12.dp, end = 10.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✦", color = IslamicGold, style = LocalTextStyle.current.copy(fontSize = 10.sp))
                        Text(
                            "  Waktu Sholat  ",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = LocalTextStyle.current.copy(fontSize = 13.sp, letterSpacing = 1.sp)
                        )
                        Text("✦", color = IslamicGold, style = LocalTextStyle.current.copy(fontSize = 10.sp))
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(IslamicGold.copy(0.3f)))
                    Spacer(Modifier.height(6.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        todayPrayerList.forEach { (name, time) ->
                            val isNext = pairingUiState.todaySchedule != null && name == nextPrayerName
                            val icon = when (name) {
                                "Subuh" -> "◐"; "Syuruk" -> "☀"; "Dzuhur" -> "☀"
                                "Ashar" -> "◕"; "Maghrib" -> "☾"; "Isya" -> "★"; else -> "✦"
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isNext) IslamicTeal.copy(0.18f) else Color.Transparent)
                                    .then(
                                        if (isNext) Modifier.drawWithContent {
                                            drawContent()
                                            drawRect(
                                                brush = Brush.horizontalGradient(
                                                    listOf(Color.Transparent, Color.White.copy(0.22f), Color.Transparent),
                                                    startX = size.width * (shimmerOffset - 0.3f),
                                                    endX = size.width * (shimmerOffset + 0.3f)
                                                )
                                            )
                                            drawRect(
                                                color = IslamicGold,
                                                topLeft = Offset.Zero,
                                                size = Size(2.5.dp.toPx(), size.height)
                                            )
                                        } else Modifier
                                    )
                                    .padding(horizontal = 8.dp, vertical = 7.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            icon,
                                            color = if (isNext) IslamicGold else Color.White.copy(0.35f),
                                            style = LocalTextStyle.current.copy(fontSize = 11.sp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            name,
                                            color = if (isNext) Color.White else Color.White.copy(0.7f),
                                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                                            style = LocalTextStyle.current.copy(fontSize = 12.sp)
                                        )
                                    }
                                    Text(
                                        time,
                                        color = if (isNext) IslamicGold else IslamicTeal.copy(0.75f),
                                        fontWeight = FontWeight.Bold,
                                        style = LocalTextStyle.current.copy(fontSize = 12.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // CENTER: Next Prayer
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF062030), Color(0xFF0A3040), Color(0xFF062030)),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2; val cy = size.height / 2
                    val r = size.minDimension * 0.42f
                    for (i in 0 until 12) {
                        val angle = i * 30.0 * PI / 180.0
                        drawLine(
                            color = Color.White.copy(0.025f),
                            start = Offset(cx, cy),
                            end = Offset(cx + r * cos(angle).toFloat(), cy + r * sin(angle).toFloat()),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                    for (ratio in listOf(0.35f, 0.65f, 0.9f)) {
                        drawCircle(
                            color = Color.White.copy(0.025f),
                            radius = r * ratio, center = Offset(cx, cy),
                            style = Stroke(1.dp.toPx())
                        )
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(IslamicTeal.copy(centerGlow * 0.18f), Color.Transparent),
                            center = Offset(cx, cy), radius = r * 0.75f
                        ),
                        radius = r * 0.75f, center = Offset(cx, cy)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "✦  SHOLAT SELANJUTNYA  ✦",
                        color = IslamicGold.copy(0.8f),
                        style = LocalTextStyle.current.copy(fontSize = 10.sp, letterSpacing = 2.sp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.img_3),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        nextPrayerName.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = LocalTextStyle.current.copy(fontSize = 58.sp, letterSpacing = 3.sp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(220.dp)
                    ) {
                        Box(Modifier.weight(1f).height(1.dp).background(IslamicGold.copy(0.45f)))
                        Text("  ✦  ", color = IslamicGold, style = LocalTextStyle.current.copy(fontSize = 10.sp))
                        Box(Modifier.weight(1f).height(1.dp).background(IslamicGold.copy(0.45f)))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Pukul  ",
                            color = Color.White.copy(0.7f),
                            style = LocalTextStyle.current.copy(fontSize = 16.sp)
                        )
                        Text(
                            nextPrayerTime,
                            color = IslamicGold,
                            fontWeight = FontWeight.Bold,
                            style = LocalTextStyle.current.copy(fontSize = 40.sp)
                        )
                    }
                }
            }

            // RIGHT: Countdown Ring + Lokasi
            Column(
                modifier = Modifier.width(200.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(DeepCard.copy(0.88f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            "Hitung Mundur",
                            color = Color.White.copy(0.6f),
                            fontWeight = FontWeight.Bold,
                            style = LocalTextStyle.current.copy(fontSize = 11.sp)
                        )
                        Spacer(Modifier.height(6.dp))
                        CountdownRingPanel(
                            countdownState = countdownState,
                            countdownDigits = countdownDigits,
                            countdownLabel = countdownLabel,
                            countdownUnitLabel = countdownUnitLabel
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DeepCard.copy(0.78f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("☪ Lokasi", color = IslamicGold.copy(0.8f), style = LocalTextStyle.current.copy(fontSize = 10.sp))
                        Spacer(Modifier.height(2.dp))
                        Text(pairingUiState.settings.city, color = Color.White, fontWeight = FontWeight.Bold, style = LocalTextStyle.current.copy(fontSize = 13.sp))
                        Text(pairingUiState.settings.province, color = Color.White.copy(0.6f), style = LocalTextStyle.current.copy(fontSize = 11.sp))
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        PageIndicator(currentPage = currentPage, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(6.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DeepCard.copy(alpha = 0.72f)),
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape
        ) {
            RunningTickerText(
                text = runningTextValue,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(36.dp),
                textColor = Color.White,
                speed = pairingUiState.settings.runningTextSpeed,
                brightness = pairingUiState.settings.runningTextBrightness
            )
        }
    }
}

@Composable
private fun CountdownRingPanel(
    countdownState: CountdownState,
    countdownDigits: String,
    countdownLabel: String,
    countdownUnitLabel: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "countdown_ring")
    val rotateAngle by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "ring_rotate"
    )
    val pulseScale by infiniteTransition.animateFloat(
        0.96f, 1.04f,
        infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "ring_pulse"
    )

    val phase = countdownState.phase
    val phaseColor = when (phase) {
        CountdownPhase.IQOMAH -> Color(0xFFFF7043)
        CountdownPhase.ADZAN_HOLD -> Color(0xFFFFB300)
        else -> IslamicTeal
    }
    val totalSecs = when (phase) {
        CountdownPhase.IQOMAH -> IQOMAH_DURATION_MINUTES * 60L
        CountdownPhase.ADZAN_HOLD -> ADZAN_HOLD_MINUTES * 60L
        else -> 1L
    }
    val progress = if (phase == CountdownPhase.BEFORE_PRAYER) 1f
    else (countdownState.remainingSeconds.toFloat() / totalSecs).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .size(148.dp)
            .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 9.dp.toPx()
            val decorStroke = 2.5.dp.toPx()
            val pad = stroke / 2 + 10.dp.toPx()
            val outerPad = stroke / 2 + 2.dp.toPx()
            val d = size.minDimension - pad * 2
            val outerD = size.minDimension - outerPad * 2
            val tl = Offset((size.width - d) / 2, (size.height - d) / 2)
            val outerTl = Offset((size.width - outerD) / 2, (size.height - outerD) / 2)

            drawArc(
                color = Color.White.copy(0.07f), 0f, 360f, false,
                tl, Size(d, d), style = Stroke(stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = phaseColor, -90f, 360f * progress, false,
                tl, Size(d, d), style = Stroke(stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = IslamicGold, rotateAngle - 90f, 55f, false,
                outerTl, Size(outerD, outerD), style = Stroke(decorStroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = IslamicGold.copy(0.35f), rotateAngle + 90f, 55f, false,
                outerTl, Size(outerD, outerD), style = Stroke(decorStroke, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(phaseColor.copy(0.2f))
                    .padding(horizontal = 7.dp, vertical = 1.dp)
            ) {
                Text(
                    countdownLabel.uppercase(),
                    color = phaseColor,
                    fontWeight = FontWeight.Bold,
                    style = LocalTextStyle.current.copy(fontSize = 8.sp, letterSpacing = 1.sp)
                )
            }
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${countdownDigits[0]}${countdownDigits[1]}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = LocalTextStyle.current.copy(fontSize = 28.sp)
                )
                Text(
                    " : ",
                    color = IslamicGold,
                    fontWeight = FontWeight.Bold,
                    style = LocalTextStyle.current.copy(fontSize = 20.sp)
                )
                Text(
                    "${countdownDigits[2]}${countdownDigits[3]}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = LocalTextStyle.current.copy(fontSize = 28.sp)
                )
            }
            val parts = countdownUnitLabel.split(" : ")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    parts.getOrElse(0) { "MNT" }.uppercase(),
                    color = IslamicTeal.copy(0.7f),
                    style = LocalTextStyle.current.copy(fontSize = 8.sp)
                )
                Text(" : ", color = IslamicGold.copy(0.5f), style = LocalTextStyle.current.copy(fontSize = 8.sp))
                Text(
                    parts.getOrElse(1) { "DTK" }.uppercase(),
                    color = IslamicTeal.copy(0.7f),
                    style = LocalTextStyle.current.copy(fontSize = 8.sp)
                )
            }
        }
    }
}
