package com.zaidhack.pink

import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.zaidhack.pink.ui.theme.ZaidHackTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.security.MessageDigest
import java.time.OffsetDateTime
import javax.net.ssl.HttpsURLConnection

private val Background = Color(0xFF180F1B)
private val CardColor = Color(0xFF291A2B)
private val Pink = Color(0xFFF4ACD4)
private val Pale = Color(0xFFFFDDED)
private val Muted = Color(0xFFD4B8CB)
private const val LICENSE_URL = "https://zzlvupunploglgxbgllm.supabase.co/functions/v1/zh-license"
private const val GAME_PACKAGE = "com.dts.freefireth"

private data class CommandResult(val exit: Int, val output: String) {
    fun description(): String = "Exit: " + exit + "\n" + output
}
private data class LicenseResult(val expiry: Long, val expiresAt: String)

class MainActivity : ComponentActivity() {
    private lateinit var prefs: SharedPreferences
    private var imported by mutableStateOf(false)
    private var importedHash by mutableStateOf("")
    private var rootReady by mutableStateOf(false)
    private var busy by mutableStateOf(false)
    private var log by mutableStateOf("Importa el Zaid-Hack-v1.3.sh desde Descargas para comenzar.")
    private var keyText by mutableStateOf("")
    private var expiry by mutableLongStateOf(0L)

    private val picker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runJob("Importar script") { importScript(uri) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("zh_local", MODE_PRIVATE)
        imported = File(filesDir, "overlay-arm64").exists()
        importedHash = prefs.getString("elf_sha", "") ?: ""
        keyText = prefs.getString("key", "") ?: ""
        expiry = prefs.getLong("expiry", 0)
        setContent {
            ZaidHackTheme {
                MainScreen()
            }
        }
        runJob("Diagnóstico root") {
            val result = root("id -u")
            rootReady = result.exit == 0 && result.output.trim() == "0"
            if (rootReady) "Root concedido. KernelSU, Magisk o su compatible."
            else "Root no disponible o permiso rechazado: " + result.description()
        }
    }

    @Composable
    private fun MainScreen() {
        var now by remember { mutableLongStateOf(System.currentTimeMillis() / 1000L) }
        LaunchedEffect(Unit) {
            while (true) {
                now = System.currentTimeMillis() / 1000L
                delay(1000)
            }
        }
        val remaining = (expiry - now).coerceAtLeast(0L)
        val countdown = if (expiry <= 0L) "Sin verificar"
            else if (remaining == 0L) "Licencia vencida"
            else String.format(
                "%02dd  %02dh  %02dm  %02ds",
                remaining / 86400, remaining / 3600 % 24,
                remaining / 60 % 60, remaining % 60
            )
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
                    .verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_zh),
                        contentDescription = "Logo Zaid Hack",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(84.dp).clip(RoundedCornerShape(25.dp))
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("ZAID HACK", color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("Pink Monet", color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("Android 12+ · ARM64 · Root",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("LICENCIA · TIEMPO RESTANTE",
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                            style = MaterialTheme.typography.labelSmall)
                        Text(countdown, color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("Se verifica con Supabase antes de iniciar",
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.80f),
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                CardBlock {
                    Text("Estado del dispositivo", fontWeight = FontWeight.SemiBold, color = Pale)
                    Spacer(Modifier.height(8.dp))
                    Text(if (rootReady) "● Root autorizado" else "○ Root pendiente", color = if (rootReady) Pink else Muted)
                    Text(if (Build.SUPPORTED_ABIS.contains("arm64-v8a")) "● ARM64 compatible" else "○ Requiere ARM64", color = Muted)
                    Text(if (imported) "● Ejecutable importado" else "○ Falta importar tu script", color = Muted)
                    if (importedHash.isNotBlank()) {
                        Text("SHA-256: " + importedHash.take(18) + "…", color = Muted, fontSize = 11.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    FilledTonalButton(
                        onClick = { picker.launch(arrayOf("*/*")) },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (imported) "Cambiar archivo .sh" else "Importar Zaid-Hack-v1.3.sh") }
                }
                CardBlock {
                    Text("Licencia ZH", color = Pale, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text("Introduce tu key para activar o revisar la vigencia.", color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = keyText,
                        onValueChange = { keyText = it.uppercase().take(24) },
                        singleLine = true,
                        label = { Text("ZH-XXXX-XXXX-XXXX") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { runJob("Validar key") { verifyAndSave(keyText) } }, enabled = !busy && keyText.isNotBlank(), modifier = Modifier.weight(1f)) {
                            Text("Activar / verificar", maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                        }
                        OutlinedButton(onClick = {
                            runJob("Salir de la licencia") {
                                prefs.edit().remove("key").remove("expiry").apply()
                                expiry = 0L
                                val result = if (rootReady) stopNow() else CommandResult(0, "Sin root")
                                "Key local eliminada (no revoca la del servidor).\n" + result.description()
                            }
                        }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Cerrar sesión") }
                    }
                }
                CardBlock {
                    Text("Control del overlay", color = Pale, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        enabled = !busy && imported && rootReady && Build.SUPPORTED_ABIS.contains("arm64-v8a"),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { runJob("Iniciar overlay") { startNow() } }
                    ) { Text("▶  Iniciar overlay y Free Fire") }
                    OutlinedButton(
                        enabled = !busy && rootReady, modifier = Modifier.fillMaxWidth(),
                        onClick = { runJob("Detener overlay") { stopNow().description() } }
                    ) { Text("■  Detener overlay") }
                    Text("Solo se detiene el ejecutable de Zaid Hack, nunca el proceso del juego.", color = Muted, fontSize = 12.sp)
                }
                CardBlock {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Diagnóstico", color = Pale, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            runJob("Ver registros") { root("tail -n 30 /data/local/tmp/overlay.log; tail -n 15 /data/local/tmp/zaid_license_watch.log").description() }
                        }, enabled = !busy && rootReady) { Text("Logs") }
                    }
                    if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(log, color = Muted, fontSize = 12.sp)
                }
                Text(
                    "Importa únicamente scripts de confianza. La app extrae su ELF ARM64 y lo ejecuta con root. " +
                        "El archivo importado no se sube a GitHub ni a otros servidores.",
                    color = Muted, fontSize = 11.sp
                )
            }
        }
    }

    @Composable
    private fun CardBlock(content: @Composable ColumnScope.() -> Unit) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(19.dp), content = content)
        }
    }

    private fun runJob(title: String, action: suspend () -> String) {
        if (busy) return
        lifecycleScope.launch {
            busy = true
            log = title + "..."
            try {
                val message = withContext(Dispatchers.IO) { action() }
                log = title + "\n" + message
            } catch (e: Exception) {
                log = title + " — error\n" + (e.message ?: e.javaClass.simpleName)
            } finally {
                busy = false
            }
        }
    }

    private fun importScript(uri: Uri): String {
        val source = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("No se pudo abrir el archivo.")
        require(source.size in 1024..16_000_000) { "Tamaño inesperado de script." }
        val text = source.toString(Charsets.US_ASCII)
        require(text.startsWith("#!/")) { "El archivo no parece ser un script shell." }
        val marker = "\n__PAYLOAD__\n"
        val index = text.indexOf(marker)
        require(index > 0) { "No se encontró el marcador __PAYLOAD__." }
        val elf = Base64.decode(text.substring(index + marker.length), Base64.DEFAULT)
        require(elf.size >= 64 && elf[0] == 0x7f.toByte() && elf[1] == 'E'.code.toByte()
            && elf[2] == 'L'.code.toByte() && elf[3] == 'F'.code.toByte()) {
            "El payload no es un ELF válido."
        }
        require(elf[4] == 2.toByte() && (elf[18].toInt() and 255) == 183
            && elf[19] == 0.toByte()) {
            "El ejecutable no es ELF64 AArch64."
        }
        val tmp = File(filesDir, "overlay-arm64.tmp")
        tmp.writeBytes(elf)
        val destination = File(filesDir, "overlay-arm64")
        require(tmp.renameTo(destination)) { "No se pudo guardar el ejecutable." }
        val sha = sha256(elf)
        prefs.edit().putString("elf_sha", sha).apply()
        imported = true
        importedHash = sha
        return "Importación correcta: " + elf.size + " bytes ARM64.\nSHA-256 " + sha
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }

    private fun deviceUuid(): String {
        val result = root("settings get secure android_id; getprop ro.product.device")
        require(result.exit == 0) { "Se requiere root para leer el identificador del script original." }
        val lines = result.output.trim().lines()
        require(lines.size >= 2 && lines[0].isNotBlank() && lines[0] != "null") {
            "Identificador de dispositivo no disponible."
        }
        val h = sha256(("zh:v1:" + lines[0].trim() + ":" + lines[1].trim()).toByteArray())
        return h.substring(0, 8) + "-" + h.substring(8, 12) +
            "-4" + h.substring(13, 16) + "-a" + h.substring(17, 20) +
            "-" + h.substring(20, 32)
    }

    private fun verifyRemote(key: String): LicenseResult {
        val cleanKey = key.trim().uppercase()
        require(Regex("^ZH-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{4}$").matches(cleanKey)) {
            "Formato de key incorrecto: ZH-XXXX-XXXX-XXXX."
        }
        val json = JSONObject()
            .put("action", "redeem").put("key", cleanKey).put("device", deviceUuid())
        val connection = URL(LICENSE_URL).openConnection() as HttpsURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 8000
            connection.readTimeout = 20000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write(json.toString().toByteArray(Charsets.UTF_8)) }
            val stream = if (connection.responseCode in 200..299)
                connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
            val response = JSONObject(body)
            require(response.optBoolean("valid", false)) {
                when (response.optString("reason")) {
                    "expired" -> "La key ya venció."
                    "revoked" -> "La key fue revocada."
                    "other_device" -> "La key está vinculada a otro dispositivo."
                    "rate_limited" -> "Demasiados intentos de validación."
                    "not_redeemed" -> "Pasó el plazo permitido de activación."
                    else -> "Supabase rechazó la key o hubo un error."
                }
            }
            val expiresAt = response.getString("expires_at")
            val seconds = OffsetDateTime.parse(expiresAt).toEpochSecond()
            require(seconds > System.currentTimeMillis() / 1000L) { "La key ya venció." }
            return LicenseResult(seconds, expiresAt)
        } finally {
            connection.disconnect()
        }
    }

    private fun verifyAndSave(key: String): String {
        val clean = key.trim().uppercase()
        val license = verifyRemote(clean)
        prefs.edit().putString("key", clean).putLong("expiry", license.expiry).apply()
        expiry = license.expiry
        return "Licencia verificada por Supabase.\nVence UTC: " + license.expiresAt
    }

    private fun stageAsset(name: String): File {
        val destination = File(filesDir, name)
        assets.open(name).use { input ->
            destination.outputStream().use { output -> input.copyTo(output) }
        }
        return destination
    }

    private fun startNow(): String {
        require(imported) { "Importa primero Zaid-Hack-v1.3.sh." }
        require(Build.SUPPORTED_ABIS.contains("arm64-v8a")) { "Requiere procesador ARM64." }
        val savedKey = prefs.getString("key", "") ?: ""
        require(savedKey.isNotBlank()) { "Activa primero tu licencia ZH." }
        val verified = verifyRemote(savedKey)
        prefs.edit().putLong("expiry", verified.expiry).apply()
        expiry = verified.expiry
        val start = stageAsset("start.sh")
        val stop = stageAsset("stop.sh")
        val watcher = stageAsset("watch.sh")
        val cleanup = root("/system/bin/sh " + quoted(stop.absolutePath))
        require(cleanup.exit == 0) { "No se pudo detener la instancia previa: " + cleanup.output }
        val command = "/system/bin/sh " + quoted(start.absolutePath) + " " +
            quoted(File(filesDir, "overlay-arm64").absolutePath) + " " +
            verified.expiry.toString() + " " + sha256(savedKey.toByteArray()) + " " +
            quoted(watcher.absolutePath)
        val result = root(command)
        require(result.exit == 0) { "No se pudo iniciar: " + result.description() }
        return "Licencia verificada. " + result.output +
            "\nVigencia supervisada hasta: " + verified.expiresAt
    }

    private fun stopNow(): CommandResult {
        val stop = stageAsset("stop.sh")
        return root("/system/bin/sh " + quoted(stop.absolutePath))
    }

    private fun quoted(path: String): String = "'" + path.replace("'", "'\\''") + "'"

    private fun root(command: String): CommandResult {
        val process = ProcessBuilder("su", "-c", command).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().use { it.readText().takeLast(5000) }
        return CommandResult(process.waitFor(), output)
    }
}
