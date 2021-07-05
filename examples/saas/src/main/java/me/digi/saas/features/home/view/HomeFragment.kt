package me.digi.saas.features.home.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import me.digi.saas.R
import me.digi.saas.databinding.FragmentHomeBinding
import me.digi.saas.features.utils.ContractType

class HomeFragment: Fragment(R.layout.fragment_home), View.OnClickListener {

    private val binding: FragmentHomeBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.triggerPullContract.setOnClickListener(this)
        binding.triggerPushContract.setOnClickListener(this)
    }

    private fun navigateToAuthentication(type: String) {
        val bundle = Bundle()
        bundle.putString(ContractType.key, type)
        findNavController().navigate(R.id.homeToAuth, bundle)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.triggerPullContract -> navigateToAuthentication(ContractType.pull)
            R.id.triggerPushContract -> navigateToAuthentication(ContractType.push)
        }
    }
}