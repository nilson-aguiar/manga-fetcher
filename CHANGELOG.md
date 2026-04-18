# Changelog

## [1.1.0](https://github.com/nilson-aguiar/manga-fetcher/compare/v1.0.0...v1.1.0) (2026-04-17)


### Features

* add new provider + docs ([0e02b67](https://github.com/nilson-aguiar/manga-fetcher/commit/0e02b679085efb0343059184a713876747d079a1))
* create composite work for download and metadata ([f0af937](https://github.com/nilson-aguiar/manga-fetcher/commit/f0af9378c631b64c369802b08608a7a3173a38e8))
* **docker:** Add Docker compose setup with documentation ([f019df4](https://github.com/nilson-aguiar/manga-fetcher/commit/f019df493ae7aa191d5cc6058fc2de05a5c58018))
* fetch metadata, add cover and db for downloads ([6f3a670](https://github.com/nilson-aguiar/manga-fetcher/commit/6f3a67099019cccaf54c8365473a9d19860be513))
* improve tests ([a175fdf](https://github.com/nilson-aguiar/manga-fetcher/commit/a175fdf4ef0a200be2baf4da08546c7565258875))
* **taosect:** Add logging, caching, and provider selection ([8bb1b67](https://github.com/nilson-aguiar/manga-fetcher/commit/8bb1b67e087d5daa2176cc0d48722352b3f8c4df))


### Bug Fixes

* add browser args ([c0c7b39](https://github.com/nilson-aguiar/manga-fetcher/commit/c0c7b3991d18374e8a1e7c59103edd635601258f))
* metadata fetching ([b1d2468](https://github.com/nilson-aguiar/manga-fetcher/commit/b1d2468dc3975f5a82a3e0593eab2d8c5df479e8))
* remove deprecated methods and fix image generation ([541e7fb](https://github.com/nilson-aguiar/manga-fetcher/commit/541e7fb61bd449fc6e49313125c3b1e5430e2ae9))

## 1.0.0 (2026-04-15)


### Features

* add automated release ([f35a8a8](https://github.com/nilson-aguiar/manga-fetcher/commit/f35a8a88c569b118198c805c59420100bf0af95a))
* **cbz:** Implement CbzConverter utility ([0d859ef](https://github.com/nilson-aguiar/manga-fetcher/commit/0d859efd8c1e0ddf688ff2e4cbccc13ed8097801))
* **ci:** Add GraalVM native image build step to CI workflow ([ff18186](https://github.com/nilson-aguiar/manga-fetcher/commit/ff1818686f3d4af38e7e40394172459dcc3d684c))
* **ci:** Configure multi-arch Docker builds with Buildx and QEMU ([ba7a62a](https://github.com/nilson-aguiar/manga-fetcher/commit/ba7a62aeab36c097ca46bec1880ef0d7b7f2eb1d))
* **ci:** Create Dockerfile for the native CLI application ([43a1e72](https://github.com/nilson-aguiar/manga-fetcher/commit/43a1e720add50cf6b10110af335056f4362174f9))
* **ci:** Implement dynamic image tagging and push to GHCR ([bc63393](https://github.com/nilson-aguiar/manga-fetcher/commit/bc63393e625d13a7d0339ade2a7bfe3316cb6b27))
* **ci:** Implement GHCR authentication in GitHub Actions workflow ([04ad0b1](https://github.com/nilson-aguiar/manga-fetcher/commit/04ad0b1c3f608b6d9123978134ad1d4f059be746))
* **ci:** Implement test execution in CI with Playwright support ([119e908](https://github.com/nilson-aguiar/manga-fetcher/commit/119e908c5600b325f54e3af75e6187fe617a2c9b))
* **ci:** Setup basic GitHub Actions workflow with GraalVM and caching ([b00350d](https://github.com/nilson-aguiar/manga-fetcher/commit/b00350d43ff97c40176d299d4587eb8dc0424663))
* **cli:** Implement range download and output directory options ([11fd05e](https://github.com/nilson-aguiar/manga-fetcher/commit/11fd05e2698c1b48d769be9d6626b10c8552b4ef))
* **cli:** implement search and download commands ([82d33ff](https://github.com/nilson-aguiar/manga-fetcher/commit/82d33ffb88bc777bff98df00c7399a5bee38133e))
* **cli:** integrate scrapper with converter in download command ([add4c61](https://github.com/nilson-aguiar/manga-fetcher/commit/add4c61044c5b977244b4817b2539eb8b8dea3a3))
* **cli:** Refactor download command to use -c/--chapter flag ([88c291c](https://github.com/nilson-aguiar/manga-fetcher/commit/88c291cdbecd6ce2403b68a337960dfb21f97c1a))
* **http:** Implement CoreHttpClient with retries and rate limiting ([426105a](https://github.com/nilson-aguiar/manga-fetcher/commit/426105aab741b2e715a67297f40081d60dd5d505))
* **naming:** Implement ChapterNamingUtils and auto-rename logic ([8e318c5](https://github.com/nilson-aguiar/manga-fetcher/commit/8e318c58e7f8293ca5bfacfe312198b2f1fdd03a))
* **naming:** Refine renaming logic to handle old format and ensure correct naming ([ae4d5aa](https://github.com/nilson-aguiar/manga-fetcher/commit/ae4d5aa0cf9f27b1e289ed3580d648b8472a434f))
* **scraper:** Add volume extraction to ChapterResult and parseChapters ([a58e4f2](https://github.com/nilson-aguiar/manga-fetcher/commit/a58e4f2ee73c94e53392462df85bfd55081bbaba))
* **scraper:** Implement chapter fetching for MangaLivre ([bf1e137](https://github.com/nilson-aguiar/manga-fetcher/commit/bf1e137caacd4bbc22a0728fd7caf61b9e3ea500))
* **scraper:** Implement image downloading for MangaLivre ([5442d3b](https://github.com/nilson-aguiar/manga-fetcher/commit/5442d3b0726e50428740377a9fe2dbe8de00a6e4))
* **scraper:** Implement MangaLivre search functionality ([7b27328](https://github.com/nilson-aguiar/manga-fetcher/commit/7b27328dd512cf74e619af71e5d699737b4d4cce))
* **setup:** Configure Picocli and OkHttp dependencies ([b51120c](https://github.com/nilson-aguiar/manga-fetcher/commit/b51120c3b97b694e4d8d1e8ff6a4b101c3fa7b59))


### Bug Fixes

* **naming:** Strip Capitulo regardless of tilde/case and format numbers correctly ([c00e710](https://github.com/nilson-aguiar/manga-fetcher/commit/c00e710b5a2fb42598c7823049b9907dd8541e0f))
