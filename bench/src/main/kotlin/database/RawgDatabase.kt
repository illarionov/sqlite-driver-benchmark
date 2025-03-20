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
import kotlin.use

class RawgDatabase(
    private val connection: TestSqliteConnection,
    private val assetManager: AssetManager,
) {
    fun loadDatabase(maxEntries: Int? = null) {
        createRawDatabaseSchema()

        connection.execSQL("PRAGMA foreign_keys=0")
        connection.execSQL("PRAGMA ignore_check_constraints=true")
        connection.execSQL("PRAGMA journal_mode=OFF")
        connection.execSQL("PRAGMA synchronous=0")
        try {
            loadGames(maxEntries)
        } finally {
            connection.execSQL("PRAGMA foreign_keys=1")
            connection.execSQL("PRAGMA ignore_check_constraints=false")
            connection.execSQL("PRAGMA journal_mode=WAL")
            connection.execSQL("PRAGMA synchronous=1")
        }
        createIndicies()
    }

    fun createRawDatabaseSchema() {
        RAWG_DATABASE_RAWSCHEMA.forEach { connection.execSQL(it) }
    }

    fun createIndicies() {
        RAWG_DATABASE_INDICIES.forEach { connection.execSQL(it) }
    }

    fun loadGames(maxEntries: Int? = null) {
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
            RawgDatabaseLoader(connection).use { it.insertGames(entries) }
        }
    }

    private fun RawgDatabaseLoader(
        connection: TestSqliteConnection,
    ): RawgDatabaseLoader {
        var lastInsertRowidStatement: SQLiteStatement? = null
        var insertGameStatement: SQLiteStatement? = null
        var insertGenreStatement: SQLiteStatement? = null
        var insertGameToGenreStatement: SQLiteStatement? = null
        var insertPlatformStatement: SQLiteStatement? = null
        var insertGameToPlatformStatement: SQLiteStatement? = null
        var insertCompanyStatement: SQLiteStatement? = null
        var insertGameToCompanyStatement: SQLiteStatement? = null
        try {
            lastInsertRowidStatement = connection.prepare("SELECT last_insert_rowid()")
            insertGameStatement = connection.prepare(
                """
                    INSERT INTO game(id,name,released,rating,tags,metacritic,playtime,description)
                        VALUES(?,?,?,?,?,?,?,?)
                """
            )
            insertGenreStatement = connection.prepare(
                "INSERT INTO genre(name) VALUES(?)",
            )
            insertGameToGenreStatement = connection.prepare(
                "INSERT INTO game_genre(game_id, genre_id) VALUES(?, ?)",
            )
            insertPlatformStatement = connection.prepare(
                "INSERT INTO platform(name) VALUES(?)",
            )
            insertGameToPlatformStatement = connection.prepare(
                "INSERT INTO game_platform(game_id, platform_id) VALUES(?, ?)",
            )
            insertCompanyStatement = connection.prepare(
                "INSERT INTO company(name) VALUES(?)",
            )
            insertGameToCompanyStatement = connection.prepare(
                "INSERT INTO game_company(game_id, company_id, type) VALUES(?,?,?)",
            )

            return RawgDatabaseLoader(
                connection = connection,
                lastInsertRowidStatement = lastInsertRowidStatement,
                insertGameStatement = insertGameStatement,
                insertGenreStatement = insertGenreStatement,
                insertGameToGenreStatement = insertGameToGenreStatement,
                insertPlatformStatement = insertPlatformStatement,
                insertGameToPlatformStatement = insertGameToPlatformStatement,
                insertCompanyStatement = insertCompanyStatement,
                insertGameToCompanyStatement = insertGameToCompanyStatement,
            )
        } catch (ex: Throwable) {
            lastInsertRowidStatement?.closeSilent()
            insertGameStatement?.closeSilent()
            insertGenreStatement?.closeSilent()
            insertGameToGenreStatement?.closeSilent()
            insertPlatformStatement?.closeSilent()
            insertGameToPlatformStatement?.closeSilent()
            insertCompanyStatement?.closeSilent()
            insertGameToCompanyStatement?.closeSilent()
            throw ex
        }
    }

    private class RawgDatabaseLoader(
        private val connection: TestSqliteConnection,
        val lastInsertRowidStatement: SQLiteStatement,
        val insertGameStatement: SQLiteStatement,
        val insertGenreStatement: SQLiteStatement,
        val insertGameToGenreStatement: SQLiteStatement,
        val insertPlatformStatement: SQLiteStatement,
        val insertGameToPlatformStatement: SQLiteStatement,
        val insertCompanyStatement: SQLiteStatement,
        val insertGameToCompanyStatement: SQLiteStatement,
    ) : AutoCloseable {
        private val genres: MutableMap<String, Long> = mutableMapOf()
        private val platforms: MutableMap<String, Long> = mutableMapOf()
        private val companies: MutableMap<String, Long> = mutableMapOf()

        fun insertGames(csvEntries: Sequence<List<String>>) {
            var transactionActive = true
            connection.execSQL("BEGIN EXCLUSIVE")
            try {
                csvEntries.forEachIndexed { index: Int, csvEntry: List<String> ->
                    insertGameCsvEntry(csvEntry)
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

        private fun insertGameCsvEntry(csvEntry: List<String>) {
            val id = csvEntry[0].toLong()
            val name = csvEntry[1]
            val released = csvEntry.getOrNull(2)
            val rating = csvEntry.getOrNull(3)
            val genres = csvEntry.getOrElse(4) { "" }
            val platforms = csvEntry.getOrElse(5) { "" }
            val tags = csvEntry.getOrElse(6) { "" }
            val metacritic = csvEntry.getOrNull(7)
            val developers = csvEntry.getOrElse(8) { "" }
            val publishers = csvEntry.getOrElse(9) { "" }
            val playtime = csvEntry.getOrNull(10)
            val description = csvEntry.getOrNull(11)

            insertGameStatement.reset()
            insertGameStatement.bindArgs(
                id,
                name,
                released,
                rating,
                tags,
                metacritic,
                playtime,
                description
            )
            insertGameStatement.step()

            insertGenres(id, genres)
            insertPlatforms(id, platforms)
            insertCompanies(id, developers, publishers)
        }

        private fun insertGenres(gameId: Long, genres: String) {
            genres.split(", ").filter { it.isNotBlank() }.forEach { genre ->
                val genreId = this.genres.getOrPut(genre) {
                    with(insertGenreStatement) {
                        reset()
                        bindText(1, genre)
                        step()
                        getLastInsertRowId()
                    }
                }

                with(insertGameToGenreStatement) {
                    reset()
                    bindLong(1, gameId)
                    bindLong(2, genreId)
                    step()
                }
            }
        }

        private fun insertPlatforms(gameId: Long, platforms: String) {
            platforms.split(", ").filter { it.isNotBlank() }.forEach { platform ->
                val platformId = this.platforms.getOrPut(platform) {
                    with(insertPlatformStatement) {
                        reset()
                        bindText(1, platform)
                        step()
                        getLastInsertRowId()
                    }
                }

                with(insertGameToPlatformStatement) {
                    reset()
                    bindLong(1, gameId)
                    bindLong(2, platformId)
                    step()
                }
            }
        }

        private fun insertCompanies(gameId: Long, developers: String, publishers: String) {
            val companies = buildMap {
                developers.split(", ").forEach { developer ->
                    this.getOrPut(developer, ::mutableSetOf) += 'D'
                }
                publishers.split(", ").forEach { publisher ->
                    this.getOrPut(publisher, ::mutableSetOf) += 'P'
                }
            }

            companies.forEach { (company, types) ->
                val companyId = this.companies.getOrPut(company) {
                    with(insertCompanyStatement) {
                        reset()
                        bindText(1, company)
                        step()
                        getLastInsertRowId()
                    }
                }

                with(insertGameToCompanyStatement) {
                    types.forEach { type ->
                        reset()
                        bindLong(1, gameId)
                        bindLong(2, companyId)
                        bindText(3, type.toString())
                        step()
                    }
                }
            }
        }

        private fun getLastInsertRowId(): Long = with(lastInsertRowidStatement) {
            reset()
            step()
            getLong(0)
        }

        override fun close() {
            lastInsertRowidStatement.closeSilent()
            insertGameStatement.closeSilent()
            insertGameStatement.closeSilent()
            insertGenreStatement.closeSilent()
            insertGameToGenreStatement.closeSilent()
            insertPlatformStatement.closeSilent()
            insertGameToPlatformStatement.closeSilent()
            insertCompanyStatement.closeSilent()
            insertGameToCompanyStatement.closeSilent()
        }

        private fun List<String>.getOrNull(idx: Int) = getOrElse(idx) { null }
    }

    companion object {
        private const val TRANSACTION_WINDOW_SIZE = 1000
        private const val RAWG_GAMES_DATA_CSV = "rawg-games-dataset/rawg_games_data.csv"

        private val RAWG_DATABASE_RAWSCHEMA: List<String> = listOf(
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
