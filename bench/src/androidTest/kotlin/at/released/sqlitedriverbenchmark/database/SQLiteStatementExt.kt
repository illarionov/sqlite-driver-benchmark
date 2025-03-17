package at.released.sqlitedriverbenchmark.database

import androidx.sqlite.SQLiteStatement

internal fun SQLiteStatement.bindArgs(args: Collection<Any?>) = args.forEachIndexed { index, arg ->
    bind(index + 1, arg)
}

internal fun SQLiteStatement.bind(index: Int, arg: Any?) = when (arg) {
    null -> bindNull(index)
    is Boolean -> bindBoolean(index, arg)
    is ByteArray -> bindBlob(index, arg)
    is Double -> bindDouble(index, arg)
    is Float -> bindFloat(index, arg)
    is Int -> bindInt(index, arg)
    is Long -> bindLong(index, arg)
    is String -> bindText(index, arg)
    else -> error("Unsupported argument type $arg")
}

internal fun SQLiteStatement.readResult(): List<Map<String, String?>> {
    val result: MutableList<Map<String, String?>> = mutableListOf()
    val columnNames = getColumnNames()
    while (step()) {
        val row = buildMap(columnNames.size) {
            columnNames.forEachIndexed { columnIndex, columnName ->
                val value = if (!isNull(columnIndex)) {
                    this@readResult.getText(columnIndex)
                } else {
                    null
                }
                this[columnName] = value
            }
        }
        result.add(row)
    }
    return result
}
