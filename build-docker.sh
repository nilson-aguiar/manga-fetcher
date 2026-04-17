#!/bin/bash
set -e

echo "=== Building Manga Fetcher Docker Image ==="
echo ""

# Build the application using Gradle
echo "Step 1/2: Building application with Gradle..."
./gradlew installDist --no-daemon

# Build Docker image
echo ""
echo "Step 2/2: Building Docker image..."
docker build -t manga-fetcher:latest -f Dockerfile .

echo ""
echo "=== Build Complete! ==="
echo ""
echo "Test the image:"
echo "  docker run --rm manga-fetcher:latest --help"
echo ""
echo "Search for manga:"
echo '  docker run --rm manga-fetcher:latest search "one punch man"'
echo ""
echo "Download manga (example):"
echo '  docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \'
echo '    download one-punch-man 1-3'
echo ""
echo "Or use docker-compose:"
echo "  docker-compose run --rm manga-fetcher --help"
