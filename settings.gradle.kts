/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
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
include(":bench")
