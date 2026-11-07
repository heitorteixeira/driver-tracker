@file:OptIn(ExperimentalMaterial3Api::class)

package com.driverprofit.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.driverprofit.R

/** Where customer feedback is sent. Change this to your own address. */
const val FEEDBACK_EMAIL = "heitoret@gmail.com"

/**
 * A free-text box for suggestions and feature requests. The app has no internet permission,
 * so "Send" opens the user's email app with the message already filled in.
 */
@Composable
fun FeedbackScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var message by rememberSaveable { mutableStateOf("") }

    val subject = stringResource(R.string.feedback_subject)
    val chooserTitle = stringResource(R.string.feedback_chooser)
    val noEmailApp = stringResource(R.string.feedback_no_email_app)

    fun send() {
        val version = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "?"
        val body = buildString {
            append(message.trim())
            append("\n\n--\n")
            append("App $version, Android ${Build.VERSION.RELEASE}, ${Build.MODEL}")
        }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(FEEDBACK_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            context.startActivity(Intent.createChooser(intent, chooserTitle))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, noEmailApp, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_feedback)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.feedback_intro),
                style = MaterialTheme.typography.bodyLarge,
            )
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text(stringResource(R.string.feedback_hint)) },
                minLines = 6,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = ::send,
                enabled = message.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.feedback_send))
            }
        }
    }
}
