package me.digi.ongoingpostbox.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import me.digi.ongoingpostbox.R

fun Fragment.snackBar(message: String) =
    Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()

fun Fragment.replaceFragment(fragmentManager: FragmentManager) {
    fragmentManager
        .beginTransaction()
        .replace(R.id.navigation_fragment_holder, this)
        .commit()
}