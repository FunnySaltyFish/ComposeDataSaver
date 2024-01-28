package com.funny.data_saver.extensions

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun Context.toastOnUI(msg: String) = withContext(
     Dispatchers.Main.immediate
) {
     Toast.makeText(this@toastOnUI, msg, Toast.LENGTH_SHORT).show()
}
