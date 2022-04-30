package com.example.heymama.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.heymama.R

class MoodDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(context)
        dialog.setView(layoutInflater.inflate(R.layout.dialog_mood,null))

        dialog.create()
        return dialog.show()
        //return super.onCreateDialog(savedInstanceState)
    }
}