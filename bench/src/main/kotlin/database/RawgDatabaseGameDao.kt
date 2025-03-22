/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark.database

import android.util.Log
import androidx.sqlite.SQLiteStatement
import com.dynatrace.hash4j.hashing.HashSink
import com.dynatrace.hash4j.hashing.Hashing

class RawgDatabaseGameDao private constructor(
    private val selectGamesStatement: SQLiteStatement,
    private val selectCompaniesStatement: SQLiteStatement,
) : AutoCloseable {

    fun getGames(
        offset: Int = 0,
        limit: Int = 10,
    ): List<Map<String, String?>> = with(selectGamesStatement) {
        reset()
        bindInt(1, limit)
        bindInt(2, offset)
        readResult()
    }

    fun getCompanies(
        maxId: Long = 1_000_000,
    ): List<Map<String, String?>> = with(selectCompaniesStatement) {
        reset()
        bindLong(1, maxId)
        readResult()
    }

    override fun close() {
        selectGamesStatement.closeSilent()
        selectCompaniesStatement.closeSilent()
    }

    public companion object {
        val GAMES_HASH_1000 = HashWithCount(-0x494f78e9218684b2, 1000)
        val COMPANIES_HASH_1_000_000 = HashWithCount(0xcab31b759d02b5e, 1_000_000)
        val COMPANIES_HASH_5000 = HashWithCount(-0x7cd81af22fc88b04, 5_000)
        val COMPANIES_HASH_1000 = HashWithCount(0x2d06800538d394c2, 1_000)

        fun RawgDatabaseGameDao.calculateGamesHash(
            maxEntries: Int = 1000,
            step: Int = 40
        ): HashWithCount {
            val stream = Hashing.xxh3_64().hashStream()
            var offset = 0
            var result: List<Map<String, String?>> = emptyList()
            do {
                result = this.getGames(offset, step)
                offset += result.size
                stream.putMaps(result)
            } while (result.isNotEmpty() && offset < maxEntries)

            return HashWithCount(stream.asLong, offset)
        }

        fun RawgDatabaseGameDao.calculateCompaniesHash(
            maxId: Long = 1_000_000
        ): Long {
            val stream = Hashing.xxh3_64().hashStream()
            var result: List<Map<String, String?>> = this.getCompanies(maxId)
            stream.putMaps(result)
            return stream.asLong
        }

        data class HashWithCount(
            val hash: Long,
            val count: Int,
        ) {
            override fun toString(): String {
                return "HashWithCount(hash=0x${hash.toString(16)}, count=$count)"
            }
        }

        private fun HashSink.putMaps(maps: List<Map<String, String?>>) {
            maps.forEach { gameEntity: Map<String, String?> ->
                gameEntity.values.forEach {
                    if (it != null) {
                        putString(it)
                    }
                }
            }
        }

        operator fun invoke(connection: TestSqliteConnection): RawgDatabaseGameDao {
            var selectGamesStatement: SQLiteStatement? = null
            var selectCompaniesStatement: SQLiteStatement? = null
            try {
                selectGamesStatement = connection.prepare(
                    """
                    SELECT 
                        game.id,
                        game.name,
                        game.released,
                        substr(game.description, 0, 30) AS description,
                        group_concat(DISTINCT genre.name) AS genres
                    FROM game
                        LEFT JOIN game_genre ON game_genre.game_id=game.id
                        LEFT JOIN genre ON genre.id=game_genre.genre_id
                        LEFT JOIN game_platform ON game.id=game_platform.game_id
                        LEFT JOIN platform ON platform.id=game_platform.platform_id
                    WHERE (game.released IS NOT NULL)
                        AND platform.name IN('macOS', 'Linux')
                        AND game.name NOT LIKE '%Puzzle%'
                        AND game.tags NOT LIKE '%NSFW%'
                        AND genre.name IN ('Arcade', 'Sports')
                    GROUP BY game.id
                    ORDER BY game.id
                    LIMIT ? OFFSET ?
                    """.trimIndent()
                )

                selectCompaniesStatement = connection.prepare(
                    """
                    SELECT company.name,genre.name,count(game.id) as games_cnt
                    FROM company
                        INNER join game_company on company.id=game_company.company_id
                        INNER join game on game.id=game_company.game_id
                        INNER JOIN game_genre ON game_genre.game_id=game.id
                        INNER JOIN genre ON genre.id=game_genre.genre_id
                    WHERE game.id<?
                    GROUP BY company.id,genre.id
                    HAVING games_cnt>40
                    ORDER BY company.name,games_cnt DESC
                    LIMIT 5 offset 10;
                    """.trimIndent()
                )

                return RawgDatabaseGameDao(selectGamesStatement, selectCompaniesStatement)
            } catch (ex: Throwable) {
                selectGamesStatement?.closeSilent()
                selectCompaniesStatement?.closeSilent()
                throw ex
            }
        }
    }
}
