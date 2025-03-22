/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark.database

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import at.released.sqlitedriverbenchmark.database.TestSqliteConnection.Companion.setupRawgDbDefaults
import java.io.File
import kotlin.use

fun <R> SQLiteDriver.execute(
    path: File? = null,
    block: TestSqliteConnection.() -> R,
): R {
    val fileName = path?.toString() ?: ":memory:"
    return open(fileName).use { connection: SQLiteConnection ->
        block(connection.setupRawgDbDefaults())
    }
}
