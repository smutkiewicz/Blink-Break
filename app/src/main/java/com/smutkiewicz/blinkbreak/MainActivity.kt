package com.smutkiewicz.blinkbreak

import android.annotation.SuppressLint
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
import com.smutkiewicz.blinkbreak.extensions.checkForDrawOverlaysPermissions
import com.smutkiewicz.blinkbreak.extensions.checkForWritePermissions
import com.smutkiewicz.blinkbreak.extensions.createDrawOverlayPermissionsIntent
import com.smutkiewicz.blinkbreak.extensions.createWritePermissionsIntent
import com.smutkiewicz.blinkbreak.extensions.getProgressFrequencyLabel
import com.smutkiewicz.blinkbreak.extensions.getProgressLabel
import com.smutkiewicz.blinkbreak.extensions.showSnackbar
import com.smutkiewicz.blinkbreak.util.NotificationsManager
import com.smutkiewicz.blinkbreak.util.PREF_BREAK_DURATION_PROGRESS
import com.smutkiewicz.blinkbreak.util.PREF_BREAK_EVERY_PROGRESS
import com.smutkiewicz.blinkbreak.util.PREF_LOWER_BRIGHTNESS
import com.smutkiewicz.blinkbreak.util.PREF_NOTIFICATIONS
import com.smutkiewicz.blinkbreak.util.PREF_RSI_BREAK_WINDOW
import com.smutkiewicz.blinkbreak.util.StatsHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener
{
    private lateinit var layout: View
    private lateinit var sp: SharedPreferences
    private lateinit var alarmHelper: AlarmHelper
    private lateinit var statsHelper: StatsHelper

    public override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        layout = findViewById(R.id.layout)
        sp = PreferenceManager.getDefaultSharedPreferences(this)
        alarmHelper = AlarmHelper(applicationContext)
        statsHelper = StatsHelper(this)

        readUserSettings()
        createToggleButton()
        createSeekBars()
        createCheckBoxes()
        createStatsScreen()
    }

    public override fun onResume()
    {
        super.onResume()
        createStatsScreen()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId)
        {
            R.id.action_settings ->
            {
                try
                {
                    val preferencesIntent = Intent(this, SettingsActivity::class.java)
                    startActivity(preferencesIntent)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }

                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean)
    {
        var prefName = ""

        when (seekBar)
        {
            breakDurationSeekBar ->
            {
                prefName = PREF_BREAK_DURATION_PROGRESS
                breakDurationValTextView.text =
                        breakDurationSeekBar.getProgressLabel(this)
            }

            breakEverySeekBar ->
            {
                prefName = PREF_BREAK_EVERY_PROGRESS
                breakEveryValTextView.text =
                        breakEverySeekBar.getProgressFrequencyLabel(this)
            }
        }

        sp.edit().putInt(prefName, progress).apply()
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?)
    {
        when (seekBar) { breakEverySeekBar -> reschedule() }
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {}

    private fun createToggleButton()
    {
        serviceToggleButton.setOnCheckedChangeListener{ _, isChecked ->

            if (isChecked)
            {
                activatedTextView.text = getString(R.string.service_activated)
                NotificationsManager.showServiceActiveNotification(this)
                schedule()
            }
            else
            {
                activatedTextView.text = getString(R.string.service_deactivated)
                NotificationsManager.cancelServiceActiveNotification(this)
                alarmHelper.cancelAlarm()
            }

        }
    }

    private fun createSeekBars()
    {
        breakDurationSeekBar.setOnSeekBarChangeListener(this)
        breakEverySeekBar.setOnSeekBarChangeListener(this)
    }

    private fun createCheckBoxes()
    {
        notificCheckBox.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(PREF_NOTIFICATIONS, isChecked).apply()
        }

        notificBrightnessCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (checkForWritePermissions())
            {
                sp.edit().putBoolean(PREF_LOWER_BRIGHTNESS, isChecked).apply()
            }
            else
            {
                notificBrightnessCheckBox.isChecked = false
                requestWriteSettingsPermission()
            }
        }

        notificRsiWindowCheckBox.setOnCheckedChangeListener {_, isChecked ->
            if (checkForDrawOverlaysPermissions())
            {
                sp.edit().putBoolean(PREF_RSI_BREAK_WINDOW, isChecked).apply()
            }
            else
            {
                notificRsiWindowCheckBox.isChecked = false
                requestDrawOverlayPermission()
            }
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun createStatsScreen()
    {
        val unskippedInARow = statsHelper.unskippedInARow
        var colorResId = R.color.unskipped_low
        var textString = ""

        updateStatsTextViews()

        when
        {
            unskippedInARow > resources.getInteger(R.integer.unskipped_high) ->
            {
                textString = getString(R.string.stats_wow)
                colorResId = R.color.unskipped_high
            }

            unskippedInARow > resources.getInteger(R.integer.unskipped_med) ->
            {
                textString = getString(R.string.stats_nice)
                colorResId = R.color.unskipped_med
            }

            unskippedInARow == 0 -> textString = ""

            else -> textString = getString(R.string.stats_only)
        }

        val mark = when
        {
            unskippedInARow > resources.getInteger(R.integer.unskipped_med) -> "!"
            else -> "."
        }

        unskippedSeriesTextView.apply {
            setTextColor(resources.getColor(colorResId))
            text = getString(
                    R.string.stats_unskipped_breaks_in_a_row,
                    textString,
                    unskippedInARow,
                    mark
                )
        }
    }

    private fun updateStatsTextViews()
    {
        skippedBreaksTextView.text = statsHelper.skippedBreaks.toString()
        unskippedBreaksTextView.text = statsHelper.unskippedBreaks.toString()
        lastBreakStatTextView.text = statsHelper.getTimeDifferenceString()
    }

    private fun requestWriteSettingsPermission()
        = layout.showSnackbar(
            R.string.write_settings_required,
            Snackbar.LENGTH_INDEFINITE, R.string.ok) {
            createWritePermissionsIntent()
        }

    private fun requestDrawOverlayPermission()
        = layout.showSnackbar(
            R.string.draw_overlay_required,
            Snackbar.LENGTH_INDEFINITE, R.string.ok) {
            createDrawOverlayPermissionsIntent()
        }


    private fun schedule()
    {
        alarmHelper.scheduleAlarm()
    }

    private fun reschedule()
    {
        if (alarmHelper.checkIfThereArePendingTasks())
        {
            Log.d(TAG, "Rescheduling...")
            alarmHelper.cancelAlarm()
            schedule()
        }
        else
        {
            if (serviceToggleButton.isChecked)
            {
                Log.d(TAG, "Rescheduling...")
                schedule()
            }
            else
            {
                Log.d(TAG, "No need to reschedule.")
            }
        }
    }

    private fun readUserSettings()
    {
        readServiceActivatedDeactivatedSettings()
        readBreakLengthDurationSeekbarsSettings()
        readNotificationsSectionSettings()
    }

    private fun readServiceActivatedDeactivatedSettings()
    {
        val thereArePendingTasks = alarmHelper.checkIfThereArePendingTasks()
        serviceToggleButton.isChecked = thereArePendingTasks

        if (thereArePendingTasks)
        {
            activatedTextView.text = getString(R.string.service_activated)
            NotificationsManager.showServiceActiveNotification(this)
        }
        else
        {
            activatedTextView.text = getString(R.string.service_deactivated)
            NotificationsManager.cancelServiceActiveNotification(this)
        }
    }

    private fun readBreakLengthDurationSeekbarsSettings()
    {
        breakEverySeekBar.progress = sp.getInt(PREF_BREAK_EVERY_PROGRESS, 0)
        breakDurationSeekBar.progress = sp.getInt(PREF_BREAK_DURATION_PROGRESS, 0)

        breakDurationValTextView.text = breakDurationSeekBar.getProgressLabel(this)
        breakEveryValTextView.text = breakEverySeekBar.getProgressFrequencyLabel(this)
    }

    private fun readNotificationsSectionSettings()
    {
        notificCheckBox.isChecked = sp.getBoolean(PREF_NOTIFICATIONS, true)
        notificBrightnessCheckBox.isChecked = sp.getBoolean(PREF_LOWER_BRIGHTNESS, false)
        notificRsiWindowCheckBox.isChecked = sp.getBoolean(PREF_RSI_BREAK_WINDOW, false)
    }

    companion object
    {
        const val MY_PERMISSIONS_REQUEST_WRITE_SETTINGS = 0
        const val MY_PERMISSIONS_REQUEST_DRAW_OVERLAY = 1
    }
}
