# Stage 1: Java Runtime
FROM eclipse-temurin:25-jre-jammy AS jre

# Stage 2: Final minimal image with Playwright dependencies and Java
FROM mcr.microsoft.com/playwright:v1.44.0-jammy

# Copy the JRE from the eclipse-temurin image
COPY --from=jre /opt/java/openjdk /opt/java/openjdk
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Set working directory
WORKDIR /app

# Copy the application distribution (pre-built by Gradle on the host)
COPY build/install/downloader /app/

# Create a downloads directory
RUN mkdir /app/downloads && chmod 777 /app/downloads

# Ensure the start script is executable
RUN chmod +x /app/bin/downloader

# Set the entrypoint to the generated start script
ENTRYPOINT ["/app/bin/downloader"]

# Default command
CMD ["--help"]
