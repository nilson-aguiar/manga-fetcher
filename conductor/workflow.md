# Project Workflow

This document outlines the standard development workflow and processes for this project.

## 1. Task Execution & Commits
- **Commit Frequency:** Commits should be made **Per track**. Do not commit after every single task or phase unless specifically requested or if it makes sense for a significant milestone.
- **Task Summaries:** Task summaries must be recorded in the **Commit Messages**. Ensure that your commit message body provides a clear, concise summary of the work completed.

## 2. Quality & Testing
- **Test Coverage:** All new code and modifications must maintain a minimum test code coverage of **80%**.
- **Test-Driven Development (TDD):** Where applicable, follow TDD practices. Write tests for new features and bug fixes before implementing the core logic.
- **Code Style:** Ensure all code adheres to the project's defined style guides (e.g., `ktlint` and SonarQube rules for Kotlin) before finalizing a track.

## 3. Phase Completion Verification and Checkpointing Protocol
Before concluding any major phase of a track, the following verification steps must be performed:
1. Ensure all tasks within the phase are marked as completed.
2. Verify that test coverage requirements (80%) are met.
3. Validate that the code passes all formatting and linting checks.
4. If this is the final phase of a track, prepare the comprehensive commit message containing the task summaries as defined above.