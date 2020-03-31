package me.digi.examples.ongoing.base

import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

abstract class BaseActivity(@LayoutRes layout: Int) : AppCompatActivity(layout) {

    fun setFragment(root: Int, fragment: Fragment) {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(root, fragment)
        transaction.commit()
    }

}