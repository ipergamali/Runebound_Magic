package com.example.rouneboundmagic

import android.app.Application
import android.content.Intent
import android.util.Log

class CrashHandlerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (!handleUncaughtException(throwable)) {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun handleUncaughtException(throwable: Throwable): Boolean {
        val errorMessage = throwable.message?.takeIf { it.isNotBlank() }
            ?: throwable::class.simpleName
            ?: "Άγνωστο σφάλμα"
        val stackTrace = Log.getStackTraceString(throwable)

        val crashIntent = Intent(this, CrashReportActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra(CrashReportActivity.EXTRA_ERROR_MESSAGE, errorMessage)
            putExtra(CrashReportActivity.EXTRA_STACK_TRACE, stackTrace)
        }

        return runCatching {
            startActivity(crashIntent)
        }.isSuccess
    }
}
