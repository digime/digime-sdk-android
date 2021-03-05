package me.digi.ongoingpostbox.utils

import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import me.digi.ongoingpostbox.R

fun Fragment.snackBarLong(message: String) =
    Snackbar
        .make(requireView(), message, Snackbar.LENGTH_LONG)
        .show()

fun Fragment.snackBarIndefiniteWithAction(message: String) {
    val snackBar: Snackbar = Snackbar
        .make(requireView(), message, Snackbar.LENGTH_INDEFINITE)
        .setAction(getText(R.string.action_ok)) {}

    val snackBarView: View = snackBar.view

    val tv: TextView = snackBarView.findViewById(R.id.snackbar_text) as TextView

    tv.maxLines = 5

    snackBar.show()
}

fun Fragment.replaceFragment(fragmentManager: FragmentManager) {
    fragmentManager
        .beginTransaction()
        .replace(R.id.navigation_fragment_holder, this)
        .commit()
}