package com.example.jadwalsholattv.data

import android.content.Context
import android.util.Log
import com.example.jadwalsholattv.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlin.random.Random

data class DeviceSettings(
    val mosqueName: String = "Belum diatur",
    val province: String = "Belum diatur",
    val city: String = "Belum diatur",
    val timezone: String = "Asia/Jakarta",
    val videoUrl: String = "",
    val runningText: String = "",
    val runningTextSpeed: Float = 5f,
    val runningTextBrightness: Float = 80f,
    val educationVideos: List<EducationVideo> = emptyList(),
    val hadithText: String = "",
    val hadithSource: String = ""
)

data class PairingUiState(
    val deviceId: String = "-",
    val pairingCode: String? = null,
    val paired: Boolean = false,
    val settings: DeviceSettings = DeviceSettings(),
    val todaySchedule: DailyPrayerSchedule? = null,
    val tomorrowSchedule: DailyPrayerSchedule? = null,
    val errorMessage: String? = null
)

data class EducationVideo(
    val id: String,
    val title: String,
    val url: String,
    val order: Int = 0
)

data class DailyPrayerSchedule(
    val date: String,
    val fajr: String = "-",
    val syuruk: String = "-",
    val dzuhur: String = "-",
    val ashar: String = "-",
    val maghrib: String = "-",
    val isya: String = "-"
)

class FirebasePairingManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("pairing_prefs", Context.MODE_PRIVATE)
    private var deviceListener: ValueEventListener? = null
    private var deviceRef: DatabaseReference? = null
    private var cachedDeviceId: String = ""
    private var lastPairedState: Boolean? = null

    companion object {
        private const val TAG = "FirebasePairing"
        private var persistenceEnabled = false
    }

    // ════════════════════════════════════════════════════════════
    // Inisialisasi & Pairing
    // ════════════════════════════════════════════════════════════

    suspend fun initializeAndPublishPairingCode(forceNewCode: Boolean = false): PairingUiState {
        val app = ensureFirebaseApp()
        val database = FirebaseDatabase.getInstance(app, BuildConfig.FIREBASE_DB_URL)

        // Layer 1: aktifkan Firebase offline persistence (hanya sekali)
        if (!persistenceEnabled) {
            try {
                database.setPersistenceEnabled(true)
                Log.i(TAG, "Firebase offline persistence aktif")
            } catch (e: Exception) {
                Log.w(TAG, "setPersistenceEnabled dilewati: ${e.message}")
            }
            persistenceEnabled = true
        }

        val deviceId = getOrCreateDeviceId()
        cachedDeviceId = deviceId
        val now = System.currentTimeMillis()

        // Ambil data dari Firebase (offline persistence akan return cache jika tidak ada internet)
        val deviceSnapshot = try {
            database.reference
                .child("devices")
                .child(deviceId)
                .get()
                .await()
        } catch (e: Exception) {
            // Tidak ada internet DAN belum ada Firebase cache → pakai Layer 2 (SharedPreferences)
            Log.w(TAG, "Offline & no Firebase cache – memuat dari SharedPreferences", e)
            return loadLocalCache()
                ?: PairingUiState(deviceId = deviceId, errorMessage = "Offline, belum ada data tersimpan")
        }

        val meta = deviceSnapshot.child("meta")
        val settings = deviceSnapshot.child("settings")
        val schedule = deviceSnapshot.child("schedule")
        val isAlreadyPaired = meta.child("paired").getValue(Boolean::class.java) ?: false
        val deviceSettings = settings.toDeviceSettings()
        val scheduleState = schedule.toScheduleState(deviceSettings.timezone)

        if (lastPairedState == null) lastPairedState = isAlreadyPaired

        if (isAlreadyPaired && !forceNewCode) {
            silentUpdate(database.reference.child("devices").child(deviceId).child("meta").child("lastSeenAt"))
            val state = PairingUiState(
                deviceId = deviceId,
                pairingCode = meta.child("pairingCode").getValue(String::class.java),
                paired = true,
                settings = deviceSettings,
                todaySchedule = scheduleState.first,
                tomorrowSchedule = scheduleState.second
            )
            saveLocalCache(state)
            return state
        }

        val existingCode = meta.child("pairingCode").getValue(String::class.java)
        val existingExpiresAt = meta.child("pairingCodeExpiresAt").getValue(Long::class.java) ?: 0L

        if (!forceNewCode && !existingCode.isNullOrBlank() && existingExpiresAt > now) {
            silentUpdate(database.reference.child("devices").child(deviceId).child("meta").child("lastSeenAt"))
            val state = PairingUiState(
                deviceId = deviceId,
                pairingCode = existingCode,
                paired = false,
                settings = deviceSettings,
                todaySchedule = scheduleState.first,
                tomorrowSchedule = scheduleState.second
            )
            saveLocalCache(state)
            return state
        }

        val pairingCode = generatePairingCode()
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

        try {
            database.reference.updateChildren(updates).await()
        } catch (e: Exception) {
            Log.w(TAG, "Tidak bisa tulis kode pairing (offline?): ${e.message}")
        }

        lastPairedState = false
        val state = PairingUiState(
            deviceId = deviceId,
            pairingCode = pairingCode,
            paired = false,
            settings = deviceSettings,
            todaySchedule = scheduleState.first,
            tomorrowSchedule = scheduleState.second
        )
        saveLocalCache(state)
        return state
    }

    // ════════════════════════════════════════════════════════════
    // Real-time Listener
    // ════════════════════════════════════════════════════════════

    fun observeDevice(onChanged: (PairingUiState) -> Unit, onError: (String) -> Unit) {
        if (cachedDeviceId.isBlank()) {
            onError("Device ID belum siap.")
            return
        }
        val app = FirebaseApp.getInstance()
        val database = FirebaseDatabase.getInstance(app, BuildConfig.FIREBASE_DB_URL)
        deviceRef = database.reference.child("devices").child(cachedDeviceId)

        // Layer 1: pastikan node ini selalu tersinkron untuk akses offline
        deviceRef?.keepSynced(true)

        deviceListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val meta = snapshot.child("meta")
                val settings = snapshot.child("settings")
                val schedule = snapshot.child("schedule")

                val isPaired = meta.child("paired").getValue(Boolean::class.java) ?: false
                val pairingCode = meta.child("pairingCode").getValue(String::class.java)
                val expiresAt = meta.child("pairingCodeExpiresAt").getValue(Long::class.java) ?: 0L
                val now = System.currentTimeMillis()
                val deviceSettings = settings.toDeviceSettings()
                val scheduleState = schedule.toScheduleState(deviceSettings.timezone)

                val justUnpaired = (lastPairedState == true && !isPaired)
                lastPairedState = isPaired

                if (!isPaired && (justUnpaired || pairingCode.isNullOrBlank() || expiresAt < now)) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val newState = initializeAndPublishPairingCode(forceNewCode = justUnpaired)
                            withContext(Dispatchers.Main) {
                                saveLocalCache(newState)
                                onChanged(newState)
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                onError(e.message ?: "Gagal membuat kode baru otomatis")
                            }
                        }
                    }
                } else {
                    val newState = PairingUiState(
                        deviceId = cachedDeviceId,
                        pairingCode = pairingCode,
                        paired = isPaired,
                        settings = deviceSettings,
                        todaySchedule = scheduleState.first,
                        tomorrowSchedule = scheduleState.second
                    )
                    // Layer 2: simpan setiap data baru ke SharedPreferences
                    saveLocalCache(newState)
                    onChanged(newState)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Listener dibatalkan: ${error.message}")
                // Coba sajikan dari cache lokal
                val cached = loadLocalCache()
                if (cached != null) {
                    Log.i(TAG, "Menyajikan data dari cache lokal")
                    onChanged(cached)
                } else {
                    onError(error.message ?: "Database error: ${error.code}")
                }
            }
        }
        deviceRef?.addValueEventListener(deviceListener as ValueEventListener)
    }

    /** Kembalikan data terakhir yang tersimpan di SharedPreferences (untuk fallback di MainActivity). */
    fun loadCachedState(): PairingUiState? = loadLocalCache()

    fun stopObserving() {
        deviceListener?.let { deviceRef?.removeEventListener(it) }
        deviceListener = null
    }

    // ════════════════════════════════════════════════════════════
    // Layer 2: SharedPreferences Cache
    // ════════════════════════════════════════════════════════════

    private fun saveLocalCache(state: PairingUiState) {
        prefs.edit().apply {
            putString("cache_mosqueName", state.settings.mosqueName)
            putString("cache_province", state.settings.province)
            putString("cache_city", state.settings.city)
            putString("cache_timezone", state.settings.timezone)
            putString("cache_videoUrl", state.settings.videoUrl)
            putString("cache_runningText", state.settings.runningText)
            putFloat("cache_runningTextSpeed", state.settings.runningTextSpeed)
            putFloat("cache_runningTextBrightness", state.settings.runningTextBrightness)
            putString("cache_hadithText", state.settings.hadithText)
            putString("cache_hadithSource", state.settings.hadithSource)
            putBoolean("cache_paired", state.paired)
            putString("cache_pairingCode", state.pairingCode ?: "")
            putString("cache_todaySchedule", state.todaySchedule?.toSaveString() ?: "")
            putString("cache_tomorrowSchedule", state.tomorrowSchedule?.toSaveString() ?: "")
            putLong("cache_savedAt", System.currentTimeMillis())
        }.apply()
        Log.d(TAG, "Cache lokal diperbarui")
    }

    private fun loadLocalCache(): PairingUiState? {
        if (prefs.getLong("cache_savedAt", 0L) == 0L) return null
        val settings = DeviceSettings(
            mosqueName = prefs.getString("cache_mosqueName", "Belum diatur") ?: "Belum diatur",
            province = prefs.getString("cache_province", "Belum diatur") ?: "Belum diatur",
            city = prefs.getString("cache_city", "Belum diatur") ?: "Belum diatur",
            timezone = prefs.getString("cache_timezone", "Asia/Jakarta") ?: "Asia/Jakarta",
            videoUrl = prefs.getString("cache_videoUrl", "") ?: "",
            runningText = prefs.getString("cache_runningText", "") ?: "",
            runningTextSpeed = prefs.getFloat("cache_runningTextSpeed", 5f),
            runningTextBrightness = prefs.getFloat("cache_runningTextBrightness", 80f),
            hadithText = prefs.getString("cache_hadithText", "") ?: "",
            hadithSource = prefs.getString("cache_hadithSource", "") ?: ""
        )
        return PairingUiState(
            deviceId = getOrCreateDeviceId(),
            pairingCode = prefs.getString("cache_pairingCode", null)?.ifBlank { null },
            paired = prefs.getBoolean("cache_paired", false),
            settings = settings,
            todaySchedule = parseSavedSchedule(prefs.getString("cache_todaySchedule", "") ?: ""),
            tomorrowSchedule = parseSavedSchedule(prefs.getString("cache_tomorrowSchedule", "") ?: "")
        )
    }

    private fun DailyPrayerSchedule.toSaveString(): String =
        "$date|$fajr|$syuruk|$dzuhur|$ashar|$maghrib|$isya"

    private fun parseSavedSchedule(str: String): DailyPrayerSchedule? {
        if (str.isBlank()) return null
        val p = str.split("|")
        if (p.size < 7) return null
        return DailyPrayerSchedule(
            date = p[0], fajr = p[1], syuruk = p[2],
            dzuhur = p[3], ashar = p[4], maghrib = p[5], isya = p[6]
        )
    }

    private fun addMinutesToTime(timeStr: String, minutes: Int): String {
        if (timeStr == "-" || timeStr.isBlank()) return "-"
        return try {
            val parts = timeStr.trim().split(":")
            val totalMinutes = parts[0].toInt() * 60 + parts[1].toInt() + minutes
            String.format("%02d:%02d", (totalMinutes / 60) % 24, totalMinutes % 60)
        } catch (e: Exception) {
            "-"
        }
    }

    // ════════════════════════════════════════════════════════════
    // Helper
    // ════════════════════════════════════════════════════════════

    /** Update Firebase tanpa throw jika offline. */
    private suspend fun silentUpdate(ref: DatabaseReference) {
        try {
            ref.setValue(ServerValue.TIMESTAMP).await()
        } catch (e: Exception) {
            Log.d(TAG, "silentUpdate dilewati (offline?): ${e.message}")
        }
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
                "Firebase belum siap. Tambahkan app/google-services.json ATAU isi FIREBASE_API_KEY, " +
                "FIREBASE_APP_ID, dan FIREBASE_PROJECT_ID di app/build.gradle.kts."
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

    private fun generatePairingCode(): String =
        Random.nextInt(100_000, 1_000_000).toString()

    // ════════════════════════════════════════════════════════════
    // DataSnapshot parsers
    // ════════════════════════════════════════════════════════════

    private fun DataSnapshot.toDeviceSettings(): DeviceSettings {
        val speed = child("runningTextSpeed").toDoubleValue(default = 5.0)
        val brightness = child("runningTextBrightness").toDoubleValue(default = 80.0)
        return DeviceSettings(
            mosqueName = child("mosqueName").toStringValue(default = "Belum diatur"),
            province = child("province").toStringValue(default = "Belum diatur"),
            city = child("city").toStringValue(default = "Belum diatur"),
            timezone = child("timezone").toStringValue(default = "Asia/Jakarta"),
            videoUrl = child("videoUrl").toStringValue(),
            runningText = child("runningText").toRunningTextValue(),
            runningTextSpeed = speed.toFloat(),
            runningTextBrightness = brightness.toFloat(),
            educationVideos = child("educationVideos").toEducationVideoList(),
            hadithText = child("hadithText").toStringValue(),
            hadithSource = child("hadithSource").toStringValue()
        )
    }

    private fun DataSnapshot.toStringValue(default: String = ""): String =
        when (val raw = value) {
            null -> default
            is String -> raw
            is Number, is Boolean -> raw.toString()
            else -> default
        }

    private fun DataSnapshot.toDoubleValue(default: Double): Double =
        when (val raw = value) {
            is Double -> raw
            is Long -> raw.toDouble()
            is Int -> raw.toDouble()
            is Float -> raw.toDouble()
            is String -> raw.toDoubleOrNull() ?: default
            else -> default
        }

    private fun DataSnapshot.toRunningTextValue(): String {
        val raw = value ?: return ""
        return when (raw) {
            is String -> raw
            is Number, is Boolean -> raw.toString()
            is Map<*, *> -> {
                val preferredKeys = listOf("text", "value", "message", "content", "runningText")
                preferredKeys.firstNotNullOfOrNull { key ->
                    (raw[key] as? String)?.takeIf { it.isNotBlank() }
                } ?: raw.values.firstNotNullOfOrNull { it as? String } ?: ""
            }
            else -> ""
        }
    }

    private fun DataSnapshot.toScheduleState(
        timezone: String
    ): Pair<DailyPrayerSchedule?, DailyPrayerSchedule?> {
        val scheduleMap = toScheduleMap()
        if (scheduleMap.isEmpty()) return Pair(null, null)
        val todayKey = currentDateKey(timezone)
        val today = scheduleMap[todayKey]
        val tomorrowKey = runCatching { LocalDate.parse(todayKey).plusDays(1).toString() }
            .getOrDefault(todayKey)
        val tomorrow = scheduleMap[tomorrowKey]
        return Pair(today, tomorrow)
    }

    private fun DataSnapshot.toScheduleMap(): Map<String, DailyPrayerSchedule> {
        if (!exists()) return emptyMap()
        val result = linkedMapOf<String, DailyPrayerSchedule>()
        children.forEach { daySnap ->
            val dateKey = daySnap.key?.trim().orEmpty()
            if (dateKey.isEmpty()) return@forEach
            val fajr = daySnap.child("fajr").getValue(String::class.java)
                ?: daySnap.child("subuh").getValue(String::class.java)
                ?: daySnap.child("imsak").getValue(String::class.java) ?: "-"
            // Syuruk = Subuh + 1 jam 10 menit (70 menit)
            val syuruk = addMinutesToTime(fajr, 70)
            val dzuhur = daySnap.child("dzuhur").getValue(String::class.java)
                ?: daySnap.child("dhuhr").getValue(String::class.java) ?: "-"
            val ashar = daySnap.child("ashar").getValue(String::class.java)
                ?: daySnap.child("asar").getValue(String::class.java) ?: "-"
            val maghrib = daySnap.child("maghrib").getValue(String::class.java) ?: "-"
            val isya = daySnap.child("isya").getValue(String::class.java)
                ?: daySnap.child("isha").getValue(String::class.java) ?: "-"

            // Terapkan koreksi waktu sesuai tabel referensi (image):
            // Subuh (fajr) +10, Syuruk +10, Dzuhur -1, Ashar 0, Maghrib -4, Isya 0
            val offsets = mapOf(
                "fajr" to 10,
                "syuruk" to 10,
                "dzuhur" to -1,
                "ashar" to 0,
                "maghrib" to -4,
                "isya" to 0
            )

            val fajrCorr = addMinutesToTime(fajr, offsets["fajr"]!!)
            val syurukCorr = addMinutesToTime(syuruk, offsets["syuruk"]!!)
            val dzuhurCorr = addMinutesToTime(dzuhur, offsets["dzuhur"]!!)
            val asharCorr = addMinutesToTime(ashar, offsets["ashar"]!!)
            val maghribCorr = addMinutesToTime(maghrib, offsets["maghrib"]!!)
            val isyaCorr = addMinutesToTime(isya, offsets["isya"]!!)

            result[dateKey] = DailyPrayerSchedule(
                date = dateKey, fajr = fajrCorr, syuruk = syurukCorr,
                dzuhur = dzuhurCorr, ashar = asharCorr, maghrib = maghribCorr, isya = isyaCorr
            )
        }
        return result
    }

    private fun DataSnapshot.toEducationVideoList(): List<EducationVideo> {
        if (!exists()) return emptyList()
        val videos = mutableListOf<EducationVideo>()
        children.forEach { videoSnap ->
            val id = videoSnap.key?.trim().orEmpty()
            if (id.isEmpty()) return@forEach
            val title = videoSnap.child("title").getValue(String::class.java)
                ?: videoSnap.child("name").getValue(String::class.java) ?: "Video"
            val url = videoSnap.child("url").getValue(String::class.java)
                ?: videoSnap.child("videoUrl").getValue(String::class.java) ?: ""
            val order = videoSnap.child("order").getValue(Int::class.java)
                ?: videoSnap.child("order").getValue(Long::class.java)?.toInt()
                ?: videoSnap.child("index").getValue(Int::class.java)
                ?: videoSnap.child("index").getValue(Long::class.java)?.toInt() ?: 0
            videos.add(EducationVideo(id = id, title = title, url = url, order = order))
        }
        return videos.sortedWith(compareBy<EducationVideo> { it.order }.thenBy { it.title })
    }

    private fun currentDateKey(timezone: String): String {
        val zone = runCatching { ZoneId.of(timezone) }.getOrElse { ZoneId.systemDefault() }
        return LocalDate.now(zone).toString()
    }
}
