package com.onwd.arc.im.sidekick.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.onwd.arc.im.sidekick.MainApplication
import com.onwd.arc.im.sidekick.data.PassiveDataRepository
import com.onwd.arc.im.sidekick.extensions.checkPermissions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Background data subscriptions are not persisted across device restarts. This receiver checks if
 * we enabled background data and, if so, registers again.
 */
class StartupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repository = PassiveDataRepository(context)
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        runBlocking {
            if (repository.passiveDataEnabled.first()) {
                // Make sure we have permission.
                if (context.checkPermissions()) {
                    scheduleWorker(context)
                } else {
                    // We may have lost the permission somehow. Mark that background data is
                    // disabled so the state is consistent the next time the user opens the app UI.
                    repository.setPassiveDataEnabled(false)
                }
            }
        }
    }

    private fun scheduleWorker(context: Context) {
        // BroadcastReceiver's onReceive must complete within 10 seconds. During device startup,
        // sometimes the call to register for background data takes longer than that and our
        // BroadcastReceiver gets destroyed before it completes. Instead we schedule a WorkManager
        // job to perform the registration.
        Log.i(this::class.simpleName, "Enqueuing worker")
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<RegisterForBackgroundDataWorker>().build()
        )
    }
}

class RegisterForBackgroundDataWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.i(this::class.simpleName, "Worker running")
        with(appContext as MainApplication) {
            healthServicesRepository.registerForPassiveData()
        }
        return Result.success()
    }
}
