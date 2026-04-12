# Track: GH Actions Build & Docker Image Generation

## Overview
This track implements a robust CI/CD pipeline using GitHub Actions to automate the build, testing, and containerization of the Manga Fetcher CLI. The goal is to ensure code quality on every contribution and provide a ready-to-use Docker image for users and developers.

## Functional Requirements
- **Automated Workflow:** A GitHub Actions workflow (`.github/workflows/build-and-push.yml`).
- **Build & Test:**
    - Execute Gradle tests and static analysis on every push and Pull Request.
    - Build a GraalVM native binary optimized for the target platform.
- **Docker Image Generation:**
    - Create a multi-arch Docker image (supporting `linux/amd64` and `linux/arm64`).
    - The image must contain the GraalVM native binary for minimal footprint and fast startup.
- **Image Distribution:**
    - Push the generated Docker image to the GitHub Container Registry (GHCR) upon successful builds on the `main` branch or on tag creation.
- **Version Tagging:**
    - Tag Docker images with the Git commit SHA, branch name, and/or release version.

## Non-Functional Requirements
- **Performance:** GraalVM native builds can be slow; use caching (Gradle build cache, GraalVM dependencies) to optimize workflow duration.
- **Security:** Use short-lived GITHUB_TOKEN for registry authentication where possible.
- **Reliability:** Ensure Playwright dependencies are correctly handled in the build and Docker environments.

## Acceptance Criteria
- [ ] GitHub Actions workflow is triggered on pushes and PRs.
- [ ] Workflow successfully runs `./gradlew check` (tests and quality gates).
- [ ] Workflow successfully builds a GraalVM native binary.
- [ ] Multi-arch Docker image is built and pushed to GHCR (verified by manual inspection or log check).
- [ ] Docker image starts up and displays the CLI help menu correctly.

## Out of Scope
- Automated deployment to a production environment (e.g., K8s, ECS).
- Extensive integration testing against all manga sources in the CI environment (to avoid IP bans/rate limits).
