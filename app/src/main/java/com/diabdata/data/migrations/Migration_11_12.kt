package com.diabdata.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS medical_devices_infos (
                cipGtin TEXT PRIMARY KEY NOT NULL,
                manufacturer TEXT NOT NULL,
                deviceType TEXT,
                fullName TEXT,
                daysLifespan INTEGER NOT NULL,
            )
            """.trimIndent()
        )
    }
}