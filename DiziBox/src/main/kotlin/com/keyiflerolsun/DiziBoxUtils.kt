// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Base64
import com.lagradost.cloudstream3.app
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Arrays
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.min

/**
 * MolyStream (dbx.molystream.org) HLS segmentlerini (.png uzantılı ve Content-Type: image/png)
 * yerel olarak video/mp2t (.ts) başlığıyla sunan hafif vekil sunucu.
 * VLC / ExoPlayer adaptive demuxer'ın "image/png" yüzünden arabellekte takılmasını önler.
 */
object MolyStreamProxy {
    private const val UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36"
    private val uriAttrRegex = Regex("""URI="([^"]+)"""")

    private val httpClient: OkHttpClient by lazy {
        app.baseClient.newBuilder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .build()
    }

    private val executor = Executors.newCachedThreadPool { r ->
        Thread(r, "DiziBox-MolyProxy").apply { isDaemon = true }
    }

    @Volatile
    private var serverSocket: ServerSocket? = null

    fun patchDnsForLocalhost() {
        runCatching {
            val currentClient = app.baseClient
            val currentDns = currentClient.dns
            val safeDns = object : okhttp3.Dns {
                override fun lookup(hostname: String): List<InetAddress> {
                    if (hostname == "127.0.0.1" || hostname.equals("localhost", ignoreCase = true)) {
                        return okhttp3.Dns.SYSTEM.lookup(hostname)
                    }
                    return try {
                        currentDns.lookup(hostname)
                    } catch (_: Throwable) {
                        okhttp3.Dns.SYSTEM.lookup(hostname)
                    }
                }
            }
            val newClient = currentClient.newBuilder().dns(safeDns).build()
            app.baseClient = newClient
            runCatching {
                val spClass = Class.forName("com.lagradost.cloudstream4.desktop.player.StreamProxy")
                val delegateField = spClass.getDeclaredField("client\$delegate")
                delegateField.isAccessible = true
                val lazyObj = delegateField.get(null)
                val valField = lazyObj.javaClass.getDeclaredField("_value")
                valField.isAccessible = true
                valField.set(
                    lazyObj,
                    newClient.newBuilder().followRedirects(true).readTimeout(60, TimeUnit.SECONDS).build()
                )
            }
        }
    }

    private fun ensureServer(): Int {
        patchDnsForLocalhost()
        serverSocket?.let { if (!it.isClosed) return it.localPort }
        synchronized(this) {
            serverSocket?.let { if (!it.isClosed) return it.localPort }
            val ss = ServerSocket(0, 64, InetAddress.getByName("127.0.0.1"))
            serverSocket = ss
            executor.execute {
                while (!ss.isClosed) {
                    try {
                        val client = ss.accept()
                        executor.execute {
                            try {
                                handleClient(client)
                            } catch (_: Throwable) {
                            }
                        }
                    } catch (_: Throwable) {
                        if (ss.isClosed) break
                    }
                }
            }
            return ss.localPort
        }
    }

    fun proxyPlaylist(playlistUrl: String, referer: String): String {
        val port = ensureServer()
        val u = URLEncoder.encode(playlistUrl, "UTF-8")
        val r = URLEncoder.encode(referer, "UTF-8")
        return "http://127.0.0.1:$port/playlist.m3u8?u=$u&r=$r"
    }

    private fun parseQuery(rawQuery: String?): Map<String, String> {
        if (rawQuery.isNullOrBlank()) return emptyMap()
        return rawQuery.split('&').mapNotNull { part ->
            val idx = part.indexOf('=')
            if (idx <= 0) null
            else part.substring(0, idx) to URLDecoder.decode(part.substring(idx + 1), "UTF-8")
        }.toMap()
    }

    private fun isPlaylistUrl(url: String): Boolean {
        val clean = url.substringBefore('?').lowercase()
        return clean.endsWith(".m3u8") || (url.contains("/embed/") && url.contains("/q/"))
    }

    private fun wrapUrl(port: Int, rawUrl: String, baseUrl: String, refererEnc: String): String {
        val abs = baseUrl.toHttpUrlOrNull()?.resolve(rawUrl.trim())?.toString() ?: rawUrl.trim()
        val uEnc = URLEncoder.encode(abs, "UTF-8")
        return if (isPlaylistUrl(abs)) {
            "http://127.0.0.1:$port/playlist.m3u8?u=$uEnc&r=$refererEnc"
        } else {
            "http://127.0.0.1:$port/segment.ts?u=$uEnc&r=$refererEnc"
        }
    }

    private fun handleClient(socket: Socket) {
        socket.use { sock ->
            sock.soTimeout = 30_000
            val input = sock.getInputStream()
            val out = sock.getOutputStream()
            val reader = BufferedReader(InputStreamReader(input, Charsets.ISO_8859_1))

            val requestLine = reader.readLine() ?: return
            val parts = requestLine.split(' ')
            if (parts.size < 2) return
            val fullPath = parts[1]

            while (true) {
                val line = reader.readLine() ?: break
                if (line.isEmpty()) break
            }

            val qIdx = fullPath.indexOf('?')
            val path = if (qIdx >= 0) fullPath.substring(0, qIdx) else fullPath
            val query = if (qIdx >= 0) parseQuery(fullPath.substring(qIdx + 1)) else emptyMap()
            val targetUrl = query["u"] ?: return
            val referer = query["r"] ?: "https://dbx.molystream.org/"
            val origin = Regex("""^(https?://[^/]+)""").find(referer)?.groupValues?.get(1) ?: "https://dbx.molystream.org"

            if (path.endsWith(".m3u8")) {
                val req = Request.Builder()
                    .url(targetUrl)
                    .header("User-Agent", UA)
                    .header("Referer", referer)
                    .header("Origin", origin)
                    .build()

                httpClient.newCall(req).execute().use { resp ->
                    val bodyText = resp.body.string()
                    val finalBase = resp.request.url.toString()
                    val port = ensureServer()
                    val rEnc = URLEncoder.encode(referer, "UTF-8")
                    val rewritten = bodyText.lineSequence().joinToString("\n") { line ->
                        val trimmed = line.trim()
                        when {
                            trimmed.isEmpty() -> line
                            trimmed.startsWith("#") -> uriAttrRegex.replace(line) { m ->
                                """URI="${wrapUrl(port, m.groupValues[1], finalBase, rEnc)}""""
                            }
                            else -> wrapUrl(port, trimmed, finalBase, rEnc)
                        }
                    }
                    val bytes = rewritten.toByteArray(Charsets.UTF_8)
                    val header = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: application/vnd.apple.mpegurl\r\n" +
                        "Content-Length: ${bytes.size}\r\n" +
                        "Access-Control-Allow-Origin: *\r\n" +
                        "Connection: close\r\n\r\n"
                    out.write(header.toByteArray(Charsets.ISO_8859_1))
                    out.write(bytes)
                    out.flush()
                }
            } else {
                val req = Request.Builder()
                    .url(targetUrl)
                    .header("User-Agent", UA)
                    .header("Referer", "$origin/")
                    .header("Origin", origin)
                    .build()

                httpClient.newCall(req).execute().use { resp ->
                    val stream = resp.body.byteStream()
                    val headBuf = ByteArray(1024)
                    var headLen = 0
                    while (headLen < headBuf.size) {
                        val n = stream.read(headBuf, headLen, headBuf.size - headLen)
                        if (n <= 0) break
                        headLen += n
                    }

                    // Eğer sahte PNG başlığı varsa ilk MPEG-TS sync byte (0x47 .. +188 = 0x47) konumunu bul
                    var tsOffset = 0
                    if (headLen >= 190 && (headBuf[0].toInt() and 0xFF) != 0x47) {
                        for (i in 0 until (headLen - 188)) {
                            if ((headBuf[i].toInt() and 0xFF) == 0x47 && (headBuf[i + 188].toInt() and 0xFF) == 0x47) {
                                tsOffset = i
                                break
                            }
                        }
                    }

                    val totalLen = resp.body.contentLength()
                    val adjLen = if (totalLen > tsOffset) totalLen - tsOffset else -1L
                    val sb = StringBuilder()
                    sb.append("HTTP/1.1 200 OK\r\n")
                    sb.append("Content-Type: video/mp2t\r\n")
                    if (adjLen > 0) sb.append("Content-Length: $adjLen\r\n")
                    sb.append("Access-Control-Allow-Origin: *\r\n")
                    sb.append("Connection: close\r\n\r\n")
                    out.write(sb.toString().toByteArray(Charsets.ISO_8859_1))

                    if (headLen > tsOffset) {
                        out.write(headBuf, tsOffset, headLen - tsOffset)
                    }
                    val buf = ByteArray(64 * 1024)
                    while (true) {
                        val r = stream.read(buf)
                        if (r <= 0) break
                        out.write(buf, 0, r)
                    }
                    out.flush()
                }
            }
        }
    }
}

@Suppress("unused", "SameParameterValue")
object CryptoJS {

    private const val KEY_SIZE    = 256
    private const val IV_SIZE     = 128
    private const val HASH_CIPHER = "AES/CBC/PKCS5Padding"
    private const val AES         = "AES"
    private const val KDF_DIGEST  = "MD5"
    private const val APPEND      = "Salted__"

    fun encrypt(password: String, plainText: String): String {
        val saltBytes = generateSalt(8)
        val key       = ByteArray(KEY_SIZE / 8)
        val iv        = ByteArray(IV_SIZE / 8)
        evpkdf(password.toByteArray(), KEY_SIZE, IV_SIZE, saltBytes, key, iv)

        val keyS   = SecretKeySpec(key, AES)
        val cipher = Cipher.getInstance(HASH_CIPHER)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, keyS, ivSpec)

        val cipherText = cipher.doFinal(plainText.toByteArray())
        val sBytes     = APPEND.toByteArray()
        val b          = ByteArray(sBytes.size + saltBytes.size + cipherText.size)
        System.arraycopy(sBytes, 0, b, 0, sBytes.size)
        System.arraycopy(saltBytes, 0, b, sBytes.size, saltBytes.size)
        System.arraycopy(cipherText, 0, b, sBytes.size + saltBytes.size, cipherText.size)

        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    fun decrypt(password: String, cipherText: String): String {
        val ctBytes         = Base64.decode(cipherText.toByteArray(), Base64.DEFAULT)
        val saltBytes       = Arrays.copyOfRange(ctBytes, 8, 16)
        val cipherTextBytes = Arrays.copyOfRange(ctBytes, 16, ctBytes.size)

        val key = ByteArray(KEY_SIZE / 8)
        val iv  = ByteArray(IV_SIZE / 8)
        evpkdf(password.toByteArray(), KEY_SIZE, IV_SIZE, saltBytes, key, iv)

        val cipher = Cipher.getInstance(HASH_CIPHER)
        val keyS   = SecretKeySpec(key, AES)
        cipher.init(Cipher.DECRYPT_MODE, keyS, IvParameterSpec(iv))

        val plainText = cipher.doFinal(cipherTextBytes)
        return String(plainText)
    }

    private fun evpkdf(password: ByteArray, keySize: Int, ivSize: Int, salt: ByteArray, resultKey: ByteArray, resultIv: ByteArray): ByteArray {
        return evpkdf(password, keySize, ivSize, salt, 1, KDF_DIGEST, resultKey, resultIv)
    }

    @Suppress("NAME_SHADOWING")
    private fun evpkdf(password: ByteArray, keySize: Int, ivSize: Int, salt: ByteArray, iterations: Int, hashAlgorithm: String, resultKey: ByteArray, resultIv: ByteArray): ByteArray {
        val keySize              = keySize / 32
        val ivSize               = ivSize / 32
        val targetKeySize        = keySize + ivSize
        val derivedBytes         = ByteArray(targetKeySize * 4)
        var numberOfDerivedWords = 0
        var block: ByteArray?    = null
        val hash                 = MessageDigest.getInstance(hashAlgorithm)

        while (numberOfDerivedWords < targetKeySize) {
            if (block != null) {
                hash.update(block)
            }

            hash.update(password)
            block = hash.digest(salt)
            hash.reset()

            for (i in 1 until iterations) {
                block = hash.digest(block!!)
                hash.reset()
            }

            System.arraycopy(
                block!!, 0, derivedBytes, numberOfDerivedWords * 4,
                min(block.size, (targetKeySize - numberOfDerivedWords) * 4)
            )

            numberOfDerivedWords += block.size / 4
        }

        System.arraycopy(derivedBytes, 0, resultKey, 0, keySize * 4)
        System.arraycopy(derivedBytes, keySize * 4, resultIv, 0, ivSize * 4)

        return derivedBytes
    }

    private fun generateSalt(length: Int): ByteArray {
        return ByteArray(length).apply {
            SecureRandom().nextBytes(this)
        }
    }
}
