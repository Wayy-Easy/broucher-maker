package com.brochurecraft.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brochurecraft.app.ui.theme.*
import kotlinx.coroutines.launch

private data class OnboardingPage(val title: String, val body: String)

private val pages = listOf(
    OnboardingPage(
        "Create beautiful restaurant designs in minutes.",
        "Craft stunning menus, promotional flyers, and social posts effortlessly with our intuitive tools."
    ),
    OnboardingPage(
        "Stay on-brand, every time.",
        "Save your logo, colors and contact details once - every design instantly matches your brand."
    ),
    OnboardingPage(
        "Export print-ready or share instantly.",
        "Download high-resolution PDFs for print or share straight to Instagram and WhatsApp."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingWelcomeScreen(onNext: () -> Unit, onSkip: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(VCSurface)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF2B2E63)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Restaurant,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.size(140.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(VCSurface, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.height(160.dp)) { page ->
                Column {
                    Text(
                        text = pages[page].title,
                        style = HeadlineLgMobile,
                        color = VCOnSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = pages[page].body,
                        style = BodyLg,
                        color = VCOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                pages.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(if (pagerState.currentPage == i) 24.dp else 8.dp)
                            .background(
                                if (pagerState.currentPage == i) VCPrimary else VCSurfaceContainerHigh,
                                CircleShape
                            )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.lastIndex) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onNext()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VCPrimary, contentColor = VCOnPrimary),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Next", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text("Skip", color = VCOnSurfaceVariant)
            }
        }
    }
}
