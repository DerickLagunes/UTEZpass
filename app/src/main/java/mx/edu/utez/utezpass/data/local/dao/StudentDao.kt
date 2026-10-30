package mx.edu.utez.utezpass.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mx.edu.utez.utezpass.data.local.entity.Student

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Query("SELECT * FROM students WHERE studentId = :studentId")
    suspend fun getStudentById(studentId: String): Student?

    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE groupName = :groupName ORDER BY name ASC")
    fun getStudentsByGroup(groupName: String): Flow<List<Student>>

    @Query("SELECT DISTINCT groupName FROM students ORDER BY groupName ASC")
    fun getDistinctGroups(): Flow<List<String>>
}
