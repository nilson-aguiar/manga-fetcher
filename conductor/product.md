# Initial Concept
I want to have a webscrapper that fetch can fetch manga files from multiple sources but we'll start with a single option, and after downloading the images converts them into an .cbz file format. This should be an cli application. The application should be build using gradle and kotlin, I want it to use graalvm.

# Product Definition: Manga Fetcher CLI

## Vision
A high-performance CLI application designed for manga readers, collectors, and power users, enabling seamless manga downloading and conversion into standard .cbz formats.

## Target Audience
- **Manga Readers:** Seeking an easy way to download series for offline reading.
- **Collectors/Archivists:** Standardizing their manga collections into a unified format.
- **Developers/Power Users:** Utilizing a fast, GraalVM-powered CLI tool for automation.

## Key Goals
- **Reliability:** Consistent downloads from multiple sources.
- **CBZ Conversion:** Efficient image-to-.cbz conversion.
- **Performance:** Optimized execution via Kotlin and GraalVM.

## Core Features
- **Chapter Selection:** Search and select specific chapters for download.
- **Multiple Sources Support:** A pluggable architecture to support various manga sites, starting with one initial source.
- **Metadata Tagging:** Enriching downloaded files with relevant manga metadata.
- **CLI Native:** Fast startup and minimal overhead via GraalVM native images.
