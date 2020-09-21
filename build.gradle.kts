buildscript {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        jcenter()
        mavenCentral()
    }
}

plugins {
    java
    idea
    id("com.adarshr.test-logger") version Versions.gradleTestLoggerPlugin apply false
    id("io.micronaut.application") version Versions.gradleMicronautPlugin apply false
    id("io.micronaut.library") version Versions.gradleMicronautPlugin apply false
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

idea {
    module {
        isDownloadJavadoc = false
        isDownloadSources = false
    }
}

allprojects {
    repositories {
        mavenLocal()
        jcenter()
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    group = "com.zhokhov.progimage"
    version = Versions.project

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Test> {
        useJUnitPlatform {
            includeEngines = setOf("junit-jupiter")
            excludeEngines = setOf("junit-vintage")
        }
    }
}
