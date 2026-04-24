package com.example.fitgymkt.data

import android.content.Context
import android.content.ContentResolver
import android.net.Uri
import com.example.fitgymkt.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Cliente REST simple para consumir tablas de Supabase (PostgREST).
 *
 * Usa las variables de BuildConfig:
 * - SUPABASE_URL
 * - SUPABASE_ANON_KEY
 */
class SupabaseRestClient(context: Context) {

    private val baseUrl = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
    private val anonKey = BuildConfig.SUPABASE_ANON_KEY.trim()

    @Suppress("UNUSED_PARAMETER")
    private val appContext = context.applicationContext

    fun isConfigured(): Boolean = baseUrl.isNotBlank() && anonKey.isNotBlank()

    fun configurationIssue(): String? {
        val missing = mutableListOf<String>()
        if (baseUrl.isBlank()) missing += "SUPABASE_URL"
        if (anonKey.isBlank()) missing += "SUPABASE_ANON_KEY"
        if (missing.isEmpty()) return null
        return "Faltan variables: ${missing.joinToString(", ")}. " +
                "Añádelas en local.properties (raíz del proyecto) y recompila la app."
    }

    fun select(
        table: String,
        select: String = "*",
        filters: Map<String, String> = emptyMap(),
        orderBy: String? = null,
        ascending: Boolean = true,
        limit: Int? = null
    ): JSONArray {
        if (!isConfigured()) return JSONArray()

        val uriBuilder = Uri.parse("$baseUrl/rest/v1/$table").buildUpon()
            .appendQueryParameter("select", select)

        filters.forEach { (key, value) -> uriBuilder.appendQueryParameter(key, value) }

        if (!orderBy.isNullOrBlank()) {
            val direction = if (ascending) "asc" else "desc"
            uriBuilder.appendQueryParameter("order", "$orderBy.$direction")
        }

        if (limit != null && limit > 0) {
            uriBuilder.appendQueryParameter("limit", limit.toString())
        }

        return request(
            method = "GET",
            url = uriBuilder.build().toString(),
            extraHeaders = mapOf("Accept" to "application/json")
        )
    }

    fun insert(table: String, body: JSONObject): JSONArray {
        if (!isConfigured()) return JSONArray()

        return request(
            method = "POST",
            url = "$baseUrl/rest/v1/$table",
            body = body,
            extraHeaders = mapOf("Prefer" to "return=representation")
        )
    }

    fun update(table: String, filters: Map<String, String>, body: JSONObject): JSONArray {
        if (!isConfigured()) return JSONArray()

        val uriBuilder = Uri.parse("$baseUrl/rest/v1/$table").buildUpon()
        filters.forEach { (key, value) -> uriBuilder.appendQueryParameter(key, value) }

        return request(
            method = "PATCH",
            url = uriBuilder.build().toString(),
            body = body,
            extraHeaders = mapOf("Prefer" to "return=representation")
        )
    }

    fun delete(table: String, filters: Map<String, String>): JSONArray {
        if (!isConfigured()) return JSONArray()

        val uriBuilder = Uri.parse("$baseUrl/rest/v1/$table").buildUpon()
        filters.forEach { (key, value) -> uriBuilder.appendQueryParameter(key, value) }

        return request(
            method = "DELETE",
            url = uriBuilder.build().toString(),
            extraHeaders = mapOf("Prefer" to "return=representation")
        )
    }

    fun uploadPublicImage(
        bucket: String,
        folder: String,
        imageUri: Uri
    ): String {
        check(isConfigured()) { "Supabase no está configurado" }

        val resolver = appContext.contentResolver
        val mimeType = resolver.getType(imageUri).orEmpty().ifBlank { "image/jpeg" }
        val extension = mimeType.substringAfter('/', "jpg").substringBefore(';')
        val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}.$extension"
        val objectPath = listOf(folder.trim('/'), fileName).filter { it.isNotBlank() }.joinToString("/")
        val uploadUrl = "$baseUrl/storage/v1/object/$bucket/$objectPath"

        val bytes = resolver.readBytes(imageUri)

        val connection = (URL(uploadUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 15000
            doInput = true
            doOutput = true
            setRequestProperty("apikey", anonKey)
            setRequestProperty("Authorization", "Bearer $anonKey")
            setRequestProperty("Content-Type", mimeType)
            setRequestProperty("x-upsert", "true")
        }

        try {
            connection.outputStream.use { output -> output.write(bytes) }
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val payload = stream?.bufferedReader(Charsets.UTF_8)?.use(BufferedReader::readText).orEmpty()
            if (responseCode !in 200..299) {
                throw IllegalStateException("Supabase storage error ($responseCode): $payload")
            }
        } finally {
            connection.disconnect()
        }

        return "$baseUrl/storage/v1/object/public/$bucket/$objectPath"
    }

    private fun request(
        method: String,
        url: String,
        body: JSONObject? = null,
        extraHeaders: Map<String, String> = emptyMap()
    ): JSONArray {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15000
            readTimeout = 15000
            doInput = true
            setRequestProperty("apikey", anonKey)
            setRequestProperty("Authorization", "Bearer $anonKey")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            extraHeaders.forEach { (k, v) -> setRequestProperty(k, v) }
        }

        try {
            if (body != null) {
                connection.doOutput = true
                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(body.toString())
                    writer.flush()
                }
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val payload = stream?.bufferedReader(Charsets.UTF_8)?.use(BufferedReader::readText).orEmpty()

            if (responseCode !in 200..299) {
                throw IllegalStateException("Supabase error ($responseCode): $payload")
            }

            if (payload.isBlank()) return JSONArray()

            return when {
                payload.trimStart().startsWith("[") -> JSONArray(payload)
                payload.trimStart().startsWith("{") -> JSONArray().put(JSONObject(payload))
                else -> JSONArray()
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun ContentResolver.readBytes(uri: Uri): ByteArray {
        openInputStream(uri)?.use { input ->
            val buffer = ByteArrayOutputStream()
            input.copyTo(buffer)
            return buffer.toByteArray()
        }
        throw IllegalStateException("No se pudo leer el archivo seleccionado")
    }
}
