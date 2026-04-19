package com.mangafetcher.downloader

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class CIConfigurationTest {
    @Test
    fun `github actions workflow files exist`() {
        assertTrue(File(".github/workflows/build-and-push.yml").exists(), "Standard workflow file should exist")
    }

    @Test
    fun `standard workflow contains gradlew check`() {
        val workflowFile = File(".github/workflows/build-and-push.yml")
        val content = workflowFile.readText()
        assertTrue(content.contains("./gradlew check"), "Standard workflow should contain './gradlew check'")
    }

    @Test
    fun `dockerfile exists`() {
        val dockerfile = File("Dockerfile")
        assertTrue(dockerfile.exists(), "Dockerfile should exist")
    }

    @Test
    fun `standard workflow references standard Dockerfile`() {
        val workflowFile = File(".github/workflows/build-and-push.yml")
        val content = workflowFile.readText()
        assertTrue(content.contains("file: Dockerfile"), "Standard workflow should build from Dockerfile")
    }

    @Test
    fun `workflows contain ghcr login`() {
        listOf(".github/workflows/build-and-push.yml").forEach { path ->
            val content = File(path).readText()
            assertTrue(content.contains("ghcr.io"), "Workflow $path should contain login for 'ghcr.io'")
            assertTrue(content.contains("GITHUB_TOKEN"), "Workflow $path should use 'GITHUB_TOKEN' for authentication")
        }
    }

    @Test
    fun `workflows contain metadata and push configuration`() {
        val stdContent = File(".github/workflows/build-and-push.yml").readText()
        assertTrue(stdContent.contains("docker/metadata-action"), "Standard workflow should contain 'docker/metadata-action'")
        assertTrue(
            stdContent.contains("push: \${{ github.event_name != 'pull_request' }}"),
            "Standard workflow should push only on non-PR events",
        )
    }
}
