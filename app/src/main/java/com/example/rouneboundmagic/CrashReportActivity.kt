package com.example.rouneboundmagic

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class CrashReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showCrashDialog()
    }

    private fun showCrashDialog() {
        val errorMessage = intent.getStringExtra(EXTRA_ERROR_MESSAGE) ?: "Άγνωστο σφάλμα"
        val stackTrace = intent.getStringExtra(EXTRA_STACK_TRACE) ?: "Δεν βρέθηκε διαθέσιμο stack trace."

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setMessage("Η εφαρμογή κατέρρευσε. Θέλεις να στείλεις μια αναφορά σφάλματος;")
            .setCancelable(false)
            .setPositiveButton("Αποστολή") { _, _ ->
                sendCrashEmail(errorMessage, stackTrace)
            }
            .setNegativeButton("Άκυρο") { _, _ ->
                Toast.makeText(
                    this,
                    "Η αναφορά σφάλματος δεν στάλθηκε.",
                    Toast.LENGTH_SHORT
                ).show()
                finishAffinity()
            }
            .show()
    }

    private fun sendCrashEmail(message: String, stackTrace: String) {
        val body = buildString {
            appendLine("Μήνυμα σφάλματος:")
            appendLine(message)
            appendLine()
            appendLine("Stack trace:")
            appendLine(stackTrace)
        }

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:ioannapergamali@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "App Crash Report")
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Αποστολή μέσω"))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "Δεν βρέθηκε εφαρμογή email για αποστολή.",
                Toast.LENGTH_LONG
            ).show()
        } finally {
            finishAffinity()
        }
    }

    companion object {
        const val EXTRA_ERROR_MESSAGE = "extra_error_message"
        const val EXTRA_STACK_TRACE = "extra_stack_trace"
    }
}
