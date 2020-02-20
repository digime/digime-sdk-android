package me.digi.examples.ongoing.ui.home.fragments

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import io.objectbox.Box
import me.digi.examples.ongoing.base.BaseFragment
import me.digi.examples.ongoing.model.Song
import me.digi.examples.ongoing.service.DigiMeService
import me.digi.examples.ongoing.service.ObjectBox
import me.digi.ongoing.R

class LoadDigimeDataFragment : BaseFragment(R.layout.fragment_loading) {

    private val genreBox: Box<Song> = ObjectBox.boxStore.boxFor(Song::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserData()
    }

    private fun loadUserData() {
        genreBox.removeAll()

        DigiMeService.requestConsent(context as Activity) { session, credentials, error ->
            if (error == null) {
                DigiMeService.getData { songs ->
                    genreBox.put(songs)
                    openDataBreakdownFragment()
                }
            }
        }
    }

    private fun openDataBreakdownFragment() {
        val fragment: Fragment = DataBreakdownFragment()
        val manager: FragmentManager = activity!!.supportFragmentManager
        val transaction: FragmentTransaction = manager.beginTransaction()
        transaction.replace(R.id.homeRoot, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}