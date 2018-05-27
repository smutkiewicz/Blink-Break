package com.smutkiewicz.blinkbreak.util

import android.os.Handler
import android.os.Message
import android.os.Messenger
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
        when (msg.what) {
            MSG_COLOR_START -> {
                sendMessageDelayed(Message.obtain(this, MSG_UNCOLOR_START),
                        TimeUnit.SECONDS.toMillis(1))
            }
            MSG_COLOR_STOP -> {
                sendMessageDelayed(obtainMessage(MSG_UNCOLOR_STOP), TimeUnit.SECONDS.toMillis(1))
            }
            MSG_UNCOLOR_START -> {

            }
            MSG_UNCOLOR_STOP -> {

            }
        }
    }
}