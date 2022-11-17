package com.gta.presentation.ui.license.registration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gta.presentation.R
import com.gta.presentation.databinding.FragmentLicenseRegistrationBinding
import com.gta.presentation.ui.MainActivity
import com.gta.presentation.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LicenseRegistrationFragment : BaseFragment<FragmentLicenseRegistrationBinding>(
    R.layout.fragment_license_registration
) {
    private val viewModel: LicenseRegistrationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        (requireActivity() as MainActivity).supportActionBar?.title =
            getString(R.string.license_registration_toolbar)
        initCollector()
    }

    private fun initCollector() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerEvent.collectLatest { state ->
                    // 네비게이트
                }
            }
        }
    }
}
