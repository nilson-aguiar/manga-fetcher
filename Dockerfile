# Stage 1: Build application
FROM eclipse-temurin:25-jdk-jammy AS builder

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

# Build the standard JVM distribution (creates build/install/downloader)
RUN ./gradlew installDist --no-daemon

# Stage 2: Java Runtime
FROM eclipse-temurin:25-jre-jammy AS jre

# Stage 3: Final minimal image with Playwright dependencies and Java
FROM mcr.microsoft.com/playwright:v1.44.0-jammy

# Copy the JRE from the eclipse-temurin image
COPY --from=jre /opt/java/openjdk /opt/java/openjdk
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Set working directory
WORKDIR /app

# Copy the application distribution from the builder stage
COPY --from=builder /build/build/install/downloader /app/

# Create a downloads directory
RUN mkdir /app/downloads && chmod 777 /app/downloads

# Ensure the start script is executable
RUN chmod +x /app/bin/downloader

# Set the entrypoint to the generated start script
ENTRYPOINT ["/app/bin/downloader"]

# Default command
CMD ["--help"]
