package com.ravisharma.playbackmusic.new_work.ui.fragments.onboarding

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.ScanStatus
import com.ravisharma.playbackmusic.databinding.FragmentOnboardingBinding
import com.ravisharma.playbackmusic.new_work.ui.extensions.showToast
import com.ravisharma.playbackmusic.new_work.viewmodel.MusicScanViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OnBoardingFragment : Fragment(R.layout.fragment_onboarding) {

    private val viewModel: MusicScanViewModel by viewModels()

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private var scanStatus: ScanningStatus = ScanningStatus.NOT_STARTED

    private var isDenied: Boolean = false

    private val isPermissionGranted: (Boolean) -> Unit = {
        showScanViews()
    }

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (!permissions.isNullOrEmpty()) {
            val isGranted = permissions.entries.all { it.value }
            permissions.entries.forEach {
                Log.i("DEBUG_PERMISSIONS", "${it.key} = ${it.value}")
            }
            if (isGranted) {
                println("Successful......")
                isPermissionGranted(true)
            } else {
                checkForRationaleDialog()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOnboardingBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        initObservers()
        binding.apply {
            btnGrant.setOnClickListener {
                checkForMultiplePermissions(getPermissionList())
            }
            btnScan.setOnClickListener {
                when (scanStatus) {
                    ScanningStatus.NOT_STARTED -> {
                        viewModel.scanForMusic()
                        scanStatus = ScanningStatus.STARTED
                    }

                    ScanningStatus.COMPLETED -> {
                        viewModel.setOnBoardingCompleted()
                        findNavController().navigate(R.id.action_onBoardingFragment_to_homeFragment)
                    }

                    else -> {
                        requireContext().showToast("Under Scanning")
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (getPermissionList().isEmpty()) {
                    showScanViews()
                } else {
                    showPermissionViews()
                }
            } else {
                showScanViews()
            }
        }
    }

    private fun showPermissionViews() {
        binding.apply {
            grpPermission.isVisible = true
            grpScanner.isVisible = false
        }
    }

    private fun showScanViews() {
        binding.apply {
            grpPermission.isVisible = false
            grpScanner.isVisible = true
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.scanStatus.collect {
                    when (it) {
                        ScanStatus.ScanComplete -> {
                            scanStatus = ScanningStatus.COMPLETED
                            binding.btnScan.text = "Finish"
                            binding.btnScan.isClickable = true
                            requireContext().showToast("Scan Completed")
                            launch(Dispatchers.IO) {
                                viewModel.migratePlaylists()
                            }
                        }

                        ScanStatus.ScanNotRunning -> {}
                        is ScanStatus.ScanProgress -> updateProgress(it.parsed, it.total)
                        ScanStatus.ScanStarted -> {
                            scanStatus = ScanningStatus.STARTED
                            binding.btnScan.text = "Scanning"
                            binding.btnScan.isClickable = false
                            requireContext().showToast("Scan Started")
                        }
                    }
                }
            }

        }
    }

    private fun updateProgress(parsed: Int, total: Int) {
        binding.apply {
            pbScan.max = total
            pbScan.progress = parsed
            val noOfSongs = resources.getQuantityString(R.plurals.numberOfSongs, total, total)
            tvSongCount.text = "$noOfSongs Found"
        }
    }

    private fun getPermissionList(): Array<String> {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE)
        }
        permissions.add(Manifest.permission.MODIFY_AUDIO_SETTINGS)
        permissions.add(Manifest.permission.WAKE_LOCK)

        val list = permissions.filter {
            ContextCompat.checkSelfPermission(
                requireContext(),
                it
            ) != PackageManager.PERMISSION_GRANTED
        }
        return list.toTypedArray()
    }

    private fun checkForMultiplePermissions(manifestPermissions: Array<String>) {
        for (permission in manifestPermissions) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                println("Permission Granted....")
                isDenied = false
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    permission
                )
            ) {
                isDenied = true
            } else {
                requestMultiplePermissionsLauncher.launch(manifestPermissions)
            }
        }
        if (isDenied) {
            isPermissionGranted(false)
            showPermissionRationaleDialog(true)
        }
    }

    private fun checkForRationaleDialog() {
        val permissions = getPermissionList()

        val showRationale = permissions.all {
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                it
            )
        }

        showPermissionRationaleDialog(showRationale)
    }

    private fun showPermissionRationaleDialog(showRationale: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(getString(R.string.permissionAlert))
            .setPositiveButton(getString(R.string.Grant)) { dialog, _ ->
                if (showRationale) {
                    requestMultiplePermissionsLauncher.launch(getPermissionList())
                    dialog.cancel()
                } else {
                    requireActivity().finish()
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts(
                            getString(R.string.packageName),
                            requireActivity().packageName,
                            null
                        )
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
            .setNegativeButton(getString(R.string.Cancel)) { dialog, _ ->
                dialog.cancel()
            }
        builder.setCancelable(false)
        builder.create().show()
    }
}