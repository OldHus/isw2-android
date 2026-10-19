package com.svape.unal.ingesoftii.presentation.polls

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDateTime(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun formatRemaining(remainingMillis: Long): String {
    if (remainingMillis <= 0) return "Expirado"
    val totalSeconds = remainingMillis / 1000
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        days > 0 -> "${days}d ${hours}h restantes"
        hours > 0 -> "${hours}h ${minutes}m restantes"
        minutes > 0 -> "${minutes}m ${seconds}s restantes"
        else -> "${seconds}s restantes"
    }
}