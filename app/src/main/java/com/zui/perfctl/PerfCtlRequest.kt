package com.zui.perfctl

import android.content.Context
import android.os.SystemClock
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
        val requestId = "${System.currentTimeMillis()}_${SystemClock.elapsedRealtimeNanos()}_$cmd"
        val requestText = listOf(
            requestId,
            cmd,
            rate?.toString().orEmpty(),
            pkg.orEmpty().replace("|", ""),
            refresh.toFlag(),
            zuipp.toFlag(),
            asoul.toFlag(),
        ).joinToString("|")
        Settings.System.putString(
            resolver,
            PerfCtlContract.KEY_REQUEST_TEXT,
            requestText,
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
