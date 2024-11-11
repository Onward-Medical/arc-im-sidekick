package com.onwd.arc.im.sidekick.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.onwd.arc.im.sidekick.data.PassiveDataRepository
import java.time.OffsetDateTime

internal class UploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private val repository = PassiveDataRepository(context)

    override suspend fun doWork(): Result {
        Log.i(this::class.simpleName, "Uploading data")
        repository.storeLatestUpload(OffsetDateTime.now())
        return Result.success()
    }
}
