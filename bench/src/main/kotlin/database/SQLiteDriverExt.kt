/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark.database

import androidx.benchmark.junit4.BenchmarkRule
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import java.io.File
import kotlin.use

fun <R> BenchmarkRule.Scope.measureSQLiteDriverBlock(
    driver: SQLiteDriver,
    path: File? = null,
    block: TestSqliteConnection.() -> R,
): R {
    pauseMeasurement()
    val fileName = path?.toString() ?: ":memory:"
    val connection = driver.open(fileName)
    return try {
        resumeMeasurement()
        block(TestSqliteConnection(connection))
    } finally {
        connection.close()
    }
}

fun <R> SQLiteDriver.run(
    path: File? = null,
    block: TestSqliteConnection.() -> R,
): R {
    val fileName = path?.toString() ?: ":memory:"
    return open(fileName).use { connection: SQLiteConnection ->
        block(TestSqliteConnection(connection))
    }
}
