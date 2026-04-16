# CI Optimization and Release Fix Plan

## Objective
1. **Speed up standard multi-architecture builds:** The standard image build takes >10m because Gradle is compiling Java code inside an emulated `arm64` container (QEMU). Since Java bytecode is platform-independent, we can compile it natively *once* on the fast `amd64` GitHub runner, and just copy the compiled files into both `amd64` and `arm64` Docker images. This will drastically reduce build time while keeping both architectures.
2. **Fix missing semantic tags:** Your release was created successfully by `release-please`, but GitHub Actions intentionally does not trigger workflows from tags created by the default `GITHUB_TOKEN` (to prevent infinite loops). As a result, the Docker pipeline didn't run for the new `v1.x.x` tag, and the image only received the `main` and `latest` tags.

## Implementation Steps

### Part 1: Optimize Standard Docker Build
1. **Update `Dockerfile`:** Remove the `builder` stage. Have the Dockerfile simply `COPY` the pre-built `build/install/downloader` directory into the final image.
2. **Update `build-and-push.yml`:** 
   - Add a step to run `./gradlew installDist` *before* the Docker build.
   - Configure `docker/build-push-action@v6` to use GitHub Actions cache (`cache-from/cache-to`) to cache the Playwright base image.
   - Keep `platforms: linux/amd64,linux/arm64`. Because compilation is now outside Docker, building for `arm64` will be lightning fast (it will just be copying files instead of running Gradle in an emulator).

### Part 2: Fix Release Tag Triggers (Combined Workflow)
Since GitHub Actions prevents the `release` or `push: tags` event from firing without a PAT, the most elegant solution is to add `release-please` directly into your `build-and-push.yml` workflow.
1. We will delete `.github/workflows/release-please.yml` and move its step into `build-and-push.yml` as the first job.
2. The Docker build job will run *after* `release-please`. 
3. If `release-please` creates a release, it outputs the tag name (e.g., `v1.0.0`). We will pass this tag to `docker/metadata-action` so your Docker images get the correct semantic tags (`1.0.0`, `1.0`, `1`). If no release is created, it will just tag as `main` and `latest`.

## Verification
- The standard Docker build will drop from >10 minutes to ~2-3 minutes.
- When a new release PR is merged, the `release-please` job in `build-and-push.yml` will create the release and pass the semantic version directly to the Docker build job!