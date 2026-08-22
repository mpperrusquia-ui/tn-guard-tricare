package org.tnguardtricare.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import org.tnguardtricare.app.ui.screens.disclaimer.DisclaimerScreen
import org.tnguardtricare.app.ui.theme.TNGuardTricareTheme

private const val PREFS_NAME = "org.tnguardtricare.app.prefs"
private const val KEY_HAS_SEEN_DISCLAIMER = "has_seen_disclaimer"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as TNGuardTricareApplication

        setContent {
            TNGuardTricareTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot(app)
                }
            }
        }
    }
}

@Composable
private fun AppRoot(app: TNGuardTricareApplication) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val content by app.contentRepository.content.collectAsState()

    var hasSeenDisclaimer by remember { mutableStateOf(readHasSeenDisclaimer(context)) }

    LaunchedEffect(Unit) {
        scope.launch { app.contentRepository.load() }
    }

    when {
        content == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        !hasSeenDisclaimer -> {
            DisclaimerScreen(
                disclaimer = content!!.disclaimer,
                onContinue = {
                    writeHasSeenDisclaimer(context)
                    hasSeenDisclaimer = true
                },
            )
        }
        else -> {
            RootNavHost(app)
        }
    }
}

private fun readHasSeenDisclaimer(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_HAS_SEEN_DISCLAIMER, false)
}

private fun writeHasSeenDisclaimer(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(KEY_HAS_SEEN_DISCLAIMER, true).apply()
}
