# Product Guidelines

## CLI Interaction & Tone
- **Informative & Verbose:** The CLI should provide clear visibility into its operations. Use progress bars, detailed info logs, and step-by-step execution feedback so users know exactly what is happening during potentially long-running downloads.

## Error Handling UX
- **Resilient & Retry:** For bulk operations or unexpected network issues, the application should not fail-fast. Instead, it should log the error gracefully, attempt reasonable retries, and proceed to the next chapter or manga to maximize the success of automated batches.

## Logging & Output Formatting
- **Configurable Outputs:** By default, output should be colorful and human-readable, optimized for standard terminal viewing. There must be an option (e.g., via a flag) to switch the log output to structured JSON format to support integration with other tools or automated parsing.