package com.svape.unal.ingesoftii.presentation.posts.teacher

import com.svape.unal.ingesoftii.domain.model.Post
import com.svape.unal.ingesoftii.presentation.posts.ReactionSummary

sealed class TeacherPostsUiState {
    data object Loading : TeacherPostsUiState()
    data class Error(val message: String) : TeacherPostsUiState()
    data class Content(
        val posts: List<Post>,
        val reactionsByPost: Map<String, List<ReactionSummary>>
    ) : TeacherPostsUiState()
}