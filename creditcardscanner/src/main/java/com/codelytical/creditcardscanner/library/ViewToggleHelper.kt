package com.codelytical.creditcardscanner.library

import android.view.View

object ViewToggleHelper {

    var showConfirm: Boolean = false

    var showIsDone: Boolean = false

    fun toggleViewSupport(view: View) {
        view.visibility = if (showConfirm) View.VISIBLE else View.GONE
    }

}