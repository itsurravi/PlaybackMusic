package com.ravisharma.playbackmusic.new_work.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ravisharma.playbackmusic.data.db.model.ScanStatus
import com.ravisharma.playbackmusic.databinding.ActivityNewPlayerBinding
import com.ravisharma.playbackmusic.new_work.viewmodel.MusicScanViewModel
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

        initObservers()

        viewModel.scanForMusic()
    }

    private fun initObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.scanStatus.collect {
                        when (it) {
                            ScanStatus.ScanComplete -> {
                                Log.i("ScanStatus", "ScanComplete")
                            }

                            ScanStatus.ScanNotRunning -> {
                                Log.i("ScanStatus", "ScanNotRunning")
                            }

                            is ScanStatus.ScanProgress -> {
                                Log.i("ScanStatus", "ScanProgress ${it.parsed} ${it.total}")
                            }

                            ScanStatus.ScanStarted -> {
                                Log.i("ScanStatus", "ScanStarted")
                            }
                        }
                    }
                }
            }
        }
    }
}