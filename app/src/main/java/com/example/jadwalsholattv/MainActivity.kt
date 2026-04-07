package com.example.jadwalsholattv

import android.graphics.BlurMaskFilter
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.jadwalsholattv.data.FirebasePairingManager
import com.example.jadwalsholattv.data.PairingUiState
import com.example.jadwalsholattv.data.DailyPrayerSchedule
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.io.path.Path
import kotlin.math.roundToInt
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

val gradientColors = listOf(Color(0xFF01A671), Color(0xFF00A78A),Color(0xFF005066))

private enum class CountdownPhase {
    BEFORE_PRAYER,
    ADZAN_HOLD,
    IQOMAH
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
                pairingUiState.value = pairingUiState.value.copy(errorMessage = error.message)
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
    val pairingStatusText = when {
        pairingUiState.paired -> "Terhubung"
        pairingUiState.pairingCode.isNullOrBlank() -> "Belum Pairing"
        else -> "Pair ${pairingUiState.pairingCode}"
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

    val zoneId = runCatching { ZoneId.of(pairingUiState.settings.timezone) }
        .getOrElse { ZoneId.systemDefault() }
    val now = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDateTime()
    val dayName = now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("id", "ID"))
    val dateText = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

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
    val minutes = (remainingSeconds / 60).toInt().coerceAtLeast(0)
    val seconds = (remainingSeconds % 60).toInt().coerceAtLeast(0)
    val countdownDigits = String.format("%02d%02d", minutes, seconds)

    val todayPrayerList = listOf(
        "Subuh" to (pairingUiState.todaySchedule?.fajr ?: "--:--"),
        "Dzuhur" to (pairingUiState.todaySchedule?.dzuhur ?: "--:--"),
        "Ashar" to (pairingUiState.todaySchedule?.ashar ?: "--:--"),
        "Maghrib" to (pairingUiState.todaySchedule?.maghrib ?: "--:--"),
        "Isya" to (pairingUiState.todaySchedule?.isya ?: "--:--")
    )

    // Gunakan Box untuk menumpuk elemen (Z-axis)
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. Lapisan Paling Bawah: Gambar Background
        Image(
            painter = painterResource(id = R.drawable.bck_dsh),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // 2. Lapisan Tengah: Overlay Hitam
        // Menggunakan Box kosong dengan background lebih ringan daripada menggunakan Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        // 3. Lapisan Atas: Konten Utama
        Column (modifier = Modifier.padding(20.dp).fillMaxSize()){
            Row(modifier = Modifier.weight(8f),horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                // Left Panel
                Column {
                    // Bagian Lokasi
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.size(width = 260.dp, height = 160.dp)
                    ) {
                        Column (modifier = Modifier.padding(15.dp).weight(1f), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
                            Text(
                                text = "Lokasi",
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                style = LocalTextStyle.current.copy(fontSize = 15.sp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row (
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column (
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start
                                ){
                                    Text(
                                        text = "Nama Device",
                                        textAlign = TextAlign.Left,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        style = LocalTextStyle.current.copy(fontSize = 13.sp)
                                    )
                                    Text(
                                        text = pairingUiState.settings.mosqueName,
                                        textAlign = TextAlign.Left,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        style = LocalTextStyle.current.copy(fontSize = 15.sp)

                                    )
                                }
                                Card (
                                    modifier = Modifier.size(height = 30.dp, width = 80.dp),
                                    shape = CircleShape,
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = pairingStatusText,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 10.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .height(1.dp)
                                    .background(
                                        color = Color.Gray.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                            )
                            Row (
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column (
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start
                                ){
                                    Text(
                                        text = "Provinsi",
                                        textAlign = TextAlign.Left,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        style = LocalTextStyle.current.copy(fontSize = 13.sp)
                                    )
                                    Text(
                                        text = pairingUiState.settings.province,
                                        textAlign = TextAlign.Left,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        style = LocalTextStyle.current.copy(fontSize = 15.sp)

                                    )
                                }
                                Column (
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.End
                                ){
                                    Text(
                                        text = "Kota",
                                        textAlign = TextAlign.Left,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        style = LocalTextStyle.current.copy(fontSize = 13.sp)
                                    )
                                    Text(
                                        text = pairingUiState.settings.city,
                                        textAlign = TextAlign.Left,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        style = LocalTextStyle.current.copy(fontSize = 15.sp)

                                    )
                                }
                            }
                        }
                    }
                    Spacer( modifier = Modifier.size(width = 260.dp,height = 15.dp) )

                    // Bagian Waktu Sholat
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.size(width = 260.dp, height = 700.dp)
                    ) {
                        Column (modifier = Modifier.padding(vertical = 5.dp, horizontal = 15.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
                            Row (modifier = Modifier.height(65.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
                                Column (modifier = Modifier.weight(2f), horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = "Hari",
                                        textAlign = TextAlign.Left,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF005066),
                                        style = LocalTextStyle.current.copy(fontSize = 12.sp)
                                    )
                                    Text(
                                        text = dayName,
                                        textAlign = TextAlign.Left,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        style = LocalTextStyle.current.copy(fontSize = 12.sp)
                                    )
                                }
                                Column (modifier = Modifier.weight(2f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Waktu",
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        style = LocalTextStyle.current.copy(fontSize = 12.sp)
                                    )
                                    Text(
                                        text = "Sholat Fardu",
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        style = LocalTextStyle.current.copy(fontSize = 12.sp)
                                    )
                                }
                                Column (modifier = Modifier.weight(2f), horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Tanggal",
                                        textAlign = TextAlign.Right,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF005066),
                                        style = LocalTextStyle.current.copy(fontSize = 12.sp)
                                    )
                                    Text(
                                        text = dateText,
                                        textAlign = TextAlign.Right,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        style = LocalTextStyle.current.copy(fontSize = 12.sp)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = gradientColors,
                                            start = Offset(0f, Float.POSITIVE_INFINITY),
                                            end = Offset(Float.POSITIVE_INFINITY, 0f)
                                        )
                                    )
                            ) {
                                Column (modifier = Modifier.padding(13.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
                                    Row (
                                        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                                    ){
                                        Text(
                                            text = nextPrayerName.uppercase(),
                                            textAlign = TextAlign.Left,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            style = LocalTextStyle.current.copy(fontSize = 25.sp)
                                        )
                                        Image(
                                            painter = painterResource(id = R.drawable.img_3),
                                            contentDescription = null,
                                            modifier = Modifier.size(width = 45.dp, height = 45.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp)
                                            .height(1.dp)
                                            .background(
                                                color = Color.White,
                                                shape = CircleShape
                                            )
                                    )
                                    Row (
                                        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                                    ){
                                        Text(
                                            text = "WAKTU :",
                                            textAlign = TextAlign.Left,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            style = LocalTextStyle.current.copy(fontSize = 15.sp)
                                        )
                                        Text(
                                            text = nextPrayerTime,
                                            textAlign = TextAlign.Left,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            style = LocalTextStyle.current.copy(fontSize = 25.sp)
                                        )
                                    }
                                }
                            }
                            Row (modifier = Modifier.height(65.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
                                Column (modifier = Modifier.weight(2f), horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = "Hari",
                                        textAlign = TextAlign.Left,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF005066),
                                        style = LocalTextStyle.current.copy(fontSize = 12.sp)
                                    )
                                    Text(
                                        text = "Al-Itsnain",
                                        textAlign = TextAlign.Left,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        style = LocalTextStyle.current.copy(fontSize = 12.sp)
                                    )
                                }
                                Column (modifier = Modifier.weight(2f), horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Tanggal Hijriah",
                                        textAlign = TextAlign.Right,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF005066),
                                        style = LocalTextStyle.current.copy(fontSize = 12.sp)
                                    )
                                    Text(
                                        text = "6 Ramadhan 1446 H",
                                        textAlign = TextAlign.Right,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        style = LocalTextStyle.current.copy(fontSize = 12.sp)
                                    )
                                }
                            }
                        }
                    }
                }
                // Navbar
                Column (modifier = Modifier.weight(1f),horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
                    Spacer( modifier = Modifier.size(width = 380.dp,height = 360.dp) )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                color = Color.White.copy(alpha = 0.7f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row (
                            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                        ){
                            Box(
                                modifier = Modifier
                                    .shadow(
                                        elevation = 5.dp,
                                        shape = RoundedCornerShape(80.dp)
                                    )
                                    .width(150.dp)
                                    .height(45.dp)
                                    .clip(RoundedCornerShape(80.dp))
                                    .background(
                                        color = Color.White
                                    )
                            )
                            Spacer(
                                modifier = Modifier.width(15.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .shadow(
                                        elevation = 5.dp,
                                        shape = RoundedCornerShape(80.dp)
                                    )
                                    .width(150.dp)
                                    .height(45.dp)
                                    .clip(RoundedCornerShape(80.dp))
                                    .background(
                                        color = Color.White
                                    )
                            )
                        }
                    }
                }
                Column {
                    // Bagian Jadwal Sholat
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.size(width = 260.dp, height = 260.dp)
                    ) {
                        Column (
                            modifier = Modifier.padding(20.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Jadwal Sholat",
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                style = LocalTextStyle.current.copy(fontSize = 15.sp)
                            )
                            val firstRow = todayPrayerList.take(3)
                            val secondRow = todayPrayerList.drop(3)

                            Row (
                                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                            ) {
                                firstRow.forEach { (label, time) ->
                                    PrayerTimeBox(time = time, label = label)
                                }
                            }
                            Row (
                                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (secondRow.isNotEmpty()) {
                                    PrayerTimeBox(time = secondRow[0].second, label = secondRow[0].first)
                                } else {
                                    Box(modifier = Modifier.width(65.dp).height(90.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .width(65.dp)
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                if (secondRow.size > 1) {
                                    PrayerTimeBox(time = secondRow[1].second, label = secondRow[1].first)
                                } else {
                                    Box(modifier = Modifier.width(65.dp).height(90.dp))
                                }
                            }
                        }
                    }
                    Spacer( modifier = Modifier.size(width = 260.dp,height = 15.dp) )
                    // Bagian Hitung Mundur Sholat dan Iqomah
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.size(width = 260.dp, height = 160.dp)
                    ) {
                        Column (
                            modifier = Modifier.padding(15.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween,
                        ){
                            Row (
                                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                            ){
                                Text(
                                    text = "Hitung Mundur",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Box(
                                    modifier = Modifier
                                        .shadow(
                                            elevation = 5.dp,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .width(80.dp)
                                        .height(25.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            color = Color(0xFF00A78A)
                                        ),
                                    contentAlignment = Alignment.Center,
                                ){
                                    Text(
                                        text = countdownLabel,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Row (
                                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                            ){
                                Box(
                                    modifier = Modifier
                                        .shadow(
                                            elevation = 5.dp,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .width(50.dp)
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            color = Color(0xFF00A78A)
                                        ),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text(
                                        text = countdownDigits[0].toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        style = LocalTextStyle.current.copy(fontSize = 60.sp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .shadow(
                                            elevation = 5.dp,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .width(50.dp)
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            color = Color(0xFF00A78A)
                                        ),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text(
                                        text = countdownDigits[1].toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        style = LocalTextStyle.current.copy(fontSize = 60.sp)
                                    )
                                }
                                Column (
                                    modifier = Modifier.width(15.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                                ){
                                    Box(
                                        modifier = Modifier
                                            .width(10.dp)
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(color = Color(0xFFFF9900))
                                    )
                                    Spacer(
                                        modifier = Modifier.height(15.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(10.dp)
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(color = Color(0xFFFF9900))
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .shadow(
                                            elevation = 5.dp,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .width(50.dp)
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            color = Color(0xFF00A78A)
                                        ),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text(
                                        text = countdownDigits[2].toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        style = LocalTextStyle.current.copy(fontSize = 60.sp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .shadow(
                                            elevation = 5.dp,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .width(50.dp)
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            color = Color(0xFF00A78A)
                                        ),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text(
                                        text = countdownDigits[3].toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        style = LocalTextStyle.current.copy(fontSize = 60.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(15.dp))
            val runningTextValue = pairingUiState.settings.runningText.trim().ifEmpty {
                "Info: Sholat berjamaah dimulai 10 menit setelah adzan. Mohon merapatkan shaf dan menonaktifkan nada dering."
            }
            val runningTextSpeed = pairingUiState.settings.runningTextSpeed
            val runningTextBrightness = pairingUiState.settings.runningTextBrightness

            Card (
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.7f)
                ),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = CircleShape
            ){
                RunningTickerText(
                    text = runningTextValue,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    textColor = Color.Black,
                    speed = runningTextSpeed,
                    brightness = runningTextBrightness
                )
            }
        }

    }
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

private fun computeCountdownState(
    now: LocalDateTime,
    todaySchedule: DailyPrayerSchedule?,
    tomorrowSchedule: DailyPrayerSchedule?
): CountdownState {
    val todayDate = now.toLocalDate()
    val prayers = buildPrayerList(todaySchedule, todayDate) +
        buildPrayerList(tomorrowSchedule, todayDate.plusDays(1))

    val nextPrayer = prayers.firstOrNull { now.isBefore(it.time.plusMinutes(5)) }
    if (nextPrayer == null) {
        return CountdownState(
            nextPrayer = null,
            phase = CountdownPhase.BEFORE_PRAYER,
            remainingSeconds = 0,
            label = "Iqomah"
        )
    }

    val prayerTime = nextPrayer.time
    return when {
        now.isBefore(prayerTime) -> CountdownState(
            nextPrayer = nextPrayer,
            phase = CountdownPhase.BEFORE_PRAYER,
            remainingSeconds = Duration.between(now, prayerTime).seconds,
            label = nextPrayer.name
        )
        now.isBefore(prayerTime.plusMinutes(2)) -> CountdownState(
            nextPrayer = nextPrayer,
            phase = CountdownPhase.ADZAN_HOLD,
            remainingSeconds = 0,
            label = nextPrayer.name
        )
        now.isBefore(prayerTime.plusMinutes(5)) -> CountdownState(
            nextPrayer = nextPrayer,
            phase = CountdownPhase.IQOMAH,
            remainingSeconds = Duration.between(now, prayerTime.plusMinutes(5)).seconds,
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
