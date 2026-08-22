package org.tnguardtricare.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val Context.progressDataStore by preferencesDataStore(name = "progress")

enum class Track { TRS, REIMBURSEMENT }

/**
 * Tracks which checklist steps and forms the user has marked complete. Non-sensitive — just
 * step IDs — so it's stored in plain DataStore preferences. Sensitive form field values (SSN,
 * bank info) live in SecureFieldStore instead. Mirrors iOS's ProgressStore.swift.
 */
class ProgressStore(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val keyTrs = stringSetPreferencesKey("completed_trs_steps")
    private val keyReimbursement = stringSetPreferencesKey("completed_reimbursement_steps")
    private val keyForms = stringSetPreferencesKey("completed_forms")

    private val _completedTrsSteps = MutableStateFlow<Set<String>>(emptySet())
    val completedTrsSteps: StateFlow<Set<String>> = _completedTrsSteps.asStateFlow()

    private val _completedReimbursementSteps = MutableStateFlow<Set<String>>(emptySet())
    val completedReimbursementSteps: StateFlow<Set<String>> = _completedReimbursementSteps.asStateFlow()

    private val _completedForms = MutableStateFlow<Set<String>>(emptySet())
    val completedForms: StateFlow<Set<String>> = _completedForms.asStateFlow()

    init {
        scope.launch {
            context.progressDataStore.data.collect { prefs ->
                _completedTrsSteps.value = prefs[keyTrs] ?: emptySet()
                _completedReimbursementSteps.value = prefs[keyReimbursement] ?: emptySet()
                _completedForms.value = prefs[keyForms] ?: emptySet()
            }
        }
    }

    fun isStepComplete(id: String, track: Track): Boolean {
        val set = if (track == Track.TRS) _completedTrsSteps.value else _completedReimbursementSteps.value
        return set.contains(id)
    }

    fun toggleStep(id: String, track: Track) {
        val current = if (track == Track.TRS) _completedTrsSteps.value else _completedReimbursementSteps.value
        setStepComplete(id, track, !current.contains(id))
    }

    fun setStepComplete(id: String, track: Track, complete: Boolean) {
        scope.launch {
            context.progressDataStore.edit { prefs ->
                val key = if (track == Track.TRS) keyTrs else keyReimbursement
                val current = prefs[key] ?: emptySet()
                prefs[key] = if (complete) current + id else current - id
            }
        }
    }

    fun isFormComplete(id: String): Boolean = _completedForms.value.contains(id)

    fun setFormComplete(id: String, complete: Boolean) {
        scope.launch {
            context.progressDataStore.edit { prefs ->
                val current = prefs[keyForms] ?: emptySet()
                prefs[keyForms] = if (complete) current + id else current - id
            }
        }
    }

    fun progressFraction(track: Track, totalSteps: Int): Float {
        if (totalSteps <= 0) return 0f
        val completedCount = if (track == Track.TRS) _completedTrsSteps.value.size else _completedReimbursementSteps.value.size
        return (completedCount.toFloat() / totalSteps.toFloat()).coerceAtMost(1f)
    }

    fun resetAll() {
        scope.launch {
            context.progressDataStore.edit { it.clear() }
        }
    }
}
