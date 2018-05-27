package com.smutkiewicz.blinkbreak.util

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import java.util.concurrent.TimeUnit

/**
 * Created by Admin on 2018-05-28.
 */
class JobSchedulerHelper {

    lateinit private var serviceComponent: ComponentName
    lateinit private var context: Context
    private var jobId = 0

    private fun scheduleJob(breakEvery: Int, breakDuration: Int) {
        val builder = JobInfo.Builder(jobId++, serviceComponent)
        val minimumLatency: Long = 1000

        // Extras, interval between consecutive jobs.
        val extras = PersistableBundle()

        // Extras, duration of lower brightness break
        val breakEveryInMillis = breakEvery.toLong() * TimeUnit.SECONDS.toMillis(1)
        val breakDurationInMillis = breakDuration.toLong() * TimeUnit.SECONDS.toMillis(1)

        extras.putLong(BREAK_EVERY_KEY, breakEveryInMillis)
        extras.putLong(BREAK_DURATION_KEY, breakDurationInMillis)

        // Finish configuring the builder
        builder.run {
            setMinimumLatency(minimumLatency)
            setBackoffCriteria(minimumLatency, JobInfo.BACKOFF_POLICY_LINEAR)
            setRequiresDeviceIdle(false)
            setRequiresCharging(false)
            setExtras(extras)
        }

        // Schedule job
        (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(builder.build())
    }

    private fun cancelAllJobs() {
        (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).cancelAll()
    }

    private fun checkIfThereArePendingJobs(): Boolean {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val allPendingJobs = jobScheduler.allPendingJobs

        return allPendingJobs.size > 0
    }
}