package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.AiAppGenerator
import com.example.data.apk.ApkBuilderEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AI APK Builder", appName)
    }

    @Test
    fun `synthesize app from prompt generates valid project and components`() {
        val result = AiAppGenerator.synthesizeApp("Cyberpunk Soundboard with drum triggers and bass filters")
        assertNotNull(result.project)
        assertTrue(result.components.isNotEmpty())
        assertTrue(result.project.name.isNotBlank())
    }

    @Test
    fun `apk engine builds valid apk archive`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val synth = AiAppGenerator.synthesizeApp("Test Calculator")
        var completed = false
        val apkFile = ApkBuilderEngine.buildApk(context, synth.project, synth.components) { progress ->
            if (progress.isComplete) completed = true
        }
        assertTrue(completed)
        assertTrue(apkFile.exists())
        assertTrue(apkFile.length() > 0)
    }
}
