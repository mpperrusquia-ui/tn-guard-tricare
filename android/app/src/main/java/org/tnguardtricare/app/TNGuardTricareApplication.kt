package org.tnguardtricare.app

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import org.tnguardtricare.app.data.ContentRepository
import org.tnguardtricare.app.data.FormDraftStore
import org.tnguardtricare.app.data.ProgressStore
import org.tnguardtricare.app.data.SecureFieldStore

/**
 * Simple manual DI container — small enough app that Hilt would be more ceremony than value.
 * Mirrors how the iOS app wires its stores via @StateObject in the App entry point.
 */
class TNGuardTricareApplication : Application() {
    lateinit var contentRepository: ContentRepository
        private set
    lateinit var progressStore: ProgressStore
        private set
    lateinit var secureFieldStore: SecureFieldStore
        private set
    lateinit var formDraftStore: FormDraftStore
        private set

    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(applicationContext)
        contentRepository = ContentRepository(this)
        progressStore = ProgressStore(this)
        secureFieldStore = SecureFieldStore(this)
        formDraftStore = FormDraftStore(this, secureFieldStore)
    }
}
