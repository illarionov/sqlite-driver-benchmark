/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark.database

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.execSQL
import at.released.sqlitedriverbenchmark.database.RawgDatabaseSchema.RAWG_DATABASE_INDICIES
import at.released.sqlitedriverbenchmark.database.RawgDatabaseSchema.RAWG_DATABASE_SCHEMA_RAW
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.InputStream

public fun importRawgDatabase(
    connection: SQLiteConnection,
    csvSource: InputStream,
    maxEntries: Int? = null
) {
    RAWG_DATABASE_SCHEMA_RAW.forEach(connection::execSQL)
    connection.execSQL("PRAGMA foreign_keys=OFF")
    connection.execSQL("PRAGMA ignore_check_constraints=ON")
    connection.execSQL("PRAGMA journal_mode=OFF")
    connection.execSQL("PRAGMA synchronous=0")
    try {
        connection.importGames(csvSource, maxEntries)
    } finally {
        connection.execSQL("PRAGMA foreign_keys=1")
        connection.execSQL("PRAGMA ignore_check_constraints=false")
        connection.execSQL("PRAGMA journal_mode=WAL")
        connection.execSQL("PRAGMA synchronous=1")
    }
    RAWG_DATABASE_INDICIES.forEach(connection::execSQL)
}

private fun SQLiteConnection.importGames(
    inputStream: InputStream,
    maxEntries: Int? = null
) {
    csvReader().open(inputStream) {
        val entries: Sequence<List<String>> = this.readAllAsSequence(fieldsNum = 12)
            .drop(1)
            .run {
                if (maxEntries != null) {
                    take(maxEntries)
                } else {
                    this
                }
            }
        RawgDatabaseImporter(this@importGames).use { it.insertGames(entries) }
    }
}

private class RawgDatabaseImporter(
    private val connection: SQLiteConnection,
    val lastInsertRowidStatement: SQLiteStatement,
    val insertGameStatement: SQLiteStatement,
    val insertGameBatchStatement: SQLiteStatement,
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
            csvEntries
                .map { GameCsvEntry(it) }
                .chunked(INSERT_GAME_BATCH_SIZE)
                .forEachIndexed { index: Int, csvEntries: List<GameCsvEntry> ->
                    insertGameCsvEntriesBatch(csvEntries)
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

    private fun insertGameCsvEntriesBatch(csvEntries: List<GameCsvEntry>) {
        if (csvEntries.size == INSERT_GAME_BATCH_SIZE) {
            insertGameBatchStatement.reset()
            csvEntries.forEachIndexed { idx, entry ->
                insertGameBatchStatement.bindGameEntryArgs(idx, entry)
            }
            insertGameBatchStatement.step()
        } else {
            csvEntries.forEach { csvEntry ->
                insertGameStatement.reset()
                insertGameStatement.bindGameEntryArgs(0, csvEntry)
                insertGameStatement.step()
            }
        }

        csvEntries.forEach { csvEntry ->
            val gameId = csvEntry.id
            insertGenres(gameId, csvEntry.genres)
            insertPlatforms(gameId, csvEntry.platforms)
            insertCompanies(gameId, csvEntry.developers, csvEntry.publishers)
        }
    }

    private fun SQLiteStatement.bindGameEntryArgs(
        csvEntryNo: Int,
        csvEntry: GameCsvEntry
    ) {
        var index = 1 + csvEntryNo * 8
        bind(index++, csvEntry.id)
        bind(index++, csvEntry.name)
        bind(index++, csvEntry.released)
        bind(index++, csvEntry.rating)
        bind(index++, csvEntry.tags)
        bind(index++, csvEntry.metacritic)
        bind(index++, csvEntry.playtime)
        bind(index, csvEntry.description)
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
        val companies = buildMap<String, MutableSet<Char>> {
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
        insertGameBatchStatement.closeSilent()
        insertGameStatement.closeSilent()
        insertGenreStatement.closeSilent()
        insertGameToGenreStatement.closeSilent()
        insertPlatformStatement.closeSilent()
        insertGameToPlatformStatement.closeSilent()
        insertCompanyStatement.closeSilent()
        insertGameToCompanyStatement.closeSilent()
    }

    @JvmInline
    private value class GameCsvEntry(private val csvEntry: List<String>) {
        val id: Long get() = csvEntry[0].toLong()
        val name: String get() = csvEntry[1]
        val released: String? get() = csvEntry.getIfNotBlank(2)
        val rating: String? get() = csvEntry.getIfNotBlank(3)
        val genres: String get() = csvEntry.getOrElse(4) { "" }
        val platforms: String get() = csvEntry.getOrElse(5) { "" }
        val tags: String get() = csvEntry.getOrElse(6) { "" }
        val metacritic: String? get() = csvEntry.getIfNotBlank(7)
        val developers: String get() = csvEntry.getOrElse(8) { "" }
        val publishers: String get() = csvEntry.getOrElse(9) { "" }
        val playtime: String? get() = csvEntry.getIfNotBlank(10)
        val description: String? get() = csvEntry.getIfNotBlank(11)
    }

    companion object {
        const val TRANSACTION_WINDOW_SIZE = 2500
        const val INSERT_GAME_BATCH_SIZE = 200
        private val insertGameStatementColumns = listOf(
            "id", "name", "released", "rating", "tags", "metacritic", "playtime", "description"
        )
        private val insertGameStatementPrefix = insertGameStatementColumns.joinToString(
            prefix = "INSERT INTO game(",
            postfix = ") VALUES"
        )
        private val insertGameStatementValues = insertGameStatementColumns
            .joinToString(",", "(", ")") { "?" }
        private val insertGameStatementValuesBatch =
            List(INSERT_GAME_BATCH_SIZE) { insertGameStatementValues }.joinToString(",")

        fun List<String>.getIfNotBlank(index: Int) = getOrNull(index)?.takeIf(String::isNotBlank)

        operator fun invoke(connection: SQLiteConnection): RawgDatabaseImporter {
            var lastInsertRowidStatement: SQLiteStatement? = null
            var insertGameStatement: SQLiteStatement? = null
            var insertGameBatchStatement: SQLiteStatement? = null
            var insertGenreStatement: SQLiteStatement? = null
            var insertGameToGenreStatement: SQLiteStatement? = null
            var insertPlatformStatement: SQLiteStatement? = null
            var insertGameToPlatformStatement: SQLiteStatement? = null
            var insertCompanyStatement: SQLiteStatement? = null
            var insertGameToCompanyStatement: SQLiteStatement? = null
            try {
                lastInsertRowidStatement = connection.prepare("SELECT last_insert_rowid()")
                insertGameStatement = connection.prepare(
                    "$insertGameStatementPrefix $insertGameStatementValues"
                )
                insertGameBatchStatement = connection.prepare(
                    "$insertGameStatementPrefix $insertGameStatementValuesBatch"
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

                return RawgDatabaseImporter(
                    connection = connection,
                    lastInsertRowidStatement = lastInsertRowidStatement,
                    insertGameStatement = insertGameStatement,
                    insertGameBatchStatement = insertGameBatchStatement,
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
                insertGameBatchStatement?.closeSilent()
                insertGenreStatement?.closeSilent()
                insertGameToGenreStatement?.closeSilent()
                insertPlatformStatement?.closeSilent()
                insertGameToPlatformStatement?.closeSilent()
                insertCompanyStatement?.closeSilent()
                insertGameToCompanyStatement?.closeSilent()
                throw ex
            }
        }
    }
}
