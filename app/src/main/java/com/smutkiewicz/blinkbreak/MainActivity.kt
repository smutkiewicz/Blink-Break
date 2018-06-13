package com.smutkiewicz.blinkbreak

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Messenger
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import com.smutkiewicz.blinkbreak.extensions.*
import com.smutkiewicz.blinkbreak.model.Job
import com.smutkiewicz.blinkbreak.util.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    lateinit private var layout: View
    lateinit private var handler: IncomingMessageHandler
    lateinit private var jobHelper: JobSchedulerHelper
    lateinit private var sp: SharedPreferences

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        layout = findViewById(R.id.layout)
        handler = IncomingMessageHandler(this)
        jobHelper = JobSchedulerHelper(this)
        sp = PreferenceManager.getDefaultSharedPreferences(this)

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

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }


    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {
        var prefName = ""

        when (seekBar) {
            breakDurationSeekBar -> {
                prefName = PREF_BREAK_DURATION
                tinyBreakDurationValTextView.text =
                        breakDurationSeekBar.getProgressLabel(this)
            }

            breakEverySeekBar -> {
                prefName = PREF_BREAK_EVERY
                tinyBreakEveryValTextView.text =
                        breakEverySeekBar.getProgressLabel(this)
            }
        }

        sp.edit().putInt(prefName, progress).apply()
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
        reschedule()
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {}

    private fun setUpToggleButton() {
        serviceToggleButton.setOnCheckedChangeListener{ _, isChecked ->
            when {
                isChecked -> {
                    if (checkForWritePermissions()) {
                        activatedTextView.text = getString(R.string.service_activated)
                        showServiceActiveNotification()
                        schedule()
                    } else {
                        requestWriteSettingsPermission()
                        serviceToggleButton.isChecked = false
                    }
                }
                else -> {
                    activatedTextView.text = getString(R.string.service_deactivated)
                    cancelServiceActiveNotification()
                    jobHelper.cancelAllJobs()
                }
            }
        }
    }

    private fun setUpSeekBars() {
        breakDurationSeekBar.setOnSeekBarChangeListener(this)
        breakEverySeekBar.setOnSeekBarChangeListener(this)
    }

    /**
     * Fetches types of breaks settings from Prefs and then sets up layout.
     * Checkboxes are part of UI responsible for enabling/disabling breaks' settings.
     */
    private fun setUpCheckBoxes() {
        tinyBreakCheckBox.setOnCheckedChangeListener { _, isChecked ->
            // save user preference
            sp.edit().putBoolean(PREF_BREAK_ENABLED, isChecked).apply()

            // block/unlock the layout
            tinyBreakLayout.setIsEnabledForChildren(isChecked)

            // settings changed, reschedule if needed
            reschedule()
        }

        notificCheckBox.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(PREF_NOTIFICATIONS, isChecked).apply()
            reschedule()
        }

        notificBrightnessCheckBox.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(PREF_LOWER_BRIGHTNESS, isChecked).apply()
            reschedule()
        }
    }

    /**
     * Binds active job service to our Activity. This is needed for catching eventual callbacks
     * from the service.
     */
    private fun bindToBlinkBreakService() {
        val startServiceIntent = Intent(this, BlinkBreakJobService::class.java)
        val messengerIncoming = Messenger(handler)
        startServiceIntent.putExtra(MESSENGER_INTENT_KEY, messengerIncoming)
        startService(startServiceIntent)
    }

    private fun requestWriteSettingsPermission() {
        layout.showSnackbar(R.string.write_settings_required,
                Snackbar.LENGTH_INDEFINITE, R.string.ok) {
            openAndroidPermissionsMenu()
        }
    }

    /**
     * Opens app's settings menu, as WRITE_SETTINGS permission requires intent to Settings.
     */
    private fun openAndroidPermissionsMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + packageName)
            startActivityForResult(intent, MY_PERMISSIONS_REQUEST_WRITE_SETTINGS)
        }
    }

    private fun schedule() {
        val jobToSchedule: Job

        if (tinyBreakCheckBox.isChecked) {
            jobToSchedule = Job(breakEvery = breakEverySeekBar.getProgress(this),
                    breakDuration = breakDurationSeekBar.getProgress(this),
                    areNotificationsEnabled = notificCheckBox.isChecked,
                    isLowerBrightness = notificBrightnessCheckBox.isChecked)

            jobHelper.scheduleJob(jobToSchedule)
        }
    }

    private fun reschedule() {
        if (checkForWritePermissions()) {
            if (jobHelper.checkIfThereArePendingJobs()) {
                Log.d(TAG, "Rescheduling...")
                jobHelper.cancelAllJobs()
                schedule()
            } else {
                if (serviceToggleButton.isChecked) {
                    Log.d(TAG, "Rescheduling...")
                    schedule()
                } else {
                    Log.d(TAG, "No need to reschedule.")
                }
            }
        } else {
            requestWriteSettingsPermission()
        }
    }

    /**
     * Fetches user's setup and enables/disables UI elements
     * related to the setup on app start.
     */
    private fun setUserSettings() {
        // service activated/deactivated CardView
        if (jobHelper.checkIfThereArePendingJobs()) {
            serviceToggleButton.isChecked = true
            activatedTextView.text = getString(R.string.service_activated)
        } else {
            serviceToggleButton.isChecked = false
            activatedTextView.text = getString(R.string.service_deactivated)
        }

        // Checkboxes
        val isTinyBreakEnabled = sp.getBoolean(PREF_BREAK_ENABLED, false)
        tinyBreakCheckBox.isChecked = isTinyBreakEnabled
        tinyBreakLayout.setIsEnabledForChildren(isTinyBreakEnabled)

        // Break length/duration SeekBars
        breakEverySeekBar.progress = sp.getInt(PREF_BREAK_EVERY, 0)
        breakDurationSeekBar.progress = sp.getInt(PREF_BREAK_DURATION, 0)

        // Break length/duration textViews connected with their SeekBars
        tinyBreakDurationValTextView.text =
                breakDurationSeekBar.getProgressLabel(this)
        tinyBreakEveryValTextView.text =
                breakEverySeekBar.getProgressLabel(this)

        // Notifications section
        notificCheckBox.isChecked = sp.getBoolean(PREF_NOTIFICATIONS, true)
        notificBrightnessCheckBox.isChecked = sp.getBoolean(PREF_LOWER_BRIGHTNESS, true)
    }

    private fun showServiceActiveNotification() {
        val nm = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        val title = getString(R.string.service_is_active)

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val contentIntent = PendingIntent.getActivity(this, 0,
                intent, 0)

        val builder = NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_eye_black)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setColor(Color.MAGENTA)
                .setOngoing(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.setNotificationChannel(SERVICE_CHANNEL_ID,
                    getString(R.string.service_notification_channel_name),
                    NotificationManager.IMPORTANCE_MIN)
            builder.setChannelId(SERVICE_CHANNEL_ID)
        }

        val notification = builder.build()
        nm.notify(R.string.service_is_active, notification)
    }

    private fun cancelServiceActiveNotification() {
        val nm = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(R.string.service_is_active)
    }

    private companion object {
        val TAG = "MainActivity"
        val MY_PERMISSIONS_REQUEST_WRITE_SETTINGS = 0
        val SERVICE_CHANNEL_ID = "blink_break_channel_id"
    }
}
