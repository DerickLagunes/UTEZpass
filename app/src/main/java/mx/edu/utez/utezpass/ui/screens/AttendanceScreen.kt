package mx.edu.utez.utezpass.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utez.utezpass.data.local.entity.AttendanceWithStudent
import mx.edu.utez.utezpass.ui.viewmodel.MainViewModel
import mx.edu.utez.utezpass.ui.viewmodel.NfcState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedGroupTab.collectAsState()
    val attendanceList by viewModel.attendanceRecords.collectAsState()
    val nfcState by viewModel.nfcState.collectAsState()

    val isScanningMode = nfcState is NfcState.Reading
    val groups = listOf("Todos", "4°A", "4°B", "4°C", "5°A", "5°B")

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Group Tabs Selector
        ScrollableTabRow(
            selectedTabIndex = groups.indexOf(selectedTab).coerceAtLeast(0),
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            groups.forEach { groupName ->
                Tab(
                    selected = (selectedTab == groupName),
                    onClick = { viewModel.onGroupTabSelected(groupName) },
                    text = {
                        Text(
                            text = groupName,
                            fontWeight = if (selectedTab == groupName) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // NFC Scanner Status & Trigger Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isScanningMode) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Nfc,
                                    contentDescription = "Escanear NFC",
                                    tint = if (isScanningMode) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Control de Lectura NFC",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isScanningMode) "Escáner Activo: Acerque tarjeta..." else "Pulse para activar la lectura de tarjetas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (isScanningMode) {
                        OutlinedButton(
                            onClick = { viewModel.cancelNfcOperation() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Detener Escáner")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.startScanningAttendance() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Nfc, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Escanear Tarjeta NFC", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Attendance Records Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registros de Asistencia ($selectedTab)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${attendanceList.size} Alumnos",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Attendance List
            if (attendanceList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentTurnedIn,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "No hay registros de asistencia para el grupo '$selectedTab'.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = attendanceList,
                        key = { it.record.id }
                    ) { item ->
                        AttendanceCard(
                            item = item,
                            onLateToggled = { isLate ->
                                viewModel.toggleLateStatus(item.record.id, isLate)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceCard(
    item: AttendanceWithStudent,
    onLateToggled: (Boolean) -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy - hh:mm:ss a", Locale("es", "MX"))
    val formattedTime = dateFormat.format(Date(item.record.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.record.late)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AssistChip(
                        onClick = { },
                        label = { Text(item.student.groupName) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ID: ${item.student.studentId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Late Checkbox
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Retardo",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (item.record.late) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Checkbox(
                    checked = item.record.late,
                    onCheckedChange = { onLateToggled(it) }
                )
            }
        }
    }
}
