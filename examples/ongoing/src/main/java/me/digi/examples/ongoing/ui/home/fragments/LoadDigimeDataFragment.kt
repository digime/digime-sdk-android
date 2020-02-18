package me.digi.examples.ongoing.ui.home.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.gson.Gson
import io.objectbox.Box
import me.digi.examples.ongoing.base.BaseFragment
import me.digi.examples.ongoing.model.Song
import me.digi.examples.ongoing.model.Song_
import me.digi.examples.ongoing.service.DigiMeService
import me.digi.examples.ongoing.service.ObjectBox
import me.digi.examples.ongoing.ui.home.HomeActivity
import me.digi.examples.ongoing.utils.GenreInsightGenerator
import me.digi.ongoing.R
import me.digi.sdk.entities.DMEOAuthToken

class LoadDigimeDataFragment : BaseFragment(R.layout.fragment_loading) {

    private val genreBox: Box<Song> = ObjectBox.boxStore.boxFor(Song::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserData()
    }

    private fun loadUserData() {
        var counter = 0
        genreBox.removeAll()

        val digiMePreference = context?.getSharedPreferences("Default", Context.MODE_PRIVATE)
        val oAuthTokenJson = digiMePreference?.getString("Token", null)!!
        val oAuthToken = Gson().fromJson(oAuthTokenJson, DMEOAuthToken::class.java)

        DigiMeService.requestConsent(context as Activity, oAuthToken) { session, error ->
            if (error != null) Log.e("Error", error.toString())
            if (session != null) {
                Log.d("Session", session.toString())
                DigiMeService.getData {
                    it.forEach {
                        genreBox.put(it)
                        counter++
                    }
                    Log.i("COUNTER TOTAL", counter.toString())
                    openDataBreakdownFragment()
                }
            }
            else {
                Toast.makeText(context, "Invalid session", Toast.LENGTH_LONG).show()
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