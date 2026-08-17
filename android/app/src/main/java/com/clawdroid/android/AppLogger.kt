package com.clawdroid.android

import android.util.Log

object AppLogger {
    private const val TAG = "ClawDroid"

    fun d(tag: String, message: String) = Log.d("$TAG/$tag", message)
    fun i(tag: String, message: String) = Log.i("$TAG/$tag", message)
    fun w(tag: String, message: String, e: Exception? = null) {
        if (e != null) Log.w("$TAG/$tag", message, e) else Log.w("$TAG/$tag", message)
    }
    fun e(tag: String, message: String, e: Exception? = null) {
        if (e != null) Log.e("$TAG/$tag", message, e) else Log.e("$TAG/$tag", message)
    }
}
