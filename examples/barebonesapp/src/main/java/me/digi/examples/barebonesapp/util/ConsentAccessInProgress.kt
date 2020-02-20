package me.digi.examples.barebonesapp.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.data_transfer_in_progress_layout.view.*
import me.digi.examples.barebonesapp.R

class ConsentAccessInProgress : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.data_transfer_in_progress_layout, container, false)

        val bundle = arguments
        val progresText = bundle!!.getString("progressText")

        view.progress_text.text = progresText

        view.loadanimation.show()
        return view
    }
}
