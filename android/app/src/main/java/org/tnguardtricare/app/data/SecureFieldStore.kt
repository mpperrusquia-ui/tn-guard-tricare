package org.tnguardtricare.app.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores sensitive form field values (SSN, bank routing/account numbers) in
 * EncryptedSharedPreferences — encrypted at rest via the Android Keystore. Mirrors iOS's
 * KeychainStore.swift. Non-sensitive form fields (name, unit, dates) go through
 * FormDraftStore's plain DataStore path instead.
 */
class SecureFieldStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "org.tnguardtricare.app.sensitive_fields",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun set(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun get(key: String): String? = prefs.getString(key, null)

    fun delete(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun deleteAll() {
        prefs.edit().clear().apply()
    }
}
