package com.smutkiewicz.blinkbreak

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import com.smutkiewicz.blinkbreak.alarmmanager.AlarmHelper
import com.smutkiewicz.blinkbreak.extensions.*
import com.smutkiewicz.blinkbreak.model.Task
import com.smutkiewicz.blinkbreak.util.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    private lateinit var layout: View
    private lateinit var alarmHelper: AlarmHelper
    private lateinit var sp: SharedPreferences
    private lateinit var statsHelper: StatsHelper

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false)

        layout = findViewById(R.id.layout)
        alarmHelper = AlarmHelper(applicationContext)
        sp = PreferenceManager.getDefaultSharedPreferences(this)
        statsHelper = StatsHelper(this)

        setUserSettings()
        setUpToggleButton()
        setUpSeekBars()
        setUpCheckBoxes()
        setUpStatsScreen()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_settings -> {
                try {
                    val preferencesIntent = Intent(this, SettingsActivity::class.java)
                    startActivity(preferencesIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {
        var prefName = ""

        when (seekBar) {
            breakDurationSeekBar -> {
                prefName = PREF_BREAK_DURATION_PROGRESS
                breakDurationValTextView.text =
                        breakDurationSeekBar.getProgressLabel(this)
            }

            breakEverySeekBar -> {
                prefName = PREF_BREAK_EVERY_PROGRESS
                breakEveryValTextView.text =
                        breakEverySeekBar.getProgressFrequencyLabel(this)
            }
        }

        sp.edit().putInt(prefName, progress).apply()
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        when (seekBar) {
            breakEverySeekBar -> reschedule()
        }
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {}

    private fun setUpToggleButton() {
        serviceToggleButton.setOnCheckedChangeListener{ _, isChecked ->
            when {
                isChecked -> {
                    activatedTextView.text = getString(R.string.service_activated)
                    NotificationsManager.showServiceActiveNotification(this)
                    schedule()
                }
                else -> {
                    activatedTextView.text = getString(R.string.service_deactivated)
                    NotificationsManager.cancelServiceActiveNotification(this)
                    alarmHelper.cancelAlarm()
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
        breakCheckBox.setOnCheckedChangeListener { _, isChecked ->
            // save user preference
            sp.edit().putBoolean(PREF_BREAK_ENABLED, isChecked).apply()

            // block/unlock the layout
            breakLayout.setIsEnabledForChildren(isChecked)

            // settings changed, reschedule if needed
            reschedule()
        }

        notificCheckBox.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(PREF_NOTIFICATIONS, isChecked).apply()
        }

        notificBrightnessCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (checkForWritePermissions()) {
                sp.edit().putBoolean(PREF_LOWER_BRIGHTNESS, isChecked).apply()
            } else {
                notificBrightnessCheckBox.isChecked = false
                requestWriteSettingsPermission()
            }
        }

        notificRsiWindowCheckBox.setOnCheckedChangeListener {_, isChecked ->
            if (checkForDrawOverlaysPermissions()) {
                sp.edit().putBoolean(PREF_RSI_BREAK_WINDOW, isChecked).apply()
            } else {
                notificRsiWindowCheckBox.isChecked = false
                requestDrawOverlayPermission()
            }
        }
    }

    private fun setUpStatsScreen() {
        skippedBreaksTextView.text = statsHelper.skippedBreaks.toString()
        unskippedBreaksTextView.text = statsHelper.unskippedBreaks.toString()
        lastBreakStatTextView.text = statsHelper.calculateTimeDifference()
    }

    private fun requestWriteSettingsPermission() {
        layout.showSnackbar(R.string.write_settings_required,
                Snackbar.LENGTH_INDEFINITE, R.string.ok) {
            createWritePermissionsIntent()
        }
    }

    private fun requestDrawOverlayPermission() {
        layout.showSnackbar(R.string.draw_overlay_required,
                Snackbar.LENGTH_INDEFINITE, R.string.ok) {
            createDrawOverlayPermissionsIntent()
        }
    }

    private fun schedule() {
        val taskToSchedule: Task

        if (breakCheckBox.isChecked) {
            taskToSchedule = Task(breakEvery = breakEverySeekBar.getProgressFrequency(this),
                    breakDuration = breakDurationSeekBar.getProgress(this),
                    areNotificationsEnabled = notificCheckBox.isChecked,
                    isLowerBrightness = notificBrightnessCheckBox.isChecked)

            alarmHelper.scheduleAlarm(taskToSchedule)
        }
    }

    private fun reschedule() {
        if (alarmHelper.checkIfThereArePendingTasks()) {

            Log.d(TAG, "Rescheduling...")
            alarmHelper.cancelAlarm()
            schedule()

        } else {
            if (serviceToggleButton.isChecked) {
                Log.d(TAG, "Rescheduling...")
                schedule()
            } else {
                Log.d(TAG, "No need to reschedule.")
            }
        }
    }

    /**
     * Fetches user's setup and enables/disables UI elements
     * related to the setup on app start.
     */
    private fun setUserSettings() {
        // service activated/deactivated CardView
        if (alarmHelper.checkIfThereArePendingTasks()) {
            serviceToggleButton.isChecked = true
            activatedTextView.text = getString(R.string.service_activated)
            NotificationsManager.showServiceActiveNotification(this)
        } else {
            serviceToggleButton.isChecked = false
            activatedTextView.text = getString(R.string.service_deactivated)
        }

        // Checkboxes
        val isTinyBreakEnabled = sp.getBoolean(PREF_BREAK_ENABLED, false)
        breakCheckBox.isChecked = isTinyBreakEnabled
        breakLayout.setIsEnabledForChildren(isTinyBreakEnabled)

        // Break length/duration SeekBars
        breakEverySeekBar.progress = sp.getInt(PREF_BREAK_EVERY_PROGRESS, 0)
        breakDurationSeekBar.progress = sp.getInt(PREF_BREAK_DURATION_PROGRESS, 0)

        // Break length/duration textViews connected with their SeekBars
        breakDurationValTextView.text =
                breakDurationSeekBar.getProgressLabel(this)
        breakEveryValTextView.text =
                breakEverySeekBar.getProgressFrequencyLabel(this)

        // Notifications section
        notificCheckBox.isChecked = sp.getBoolean(PREF_NOTIFICATIONS, true)
        notificBrightnessCheckBox.isChecked = sp.getBoolean(PREF_LOWER_BRIGHTNESS, false)
        notificRsiWindowCheckBox.isChecked = sp.getBoolean(PREF_RSI_BREAK_WINDOW, false)
    }

    companion object {
        const val TAG = "MainActivity"
        const val MY_PERMISSIONS_REQUEST_WRITE_SETTINGS = 0
        const val MY_PERMISSIONS_REQUEST_DRAW_OVERLAY = 1
    }
}
