package com.example.jadwalsholattv

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.jadwalsholattv.ui.theme.JadwalSholatTVTheme

private const val PREFS_NAME = "pairing_prefs"
private const val KEY_IS_PAIRED = "is_paired"
private const val KEY_PAIRED_DEVICE = "paired_device"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pairingStore = PairingStore(this)

        setContent {
            JadwalSholatTVTheme {
                val isPaired = remember { mutableStateOf(pairingStore.isPaired()) }
                val pairedDevice = remember { mutableStateOf(pairingStore.getPairedDevice()) }

                if (isPaired.value) {
                    WeatherDashboardScreen(
                        pairedDevice = pairedDevice.value,
                        onResetPairing = {
                            pairingStore.clearPairing()
                            pairedDevice.value = null
                            isPaired.value = false
                        }
                    )
                } else {
                    PairingScreen(
                        onPairSuccess = { deviceCode ->
                            pairingStore.savePairing(deviceCode)
                            pairedDevice.value = deviceCode
                            isPaired.value = true
                        }
                    )
                }
            }
        }
    }
}

private class PairingStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isPaired(): Boolean = prefs.getBoolean(KEY_IS_PAIRED, false)

    fun getPairedDevice(): String? = prefs.getString(KEY_PAIRED_DEVICE, null)

    fun savePairing(deviceCode: String) {
        prefs.edit()
            .putBoolean(KEY_IS_PAIRED, true)
            .putString(KEY_PAIRED_DEVICE, deviceCode)
            .apply()
    }

    fun clearPairing() {
        prefs.edit()
            .putBoolean(KEY_IS_PAIRED, false)
            .remove(KEY_PAIRED_DEVICE)
            .apply()
    }
}

@Composable
private fun PairingScreen(onPairSuccess: (String) -> Unit) {
    var deviceCode by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111317))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pairing TV",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Masukkan kode pairing sekali saja. Data pairing akan disimpan.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
        OutlinedTextField(
            value = deviceCode,
            onValueChange = { deviceCode = it },
            label = { Text("Kode pairing") },
            singleLine = true
        )
        Button(
            onClick = { onPairSuccess(deviceCode.trim()) },
            enabled = deviceCode.isNotBlank(),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Simpan pairing")
        }
    }
}

@Composable
private fun WeatherDashboardScreen(
    pairedDevice: String?,
    onResetPairing: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111317))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Jadwal Sholat TV",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Perangkat ter-pairing: ${pairedDevice ?: "-"}",
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = "Aplikasi tidak perlu pairing ulang saat dibuka kembali.",
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
            Button(onClick = onResetPairing, modifier = Modifier.padding(top = 24.dp)) {
                Text("Lepas pairing")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PairingPreview() {
    JadwalSholatTVTheme {
        PairingScreen(onPairSuccess = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() {
    JadwalSholatTVTheme {
        WeatherDashboardScreen(pairedDevice = "TV-RUANG-01", onResetPairing = {})
    }
}
