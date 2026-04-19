# Specification: Enhance error handling and implement configurable JSON logging

## Objective
To improve the reliability and observability of the Manga Fetcher CLI. This track addresses the need for robust error handling during bulk download operations and provides an alternative, structured JSON logging output for automated consumption.

## Context
As defined in the project guidelines:
- **Resilient & Retry:** Operations must log errors gracefully, attempt reasonable retries for network issues, and continue processing the next chapter/manga instead of failing fast.
- **Configurable Outputs:** Provide a flag (e.g., `--json`) to toggle standard colorful text output to a structured JSON format, enabling easier ingestion by external monitoring tools.

## Requirements
1. **Retry Mechanism:**
   - Integrate a configurable retry strategy for network operations (e.g., HTTP requests in `ResilientHttpClient` or Playwright navigation logic).
   - Log retry attempts and failures without abruptly terminating the CLI execution.
2. **Error Recovery for Bulk Downloads:**
   - In cases of unrecoverable errors for a specific chapter, the application should flag the chapter as failed in the local SQLite database and proceed to the next item.
3. **Structured JSON Logging:**
   - Implement an optional JSON appender or formatter for SLF4J (or custom logger) that outputs log statements as JSON.
   - Add a `--json-log` CLI argument via Picocli to activate the JSON output formatter.

## Acceptance Criteria
- Failed network requests are retried a minimum of 3 times with exponential backoff.
- The CLI completes its execution over a batch of manga even if one or more chapters fail to download.
- Providing `--json-log` results in standard output displaying logs as valid, structured JSON strings.