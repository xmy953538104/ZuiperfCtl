package com.zui.perfctl

data class AppProfile(
    val packageName: String,
    val rate: Int = 120,
    val refreshEnabled: Boolean = true,
    val zuippEnabled: Boolean = false,
    val asoulEnabled: Boolean = false,
) {
    val enabled: Boolean
        get() = refreshEnabled || zuippEnabled || asoulEnabled

    fun serialize(): String {
        return listOf(
            packageName,
            rate.toString(),
            refreshEnabled.toBit(),
            zuippEnabled.toBit(),
            asoulEnabled.toBit(),
        ).joinToString("|")
    }

    companion object {
        fun parse(line: String): AppProfile? {
            val parts = line.trim().split("|")
            if (parts.size < 5) {
                return null
            }
            val pkg = parts[0]
            if (!isValidPackageName(pkg)) {
                return null
            }
            val rate = parts[1].toIntOrNull()?.takeIf { it in PerfCtlContract.rates } ?: 120
            return AppProfile(
                packageName = pkg,
                rate = rate,
                refreshEnabled = parts[2] == "1",
                zuippEnabled = parts[3] == "1",
                asoulEnabled = parts[4] == "1",
            )
        }

        fun isValidPackageName(value: String): Boolean {
            if (value.isBlank() || value.startsWith(".") || value.endsWith(".") || value.contains("..")) {
                return false
            }
            return value.all { it.isLetterOrDigit() || it == '_' || it == '.' }
        }

        private fun Boolean.toBit(): String = if (this) "1" else "0"
    }
}
