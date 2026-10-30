package mx.edu.utez.utezpass.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import mx.edu.utez.utezpass.data.local.entity.AttendanceRecord
import mx.edu.utez.utezpass.data.local.entity.AttendanceWithStudent

@Dao
interface AttendanceRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecord): Long

    @Query("UPDATE attendance_records SET late = :late WHERE id = :recordId")
    suspend fun updateLateStatus(recordId: Int, late: Boolean)

    @Transaction
    @Query("""
        SELECT * FROM attendance_records 
        ORDER BY timestamp DESC
    """)
    fun getAllAttendanceWithStudent(): Flow<List<AttendanceWithStudent>>

    @Transaction
    @Query("""
        SELECT ar.* FROM attendance_records ar
        INNER JOIN students s ON ar.studentId = s.studentId
        WHERE s.groupName = :groupName
        ORDER BY ar.timestamp DESC
    """)
    fun getAttendanceWithStudentByGroup(groupName: String): Flow<List<AttendanceWithStudent>>
}
