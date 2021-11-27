package com.ravisharma.playbackmusic.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.prefrences.PrefManager
import java.lang.Thread.UncaughtExceptionHandler

class AppRate(private val hostActivity: Activity) {

    private val preferences = PrefManager(hostActivity)

    private var minLaunchesUntilPrompt: Long = 0
    private var minDaysUntilPrompt: Long = 0

    private var showIfHasCrashed = true

    fun setMinLaunchesUntilPrompt(minLaunchesUntilPrompt: Long): AppRate {
        this.minLaunchesUntilPrompt = minLaunchesUntilPrompt
        return this
    }

    fun setMinDaysUntilPrompt(minDaysUntilPrompt: Long): AppRate {
        this.minDaysUntilPrompt = minDaysUntilPrompt
        return this
    }

    fun setShowIfAppHasCrashed(showIfCrash: Boolean): AppRate {
        showIfHasCrashed = showIfCrash
        preferences.putBooleanPref(PrefsContract.PREF_DONT_SHOW_IF_CRASHED, showIfCrash)
        return this
    }

    fun init() {

        if (preferences.getBooleanPref(PrefsContract.PREF_DONT_SHOW_AGAIN) ||
            preferences.getBooleanPref(PrefsContract.PREF_APP_HAS_CRASHED) &&
            !showIfHasCrashed
        ) {
            return
        }

        if (!showIfHasCrashed) {
            initExceptionHandler()
        }

        // Get date of first launch.
        var dateFirstLaunch: Long? = preferences.getLongPref(PrefsContract.PREF_DATE_FIRST_LAUNCH, 0)
        if (dateFirstLaunch == 0L) {
            dateFirstLaunch = System.currentTimeMillis()
            preferences.putLongPref(PrefsContract.PREF_DATE_FIRST_LAUNCH, dateFirstLaunch)
        }

        // Get and increment launch counter.
        val launchCount = preferences.getLongPref(PrefsContract.PREF_LAUNCH_COUNT, 0) + 1
        preferences.putLongPref(PrefsContract.PREF_LAUNCH_COUNT, launchCount)

        // Show the rate dialog if needed.
        if (launchCount >= minLaunchesUntilPrompt) {
            if (System.currentTimeMillis() >= dateFirstLaunch!! + minDaysUntilPrompt * DateUtils.DAY_IN_MILLIS) {
                showDefaultDialog()
            }
        }
    }

    private fun initExceptionHandler() {
        val currentHandler = Thread.getDefaultUncaughtExceptionHandler()

        // Don't register again if already registered.
        if (currentHandler !is ExceptionHandler) {

            // Register default exceptions handler.
            Thread.setDefaultUncaughtExceptionHandler(
                ExceptionHandler(
                    currentHandler,
                    hostActivity
                )
            )
        }
    }

    private fun showDefaultDialog() {
        val title = "Rate Our App"
        val message =
            "If you are enjoying our app, Please take a moment to rate it on PlayStore. Thanks for your support!"
        val rate = "Rate it !"
        val dismiss = "Remind Me Later"

        AlertDialog.Builder(hostActivity, R.style.AlertDialogCustom)
            .setCancelable(false)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(rate) { dialog, which ->
                onPositive()
                dialog.dismiss()
            }
            .setNeutralButton(dismiss) { dialog, which ->
                onNegative()
                dialog.dismiss()
            }.show()
    }

    private fun onPositive() {
        try {
            hostActivity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + hostActivity.packageName)
                )
            )
        } catch (ignored: ActivityNotFoundException) {
            Toast.makeText(
                hostActivity,
                "No Play Store installed on device",
                Toast.LENGTH_SHORT
            ).show()
        }

        preferences.putBooleanPref(PrefsContract.PREF_DONT_SHOW_AGAIN, true)
    }

    private fun onNegative() {
        preferences.putLongPref(PrefsContract.PREF_DATE_FIRST_LAUNCH, System.currentTimeMillis())
        preferences.putLongPref(PrefsContract.PREF_LAUNCH_COUNT, 0)
    }
}

class ExceptionHandler
internal constructor(
    private val defaultExceptionHandler: UncaughtExceptionHandler,
    context: Context
) : UncaughtExceptionHandler {
    private val preferences = PrefManager(context)

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        preferences.putBooleanPref(PrefsContract.PREF_APP_HAS_CRASHED, true)

        // Call the original handler.
        defaultExceptionHandler.uncaughtException(thread, throwable)
    }
}

object PrefsContract {
    internal const val PREF_APP_HAS_CRASHED = "pref_app_has_crashed"
    internal const val PREF_DATE_FIRST_LAUNCH = "date_firstlaunch"
    internal const val PREF_LAUNCH_COUNT = "launch_count"
    internal const val PREF_DONT_SHOW_AGAIN = "dont_show_again"
    internal const val PREF_DONT_SHOW_IF_CRASHED = "pref_dont_show_if_crashed"
}