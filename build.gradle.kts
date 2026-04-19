plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("kapt") version "2.3.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20"
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
    implementation("info.picocli:picocli:4.7.7")
    kapt("info.picocli:picocli-codegen:4.7.7")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("com.microsoft.playwright:playwright:1.59.0")
    implementation("org.jsoup:jsoup:1.22.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.xerial:sqlite-jdbc:3.53.0.0")
    implementation("org.slf4j:slf4j-simple:2.0.17")

    testImplementation("com.squareup.okhttp3:mockwebserver:5.3.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform {
        val excludeTags = project.findProperty("excludeTags") as? String
        if (!excludeTags.isNullOrBlank()) {
            excludeTags(excludeTags)
        }
    }
    testLogging {
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
