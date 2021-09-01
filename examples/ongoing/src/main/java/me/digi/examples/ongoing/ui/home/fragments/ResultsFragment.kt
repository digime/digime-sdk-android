package me.digi.examples.ongoing.ui.home.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_data_breakdown.*
import me.digi.examples.ongoing.base.BaseFragment
import me.digi.examples.ongoing.model.Song
import me.digi.examples.ongoing.service.DigiMeService
import me.digi.examples.ongoing.ui.home.HomeActivity
import me.digi.examples.ongoing.ui.home.ResultsAdapter
import me.digi.examples.ongoing.utils.GenreInsightGenerator
import me.digi.ongoing.R
import java.util.concurrent.TimeUnit

class ResultsFragment(private val digiMeService: DigiMeService) :
    BaseFragment(R.layout.fragment_data_breakdown) {

    private val parent: HomeActivity by lazy { activity as HomeActivity }
    private val resultsAdapter: ResultsAdapter by lazy { ResultsAdapter() }
    private var firstExecution = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        breakdownRecyclerView.adapter = resultsAdapter
        resultsAdapter.submitList(GenreInsightGenerator.generateInsights(digiMeService.getCachedSongs()))
    }

    override fun onResume() {
        super.onResume()
        if (firstExecution) {
            handleFlow()
            firstExecution = false
        }
    }

    private fun handleFlow() {
        digiMeService.getCachedCredential()?.let {
            digiMeService.updateCurrentSessionProceedToGetData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onSuccess = { handleDataFlow() }, onError = {
                    Toast.makeText(
                        requireContext(),
                        it.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                })
        } ?: loadData()
    }

    private fun handleDataFlow() {
        resultsAdapter.submitList(GenreInsightGenerator.generateInsights(digiMeService.getCachedSongs()))
        val songs: MutableList<Song> = mutableListOf()
        getData(songs)
    }

    private fun loadData() {
        resultsAdapter.submitList(GenreInsightGenerator.generateInsights(digiMeService.getCachedSongs()))

        val songs: MutableList<Song> = mutableListOf()

        digiMeService.obtainAccessRights(parent)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { getData(songs) },
                onError = ::handleError
            )
    }

    private fun getData(songs: MutableList<Song>) {
        digiMeService.fetchData()
            .doOnNext { songs.add(it) }
            .buffer(3L, TimeUnit.SECONDS)
            .map { GenreInsightGenerator.generateInsights(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(resultsAdapter::submitList, ::handleError) {
                digiMeService.cacheSongs(songs)
                dismissLoadingState()
            }
    }

    private fun handleError(error: Throwable) {
        val msg = AlertDialog.Builder(parent)
        msg.setTitle("Oops...")
        msg.setMessage(
            """
        We encountered an error whilst communicating with digi.me.
                
        Error Details: ${error.localizedMessage}
        """.trimIndent()
        )
        msg.setNeutralButton("Try Again") { _, _ ->
            loadData()
        }
        msg.create().show()
    }

    private fun dismissLoadingState() {
        loadingSection.visibility = View.GONE
    }
}