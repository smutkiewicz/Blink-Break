package com.smutkiewicz.blinkbreak

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Messenger
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
import com.smutkiewicz.blinkbreak.extensions.checkForWritePermissions
import com.smutkiewicz.blinkbreak.extensions.getProgress
import com.smutkiewicz.blinkbreak.extensions.getProgressLabel
import com.smutkiewicz.blinkbreak.extensions.showSnackbar
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
        if (jobHelper.checkIfThereArePendingJobs()) {
            jobHelper.cancelAllJobs()
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
                    jobHelper.cancelAllJobs()
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
     * Checkboxes are part of UI responsible for enabling/disabling breaks' settings.
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

    /**
     * Utility function. Sets isEnabled property for all given layout's children,
     * excluding checkboxes. They are used for setting isEnabled layout's property.
     */
    private fun setIsEnabledForChildren(layout: ConstraintLayout, isEnabled: Boolean) {
        (0 until layout.childCount)
                .map { layout.getChildAt(it) }
                .forEach {
                    if (it !is CheckBox) {
                        it.isEnabled = isEnabled
                    }
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
        if (tinyBreakCheckBox.isChecked) {
            jobHelper.scheduleJob(tinyBreakEverySeekBar.getProgress(this),
                    tinyBreakDurationSeekBar.getProgress(this))
        }

        if (bigBreakCheckBox.isChecked) {
            jobHelper.scheduleJob(bigBreakEverySeekBar.getProgress(this),
                    bigBreakDurationSeekBar.getProgress(this))
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
