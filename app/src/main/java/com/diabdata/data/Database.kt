package com.diabdata.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.diabdata.dao.AppointmentDao
import com.diabdata.dao.HBA1CDao
import com.diabdata.dao.TreatmentDao
import com.diabdata.dao.WeightDao
import com.diabdata.data.converters.DateConverters
import com.diabdata.models.Appointment
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.Treatment
import com.diabdata.models.WeightEntry

@Database(
    entities = [
        WeightEntry::class,
        HBA1CEntry::class,
        Appointment::class,
        Treatment::class
    ],
    version = 1,
    exportSchema = false
)

@TypeConverters(DateConverters::class)
abstract class DiabDataDatabase : RoomDatabase() {

    abstract fun weightDao(): WeightDao
    abstract fun hba1cDao(): HBA1CDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun treatmentDao(): TreatmentDao

    companion object {
        @Volatile
        private var INSTANCE: DiabDataDatabase? = null

        fun getDatabase(context: Context): DiabDataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiabDataDatabase::class.java,
                    "diabdata_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
