package com.svape.unal.ingesoftii.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.svape.unal.ingesoftii.domain.model.KanbanTask
import com.svape.unal.ingesoftii.domain.model.Team
import com.svape.unal.ingesoftii.domain.model.TaskColumn
import com.svape.unal.ingesoftii.domain.model.TaskError
import com.svape.unal.ingesoftii.domain.model.TeamError
import com.svape.unal.ingesoftii.domain.repository.TeamRepository
import com.svape.unal.ingesoftii.domain.service.CrashReporter
import kotlinx.coroutines.tasks.await

private const val COURSES_COLLECTION = "cursos"
private const val TEAMS_SUBCOLLECTION = "equipos"
private const val TASKS_SUBCOLLECTION = "tareas"

private const val NAME_FIELD = "nombre"
private const val MEMBERS_FIELD = "integrantesUids"
private const val CREATED_AT_FIELD = "creadoEn"

private const val TITLE_FIELD = "titulo"
private const val DESCRIPTION_FIELD = "descripcion"
private const val COLUMN_FIELD = "columna"
private const val CREATED_BY_FIELD = "creadaPorUid"
private const val ORDER_FIELD = "orden"
private const val TASK_CREATED_AT_FIELD = "creadaEn"
private const val TASK_UPDATED_AT_FIELD = "actualizadaEn"

class TeamRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val crashReporter: CrashReporter
) : TeamRepository {

    //Teams

    override suspend fun createTeam(courseId: String, name: String): Result<Team> {
        return try {
            val teamsRef = teamsCollection(courseId)
            val newDocRef = teamsRef.document()

            val data = mapOf(
                NAME_FIELD to name,
                MEMBERS_FIELD to emptyList<String>(),
                CREATED_AT_FIELD to Timestamp.now()
            )
            newDocRef.set(data).await()

            Result.success(Team(newDocRef.id, name, emptyList(), System.currentTimeMillis()))
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(TeamError.Unknown(e.message ?: "No se pudo crear el equipo"))
        }
    }

    override suspend fun getCourseTeams(courseId: String): Result<List<Team>> {
        return try {
            val snapshot = teamsCollection(courseId).get().await()
            Result.success(snapshot.documents.map { it.toTeam() })
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(TeamError.Unknown(e.message ?: "No se pudieron cargar los equipos"))
        }
    }

    override suspend fun getTeamById(courseId: String, teamId: String): Result<Team> {
        return try {
            val doc = teamsCollection(courseId).document(teamId).get().await()
            if (!doc.exists()) {
                return Result.failure(TeamError.TeamNotFound)
            }
            Result.success(doc.toTeam())
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(TeamError.Unknown(e.message ?: "No se pudo cargar el equipo"))
        }
    }

    override suspend fun addStudentToTeam(
        courseId: String,
        teamId: String,
        studentUid: String
    ): Result<Unit> {
        return try {
            teamsCollection(courseId).document(teamId)
                .update(MEMBERS_FIELD, FieldValue.arrayUnion(studentUid))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(
                TeamError.Unknown(
                    e.message ?: "No se pudo agregar el estudiante al equipo"
                )
            )
        }
    }

    override suspend fun removeStudentFromTeam(
        courseId: String,
        teamId: String,
        studentUid: String
    ): Result<Unit> {
        return try {
            teamsCollection(courseId).document(teamId)
                .update(MEMBERS_FIELD, FieldValue.arrayRemove(studentUid))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(
                TeamError.Unknown(
                    e.message ?: "No se pudo quitar el estudiante del equipo"
                )
            )
        }
    }

    override suspend fun getMyTeam(courseId: String): Result<Team?> {
        val studentUid = auth.currentUser?.uid
            ?: return Result.failure(TeamError.Unknown("Usuario no autenticado"))

        return try {
            val snapshot = teamsCollection(courseId)
                .whereArrayContains(MEMBERS_FIELD, studentUid)
                .limit(1)
                .get()
                .await()

            Result.success(snapshot.documents.firstOrNull()?.toTeam())
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(TeamError.Unknown(e.message ?: "No se pudo cargar tu equipo"))
        }
    }

    //Tasks

    override suspend fun getTeamTasks(courseId: String, teamId: String): Result<List<KanbanTask>> {
        return try {
            val snapshot = tasksCollection(courseId, teamId)
                .orderBy(ORDER_FIELD)
                .get()
                .await()

            Result.success(snapshot.documents.map { it.toKanbanTask(teamId) })
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(
                TaskError.Unknown(
                    e.message ?: "No se pudieron cargar las tareas del equipo"
                )
            )
        }
    }

    override suspend fun createTask(
        courseId: String,
        teamId: String,
        title: String,
        description: String
    ): Result<KanbanTask> {
        val createdByUid = auth.currentUser?.uid
            ?: return Result.failure(TaskError.NotAuthenticated)

        return try {
            val tasksRef = tasksCollection(courseId, teamId)
            val currentCount = tasksRef.get().await().size()
            val newDocRef = tasksRef.document()
            val now = Timestamp.now()

            val data = mapOf(
                TITLE_FIELD to title,
                DESCRIPTION_FIELD to description,
                COLUMN_FIELD to TaskColumn.PROPUESTAS.firestoreValue,
                CREATED_BY_FIELD to createdByUid,
                ORDER_FIELD to currentCount,
                TASK_CREATED_AT_FIELD to now,
                TASK_UPDATED_AT_FIELD to now
            )
            newDocRef.set(data).await()

            Result.success(
                KanbanTask(
                    id = newDocRef.id,
                    teamId = teamId,
                    title = title,
                    description = description,
                    column = TaskColumn.PROPUESTAS,
                    createdByUid = createdByUid,
                    order = currentCount,
                    createdAt = now.toDate().time,
                    updatedAt = now.toDate().time
                )
            )
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(TaskError.Unknown(e.message ?: "No se pudo crear la tarea"))
        }
    }

    override suspend fun moveTask(
        courseId: String,
        teamId: String,
        taskId: String,
        newColumn: TaskColumn
    ): Result<Unit> {
        return try {
            tasksCollection(courseId, teamId).document(taskId)
                .update(
                    mapOf(
                        COLUMN_FIELD to newColumn.firestoreValue,
                        TASK_UPDATED_AT_FIELD to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(TaskError.Unknown(e.message ?: "No se pudo mover la tarea"))
        }
    }

    override suspend fun deleteTask(
        courseId: String,
        teamId: String,
        taskId: String
    ): Result<Unit> {
        return try {
            tasksCollection(courseId, teamId).document(taskId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(TaskError.Unknown(e.message ?: "No se pudo eliminar la tarea"))
        }
    }

    override suspend fun renameTeam(courseId: String, teamId: String, newName: String): Result<Unit> {
        return try {
            teamsCollection(courseId).document(teamId)
                .update(NAME_FIELD, newName)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(TeamError.Unknown(e.message ?: "No se pudo renombrar el equipo"))
        }
    }

    override suspend fun deleteTeam(courseId: String, teamId: String): Result<Unit> {
        return try {
            val tasksSnapshot = tasksCollection(courseId, teamId).get().await()
            val batch = firestore.batch()
            tasksSnapshot.documents.forEach { batch.delete(it.reference) }
            batch.delete(teamsCollection(courseId).document(teamId))
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(TeamError.Unknown(e.message ?: "No se pudo eliminar el equipo"))
        }
    }

    //Helpers

    private fun teamsCollection(courseId: String) =
        firestore.collection(COURSES_COLLECTION).document(courseId).collection(TEAMS_SUBCOLLECTION)

    private fun tasksCollection(courseId: String, teamId: String) =
        teamsCollection(courseId).document(teamId).collection(TASKS_SUBCOLLECTION)

    private fun com.google.firebase.firestore.DocumentSnapshot.toTeam(): Team {
        val membersRaw = get(MEMBERS_FIELD) as? List<*> ?: emptyList<Any>()
        return Team(
            id = id,
            name = getString(NAME_FIELD).orEmpty(),
            memberUids = membersRaw.filterIsInstance<String>(),
            createdAt = getTimestamp(CREATED_AT_FIELD)?.toDate()?.time ?: 0L
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toKanbanTask(teamId: String): KanbanTask {
        return KanbanTask(
            id = id,
            teamId = teamId,
            title = getString(TITLE_FIELD).orEmpty(),
            description = getString(DESCRIPTION_FIELD).orEmpty(),
            column = TaskColumn.fromFirestoreValue(getString(COLUMN_FIELD).orEmpty()),
            createdByUid = getString(CREATED_BY_FIELD).orEmpty(),
            order = getLong(ORDER_FIELD)?.toInt() ?: 0,
            createdAt = getTimestamp(TASK_CREATED_AT_FIELD)?.toDate()?.time ?: 0L,
            updatedAt = getTimestamp(TASK_UPDATED_AT_FIELD)?.toDate()?.time ?: 0L
        )
    }
}