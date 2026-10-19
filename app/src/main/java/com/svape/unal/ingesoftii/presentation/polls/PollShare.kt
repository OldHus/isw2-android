package com.svape.unal.ingesoftii.presentation.polls

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.svape.unal.ingesoftii.domain.model.Poll
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

fun buildPollReport(poll: Poll, results: List<PollOptionResult>): String {
    val total = results.sumOf { it.voterNames.size }
    val leader = leadingOptionIndex(results)
    val generated =
        SimpleDateFormat("d 'de' MMMM 'de' yyyy, h:mm a", Locale("es", "CO")).format(Date())

    return buildString {
        appendLine("📊 ENCUESTA")
        appendLine(poll.question)
        appendLine()
        appendLine(
            "Estado: " + if (poll.isActive) "Activa · cierra ${formatPollDeadline(poll.expiresAt)}"
            else "Cerrada · cerró ${formatPollDeadline(poll.expiresAt)}",
        )
        appendLine("Total de votos: $total")
        appendLine()
        appendLine("RESULTADOS")
        appendLine("──────────────────────")
        results.forEachIndexed { index, result ->
            val votes = result.voterNames.size
            val percent = if (total == 0) 0 else (votes * 100f / total).roundToInt()
            val trophy = if (index == leader && !poll.isActive) " 🏆" else ""
            appendLine("${index + 1}. ${result.optionText} — ${if (votes == 1) "1 voto" else "$votes votos"} ($percent %)$trophy")
            if (result.voterNames.isEmpty()) {
                appendLine("   (sin votos)")
            } else {
                result.voterNames.sortedBy { it.lowercase() }
                    .forEach { appendLine("   • ${it.ifBlank { "Estudiante" }}") }
            }
            appendLine()
        }
        appendLine("──────────────────────")
        append("Generado desde Ingesoft II el $generated")
    }
}

fun sharePollResults(context: Context, poll: Poll, results: List<PollOptionResult>) {
    val report = buildPollReport(poll, results)

    val dir = File(context.cacheDir, "shared").apply { mkdirs() }
    val file = File(dir, SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.US).format(Date()) + ".txt")
    file.writeText(report, Charsets.UTF_8)

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Resultados de la encuesta: ${poll.question}")
        putExtra(Intent.EXTRA_TEXT, report)
        clipData = ClipData.newUri(context.contentResolver, file.name, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, "Compartir resultados"))
}