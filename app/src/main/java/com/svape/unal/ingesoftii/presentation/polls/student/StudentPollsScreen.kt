package com.svape.unal.ingesoftii.presentation.polls.student

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Poll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.svape.unal.ingesoftii.presentation.polls.PollHeaderCard
import com.svape.unal.ingesoftii.presentation.polls.PollOptionRow
import com.svape.unal.ingesoftii.presentation.polls.VotersSheet
import com.svape.unal.ingesoftii.presentation.polls.leadingOptionIndex
import com.svape.unal.ingesoftii.presentation.polls.rememberPollRemaining
import com.svape.unal.ingesoftii.ui.components.AppTopBar
import com.svape.unal.ingesoftii.ui.components.EmptyState
import com.svape.unal.ingesoftii.ui.components.LoadingList
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import com.svape.unal.ingesoftii.ui.theme.StaggeredEnter
import kotlinx.coroutines.delay

private const val SHOW_RESULTS_BEFORE_VOTING = false

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPollsScreen(
    courseId: String,
    viewModel: StudentPollsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var votersForOption by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(courseId) { viewModel.start(courseId) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Encuestas") },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val state = uiState) {
                StudentPollsUiState.Loading -> LoadingList(
                    header = true,
                    rows = 4,
                    rowHeight = 72.dp
                )

                is StudentPollsUiState.Error -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Outlined.CloudOff,
                        title = "No pudimos cargar la encuesta",
                        message = state.message,
                        action = { TextButton(onClick = { viewModel.start(courseId) }) { Text("Reintentar") } },
                    )
                }

                StudentPollsUiState.NoPoll -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Outlined.Poll,
                        title = "No hay encuestas por ahora",
                        message = "Cuando tu docente publique una encuesta aparecerá aquí para que votes.",
                    )
                }

                is StudentPollsUiState.Result -> PollVoting(
                    state = state,
                    snackbarHostState = snackbarHostState,
                    onVote = { viewModel.vote(it) },
                    onShowVoters = { votersForOption = it },
                )
            }
        }
    }

    val option = votersForOption
    val result = uiState as? StudentPollsUiState.Result
    if (option != null && result != null) {
        VotersSheet(
            results = result.optionResults,
            initialOption = option,
            onDismiss = { votersForOption = null })
    }
}

@Composable
private fun PollVoting(
    state: StudentPollsUiState.Result,
    snackbarHostState: SnackbarHostState,
    onVote: (Int) -> Unit,
    onShowVoters: (Int) -> Unit,
) {
    val poll = state.poll
    val haptics = LocalHapticFeedback.current
    val remaining = rememberPollRemaining(poll.expiresAt, poll.isActive)
    val canVote = poll.isActive && remaining > 0

    var pendingIndex by rememberSaveable(poll.id) { mutableStateOf<Int?>(null) }
    LaunchedEffect(pendingIndex, state.myOptionIndex) {
        val pending = pendingIndex ?: return@LaunchedEffect
        if (state.myOptionIndex == pending) {
            pendingIndex = null
        } else {
            delay(6_000)
            pendingIndex = null
            snackbarHostState.showSnackbar("No se pudo registrar tu voto. Intenta de nuevo.")
        }
    }
    val myIndex = pendingIndex ?: state.myOptionIndex
    val hasVoted = myIndex != null
    val showResults = SHOW_RESULTS_BEFORE_VOTING || hasVoted || !poll.isActive

    val results = state.optionResults
    val totalVotes = results.sumOf { it.voterNames.size }
    val leader = leadingOptionIndex(results)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "header_${poll.id}") {
            PollHeaderCard(poll = poll, remainingMillis = remaining, totalVotes = totalVotes)
        }
        item(key = "hint") {
            AnimatedContent(
                targetState = when {
                    !canVote -> Hint.Closed
                    hasVoted -> Hint.Voted
                    else -> Hint.Vote
                },
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "pollHint",
            ) { hint ->
                val (icon, text, tint) = when (hint) {
                    Hint.Vote -> Triple(
                        Icons.Outlined.HowToVote,
                        "Elige una opción. Podrás cambiar tu voto mientras la encuesta esté abierta.",
                        MaterialTheme.colorScheme.primary,
                    )

                    Hint.Voted -> Triple(
                        Icons.Outlined.CheckCircle,
                        "¡Listo, tu voto quedó registrado! Toca otra opción si quieres cambiarlo.",
                        IngesoftiiTheme.extendedColors.success,
                    )

                    Hint.Closed -> Triple(
                        Icons.Outlined.Lock,
                        "La encuesta cerró. Estos son los resultados finales.",
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HintRow(icon, text, tint)
            }
        }
        itemsIndexed(results, key = { index, _ -> "${poll.id}_$index" }) { index, option ->
            StaggeredEnter(index = index) {
                PollOptionRow(
                    result = option,
                    totalVotes = totalVotes,
                    isMine = myIndex == index,
                    isLeader = index == leader,
                    closed = !poll.isActive,
                    showResults = showResults,
                    enabled = canVote && myIndex != index,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        pendingIndex = index
                        onVote(index)
                    },
                    onShowVoters = { onShowVoters(index) },
                )
            }
        }
        if (!showResults) {
            item(key = "results_hidden") {
                Text(
                    "Vota para ver cómo va la encuesta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                )
            }
        }
    }
}

private enum class Hint { Vote, Voted, Closed }

@Composable
private fun HintRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color
) {
    Row(
        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}