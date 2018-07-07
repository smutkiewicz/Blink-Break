package com.smutkiewicz.blinkbreak.jobscheduler

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import com.smutkiewicz.blinkbreak.extensions.putBooleanValue
import com.smutkiewicz.blinkbreak.model.Task
import com.smutkiewicz.blinkbreak.util.BREAK_DURATION_KEY
import com.smutkiewicz.blinkbreak.util.LOWER_BRIGHTNESS_KEY
import com.smutkiewicz.blinkbreak.util.NOTIFICATIONS_KEY

/**
 * Utility class used by Activities to schedule jobs using JobScheduler class.
 */
class JobSchedulerHelper(private val context: Context) {

    private var jobId = 0
    private var serviceComponent =
            ComponentName(context, BlinkBreakJobService::class.java)

    fun scheduleJob(task: Task?) {
        val builder = JobInfo.Builder(jobId++, serviceComponent)

        // Extras, interval between consecutive jobs.
        val extras = PersistableBundle()

        // Extras, periodic fire time of the break and its duration
        extras.putLong(BREAK_DURATION_KEY, task!!.breakDuration.toLong())
        extras.putBooleanValue(NOTIFICATIONS_KEY, task.areNotificationsEnabled)
        extras.putBooleanValue(LOWER_BRIGHTNESS_KEY, task.isLowerBrightness)

        // Finish configuring the builder
        builder.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setMinimumLatency(task.breakEvery.toLong())
                setBackoffCriteria(task.breakEvery.toLong(), JobInfo.BACKOFF_POLICY_EXPONENTIAL)
                setOverrideDeadline(task.breakEvery.toLong())
            } else {
                setPeriodic(task.breakEvery.toLong())
            }

            setPersisted(true)
            setExtras(extras)
        }

        // Schedule task
        (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler)
                .schedule(builder.build())
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