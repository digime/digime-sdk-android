package me.digi.sdk.ui

import android.app.DialogFragment
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import me.digi.sdk.R
import kotlinx.android.synthetic.main.consent_mode_selection_fragment.*

class ConsentModeSelectionDialogue: DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
        }
        return inflater?.inflate(R.layout.consent_mode_selection_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        consentSelectionHeader.text = getText(R.string.consent_mode_selection_heading)
        consentSelectionDetail.text = getText(R.string.consent_mode_selection_detail)
        consentSelectionShareAsGuest.text = getText(R.string.consent_mode_selection_share_as_guest)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            setDimAmount(0.8F)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}