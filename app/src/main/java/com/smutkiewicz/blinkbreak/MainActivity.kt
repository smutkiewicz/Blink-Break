package com.smutkiewicz.blinkbreak

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Messenger
import android.os.PersistableBundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.SeekBar
import com.smutkiewicz.blinkbreak.extensions.getProgress
import com.smutkiewicz.blinkbreak.extensions.getProgressLabel
import com.smutkiewicz.blinkbreak.extensions.showSnackbar
import com.smutkiewicz.blinkbreak.extensions.showToast
import com.smutkiewicz.blinkbreak.util.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    lateinit private var layout: View
    lateinit private var handler: IncomingMessageHandler
    lateinit private var serviceComponent: ComponentName
    lateinit private var sp: SharedPreferences

    private var jobId = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        layout = findViewById(R.id.layout)
        handler = IncomingMessageHandler(this)
        sp = PreferenceManager.getDefaultSharedPreferences(this)
        serviceComponent = ComponentName(this, BlinkBreakJobService::class.java)

        setUserSettings()
        setUpToggleButton()
        setUpSeekBars()
        setUpCheckBoxes()

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
        var prefName: String = ""

        when (seekBar) {
            tinyBreakDurationSeekBar -> {
                prefName = PREF_TINY_BREAK_DURATION
                tinyBreakDurationValTextView.text =
                        tinyBreakDurationSeekBar.getProgressLabel(this)
            }

            tinyBreakEverySeekBar -> {
                prefName = PREF_TINY_BREAK_EVERY
                tinyBreakEveryValTextView.text =
                        tinyBreakEverySeekBar.getProgressLabel(this)
            }

            bigBreakDurationSeekBar -> {
                prefName = PREF_BIG_BREAK_DURATION
                bigBreakDurationValTextView.text =
                        bigBreakDurationSeekBar.getProgressLabel(this)
            }

            bigBreakEverySeekBar -> {
                prefName = PREF_BIG_BREAK_EVERY
                bigBreakEveryValTextView.text =
                        bigBreakEverySeekBar.getProgressLabel(this)
            }
        }

        sp.edit().putInt(prefName, progress).apply()
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
        if (checkIfThereArePendingJobs()) {
            cancelAllJobs()
            schedule()
        }
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {}

    private fun setUpToggleButton() {
        serviceToggleButton.setOnCheckedChangeListener{ _, isChecked ->
            when {
                isChecked -> {
                    if (checkForWritePermissions()) {
                        activatedTextView.text = getString(R.string.service_activated)
                        schedule()
                    } else {
                        requestWriteSettingsPermission()
                        serviceToggleButton.isChecked = false
                    }
                }
                else -> {
                    activatedTextView.text = getString(R.string.service_deactivated)
                    cancelAllJobs()
                }
            }
        }
    }

    private fun setUpSeekBars() {
        tinyBreakDurationSeekBar.setOnSeekBarChangeListener(this)
        tinyBreakEverySeekBar.setOnSeekBarChangeListener(this)
        bigBreakDurationSeekBar.setOnSeekBarChangeListener(this)
        bigBreakEverySeekBar.setOnSeekBarChangeListener(this)
    }

    /**
     * Fetches types of breaks settings from Prefs and then sets up layout.
     */
    private fun setUpCheckBoxes() {
        tinyBreakCheckBox.setOnCheckedChangeListener { _, isChecked ->
            // save user preference
            sp.edit().putBoolean(PREF_TINY_BREAK_ENABLED, isChecked).apply()

            // block/unlock the layout
            setIsEnabledForChildren(tinyBreakLayout, isChecked)
        }

        bigBreakCheckBox.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(PREF_BIG_BREAK_ENABLED, isChecked).apply()
            setIsEnabledForChildren(bigBreakLayout, isChecked)
        }
    }

    private fun setIsEnabledForChildren(layout: ConstraintLayout, isEnabled: Boolean) {
        (0 until layout.childCount)
                .map { layout.getChildAt(it) }
                .forEach {
                    if (it !is CheckBox) {
                        it.isEnabled = isEnabled
                    }
                }
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

    private fun checkForWritePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(applicationContext)
        } else {
            true
        }
    }

    private fun schedule() {
        if (tinyBreakCheckBox.isChecked) {
            scheduleJob(tinyBreakEverySeekBar.getProgress(this),
                    tinyBreakDurationSeekBar.getProgress(this))
        }

        if (bigBreakCheckBox.isChecked) {
            scheduleJob(bigBreakEverySeekBar.getProgress(this),
                    bigBreakDurationSeekBar.getProgress(this))
        }
    }

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

    /**
     * Fetches user's setup and enables/disables UI elements related to the setup.
     */
    private fun setUserSettings() {
        // service activated/deactivated CardView
        if (checkIfThereArePendingJobs()) {
            serviceToggleButton.isChecked = true
            activatedTextView.text = getString(R.string.service_activated)
        } else {
            serviceToggleButton.isChecked = false
            activatedTextView.text = getString(R.string.service_deactivated)
        }

        // Checkboxes
        val isTinyBreakEnabled = sp.getBoolean(PREF_TINY_BREAK_ENABLED, false)
        val isBigBreakEnabled = sp.getBoolean(PREF_BIG_BREAK_ENABLED, false)

        tinyBreakCheckBox.isChecked = isTinyBreakEnabled
        setIsEnabledForChildren(tinyBreakLayout, isTinyBreakEnabled)

        bigBreakCheckBox.isChecked = isBigBreakEnabled
        setIsEnabledForChildren(bigBreakLayout, isBigBreakEnabled)

        // Break length/duration SeekBars
        tinyBreakEverySeekBar.progress = sp.getInt(PREF_TINY_BREAK_EVERY, 0)
        bigBreakEverySeekBar.progress = sp.getInt(PREF_BIG_BREAK_EVERY, 0)
        tinyBreakDurationSeekBar.progress = sp.getInt(PREF_TINY_BREAK_DURATION, 0)
        bigBreakDurationSeekBar.progress = sp.getInt(PREF_BIG_BREAK_DURATION, 0)

        // Break length/duration textViews connected with their SeekBars
        tinyBreakDurationValTextView.text =
                tinyBreakDurationSeekBar.getProgressLabel(this)
        tinyBreakEveryValTextView.text =
                tinyBreakEverySeekBar.getProgressLabel(this)
        bigBreakDurationValTextView.text =
                bigBreakDurationSeekBar.getProgressLabel(this)
        bigBreakEveryValTextView.text =
                bigBreakEverySeekBar.getProgressLabel(this)
    }

    private companion object {
        val TAG = "MainActivity"
        val MY_PERMISSIONS_REQUEST_WRITE_SETTINGS = 0
    }
}
