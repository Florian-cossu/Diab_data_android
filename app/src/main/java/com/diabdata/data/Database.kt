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
import com.diabdata.dao.MedicalDeviceDao
import com.diabdata.dao.MedicalDevicesInfoDao
import com.diabdata.dao.MedicationDao
import com.diabdata.dao.TreatmentDao
import com.diabdata.dao.UserDetailsDao
import com.diabdata.dao.WeightDao
import com.diabdata.data.converters.DateConverters
import com.diabdata.data.migrations.ALL_MIGRATIONS
import com.diabdata.models.Appointment
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.ImportantDate
import com.diabdata.models.MedicalDeviceEntry
import com.diabdata.models.MedicalDeviceInfoEntity
import com.diabdata.models.MedicationEntity
import com.diabdata.models.Treatment
import com.diabdata.models.UserDetails
import com.diabdata.models.WeightEntry
import com.diabdata.utils.MedicalDevicesInitializer
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
        MedicalDeviceEntry::class,
        MedicationEntity::class,
        MedicalDeviceInfoEntity::class,
        UserDetails::class
    ],
    version = 20,
    exportSchema = true
)

@TypeConverters(DateConverters::class)
abstract class DiabDataDatabase : RoomDatabase() {

    abstract fun weightDao(): WeightDao
    abstract fun hba1cDao(): HBA1CDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun treatmentDao(): TreatmentDao
    abstract fun importantDateDao(): ImportantDateDao
    abstract fun medicationDao(): MedicationDao
    abstract fun medicalDevicesDao(): MedicalDeviceDao
    abstract fun medicalDevicesInfoDao(): MedicalDevicesInfoDao
    abstract fun userDetailsDao(): UserDetailsDao


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

            val tables = getAllTableNames().filter {
                it != "medications" && it != "medical_devices_infos"
            }

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
                    .addMigrations(*ALL_MIGRATIONS)
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)

                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getDatabase(context)
                                val countMedicationInfo = database.medicationDao().countAll()
                                val countDeviceInfo = database.medicalDevicesInfoDao().countAll()

                                if (countMedicationInfo == 0) {
                                    MedicationInitializer(context, database).initialize()
                                }

                                if (countDeviceInfo == 0) {
                                    MedicalDevicesInitializer(context, database).initialize()
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
