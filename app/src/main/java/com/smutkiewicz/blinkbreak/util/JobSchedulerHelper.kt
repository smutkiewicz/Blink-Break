package com.smutkiewicz.blinkbreak.util

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import com.smutkiewicz.blinkbreak.BlinkBreakJobService

/**
 * Utility class used by Activities to schedule jobs using JobScheduler class.
 */
class JobSchedulerHelper(private val context: Context) {

    private var serviceComponent =
            ComponentName(context, BlinkBreakJobService::class.java)
    private var jobId = 0

    fun scheduleJob(breakEvery: Int, breakDuration: Int) {
        val builder = JobInfo.Builder(jobId++, serviceComponent)

        // Extras, interval between consecutive jobs.
        val extras = PersistableBundle()

        // Extras, periodic fire time of the break and its duration
        extras.putLong(BREAK_EVERY_KEY, breakEvery.toLong())
        extras.putLong(BREAK_DURATION_KEY, breakDuration.toLong())

        // Finish configuring the builder
        builder.run {
            setMinimumLatency(breakEvery.toLong())
            setBackoffCriteria(breakEvery.toLong(), JobInfo.BACKOFF_POLICY_LINEAR)
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