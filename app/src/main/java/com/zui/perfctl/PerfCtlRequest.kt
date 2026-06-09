package com.zui.perfctl

import android.content.Context
import android.provider.Settings

object PerfCtlRequest {
    fun send(
        context: Context,
        cmd: String,
        rate: Int? = null,
        pkg: String? = null,
        refresh: Boolean? = null,
        zuipp: Boolean? = null,
        asoul: Boolean? = null,
    ) {
        val resolver = context.contentResolver
        Settings.System.putString(resolver, PerfCtlContract.KEY_CMD, cmd)
        Settings.System.putString(resolver, PerfCtlContract.KEY_RATE, rate?.toString().orEmpty())
        Settings.System.putString(resolver, PerfCtlContract.KEY_PACKAGE, pkg.orEmpty())
        Settings.System.putString(resolver, PerfCtlContract.KEY_PROFILE_REFRESH, refresh.toFlag())
        Settings.System.putString(resolver, PerfCtlContract.KEY_PROFILE_ZUIPP, zuipp.toFlag())
        Settings.System.putString(resolver, PerfCtlContract.KEY_PROFILE_ASOUL, asoul.toFlag())
        Settings.System.putString(
            resolver,
            PerfCtlContract.KEY_REQUEST_ID,
            "${System.currentTimeMillis()}_$cmd",
        )
    }

    private fun Boolean?.toFlag(): String {
        return when (this) {
            true -> "1"
            false -> "0"
            null -> ""
        }
    }
}
