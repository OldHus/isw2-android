package com.svape.unal.ingesoftii.presentation.syllabus

import android.graphics.Bitmap

sealed class SyllabusUiState {
    data object  Loading : SyllabusUiState()
    data class  Success (val pages: List<Bitmap>) : SyllabusUiState()
    data object NotAvailable : SyllabusUiState()
    data class Error(val message: String) : SyllabusUiState()
}