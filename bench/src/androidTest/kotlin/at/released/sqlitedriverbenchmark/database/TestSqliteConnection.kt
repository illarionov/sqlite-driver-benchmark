package at.released.sqlitedriverbenchmark.database

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

class TestSqliteConnection private constructor(
    val connection: SQLiteConnection,
) : AutoCloseable, SQLiteConnection by connection {
    fun setupConnection() {
        CONNECTION_DEFAULTS.forEach(connection::execSQL)
    }

    fun version(): String =
        connection.queryForString("SELECT sqlite_version()") ?: "Version is null"

    override fun close() {
        connection.close()
    }

    internal companion object {
        private val CONNECTION_DEFAULTS: List<String> = listOf(
            "journal_size_limit=-1",
            "locking_mode=EXCLUSIVE",
            "auto_vacuum=0",
            "cache_size=-2000",
            "encoding='UTF-8'",
            "ignore_check_constraints=false",
            "page_size=4096",
            "secure_delete=1",
            "synchronous=1",
            "temp_store=2",
            "threads=0",
            "trusted_schema=0",
            "wal_autocheckpoint=0",
            "journal_mode=WAL",
        ).map { "PRAGMA $it" }

        operator fun invoke(connection: SQLiteConnection): TestSqliteConnection {
            return TestSqliteConnection(connection).also(TestSqliteConnection::setupConnection)
        }
    }
}

