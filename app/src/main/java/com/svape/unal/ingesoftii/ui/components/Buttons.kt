package com.svape.unal.ingesoftii.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.svape.unal.ingesoftii.R
import com.svape.unal.ingesoftii.ui.theme.Motion
import com.svape.unal.ingesoftii.ui.theme.pressScale

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    Button(
        onClick = { if (!loading) onClick() },
        modifier = modifier
            .heightIn(min = 56.dp)
            .pressScale(interaction),
        enabled = enabled,
        shape = CircleShape,
        interactionSource = interaction,
        contentPadding = PaddingValues(horizontal = 24.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
    ) {
        val leading: (@Composable () -> Unit)? =
            if (leadingIcon != null) {
                { Icon(leadingIcon, null, Modifier.size(20.dp)) }
            } else null
        ButtonContent(text = text, loading = loading, leading = leading)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = { if (!loading) onClick() },
        modifier = modifier
            .heightIn(min = 56.dp)
            .pressScale(interaction),
        enabled = enabled,
        shape = CircleShape,
        interactionSource = interaction,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        contentPadding = PaddingValues(horizontal = 24.dp),
    ) {
        ButtonContent(text = text, loading = loading, leading = leading)
    }
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Continuar con Google institucional",
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    SecondaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        leading = {
            Image(
                painterResource(R.drawable.ic_google),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
    )
}

@Composable
private fun ButtonContent(
    text: String,
    loading: Boolean,
    leading: (@Composable () -> Unit)?,
) {
    AnimatedContent(
        targetState = loading,
        transitionSpec = {
            (fadeIn(tween(Motion.Medium)) + scaleIn(
                tween(
                    Motion.Medium,
                    easing = Motion.EmphasizedDecelerate
                ), initialScale = 0.8f
            )) togetherWith
                    fadeOut(tween(Motion.Short))
        },
        contentAlignment = Alignment.Center,
        label = "buttonContent",
    ) { isLoading ->
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
                color = LocalContentColor.current,
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leading != null) {
                    leading()
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize)
                )
            }
        }
    }
}