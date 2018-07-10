package com.smutkiewicz.blinkbreak

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity


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
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)

            setInfoPreferenceListener()
        }

        private fun setInfoPreferenceListener() {
            val info = preferenceManager.findPreference(INFO)
            info.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
                onInfoPreferenceClicked()
                return@OnPreferenceClickListener true
            }
        }

        private fun onInfoPreferenceClicked() {
            val builder = AlertDialog.Builder(activity)
            val info = resources.getString(R.string.settings_info_author)

            builder.setMessage(info)
            builder.setTitle(R.string.settings_info_author_title)
            builder.setIcon(R.mipmap.ic_launcher_round)
            builder.setPositiveButton(android.R.string.ok,
                    { dialog, _ -> dialog.dismiss() }
            )

            builder.create().show()
        }
    }

    companion object {
        private val INFO = "pref_info"
    }
}
