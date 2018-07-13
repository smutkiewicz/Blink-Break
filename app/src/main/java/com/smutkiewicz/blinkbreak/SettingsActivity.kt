package com.smutkiewicz.blinkbreak

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.smutkiewicz.blinkbreak.util.PREF_NOTIFICATIONS_WHEN_DEVICE_LOCKED
import com.smutkiewicz.blinkbreak.util.StatsHelper.Companion.STAT_LAST_BREAK
import com.smutkiewicz.blinkbreak.util.StatsHelper.Companion.STAT_SKIPPED_BREAKS
import com.smutkiewicz.blinkbreak.util.StatsHelper.Companion.STAT_UNSKIPPED_BREAKS

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (fragmentManager.findFragmentById(android.R.id.content) == null) {
            fragmentManager.beginTransaction()
                    .add(android.R.id.content, SettingsFragment()).commit()
        }

        setActionBar()
    }

    private fun setActionBar() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragment() {

        lateinit var sp: SharedPreferences

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)
            sp = PreferenceManager.getDefaultSharedPreferences(activity)

            setNotifyWhenLockedPreferenceListener()
            setStatsPreferenceListener()
            setInfoPreferenceListener()
        }

        private fun setNotifyWhenLockedPreferenceListener() {
            val notifyWhenLocked = preferenceManager.findPreference(NOTIFY_WHEN_LOCKED)
            notifyWhenLocked.setOnPreferenceChangeListener({ _, newValue ->
                sp.edit().putBoolean(PREF_NOTIFICATIONS_WHEN_DEVICE_LOCKED, newValue as Boolean).apply()
                true
            })
        }

        private fun setStatsPreferenceListener() {
            val stats = preferenceManager.findPreference(STATS)
            stats.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val builder = AlertDialog.Builder(activity)
                val info = resources.getString(R.string.settings_stats_message)

                builder.setMessage(info)
                        .setTitle(R.string.settings_reset_stats_title)
                        .setIcon(R.drawable.ic_alert_24dp)
                        .setPositiveButton(android.R.string.ok,
                                { dialog, _ ->
                                    sp.edit().putString(STAT_LAST_BREAK, "Never").apply()
                                    sp.edit().putInt(STAT_UNSKIPPED_BREAKS, 0).apply()
                                    sp.edit().putInt(STAT_SKIPPED_BREAKS, 0).apply()
                                    dialog.dismiss()
                                })
                        .setNegativeButton(android.R.string.no, { dialog, _ ->
                            dialog.dismiss()
                        })
                        .create().show()

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

            builder.setMessage(info)
                    .setTitle(R.string.settings_info_author_title)
                    .setIcon(R.mipmap.ic_launcher_round)
                    .setPositiveButton(android.R.string.ok,
                    { dialog, _ -> dialog.dismiss() })
                    .create().show()
        }
    }

    companion object {
        private const val INFO = "pref_info"
        private const val STATS = "pref_reset_stats"
        private const val NOTIFY_WHEN_LOCKED = "pref_notifications_device_locked"
    }
}
