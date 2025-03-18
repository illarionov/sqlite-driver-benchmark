/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark.database

import android.content.res.AssetManager
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.execSQL
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader

class RawgDatabase(
    private val connection: TestSqliteConnection,
    private val assetManager: AssetManager,
) {
    fun loadDatabase(
        maxEntries: Int? = null
    ) {
        createRawDatabaseSchema()
        loadGames(maxEntries)
    }

    fun createRawDatabaseSchema() {
        RAWG_DATABASE_SCHEMA.forEach { connection.execSQL(it) }
    }

    fun loadGames(
        maxEntries: Int? = null
    ) {
        csvReader().open(assetManager.open(RAWG_GAMES_DATA_CSV)) {
            val entries: Sequence<List<String>> = this.readAllAsSequence(fieldsNum = 12)
                .drop(1)
                .run {
                    if (maxEntries != null) {
                        take(maxEntries)
                    } else {
                        this
                    }
                }
            loadGames(entries)
        }
    }

    private fun loadGames(csvEntries: Sequence<List<String>>) {
        connection.prepare(INSERT_INTO_GAME_REQUEST).use { statement: SQLiteStatement ->
            var transactionActive = true
            connection.execSQL("BEGIN EXCLUSIVE")
            try {
                csvEntries.forEachIndexed { index: Int, csvEntry: List<String> ->
                    statement.reset()
                    statement.bindArgs(csvEntry)
                    statement.step()
                    if ((index + 1) % TRANSACTION_WINDOW_SIZE == 0) {
                        transactionActive = false
                        connection.execSQL("COMMIT")
                        connection.execSQL("BEGIN EXCLUSIVE")
                        transactionActive = true
                    }
                }
            } catch (ex: Throwable) {
                if (transactionActive) {
                    connection.execSQL("ROLLBACK")
                    transactionActive = false
                }
                throw ex
            }
            connection.execSQL("COMMIT")
        }
    }

    companion object {
        private const val TRANSACTION_WINDOW_SIZE = 1000
        private const val RAWG_GAMES_DATA_CSV = "rawg-games-dataset/rawg_games_data.csv"

        internal const val INSERT_INTO_GAME_REQUEST = """
            INSERT INTO game(
                    id,
                    name,
                    released,
                    rating,
                    genres,
                    platforms,
                    tags,
                    metacritic,
                    developers,
                    publishers,
                    playtime,
                    description
            ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)
        """

        private val RAWG_DATABASE_SCHEMA: List<String> = listOf(
            """
                CREATE TABLE IF NOT EXISTS "game"(
                    "id" INTEGER NOT NULL PRIMARY KEY,
                    "name" TEXT NOT NULL,
                    "released" TEXT,
                    "rating" DECIMAL(3,2),
                    "genres" TEXT NOT NULL DEFAULT '',
                    "platforms" TEXT NOT NULL DEFAULT '',
                    "tags" TEXT NOT NULL DEFAULT '',
                    "metacritic" DECIMAL(4,2),
                    "developers" TEXT NOT NULL DEFAULT '',
                    "publishers" TEXT NOT NULL DEFAULT '',
                    "playtime" TEXT,
                    "description" TEXT
                 );
            """.trimIndent()
        )

    }
}
