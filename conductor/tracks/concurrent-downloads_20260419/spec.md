# Specification: Parallel Image Downloads

## Overview
This track aims to improve the performance of the Manga Fetcher CLI by parallelizing image downloads within a single chapter. Currently, images are fetched sequentially, leading to slow download times. We will use Kotlin Coroutines to orchestrate concurrent downloads.

## Functional Requirements
- **HTTP Client**: Use Playwright's `APIRequestContext` for fetching image bytes to bypass the overhead of creating a new `Page` (browser tab) for every image.
- **Concurrency Strategy**: Implement a `Semaphore`-based approach with Coroutines to limit concurrent image downloads (default limit: 5).
- **Error Handling**: If a single image download fails, the system must retry a few times. If it exhausts retries and still fails, the entire chapter download must fail.
- **Configurability**: 
  - Add a configurable concurrency limit (e.g., via CLI flag or config file).
  - Add configurable timeouts specifically for the image download requests.
  - Implement comprehensive debug logging for coroutine execution and HTTP status.

## Non-Functional Requirements
- **Performance**: Chapter download time should decrease significantly based on the concurrency limit.
- **Resource Usage**: Memory and CPU usage must remain stable and bounded; the application should not spawn excessive Playwright contexts or browser tabs.
- **Code Structure**: The `ImageDownloaderPort` and its implementations should be updated to support suspend functions or non-blocking concurrent flows.

## Acceptance Criteria
- [ ] Coroutines and a `Semaphore` control the parallel image downloads.
- [ ] `PlaywrightClient` utilizes `APIRequestContext` (e.g., `context.request().get()`) instead of `newPage()` for downloading raw files/images.
- [ ] A failing image triggers retries, and if unsuccessful, fails the chapter cleanly.
- [ ] Concurrency limit and timeouts are configurable.
- [ ] Debug logs show the coroutine execution trace.

## Out of Scope
- Parallelizing multiple chapter downloads concurrently.
- Fetching HTML pages (chapter lists, metadata) concurrently.