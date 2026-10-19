package com.svape.unal.ingesoftii.di

import android.content.Context
import com.svape.unal.ingesoftii.R
import com.svape.unal.ingesoftii.data.repository.AuthRepositoryImpl
import com.svape.unal.ingesoftii.data.repository.CourseRepositoryImpl
import com.svape.unal.ingesoftii.data.repository.GradeRepositoryImpl
import com.svape.unal.ingesoftii.data.repository.SyllabusRepositoryImpl
import com.svape.unal.ingesoftii.data.repository.TeamRepositoryImpl
import com.svape.unal.ingesoftii.data.service.FirebaseAnalyticsReporter
import com.svape.unal.ingesoftii.data.service.FirebaseCrashReporter
import com.svape.unal.ingesoftii.domain.repository.AuthRepository
import com.svape.unal.ingesoftii.domain.repository.CourseRepository
import com.svape.unal.ingesoftii.domain.repository.GradeRepository
import com.svape.unal.ingesoftii.domain.repository.SyllabusRepository
import com.svape.unal.ingesoftii.data.repository.QuizStandingRepositoryImpl
import com.svape.unal.ingesoftii.domain.repository.QuizStandingRepository
import com.svape.unal.ingesoftii.domain.usecase.ObserveCourseQuizStandingUseCase
import com.svape.unal.ingesoftii.domain.usecase.ResetQuizStandingUseCase
import com.svape.unal.ingesoftii.domain.repository.TeamRepository
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.service.CrashReporter
import com.svape.unal.ingesoftii.domain.usecase.AddGradeItemUseCase
import com.svape.unal.ingesoftii.domain.usecase.AddStudentToTeamUseCase
import com.svape.unal.ingesoftii.domain.usecase.CreateTaskUseCase
import com.svape.unal.ingesoftii.domain.usecase.CreateTeamUseCase
import com.svape.unal.ingesoftii.domain.usecase.DeleteStudentGradeUseCase
import com.svape.unal.ingesoftii.domain.usecase.DeleteTaskUseCase
import com.svape.unal.ingesoftii.domain.usecase.DeleteTeamUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetCourseGradesOverviewUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetCourseStudentsUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetCourseTeamsUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetCurrentSessionUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetGradeItemsUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetMyGradesUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetMyTeamUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetStudentGradesUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetSyllabusFileUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetTeamByIdUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetTeamTasksUseCase
import com.svape.unal.ingesoftii.domain.usecase.MoveTaskUseCase
import com.svape.unal.ingesoftii.domain.usecase.RedeemRegistrationCodeUseCase
import com.svape.unal.ingesoftii.domain.usecase.RemoveStudentFromTeamUseCase
import com.svape.unal.ingesoftii.data.repository.AttendanceRepositoryImpl
import com.svape.unal.ingesoftii.domain.repository.AttendanceRepository
import com.svape.unal.ingesoftii.domain.usecase.CloseAttendanceSessionUseCase
import com.svape.unal.ingesoftii.domain.usecase.HasSubmittedAttendanceUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveActiveAttendanceSessionUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveAttendanceRegistrationsUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveMyVoteUseCase
import com.svape.unal.ingesoftii.domain.usecase.StartAttendanceSessionUseCase
import com.svape.unal.ingesoftii.domain.usecase.SubmitAttendanceUseCase
import com.svape.unal.ingesoftii.domain.usecase.RenameTeamUseCase
import com.svape.unal.ingesoftii.domain.usecase.SetStudentGradeUseCase
import com.svape.unal.ingesoftii.domain.usecase.SignInWithGoogleUseCase
import com.svape.unal.ingesoftii.domain.usecase.SignInWithEmailPasswordUseCase
import com.svape.unal.ingesoftii.domain.usecase.CountStudentsGradedUseCase
import com.svape.unal.ingesoftii.domain.usecase.DeleteGradeItemUseCase
import com.svape.unal.ingesoftii.domain.usecase.RenameGradeItemUseCase
import com.svape.unal.ingesoftii.domain.usecase.SignOutUseCase
import com.svape.unal.ingesoftii.data.repository.PollRepositoryImpl
import com.svape.unal.ingesoftii.data.repository.PostRepositoryImpl
import com.svape.unal.ingesoftii.data.repository.QuizRepositoryImpl
import com.svape.unal.ingesoftii.data.service.FirebaseNotificationService
import com.svape.unal.ingesoftii.domain.repository.PollRepository
import com.svape.unal.ingesoftii.domain.repository.PostRepository
import com.svape.unal.ingesoftii.domain.repository.QuizRepository
import com.svape.unal.ingesoftii.domain.service.NotificationService
import com.svape.unal.ingesoftii.domain.usecase.ClosePollUseCase
import com.svape.unal.ingesoftii.domain.usecase.CloseQuizSessionUseCase
import com.svape.unal.ingesoftii.domain.usecase.CreatePostUseCase
import com.svape.unal.ingesoftii.domain.usecase.CreateQuizQuestionUseCase
import com.svape.unal.ingesoftii.domain.usecase.DeletePostUseCase
import com.svape.unal.ingesoftii.domain.usecase.DeleteQuizQuestionUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetQuestionBankUseCase
import com.svape.unal.ingesoftii.domain.usecase.LaunchQuizQuestionUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveLatestPollUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveLatestQuizSessionUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveMyQuizAnswerUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveMyReactionUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObservePollVotesUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObservePostReactionsUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObservePostsUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveQuizSessionAnswersUseCase
import com.svape.unal.ingesoftii.domain.usecase.RemoveMyReactionUseCase
import com.svape.unal.ingesoftii.domain.usecase.SetMyReactionUseCase
import com.svape.unal.ingesoftii.domain.usecase.StartPollUseCase
import com.svape.unal.ingesoftii.domain.usecase.SubmitQuizAnswerUseCase
import com.svape.unal.ingesoftii.domain.usecase.SubmitVoteUseCase
import com.svape.unal.ingesoftii.domain.usecase.SubscribeToCourseNotificationsUseCase
import com.svape.unal.ingesoftii.domain.usecase.UnsubscribeFromCourseNotificationsUseCase
import com.svape.unal.ingesoftii.domain.usecase.UpdatePostUseCase
import com.svape.unal.ingesoftii.domain.usecase.UpdateQuizQuestionUseCase
import com.svape.unal.ingesoftii.navigation.DeepLinkTarget
import com.svape.unal.ingesoftii.presentation.session.CurrentSessionHolder
import kotlinx.coroutines.flow.MutableStateFlow


class AppContainer(private val context: Context) {


    val pendingDeepLink = MutableStateFlow<DeepLinkTarget?>(null)

    val currentSessionHolder = CurrentSessionHolder()

    private val webClientId: String by lazy {
        context.getString(R.string.default_web_client_id)
    }

    val crashReporter: CrashReporter by lazy {
        FirebaseCrashReporter()
    }

    val analyticsReporter: AnalyticsReporter by lazy {
        FirebaseAnalyticsReporter(context.applicationContext)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(context.applicationContext, webClientId, crashReporter = crashReporter)
    }

    val courseRepository: CourseRepository by lazy {
        CourseRepositoryImpl(crashReporter = crashReporter)
    }

    val syllabusRepository: SyllabusRepository by lazy {
        SyllabusRepositoryImpl(context.applicationContext, crashReporter = crashReporter)
    }

    val gradeRepository: GradeRepository by lazy {
        GradeRepositoryImpl(crashReporter = crashReporter)
    }

    val teamRepository: TeamRepository by lazy {
        TeamRepositoryImpl(crashReporter = crashReporter)
    }

    val signInWithGoogleUseCase: SignInWithGoogleUseCase by lazy {
        SignInWithGoogleUseCase(authRepository)
    }

    val signInWithEmailPasswordUseCase: SignInWithEmailPasswordUseCase by lazy {
        SignInWithEmailPasswordUseCase(authRepository)
    }

    val redeemRegistrationCodeUseCase: RedeemRegistrationCodeUseCase by lazy {
        RedeemRegistrationCodeUseCase(courseRepository)
    }

    val getCurrentSessionUseCase: GetCurrentSessionUseCase by lazy {
        GetCurrentSessionUseCase(authRepository)
    }

    val signOutUseCase: SignOutUseCase by lazy {
        SignOutUseCase(authRepository)
    }

    val getSyllabusFileUseCase: GetSyllabusFileUseCase by lazy {
        GetSyllabusFileUseCase(syllabusRepository)
    }

    val getGradeItemsUseCase: GetGradeItemsUseCase by lazy {
        GetGradeItemsUseCase(gradeRepository)
    }

    val addGradeItemUseCase: AddGradeItemUseCase by lazy {
        AddGradeItemUseCase(gradeRepository)
    }

    val deleteStudentGradeUseCase: DeleteStudentGradeUseCase by lazy {
        DeleteStudentGradeUseCase(gradeRepository)
    }

    val renameGradeItemUseCase: RenameGradeItemUseCase by lazy {
        RenameGradeItemUseCase(gradeRepository)
    }

    val countStudentsGradedUseCase: CountStudentsGradedUseCase by lazy {
        CountStudentsGradedUseCase(gradeRepository)
    }

    val deleteGradeItemUseCase: DeleteGradeItemUseCase by lazy {
        DeleteGradeItemUseCase(gradeRepository)
    }

    val getCourseGradesOverviewUseCase: GetCourseGradesOverviewUseCase by lazy {
        GetCourseGradesOverviewUseCase(gradeRepository)
    }

    val getMyGradesUseCase: GetMyGradesUseCase by lazy {
        GetMyGradesUseCase(gradeRepository)
    }

    val getStudentGradesUseCase: GetStudentGradesUseCase by lazy {
        GetStudentGradesUseCase(gradeRepository)
    }

    val setStudentGradeUseCase: SetStudentGradeUseCase by lazy {
        SetStudentGradeUseCase(gradeRepository)
    }

    val getCourseStudentsUseCase: GetCourseStudentsUseCase by lazy {
        GetCourseStudentsUseCase(courseRepository)
    }

    val createTeamUseCase: CreateTeamUseCase by lazy {
        CreateTeamUseCase(teamRepository)
    }

    val getCourseTeamsUseCase: GetCourseTeamsUseCase by lazy {
        GetCourseTeamsUseCase(teamRepository)
    }

    val addStudentToTeamUseCase: AddStudentToTeamUseCase by lazy {
        AddStudentToTeamUseCase(teamRepository)
    }

    val removeStudentFromTeamUseCase: RemoveStudentFromTeamUseCase by lazy {
        RemoveStudentFromTeamUseCase(teamRepository)
    }

    val getMyTeamUseCase: GetMyTeamUseCase by lazy {
        GetMyTeamUseCase(teamRepository)
    }

    val getTeamTasksUseCase: GetTeamTasksUseCase by lazy {
        GetTeamTasksUseCase(teamRepository)
    }

    val createTaskUseCase: CreateTaskUseCase by lazy {
        CreateTaskUseCase(teamRepository)
    }

    val moveTaskUseCase: MoveTaskUseCase by lazy {
        MoveTaskUseCase(teamRepository)
    }

    val deleteTaskUseCase: DeleteTaskUseCase by lazy {
        DeleteTaskUseCase(teamRepository)
    }

    val getTeamByIdUseCase: GetTeamByIdUseCase by lazy {
        GetTeamByIdUseCase(teamRepository)
    }

    val renameTeamUseCase: RenameTeamUseCase by lazy {
        RenameTeamUseCase(teamRepository)
    }

    val deleteTeamUseCase: DeleteTeamUseCase by lazy {
        DeleteTeamUseCase(teamRepository)
    }

    val attendanceRepository: AttendanceRepository by lazy {
        AttendanceRepositoryImpl(crashReporter = crashReporter)
    }

    val startAttendanceSessionUseCase: StartAttendanceSessionUseCase by lazy {
        StartAttendanceSessionUseCase(attendanceRepository)
    }

    val closeAttendanceSessionUseCase: CloseAttendanceSessionUseCase by lazy {
        CloseAttendanceSessionUseCase(attendanceRepository)
    }

    val observeActiveAttendanceSessionUseCase: ObserveActiveAttendanceSessionUseCase by lazy {
        ObserveActiveAttendanceSessionUseCase(attendanceRepository)
    }

    val observeAttendanceRegistrationsUseCase: ObserveAttendanceRegistrationsUseCase by lazy {
        ObserveAttendanceRegistrationsUseCase(attendanceRepository)
    }

    val submitAttendanceUseCase: SubmitAttendanceUseCase by lazy {
        SubmitAttendanceUseCase(attendanceRepository)
    }

    val hasSubmittedAttendanceUseCase: HasSubmittedAttendanceUseCase by lazy {
        HasSubmittedAttendanceUseCase(attendanceRepository)
    }

    val pollRepository: PollRepository by lazy {
        PollRepositoryImpl(crashReporter = crashReporter)
    }

    val startPollUseCase: StartPollUseCase by lazy {
        StartPollUseCase(pollRepository)
    }

    val closePollUseCase: ClosePollUseCase by lazy {
        ClosePollUseCase(pollRepository)
    }

    val observeLatestPollUseCase: ObserveLatestPollUseCase by lazy {
        ObserveLatestPollUseCase(pollRepository)
    }

    val observePollVotesUseCase: ObservePollVotesUseCase by lazy {
        ObservePollVotesUseCase(pollRepository)
    }

    val submitVoteUseCase: SubmitVoteUseCase by lazy {
        SubmitVoteUseCase(pollRepository)
    }

    val observeMyVoteUseCase: ObserveMyVoteUseCase by lazy {
        ObserveMyVoteUseCase(pollRepository)
    }

    val quizRepository: QuizRepository by lazy {
        QuizRepositoryImpl(crashReporter = crashReporter)
    }

    val getQuestionBankUseCase by lazy {
        GetQuestionBankUseCase(quizRepository)
    }

    val createQuizQuestionUseCase by lazy {
        CreateQuizQuestionUseCase(quizRepository)
    }

    val updateQuizQuestionUseCase by lazy {
        UpdateQuizQuestionUseCase(quizRepository)
    }

    val deleteQuizQuestionUseCase by lazy {
        DeleteQuizQuestionUseCase(quizRepository)
    }

    val launchQuizQuestionUseCase by lazy {
        LaunchQuizQuestionUseCase(quizRepository)
    }

    val closeQuizSessionUseCase by lazy {
        CloseQuizSessionUseCase(quizRepository)
    }

    val observeLatestQuizSessionUseCase by lazy {
        ObserveLatestQuizSessionUseCase(quizRepository)
    }

    val observeQuizSessionAnswersUseCase by lazy {
        ObserveQuizSessionAnswersUseCase(quizRepository)
    }

    val submitQuizAnswerUseCase by lazy {
        SubmitQuizAnswerUseCase(quizRepository)
    }

    val observeMyQuizAnswerUseCase by lazy {
        ObserveMyQuizAnswerUseCase(quizRepository)
    }


    val postRepository: PostRepository by lazy {
        PostRepositoryImpl(crashReporter = crashReporter)
    }

    val notificationService: NotificationService by lazy {
        FirebaseNotificationService(crashReporter = crashReporter)
    }

    val createPostUseCase by lazy {
        CreatePostUseCase(postRepository)
    }

    val updatePostUseCase by lazy {
        UpdatePostUseCase(postRepository)
    }

    val deletePostUseCase by lazy {
        DeletePostUseCase(postRepository)
    }

    val observePostsUseCase by lazy {
        ObservePostsUseCase(postRepository)
    }

    val observePostReactionsUseCase by lazy {
        ObservePostReactionsUseCase(postRepository)
    }

    val setMyReactionUseCase by lazy {
        SetMyReactionUseCase(postRepository)
    }

    val removeMyReactionUseCase by lazy {
        RemoveMyReactionUseCase(postRepository)
    }

    val observeMyReactionUseCase by lazy {
        ObserveMyReactionUseCase(postRepository)
    }

    val quizStandingRepository: QuizStandingRepository by lazy {
        QuizStandingRepositoryImpl()
    }

    val observeCourseQuizStandingUseCase by lazy {
        ObserveCourseQuizStandingUseCase(quizStandingRepository)
    }

    val resetQuizStandingUseCase by lazy {
        ResetQuizStandingUseCase(quizStandingRepository)
    }

    val subscribeToCourseNotificationsUseCase by lazy {
        SubscribeToCourseNotificationsUseCase(notificationService)
    }

    val unsubscribeFromCourseNotificationsUseCase by lazy {
        UnsubscribeFromCourseNotificationsUseCase(notificationService)
    }


}