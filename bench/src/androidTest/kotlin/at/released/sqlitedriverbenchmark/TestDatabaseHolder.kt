/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import android.content.Context
import androidx.sqlite.SQLiteConnection
import at.released.sqlitedriverbenchmark.database.importRawgDatabase
import java.io.File

const val RAWG_GAMES_DATA_CSV_PATH = "rawg-games-dataset/rawg_games_data.csv"
const val RAWG_SQL_PATH = "rawg_games_data.sql"

internal fun Context.createRawgDatabaseFromCsv(
    connection: SQLiteConnection,
    maxEntries: Int? = null
) = assets.open(RAWG_GAMES_DATA_CSV_PATH).use { stream ->
    importRawgDatabase(
        connection = connection,
        csvSource = stream,
        maxEntries = maxEntries
    )
}

fun Context.copyPreparedRawgDatabase(dst: File): File = dst.outputStream().buffered().use { out ->
    assets.open(RAWG_SQL_PATH).buffered().use { inStream ->
        inStream.copyTo(out)
    }
    return dst
}
