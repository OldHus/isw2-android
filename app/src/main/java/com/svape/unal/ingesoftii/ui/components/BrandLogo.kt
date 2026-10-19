package com.svape.unal.ingesoftii.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val BrandNavy = Color(0xFF011252)

fun ingesoftLogoPath(): Path = Path().apply {
    moveTo(365f, 346f)
    lineTo(525f, 346f)
    lineTo(525f, 660f)
    cubicTo(525f, 725f, 570f, 775f, 627f, 775f)
    cubicTo(685f, 775f, 728f, 725f, 728f, 660f)
    lineTo(728f, 346f)
    lineTo(888f, 346f)
    lineTo(888f, 680f)
    cubicTo(888f, 830f, 790f, 918f, 627f, 918f)
    cubicTo(480f, 918f, 380f, 840f, 365f, 712f)
    cubicTo(375f, 722f, 385f, 730f, 398f, 737f)
    lineTo(568f, 832f)
    cubicTo(565f, 810f, 545f, 790f, 520f, 774f)
    lineTo(398f, 693f)
    cubicTo(380f, 682f, 365f, 665f, 365f, 640f)
    close()
}

@Composable
fun BrandMark(modifier: Modifier = Modifier, size: Dp = 32.dp) {
    val logo = remember { ingesoftLogoPath() }
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(BrandNavy),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val k = this.size.width / 1254f * 1.25f
            withTransform({
                translate(
                    left = this.size.width / 2f - 626.5f * k,
                    top = this.size.height / 2f - 632f * k
                )
                scale(scaleX = k, scaleY = k, pivot = Offset.Zero)
            }) {
                drawPath(logo, Color.White)
            }
        }
    }
}