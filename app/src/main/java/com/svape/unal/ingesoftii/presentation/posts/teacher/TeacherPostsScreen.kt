package com.svape.unal.ingesoftii.presentation.posts.teacher

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.svape.unal.ingesoftii.domain.model.Post
import com.svape.unal.ingesoftii.presentation.posts.PostCard
import com.svape.unal.ingesoftii.presentation.posts.ReactionSummary
import com.svape.unal.ingesoftii.presentation.posts.ReactionSummaryRow
import com.svape.unal.ingesoftii.presentation.posts.ReactionsSheet
import com.svape.unal.ingesoftii.ui.components.AppTopBar
import com.svape.unal.ingesoftii.ui.components.EmptyState
import com.svape.unal.ingesoftii.ui.components.LoadingList
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import com.svape.unal.ingesoftii.ui.theme.StaggeredEnter
import com.svape.unal.ingesoftii.ui.theme.pressScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherPostsScreen(
    courseId: String,
    viewModel: TeacherPostsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showComposer by remember { mutableStateOf(false) }
    var editingPost by remember { mutableStateOf<Post?>(null) }
    var postToDelete by remember { mutableStateOf<Post?>(null) }
    var reactionsToShow by remember { mutableStateOf<List<ReactionSummary>?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    LaunchedEffect(courseId) { viewModel.start(courseId) }

    var lastContent by remember { mutableStateOf<TeacherPostsUiState.Content?>(null) }
    var pendingMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is TeacherPostsUiState.Content -> lastContent = state
            is TeacherPostsUiState.Error -> if (lastContent != null) {
                pendingMessage = null
                snackbarHostState.showSnackbar(state.message)
            }
            else -> Unit
        }
    }
    val content = (uiState as? TeacherPostsUiState.Content) ?: lastContent

    LaunchedEffect(content?.posts) {
        pendingMessage?.let { message ->
            pendingMessage = null
            snackbarHostState.showSnackbar(message)
        }
    }
    LaunchedEffect(pendingMessage) {
        if (pendingMessage != null) {
            delay(30_000)
            pendingMessage = null
        }
    }

    fun openComposer(post: Post?) {
        editingPost = post
        showComposer = true
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                AppTopBar(
                    title = "Muro del curso",
                    subtitle = content?.let {
                        val n = it.posts.size
                        if (n == 1) "1 publicación" else "$n publicaciones"
                    },
                    scrollBehavior = scrollBehavior,
                )
                AnimatedVisibility(
                    visible = pendingMessage != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            }
        },
        floatingActionButton = {
            if (content != null && content.posts.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    text = { Text("Publicar") },
                    icon = { Icon(Icons.Outlined.EditNote, contentDescription = null) },
                    expanded = fabExpanded,
                    onClick = { openComposer(null) },
                )
            }
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
                        icon = Icons.Outlined.Campaign,
                        title = "Tu muro está vacío",
                        message = "Publica anuncios, enlaces o imágenes. Tus estudiantes los verán al instante y podrán reaccionar.",
                        action = {
                            Button(onClick = { openComposer(null) }) {
                                Icon(Icons.Outlined.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Crear publicación")
                            }
                        },
                    )
                }

                content != null -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 104.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "composer_prompt") {
                        ComposerPrompt(onClick = { openComposer(null) })
                    }
                    itemsIndexed(content.posts, key = { _, post -> post.id }) { index, post ->
                        val reactions = content.reactionsByPost[post.id].orEmpty()
                        StaggeredEnter(index = index, modifier = Modifier.animateItem()) {
                            PostCard(
                                post = post,
                                authorLabel = "Tú",
                                headerAction = {
                                    PostMenu(
                                        onEdit = { openComposer(post) },
                                        onDelete = { postToDelete = post },
                                    )
                                },
                                footer = {
                                    Box(Modifier.padding(start = 8.dp, end = 8.dp, top = 6.dp)) {
                                        if (reactions.isEmpty()) {
                                            Text(
                                                "Aún sin reacciones",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                            )
                                        } else {
                                            ReactionSummaryRow(reactions, onClick = { reactionsToShow = reactions })
                                        }
                                    }
                                },
                            )
                        }
                    }
                }

                state is TeacherPostsUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

    if (showComposer) {
        PostComposerSheet(
            initial = editingPost,
            onDismiss = { showComposer = false },
            onConfirm = { text, imageUri, removeImage ->
                val current = editingPost
                if (current == null) {
                    viewModel.createPost(text, imageUri)
                    pendingMessage = "Publicado en el muro"
                } else {
                    viewModel.updatePost(current.id, text, imageUri, removeImage)
                    pendingMessage = "Cambios guardados"
                }
                showComposer = false
                scope.launch { listState.animateScrollToItem(0) }
            },
        )
    }

    postToDelete?.let { post ->
        val colors = MaterialTheme.colorScheme
        AlertDialog(
            onDismissRequest = { postToDelete = null },
            icon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = colors.error) },
            title = { Text("¿Eliminar publicación?") },
            text = { Text("Se borrará del muro junto con sus reacciones. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePost(post.id)
                        postToDelete = null
                        scope.launch { snackbarHostState.showSnackbar("Publicación eliminada") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.onError),
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { postToDelete = null }) { Text("Cancelar") } },
        )
    }

    reactionsToShow?.let { reactions ->
        ReactionsSheet(reactions = reactions, onDismiss = { reactionsToShow = null })
    }
}

@Composable
private fun ComposerPrompt(onClick: () -> Unit) {
    val ext = IngesoftiiTheme.extendedColors
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        shape = MaterialTheme.shapes.large,
        color = ext.card,
        border = BorderStroke(1.dp, ext.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction),
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Comparte algo con tu clase…",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Outlined.Image,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
    }
}

@Composable
private fun PostMenu(onEdit: () -> Unit, onDelete: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "Opciones de la publicación", tint = colors.onSurfaceVariant)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text("Editar") },
                leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                onClick = {
                    open = false
                    onEdit()
                },
            )
            DropdownMenuItem(
                text = { Text("Eliminar", color = colors.error) },
                leadingIcon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = colors.error) },
                onClick = {
                    open = false
                    onDelete()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostComposerSheet(
    initial: Post?,
    onDismiss: () -> Unit,
    onConfirm: (text: String, imageUri: Uri?, removeImage: Boolean) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var text by rememberSaveable { mutableStateOf(initial?.text.orEmpty()) }
    var newImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var removeExistingImage by rememberSaveable { mutableStateOf(false) }
    var showDiscard by remember { mutableStateOf(false) }

    val existingImage = initial?.imageUrl?.takeIf { it.isNotBlank() && !removeExistingImage }
    val previewModel: Any? = newImageUri ?: existingImage
    val dirty = text != initial?.text.orEmpty() || newImageUri != null || removeExistingImage
    val canPublish = (text.isNotBlank() || previewModel != null) && (initial == null || dirty)

    val currentDirty by rememberUpdatedState(dirty)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { value ->
            if (value == SheetValue.Hidden && currentDirty) {
                showDiscard = true
                false
            } else {
                true
            }
        },
    )

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            newImageUri = uri
            removeExistingImage = false
        }
    }
    val pickPhoto = {
        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceContainerLow,
    ) {
        Column(Modifier.padding(bottom = 16.dp)) {
            Row(
                Modifier.padding(start = 20.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (initial == null) "Nueva publicación" else "Editar publicación",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = { onConfirm(text.trim(), newImageUri, removeExistingImage) },
                    enabled = canPublish,
                ) { Text(if (initial == null) "Publicar" else "Guardar") }
            }

            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("¿Qué quieres compartir con tu clase?") },
                textStyle = MaterialTheme.typography.bodyLarge,
                minLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp, max = 320.dp)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
            )

            if (previewModel != null) {
                Box(
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(colors.surfaceContainerHigh),
                ) {
                    AsyncImage(
                        model = previewModel,
                        contentDescription = "Imagen adjunta",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp, max = 240.dp),
                    )
                    FilledIconButton(
                        onClick = {
                            if (newImageUri != null) newImageUri = null else removeExistingImage = true
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.55f),
                            contentColor = Color.White,
                        ),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(36.dp),
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Quitar imagen", modifier = Modifier.size(20.dp))
                    }
                }
            }

            Row(
                Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AssistChip(
                    onClick = pickPhoto,
                    label = { Text(if (previewModel != null) "Cambiar foto" else "Agregar foto") },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize),
                        )
                    },
                )
                Spacer(Modifier.width(12.dp))
                Icon(
                    Icons.Outlined.Link,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Los enlaces se vuelven tocables",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }

    if (showDiscard) {
        AlertDialog(
            onDismissRequest = { showDiscard = false },
            title = { Text(if (initial == null) "¿Descartar publicación?" else "¿Descartar cambios?") },
            text = { Text("Perderás lo que escribiste.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscard = false
                    onDismiss()
                }) { Text("Descartar", color = colors.error) }
            },
            dismissButton = { TextButton(onClick = { showDiscard = false }) { Text("Seguir editando") } },
        )
    }
}