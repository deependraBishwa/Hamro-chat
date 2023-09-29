package com.deepdev.hamrochat.utils

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.deepdev.hamrochat.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MyBottomSheetDialog(view: View): BottomSheetDialogFragment() {

    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var rootView: View

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    init {
        rootView = view
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return bottomSheetDialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        val layout : LinearLayout = dialog!!.findViewById(R.id.parent_layout)
        assert(layout != null)
        layout.minimumHeight = 500
    }
}