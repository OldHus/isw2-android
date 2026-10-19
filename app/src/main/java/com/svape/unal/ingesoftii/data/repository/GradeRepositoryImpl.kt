package com.svape.unal.ingesoftii.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.svape.unal.ingesoftii.domain.model.GradeError
import com.svape.unal.ingesoftii.domain.model.GradeItem
import com.svape.unal.ingesoftii.domain.model.GradeItemType
import com.svape.unal.ingesoftii.domain.model.StudentGrades
import com.svape.unal.ingesoftii.domain.repository.GradeRepository
import com.svape.unal.ingesoftii.domain.service.CrashReporter
import kotlinx.coroutines.tasks.await

private const val COURSES_COLLECTION = "cursos"
private const val GRADE_ITEMS_SUBCOLLECTION = "itemsCalificacion"
private const val GRADES_COLLECTION = "calificaciones"
private const val STUDENTS_SUBCOLLECTION = "estudiantes"
private const val NAME_FIELD = "nombre"
private const val WEIGHT_FIELD = "peso"
private const val TYPE_FIELD = "tipo"
private const val ORDER_FIELD = "orden"
private const val CREATED_AT_FIELD = "creadoEn"
private const val GRADES_FIELD = "notas"

private const val MAX_BATCH_OPERATIONS = 450

class GradeRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val crashReporter: CrashReporter
) : GradeRepository {

    override suspend fun getGradeItems(courseId: String): Result<List<GradeItem>> {
        return try {
            val snapshot = firestore.collection(COURSES_COLLECTION)
                .document(courseId)
                .collection(GRADE_ITEMS_SUBCOLLECTION)
                .orderBy(ORDER_FIELD)
                .get()
                .await()

            val items = snapshot.documents.map { doc ->
                GradeItem(
                    id = doc.id,
                    name = doc.getString(NAME_FIELD).orEmpty(),
                    weight = doc.getDouble(WEIGHT_FIELD) ?: 0.0,
                    type = doc.getString(TYPE_FIELD) ?: GradeItemType.DYNAMIC,
                    order = doc.getLong(ORDER_FIELD)?.toInt() ?: 0
                )
            }
            Result.success(items)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(
                GradeError.Unknown(
                    e.message ?: "No se pudieron cargar los ítems de calificación"
                )
            )
        }
    }

    override suspend fun addGradeItem(
        courseId: String,
        name: String,
        weight: Double,
        type: String
    ): Result<GradeItem> {
        return try {
            val itemsRef = firestore.collection(COURSES_COLLECTION)
                .document(courseId)
                .collection(GRADE_ITEMS_SUBCOLLECTION)

            val currentCount = itemsRef.get().await().size()
            val newDocRef = itemsRef.document()

            val data = mapOf(
                NAME_FIELD to name,
                WEIGHT_FIELD to weight,
                TYPE_FIELD to type,
                ORDER_FIELD to currentCount,
                CREATED_AT_FIELD to Timestamp.now()
            )
            newDocRef.set(data).await()

            Result.success(GradeItem(newDocRef.id, name, weight, type, currentCount))
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(
                GradeError.Unknown(
                    e.message ?: "No se pudo crear el ítem de calificación"
                )
            )
        }
    }

    override suspend fun renameGradeItem(
        courseId: String,
        itemId: String,
        newName: String
    ): Result<Unit> {
        return try {
            firestore.collection(COURSES_COLLECTION)
                .document(courseId)
                .collection(GRADE_ITEMS_SUBCOLLECTION)
                .document(itemId)
                .update(NAME_FIELD, newName)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(GradeError.Unknown(e.message ?: "No se pudo renombrar el ítem"))
        }
    }

    override suspend fun countStudentsGraded(courseId: String, itemId: String): Result<Int> {
        return try {
            val snapshot = firestore.collection(GRADES_COLLECTION)
                .document(courseId)
                .collection(STUDENTS_SUBCOLLECTION)
                .get()
                .await()

            val count = snapshot.documents.count { doc ->
                parseGradesMap(doc.get(GRADES_FIELD)).containsKey(itemId)
            }
            Result.success(count)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(GradeError.Unknown(e.message ?: "No se pudo revisar el ítem"))
        }
    }

    override suspend fun deleteGradeItem(courseId: String, itemId: String): Result<Unit> {
        return try {
            val itemRef = firestore.collection(COURSES_COLLECTION)
                .document(courseId)
                .collection(GRADE_ITEMS_SUBCOLLECTION)
                .document(itemId)

            val gradedDocs = firestore.collection(GRADES_COLLECTION)
                .document(courseId)
                .collection(STUDENTS_SUBCOLLECTION)
                .get()
                .await()
                .documents
                .filter { parseGradesMap(it.get(GRADES_FIELD)).containsKey(itemId) }

            gradedDocs.chunked(MAX_BATCH_OPERATIONS).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { doc ->
                    batch.update(doc.reference, "$GRADES_FIELD.$itemId", FieldValue.delete())
                }
                batch.commit().await()
            }
            itemRef.delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(GradeError.Unknown(e.message ?: "No se pudo eliminar el ítem"))
        }
    }

    override suspend fun getMyGrades(courseId: String): Result<StudentGrades> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(GradeError.Unknown("Usuario no autenticado"))
        return getStudentGrades(courseId, uid)
    }

    override suspend fun getStudentGrades(
        courseId: String,
        studentUid: String
    ): Result<StudentGrades> {
        return try {
            val doc = firestore.collection(GRADES_COLLECTION)
                .document(courseId)
                .collection(STUDENTS_SUBCOLLECTION)
                .document(studentUid)
                .get()
                .await()

            Result.success(StudentGrades(studentUid, parseGradesMap(doc.get(GRADES_FIELD))))
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(
                GradeError.Unknown(
                    e.message ?: "No se pudieron cargar las calificaciones"
                )
            )
        }
    }

    override suspend fun setStudentGrade(
        courseId: String,
        studentUid: String,
        itemId: String,
        grade: Double
    ): Result<Unit> {
        return try {
            firestore.collection(GRADES_COLLECTION)
                .document(courseId)
                .collection(STUDENTS_SUBCOLLECTION)
                .document(studentUid)
                .set(mapOf(GRADES_FIELD to mapOf(itemId to grade)), SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(GradeError.Unknown(e.message ?: "No se pudo guardar la calificación"))
        }
    }

    override suspend fun deleteStudentGrade(
        courseId: String,
        studentUid: String,
        itemId: String
    ): Result<Unit> {
        return try {
            firestore.collection(GRADES_COLLECTION)
                .document(courseId)
                .collection(STUDENTS_SUBCOLLECTION)
                .document(studentUid)
                .update(mapOf("$GRADES_FIELD.$itemId" to FieldValue.delete()))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(GradeError.Unknown(e.message ?: "No se pudo eliminar la calificación"))
        }
    }

    override suspend fun getAllStudentsGrades(courseId: String): Result<Map<String, StudentGrades>> {
        return try {
            val snapshot = firestore.collection(GRADES_COLLECTION)
                .document(courseId)
                .collection(STUDENTS_SUBCOLLECTION)
                .get()
                .await()

            val result = snapshot.documents.associate { doc ->
                doc.id to StudentGrades(doc.id, parseGradesMap(doc.get(GRADES_FIELD)))
            }
            Result.success(result)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(
                GradeError.Unknown(
                    e.message ?: "No se pudieron cargar las calificaciones del curso"
                )
            )
        }
    }

    private fun parseGradesMap(raw: Any?): Map<String, Double> {
        val rawMap = raw as? Map<*, *> ?: emptyMap<Any, Any>()
        return rawMap.mapNotNull { (key, value) ->
            val itemId = key as? String ?: return@mapNotNull null
            val grade = (value as? Number)?.toDouble() ?: return@mapNotNull null
            itemId to grade
        }.toMap()
    }
}