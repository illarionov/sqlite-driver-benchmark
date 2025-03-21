/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark.database

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import java.io.File
import kotlin.use

fun <R> BenchmarkRule.measureRepeatedSQLiteDriverBlock(
    driver: SQLiteDriver,
    path: (() -> File)? = null,
    block: TestSqliteConnection.() -> R,
) {
    measureRepeated {
        pauseMeasurement()
        val fileName = path?.invoke()?.toString() ?: ":memory:"
        val connection = driver.open(fileName)
        try {
            resumeMeasurement()
            block(TestSqliteConnection(connection))
        } finally {
            connection.close()
        }
    }
}

fun <R> SQLiteDriver.execute(
    path: File? = null,
    block: TestSqliteConnection.() -> R,
): R {
    val fileName = path?.toString() ?: ":memory:"
    return open(fileName).use { connection: SQLiteConnection ->
        block(TestSqliteConnection(connection))
    }
}
