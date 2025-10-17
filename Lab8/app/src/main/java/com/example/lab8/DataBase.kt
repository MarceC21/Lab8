package com.example.lab8



import android.content.Context
import androidx.room.Room
import com.example.lab8.Data.AppDatabase


//Para incializar la base de datos una vez
object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            )
                .fallbackToDestructiveMigration() // Opcional: para desarrollo
                .build()
            INSTANCE = instance
            instance
        }
    }
}
