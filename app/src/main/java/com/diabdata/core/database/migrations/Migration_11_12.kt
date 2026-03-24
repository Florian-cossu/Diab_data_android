package com.diabdata.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS medical_devices_infos (
                cipGtin TEXT PRIMARY KEY NOT NULL,
                manufacturer TEXT NOT NULL,
                deviceType TEXT NOT NULL DEFAULT '',
                fullName TEXT NOT NULL DEFAULT '',
                daysLifespan INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}
