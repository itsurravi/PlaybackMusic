package com.ravisharma.playbackmusic.new_work.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.ScanStatus
import com.ravisharma.playbackmusic.databinding.ActivityNewPlayerBinding
import com.ravisharma.playbackmusic.new_work.viewmodel.MusicScanViewModel
import com.ravisharma.playbackmusic.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewPlayerActivity : AppCompatActivity() {

    private val binding: ActivityNewPlayerBinding by lazy {
        ActivityNewPlayerBinding.inflate(layoutInflater)
    }

    private val viewModel: MusicScanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navController =
            (supportFragmentManager.findFragmentById(R.id.nav_container) as NavHostFragment).navController
        val graphInflater = navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.nav_graph)

        val isOnBoardingCompleted = viewModel.isOnBoardingCompleted()

        val startDestination = if (isOnBoardingCompleted) {
            R.id.homeFragment
        } else {
            R.id.onBoardingFragment
        }

        navGraph.setStartDestination(startDestination)
        navController.graph = navGraph

        scannerObserver()
    }

    private fun scannerObserver() {
        lifecycleScope.launch {
            viewModel.scanStatus.collect {
                when (it) {
                    ScanStatus.ScanComplete -> showToast("Scan Completed")
                    ScanStatus.ScanStarted -> showToast("Scan Started")
                    else -> Unit
                }
            }
        }
    }
}