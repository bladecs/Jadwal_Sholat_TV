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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.io.path.Path
import kotlin.math.roundToInt

val gradientColors = listOf(Color(0xFF01A671), Color(0xFF00A78A),Color(0xFF005066))

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
    var countdownSeconds by remember { mutableStateOf(12 * 60) } // 12:00
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

    LaunchedEffect(countdownSeconds) {
        if (countdownSeconds > 0) {
            delay(1_000)
            countdownSeconds--
        }
    }

    val minutes = countdownSeconds / 60
    val seconds = countdownSeconds % 60
    val countdownDigits = String.format("%02d%02d", minutes, seconds)

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
                                        text = "Nama Masjid",
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
                                        text = "Senin",
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
                                        text = "06/03/2026",
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
                                            text = "DZUHUR",
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
                                            text = "11:50",
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
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(100.dp)
//                            .clip(RoundedCornerShape(12.dp))
//                            .background(
//                                color = Color.White.copy(alpha = 0.7f)
//                            ),
//                        contentAlignment = Alignment.Center
//                    ){
//                        Column (
//                            modifier = Modifier.padding(15.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
//                        ){
//                            Text(
//                                text = "ID TV: $shortDeviceId",
//                                textAlign = TextAlign.Center,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF005066),
//                                style = LocalTextStyle.current.copy(fontSize = 20.sp),
//                                modifier = Modifier.fillMaxWidth()
//                            )
//                            pairingUiState.errorMessage?.let { errorMessage ->
//                                Text(
//                                    text = errorMessage,
//                                    textAlign = TextAlign.Left,
//                                    color = Color(0xFFB71C1C),
//                                    style = LocalTextStyle.current.copy(fontSize = 10.sp),
//                                    maxLines = 2,
//                                    modifier = Modifier.fillMaxWidth()
//                                )
//                            }
//                        }
//                    }
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
                            Row (
                                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                            ) {
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
                                        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                                    ){
                                        Text(
                                            text = "12",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            style = LocalTextStyle.current.copy(fontSize = 25.sp)
                                        )
                                        Text(
                                            text = "30",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF9900),
                                            style = LocalTextStyle.current.copy(fontSize = 25.sp)
                                        )
                                        Text(
                                            text = "Subuh",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            style = LocalTextStyle.current.copy(fontSize = 15.sp)
                                        )
                                    }
                                }
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
                                        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                                    ){
                                        Text(
                                            text = "12",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            style = LocalTextStyle.current.copy(fontSize = 25.sp)
                                        )
                                        Text(
                                            text = "30",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF9900),
                                            style = LocalTextStyle.current.copy(fontSize = 25.sp)
                                        )
                                        Text(
                                            text = "Subuh",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            style = LocalTextStyle.current.copy(fontSize = 15.sp)
                                        )
                                    }
                                }
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
                                        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                                    ){
                                        Text(
                                            text = "12",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            style = LocalTextStyle.current.copy(fontSize = 25.sp)
                                        )
                                        Text(
                                            text = "30",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF9900),
                                            style = LocalTextStyle.current.copy(fontSize = 25.sp)
                                        )
                                        Text(
                                            text = "Subuh",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            style = LocalTextStyle.current.copy(fontSize = 15.sp)
                                        )
                                    }
                                }
                            }
                            Row (
                                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                            ) {
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
                                        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                                    ){
                                        Text(
                                            text = "12",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            style = LocalTextStyle.current.copy(fontSize = 25.sp)
                                        )
                                        Text(
                                            text = "30",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF9900),
                                            style = LocalTextStyle.current.copy(fontSize = 25.sp)
                                        )
                                        Text(
                                            text = "Subuh",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            style = LocalTextStyle.current.copy(fontSize = 15.sp)
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .width(65.dp)
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
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
                                        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                                    ){
                                        Text(
                                            text = "12",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            style = LocalTextStyle.current.copy(fontSize = 25.sp)
                                        )
                                        Text(
                                            text = "30",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF9900),
                                            style = LocalTextStyle.current.copy(fontSize = 25.sp)
                                        )
                                        Text(
                                            text = "Subuh",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            style = LocalTextStyle.current.copy(fontSize = 15.sp)
                                        )
                                    }
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
                                        text = "Iqomah",
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
            Card (
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.7f)
                ),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = CircleShape
            ){
                RunningTickerText(
                    text = "Info: Sholat berjamaah dimulai 10 menit setelah adzan. Mohon merapatkan shaf dan menonaktifkan nada dering.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    textColor = Color.Black
                )
            }
        }

    }
}

@Composable
fun RunningTickerText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Black
) {
    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    var textWidthPx by remember { mutableFloatStateOf(0f) }

    val transition = rememberInfiniteTransition(label = "tickerTransition")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
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
            color = textColor,
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
