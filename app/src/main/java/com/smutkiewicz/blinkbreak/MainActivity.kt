package com.smutkiewicz.blinkbreak

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Messenger
import android.os.PersistableBundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.smutkiewicz.blinkbreak.extensions.showSnackbar
import com.smutkiewicz.blinkbreak.extensions.showToast
import com.smutkiewicz.blinkbreak.util.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.concurrent.TimeUnit



class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    lateinit private var layout: View
    lateinit private var intervalTextView: TextView
    lateinit private var breakLengthTextView: TextView
    lateinit private var activatedTextView: TextView
    lateinit private var handler: IncomingMessageHandler
    lateinit private var serviceComponent: ComponentName

    private var jobId = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        layout = findViewById(R.id.layout)
        intervalTextView = findViewById(R.id.interval_val_text_view)
        breakLengthTextView = findViewById(R.id.break_length_val_text_view)
        activatedTextView = findViewById(R.id.activated_text_view)
        handler = IncomingMessageHandler(this)
        serviceComponent = ComponentName(this, BlinkBreakJobService::class.java)

        setUpToggleButton()
        setUpSeekBars()

        if (!checkForWritePermissions()) {
            requestWriteSettingsPermission()
        }
    }

    override fun onStop() {
        stopService(Intent(this, BlinkBreakJobService::class.java))
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        bindToBlinkBreakService()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {
        when (seekBar) {
            breakLengthSeekBar -> breakLengthTextView.text = breakLengthSeekBar.progress.toString()
            else -> intervalTextView.text = intervalSeekBar.progress.toString()
        }
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
        if (checkIfThereArePendingJobs()) {
            cancelAllJobs()
            scheduleJob()
        }
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {}

    private fun setUpToggleButton() {
        serviceToggleButton.isChecked = checkIfThereArePendingJobs()
        serviceToggleButton.setOnCheckedChangeListener{ _, isChecked ->
            when {
                isChecked -> {
                    if (checkForWritePermissions()) {
                        activatedTextView.text = getString(R.string.local_service_started)
                        scheduleJob()
                    } else {
                        requestWriteSettingsPermission()
                        serviceToggleButton.isChecked = false
                    }
                }
                else -> {
                    activatedTextView.text = getString(R.string.local_service_stopped)
                    cancelAllJobs()
                }
            }
        }
    }

    private fun setUpSeekBars() {
        breakLengthSeekBar.setOnSeekBarChangeListener(this)
        breakLengthTextView.text = breakLengthSeekBar.progress.toString()

        intervalSeekBar.setOnSeekBarChangeListener(this)
        intervalTextView.text = intervalSeekBar.progress.toString()
    }

    private fun requestWriteSettingsPermission() {
        layout.showSnackbar(R.string.write_settings_required,
                Snackbar.LENGTH_INDEFINITE, R.string.ok) {
            openAndroidPermissionsMenu()
        }
    }

    private fun bindToBlinkBreakService() {
        val startServiceIntent = Intent(this, BlinkBreakJobService::class.java)
        val messengerIncoming = Messenger(handler)
        startServiceIntent.putExtra(MESSENGER_INTENT_KEY, messengerIncoming)
        startService(startServiceIntent)
    }

    private fun openAndroidPermissionsMenu() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:" + packageName)
        startActivityForResult(intent, MY_PERMISSIONS_REQUEST_WRITE_SETTINGS)
    }

    private fun checkForWritePermissions() =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                Settings.System.canWrite(applicationContext)
            else -> true
        }

    private fun scheduleJob() {
        val builder = JobInfo.Builder(jobId++, serviceComponent)
        val minimumLatency: Long = 1000

        // Extras, interval between consecutive jobs.
        val extras = PersistableBundle()
        val interval = intervalSeekBar.progress

        // Extras, duration of lower brightness break
        val breakLength = breakLengthSeekBar.progress

        val intervalInMillis = interval.toLong() * TimeUnit.SECONDS.toMillis(1)
        val breakLengthInMillis = breakLength.toLong() * TimeUnit.SECONDS.toMillis(1)
        val brightnessValue = 0
        extras.putLong(INTERVAL_KEY, intervalInMillis)
        extras.putLong(BREAK_LENGTH_KEY, breakLengthInMillis)
        extras.putInt(BRIGHTNESS_KEY, brightnessValue)

        // Finish configuring the builder
        builder.run {
            setMinimumLatency(minimumLatency)
            setBackoffCriteria(minimumLatency, JobInfo.BACKOFF_POLICY_LINEAR)
            setRequiresDeviceIdle(false)
            setRequiresCharging(false)
            setExtras(extras)
        }

        // Schedule job
        (getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(builder.build())
    }

    private fun cancelAllJobs() {
        (getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).cancelAll()
        showToast(getString(R.string.all_jobs_cancelled))
    }

    private fun checkIfThereArePendingJobs(): Boolean {
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val allPendingJobs = jobScheduler.allPendingJobs

        return allPendingJobs.size > 0
    }

    private companion object {
        val TAG = "MainActivity"
        val MY_PERMISSIONS_REQUEST_WRITE_SETTINGS = 0
    }
}
