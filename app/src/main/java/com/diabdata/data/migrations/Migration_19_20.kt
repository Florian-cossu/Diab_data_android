package com.diabdata.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_19_20 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // No-op : Fixed build after the viewmodel and reepository
        // updates were forgotten
    }
}