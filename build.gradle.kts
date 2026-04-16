plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("kapt") version "2.3.0"
    id("org.graalvm.buildtools.native") version "0.11.5"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"
    application
    jacoco
}

application {
    mainClass.set("com.mangafetcher.downloader.cli.DownloaderApplicationKt")
}

group = "com.mangafetcher"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("info.picocli:picocli:4.7.6")
    kapt("info.picocli:picocli-codegen:4.7.6")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.microsoft.playwright:playwright:1.44.0")
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")
    implementation("org.slf4j:slf4j-simple:2.0.9")

    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("manga-fetcher")
            mainClass.set("com.mangafetcher.downloader.cli.DownloaderApplicationKt")
            buildArgs.add("--verbose")
            buildArgs.add("--no-fallback")
            buildArgs.add("--initialize-at-build-time=kotlin.DeprecationLevel")
            // Optimization flags for faster builds
            if (project.hasProperty("quick")) {
                buildArgs.add("-Ob") // Quick build mode
            }
            buildArgs.add("-O1") // Basic optimization (faster than default -O2)

            // Resource management
            buildArgs.add("-J-Xmx4g")
            buildArgs.add("-H:+ReportExceptionStackTraces")
            // Initialize Playwright's driver logic at build time to capture resource state
            buildArgs.add("--initialize-at-build-time=com.microsoft.playwright.impl.driver.jar.DriverJar")
            buildArgs.add("--initialize-at-build-time=com.microsoft.playwright.impl.driver.Driver")
            buildArgs.add("--initialize-at-build-time=kotlin.DeprecationLevel")

            // Ensure all resources, specifically the driver binaries, are included
            buildArgs.add("-H:IncludeResources=driver/.*")
            buildArgs.add("-H:IncludeResources=.*\\.properties")
        }
    }
}
