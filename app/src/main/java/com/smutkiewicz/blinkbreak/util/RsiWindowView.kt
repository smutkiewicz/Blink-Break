package com.smutkiewicz.blinkbreak.util

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.smutkiewicz.blinkbreak.R
import com.smutkiewicz.blinkbreak.alarmmanager.AlarmHelper
import com.smutkiewicz.blinkbreak.util.StatsHelper.Companion.STAT_SKIPPED_BREAKS

private const val TAG = "RsiWindowView"

/**
 * Creates the head layer view which is displayed directly on window manager.
 * It means that the view is above every application's view on your phone -
 * until another application does the same.
 */
class RsiWindowView(myContext: Context, breakDuration: Long) : View(myContext)
{
    private var frameLayout: FrameLayout? = FrameLayout(context)
    private var windowManager: WindowManager? = null
    private var statsHelper: StatsHelper? = StatsHelper(myContext)
    private var skipped: Boolean = false
    private var postponed: Boolean = false

    init
    {
        addToWindowManager()
        initCountdownTimer(breakDuration)
        initCountdownProgressBar(breakDuration)
    }

    fun destroy()
    {
        val isAttachedToWindow: Boolean? = frameLayout?.isAttachedToWindow
        if (isAttachedToWindow != null && isAttachedToWindow)
        {
            windowManager!!.removeView(frameLayout)
            frameLayout = null
        }
    }

    private fun addToWindowManager()
    {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }
        else
        {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.CENTER

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager!!.addView(frameLayout, params)

        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        layoutInflater.inflate(R.layout.rsi_break_window, frameLayout)

        initSkipButton()
        initPostponeButton()
    }

    private fun initSkipButton()
    {
        val rsiWindowButton = frameLayout?.findViewById<Button>(R.id.rsiWindowButton)
        rsiWindowButton?.setOnClickListener {
            skipped = true

            statsHelper?.unskippedInARow = 0
            statsHelper?.increaseValue(STAT_SKIPPED_BREAKS)

            windowManager!!.removeView(frameLayout)
        }
    }

    private fun initPostponeButton()
    {
        val postponeButton = frameLayout?.findViewById<Button>(R.id.postponeButton)
        postponeButton?.setOnClickListener {
            postponed = true

            AlarmHelper(context).schedulePostponedAlarm()
            windowManager!!.removeView(frameLayout)
        }
    }

    private fun initCountdownTimer(millisInFuture: Long)
    {
        object : CountDownTimer(millisInFuture, 1000)
        {
            var textView = frameLayout?.findViewById<TextView>(R.id.windowCountdownTextView)

            override fun onTick(millisUntilFinished: Long)
            {
                var seconds = (millisUntilFinished / 1000)
                val minutes = (seconds / 60)

                seconds %= 60
                textView?.text = (
                        String.format("%02d", minutes)
                        + ":" +
                        String.format("%02d", seconds)
                    )
            }

            override fun onFinish()
            {
                updateStats()
                destroy()
            }

        }.start()
    }

    private fun initCountdownProgressBar(breakDuration: Long)
    {
        object : CountDownTimer(breakDuration, 10)
        {
            var progressBar = frameLayout?.findViewById<ProgressBar>(R.id.progressBar)

            override fun onTick(millisUntilFinished: Long)
            {
                val progress: Int = (100 * (1 - millisUntilFinished.toDouble() / breakDuration.toDouble())).toInt()
                progressBar?.progress = progress
            }

            override fun onFinish()
            {
                progressBar?.progress = 100
            }

        }.start()
    }

    private fun updateStats()
    {
        if (!postponed)
        {
            if (!skipped)
            {
                statsHelper?.apply {
                    increaseValue(StatsHelper.STAT_UNSKIPPED_BREAKS)
                    increaseValue(StatsHelper.STAT_UNSKIPPED_IN_A_ROW)
                    lastBreak = StatsHelper.getTimeStamp()
                }
            }
        }
    }
}