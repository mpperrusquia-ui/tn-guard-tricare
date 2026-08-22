package org.tnguardtricare.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.tnguardtricare.app.model.AppContent

private val Context.formDraftsDataStore by preferencesDataStore(name = "form_drafts")

/**
 * Single entry point for reading/writing form field drafts (Enrollment, Attestation, W-4).
 * Routes sensitive fields (SSN, bank routing/account) to SecureFieldStore and everything else
 * to plain DataStore, based on each field's `sensitive` flag in content.json — callers don't
 * need to know which backing store a field uses. Mirrors iOS's FormDraftStore.swift.
 */
class FormDraftStore(
    private val context: Context,
    private val secureFieldStore: SecureFieldStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _nonSensitiveValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val nonSensitiveValues: StateFlow<Map<String, String>> = _nonSensitiveValues.asStateFlow()

    init {
        scope.launch {
            context.formDraftsDataStore.data.collect { prefs ->
                _nonSensitiveValues.value = prefs.asMap().entries.associate { (k, v) -> k.name to v.toString() }
            }
        }
    }

    fun value(formId: String, field: AppContent.FormField): String {
        val key = draftKey(formId, field.id)
        return if (field.sensitive == true) {
            secureFieldStore.get(key) ?: ""
        } else {
            _nonSensitiveValues.value[key] ?: ""
        }
    }

    fun setValue(value: String, formId: String, field: AppContent.FormField) {
        val key = draftKey(formId, field.id)
        if (field.sensitive == true) {
            if (value.isEmpty()) secureFieldStore.delete(key) else secureFieldStore.set(key, value)
        } else {
            scope.launch {
                context.formDraftsDataStore.edit { prefs ->
                    val prefKey = stringPreferencesKey(key)
                    if (value.isEmpty()) prefs.remove(prefKey) else prefs[prefKey] = value
                }
            }
        }
    }

    fun clearAll(forms: List<AppContent.FormDefinition>) {
        for (form in forms) {
            for (field in form.fields) {
                if (field.sensitive == true) {
                    secureFieldStore.delete(draftKey(form.id, field.id))
                }
            }
        }
        scope.launch {
            context.formDraftsDataStore.edit { it.clear() }
        }
    }

    private fun draftKey(formId: String, fieldId: String) = "$formId.$fieldId"
}
