package mx.edu.utez.utezpass.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import mx.edu.utez.utezpass.data.local.entity.Student
import mx.edu.utez.utezpass.ui.viewmodel.MainViewModel
import mx.edu.utez.utezpass.ui.viewmodel.NfcState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WriteNfcScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val studentId by viewModel.studentIdInput.collectAsState()
    val name by viewModel.nameInput.collectAsState()
    val group by viewModel.groupInput.collectAsState()
    val nfcState by viewModel.nfcState.collectAsState()

    val isWritingMode = nfcState is NfcState.Writing

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Banner
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = "Grabar NFC",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Grabación de Tarjetas NFC",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Registra los datos del alumno y escríbelos en su etiqueta NDEF.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Student Data Form Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Datos del Alumno",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Matrícula / Student ID
                OutlinedTextField(
                    value = studentId,
                    onValueChange = { viewModel.onStudentIdChanged(it) },
                    label = { Text("Matrícula / ID") },
                    placeholder = { Text("Ej. 20233tn001") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    singleLine = true,
                    enabled = !isWritingMode,
                    modifier = Modifier.fillMaxWidth()
                )

                // Student Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.onNameChanged(it) },
                    label = { Text("Nombre Completo") },
                    placeholder = { Text("Ej. Juan Pérez Gómez") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    enabled = !isWritingMode,
                    modifier = Modifier.fillMaxWidth()
                )

                // Group Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Grupo:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.availableGroups.forEach { groupOption ->
                            FilterChip(
                                selected = (group == groupOption),
                                onClick = { if (!isWritingMode) viewModel.onGroupChanged(groupOption) },
                                label = { Text(groupOption) },
                                leadingIcon = if (group == groupOption) {
                                    { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Button: Grabar NFC
                Button(
                    onClick = { viewModel.prepareToWriteNfc() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isWritingMode && studentId.isNotBlank() && name.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Grabar en Tarjeta NFC",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Active NFC Writing Status Banner/Card
        AnimatedVisibility(
            visible = isWritingMode,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val currentStudent = (nfcState as? NfcState.Writing)?.student
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = "Ready for NFC",
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Listo para Grabar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )

                    Text(
                        text = "Acerca la tarjeta NFC a la parte posterior de tu teléfono para escribir la matrícula '${currentStudent?.studentId}'.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )

                    OutlinedButton(
                        onClick = { viewModel.cancelNfcOperation() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cancelar Grabación")
                    }
                }
            }
        }
    }
}
