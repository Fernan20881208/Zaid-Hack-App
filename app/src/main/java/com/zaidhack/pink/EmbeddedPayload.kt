package com.zaidhack.pink

import android.content.Context
import android.util.Base64
import java.io.File
import java.io.FileNotFoundException
import java.security.MessageDigest

/**
 * Extracts the owner's verified ARM64 ELF from the bundled v1.5 Streamer asset.
 * APK installation is self-contained when the private source asset is present.
 *
 * Public builds omit the proprietary Streamer asset, and remain import-only.
 */
object EmbeddedPayload {
    const val ASSET_NAME = "Zaid-Hack-v1.5-Streamer.sh"
    const val REQUIRED_ELF_SHA256 =
        "35d7354d62899b8e2baf1c562a77b95dc2ca2262918c5c21ee95a3cb0967ba58"
    const val REQUIRED_SCRIPT_SHA256 =
        "137fe5c326eb79dcf9d687fadfbcef040661d3b92eeef424ec1b628ad5d90ddb"

    data class Installation(val target: File, val script: File, val sha256: String)

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }

    fun install(context: Context): Installation? {
        val input = try { context.assets.open(ASSET_NAME) }
        catch (_: FileNotFoundException) { return null }
        val scriptBytes = input.use { it.readBytes() }
        require(sha256(scriptBytes) == REQUIRED_SCRIPT_SHA256) {
            "El script integrado no corresponde a Zaid Hack v1.5 Streamer. Instalación bloqueada."
        }
        val source = String(scriptBytes, Charsets.UTF_8)
        val marker = "\n__PAYLOAD__\n"
        val start = source.indexOf(marker)
        require(start >= 0) { "Falta __PAYLOAD__ en el script integrado" }
        val elf = Base64.decode(source.substring(start + marker.length), Base64.DEFAULT)
        require(elf.size > 65536 && elf[0] == 0x7f.toByte() &&
            elf[1] == 'E'.code.toByte() && elf[2] == 'L'.code.toByte() &&
            elf[3] == 'F'.code.toByte() && elf[4] == 2.toByte() &&
            (elf[18].toInt() and 255) == 183) { "El payload no es ELF64 para ARM64" }
        require(sha256(elf) == REQUIRED_ELF_SHA256) {
            "SHA-256 incorrecto: el binario ARM64 está modificado."
        }
        val target = File(context.filesDir, "overlay-arm64")
        val stagedScript = File(context.filesDir, ASSET_NAME)
        if (!target.exists() || sha256(target.readBytes()) != REQUIRED_ELF_SHA256) {
            val tmp = File(context.filesDir, "overlay-arm64.tmp")
            tmp.writeBytes(elf)
            require(tmp.renameTo(target)) { "No se pudo instalar el ELF integrado" }
        }
        if (!stagedScript.exists() || sha256(stagedScript.readBytes()) != REQUIRED_SCRIPT_SHA256) {
            val tmp = File(context.filesDir, "$ASSET_NAME.tmp")
            tmp.writeBytes(scriptBytes)
            require(tmp.renameTo(stagedScript)) { "No se pudo guardar el script integrado" }
        }
        target.setReadable(true, true)
        stagedScript.setReadable(true, true)
        return Installation(target, stagedScript, REQUIRED_ELF_SHA256)
    }
}