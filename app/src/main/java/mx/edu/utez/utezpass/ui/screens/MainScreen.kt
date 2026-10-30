package mx.edu.utez.utezpass.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.edu.utez.utezpass.ui.viewmodel.MainViewModel
import mx.edu.utez.utezpass.ui.viewmodel.NfcState

enum class AppTab(val title: String) {
    WRITE_NFC("Grabar NFC"),
    ATTENDANCE("Toma de Asistencia")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    val uiMessage by viewModel.uiMessage.collectAsState()
    val nfcState by viewModel.nfcState.collectAsState()

    // Show Snackbar when ViewModel emits a user message
    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUiMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "UTEZpass",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // NFC State Badge
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = when (nfcState) {
                            is NfcState.Writing -> MaterialTheme.colorScheme.tertiary
                            is NfcState.Reading -> MaterialTheme.colorScheme.primary
                            NfcState.Idle -> MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            text = when (nfcState) {
                                is NfcState.Writing -> "NFC: Grabando"
                                is NfcState.Reading -> "NFC: Leyendo"
                                NfcState.Idle -> "NFC: Listo"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = when (nfcState) {
                                is NfcState.Writing -> MaterialTheme.colorScheme.onTertiary
                                is NfcState.Reading -> MaterialTheme.colorScheme.onPrimary
                                NfcState.Idle -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = (selectedTabIndex == 0),
                    onClick = { selectedTabIndex = 0 },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Grabar NFC") },
                    label = { Text("Grabar NFC") }
                )
                NavigationBarItem(
                    selected = (selectedTabIndex == 1),
                    onClick = { selectedTabIndex = 1 },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Asistencia") },
                    label = { Text("Asistencia") }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> WriteNfcScreen(viewModel = viewModel)
                    1 -> AttendanceScreen(viewModel = viewModel)
                }
            }
        }
    }
}
