package me.digi.sdk.ui

import android.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.digi.sdk.R
import kotlinx.android.synthetic.main.consent_mode_selection_fragment.*

class ConsentModeSelectionDialogue: DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.consent_mode_selection_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        consentSelectionHeader.text = getText(R.string.consent_mode_selection_heading)
        consentSelectionDetail.text = getText(R.string.consent_mode_selection_detail)
        consentSelectionShareAsGuest.text = getText(R.string.consent_mode_selection_share_as_guest)
    }
}