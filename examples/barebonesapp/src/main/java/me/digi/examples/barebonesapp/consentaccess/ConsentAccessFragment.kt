package me.digi.barebonesapp.consentaccess

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.digi.examples.barebonesapp.R

class ConsentAccessFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.consent_access_fragment_layout, container, false)
    }
}
