# Tech Stack: Manga Fetcher CLI

## Language and Platform
- **Kotlin (JVM):** The primary programming language for the CLI.
- **GraalVM Native Image (Experimental):** Available as an alternative build target for high-performance, fast-startup binaries. Note: Playwright compatibility requires significant manual configuration.
- **Gradle (Kotlin DSL):** Build automation and dependency management.

## CLI Framework
- **Picocli:** A modern, feature-rich CLI framework.

## Network and Scrapping
- **Playwright:** A modern browser automation library for handling JavaScript-heavy sites and bypassing anti-bot protections.
- **Jsoup:** Still used for parsing HTML content when static snapshots are sufficient.

## Testing & Coverage
- **Jacoco:** For measuring and reporting test code coverage.

### Design Deviations
- **2026-04-12 (Playwright):** Switched from OkHttp to Playwright for the scrapper to ensure better reliability with JS-rendered content and sites using anti-bot protections like Cloudflare, which are common in the manga ecosystem. While this increases overhead and complicates GraalVM native image support, it's a necessary trade-off for core functionality.
- **2026-04-12 (Dual Distribution):** The project now supports two distribution paths:
    1. **Standard JVM (Default):** Reliable, easy to build, and fully compatible with Playwright. Distributed via a standard `Dockerfile` using `eclipse-temurin:25`.
    2. **GraalVM Native (Experimental):** Optimized for startup time and memory footprint. Distributed via `Dockerfile.native`. Requires ongoing maintenance of reflection and resource configurations.


## File Processing
- **Kotlin Zip Library (Standard):** Standard JVM libraries for creating and managing .cbz (ZIP) files.

## Future Expansion
- **Pluggable Architecture:** Designed to easily add new manga sources as modular components.
