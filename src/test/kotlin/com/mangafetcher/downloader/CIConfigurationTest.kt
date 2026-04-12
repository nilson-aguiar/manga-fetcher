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
    fun `dockerfile exists`() {
        val dockerfile = File("Dockerfile")
        assertTrue(dockerfile.exists(), "Dockerfile should exist")
    }

    @Test
    fun `workflow references both Dockerfiles`() {
        val workflowFile = File(".github/workflows/build-and-push.yml")
        val content = workflowFile.readText()
        assertTrue(content.contains("file: Dockerfile"), "Workflow should build from Dockerfile")
        assertTrue(content.contains("file: Dockerfile-native"), "Workflow should build from Dockerfile-native")
    }

    @Test
    fun `native dockerfile exists`() {
        val dockerfile = File("Dockerfile-native")
        assertTrue(dockerfile.exists(), "Dockerfile-native should exist")
    }

    @Test
    fun `workflow contains ghcr login`() {
        val workflowFile = File(".github/workflows/build-and-push.yml")
        val content = workflowFile.readText()
        assertTrue(content.contains("ghcr.io"), "Workflow should contain login for 'ghcr.io'")
        assertTrue(content.contains("GITHUB_TOKEN"), "Workflow should use 'GITHUB_TOKEN' for authentication")
    }
}
