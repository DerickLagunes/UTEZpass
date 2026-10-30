package mx.edu.utez.utezpass.ui.viewmodel

import android.app.Application
import android.nfc.Tag
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.edu.utez.utezpass.data.local.AppDatabase
import mx.edu.utez.utezpass.data.local.entity.AttendanceWithStudent
import mx.edu.utez.utezpass.data.local.entity.Student
import mx.edu.utez.utezpass.data.repository.AttendanceRepository
import mx.edu.utez.utezpass.nfc.NfcUtils

sealed class NfcState {
    object Idle : NfcState()
    data class Writing(val student: Student) : NfcState()
    object Reading : NfcState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AttendanceRepository(database.studentDao(), database.attendanceRecordDao())
    }

    // Form states for NFC Recording Screen
    var studentIdInput = MutableStateFlow("")
        private set
    var nameInput = MutableStateFlow("")
        private set
    var groupInput = MutableStateFlow("4°A")
        private set

    val availableGroups = listOf("4°A", "4°B", "4°C", "5°A", "5°B")

    // Attendance Screen Group Filter
    val selectedGroupTab = MutableStateFlow("Todos")

    // NFC Mode State
    private val _nfcState = MutableStateFlow<NfcState>(NfcState.Idle)
    val nfcState: StateFlow<NfcState> = _nfcState.asStateFlow()

    // UI Feedback Message
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // Attendance Records Reactive List
    val attendanceRecords: StateFlow<List<AttendanceWithStudent>> = selectedGroupTab
        .flatMapLatest { group ->
            repository.getAttendanceForGroup(group)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onStudentIdChanged(value: String) {
        studentIdInput.value = value
    }

    fun onNameChanged(value: String) {
        nameInput.value = value
    }

    fun onGroupChanged(value: String) {
        groupInput.value = value
    }

    fun onGroupTabSelected(group: String) {
        selectedGroupTab.value = group
    }

    fun prepareToWriteNfc() {
        val id = studentIdInput.value.trim()
        val name = nameInput.value.trim()
        val group = groupInput.value.trim()

        if (id.isEmpty()) {
            _uiMessage.value = "Por favor ingrese la matrícula o ID del alumno."
            return
        }
        if (name.isEmpty()) {
            _uiMessage.value = "Por favor ingrese el nombre del alumno."
            return
        }
        if (group.isEmpty()) {
            _uiMessage.value = "Por favor seleccione el grupo del alumno."
            return
        }

        val student = Student(studentId = id, name = name, groupName = group)

        viewModelScope.launch {
            repository.saveStudent(student)
            _nfcState.value = NfcState.Writing(student)
            _uiMessage.value = "Alumno guardado. Acerque la tarjeta NFC para grabar..."
        }
    }

    fun startScanningAttendance() {
        _nfcState.value = NfcState.Reading
        _uiMessage.value = "Modo de escaneo activo. Acerque la tarjeta NFC del alumno."
    }

    fun cancelNfcOperation() {
        _nfcState.value = NfcState.Idle
        _uiMessage.value = "Operación NFC cancelada."
    }

    fun onTagDiscovered(tag: Tag) {
        when (val currentState = _nfcState.value) {
            is NfcState.Writing -> {
                val student = currentState.student
                val result = NfcUtils.writeTextToTag(tag, student.studentId)
                if (result.isSuccess) {
                    _uiMessage.value = "¡Éxito! Tarjeta grabada para ${student.name} (${student.studentId})"
                    _nfcState.value = NfcState.Idle
                    // Clear form after successful write
                    studentIdInput.value = ""
                    nameInput.value = ""
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Error desconocido"
                    _uiMessage.value = "Error al grabar tarjeta: $errorMsg"
                }
            }
            is NfcState.Reading -> {
                val result = NfcUtils.readTextFromTag(tag)
                if (result.isSuccess) {
                    val scannedId = result.getOrNull() ?: ""
                    viewModelScope.launch {
                        val student = repository.getStudentById(scannedId)
                        if (student != null) {
                            repository.recordAttendance(studentId = student.studentId)
                            _uiMessage.value = "¡Asistencia registrada! ${student.name} (${student.groupName})"
                        } else {
                            _uiMessage.value = "El ID '$scannedId' leído de la tarjeta no existe en la BD."
                        }
                    }
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Error desconocido"
                    _uiMessage.value = "Error al leer tarjeta: $errorMsg"
                }
            }
            NfcState.Idle -> {
                // Do nothing if idle
            }
        }
    }

    fun toggleLateStatus(recordId: Int, isLate: Boolean) {
        viewModelScope.launch {
            repository.updateLateStatus(recordId, isLate)
        }
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(application) as T
        }
    }
}
