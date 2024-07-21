package com.ravisharma.playbackmusic.new_work.services

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ravisharma.playbackmusic.new_work.Constants
import java.util.concurrent.TimeUnit

class TimerWorker(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {

    private val closeIntent = PendingIntent.getBroadcast(
        context, PlaybackBroadcastReceiver.CANCEL_ACTION_REQUEST_CODE,
        Intent(Constants.PACKAGE_NAME).putExtra(
            PlaybackBroadcastReceiver.AUDIO_CONTROL,
            PlaybackBroadcastReceiver.PLAYER_CANCEL
        ),
        PendingIntent.FLAG_IMMUTABLE
    )

    override fun doWork(): Result {
        closeIntent.send()
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "TimerWorker"

        fun scheduleSleepTimer(duration: Long, context: Context) {
            val sleepWorkRequest = OneTimeWorkRequestBuilder<TimerWorker>()
                .setInitialDelay(duration, TimeUnit.MILLISECONDS)
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                sleepWorkRequest
            )
        }

        fun cancelSleepTimer(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_NAME)
        }
    }
}