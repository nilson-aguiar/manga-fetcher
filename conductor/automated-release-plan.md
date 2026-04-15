# Automated Release Creation Plan

## Objective
Implement an automated release process using Google's Release Please action and update the Docker CI pipelines to generate semantic version tags.

## Key Files & Context
- `.github/workflows/release-please.yml` (New file)
- `.github/workflows/build-and-push.yml`
- `.github/workflows/build-and-push-native.yml`

## Implementation Steps
1. **Create `release-please.yml` Workflow:**
   - Add a new GitHub Actions workflow that runs on pushes to `main`.
   - Use `googleapis/release-please-action@v4`.
   - Configure it to track Conventional Commits, generate a changelog, and create a Release PR. Once the PR is merged, it will automatically create a GitHub Release and a Git tag (e.g., `v1.2.3`).

2. **Update Docker Metadata (`build-and-push.yml`):**
   - Modify the `docker/metadata-action@v5` configuration to include semantic tags: `{{version}}`, `{{major}}.{{minor}}`, and `{{major}}`.
   - Add a rule to automatically tag as `latest` on `main` branch pushes.

3. **Update Docker Metadata (`build-and-push-native.yml`):**
   - Modify the `docker/metadata-action@v5` configuration to include semantic tags, matching the standard build.

## Verification & Testing
- Push a test commit following Conventional Commits format (e.g., `feat: add automated release generation`).
- Verify that a Release PR is automatically created by Release Please.
- Review the Release PR to ensure the changelog looks correct.
- Merge the Release PR and verify that a new Git Tag and GitHub Release are created.
- Ensure that the `build-and-push` workflow triggers on the new `v*` tag and publishes the Docker image with semantic version tags to GHCR.
