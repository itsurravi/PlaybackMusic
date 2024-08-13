package com.ravisharma.playbackmusic.new_work.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.databinding.ActivityNewPlayerBinding
import com.ravisharma.playbackmusic.new_work.viewmodel.MusicScanViewModel
import dagger.hilt.android.AndroidEntryPoint

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
    }
}