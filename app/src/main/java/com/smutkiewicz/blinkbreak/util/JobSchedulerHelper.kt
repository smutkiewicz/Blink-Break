package com.smutkiewicz.blinkbreak.util

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import com.smutkiewicz.blinkbreak.BlinkBreakJobService
import com.smutkiewicz.blinkbreak.extensions.putBooleanValue
import com.smutkiewicz.blinkbreak.model.Job

/**
 * Utility class used by Activities to schedule jobs using JobScheduler class.
 */
class JobSchedulerHelper(private val context: Context) {

    private var jobId = 0
    private var serviceComponent =
            ComponentName(context, BlinkBreakJobService::class.java)

    fun scheduleJob(job: Job?) {
        val builder = JobInfo.Builder(jobId++, serviceComponent)

        // Extras, interval between consecutive jobs.
        val extras = PersistableBundle()

        // Extras, periodic fire time of the break and its duration
        extras.putLong(BREAK_DURATION_KEY, job!!.breakDuration.toLong())
        extras.putInt(BREAK_TYPE_KEY, job!!.breakType)
        extras.putBooleanValue(NOTIFICATIONS_KEY, job.areNotificationsEnabled)
        extras.putBooleanValue(HIGH_IMPORTANCE_KEY, job.highImportance)
        extras.putBooleanValue(LOWER_BRIGHTNESS_KEY, job.isLowerBrightness)

        // Finish configuring the builder
        builder.run {
            setMinimumLatency(job.breakEvery.toLong())
            setBackoffCriteria(job.breakEvery.toLong(), JobInfo.BACKOFF_POLICY_LINEAR)
            setRequiresDeviceIdle(false)
            setRequiresCharging(false)
            setExtras(extras)
        }

        // Schedule job
        (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(builder.build())
    }

    fun cancelAllJobs() {
        (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).cancelAll()
    }

    fun checkIfThereArePendingJobs(): Boolean {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val allPendingJobs = jobScheduler.allPendingJobs

        return allPendingJobs.size > 0
    }
}