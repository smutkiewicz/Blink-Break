package com.smutkiewicz.blinkbreak.extensions

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.smutkiewicz.blinkbreak.MainActivity

/**
 * Helper extension function for showing a [Toast]
 */
fun Activity.showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

/**
 * Permission for write/draw overlays is only needed for API >= 23
 */
fun Activity.checkForWritePermissions()
    = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.System.canWrite(applicationContext) else true

fun Activity.checkForDrawOverlaysPermissions()
    = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(applicationContext) else true

/**
 * Opens app's settings menu, as WRITE_SETTINGS permission requires intent to Settings.
 */
fun Activity.createWritePermissionsIntent()
{
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivityForResult(intent, MainActivity.MY_PERMISSIONS_REQUEST_WRITE_SETTINGS)
    }
}

fun Activity.createDrawOverlayPermissionsIntent()
{
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = Uri.parse("package:$packageName")
        startActivityForResult(intent, MainActivity.MY_PERMISSIONS_REQUEST_DRAW_OVERLAY)
    }
}

