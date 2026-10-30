package mx.edu.utez.utezpass.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mx.edu.utez.utezpass.data.local.dao.AttendanceRecordDao
import mx.edu.utez.utezpass.data.local.dao.StudentDao
import mx.edu.utez.utezpass.data.local.entity.AttendanceRecord
import mx.edu.utez.utezpass.data.local.entity.Student

@Database(
    entities = [Student::class, AttendanceRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun attendanceRecordDao(): AttendanceRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "utezpass_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
