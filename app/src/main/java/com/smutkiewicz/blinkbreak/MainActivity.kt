package com.smutkiewicz.blinkbreak

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*




class MainActivity : AppCompatActivity() {

    private var shouldUnbind: Boolean = false
    private var boundService: BlinkBreakService? = null

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            boundService = (service as BlinkBreakService.LocalBinder).service

            Toast.makeText(this@MainActivity, R.string.local_service_connected,
                    Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            boundService = null

            Toast.makeText(this@MainActivity, R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        initToggleButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun initToggleButton() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val activated = preferences.getBoolean(BlinkBreakService.PREF_SERVICE_ACTIVATED, false)

        serviceToggleButton.isChecked = activated

        if (activated) {
            doBindService()
        }

        serviceToggleButton.setOnCheckedChangeListener({ _, isChecked: Boolean ->
            setNewServiceState(isChecked)
        })
    }

    fun doBindService() {
        if (bindService(Intent(this@MainActivity, BlinkBreakService::class.java),
                mConnection, Context.BIND_AUTO_CREATE)) {
            shouldUnbind = true
        } else {
            Log.e("MY_APP_TAG", "Error: The requested service doesn't " + "exist, or this client isn't allowed access to it.")
        }
    }

    fun doUnbindService() {
        if (shouldUnbind) {
            unbindService(mConnection)
            shouldUnbind = false
        }
    }

    fun setNewServiceState(activated: Boolean) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        editor.putBoolean(BlinkBreakService.PREF_SERVICE_ACTIVATED, activated)
        editor.apply()

        if (activated) {
            startService(Intent(this@MainActivity,
                BlinkBreakService::class.java))
        } else {
            stopService(Intent(this@MainActivity,
                    BlinkBreakService::class.java))
        }
    }
}
