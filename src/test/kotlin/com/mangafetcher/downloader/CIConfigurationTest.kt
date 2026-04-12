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

    @Test
    fun `dockerfile exists`() {
        val dockerfile = File("Dockerfile")
        assertTrue(dockerfile.exists(), "Dockerfile should exist")
    }

    @Test
    fun `workflow contains buildx and build-push actions`() {
        val workflowFile = File(".github/workflows/build-and-push.yml")
        val content = workflowFile.readText()
        assertTrue(content.contains("docker/setup-buildx-action"), "Workflow should contain 'docker/setup-buildx-action'")
        assertTrue(content.contains("docker/build-push-action"), "Workflow should contain 'docker/build-push-action'")
    }
}
