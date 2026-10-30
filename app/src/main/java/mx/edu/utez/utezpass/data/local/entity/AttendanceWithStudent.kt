package mx.edu.utez.utezpass.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class AttendanceWithStudent(
    @Embedded
    val record: AttendanceRecord,

    @Relation(
        parentColumn = "studentId",
        entityColumn = "studentId"
    )
    val student: Student
)
