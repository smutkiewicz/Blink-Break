package com.smutkiewicz.blinkbreak.util

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.smutkiewicz.blinkbreak.R


/**
 * Creates the head layer view which is displayed directly on window manager.
 * It means that the view is above every application's view on your phone -
 * until another application does the same.
 */
class RsiWindowView(private val myContext: Context) : View(myContext) {

    private val frameLayout: FrameLayout = FrameLayout(context)
    private var windowManager: WindowManager? = null

    init {
        addToWindowManager()
    }

    private fun addToWindowManager() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT)

        params.gravity = Gravity.CENTER

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager!!.addView(frameLayout, params)

        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Here is the place where you can inject whatever layout you want.
        layoutInflater.inflate(R.layout.rsi_break_window, frameLayout)
        /*rsiWindowButton.setOnClickListener{
            windowManager!!.removeView(frameLayout)
        }*/

        // Support dragging the image view
        /*val imageView = frameLayout.findViewById<View>(R.id.imageView) as ImageView
        imageView.setOnTouchListener(object : OnTouchListener {
            private var initX: Int = 0
            private var initY: Int = 0
            private var initTouchX: Int = 0
            private var initTouchY: Int = 0

            fun onTouch(v: View, event: MotionEvent): Boolean {
                val x = event.rawX.toInt()
                val y = event.rawY.toInt()

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initX = params.x
                        initY = params.y
                        initTouchX = x
                        initTouchY = y
                        return true
                    }

                    MotionEvent.ACTION_UP -> return true

                    MotionEvent.ACTION_MOVE -> {
                        params.x = initX + (x - initTouchX)
                        params.y = initY + (y - initTouchY)

                        // Invalidate layout
                        windowManager!!.updateViewLayout(frameLayout, params)
                        return true
                    }
                }
                return false
            }
        })*/
    }

    /**
     * Removes the view from window manager.
     */
    fun destroy() {
        windowManager!!.removeView(frameLayout)
    }
}