package com.onwd.arc.im.sidekick.work

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest.Companion.MIN_PERIODIC_INTERVAL_MILLIS
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Internal class responsible for scheduling the [UploadWorker] to run periodically.
 */
internal class PeriodicUploadScheduler {
    companion object {

        /**
         * Schedules the [UploadWorker] to run periodically. This method can be called multiple times in a row. Internal
         * worker api will ensure that only one worker is running at a time, and respects the required constraints.
         * @param context the context to use.
         */
        fun scheduleUploadWorker(
            context: Context
        ) {
            val data = workDataOf()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresCharging(false)
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<UploadWorker>(
                MIN_PERIODIC_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS,
                15,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInputData(data)
                .build()

            MainScope().launch {
                try {
                    WorkManager.getInstance(context)
                        .enqueueUniquePeriodicWork(
                            WORKER_TAG,
                            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                            periodicWorkRequest
                        ).await()
                    Log.i(PeriodicUploadScheduler::class.simpleName, "Scheduled upload worker")
                } catch (e: Exception) {
                    Log.e(
                        PeriodicUploadScheduler::class.simpleName,
                        "Failed to schedule upload worker",
                        e
                    )
                }
            }
        }

        /**
         * Tag used to identify the [UploadWorker].
         */
        private const val WORKER_TAG = "ONWD_Sidekick_UploadWorker"
    }
}
