# Initial Concept
Manga Fetcher CLI is a powerful command-line interface tool for downloading manga chapters from various providers and converting them into `.cbz` format, optimized for e-readers and tablet viewing.

# Product Guide

## Target Audience
- **Media Server Owners:** Users who host their own comic libraries using platforms like Komga or Kavita and need a reliable way to source and populate their collections with high-quality CBZ files.

## Core Features & Focus
- **Broad Provider Support:** Prioritizing the addition and maintenance of multiple manga source providers (currently including `mangalivre` and `taosect`) to ensure a wide selection of content.
- **Rich Metadata Generation:** Generating comprehensive `ComicInfo.xml` metadata for maximum compatibility and seamless organization within library managers.
- **Automated Workflows:** Designed to be used via scripts or cron jobs for bulk, scheduled downloads, making it easy to keep local libraries up-to-date automatically.

## Architecture & Integration
- **Clean Architecture / Hexagonal:** Separates core domain logic from infrastructure details like scrapers, persistence (SQLite), and conversion.
- **Container-Ready:** Includes Docker and Docker Compose support for isolated execution and easy deployment in automated environments.