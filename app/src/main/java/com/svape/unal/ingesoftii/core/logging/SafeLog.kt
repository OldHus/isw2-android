package com.svape.unal.ingesoftii.core.logging

import android.util.Log
import com.svape.unal.ingesoftii.BuildConfig

object SafeLog {

    private const val MAX_LENGTH = 200

    private val EMAIL = Regex("""[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}""")
    private val JWT = Regex("""eyJ[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]*""")
    private val BEARER = Regex("""(?i)(bearer|token|nonce|key|secret|password)\s*[:=]\s*\S+""")
    private val LONG_SECRET = Regex("""[A-Za-z0-9_\-+/=]{32,}""")

    fun sanitize(text: String?, maxLength: Int = MAX_LENGTH): String {
        if (text.isNullOrBlank()) return "sin mensaje"
        return text
            .replace(JWT, "[jwt]")
            .replace(EMAIL, "[email]")
            .replace(BEARER) { "${it.groupValues[1]}=[redacted]" }
            .replace(LONG_SECRET, "[redacted]")
            .replace('\n', ' ')
            .trim()
            .take(maxLength)
    }

    fun describe(error: Throwable): String =
        "${error.javaClass.simpleName}: ${sanitize(error.message)}"

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, sanitize(message, 1000))
    }

    fun w(tag: String, message: String, error: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, sanitize(message, 1000), error)
        } else {
            Log.w(tag, oneLine(message, error))
        }
    }

    fun e(tag: String, message: String, error: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, sanitize(message, 1000), error)
        } else {
            Log.e(tag, oneLine(message, error))
        }
    }

    private fun oneLine(message: String, error: Throwable?): String =
        if (error == null) sanitize(message) else "${sanitize(message)} | ${describe(error)}"
}

class SanitizedException(original: Throwable) : Exception(
    SafeLog.describe(original),
    original.cause?.takeIf { it !== original }?.let { SanitizedException(it) },
) {
    init {
        stackTrace = original.stackTrace
    }
}