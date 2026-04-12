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
}
