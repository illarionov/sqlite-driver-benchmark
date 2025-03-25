/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the wasm-sqlite-open-helper project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import at.released.sqlitedriverbenchmark.gradle.ConvertBenchmarkReportsTask
import at.released.sqlitedriverbenchmark.gradle.PrepareDatabaseTask

plugins {
    id("at.released.sqlitedriverbenchmark.gradle.prefilldb")
    alias(libs.plugins.android.library)
    alias(libs.plugins.androidx.benchmark)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "at.released.sqlitedriverbenchmark"
    compileSdk = 35

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.profiling.mode"] = "None"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "DEBUGGABLE"
    }

    testBuildType = "release"

    testOptions {
        targetSdk = 35
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            testProguardFiles += listOf(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("proguard-test-rules.pro"),
            )
            @Suppress("UnstableApiUsage")
            androidTest {
                enableMinification = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

private val rawgGamesCsvPath = layout.projectDirectory
    .file("src/main/assets/rawg-games-dataset/rawg_games_data.csv")

androidComponents {
    onVariants { variant ->
        val prefillDatabaseTask =
            tasks.register<PrepareDatabaseTask>("${variant.name}PrefillDatabse") {
                inputCsv = rawgGamesCsvPath
                sqlFileName = "rawg_games_data.sql"
            }
        variant.sources.assets?.addGeneratedSourceDirectory(
            prefillDatabaseTask,
            PrepareDatabaseTask::outputDirectory
        )
    }
}

tasks.register<ConvertBenchmarkReportsTask>("convertBenchmarkReports")

dependencies {
    implementation("at.released.sqlitedriverbenchmark:rawgdb")
    implementation(libs.androidx.benchmark)
    implementation(libs.androidx.sqlite)
    implementation(libs.androidx.sqlite.bundled)
    implementation(libs.androidx.sqlite.framework)
    implementation(libs.chasm.runtime)
    implementation(libs.chicory.runtime)
    implementation(libs.hash4j)
    implementation(libs.wsoh.sqlite.binary.aot.plain)
    implementation(libs.wsoh.sqlite.binary.plain)
    implementation(libs.wsoh.sqlite.driver)
    implementation(libs.wsoh.sqlite.embedder.chasm)
    implementation(libs.wsoh.sqlite.embedder.chicory)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    testImplementation(libs.junit)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
