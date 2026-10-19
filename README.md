# ISW2 — Android

A classroom companion app for the **Software Engineering II** course at **Universidad Nacional de Colombia (UNAL)**. Teachers run attendance, live polls and quizzes from their phone, and students take part in real time from theirs.

This is the native Android client. A companion web app (PWA) shares the same Firebase backend and is available at [ingesoftun4l.com](https://ingesoftun4l.com).

<p align="center">
  <img src="https://raw.githubusercontent.com/OldHus/isw2-android/refs/heads/main/assets/login.png" alt="ISW2 app overview" width="220" />
</p>

## Features

### For students
- **Sign in with Google** using an institutional `@unal.edu.co` account, then join the course with an access code.
- **Attendance** — enter the code shown in class before the countdown runs out.
- **Live polls** — vote, change your vote while the poll is open, and see results update in real time.
- **Live quiz** — answer timed questions and follow your position in the course ranking.
- **Grades** — check every graded item in one place.
- **Team board** — a Kanban board (proposals → to do → in progress → QA) shared with your team.
- **Announcements** — read course posts and react to them.
- **Syllabus** — open the course syllabus as a PDF.
- **Push notifications** that open the right screen when tapped.

### For teachers
- **Attendance sessions** with a timed code, a live roster, a projector mode for the classroom and plain-text export.
- **Polls** with custom deadlines, live results and the list of voters per option.
- **Quiz** with a question bank, timed questions and a cumulative ranking.
- **Gradebook** with grade items, per-student editing and a full grades matrix.
- **Teams** — create teams, assign students and manage each team's board.
- **Posts** — publish, edit and delete course announcements.

## Screenshots

| Student home | Attendance |
| :---: | :---: |
| <img src="https://raw.githubusercontent.com/OldHus/isw2-android/refs/heads/main/assets/home%20student.png" width="220" /> | <img src="https://raw.githubusercontent.com/OldHus/isw2-android/refs/heads/main/assets/attendance.png" width="220" /> |

| Live poll | Quiz | Team board |
| :---: | :---: | :---: |
| <img src="https://raw.githubusercontent.com/OldHus/isw2-android/refs/heads/main/assets/poll.png" width="220" /> | <img src="https://raw.githubusercontent.com/OldHus/isw2-android/refs/heads/main/assets/quizz.png" width="220" /> | <img src="https://raw.githubusercontent.com/OldHus/isw2-android/refs/heads/main/assets/student%20team.png" width="220" /> |

| Teacher home | Projector mode | Grades matrix |
| :---: | :---: | :---: |
| <img src="https://raw.githubusercontent.com/OldHus/isw2-android/refs/heads/main/assets/home%20teacher.png" width="220" /> | <img src="https://raw.githubusercontent.com/OldHus/isw2-android/refs/heads/main/assets/teacher%20team.png" width="220" /> | <img src="https://raw.githubusercontent.com/OldHus/isw2-android/refs/heads/main/assets/teacher%20grades.png" width="220" /> |

## Tech stack

| Area | Tools |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3, Navigation Compose, Splash Screen API |
| Async | Kotlin Coroutines, Flow / StateFlow |
| Auth | Firebase Authentication, Credential Manager (Sign in with Google) |
| Data | Cloud Firestore (real-time listeners), Firebase Storage |
| Messaging | Firebase Cloud Messaging with deep links |
| Quality | Firebase Crashlytics, Google Analytics for Firebase |
| Images | Coil |

Minimum SDK 25 · Target SDK 37

## Architecture

The project follows **Clean Architecture** with an **MVVM** presentation layer.

```
app/src/main/java/com/svape/unal/ingesoftii/
├── data/            Firestore repositories and Firebase services
├── domain/          Models, repository interfaces and use cases
├── presentation/    Screens (Compose) and ViewModels, grouped by feature
├── navigation/      Navigation graph, routes and deep links
├── di/              AppContainer (manual dependency injection)
└── ui/              Theme, motion tokens and shared components
```

- **Domain** has no Android or Firebase dependencies. Each action is a use case (`SubmitAttendanceUseCase`, `StartPollUseCase`, …).
- **Data** implements the repository interfaces on top of Firestore, exposing real-time updates as `Flow`.
- **Presentation** ViewModels expose a single `StateFlow` of a sealed `UiState`, and screens render it with Compose.
- **Dependency injection** is done by hand through `AppContainer`, keeping the wiring explicit and easy to follow.

## Security model

All authorization is enforced by **Firestore Security Rules**, not by the client:

- Only institutional accounts can register, and a student can only read data from their own course.
- Teachers are the only ones who can write grades, teams, polls and quiz sessions.
- Secrets that students must not see (the attendance code and the quiz answer key) live in teacher-only `privado` subcollections, and the rules validate them on the server.
- Course access codes are stored as document IDs in a non-listable collection, so knowing the code is the only way to look it up.

## Getting started

### Requirements
- Android Studio (latest stable)
- JDK 11 or newer
- A Firebase project of your own

### Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/OldHus/isw2-android.git
   ```
2. In the Firebase console, add an Android app with the package name `com.svape.unal.ingesoftii` and register your debug SHA-1.
3. Enable **Authentication → Google**, **Cloud Firestore**, **Storage**, **Cloud Messaging** and **Crashlytics**.
4. Download `google-services.json` and place it in `app/`. This file is ignored by Git and is never committed.
5. Deploy the Firestore Security Rules for the project.
6. Create a course document in `cursos/{courseId}` and its access code in `codigosAcceso/{CODE}` with the field `cursoId`.
7. Open the project in Android Studio and run the `app` configuration.

Teacher accounts are created directly in Firestore by setting `rol: "docente"` on the user's profile in `usuarios/{uid}`.

## Academic context

This app was built for the Software Engineering II course at Universidad Nacional de Colombia as a real tool used in class, and as a hands-on example of mobile architecture, real-time data and backend security rules.

## Author

**Sergio Vargas**, Software Engineering instructor, Universidad Nacional de Colombia.

## License and attribution

Released under the [MIT License](LICENSE). You are free to clone, use, modify and share this project, including for academic and commercial purposes, as long as the copyright notice and license are kept.

If you use this project in your own work, a course or a publication, please credit the author. GitHub's **"Cite this repository"** button gives you a ready-to-use citation.
