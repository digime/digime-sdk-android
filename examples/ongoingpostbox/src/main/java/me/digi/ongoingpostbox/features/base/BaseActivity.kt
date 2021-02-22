package me.digi.ongoingpostbox.features.base

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

abstract class BaseActivity : AppCompatActivity() {

    fun setFragment(root: Int, fragment: Fragment) {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(root, fragment)
        transaction.commit()
    }
}