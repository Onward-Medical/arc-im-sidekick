package com.onwd.arc.im.sidekick.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.onwd.arc.im.sidekick.MainApplication
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class UploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private val repository by lazy {
        (applicationContext as MainApplication).passiveDataRepository
    }

    private val uploadUrl
        get() = URL(
            "https://onwarddevfileupload.blob.core.windows.net/sensor-data/${
                URLEncoder.encode(
                    DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now()),
                    Charsets.UTF_8
                )
            }?sv=2021-10-04&st=2024-11-12T10%3A30%3A42Z&se=2030-11-12T10%3A30%3A00Z&sr=c&sp=a" +
                "&sig=GXN0lNTjhEn0F2F3%2F1hp0LHdgcM8aDvGRWQuuqttxhs%3D"
        )

    override suspend fun doWork(): Result {
        Log.i(this::class.simpleName, "Uploading data")
        val fileExport = with(applicationContext as MainApplication) {
            jsonFileStore.export()
        }
        if (fileExport.length() == 0L) {
            Log.i(this::class.simpleName, "No data to upload")
            return Result.success()
        }
        withContext(Dispatchers.IO) {
            uploadUrl.openConnection() as HttpURLConnection
        }
            .apply {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("x-ms-blob-type", "BlockBlob")

                outputStream.use { writer ->
                    writer.write(fileExport.readBytes())
                    writer.flush()
                }

                Log.i(
                    this::class.simpleName,
                    "Upload response: $responseCode"
                )

                if (responseCode != 201) {
                    Log.e(this::class.simpleName, "Upload failed") // TODO store file for retry
                    return Result.failure()
                }
            }

        repository.storeLatestUpload(OffsetDateTime.now())
        return Result.success()
    }
}
