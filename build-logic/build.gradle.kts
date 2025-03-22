/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the wasm-sqlite-open-helper project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    `kotlin-dsl`
}

group = "at.released.sqlitedriverbenchmark"

dependencies {
    implementation("at.released.sqlitedriverbenchmark:rawgdb")
    implementation(libs.androidx.sqlite)
    implementation(libs.androidx.sqlite.bundled)
    implementation(libs.kotlin.csv)
}
