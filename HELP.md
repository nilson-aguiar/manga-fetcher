# Manga Fetcher CLI

A CLI tool to download manga chapters and convert them to `.cbz` format.

## Prerequisites

- JDK 24+
- (Optional) GraalVM with `native-image` for native builds.

## Quick Start

### Build

```bash
./gradlew build
```

### Run

Search for a manga:
```bash
./gradlew run --args="search 'Solo Leveling'"
```

Download a chapter (example):
```bash
./gradlew run --args="download solo-leveling -c 00 -o downloads"
```

## Native Binary

To build a native binary:

```bash
./gradlew nativeCompile
```

The binary will be located at `build/native/nativeCompile/manga-fetcher`.

## Commands

- `search <title>`: Search for manga on MangaLivre.
- `download <mangaId> -c <chapterNumber> [-o <outputDir>]`: Download a specific chapter and convert it to CBZ.
- `download <mangaId> --from <chapterNumber> [-o <outputDir>]`: Download all chapters starting from a specific one.
