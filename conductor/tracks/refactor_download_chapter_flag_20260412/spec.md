# Specification: Refactor Download Command - Chapter Flag

## Overview
The current `download` command uses a positional argument for the chapter ID (slug). This track will refactor the command to use a `-c` or `--chapter` flag for specifying a chapter number, improving the CLI's user-friendliness and consistency.

## Functional Requirements
1.  **Change Command Signature**: The `download` command will now expect:
    `download <manga-id> -c <chapter-number> [-o <output-dir>]`
2.  **Manga ID as Positional**: `<manga-id>` remains a required positional argument at index 0.
3.  **Chapter Number Flag**:
    -   Add `-c` and `--chapter` options.
    -   The value should be a chapter number (e.g., `1`, `10.5`).
4.  **Remove Positional Chapter ID**: The current positional argument at index 1 for the chapter ID (slug) will be removed.
5.  **Exclusivity**: The `-c` flag and the existing `--from` flag will be mutually exclusive.
6.  **Matching Logic**: The `-c <chapter-number>` will match the numeric part of the chapter's "number" field (e.g., `-c 200` matches "Capítulo 200").

## Non-Functional Requirements
1.  **Picocli Integration**: Use Picocli's features to handle options and mutual exclusivity.
2.  **Error Handling**: Provide a clear error message if neither `-c` nor `--from` is provided, or if both are used.

## Acceptance Criteria
-   `manga-fetcher download solo-leveling -c 1` works correctly.
-   `manga-fetcher download solo-leveling -c 1 -o downloads` works correctly.
-   `manga-fetcher download solo-leveling capitulo-00` is no longer valid.
-   `manga-fetcher download solo-leveling -c 1 --from 5` results in an error.
-   `manga-fetcher download solo-leveling` results in an error (missing required selection).

## Out of Scope
-   Adding new download range features (like `-t` or `--to`).
-   Changing the scrapper's parsing logic beyond what's needed for matching numbers.