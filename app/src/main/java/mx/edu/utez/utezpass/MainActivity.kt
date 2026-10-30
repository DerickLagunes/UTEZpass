package mx.edu.utez.utezpass

import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import mx.edu.utez.utezpass.ui.screens.MainScreen
import mx.edu.utez.utezpass.ui.theme.UTEZpassTheme
import mx.edu.utez.utezpass.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(
                this,
                "Este dispositivo no cuenta con hardware NFC. La lectura/escritura física de tarjetas no estará disponible.",
                Toast.LENGTH_LONG
            ).show()
        }

        setContent {
            UTEZpassTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.let { adapter ->
            if (!adapter.isEnabled) {
                Toast.makeText(this, "Por favor activa el NFC en la configuración de tu dispositivo.", Toast.LENGTH_SHORT).show()
            } else {
                val flags = NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or
                        NfcAdapter.FLAG_READER_NFC_V or
                        NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS

                adapter.enableReaderMode(
                    this,
                    { tag ->
                        runOnUiThread {
                            viewModel.onTagDiscovered(tag)
                        }
                    },
                    flags,
                    null
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }
}
