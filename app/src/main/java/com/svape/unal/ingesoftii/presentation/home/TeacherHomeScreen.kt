package com.svape.unal.ingesoftii.presentation.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Poll
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.svape.unal.ingesoftii.BuildConfig
import com.svape.unal.ingesoftii.domain.model.UserRole
import com.svape.unal.ingesoftii.presentation.account.AccountViewModel
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme

@Composable
fun TeacherHomeScreen(
    accountViewModel: AccountViewModel,
    onSignedOut: () -> Unit,
    onViewSyllabus: () -> Unit,
    onViewGrades: () -> Unit,
    onViewTeams: () -> Unit,
    onManageAttendance: () -> Unit,
    onManagePolls: () -> Unit,
    onManageQuiz: () -> Unit,
    onManagePosts: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors

    val moreActions = buildList {
        add(
            HomeAction(
                "polls",
                "Encuestas",
                "Lanzar y ver resultados",
                Icons.Outlined.Poll,
                onManagePolls
            )
        )
        add(
            HomeAction(
                "syllabus",
                "Syllabus",
                "Programa del curso",
                Icons.Outlined.Description,
                onViewSyllabus
            )
        )
        if (BuildConfig.DEBUG) {
            add(
                HomeAction(
                    "crash",
                    "Probar Crashlytics",
                    "Solo visible en debug",
                    Icons.Outlined.BugReport,
                    { throw RuntimeException("Test Crash") },
                ),
            )
        }
    }

    HomeScaffold(
        accountViewModel = accountViewModel,
        role = UserRole.TEACHER,
        roleLabel = "Docente · Ingeniería de Software II",
        hero = HomeHero(
            title = "Calificaciones",
            description = "Registra notas, revisa la matriz del curso y el avance de cada estudiante.",
            icon = Icons.Outlined.School,
            actionLabel = "Gestionar notas",
            onClick = onViewGrades,
        ),
        quickActions = listOf(
            HomeAction(
                "attendance",
                "Asistencia",
                "Abrir sesión QR",
                Icons.Outlined.QrCode2,
                onManageAttendance,
                ext.success
            ),
            HomeAction(
                "quiz",
                "Quiz",
                "Lanzar pregunta",
                Icons.Outlined.EmojiEvents,
                onManageQuiz,
                colors.tertiary
            ),
            HomeAction(
                "posts",
                "Muro",
                "Publicar anuncio",
                Icons.Outlined.Forum,
                onManagePosts,
                colors.primary
            ),
            HomeAction(
                "teams",
                "Equipos",
                "Proyecto de aula",
                Icons.Outlined.Groups,
                onViewTeams,
                colors.secondary
            ),
        ),
        moreActions = moreActions,
        onSignedOut = onSignedOut,
    )
}