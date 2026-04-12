# Tech Stack: Manga Fetcher CLI

## Language and Platform
- **Kotlin (JVM):** The primary programming language for the CLI.
- **GraalVM Native Image:** For high-performance, fast-startup native binaries.
- **Gradle (Kotlin DSL):** Build automation and dependency management.

## CLI Framework
- **Picocli (GraalVM Optimized):** A modern, feature-rich CLI framework specifically designed for GraalVM compatibility.

## Network and Scrapping
- **OkHttp:** A robust and efficient HTTP client for handling scrapper requests.

## File Processing
- **Kotlin Zip Library (Standard):** Standard JVM libraries for creating and managing .cbz (ZIP) files.

## Future Expansion
- **Pluggable Architecture:** Designed to easily add new manga sources as modular components.
