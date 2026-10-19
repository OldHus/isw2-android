package com.svape.unal.ingesoftii.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.svape.unal.ingesoftii.di.AppContainer
import com.svape.unal.ingesoftii.domain.model.UserRole
import com.svape.unal.ingesoftii.presentation.account.AccountViewModel
import com.svape.unal.ingesoftii.presentation.attendance.student.StudentAttendanceScreen
import com.svape.unal.ingesoftii.presentation.attendance.student.StudentAttendanceViewModel
import com.svape.unal.ingesoftii.presentation.attendance.teacher.TeacherAttendanceScreen
import com.svape.unal.ingesoftii.presentation.attendance.teacher.TeacherAttendanceViewModel
import com.svape.unal.ingesoftii.presentation.auth.AuthViewModel
import com.svape.unal.ingesoftii.presentation.auth.LoginScreen
import com.svape.unal.ingesoftii.presentation.course.CourseCodeScreen
import com.svape.unal.ingesoftii.presentation.course.CourseCodeViewModel
import com.svape.unal.ingesoftii.presentation.grades.student.StudentGradesScreen
import com.svape.unal.ingesoftii.presentation.grades.student.StudentGradesViewModel
import com.svape.unal.ingesoftii.presentation.grades.teacher.GradesMatrixScreen
import com.svape.unal.ingesoftii.presentation.grades.teacher.GradesMatrixViewModel
import com.svape.unal.ingesoftii.presentation.grades.teacher.StudentGradeEditScreen
import com.svape.unal.ingesoftii.presentation.grades.teacher.StudentGradeEditViewModel
import com.svape.unal.ingesoftii.presentation.grades.teacher.TeacherGradesScreen
import com.svape.unal.ingesoftii.presentation.grades.teacher.TeacherGradesViewModel
import com.svape.unal.ingesoftii.presentation.home.StudentHomeScreen
import com.svape.unal.ingesoftii.presentation.home.TeacherHomeScreen
import com.svape.unal.ingesoftii.presentation.polls.student.StudentPollsScreen
import com.svape.unal.ingesoftii.presentation.polls.student.StudentPollsViewModel
import com.svape.unal.ingesoftii.presentation.polls.teacher.TeacherPollsScreen
import com.svape.unal.ingesoftii.presentation.polls.teacher.TeacherPollsViewModel
import com.svape.unal.ingesoftii.presentation.posts.student.StudentPostsScreen
import com.svape.unal.ingesoftii.presentation.posts.student.StudentPostsViewModel
import com.svape.unal.ingesoftii.presentation.posts.teacher.TeacherPostsScreen
import com.svape.unal.ingesoftii.presentation.posts.teacher.TeacherPostsViewModel
import com.svape.unal.ingesoftii.presentation.quiz.student.StudentQuizScreen
import com.svape.unal.ingesoftii.presentation.quiz.student.StudentQuizViewModel
import com.svape.unal.ingesoftii.presentation.quiz.teacher.TeacherQuizScreen
import com.svape.unal.ingesoftii.presentation.quiz.teacher.TeacherQuizViewModel
import com.svape.unal.ingesoftii.presentation.session.SessionUiState
import com.svape.unal.ingesoftii.presentation.session.SessionViewModel
import com.svape.unal.ingesoftii.presentation.session.SplashScreen
import com.svape.unal.ingesoftii.presentation.syllabus.SyllabusScreen
import com.svape.unal.ingesoftii.presentation.syllabus.SyllabusViewModel
import com.svape.unal.ingesoftii.presentation.teams.student.StudentTeamBoardScreen
import com.svape.unal.ingesoftii.presentation.teams.student.StudentTeamBoardViewModel
import com.svape.unal.ingesoftii.presentation.teams.teacher.TeacherTeamBoardScreen
import com.svape.unal.ingesoftii.presentation.teams.teacher.TeacherTeamBoardViewModel
import com.svape.unal.ingesoftii.presentation.teams.teacher.TeacherTeamsScreen
import com.svape.unal.ingesoftii.presentation.teams.teacher.TeacherTeamsViewModel
import com.svape.unal.ingesoftii.ui.theme.Motion

@Composable
fun IngesoftNavHost(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    val pendingDeepLink by appContainer.pendingDeepLink.collectAsState()
    val currentSession by appContainer.currentSessionHolder.current.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    LaunchedEffect(pendingDeepLink, currentSession, currentRoute) {
        val target = pendingDeepLink ?: return@LaunchedEffect
        val session = currentSession ?: return@LaunchedEffect
        if (currentRoute == null || currentRoute == Routes.Splash.route) return@LaunchedEffect

        val isTeacher = session.role == UserRole.TEACHER
        val courseId = session.courseId
        val route = when (target) {
            DeepLinkTarget.POST ->
                if (isTeacher) Routes.TeacherPosts.createRoute(courseId)
                else Routes.StudentPosts.createRoute(courseId)

            DeepLinkTarget.POLL ->
                if (isTeacher) Routes.TeacherPolls.createRoute(courseId)
                else Routes.StudentPolls.createRoute(courseId)

            DeepLinkTarget.QUIZ ->
                if (isTeacher) Routes.TeacherQuiz.createRoute(courseId)
                else Routes.StudentQuiz.createRoute(courseId)
        }
        navController.navigate(route)
        appContainer.pendingDeepLink.value = null
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route,
        enterTransition = {
            slideIntoContainer(
                SlideDirection.Start,
                tween(400, easing = Motion.EmphasizedDecelerate)
            ) { it / 8 } + fadeIn(tween(300, delayMillis = 60))
        },
        exitTransition = {
            slideOutOfContainer(
                SlideDirection.Start,
                tween(300, easing = Motion.EmphasizedAccelerate)
            ) { it / 8 } + fadeOut(tween(150))
        },
        popEnterTransition = {
            slideIntoContainer(
                SlideDirection.End,
                tween(400, easing = Motion.EmphasizedDecelerate)
            ) { it / 8 } + fadeIn(tween(300, delayMillis = 60))
        },
        popExitTransition = {
            slideOutOfContainer(
                SlideDirection.End,
                tween(300, easing = Motion.EmphasizedAccelerate)
            ) { it / 8 } + fadeOut(tween(150))
        },
    ) {

        composable(Routes.Splash.route) {
            val sessionViewModel: SessionViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        SessionViewModel(
                            appContainer.getCurrentSessionUseCase,
                            appContainer.subscribeToCourseNotificationsUseCase,
                            appContainer.currentSessionHolder,
                            appContainer.crashReporter,
                            appContainer.analyticsReporter
                        )
                    }
                }
            )
            val uiState by sessionViewModel.uiState.collectAsState()

            SplashScreen()

            LaunchedEffect(uiState) {
                when (val state = uiState) {
                    SessionUiState.Loading -> Unit
                    SessionUiState.NotAuthenticated ->
                        navController.navigateClearingBackStack(Routes.Login.route)

                    is SessionUiState.NeedsRegistration ->
                        navController.navigateClearingBackStack(Routes.CourseCode.route)

                    is SessionUiState.Authenticated ->
                        navController.navigateClearingBackStack(
                            homeRouteForRole(state.role, state.courseId)
                        )
                }
            }
        }

        composable(
            Routes.Login.route,
            enterTransition = { fadeIn(tween(500)) },
        ) {
            TrackScreenView(appContainer, "login")
            val authViewModel: AuthViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        AuthViewModel(
                            appContainer.signInWithGoogleUseCase,
                            appContainer.signInWithEmailPasswordUseCase,
                            appContainer.subscribeToCourseNotificationsUseCase,
                            appContainer.currentSessionHolder,
                            appContainer.crashReporter,
                            appContainer.analyticsReporter
                        )
                    }
                }
            )
            LoginScreen(
                viewModel = authViewModel,
                onExistingUser = { role, courseId ->
                    navController.navigateClearingBackStack(homeRouteForRole(role, courseId))
                },
                onNewUser = {
                    navController.navigate(Routes.CourseCode.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CourseCode.route) {
            TrackScreenView(appContainer, "course_code")
            val courseCodeViewModel: CourseCodeViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        CourseCodeViewModel(
                            appContainer.redeemRegistrationCodeUseCase,
                            appContainer.subscribeToCourseNotificationsUseCase,
                            appContainer.currentSessionHolder,
                            appContainer.analyticsReporter,
                            appContainer.crashReporter
                        )
                    }
                }
            )
            CourseCodeScreen(
                viewModel = courseCodeViewModel,
                onRegistrationSuccess = { role, courseId ->
                    navController.navigateClearingBackStack(homeRouteForRole(role, courseId))
                }
            )
        }

        composable(
            route = Routes.TeacherHome.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            TrackScreenView(appContainer, "teacher_home")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val accountViewModel: AccountViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        AccountViewModel(
                            appContainer.signOutUseCase,
                            appContainer.unsubscribeFromCourseNotificationsUseCase,
                            appContainer.currentSessionHolder
                        )
                    }
                }
            )

            TeacherHomeScreen(
                accountViewModel = accountViewModel,
                onSignedOut = { navController.navigateClearingBackStack(Routes.Login.route) },
                onViewSyllabus = { navController.navigate(Routes.Syllabus.createRoute(courseId)) },
                onViewGrades = { navController.navigate(Routes.TeacherGrades.createRoute(courseId)) },
                onViewTeams = { navController.navigate(Routes.TeacherTeams.createRoute(courseId)) },
                onManageAttendance = {
                    navController.navigate(Routes.TeacherAttendance.createRoute(courseId))
                },
                onManagePolls = { navController.navigate(Routes.TeacherPolls.createRoute(courseId)) },
                onManageQuiz = { navController.navigate(Routes.TeacherQuiz.createRoute(courseId)) },
                onManagePosts = { navController.navigate(Routes.TeacherPosts.createRoute(courseId)) }
            )
        }

        composable(
            route = Routes.StudentHome.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            TrackScreenView(appContainer, "student_home")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val accountViewModel: AccountViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        AccountViewModel(
                            appContainer.signOutUseCase,
                            appContainer.unsubscribeFromCourseNotificationsUseCase,
                            appContainer.currentSessionHolder
                        )
                    }
                }
            )

            StudentHomeScreen(
                accountViewModel = accountViewModel,
                onSignedOut = { navController.navigateClearingBackStack(Routes.Login.route) },
                onViewSyllabus = { navController.navigate(Routes.Syllabus.createRoute(courseId)) },
                onViewGrades = { navController.navigate(Routes.StudentGrades.createRoute(courseId)) },
                onViewMyTeam = { navController.navigate(Routes.StudentTeamBoard.createRoute(courseId)) },
                onMarkAttendance = {
                    navController.navigate(Routes.StudentAttendance.createRoute(courseId))
                },
                onViewPolls = { navController.navigate(Routes.StudentPolls.createRoute(courseId)) },
                onViewQuiz = { navController.navigate(Routes.StudentQuiz.createRoute(courseId)) },
                onViewPosts = { navController.navigate(Routes.StudentPosts.createRoute(courseId)) }
            )
        }

        composable(
            route = Routes.Syllabus.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            TrackScreenView(appContainer, "syllabus")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val syllabusViewModel: SyllabusViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        SyllabusViewModel(
                            appContainer.getSyllabusFileUseCase,
                            appContainer.analyticsReporter
                        )
                    }
                }
            )
            SyllabusScreen(courseId = courseId, viewModel = syllabusViewModel)
        }

        composable(
            route = Routes.TeacherGrades.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            TrackScreenView(appContainer, "teacher_grades")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val teacherGradesViewModel: TeacherGradesViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        TeacherGradesViewModel(
                            appContainer.getGradeItemsUseCase,
                            appContainer.getCourseStudentsUseCase,
                            appContainer.addGradeItemUseCase,
                            appContainer.renameGradeItemUseCase,
                            appContainer.countStudentsGradedUseCase,
                            appContainer.deleteGradeItemUseCase,
                            appContainer.analyticsReporter
                        )
                    }
                }
            )
            TeacherGradesScreen(
                courseId = courseId,
                viewModel = teacherGradesViewModel,
                onGradeStudent = { studentUid ->
                    navController.navigate(
                        Routes.StudentGradeEdit.createRoute(
                            courseId,
                            studentUid
                        )
                    )
                },
                onViewMatrix = {
                    navController.navigate(Routes.TeacherGradesMatrix.createRoute(courseId))
                }
            )
        }

        composable(
            route = Routes.StudentGradeEdit.route,
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("studentUid") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            TrackScreenView(appContainer, "student_grade_edit")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val studentUid = backStackEntry.arguments?.getString("studentUid").orEmpty()
            val editViewModel: StudentGradeEditViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        StudentGradeEditViewModel(
                            appContainer.getGradeItemsUseCase,
                            appContainer.getStudentGradesUseCase,
                            appContainer.setStudentGradeUseCase,
                            appContainer.deleteStudentGradeUseCase,
                            appContainer.getCourseStudentsUseCase,
                            appContainer.analyticsReporter
                        )
                    }
                }
            )
            StudentGradeEditScreen(
                courseId = courseId,
                studentUid = studentUid,
                viewModel = editViewModel
            )
        }

        composable(
            route = Routes.TeacherGradesMatrix.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            TrackScreenView(appContainer, "teacher_grades_matrix")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val matrixViewModel: GradesMatrixViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        GradesMatrixViewModel(
                            appContainer.getGradeItemsUseCase,
                            appContainer.getCourseStudentsUseCase,
                            appContainer.getCourseGradesOverviewUseCase
                        )
                    }
                }
            )
            GradesMatrixScreen(
                courseId = courseId,
                viewModel = matrixViewModel,
                onSelectStudent = { studentUid ->
                    navController.navigate(
                        Routes.StudentGradeEdit.createRoute(
                            courseId,
                            studentUid
                        )
                    )
                }
            )
        }

        composable(
            route = Routes.StudentGrades.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            TrackScreenView(appContainer, "student_grades")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val studentGradesViewModel: StudentGradesViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        StudentGradesViewModel(
                            appContainer.getGradeItemsUseCase,
                            appContainer.getMyGradesUseCase
                        )
                    }
                }
            )
            StudentGradesScreen(courseId = courseId, viewModel = studentGradesViewModel)
        }

        composable(
            route = Routes.TeacherTeams.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            TrackScreenView(appContainer, "teacher_teams")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val teacherTeamsViewModel: TeacherTeamsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        TeacherTeamsViewModel(
                            appContainer.getCourseTeamsUseCase,
                            appContainer.createTeamUseCase,
                            appContainer.renameTeamUseCase,
                            appContainer.deleteTeamUseCase,
                            appContainer.analyticsReporter
                        )
                    }
                }
            )
            TeacherTeamsScreen(
                courseId = courseId,
                viewModel = teacherTeamsViewModel,
                onSelectTeam = { teamId ->
                    navController.navigate(Routes.TeacherTeamBoard.createRoute(courseId, teamId))
                }
            )
        }

        composable(
            route = Routes.TeacherTeamBoard.route,
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("teamId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            TrackScreenView(appContainer, "teacher_team_board")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val teamId = backStackEntry.arguments?.getString("teamId").orEmpty()
            val teacherTeamBoardViewModel: TeacherTeamBoardViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        TeacherTeamBoardViewModel(
                            appContainer.getTeamByIdUseCase,
                            appContainer.getTeamTasksUseCase,
                            appContainer.getCourseStudentsUseCase,
                            appContainer.addStudentToTeamUseCase,
                            appContainer.removeStudentFromTeamUseCase,
                            appContainer.createTaskUseCase,
                            appContainer.moveTaskUseCase,
                            appContainer.deleteTaskUseCase,
                            appContainer.analyticsReporter
                        )
                    }
                }
            )
            TeacherTeamBoardScreen(
                courseId = courseId,
                teamId = teamId,
                viewModel = teacherTeamBoardViewModel
            )
        }

        composable(
            route = Routes.StudentTeamBoard.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            TrackScreenView(appContainer, "student_team_board")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val studentTeamBoardViewModel: StudentTeamBoardViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        StudentTeamBoardViewModel(
                            appContainer.getMyTeamUseCase,
                            appContainer.getTeamTasksUseCase,
                            appContainer.createTaskUseCase,
                            appContainer.moveTaskUseCase,
                            appContainer.analyticsReporter
                        )
                    }
                }
            )
            StudentTeamBoardScreen(courseId = courseId, viewModel = studentTeamBoardViewModel)
        }

        composable(
            route = Routes.TeacherAttendance.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            TrackScreenView(appContainer, "teacher_attendance")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val teacherAttendanceViewModel: TeacherAttendanceViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        TeacherAttendanceViewModel(
                            appContainer.observeActiveAttendanceSessionUseCase,
                            appContainer.observeAttendanceRegistrationsUseCase,
                            appContainer.startAttendanceSessionUseCase,
                            appContainer.closeAttendanceSessionUseCase,
                            appContainer.getCourseStudentsUseCase,
                            appContainer.analyticsReporter
                        )
                    }
                }
            )
            TeacherAttendanceScreen(courseId = courseId, viewModel = teacherAttendanceViewModel)
        }

        composable(
            route = Routes.StudentAttendance.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            TrackScreenView(appContainer, "student_attendance")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val studentAttendanceViewModel: StudentAttendanceViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        StudentAttendanceViewModel(
                            appContainer.observeActiveAttendanceSessionUseCase,
                            appContainer.hasSubmittedAttendanceUseCase,
                            appContainer.submitAttendanceUseCase,
                            appContainer.analyticsReporter
                        )
                    }
                }
            )
            StudentAttendanceScreen(courseId = courseId, viewModel = studentAttendanceViewModel)
        }

        composable(
            route = Routes.TeacherPolls.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            TrackScreenView(appContainer, "teacher_polls")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val teacherPollsViewModel: TeacherPollsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        TeacherPollsViewModel(
                            appContainer.observeLatestPollUseCase,
                            appContainer.observePollVotesUseCase,
                            appContainer.startPollUseCase,
                            appContainer.closePollUseCase,
                            appContainer.analyticsReporter
                        )
                    }
                }
            )
            TeacherPollsScreen(courseId = courseId, viewModel = teacherPollsViewModel)
        }

        composable(
            route = Routes.StudentPolls.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            TrackScreenView(appContainer, "student_polls")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val studentPollsViewModel: StudentPollsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        StudentPollsViewModel(
                            appContainer.observeLatestPollUseCase,
                            appContainer.observePollVotesUseCase,
                            appContainer.observeMyVoteUseCase,
                            appContainer.submitVoteUseCase,
                            appContainer.analyticsReporter
                        )
                    }
                }
            )
            StudentPollsScreen(courseId = courseId, viewModel = studentPollsViewModel)
        }

        composable(
            route = Routes.TeacherQuiz.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            TrackScreenView(appContainer, "teacher_quiz")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val teacherQuizViewModel: TeacherQuizViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        TeacherQuizViewModel(
                            appContainer.getQuestionBankUseCase,
                            appContainer.createQuizQuestionUseCase,
                            appContainer.updateQuizQuestionUseCase,
                            appContainer.deleteQuizQuestionUseCase,
                            appContainer.launchQuizQuestionUseCase,
                            appContainer.closeQuizSessionUseCase,
                            appContainer.observeLatestQuizSessionUseCase,
                            appContainer.observeQuizSessionAnswersUseCase,
                            appContainer.observeCourseQuizStandingUseCase,
                            appContainer.resetQuizStandingUseCase,
                            appContainer.analyticsReporter
                        )
                    }
                }
            )
            TeacherQuizScreen(courseId = courseId, viewModel = teacherQuizViewModel)
        }

        composable(
            route = Routes.StudentQuiz.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            TrackScreenView(appContainer, "student_quiz")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val studentQuizViewModel: StudentQuizViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        StudentQuizViewModel(
                            appContainer.observeLatestQuizSessionUseCase,
                            appContainer.observeMyQuizAnswerUseCase,
                            appContainer.observeQuizSessionAnswersUseCase,
                            appContainer.submitQuizAnswerUseCase,
                            appContainer.observeCourseQuizStandingUseCase,
                            appContainer.analyticsReporter
                        )
                    }
                }
            )
            StudentQuizScreen(courseId = courseId, viewModel = studentQuizViewModel)
        }

        composable(
            route = Routes.TeacherPosts.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            TrackScreenView(appContainer, "teacher_posts")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val teacherPostsViewModel: TeacherPostsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        TeacherPostsViewModel(
                            appContainer.observePostsUseCase,
                            appContainer.observePostReactionsUseCase,
                            appContainer.createPostUseCase,
                            appContainer.updatePostUseCase,
                            appContainer.deletePostUseCase,
                            appContainer.analyticsReporter
                        )
                    }
                }
            )
            TeacherPostsScreen(courseId = courseId, viewModel = teacherPostsViewModel)
        }

        composable(
            route = Routes.StudentPosts.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            TrackScreenView(appContainer, "student_posts")
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            val studentPostsViewModel: StudentPostsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        StudentPostsViewModel(
                            appContainer.observePostsUseCase,
                            appContainer.observePostReactionsUseCase,
                            appContainer.observeMyReactionUseCase,
                            appContainer.setMyReactionUseCase,
                            appContainer.removeMyReactionUseCase,
                            appContainer.analyticsReporter
                        )
                    }
                }
            )
            StudentPostsScreen(courseId = courseId, viewModel = studentPostsViewModel)
        }
    }
}

@Composable
private fun TrackScreenView(appContainer: AppContainer, screenName: String) {
    LaunchedEffect(Unit) {
        appContainer.analyticsReporter.logScreenView(screenName)
    }
}

private fun homeRouteForRole(role: String, courseId: String): String {
    return if (role == UserRole.TEACHER) {
        Routes.TeacherHome.createRoute(courseId)
    } else {
        Routes.StudentHome.createRoute(courseId)
    }
}

private fun NavHostController.navigateClearingBackStack(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
    }
}