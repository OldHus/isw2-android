package com.svape.unal.ingesoftii.presentation.quiz

import com.svape.unal.ingesoftii.domain.model.QuizAnswer

fun buildQuizLeaderboard(
    correctOptionIndex: Int?,
    answers: List<QuizAnswer>
): List<QuizLeaderboardEntry> {
    return answers
        .mapNotNull { answer ->
            val isCorrect = answer.isCorrect
                ?: correctOptionIndex?.let { answer.selectedOptionIndex == it }
            isCorrect?.let { answer to it }
        }
        .sortedWith(
            compareByDescending<Pair<QuizAnswer, Boolean>> { it.first.score }
                .thenBy { it.first.responseTimeMillis }
        )
        .mapIndexed { index, (answer, isCorrect) ->
            QuizLeaderboardEntry(
                rank = index + 1,
                studentName = answer.studentName,
                selectedOptionIndex = answer.selectedOptionIndex,
                isCorrect = isCorrect,
                score = answer.score,
                responseTimeMillis = answer.responseTimeMillis
            )
        }
}
