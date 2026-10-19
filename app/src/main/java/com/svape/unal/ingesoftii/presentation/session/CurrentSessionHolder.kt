package com.svape.unal.ingesoftii.presentation.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class CurrentSession(val role: String, val courseId: String)

class CurrentSessionHolder {
    private val _current = MutableStateFlow<CurrentSession?>(null)
    val current: StateFlow<CurrentSession?> = _current

    fun update(role: String, courseId: String) {
        _current.value = CurrentSession(role, courseId)
    }

    fun clear() {
        _current.value = null
    }
}