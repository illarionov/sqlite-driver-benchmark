/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark.database

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.execSQL
import kotlin.use

inline fun <R> SQLiteConnection.transaction(
    isDeferred: Boolean = true,
    crossinline block: () -> R
): R {
    execSQL(if (isDeferred) "BEGIN" else "BEGIN EXCLUSIVE")
    val result =  try {
        block()
    } catch (ex: Throwable) {
        execSQL("ROLLBACK")
        throw ex
    }
    execSQL("COMMIT")
    return result
}

internal fun SQLiteConnection.queryForBoolean(
    sql: String,
    vararg bindArgs: Any?,
): Boolean = prepare(sql).use { statement ->
    statement.bindArgs(*bindArgs)
    statement.step()
}

public fun SQLiteConnection.queryForString(
    sql: String,
    vararg bindArgs: Any?,
): String? {
    return queryForSingleResult(sql, SQLiteStatement::getText, bindArgs = bindArgs)
}

public fun SQLiteConnection.queryForLong(
    sql: String,
    vararg bindArgs: Any?,
): Long? {
    return queryForSingleResult(sql, SQLiteStatement::getLong, bindArgs = bindArgs)
}

private fun <R : Any> SQLiteConnection.queryForSingleResult(
    sql: String,
    resultFetcher: (SQLiteStatement, Int) -> R?,
    vararg bindArgs: Any?,
): R? = prepare(sql).use { statement ->
    statement.bindArgs(*bindArgs)
    if (!statement.step()) {
        error("No row")
    }
    val columnCount = statement.getColumnCount()
    if (columnCount != 1) {
        error("Received `$columnCount` columns when 1 was expected ")
    }
    return if (!statement.isNull(0)) {
        resultFetcher(statement, 0)
    } else {
        null
    }
}

public fun SQLiteConnection.queryTable(
    sql: String,
    vararg bindArgs: Any?,
): List<Map<String, String?>> = prepare(sql).use { statement ->
    statement.bindArgs(*bindArgs)
    statement.readResult()
}
