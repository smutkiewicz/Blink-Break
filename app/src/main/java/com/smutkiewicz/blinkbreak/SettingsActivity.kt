package com.smutkiewicz.blinkbreak

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.smutkiewicz.blinkbreak.util.PREF_NOTIFICATIONS_WHEN_DEVICE_LOCKED
import com.smutkiewicz.blinkbreak.util.PREF_POSTPONE_DURATION
import com.smutkiewicz.blinkbreak.util.StatsHelper

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (fragmentManager.findFragmentById(android.R.id.content) == null) {
            fragmentManager
                .beginTransaction()
                .add(android.R.id.content, SettingsFragment())
                .commit()
        }

        setActionBar()
    }

    private fun setActionBar() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragment() {

        private lateinit var sp: SharedPreferences

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)
            sp = PreferenceManager.getDefaultSharedPreferences(activity)

            setNotifyWhenLockedPreferenceListener()
            setPostponeDurationPreferenceListener()
            setStatsPreferenceListener()
            setInfoPreferenceListener()
        }

        private fun setPostponeDurationPreferenceListener() {
            val postponeDuration = preferenceManager.findPreference(POSTPONE_DURATION) as ListPreference
            postponeDuration.setOnPreferenceChangeListener { _, newValue ->
                sp.edit().putString(PREF_POSTPONE_DURATION, newValue as String?).apply()
                true
            }
        }

        private fun setNotifyWhenLockedPreferenceListener() {
            val notifyWhenLocked = preferenceManager.findPreference(NOTIFY_WHEN_LOCKED)
            notifyWhenLocked.setOnPreferenceChangeListener { _, newValue ->
                sp.edit().putBoolean(PREF_NOTIFICATIONS_WHEN_DEVICE_LOCKED, newValue as Boolean).apply()
                true
            }
        }

        private fun setStatsPreferenceListener() {
            val stats = preferenceManager.findPreference(STATS)

            stats.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val builder = AlertDialog.Builder(activity)
                val info = resources.getString(R.string.settings_stats_message)

                builder.apply {
                    setMessage(info)
                    setTitle(R.string.settings_reset_stats_title)
                    setIcon(R.drawable.ic_alert_24dp)
                    setNegativeButton(android.R.string.no) { dialog, _ -> dialog.dismiss() }
                    setPositiveButton(android.R.string.ok) { dialog, _ ->
                        val statHelper = StatsHelper(activity)
                        statHelper.resetValues()
                        dialog.dismiss()
                    }
                }.create().show()

                return@OnPreferenceClickListener true
            }
        }

        private fun setInfoPreferenceListener() {
            val info = preferenceManager.findPreference(INFO)

            info.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                onInfoPreferenceClicked()
                return@OnPreferenceClickListener true
            }
        }

        private fun onInfoPreferenceClicked() {
            val builder = AlertDialog.Builder(activity)
            val info = resources.getString(R.string.settings_info_author)

            builder.apply {
                setMessage(info)
                setTitle(R.string.settings_info_author_title)
                setIcon(R.mipmap.ic_launcher_round)
                setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            }.create().show()
        }
    }

    private companion object {
        const val INFO = "pref_info"
        const val STATS = "pref_reset_stats"
        const val NOTIFY_WHEN_LOCKED = "pref_notifications_device_locked"
        const val POSTPONE_DURATION = "pref_postpone_duration"
    }
}
