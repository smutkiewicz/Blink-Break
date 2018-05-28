package com.smutkiewicz.blinkbreak.extensions

import android.app.Activity
import android.os.Build
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

/**
 * Helper extension function for showing a [Toast]
 */
fun Activity.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

/**
 * Permission for write is only needed for API >= 23
 */
fun Activity.checkForWritePermissions(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.System.canWrite(applicationContext)
    } else {
        true
    }
}

fun AppCompatActivity.checkSelfPermissionCompat(permission: String) =
        ActivityCompat.checkSelfPermission(this, permission)

fun AppCompatActivity.shouldShowRequestPermissionRationaleCompat(permission: String) =
        ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

fun AppCompatActivity.requestPermissionsCompat(permissionsArray: Array<String>,
                                               requestCode: Int) {
    ActivityCompat.requestPermissions(this, permissionsArray, requestCode)
}
