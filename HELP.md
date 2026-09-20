# Manga Fetcher CLI

A CLI tool to download manga chapters and convert them to `.cbz` format.

## Prerequisites

- JDK 25+

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

## Commands

- `search <title> [-p <provider>]`: Search for manga (providers: `mangalivre`, `taosect`).
- `download <mangaId> (-c <chapterNumber> | --from <chapterNumber>) [-o <outputDir>] [--with-volume] [-p <provider>]`: Download chapters and convert them to CBZ.
- `rename <mangaId> [-o <outputDir>]`: Retroactively rename CBZ files to include volume information.
- `check [-o <outputDir>] [-d]`: Verify database entries against existing files (`-d` / `--delete-missing` auto-deletes orphaned records).
