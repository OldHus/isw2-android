package com.svape.unal.ingesoftii.presentation.posts.teacher

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.model.Post
import com.svape.unal.ingesoftii.domain.model.PostReaction
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.usecase.CreatePostUseCase
import com.svape.unal.ingesoftii.domain.usecase.DeletePostUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObservePostReactionsUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObservePostsUseCase
import com.svape.unal.ingesoftii.domain.usecase.UpdatePostUseCase
import com.svape.unal.ingesoftii.presentation.posts.buildReactionSummaries
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TeacherPostsViewModel(
    private val observePostsUseCase: ObservePostsUseCase,
    private val observePostReactionsUseCase: ObservePostReactionsUseCase,
    private val createPostUseCase: CreatePostUseCase,
    private val updatePostUseCase: UpdatePostUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val analyticsReporter: AnalyticsReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeacherPostsUiState>(TeacherPostsUiState.Loading)
    val uiState: StateFlow<TeacherPostsUiState> = _uiState.asStateFlow()

    private var courseId: String = ""

    fun start(courseId: String) {
        this.courseId = courseId
        viewModelScope.launch {
            observePostsUseCase(courseId)
                .flatMapLatest { posts -> reactionsFor(posts).map { posts to it } }
                .catch { e ->
                    _uiState.value =
                        TeacherPostsUiState.Error(e.message ?: "No se pudo cargar el muro")
                }
                .collect { (posts, reactionsByPost) ->
                    _uiState.value = TeacherPostsUiState.Content(
                        posts = posts,
                        reactionsByPost = reactionsByPost.mapValues { buildReactionSummaries(it.value) }
                    )
                }
        }
    }

    private fun reactionsFor(posts: List<Post>) =
        if (posts.isEmpty()) {
            flowOf(emptyMap<String, List<PostReaction>>())
        } else {
            combine(posts.map { post ->
                observePostReactionsUseCase(courseId, post.id).map { post.id to it }
            }) { pairs -> pairs.toMap() }
        }

    fun createPost(text: String, imageUri: Uri?) {
        viewModelScope.launch {
            createPostUseCase(courseId, text, imageUri)
                .onSuccess {
                    analyticsReporter.logEvent("post_created", mapOf("course_id" to courseId))
                }
                .onFailure { error ->
                    _uiState.value =
                        TeacherPostsUiState.Error(error.message ?: "No se pudo publicar")
                }
        }
    }

    fun updatePost(postId: String, text: String, newImageUri: Uri?, removeImage: Boolean) {
        viewModelScope.launch {
            updatePostUseCase(courseId, postId, text, newImageUri, removeImage)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "post_updated",
                        mapOf("course_id" to courseId, "post_id" to postId)
                    )
                }
                .onFailure { error ->
                    _uiState.value = TeacherPostsUiState.Error(
                        error.message ?: "No se pudo editar la publicación"
                    )
                }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            deletePostUseCase(courseId, postId)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "post_deleted",
                        mapOf("course_id" to courseId, "post_id" to postId)
                    )
                }
                .onFailure { error ->
                    _uiState.value = TeacherPostsUiState.Error(
                        error.message ?: "No se pudo eliminar la publicación"
                    )
                }
        }
    }
}