package org.tnguardtricare.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.tnguardtricare.app.model.AppContent
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Loads app content with a "bundled first, remote refresh in background" strategy so the app
 * always renders instantly and never blocks on network — mirrors iOS's ContentStore.swift.
 * Hosted on GitHub: edit content/content.json in the tn-guard-tricare repo (bump
 * contentVersion) and installed apps pick it up on next launch, no Play Store release needed.
 */
class ContentRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    private val remoteUrl =
        "https://raw.githubusercontent.com/mpperrusquia-ui/tn-guard-tricare/main/content/content.json"

    private val _content = MutableStateFlow<AppContent?>(null)
    val content: StateFlow<AppContent?> = _content.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastRefreshError = MutableStateFlow<String?>(null)
    val lastRefreshError: StateFlow<String?> = _lastRefreshError.asStateFlow()

    private val cacheFile: File
        get() = File(context.cacheDir, "content-cache.json")

    suspend fun load() {
        if (_content.value == null) {
            _content.value = loadCached() ?: loadBundled()
        }
        refreshFromRemote()
    }

    suspend fun refreshFromRemote() {
        _isRefreshing.value = true
        try {
            withContext(Dispatchers.IO) {
                val connection = URL(remoteUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                connection.inputStream.use { stream ->
                    val text = stream.bufferedReader().readText()
                    val decoded = json.decodeFromString<AppContent>(text)
                    val current = _content.value
                    if (current == null || decoded.contentVersion >= current.contentVersion) {
                        _content.value = decoded
                        cacheFile.writeText(text)
                    }
                }
            }
            _lastRefreshError.value = null
        } catch (e: Exception) {
            _lastRefreshError.value = e.message ?: "Couldn't refresh content"
        } finally {
            _isRefreshing.value = false
        }
    }

    private fun loadCached(): AppContent? {
        return try {
            if (!cacheFile.exists()) return null
            json.decodeFromString<AppContent>(cacheFile.readText())
        } catch (e: Exception) {
            null
        }
    }

    private fun loadBundled(): AppContent? {
        return try {
            val text = context.assets.open("content.json").bufferedReader().use { it.readText() }
            json.decodeFromString<AppContent>(text)
        } catch (e: Exception) {
            null
        }
    }
}
