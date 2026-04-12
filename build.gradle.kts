plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("kapt") version "2.2.21"
    id("org.graalvm.buildtools.native") version "0.11.5"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.21"
    application
    jacoco
}

application {
    mainClass.set("com.mangafetcher.downloader.DownloaderApplicationKt")
}

group = "com.mangafetcher"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
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
            mainClass.set("com.mangafetcher.downloader.DownloaderApplicationKt")
            buildArgs.add("--verbose")
            buildArgs.add("--no-fallback")
            buildArgs.add("--initialize-at-build-time=kotlin.DeprecationLevel")
            // Playwright and OkHttp might need these
            buildArgs.add("-H:+ReportExceptionStackTraces")
        }
    }
}
