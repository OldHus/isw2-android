package com.svape.unal.ingesoftii.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.svape.unal.ingesoftii.data.storage.DefaultProfilePhoto

@Composable
fun ProfileAvatar(
    name: String,
    photoUrl: String?,
    role: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    onPhotoClick: ((String) -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme

    val ownPhoto = remember(photoUrl) {
        photoUrl?.takeIf { it.isNotBlank() }?.replace("s96-c", "s400-c")
    }
    val defaultPhoto by produceState(
        initialValue = DefaultProfilePhoto.cached(role),
        role,
        ownPhoto
    ) {
        if (ownPhoto == null) value = DefaultProfilePhoto.get(role)
    }
    val resolvedUrl = ownPhoto ?: defaultPhoto

    val initial = remember(name) { name.trim().firstOrNull()?.uppercase() ?: "?" }

    val clickable = if (ownPhoto != null && onPhotoClick != null) {
        Modifier.clickable(onClickLabel = "Ver foto de $name en grande") { onPhotoClick(ownPhoto) }
    } else {
        Modifier
    }

    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.primaryContainer)
            .then(clickable),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initial,
            style = TextStyle(
                fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                fontSize = (size.value * 0.4f).sp,
            ),
            color = colors.onPrimaryContainer,
        )
        if (resolvedUrl != null) {
            AsyncImage(
                model = resolvedUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}