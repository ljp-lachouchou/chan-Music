package com.software.mymusicplayer.view

import android.app.Dialog
import android.content.Context
import androidx.core.app.NotificationCompat.Style
import com.software.mymusicplayer.R

class CustomDialog @JvmOverloads
constructor(context: Context,style: Int = R.style.CustomDialogStyle)
    : Dialog(context,style){
        init {
            setContentView(R.layout.dialog_layout)
        }
}