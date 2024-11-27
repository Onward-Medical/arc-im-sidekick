package com.azure.blob

import java.io.File
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.internal.UTC

class BlobService(
    private val storageAccount: String,
    private val container: String,
    private val sasToken: String
) {

    private val client = OkHttpClient()

    fun putBlob(file: File, contentType: String, contentEncoding: String, blobName: String) {
        client.newCall(
            okhttp3.Request.Builder()
                .url("https://$storageAccount.blob.core.windows.net/$container/$blobName?$sasToken")
                .put(file.asRequestBody(contentType.toMediaType()))
                .header("x-ms-version", "2025-01-05").header(
                    "x-ms-date",
                    DateTimeFormatter.RFC_1123_DATE_TIME.format(
                        OffsetDateTime.now(UTC.toZoneId())
                    )
                ).header("x-ms-blob-type", "BlockBlob")
                .header("Content-Encoding", contentEncoding)
                .build()
        ).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Failed to upload blob: $response")
            }
        }
    }
}
