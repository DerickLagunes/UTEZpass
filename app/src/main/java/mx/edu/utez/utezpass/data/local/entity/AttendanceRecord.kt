package mx.edu.utez.utezpass.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId"])]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val late: Boolean = false
)
