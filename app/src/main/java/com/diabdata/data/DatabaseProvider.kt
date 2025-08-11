package com.diabdata.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
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
