/*
 * SPDX-FileCopyrightText: 2025-2026 Alexey Illarionov and the wasm-sqlite-open-helper project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.serialization)
}

group = "at.released.sqlitedriverbenchmark"

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        languageVersion = KotlinVersion.KOTLIN_2_2
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
}

dependencies {
    implementation("at.released.sqlitedriverbenchmark:rawgdb")
    implementation(libs.androidx.sqlite)
    implementation(libs.androidx.sqlite.bundled)
    implementation(libs.kotlin.csv)
    implementation(libs.kotlinx.serialization)
}
