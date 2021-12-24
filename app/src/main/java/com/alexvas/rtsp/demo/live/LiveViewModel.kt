package com.alexvas.rtsp.demo.live

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

private const val RTSP_REQUEST_KEY = "rtsp_request"
private const val RTSP_USERNAME_KEY = "rtsp_username"
private const val RTSP_PASSWORD_KEY = "rtsp_password"

private const val DEFAULT_RTSP_REQUEST = "rtsp://10.0.1.3:554/axis-media/media.amp"
private const val DEFAULT_RTSP_USERNAME = ""
private const val DEFAULT_RTSP_PASSWORD = ""

private const val LIVE_PARAMS_FILENAME = "live_params"

@SuppressLint("LogNotTimber")
class LiveViewModel : ViewModel() {

    companion object {
        private val TAG: String = LiveViewModel::class.java.simpleName
        private const val DEBUG = true
    }

    val rtspRequest = MutableLiveData<String>().apply {
        value = DEFAULT_RTSP_REQUEST
    }
    val rtspUsername = MutableLiveData<String>().apply {
        value = DEFAULT_RTSP_USERNAME
    }
    val rtspPassword = MutableLiveData<String>().apply {
        value = DEFAULT_RTSP_PASSWORD
    }

//    private val _text = MutableLiveData<String>().apply {
//        value = "This is live Fragment"
//    }
//    val text: LiveData<String> = _text

//    init {
//        // Here you could use the ID to get the user info from the DB or remote server
//        rtspRequest.value = "rtsp://10.0.1.3:554/axis-media/media.amp"
//    }

    fun loadParams(context: Context) {
        if (DEBUG)
            Log.v(TAG, "loadParams()")
        val pref = context.getSharedPreferences(LIVE_PARAMS_FILENAME, Context.MODE_PRIVATE)
        try {
            rtspRequest.setValue(pref.getString(RTSP_REQUEST_KEY, DEFAULT_RTSP_REQUEST))
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
        try {
            rtspUsername.setValue(pref.getString(RTSP_USERNAME_KEY, DEFAULT_RTSP_USERNAME))
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
        try {
            rtspPassword.setValue(pref.getString(RTSP_PASSWORD_KEY, DEFAULT_RTSP_PASSWORD))
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    fun saveParams(context: Context) {
        if (DEBUG) Log.v(TAG, "saveParams()")
        val editor = context?.getSharedPreferences(LIVE_PARAMS_FILENAME, Context.MODE_PRIVATE).edit()
        editor.putString(RTSP_REQUEST_KEY, rtspRequest.value)
        editor.putString(RTSP_USERNAME_KEY, rtspUsername.value)
        editor.putString(RTSP_PASSWORD_KEY, rtspPassword.value)
        editor.apply()
    }

}