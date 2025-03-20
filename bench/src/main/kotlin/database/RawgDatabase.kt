/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark.database

import android.content.res.AssetManager
import androidx.sqlite.execSQL

class RawgDatabase(
    private val connection: TestSqliteConnection,
    private val assetManager: AssetManager,
) {
    fun createDatabaseFromAssets(maxEntries: Int? = null) {
        createRawDatabaseSchema()
        connection.execSQL("PRAGMA foreign_keys=0")
        connection.execSQL("PRAGMA ignore_check_constraints=true")
        connection.execSQL("PRAGMA journal_mode=OFF")
        connection.execSQL("PRAGMA synchronous=0")
        try {
            loadGamesFromAssets(connection, assetManager, maxEntries)
        } finally {
            connection.execSQL("PRAGMA foreign_keys=1")
            connection.execSQL("PRAGMA ignore_check_constraints=false")
            connection.execSQL("PRAGMA journal_mode=WAL")
            connection.execSQL("PRAGMA synchronous=1")
        }
        createIndicies()
    }

    private fun createRawDatabaseSchema() {
        RAWG_DATABASE_SCHEMA_RAW.forEach(connection::execSQL)
    }

    private fun createIndicies() {
        RAWG_DATABASE_INDICIES.forEach(connection::execSQL)
    }

    companion object {
        const val TRANSACTION_WINDOW_SIZE = 1000

        private val RAWG_DATABASE_SCHEMA_RAW: List<String> = listOf(
            """
                CREATE TABLE IF NOT EXISTS "game"(
                    "id" INTEGER NOT NULL PRIMARY KEY,
                    "name" TEXT NOT NULL,
                    "released" TEXT,
                    "rating" DECIMAL(3,2),
                    "tags" TEXT,
                    "metacritic" DECIMAL(4,2),
                    "playtime" TEXT,
                    "description" TEXT
                 )
            """.trimIndent(),
            """
               CREATE TABLE IF NOT EXISTS "genre"(
                 "id" INTEGER NOT NULL PRIMARY KEY,
                 "name" TEXT NOT NULL UNIQUE
               )
            """.trimIndent(),
            """
               CREATE TABLE IF NOT EXISTS "game_genre"(
                 "game_id" INTEGER NOT NULL,
                 "genre_id" INTEGER NOT NULL,
                 FOREIGN KEY("game_id") REFERENCES game(id) ON DELETE CASCADE,
                 FOREIGN KEY("genre_id") REFERENCES genre(id) ON DELETE CASCADE
               )
            """.trimIndent(),
            """
               CREATE TABLE IF NOT EXISTS "platform"(
                 "id" INTEGER NOT NULL PRIMARY KEY,
                 "name" TEXT NOT NULL UNIQUE
               )
            """.trimIndent(),
            """
               CREATE TABLE IF NOT EXISTS "game_platform"(
                 "game_id" INTEGER NOT NULL,
                 "platform_id" INTEGER NOT NULL,
                 FOREIGN KEY("game_id") REFERENCES game(id) ON DELETE CASCADE,
                 FOREIGN KEY("platform_id") REFERENCES platform(id) ON DELETE CASCADE
               )
            """.trimIndent(),
            """
               CREATE TABLE IF NOT EXISTS "company"(
                 "id" INTEGER NOT NULL PRIMARY KEY,
                 "name" TEXT NOT NULL UNIQUE
               )
            """.trimIndent(),
            """
               CREATE TABLE IF NOT EXISTS "game_company"(
                 "id" INTEGER NOT NULL PRIMARY KEY,
                 "game_id" INTEGER NOT NULL,
                 "company_id" INTEGER NOT NULL,
                 "type" CHAR(1) NOT NULL DEFAULT 'D',
                 FOREIGN KEY("game_id") REFERENCES game(id) ON DELETE CASCADE,
                 FOREIGN KEY("company_id") REFERENCES company(id) ON DELETE CASCADE
               )
            """.trimIndent(),
        )

        private val RAWG_DATABASE_INDICIES: List<String> = listOf(
            """
                CREATE UNIQUE INDEX "game_genre_idx1" ON "game_genre" ("game_id", "genre_id")
            """.trimIndent(),
            """
               CREATE UNIQUE INDEX "game_platform_idx1" ON "game_platform" ("game_id", "platform_id")
            """.trimIndent(),
            """
               CREATE UNIQUE INDEX "game_company_id1" ON "game_company" ("game_id", "company_id", "type");
            """.trimIndent()
        )
    }
}
