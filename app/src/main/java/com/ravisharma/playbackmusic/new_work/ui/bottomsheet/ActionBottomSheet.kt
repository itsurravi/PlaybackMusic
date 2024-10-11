package com.ravisharma.playbackmusic.new_work.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ActionBottomSheet(
    private val layoutResId: Int,
    private val onViewCreatedCallback: (View, Bundle?, BottomSheetDialogFragment) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout passed as a parameter
        return inflater.inflate(layoutResId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Call the custom onViewCreated callback with the view and saved state
        onViewCreatedCallback(view, savedInstanceState, this)
    }
}