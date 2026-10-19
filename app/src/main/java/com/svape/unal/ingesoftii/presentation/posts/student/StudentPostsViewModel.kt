package com.svape.unal.ingesoftii.presentation.posts.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.model.Post
import com.svape.unal.ingesoftii.domain.model.PostReaction
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.usecase.ObserveMyReactionUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObservePostReactionsUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObservePostsUseCase
import com.svape.unal.ingesoftii.domain.usecase.RemoveMyReactionUseCase
import com.svape.unal.ingesoftii.domain.usecase.SetMyReactionUseCase
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

class StudentPostsViewModel(
    private val observePostsUseCase: ObservePostsUseCase,
    private val observePostReactionsUseCase: ObservePostReactionsUseCase,
    private val observeMyReactionUseCase: ObserveMyReactionUseCase,
    private val setMyReactionUseCase: SetMyReactionUseCase,
    private val removeMyReactionUseCase: RemoveMyReactionUseCase,
    private val analyticsReporter: AnalyticsReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentPostsUiState>(StudentPostsUiState.Loading)
    val uiState: StateFlow<StudentPostsUiState> = _uiState.asStateFlow()

    private var courseId: String = ""

    fun start(courseId: String) {
        this.courseId = courseId
        viewModelScope.launch {
            observePostsUseCase(courseId)
                .flatMapLatest { posts ->
                    if (posts.isEmpty()) {
                        flowOf(Triple(posts, emptyMap(), emptyMap<String, PostReaction?>()))
                    } else {
                        val reactions = combine(posts.map { p ->
                            observePostReactionsUseCase(courseId, p.id).map { p.id to it }
                        }) { it.toMap() }
                        val myReactions = combine(posts.map { p ->
                            observeMyReactionUseCase(courseId, p.id).map { p.id to it }
                        }) { it.toMap() }
                        combine(reactions, myReactions) { r, m -> Triple(posts, r, m) }
                    }
                }
                .catch { e ->
                    _uiState.value =
                        StudentPostsUiState.Error(e.message ?: "No se pudo cargar el muro")
                }
                .collect { (posts, reactionsByPost, myReactionByPost) ->
                    _uiState.value = StudentPostsUiState.Content(
                        posts = posts as List<Post>,
                        reactionsByPost = (reactionsByPost as Map<String, List<PostReaction>>)
                            .mapValues { buildReactionSummaries(it.value) },
                        myReactionByPost = (myReactionByPost as Map<String, PostReaction?>)
                            .mapValues { it.value?.type }
                    )
                }
        }
    }

    fun react(postId: String, type: String) {
        val current = (uiState.value as? StudentPostsUiState.Content)?.myReactionByPost?.get(postId)
        viewModelScope.launch {
            if (current == type) {
                removeMyReactionUseCase(courseId, postId)
                    .onSuccess {
                        analyticsReporter.logEvent(
                            "post_reaction_removed",
                            mapOf("course_id" to courseId, "post_id" to postId)
                        )
                    }
            } else {
                setMyReactionUseCase(courseId, postId, type)
                    .onSuccess {
                        analyticsReporter.logEvent(
                            "post_reaction_set",
                            mapOf("course_id" to courseId, "post_id" to postId, "type" to type)
                        )
                    }
            }
        }
    }
}