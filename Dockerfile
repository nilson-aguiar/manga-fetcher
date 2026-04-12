# Stage 2: Final minimal image with Playwright dependencies
FROM mcr.microsoft.com/playwright:v1.44.0-jammy

# Set working directory
WORKDIR /app

# Copy the native binary from the build context
# (This assumes the binary is built before running docker build)
COPY build/native/nativeCompile/manga-fetcher /app/manga-fetcher

# Ensure the binary is executable
RUN chmod +x /app/manga-fetcher

# Create a downloads directory
RUN mkdir /app/downloads && chmod 777 /app/downloads

# Set the entrypoint
ENTRYPOINT ["/app/manga-fetcher"]

# Default command
CMD ["--help"]
