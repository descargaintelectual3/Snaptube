package com.example.engine

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

/**
 * High-performance ID3v2.3 tagger and audio metadata embedder.
 * Injects Title, Artist, Album, and Cover Art into downloaded MP3 audio streams.
 */
object MediaTagEngine {
    private const val TAG = "MediaTagEngine"

    fun injectId3v2Tags(
        targetMp3File: File,
        title: String,
        artist: String,
        album: String = "Snaptube Downloads",
        thumbnailUrl: String? = null,
        httpClient: OkHttpClient? = null
    ) {
        try {
            if (!targetMp3File.exists() || targetMp3File.length() == 0L) return

            // Download thumbnail bytes for cover art if available
            var imageBytes: ByteArray? = null
            if (!thumbnailUrl.isNullOrBlank() && httpClient != null) {
                try {
                    val req = Request.Builder()
                        .url(thumbnailUrl)
                        .header("User-Agent", "Mozilla/5.0")
                        .build()
                    httpClient.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful && resp.body != null) {
                            imageBytes = resp.body!!.bytes()
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to download cover art thumbnail: ${e.message}")
                }
            }

            // Build ID3v2 Frames
            val framesStream = ByteArrayOutputStream()

            // TIT2 (Title)
            writeTextFrame(framesStream, "TIT2", title)

            // TPE1 (Artist)
            writeTextFrame(framesStream, "TPE1", artist)

            // TALB (Album)
            writeTextFrame(framesStream, "TALB", album)

            // APIC (Attached Picture / Cover Art)
            if (imageBytes != null && imageBytes!!.isNotEmpty()) {
                writeApicFrame(framesStream, imageBytes!!)
            }

            val framesBytes = framesStream.toByteArray()
            val id3Header = buildId3Header(framesBytes.size)

            // Read original audio content (skipping any existing ID3v2 header if present)
            val tempFile = File(targetMp3File.parentFile, "${targetMp3File.name}.tagged")
            val originalStream = FileInputStream(targetMp3File)

            // Check if file already starts with ID3
            val headerProbe = ByteArray(10)
            val bytesRead = originalStream.read(headerProbe)
            var skipBytes = 0L

            if (bytesRead == 10 && headerProbe[0] == 'I'.code.toByte() && headerProbe[1] == 'D'.code.toByte() && headerProbe[2] == '3'.code.toByte()) {
                val size = ((headerProbe[6].toInt() and 0x7F) shl 21) or
                        ((headerProbe[7].toInt() and 0x7F) shl 14) or
                        ((headerProbe[8].toInt() and 0x7F) shl 7) or
                        (headerProbe[9].toInt() and 0x7F)
                skipBytes = size.toLong()
            }

            FileOutputStream(tempFile).use { fos ->
                fos.write(id3Header)
                fos.write(framesBytes)

                // If we skipped the original ID3 header, reposition or read from there
                if (skipBytes > 0) {
                    originalStream.skip(skipBytes)
                } else if (bytesRead > 0) {
                    fos.write(headerProbe, 0, bytesRead)
                }

                val buffer = ByteArray(32768)
                var len: Int
                while (originalStream.read(buffer).also { len = it } != -1) {
                    fos.write(buffer, 0, len)
                }
                fos.flush()
            }
            originalStream.close()

            // Swap files atomically
            if (tempFile.exists() && tempFile.length() > 0) {
                targetMp3File.delete()
                tempFile.renameTo(targetMp3File)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error injecting ID3 tags: ${e.message}", e)
        }
    }

    private fun buildId3Header(payloadSize: Int): ByteArray {
        val header = ByteArray(10)
        header[0] = 'I'.code.toByte()
        header[1] = 'D'.code.toByte()
        header[2] = '3'.code.toByte()
        header[3] = 0x03 // v2.3
        header[4] = 0x00 // revision
        header[5] = 0x00 // flags

        // Size in Synchsafe integer (7 bits per byte)
        header[6] = ((payloadSize shr 21) and 0x7F).toByte()
        header[7] = ((payloadSize shr 14) and 0x7F).toByte()
        header[8] = ((payloadSize shr 7) and 0x7F).toByte()
        header[9] = (payloadSize and 0x7F).toByte()

        return header
    }

    private fun writeTextFrame(out: ByteArrayOutputStream, frameId: String, text: String) {
        val textBytes = text.toByteArray(StandardCharsets.UTF_8)
        val payload = ByteArray(1 + textBytes.size)
        payload[0] = 0x03 // UTF-8 encoding flag
        System.arraycopy(textBytes, 0, payload, 1, textBytes.size)

        writeFrameHeader(out, frameId, payload.size)
        out.write(payload)
    }

    private fun writeApicFrame(out: ByteArrayOutputStream, imageBytes: ByteArray) {
        val mimeBytes = "image/jpeg".toByteArray(StandardCharsets.ISO_8859_1)
        val apicPayload = ByteArrayOutputStream()
        apicPayload.write(0x00) // ISO-8859-1 for mime & desc
        apicPayload.write(mimeBytes)
        apicPayload.write(0x00) // Null terminator for mime
        apicPayload.write(0x03) // Picture type: Cover (front)
        apicPayload.write(0x00) // Empty description + null terminator
        apicPayload.write(imageBytes)

        val payloadBytes = apicPayload.toByteArray()
        writeFrameHeader(out, "APIC", payloadBytes.size)
        out.write(payloadBytes)
    }

    private fun writeFrameHeader(out: ByteArrayOutputStream, frameId: String, size: Int) {
        val idBytes = frameId.take(4).toByteArray(StandardCharsets.US_ASCII)
        out.write(idBytes)

        // Frame size in ID3v2.3 is standard 32-bit big-endian
        out.write((size shr 24) and 0xFF)
        out.write((size shr 16) and 0xFF)
        out.write((size shr 8) and 0xFF)
        out.write(size and 0xFF)

        // Flags (2 bytes, 0)
        out.write(0x00)
        out.write(0x00)
    }
}
