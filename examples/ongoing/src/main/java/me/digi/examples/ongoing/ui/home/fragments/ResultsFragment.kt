package me.digi.examples.ongoing.ui.home.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
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

class ResultsFragment(private val digiMeService: DigiMeService) : BaseFragment(R.layout.fragment_data_breakdown) {

    private val parent: HomeActivity by lazy { activity as HomeActivity }
    private val resultsAdapter: ResultsAdapter by lazy { ResultsAdapter(context!!) }
    private var firstExecution = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        breakdownRecyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = resultsAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        if (firstExecution) {
            loadData()
            firstExecution = false
        }
    }

    private fun loadData() {
        resultsAdapter.performDiffAndUpdate(GenreInsightGenerator.generateInsights(digiMeService.getCachedSongs()))

        val songs = emptyList<Song>().toMutableList()

        digiMeService.obtainAccessRights(parent)
            .andThen(digiMeService.fetchData())
            .doOnNext { songs.add(it) }
            .buffer(3L, TimeUnit.SECONDS)
            .map { GenreInsightGenerator.generateInsights(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(resultsAdapter::performDiffAndUpdate, ::handleError)
            {
                digiMeService.cacheSongs(songs)
                dismissLoadingState()
            }
    }

    private fun handleError(error: Throwable) {
        val msg = AlertDialog.Builder(parent)
        msg.setTitle("Oops...")
        msg.setMessage("""
        We encountered an error whilst communicating with digi.me.
                
        Error Details: ${error.localizedMessage}
        """.trimIndent())
        msg.setNeutralButton("Try Again") { _, _ ->
            loadData()
        }
        msg.create().show()
    }

    private fun dismissLoadingState() {
        loadingSection.visibility = View.GONE
    }
}