package com.diabdata.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diabdata.dao.AppointmentDao
import com.diabdata.dao.HBA1CDao
import com.diabdata.dao.ImportantDateDao
import com.diabdata.dao.MedicationDao
import com.diabdata.dao.TreatmentDao
import com.diabdata.dao.WeightDao
import com.diabdata.data.converters.DateConverters
import com.diabdata.models.Appointment
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.ImportantDate
import com.diabdata.models.MedicationEntity
import com.diabdata.models.Treatment
import com.diabdata.models.WeightEntry
import com.diabdata.utils.MedicationInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WeightEntry::class,
        HBA1CEntry::class,
        Appointment::class,
        Treatment::class,
        ImportantDate::class,
        MedicationEntity::class
    ],
    version = 8,
    exportSchema = false
)

@TypeConverters(DateConverters::class)
abstract class DiabDataDatabase : RoomDatabase() {

    abstract fun weightDao(): WeightDao
    abstract fun hba1cDao(): HBA1CDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun treatmentDao(): TreatmentDao
    abstract fun importantDateDao(): ImportantDateDao
    abstract fun medicationDao(): MedicationDao

    fun getAllTableNames(): List<String> {
        val db = openHelper.readableDatabase
        val cursor = db.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name != 'room_master_table' AND name != 'android_metadata'"
        )
        val tables = mutableListOf<String>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            tables.add(name)
        }
        cursor.close()
        return tables
    }

    fun clearAllDataAndReset() {
        runInTransaction {

            val tables = getAllTableNames().filter { it != "medications" }

            tables.forEach { table ->

                openHelper.writableDatabase.execSQL("DELETE FROM $table")

                openHelper.writableDatabase.execSQL("DELETE FROM sqlite_sequence WHERE name='$table'")
            }
        }

        openHelper.writableDatabase.execSQL("VACUUM")
    }

    companion object {
        @Volatile
        private var INSTANCE: DiabDataDatabase? = null

        fun getDatabase(context: Context): DiabDataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiabDataDatabase::class.java,
                    "diabdata_database"
                )
                    .fallbackToDestructiveMigration(false)
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)

                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getDatabase(context)
                                val count = database.medicationDao().countAll()

                                if (count == 0) {
                                    MedicationInitializer(context, database).initialize()
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
