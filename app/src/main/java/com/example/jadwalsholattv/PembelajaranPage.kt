package com.example.jadwalsholattv

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class LearningSlide(
    val imageRes: Int,
    val title: String,
    val subtitle: String
)

val LEARNING_SLIDES = listOf(
    LearningSlide(R.drawable.img_sholat, "Tata Cara Sholat", "Panduan gerakan dan bacaan sholat yang benar"),
    LearningSlide(R.drawable.img_wudhu, "Tata Cara Wudhu", "Langkah-langkah wudhu yang sempurna")
)

@Composable
fun PembelajaranPage(
    currentPhotoIndex: Int,
    onBackToHome: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "learn_anim")
    val borderGlow by infiniteTransition.animateFloat(
        0.45f, 1.0f,
        infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "border_glow"
    )
    val rotateOrnament by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(20000, easing = LinearEasing)),
        label = "ornament_rotate"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── HEADER ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.weight(1f).height(1.dp).background(
                    Brush.horizontalGradient(listOf(Color.Transparent, IslamicGold.copy(0.55f)))
                )
            )
            Text(
                "  ✦  MATERI PEMBELAJARAN  ✦  ",
                color = IslamicGold,
                fontWeight = FontWeight.Bold,
                style = LocalTextStyle.current.copy(fontSize = 13.sp, letterSpacing = 3.sp)
            )
            Box(
                Modifier.weight(1f).height(1.dp).background(
                    Brush.horizontalGradient(listOf(IslamicGold.copy(0.55f), Color.Transparent))
                )
            )
        }

        Spacer(Modifier.height(10.dp))

        // ── IMAGE + CAPTION (animates on each new visit) ─────────
        AnimatedContent(
            targetState = currentPhotoIndex,
            transitionSpec = {
                (slideInHorizontally { it } + fadeIn(tween(500))) togetherWith
                (slideOutHorizontally { -it } + fadeOut(tween(350)))
            },
            label = "image_switch",
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) { photoIdx ->
            val slide = LEARNING_SLIDES[photoIdx % LEARNING_SLIDES.size]
            val slideIdx = photoIdx % LEARNING_SLIDES.size

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                // Subtle background geometric ornament
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2; val cy = size.height / 2
                    val r = size.minDimension * 0.38f
                    for (i in 0 until 8) {
                        val angle = (rotateOrnament + i * 45f) * PI.toFloat() / 180f
                        drawLine(
                            color = IslamicGold.copy(0.04f),
                            start = Offset(cx, cy),
                            end = Offset(
                                cx + r * cos(angle.toDouble()).toFloat(),
                                cy + r * sin(angle.toDouble()).toFloat()
                            ),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(IslamicGold.copy(0.05f), Color.Transparent),
                            center = Offset(cx, cy), radius = r
                        ),
                        radius = r, center = Offset(cx, cy)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // ── Ornate image frame ──────────────────────
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(DeepCard.copy(0.65f))
                            .drawWithContent {
                                drawContent()
                                // Animated gold border
                                drawRoundRect(
                                    color = IslamicGold.copy(borderGlow * 0.5f + 0.25f),
                                    cornerRadius = CornerRadius(20.dp.toPx()),
                                    style = Stroke(2.dp.toPx())
                                )
                                // L-shape corner accents
                                val cSize = 22.dp.toPx()
                                val inset = 10.dp.toPx()
                                val lw = 2.5.dp.toPx()
                                val goldCorner = IslamicGold.copy(borderGlow * 0.75f + 0.15f)
                                // Top-left
                                drawLine(goldCorner, Offset(inset, inset), Offset(inset + cSize, inset), lw)
                                drawLine(goldCorner, Offset(inset, inset), Offset(inset, inset + cSize), lw)
                                // Top-right
                                drawLine(goldCorner, Offset(size.width - inset, inset), Offset(size.width - inset - cSize, inset), lw)
                                drawLine(goldCorner, Offset(size.width - inset, inset), Offset(size.width - inset, inset + cSize), lw)
                                // Bottom-left
                                drawLine(goldCorner, Offset(inset, size.height - inset), Offset(inset + cSize, size.height - inset), lw)
                                drawLine(goldCorner, Offset(inset, size.height - inset), Offset(inset, size.height - inset - cSize), lw)
                                // Bottom-right
                                drawLine(goldCorner, Offset(size.width - inset, size.height - inset), Offset(size.width - inset - cSize, size.height - inset), lw)
                                drawLine(goldCorner, Offset(size.width - inset, size.height - inset), Offset(size.width - inset, size.height - inset - cSize), lw)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = slide.imageRes),
                            contentDescription = slide.title,
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // ── Title badge ─────────────────────────────
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(IslamicGold.copy(0.35f), IslamicTeal.copy(0.35f))
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "☪  ${slide.title}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = LocalTextStyle.current.copy(fontSize = 16.sp, letterSpacing = 1.sp)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        slide.subtitle,
                        color = Color.White.copy(0.6f),
                        style = LocalTextStyle.current.copy(fontSize = 12.sp),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(10.dp))

                    // ── Slide dots ──────────────────────────────
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LEARNING_SLIDES.forEachIndexed { index, _ ->
                            val isActive = index == slideIdx
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(if (isActive) 22.dp else 6.dp, 6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (isActive) IslamicGold else Color.White.copy(0.3f)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
