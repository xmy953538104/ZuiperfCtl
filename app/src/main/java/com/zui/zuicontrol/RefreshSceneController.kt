package com.zui.zuicontrol

import android.content.Context
import android.provider.Settings

object RefreshSceneController {
    const val BASE_REFRESH_RATE = 120

    fun currentRate(context: Context): Int {
        Settings.System.getString(context.contentResolver, ZuiControlContract.KEY_ACTIVE_REFRESH)
            ?.toIntOrNull()
            ?.takeIf { it in ZuiControlContract.rates }
            ?.let { return it }
        val min = Settings.System.getString(context.contentResolver, "min_refresh_rate")
            ?.removeSuffix(".0")
            ?.toIntOrNull()
        val peak = Settings.System.getString(context.contentResolver, "peak_refresh_rate")
            ?.removeSuffix(".0")
            ?.toIntOrNull()
        return when {
            min != null && min == peak && min in ZuiControlContract.rates -> min
            peak in ZuiControlContract.rates -> peak!!
            else -> BASE_REFRESH_RATE
        }
    }

    fun isLocked(context: Context, rate: Int): Boolean {
        val resolver = context.contentResolver
        return Settings.System.getString(resolver, ZuiControlContract.KEY_ACTIVE_REFRESH) == rate.toString()
    }
}
