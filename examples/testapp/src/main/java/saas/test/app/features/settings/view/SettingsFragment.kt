package saas.test.app.features.settings.view

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.gson.Gson
import saas.test.app.R
import saas.test.app.data.localaccess.MainLocalDataAccess
import saas.test.app.databinding.FragmentSettingsBinding
import saas.test.app.entities.ContractHandler
import saas.test.app.framework.utils.AppConst.CACHED_APP_ID
import saas.test.app.framework.utils.AppConst.CACHED_BASE_URL
import saas.test.app.framework.utils.AppConst.CACHED_PUSH_CONTRACT
import saas.test.app.framework.utils.AppConst.CACHED_READ_CONTRACT
import saas.test.app.framework.utils.AppConst.CACHED_READ_RAW_CONTRACT
import saas.test.app.framework.utils.AppConst.CONTRACT_PREFS_KEY
import org.koin.android.ext.android.inject

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val binding: FragmentSettingsBinding by viewBinding()
    private val localAccess: MainLocalDataAccess by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        handleCachedUi()
//        setupTargetUrlUi()
//        setupClickListeners()
    }

//    private fun handleCachedUi() {
//        localAccess.getCachedBaseUrl()?.let { binding.ilBaseUrlPicker.editText?.setText(it) }
//        localAccess.getCachedAppId()?.let { binding.ilAppId.editText?.setText(it) }
//        localAccess.getCachedReadContract()?.contractId?.let {
//            binding.ilReadContractId.editText?.setText(it)
//        }
//        localAccess.getCachedReadContract()?.privateKeyHex?.let {
//            binding.ilReadPrivateKey.editText?.setText(it)
//        }
//        localAccess.getCachedPushContract()?.contractId?.let {
//            binding.ilPushContractId.editText?.setText(it)
//        }
//        localAccess.getCachedPushContract()?.privateKeyHex?.let {
//            binding.ilPushPrivateKey.editText?.setText(it)
//        }
//        localAccess.getCachedReadRawContract()?.contractId?.let {
//            binding.ilReadRawContractId.editText?.setText(it)
//        }
//        localAccess.getCachedReadRawContract()?.privateKeyHex?.let {
//            binding.ilReadRawPrivateKey.editText?.setText(it)
//        }
//    }

//    private fun setupClickListeners() {
//        binding.btnSaveConfiguration.setOnClickListener(this)
//    }

//    private fun setupTargetUrlUi() {
//        val endpoints: List<String> = resources.getStringArray(R.array.endpoints).toList()
//        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, endpoints)
//        (binding.ilBaseUrlPicker.editText as? AutoCompleteTextView)?.setAdapter(adapter)
//    }

//    private fun saveConfiguration() {
//
//        // Define views
//        val baseUrl = binding.ilBaseUrlPicker.editText?.text.toString().trim()
//        val appId = binding.ilAppId.editText?.text.toString().trim()
//        val readContractId = binding.ilReadContractId.editText?.text.toString().trim()
//        val readPrivateKey = binding.ilReadPrivateKey.editText?.text.toString().trim()
//        val pushContractId = binding.ilPushContractId.editText?.text.toString().trim()
//        val pushPrivateKey = binding.ilPushPrivateKey.editText?.text.toString().trim()
//        val readRawContractId = binding.ilReadRawContractId.editText?.text.toString().trim()
//        val readRawPrivateKey = binding.ilReadRawPrivateKey.editText?.text.toString().trim()
//
//        if (baseUrl.isNotEmpty()) {
//            requireContext().getSharedPreferences(CONTRACT_PREFS_KEY, Context.MODE_PRIVATE)
//                .edit().run {
//                    val encodedContractHandler = Gson().toJson(baseUrl)
//                    putString(CACHED_BASE_URL, encodedContractHandler)
//
//                    apply()
//                }
//            binding.ilBaseUrlPicker.error = null
//        } else binding.ilBaseUrlPicker.error = getString(R.string.errorBaseUrl)
//
//        if (appId.isNotEmpty()) {
//            requireContext().getSharedPreferences(CONTRACT_PREFS_KEY, Context.MODE_PRIVATE)
//                .edit().run {
//                    val encodedContractHandler = Gson().toJson(appId)
//                    putString(CACHED_APP_ID, encodedContractHandler)
//
//                    apply()
//                }
//
//            binding.ilAppId.error = null
//        } else binding.ilAppId.error = getString(R.string.errorAppId)
//
//        if (readContractId.isNotEmpty() && readPrivateKey.isNotEmpty())
//            storeConfiguration(CACHED_READ_CONTRACT, readContractId, readPrivateKey)
//
//        if (pushContractId.isNotEmpty() && pushPrivateKey.isNotEmpty())
//            storeConfiguration(CACHED_PUSH_CONTRACT, pushContractId, pushPrivateKey)
//
//        if (readRawContractId.isNotEmpty() && readRawPrivateKey.isNotEmpty())
//            storeConfiguration(CACHED_READ_RAW_CONTRACT, readRawContractId, readRawPrivateKey)
//    }

    private fun storeConfiguration(key: String, contractId: String, privateKeyHex: String) {
        requireContext().getSharedPreferences(CONTRACT_PREFS_KEY, Context.MODE_PRIVATE)
            .edit().run {
                val contractHandler = ContractHandler(contractId, privateKeyHex)
                val encodedContractHandler: String = Gson().toJson(contractHandler)
                putString(key, encodedContractHandler)

                apply()
            }
    }

//    override fun onClick(view: View?) {
//        when (view?.id) {
//            R.id.btnSaveConfiguration -> saveConfiguration()
//        }
//    }
}