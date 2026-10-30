package mx.edu.utez.utezpass.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import java.io.IOException
import java.nio.charset.Charset

object NfcUtils {

    /**
     * Reads the student ID text content stored in an NDEF text record on the provided [tag].
     */
    fun readTextFromTag(tag: Tag): Result<String> {
        val ndef = Ndef.get(tag) ?: return Result.failure(Exception("La tarjeta no soporta el formato NDEF."))
        return try {
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            ndef.close()

            if (ndefMessage == null || ndefMessage.records.isEmpty()) {
                return Result.failure(Exception("La tarjeta NFC está vacía."))
            }

            for (record in ndefMessage.records) {
                if (record.tnf == NdefRecord.TNF_WELL_KNOWN && record.payload.isNotEmpty()) {
                    val payload = record.payload
                    val textEncoding = if ((payload[0].toInt() and 0x80) == 0) Charsets.UTF_8 else Charset.forName("UTF-16")
                    val languageCodeLength = payload[0].toInt() and 0x3F
                    val text = String(
                        payload,
                        languageCodeLength + 1,
                        payload.size - languageCodeLength - 1,
                        textEncoding
                    ).trim()
                    if (text.isNotEmpty()) {
                        return Result.success(text)
                    }
                }
            }
            Result.failure(Exception("No se encontró ningún registro de texto en la tarjeta."))
        } catch (e: Exception) {
            try { ndef.close() } catch (_: IOException) {}
            Result.failure(e)
        }
    }

    /**
     * Writes the given [textToSave] as an NDEF Text Record onto the provided [tag].
     */
    fun writeTextToTag(tag: Tag, textToSave: String): Result<Unit> {
        val record = createTextRecord(textToSave)
        val message = NdefMessage(arrayOf(record))

        val ndef = Ndef.get(tag)
        return if (ndef != null) {
            try {
                ndef.connect()
                if (!ndef.isWritable) {
                    ndef.close()
                    return Result.failure(Exception("La tarjeta NFC es de solo lectura."))
                }
                if (ndef.maxSize < message.toByteArray().size) {
                    ndef.close()
                    return Result.failure(Exception("Capacidad insuficiente en la tarjeta NFC."))
                }
                ndef.writeNdefMessage(message)
                ndef.close()
                Result.success(Unit)
            } catch (e: Exception) {
                try { ndef.close() } catch (_: IOException) {}
                Result.failure(e)
            }
        } else {
            val formatable = NdefFormatable.get(tag)
            if (formatable != null) {
                try {
                    formatable.connect()
                    formatable.format(message)
                    formatable.close()
                    Result.success(Unit)
                } catch (e: Exception) {
                    try { formatable.close() } catch (_: IOException) {}
                    Result.failure(e)
                }
            } else {
                Result.failure(Exception("Esta tarjeta no es compatible con NDEF."))
            }
        }
    }

    private fun createTextRecord(payloadText: String): NdefRecord {
        val langBytes = "en".toByteArray(Charsets.US_ASCII)
        val textBytes = payloadText.toByteArray(Charsets.UTF_8)
        val payload = ByteArray(1 + langBytes.size + textBytes.size)

        // Status byte: UTF-8 encoding (bit 7 = 0) + language code length
        payload[0] = langBytes.size.toByte()
        System.arraycopy(langBytes, 0, payload, 1, langBytes.size)
        System.arraycopy(textBytes, 0, payload, 1 + langBytes.size, textBytes.size)

        return NdefRecord(
            NdefRecord.TNF_WELL_KNOWN,
            NdefRecord.RTD_TEXT,
            ByteArray(0),
            payload
        )
    }
}
