# Kotlin Code Style Guide

## Overview
This project strictly adheres to the official Kotlin coding conventions, enforced through **ktlint**, and follows code quality and security best practices recommended by **SonarQube**.

## 1. Formatting & Conventions (ktlint)
We use `ktlint` as our primary linter and formatter. Your code must pass `ktlint` checks before being merged.

*   **Indentation:** Use 4 spaces for standard indentation. Do not use tabs.
*   **Line Length:** Limit lines to 120 characters to ensure readability.
*   **Naming Conventions:**
    *   Classes and Interfaces: `PascalCase`
    *   Functions and Variables: `camelCase`
    *   Constants (val in companion object or top-level): `UPPER_SNAKE_CASE`
*   **Braces:** Use K&R style braces (opening brace on the same line).
*   **Spacing:** Ensure proper spacing around operators and after commas. Remove trailing whitespace.
*   **Imports:** Optimize imports. Avoid wildcard imports (e.g., `import java.util.*`) unless absolutely necessary. `ktlint` will flag unnecessary wildcard imports.
*   **Trailing Commas:** Use trailing commas in declarations and call sites for easier diffs and refactoring (supported in Kotlin 1.4+).

## 2. Code Quality & Security (SonarQube)
Beyond formatting, we adhere to SonarQube's rules for maintainability, reliability, and security.

*   **Null Safety:** Leverage Kotlin's null safety system. Avoid using the non-null assertion operator (`!!`) unless you can absolutely guarantee the value is not null and there's no better way to handle it. Prefer safe calls (`?.`), Elvis operator (`?:`), or smart casts.
*   **Immutability:** Prefer `val` over `var`. Use immutable collections (e.g., `listOf()`, `mapOf()`) whenever possible. State mutation should be tightly controlled and localized.
*   **Exception Handling:** Avoid swallowing exceptions. Catch specific exceptions rather than a generic `Exception` or `Throwable`. Always log or handle the exception appropriately.
*   **Cognitive Complexity:** Keep functions small and focused. SonarQube flags functions with high cognitive complexity. If a function does too many things, break it down into smaller, testable units.
*   **Code Duplication:** Adhere to the DRY (Don't Repeat Yourself) principle. Extract common logic into reusable functions or classes.
*   **Avoid Magic Numbers:** Extract literal numbers and strings into named constants with descriptive names.

## 3. Best Practices
*   **Data Classes:** Use `data class` for types that primary purpose is to hold data.
*   **Extension Functions:** Use extension functions judiciously to enhance existing types without inheriting from them, but avoid overusing them to the point where they obscure the origin of the logic.
*   **Coroutines:** Use coroutines for asynchronous programming. Ensure you are launching coroutines in the correct scope and handling cancellation properly.
*   **Testing:** Write unit tests for your business logic. Code must be testable.

*Reference: [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html), [ktlint Documentation](https://pinterest.github.io/ktlint/), [SonarQube Rules for Kotlin](https://rules.sonarsource.com/kotlin)*