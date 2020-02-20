package me.digi.examples.barebonesapp.postbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.postbox_fragment_layout.view.*
import me.digi.examples.barebonesapp.R

class PostboxFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.postbox_fragment_layout, container, false)

        view.open_digime.setOnClickListener { openDigiMe() }

        return view
    }

    private fun openDigiMe(){
        val launchIntent = activity?.packageManager?.getLaunchIntentForPackage("me.digi.app3")
        startActivity(launchIntent)
    }
}