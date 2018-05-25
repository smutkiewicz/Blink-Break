package com.smutkiewicz.blinkbreak.util

import android.os.Handler
import android.os.Message
import android.os.Messenger
import android.view.View
import com.smutkiewicz.blinkbreak.MainActivity
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

/**
 * A [Handler] allows you to send messages associated with a thread. A [Messenger]
 * uses this handler to communicate from [MyJobService]. It's also used to make
 * the start and stop views blink for a short period of time.
 */
internal class IncomingMessageHandler(activity: MainActivity) : Handler() {

    // Prevent possible leaks with a weak reference.
    private val mainActivity: WeakReference<MainActivity> = WeakReference(activity)

    override fun handleMessage(msg: Message) {
        val mainActivity = mainActivity.get() ?: return
        when (msg.what) {
        /*
         * Receives callback from the service when a job has landed
         * on the app. Turns on indicator and sends a message to turn it off after
         * a second.
         */
            MSG_COLOR_START -> {
                // Start received, turn on the indicator and show text.
                //showStartView.setBackgroundColor(getColor(mainActivity, R.color.start_received))
                //updateParamsTextView(msg.obj, "started")
                sendMessageDelayed(Message.obtain(this, MSG_UNCOLOR_START),
                        TimeUnit.SECONDS.toMillis(1))
            }
            MSG_COLOR_STOP -> {
                // Stop received, turn on the indicator and show text.
                //showStopView.setBackgroundColor(getColor(mainActivity, R.color.stop_received))
                //updateParamsTextView(msg.obj, "stopped")
                sendMessageDelayed(obtainMessage(MSG_UNCOLOR_STOP), TimeUnit.SECONDS.toMillis(1))
            }
            MSG_UNCOLOR_START -> {
                //uncolorButtonAndClearText(showStartView, mainActivity)
            }
            MSG_UNCOLOR_STOP -> {
                //uncolorButtonAndClearText(showStopView, mainActivity)
            }
        }
    }

    private fun uncolorButtonAndClearText(textView: View, activity: MainActivity) {
        //textView.setBackgroundColor(getColor(activity, R.color.none_received))
    }
}