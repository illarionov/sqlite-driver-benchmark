/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the wasm-sqlite-open-helper project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URI

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    buildscript {
        repositories {
            mavenCentral()
            maven {
                url = uri("https://storage.googleapis.com/r8-releases/raw")
            }
        }
        dependencies {
            classpath("com.android.tools:r8:8.9.34")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "PixnewsMaven"
            url = URI("https://maven.pixnews.ru")
            mavenContent {
                includeModule("at.released.wasm-sqlite-driver", "sqlite-wasm-emscripten-349")
                includeModule("at.released.wasm-sqlite-driver", "sqlite-android-wasm-emscripten-icu-349")
                includeModule("at.released.wasm-sqlite-driver", "sqlite-wasm-emscripten-349-android")
            }
        }
    }
}

rootProject.name = "Sqlite Driver Benchmark"
includeBuild("rawgdb")
includeBuild("build-logic")
include(":bench")
