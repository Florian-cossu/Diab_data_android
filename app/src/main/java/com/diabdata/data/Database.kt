package com.diabdata.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.diabdata.dao.AppointmentDao
import com.diabdata.dao.DiagnosisDateDao
import com.diabdata.dao.HBA1CDao
import com.diabdata.dao.TreatmentDao
import com.diabdata.dao.WeightDao
import com.diabdata.data.converters.DateConverters
import com.diabdata.models.Appointment
import com.diabdata.models.DiagnosisDate
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.Treatment
import com.diabdata.models.WeightEntry

@Database(
    entities = [
        WeightEntry::class,
        HBA1CEntry::class,
        Appointment::class,
        Treatment::class,
        DiagnosisDate::class
    ],
    version = 3,
    exportSchema = false
)

@TypeConverters(DateConverters::class)
abstract class DiabDataDatabase : RoomDatabase() {

    abstract fun weightDao(): WeightDao
    abstract fun hba1cDao(): HBA1CDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun treatmentDao(): TreatmentDao
    abstract fun diagnosisDao(): DiagnosisDateDao

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
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
