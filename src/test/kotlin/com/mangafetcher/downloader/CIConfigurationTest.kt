package com.mangafetcher.downloader

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class CIConfigurationTest {

    @Test
    fun `github actions workflow file exists`() {
        val workflowFile = File(".github/workflows/build-and-push.yml")
        assertTrue(workflowFile.exists(), "Workflow file should exist at ${workflowFile.absolutePath}")
    }

    @Test
    fun `workflow contains gradlew check`() {
        val workflowFile = File(".github/workflows/build-and-push.yml")
        val content = workflowFile.readText()
        assertTrue(content.contains("./gradlew check"), "Workflow should contain './gradlew check'")
    }

    @Test
    fun `workflow contains nativeCompile`() {
        val workflowFile = File(".github/workflows/build-and-push.yml")
        val content = workflowFile.readText()
        assertTrue(content.contains("./gradlew nativeCompile"), "Workflow should contain './gradlew nativeCompile'")
    }
}
