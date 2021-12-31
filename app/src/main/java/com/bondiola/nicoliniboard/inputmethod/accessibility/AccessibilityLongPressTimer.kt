package com.bondiola.nicoliniboard.inputmethod.accessibility

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import com.bondiola.nicoliniboard.inputmethod.keyboard.Key
import com.bondiola.nicoliniboard.inputmethod.latin.R

// Handling long press timer to show a more keys keyboard.
internal class AccessibilityLongPressTimer(private val mCallback: com.bondiola.nicoliniboard.inputmethod.accessibility.AccessibilityLongPressTimer.LongPressTimerCallback,
                                           context: Context) : Handler() {
    interface LongPressTimerCallback {
        fun performLongClickOn(key: com.bondiola.nicoliniboard.inputmethod.keyboard.Key)
    }

    private val mConfigAccessibilityLongPressTimeout: Long
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            MSG_LONG_PRESS -> {
                Log.d("Nicolino", "startLongPress: with key: " + (msg.obj as Key).label)
                cancelLongPress()
                mCallback.performLongClickOn(msg.obj as Key)
                return
            }
            else -> {
                super.handleMessage(msg)
                return
            }
        }
    }

    fun startLongPress(key: Key?) {
        cancelLongPress()
        val longPressMessage = obtainMessage(MSG_LONG_PRESS, key)
        sendMessageDelayed(longPressMessage, mConfigAccessibilityLongPressTimeout)
    }

    fun cancelLongPress() {
        removeMessages(MSG_LONG_PRESS)
    }

    companion object {
        private const val MSG_LONG_PRESS = 1
    }

    init {
        mConfigAccessibilityLongPressTimeout = context.resources.getInteger(
                R.integer.config_accessibility_long_press_key_timeout).toLong()
    }
}