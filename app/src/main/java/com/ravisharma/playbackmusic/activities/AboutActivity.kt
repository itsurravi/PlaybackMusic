package com.ravisharma.playbackmusic.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ravisharma.playbackmusic.BuildConfig
import com.ravisharma.playbackmusic.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val appVer = BuildConfig.VERSION_NAME
        binding.appVersion.text = "Ver: $appVer"
    }

    fun finishPage(view: View?) {
        finish()
    }
}