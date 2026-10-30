package mx.edu.utez.utezpass.data.repository

import kotlinx.coroutines.flow.Flow
import mx.edu.utez.utezpass.data.local.dao.AttendanceRecordDao
import mx.edu.utez.utezpass.data.local.dao.StudentDao
import mx.edu.utez.utezpass.data.local.entity.AttendanceRecord
import mx.edu.utez.utezpass.data.local.entity.AttendanceWithStudent
import mx.edu.utez.utezpass.data.local.entity.Student

class AttendanceRepository(
    private val studentDao: StudentDao,
    private val attendanceRecordDao: AttendanceRecordDao
) {
    suspend fun saveStudent(student: Student) {
        studentDao.insertStudent(student)
    }

    suspend fun getStudentById(studentId: String): Student? {
        return studentDao.getStudentById(studentId)
    }

    fun getDistinctGroups(): Flow<List<String>> {
        return studentDao.getDistinctGroups()
    }

    suspend fun recordAttendance(studentId: String, isLate: Boolean = false): Long {
        val record = AttendanceRecord(
            studentId = studentId,
            timestamp = System.currentTimeMillis(),
            late = isLate
        )
        return attendanceRecordDao.insertRecord(record)
    }

    suspend fun updateLateStatus(recordId: Int, isLate: Boolean) {
        attendanceRecordDao.updateLateStatus(recordId, isLate)
    }

    fun getAttendanceForGroup(groupName: String): Flow<List<AttendanceWithStudent>> {
        return if (groupName == "Todos" || groupName.isEmpty()) {
            attendanceRecordDao.getAllAttendanceWithStudent()
        } else {
            attendanceRecordDao.getAttendanceWithStudentByGroup(groupName)
        }
    }
}
