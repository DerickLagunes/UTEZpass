package mx.edu.utez.utezpass.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey
    val studentId: String,
    val name: String,
    val groupName: String
)
