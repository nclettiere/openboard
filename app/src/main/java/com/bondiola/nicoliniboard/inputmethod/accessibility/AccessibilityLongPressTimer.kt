package com.bondiola.nicoliniboard.inputmethod.accessibility

import android.content.Context
import android.os.Handler
import android.os.Message
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
            com.bondiola.nicoliniboard.inputmethod.accessibility.AccessibilityLongPressTimer.Companion.MSG_LONG_PRESS -> {
                cancelLongPress()
                mCallback.performLongClickOn(msg.obj as com.bondiola.nicoliniboard.inputmethod.keyboard.Key)
                return
            }
            else -> {
                super.handleMessage(msg)
                return
            }
        }
    }

    fun startLongPress(key: com.bondiola.nicoliniboard.inputmethod.keyboard.Key?) {
        cancelLongPress()
        val longPressMessage = obtainMessage(com.bondiola.nicoliniboard.inputmethod.accessibility.AccessibilityLongPressTimer.Companion.MSG_LONG_PRESS, key)
        sendMessageDelayed(longPressMessage, mConfigAccessibilityLongPressTimeout)
    }

    fun cancelLongPress() {
        removeMessages(com.bondiola.nicoliniboard.inputmethod.accessibility.AccessibilityLongPressTimer.Companion.MSG_LONG_PRESS)
    }

    companion object {
        private const val MSG_LONG_PRESS = 1
    }

    init {
        mConfigAccessibilityLongPressTimeout = context.resources.getInteger(
                R.integer.config_accessibility_long_press_key_timeout).toLong()
    }
}