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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.ScanStatus
import com.ravisharma.playbackmusic.databinding.FragmentOnboardingBinding
import com.ravisharma.playbackmusic.new_work.viewmodel.MusicScanViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OnBoardingFragment : Fragment(R.layout.fragment_onboarding) {

    private val viewModel: MusicScanViewModel by viewModels()

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private var isScanCompleted: Boolean = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionToRequest33 = arrayOf(
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.READ_MEDIA_AUDIO,
    )

    private val permissionToRequest = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.MODIFY_AUDIO_SETTINGS
    )

    private var isDenied: Boolean = false

    private val isPermissionGranted: (Boolean) -> Unit = {
        Toast.makeText(requireContext(), "Permissions Granted", Toast.LENGTH_SHORT).show()
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
                if (isScanCompleted) {
                    // MOVE TO NEXT SCREEN
                    viewModel.setOnBoardingCompleted()
                } else {
                    viewModel.scanForMusic()
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
            viewModel.scanStatus.collect {
                when (it) {
                    ScanStatus.ScanComplete -> {
                        Log.i("ScanStatus", "ScanComplete")
                        isScanCompleted = true
                        binding.btnScan.text = "Finish"
                    }

                    ScanStatus.ScanNotRunning -> {
                        Log.i("ScanStatus", "ScanNotRunning")
                    }

                    is ScanStatus.ScanProgress -> {
                        updateProgress(it.parsed, it.total)
                        Log.i("ScanStatus", "ScanProgress ${it.parsed} ${it.total}")
                    }

                    ScanStatus.ScanStarted -> {
                        Log.i("ScanStatus", "ScanStarted")
                    }
                }
            }
        }
    }

    private fun updateProgress(parsed: Int, total: Int) {
        binding.apply {
            pbScan.max = total
            pbScan.progress = parsed
        }
    }

    private fun getPermissionList(): Array<String> {
        val strings = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionToRequest33
        } else {
            permissionToRequest
        }
        val list = strings.filter {
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