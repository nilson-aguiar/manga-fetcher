# Changelog

## [1.3.0](https://github.com/nilson-aguiar/manga-fetcher/compare/v1.2.0...v1.3.0) (2026-04-19)


### Features

* add check command ([a434e5a](https://github.com/nilson-aguiar/manga-fetcher/commit/a434e5a83edc4b3bb3ac6f1388c5d61830863df7))


### Bug Fixes

* fix broken chapter names ([2d3700f](https://github.com/nilson-aguiar/manga-fetcher/commit/2d3700f4a74ce6cc925fe3feb41a8b1501ab2fcb))
* remove duplicate chapters ([4e49d31](https://github.com/nilson-aguiar/manga-fetcher/commit/4e49d31609772ed06dd417a358b3872912e35c49))
* restore chapter revision ([d34cb16](https://github.com/nilson-aguiar/manga-fetcher/commit/d34cb162045ad2e582274a3c8ba4e356159cf250))

## [1.2.0](https://github.com/nilson-aguiar/manga-fetcher/compare/v1.1.0...v1.2.0) (2026-04-19)


### Features

* add basic renovate options ([246762a](https://github.com/nilson-aguiar/manga-fetcher/commit/246762a3b1d8eca5c2391711c4d3849922923f00))
* add branches ci ([6f7c743](https://github.com/nilson-aguiar/manga-fetcher/commit/6f7c74321563387d5af84b01ff513f4c4a842aaf))


### Bug Fixes

* add logs for broken test ([414c925](https://github.com/nilson-aguiar/manga-fetcher/commit/414c925952d7ee480c2a9316dbb9228c7c038abd))
* add logs for broken test ([59fe056](https://github.com/nilson-aguiar/manga-fetcher/commit/59fe05699b71b11976b6aaaac65388f48471d0e6))
* **deps:** update all non-major dependencies ([f8962cd](https://github.com/nilson-aguiar/manga-fetcher/commit/f8962cd3681cfce5cf03b397f7e1c72d4d12e488))
* **deps:** update all non-major dependencies ([6e55750](https://github.com/nilson-aguiar/manga-fetcher/commit/6e55750c0e2df58bc3a64d80196a8485d341f743))
* **deps:** update dependency org.junit.jupiter:junit-jupiter to v6 ([e5a8af1](https://github.com/nilson-aguiar/manga-fetcher/commit/e5a8af10e478f4d04d3a6944b320457c173b0615))
* **deps:** update dependency org.junit.jupiter:junit-jupiter to v6 ([72a41ac](https://github.com/nilson-aguiar/manga-fetcher/commit/72a41ac66a2cdff6054c7750fc74d42160e8ad30))
* **deps:** update okhttp monorepo to v5 ([8120cd8](https://github.com/nilson-aguiar/manga-fetcher/commit/8120cd8dd616565a4e5e1fc25b40f1ac9c4d358f))
* **deps:** update okhttp monorepo to v5 (major) ([8e14950](https://github.com/nilson-aguiar/manga-fetcher/commit/8e149508ea340cf620cd391155156e9444c870c8))
* fix broken test ([17adc3b](https://github.com/nilson-aguiar/manga-fetcher/commit/17adc3b63cc71b5b7eebd9b39ded0c6b94511ec2))
* ignore some tests on ci ([96c9ce2](https://github.com/nilson-aguiar/manga-fetcher/commit/96c9ce26dd7a50ed21fe30097434259187d7c3a1))
* set a single concurrent ci to run ([310594f](https://github.com/nilson-aguiar/manga-fetcher/commit/310594f800d3b555293e29d7f42d1d2913134498))

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
