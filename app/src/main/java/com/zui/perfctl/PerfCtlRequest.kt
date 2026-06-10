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
        mode: String? = null,
        littleMax: Int? = null,
        littleMin: Int? = null,
        bigMax: Int? = null,
        bigMin: Int? = null,
        titanMax: Int? = null,
        titanMin: Int? = null,
        megaMax: Int? = null,
        megaMin: Int? = null,
        gpuMax: Int? = null,
        gpuMin: Int? = null,
    ) {
        val resolver = context.contentResolver
        val requestId = "${System.currentTimeMillis()}_${SystemClock.elapsedRealtimeNanos()}_$cmd"
        val requestText = listOf(
            requestId,
            cmd,
            rate?.toString().orEmpty(),
            pkg.orEmpty().replace("|", ""),
            mode.orEmpty().replace("|", ""),
            littleMax?.toString().orEmpty(),
            littleMin?.toString().orEmpty(),
            bigMax?.toString().orEmpty(),
            bigMin?.toString().orEmpty(),
            titanMax?.toString().orEmpty(),
            titanMin?.toString().orEmpty(),
            megaMax?.toString().orEmpty(),
            megaMin?.toString().orEmpty(),
            gpuMax?.toString().orEmpty(),
            gpuMin?.toString().orEmpty(),
        ).joinToString("|")
        Settings.System.putString(
            resolver,
            PerfCtlContract.KEY_REQUEST_TEXT,
            requestText,
        )
    }

}
