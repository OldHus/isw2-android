package com.svape.unal.ingesoftii.domain.model

sealed class QuizError : Exception() {
    data object EmptyQuestionText : QuizError()
    data object NotEnoughOptions : QuizError()
    data object InvalidCorrectOption : QuizError()
    data object InvalidDuration : QuizError()
    data object NoActiveSession : QuizError()
    data object SessionClosed : QuizError()
    data object InvalidOption : QuizError()
    data object AlreadyAnswered : QuizError()
    data class Unknown(override val message: String) : QuizError()
}