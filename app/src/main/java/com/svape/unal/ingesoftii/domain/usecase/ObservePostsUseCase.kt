package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.Post
import com.svape.unal.ingesoftii.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow

class ObservePostsUseCase(private val repository: PostRepository) {
    operator fun invoke(courseId: String): Flow<List<Post>> = repository.observePosts(courseId)
}