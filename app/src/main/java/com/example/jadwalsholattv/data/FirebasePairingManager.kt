package com.example.jadwalsholattv.data

import android.content.Context
import com.example.jadwalsholattv.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.random.Random

data class DeviceSettings(
    val mosqueName: String = "Belum diatur",
    val province: String = "Belum diatur",
    val city: String = "Belum diatur",
    val timezone: String = "Asia/Jakarta"
)

data class PairingUiState(
    val deviceId: String = "-",
    val pairingCode: String? = null,
    val paired: Boolean = false,
    val settings: DeviceSettings = DeviceSettings(),
    val errorMessage: String? = null
)

class FirebasePairingManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("pairing_prefs", Context.MODE_PRIVATE)
    private var deviceListener: ValueEventListener? = null
    private var deviceRef: DatabaseReference? = null
    private var cachedDeviceId: String = ""

    suspend fun initializeAndPublishPairingCode(): PairingUiState {
        val app = ensureFirebaseApp()
        val database = FirebaseDatabase.getInstance(app, BuildConfig.FIREBASE_DB_URL)
        val deviceId = getOrCreateDeviceId()
        cachedDeviceId = deviceId
        val pairingCode = generatePairingCode()
        val now = System.currentTimeMillis()
        val expiresAt = now + 5 * 60 * 1000

        val deviceMeta = hashMapOf<String, Any?>(
            "platform" to "android_tv",
            "pairingCode" to pairingCode,
            "pairingCodeExpiresAt" to expiresAt,
            "paired" to false,
            "updatedAt" to ServerValue.TIMESTAMP,
            "lastSeenAt" to ServerValue.TIMESTAMP
        )

        val pairingPayload = hashMapOf<String, Any?>(
            "deviceId" to deviceId,
            "expiresAt" to expiresAt,
            "used" to false,
            "createdAt" to ServerValue.TIMESTAMP
        )

        val updates = hashMapOf<String, Any>(
            "/devices/$deviceId/meta" to deviceMeta,
            "/pairingCodes/$pairingCode" to pairingPayload
        )

        database.reference.updateChildren(updates).await()

        return PairingUiState(
            deviceId = deviceId,
            pairingCode = pairingCode
        )
    }

    fun observeDevice(onChanged: (PairingUiState) -> Unit, onError: (String) -> Unit) {
        if (cachedDeviceId.isBlank()) {
            onError("Device ID belum siap.")
            return
        }
        val app = FirebaseApp.getInstance()
        val database = FirebaseDatabase.getInstance(app, BuildConfig.FIREBASE_DB_URL)
        deviceRef = database.reference.child("devices").child(cachedDeviceId)
        deviceListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val meta = snapshot.child("meta")
                val settings = snapshot.child("settings")

                onChanged(
                    PairingUiState(
                        deviceId = cachedDeviceId,
                        pairingCode = meta.child("pairingCode").getValue(String::class.java),
                        paired = meta.child("paired").getValue(Boolean::class.java) ?: false,
                        settings = DeviceSettings(
                            mosqueName = settings.child("mosqueName").getValue(String::class.java)
                                ?: "Belum diatur",
                            province = settings.child("province").getValue(String::class.java)
                                ?: "Belum diatur",
                            city = settings.child("city").getValue(String::class.java)
                                ?: "Belum diatur",
                            timezone = settings.child("timezone").getValue(String::class.java)
                                ?: "Asia/Jakarta"
                        )
                    )
                )
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                onError(error.message)
            }
        }
        deviceRef?.addValueEventListener(deviceListener as ValueEventListener)
    }

    fun stopObserving() {
        val listener = deviceListener ?: return
        deviceRef?.removeEventListener(listener)
        deviceListener = null
    }

    private fun ensureFirebaseApp(): FirebaseApp {
        FirebaseApp.getApps(context).firstOrNull()?.let { return it }
        FirebaseApp.initializeApp(context)?.let { return it }

        val apiKey = BuildConfig.FIREBASE_API_KEY
        val appId = BuildConfig.FIREBASE_APP_ID
        val projectId = BuildConfig.FIREBASE_PROJECT_ID
        val dbUrl = BuildConfig.FIREBASE_DB_URL

        if (apiKey.isBlank() || appId.isBlank() || projectId.isBlank()) {
            error(
                "Firebase belum siap. Tambahkan app/google-services.json ATAU isi FIREBASE_API_KEY, FIREBASE_APP_ID, dan FIREBASE_PROJECT_ID di app/build.gradle.kts."
            )
        }

        val options = FirebaseOptions.Builder()
            .setApiKey(apiKey)
            .setApplicationId(appId)
            .setProjectId(projectId)
            .setDatabaseUrl(dbUrl)
            .build()

        return FirebaseApp.initializeApp(context, options)
            ?: error("Gagal inisialisasi FirebaseApp.")
    }

    private fun getOrCreateDeviceId(): String {
        val existing = prefs.getString("device_id", null)
        if (!existing.isNullOrBlank()) return existing
        val newId = UUID.randomUUID().toString()
        prefs.edit().putString("device_id", newId).apply()
        return newId
    }

    private fun generatePairingCode(): String {
        return Random.nextInt(100_000, 1_000_000).toString()
    }
}
