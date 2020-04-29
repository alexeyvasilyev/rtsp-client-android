package com.alexvas.rtsp.demo.ui.live

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alexvas.rtsp.demo.R

class LiveFragment : Fragment() {

    private lateinit var dashboardViewModel: LiveViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
                ViewModelProvider(this).get(LiveViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_live, container, false)
        val textView: TextView = root.findViewById(R.id.text_live)
        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}
