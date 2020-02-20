package me.digi.examples.ongoing.ui.home.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_data_breakdown.*
import me.digi.examples.ongoing.base.BaseFragment
import me.digi.examples.ongoing.ui.home.BreakdownAdapter
import me.digi.ongoing.R

class DataBreakdownFragment : BaseFragment(R.layout.fragment_data_breakdown) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView : RecyclerView = breakdownRecyclerView
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = BreakdownAdapter(context)
        }
    }

}