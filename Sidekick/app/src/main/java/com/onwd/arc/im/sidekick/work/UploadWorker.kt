package com.onwd.arc.im.sidekick.work

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.azure.blob.BlobService
import com.onwd.arc.im.sidekick.MainApplication
import java.io.File
import java.net.URLEncoder
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.internal.UTC

internal class UploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private val app = applicationContext as MainApplication
    private val blobService = BlobService(
        app.blobProperties.getProperty("storageAccount"),
        app.blobProperties.getProperty("container"),
        app.blobProperties.getProperty("sasToken")
    )

    @SuppressLint("RestrictedApi")
    override suspend fun doWork(): Result {
        Log.i(UploadWorker::class.simpleName, "Uploading data")
        val userId = app.passiveDataRepository.getUserId()

        app.jsonFileStore.export()

        for (fileExport in app.jsonFileStore.exportFolder.listFiles() ?: emptyArray()) {
            if (uploadFile(fileExport, userId) is Result.Success) {
                fileExport.delete()
            } else {
                // Fail fast if any upload fails
                return Result.failure()
            }
        }

        app.passiveDataRepository.storeLatestUpload(OffsetDateTime.now())
        return Result.success()
    }

    private suspend fun uploadFile(fileExport: File, userId: String) = try {
        if (!fileExport.isFile() || fileExport.length() == 0L) {
            Log.i(UploadWorker::class.simpleName, "No data to upload")
            Result.success()
        }

        withContext(Dispatchers.IO) {
            blobService.putBlob(
                fileExport,
                "application/jsonl",
                "gzip",
                "$userId/${
                    URLEncoder.encode(
                        DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now(UTC.toZoneId())),
                        Charsets.UTF_8
                    )
                }.jsonl.gz"
            )
            Result.success()
        }
    } catch (e: Exception) {
        Log.e(UploadWorker::class.simpleName, "Upload failed", e)
        Result.failure()
    }
}
