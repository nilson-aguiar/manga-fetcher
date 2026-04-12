# Implementation Plan: GH Actions Build & Docker Image Generation

## Phase 1: CI Workflow Foundation [checkpoint: 400ac62]
- [x] **Task: Setup GitHub Actions YAML structure** b00350d
    - [ ] Create `.github/workflows/build-and-push.yml` with basic triggers.
    - [ ] Configure `setup-java` (GraalVM distribution).
    - [ ] Implement Gradle caching for faster builds.
- [x] **Task: Implement Test Execution in CI** 119e908
    - [ ] Add step to run `./gradlew check` (tests and quality gates).
    - [ ] Handle Playwright dependencies (e.g., `playwright install`).
- [x] **Task: Conductor - User Manual Verification 'Phase 1: CI Workflow Foundation' (Protocol in workflow.md)** 400ac62

## Phase 2: GraalVM Native Image Build [checkpoint: bb57098]
- [x] **Task: Configure GraalVM Native Image Compilation** ff18186
    - [ ] Add `native-image` build step to the workflow.
    - [ ] Verify native binary generation on standard Linux runner.
- [x] **Task: Conductor - User Manual Verification 'Phase 2: GraalVM Native Image Build' (Protocol in workflow.md)** bb57098

## Phase 3: Dockerization & Multi-arch Support [checkpoint: f510a8d]
- [x] **Task: Create Dockerfiles (Standard & Native)** 32f3d8f
    - [x] Write a multi-stage `Dockerfile` (Standard JVM).
    - [x] Write a multi-stage `Dockerfile-native` (GraalVM Native Image).
- [x] **Task: Configure Multi-arch Build with Buildx** 18e3024
    - [ ] Add `docker/setup-qemu-action` and `docker/setup-buildx-action` to the workflow.
    - [ ] Implement the build step for both `Dockerfile` and `Dockerfile-native`.
- [x] **Task: Conductor - User Manual Verification 'Phase 3: Dockerization & Multi-arch Support' (Protocol in workflow.md)** f510a8d

## Phase 4: GHCR Push and Release Tags [checkpoint: cd8d663]
- [x] **Task: Implement Registry Authentication** 04ad0b1
    - [ ] Add login step for GHCR using `GITHUB_TOKEN`.
- [x] **Task: Implement Image Tagging & Push** bc63393
    - [ ] Configure `docker/metadata-action` for dynamic tagging (commit SHA, branch, tags).
    - [ ] Update build step to push images on `main` branch or tag creation.
- [x] **Task: Conductor - User Manual Verification 'Phase 4: GHCR Push and Release Tags' (Protocol in workflow.md)** cd8d663

## Phase 5: Final Validation
- [x] **Task: Verify Docker Image Execution** [manual]
    - [ ] Add a post-build step or manual verification to run the built container and check the CLI output.
- [ ] **Task: Conductor - User Manual Verification 'Phase 5: Final Validation' (Protocol in workflow.md)**
