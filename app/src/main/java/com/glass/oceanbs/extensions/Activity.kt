package com.glass.oceanbs.extensions

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.glass.oceanbs.utils.JoblabDialog

inline fun Activity.alert(func: JoblabDialog.() -> Unit): AlertDialog =
    JoblabDialog(this).apply { func() }.create()