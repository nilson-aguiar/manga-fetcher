# Tech Stack: Manga Fetcher CLI

## Language and Platform
- **Kotlin (JVM):** The primary programming language for the CLI.
- **GraalVM Native Image:** For high-performance, fast-startup native binaries.
- **Gradle (Kotlin DSL):** Build automation and dependency management.

## CLI Framework
- **Picocli (GraalVM Optimized):** A modern, feature-rich CLI framework specifically designed for GraalVM compatibility.

## Network and Scrapping
- **Playwright:** A modern browser automation library for handling JavaScript-heavy sites and bypassing anti-bot protections.
- **Jsoup:** Still used for parsing HTML content when static snapshots are sufficient.

## Testing & Coverage
- **Jacoco:** For measuring and reporting test code coverage.

### Design Deviations
- **2026-04-12:** Switched from OkHttp to Playwright for the scrapper to ensure better reliability with JS-rendered content and sites using anti-bot protections like Cloudflare, which are common in the manga ecosystem. While this increases overhead and complicates GraalVM native image support, it's a necessary trade-off for core functionality.

## File Processing
- **Kotlin Zip Library (Standard):** Standard JVM libraries for creating and managing .cbz (ZIP) files.

## Future Expansion
- **Pluggable Architecture:** Designed to easily add new manga sources as modular components.
