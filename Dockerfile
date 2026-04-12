# Stage 1: Build native image
FROM ghcr.io/graalvm/native-image-community:21 AS builder

# Install Gradle dependencies
WORKDIR /build
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Download dependencies
RUN ./gradlew build -x test --no-daemon || true

# Copy source code
COPY src src

# Build native image
RUN ./gradlew nativeCompile --no-daemon

# Stage 2: Final minimal image with Playwright dependencies
FROM mcr.microsoft.com/playwright:v1.44.0-jammy

# Set working directory
WORKDIR /app

# Copy the native binary from the builder stage
COPY --from=builder /build/build/native/nativeCompile/manga-fetcher /app/manga-fetcher

# Ensure the binary is executable
RUN chmod +x /app/manga-fetcher

# Create a downloads directory
RUN mkdir /app/downloads && chmod 777 /app/downloads

# Set the entrypoint
ENTRYPOINT ["/app/manga-fetcher"]

# Default command
CMD ["--help"]
