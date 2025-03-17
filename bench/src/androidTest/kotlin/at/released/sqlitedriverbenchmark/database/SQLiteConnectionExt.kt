package at.released.sqlitedriverbenchmark.database

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import kotlin.use

internal fun SQLiteConnection.queryForBoolean(
    sql: String,
    vararg bindArgs: Any?,
): Boolean = prepare(sql).use { statement ->
    statement.bindArgs(bindArgs.toList())
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

@Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")
private fun <R : Any> SQLiteConnection.queryForSingleResult(
    sql: String,
    resultFetcher: (SQLiteStatement, Int) -> R?,
    vararg bindArgs: Any?,
): R? = prepare(sql).use { statement ->
    statement.bindArgs(bindArgs.toList())
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
    statement.bindArgs(bindArgs.toList())
    statement.readResult()
}
