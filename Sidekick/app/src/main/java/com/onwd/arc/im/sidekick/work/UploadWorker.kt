package com.onwd.arc.im.sidekick.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.onwd.arc.im.sidekick.MainApplication
import java.net.URL
import java.net.URLEncoder
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.internal.UTC

internal class UploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return applicationContext.uploadData()
    }
}

private val client = OkHttpClient()

suspend fun Context.uploadData(): Result {
    Log.i(UploadWorker::class.simpleName, "Uploading data")
    val repository = (applicationContext as MainApplication).passiveDataRepository
    val fileExport = with(applicationContext as MainApplication) {
        jsonFileStore.export()
    }

    if (fileExport.length() == 0L) {
        Log.i(UploadWorker::class.simpleName, "No data to upload")
        return Result.success()
    }

    withContext(Dispatchers.IO) {
        client.newCall(
            okhttp3.Request.Builder()
                .url(uploadUrl(repository.getUserId()))
                .put(
                    fileExport.asRequestBody("application/jsonl".toMediaType())
                )
                .header("x-ms-blob-type", "BlockBlob")
                .build()
        ).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e(
                    UploadWorker::class.simpleName,
                    "Upload request failed: ${response.code}"
                )
            }
        }
    }

    repository.storeLatestUpload(OffsetDateTime.now())
    return Result.success()
}

private fun uploadUrl(userId: String) = URL(
    "https://onwarddevfileupload.blob.core.windows.net/sensor-data/$userId/${
        URLEncoder.encode(
            DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now(UTC.toZoneId())),
            Charsets.UTF_8
        )
    }.jsonl?sv=2021-10-04&st=2024-11-12T14%3A28%3A36Z&se=2034-11-12T14%3A00%3A00Z&sr=c&sp=c" +
        "&sig=N%2BlJH4isLlsXK3PO9V46Up2LVQ1wXBGrheVH8sUd2GY%3D"
)
