package net.alienminds.ethnogram.data.repository.utils

import android.app.Activity

fun interface ActivityProvider{
    fun getActivity(): Activity
}