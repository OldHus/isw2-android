package com.svape.unal.ingesoftii.presentation.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Poll
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ViewKanban
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.svape.unal.ingesoftii.domain.model.UserRole
import com.svape.unal.ingesoftii.presentation.account.AccountViewModel
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme

@Composable
fun StudentHomeScreen(
    accountViewModel: AccountViewModel,
    onSignedOut: () -> Unit,
    onViewSyllabus: () -> Unit,
    onViewGrades: () -> Unit,
    onViewMyTeam: () -> Unit,
    onMarkAttendance: () -> Unit,
    onViewPolls: () -> Unit,
    onViewQuiz: () -> Unit,
    onViewPosts: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors

    HomeScaffold(
        accountViewModel = accountViewModel,
        role = UserRole.STUDENT,
        roleLabel = "Estudiante · Ingeniería de Software II",
        hero = HomeHero(
            title = "Mis notas",
            description = "Consulta tus calificaciones y cómo vas en el curso.",
            icon = Icons.Outlined.School,
            actionLabel = "Ver mis notas",
            onClick = onViewGrades,
        ),
        quickActions = listOf(
            HomeAction(
                "attendance",
                "Asistencia",
                "Marcar hoy",
                Icons.Outlined.QrCode2,
                onMarkAttendance,
                ext.success
            ),
            HomeAction(
                "quiz",
                "Quiz",
                "Responder",
                Icons.Outlined.EmojiEvents,
                onViewQuiz,
                colors.tertiary
            ),
            HomeAction(
                "posts",
                "Muro",
                "Anuncios",
                Icons.Outlined.Forum,
                onViewPosts,
                colors.primary
            ),
            HomeAction(
                "team",
                "Mi equipo",
                "Tablero",
                Icons.Outlined.ViewKanban,
                onViewMyTeam,
                colors.secondary
            ),
        ),
        moreActions = listOf(
            HomeAction(
                "polls",
                "Encuestas",
                "Vota en la encuesta activa",
                Icons.Outlined.Poll,
                onViewPolls
            ),
            HomeAction(
                "syllabus",
                "Syllabus",
                "Programa del curso",
                Icons.Outlined.Description,
                onViewSyllabus
            ),
        ),
        onSignedOut = onSignedOut,
    )
}