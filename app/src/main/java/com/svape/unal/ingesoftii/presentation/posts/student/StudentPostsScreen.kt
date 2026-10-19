package com.svape.unal.ingesoftii.presentation.posts.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.svape.unal.ingesoftii.presentation.posts.PostCard
import com.svape.unal.ingesoftii.presentation.posts.ReactionBar
import com.svape.unal.ingesoftii.presentation.posts.ReactionSummary
import com.svape.unal.ingesoftii.presentation.posts.ReactionSummaryRow
import com.svape.unal.ingesoftii.presentation.posts.ReactionsSheet
import com.svape.unal.ingesoftii.ui.components.AppTopBar
import com.svape.unal.ingesoftii.ui.components.EmptyState
import com.svape.unal.ingesoftii.ui.components.LoadingList
import com.svape.unal.ingesoftii.ui.theme.StaggeredEnter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPostsScreen(
    courseId: String,
    viewModel: StudentPostsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var reactionsToShow by remember { mutableStateOf<List<ReactionSummary>?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(courseId) { viewModel.start(courseId) }

    var lastContent by remember { mutableStateOf<StudentPostsUiState.Content?>(null) }
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is StudentPostsUiState.Content -> lastContent = state
            is StudentPostsUiState.Error -> if (lastContent != null) snackbarHostState.showSnackbar(state.message)
            else -> Unit
        }
    }
    val content = (uiState as? StudentPostsUiState.Content) ?: lastContent

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Muro del curso",
                subtitle = content?.let {
                    val n = it.posts.size
                    if (n == 1) "1 publicación" else "$n publicaciones"
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        val state = uiState
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                content != null && content.posts.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Outlined.Forum,
                        title = "Todo tranquilo por aquí",
                        message = "Cuando tu docente publique anuncios, enlaces o imágenes, los verás en este muro.",
                    )
                }

                content != null -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(content.posts, key = { _, post -> post.id }) { index, post ->
                        val reactions = content.reactionsByPost[post.id].orEmpty()
                        val myReaction = content.myReactionByPost[post.id]
                        StaggeredEnter(index = index, modifier = Modifier.animateItem()) {
                            PostCard(
                                post = post,
                                authorLabel = "Tu docente",
                                footer = {
                                    if (reactions.isNotEmpty()) {
                                        ReactionSummaryRow(
                                            reactions = reactions,
                                            onClick = { reactionsToShow = reactions },
                                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 6.dp),
                                        )
                                    }
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                    )
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(start = 12.dp, end = 12.dp, bottom = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        ReactionBar(
                                            myReaction = myReaction,
                                            onReact = { type -> viewModel.react(post.id, type) },
                                        )
                                        Spacer(Modifier.weight(1f))
                                    }
                                },
                            )
                        }
                    }
                }

                state is StudentPostsUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Outlined.CloudOff,
                        title = "No pudimos cargar el muro",
                        message = state.message,
                        action = { TextButton(onClick = { viewModel.start(courseId) }) { Text("Reintentar") } },
                    )
                }

                else -> LoadingList(header = false, rows = 3, rowHeight = 220.dp)
            }
        }
    }

    reactionsToShow?.let { reactions ->
        ReactionsSheet(reactions = reactions, onDismiss = { reactionsToShow = null })
    }
}