package com.svape.unal.ingesoftii.presentation.syllabus

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.model.SyllabusError
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.usecase.GetSyllabusFileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import androidx.core.graphics.createBitmap


private const val RENDER_SCALE = 2

class SyllabusViewModel(
    private val getSyllabusFileUiState: GetSyllabusFileUseCase,
    private val analyticsReporter: AnalyticsReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow<SyllabusUiState>(SyllabusUiState.Loading)
    val uiState: StateFlow<SyllabusUiState> = _uiState.asStateFlow()

    fun loadSyllabus(courseId: String) {
        _uiState.value = SyllabusUiState.Loading
        viewModelScope.launch {
            getSyllabusFileUiState(courseId)
                .onSuccess { file ->
                    val pages = renderPdfToBitmaps(file)
                    analyticsReporter.logEvent(
                        name = "syllabus_viewed",
                        params = mapOf("course_id" to courseId, "page_count" to pages.size)
                    )
                    _uiState.value = SyllabusUiState.Success(pages)
                }
                .onFailure { error ->
                    _uiState.value = when (error) {
                        is SyllabusError.NotAvailable -> {
                            analyticsReporter.logEvent(
                                name = "syllabus_not_available",
                                params = mapOf("course_id" to courseId)
                            )
                            SyllabusUiState.NotAvailable
                        }

                        else -> SyllabusUiState.Error("No se pudo cargar el syllabus, intente otra vez")
                    }

                }
        }
    }

    private suspend fun renderPdfToBitmaps(file: File): List<Bitmap> {
        return withContext(Dispatchers.IO) {
            val fileDescriptor =
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)

            val bitmaps = mutableListOf<Bitmap>()
            for (pageIndex in 0 until renderer.pageCount) {
                renderer.openPage(pageIndex).use { page ->
                    val bitmap = createBitmap(page.width * RENDER_SCALE, page.height * RENDER_SCALE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmaps.add(bitmap)
                }
            }

            renderer.close()
            fileDescriptor.close()
            bitmaps
        }
    }

}