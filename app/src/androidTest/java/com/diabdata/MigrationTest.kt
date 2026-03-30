package com.diabdata

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.core.database.migrations.ALL_MIGRATIONS
import com.diabdata.core.database.migrations.MIGRATION_20_21
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val diabDataTestDb = "migration-test"

    // Array of all migrations.
    private val migrationsList = ALL_MIGRATIONS

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        assetsFolder = DiabDataDatabase::class.java.canonicalName!!,
        openFactory = FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        helper.createDatabase(diabDataTestDb, 19).apply {
            close()
        }

        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            DiabDataDatabase::class.java,
            diabDataTestDb
        ).addMigrations(*migrationsList).build().apply {
            openHelper.writableDatabase.close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate20To21Test() {
        val db = helper.createDatabase(diabDataTestDb, 20)

        db.execSQL(
            """
                INSERT INTO appointments 
                (id, date, doctor, type, createdAt, isArchived, notes, updatedAt)
                VALUES (1, '2026-01-15', 'Dr. Dre', 'APPOINTMENT', '2026-01-15', 0, '', '2026-01-15')
            """.trimIndent()
        )

        db.close()

        val updatedDb = helper.runMigrationsAndValidate(diabDataTestDb, 21, true, MIGRATION_20_21)

        val updatedAppointment = updatedDb.query("""
            SELECT date FROM appointments
        """.trimIndent())

        updatedAppointment.moveToFirst()

        assertEquals(1, updatedAppointment.count)
        assertEquals("2026-01-15T00:00", updatedAppointment.getString(0))

        updatedDb.close()
    }
}