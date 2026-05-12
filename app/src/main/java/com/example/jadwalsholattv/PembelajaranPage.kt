package com.example.jadwalsholattv

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PembelajaranPage(onBackToHome: () -> Unit) {
    val learningSlides = listOf(
        R.drawable.img_sholat,
        R.drawable.img_wudhu
    )
    val pagerState = rememberPagerState(pageCount = { learningSlides.size })

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pembelajaran",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                style = LocalTextStyle.current.copy(fontSize = 20.sp)
            )
            BackHomeButton(onClick = onBackToHome)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .height(360.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Image(
                        painter = painterResource(id = learningSlides[page]),
                        contentDescription = "Materi pembelajaran ${page + 1}",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Slide ${pagerState.currentPage + 1}/${learningSlides.size}",
            color = Color(0xFF005066),
            fontWeight = FontWeight.Bold,
            style = LocalTextStyle.current.copy(fontSize = 14.sp)
        )
    }
}

@Composable
private fun BackHomeButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(80.dp)
            )
            .width(170.dp)
            .height(38.dp)
            .clip(RoundedCornerShape(80.dp))
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Kembali ke Home",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            style = LocalTextStyle.current.copy(fontSize = 13.sp)
        )
    }
}
