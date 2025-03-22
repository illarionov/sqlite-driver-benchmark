/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.sqlite.SQLiteDriver
import at.released.sqlitedriverbenchmark.database.TestSqliteConnection
import at.released.sqlitedriverbenchmark.database.TestSqliteConnection.Companion.setupRawgDbDefaults
import java.io.File

fun <R> BenchmarkRule.measureRepeatedSQLiteDriverBlock(
    driver: SQLiteDriver,
    path: (() -> File)? = null,
    block: TestSqliteConnection.() -> R,
) {
    measureRepeated {
        pauseMeasurement()
        val databaseFile = path?.invoke()?.toString() ?: ":memory:"
        val connection = driver.open(databaseFile)
        try {
            resumeMeasurement()
            block(connection.setupRawgDbDefaults())
        } finally {
            connection.close()
        }
    }
}
