/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the wasm-sqlite-open-helper project contributors
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
