package com.svape.unal.ingesoftii.navigation

sealed class Routes(val route: String) {
    data object Splash : Routes("splash")
    data object Login : Routes("login")
    data object CourseCode : Routes("course_code")

    data object TeacherHome : Routes("teacher_home/{courseId}") {
        fun createRoute(courseId: String) = "teacher_home/$courseId"
    }

    data object StudentHome : Routes("student_home/{courseId}") {
        fun createRoute(courseId: String) = "student_home/$courseId"
    }

    data object Syllabus : Routes("syllabus/{courseId}") {
        fun createRoute(courseId: String) = "syllabus/$courseId"
    }

    data object TeacherGrades : Routes("teacher_grades/{courseId}") {
        fun createRoute(courseId: String) = "teacher_grades/$courseId"
    }

    data object StudentGrades : Routes("student_grades/{courseId}") {
        fun createRoute(courseId: String) = "student_grades/$courseId"
    }

    data object StudentGradeEdit : Routes("student_grade_edit/{courseId}/{studentUid}") {
        fun createRoute(courseId: String, studentUid: String) =
            "student_grade_edit/$courseId/$studentUid"
    }

    data object TeacherGradesMatrix : Routes("teacher_grades_matrix/{courseId}") {
        fun createRoute(courseId: String) = "teacher_grades_matrix/$courseId"
    }

    data object TeacherTeams : Routes("teacher_teams/{courseId}") {
        fun createRoute(courseId: String) = "teacher_teams/$courseId"
    }

    data object TeacherTeamBoard : Routes("teacher_team_board/{courseId}/{teamId}") {
        fun createRoute(courseId: String, teamId: String) =
            "teacher_team_board/$courseId/$teamId"
    }

    data object StudentTeamBoard : Routes("student_team_board/{courseId}") {
        fun createRoute(courseId: String) = "student_team_board/$courseId"
    }

    data object TeacherAttendance : Routes("teacher_attendance/{courseId}") {
        fun createRoute(courseId: String) = "teacher_attendance/$courseId"
    }

    data object StudentAttendance : Routes("student_attendance/{courseId}") {
        fun createRoute(courseId: String) = "student_attendance/$courseId"
    }

    data object TeacherPolls : Routes("teacher_polls/{courseId}") {
        fun createRoute(courseId: String) = "teacher_polls/$courseId"
    }

    data object StudentPolls : Routes("student_polls/{courseId}") {
        fun createRoute(courseId: String) = "student_polls/$courseId"
    }

    data object TeacherQuiz : Routes("teacher_quiz/{courseId}") {
        fun createRoute(courseId: String) = "teacher_quiz/$courseId"
    }

    data object StudentQuiz : Routes("student_quiz/{courseId}") {
        fun createRoute(courseId: String) = "student_quiz/$courseId"
    }

    data object TeacherPosts : Routes("teacher_posts/{courseId}") {
        fun createRoute(courseId: String) = "teacher_posts/$courseId"
    }

    data object StudentPosts : Routes("student_posts/{courseId}") {
        fun createRoute(courseId: String) = "student_posts/$courseId"
    }
}