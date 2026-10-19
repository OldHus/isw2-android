package com.svape.unal.ingesoftii.presentation.posts.student

import com.svape.unal.ingesoftii.domain.model.Post
import com.svape.unal.ingesoftii.presentation.posts.ReactionSummary

sealed class StudentPostsUiState {
    data object Loading : StudentPostsUiState()
    data class Error(val message: String) : StudentPostsUiState()
    data class Content(
        val posts: List<Post>,
        val reactionsByPost: Map<String, List<ReactionSummary>>,
        val myReactionByPost: Map<String, String?>
    ) : StudentPostsUiState()
}