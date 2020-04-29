package com.alexvas.rtsp.demo.ui.live

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LiveViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is live Fragment"
    }
    val text: LiveData<String> = _text
}