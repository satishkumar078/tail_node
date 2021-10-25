package com.tailnode.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tailnode.R
import com.tailnode.databinding.DialogToastBinding

class ToastDialog(
    context: Context,
    msg: String,
    onOkListener: OnOkListener? = null) {
    private var dialog: Dialog? = null

    init {
        dialog = Dialog(context, R.style.Theme_App_Dialog_FullScreen)
        val binding = DialogToastBinding.inflate(LayoutInflater.from(context))
        dialog?.setContentView(binding.root)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        binding.tvTitle.text = msg

        binding.btnOk.setOnClickListener {
            dismiss()
            onOkListener?.onOkListener()
        }
    }

    private fun dismiss() {
        dialog?.dismiss()
    }

    fun show() {
        dialog?.show()
    }

    interface OnOkListener {
        fun onOkListener()
    }
}