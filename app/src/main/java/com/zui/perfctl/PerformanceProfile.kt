package com.zui.perfctl

data class PerformanceProfile(
    val packageName: String,
    val mode: PerformanceMode,
    val littleMaxKHz: Int,
    val littleMinKHz: Int,
    val bigMaxKHz: Int,
    val bigMinKHz: Int,
    val titanMaxKHz: Int,
    val titanMinKHz: Int,
    val megaMaxKHz: Int,
    val megaMinKHz: Int,
    val gpuMaxKHz: Int,
    val gpuMinKHz: Int,
) {
    val key: String
        get() = "$packageName|${mode.id}"

    fun serialize(): String {
        return listOf(
            packageName,
            mode.id,
            littleMaxKHz,
            littleMinKHz,
            bigMaxKHz,
            bigMinKHz,
            titanMaxKHz,
            titanMinKHz,
            megaMaxKHz,
            megaMinKHz,
            gpuMaxKHz,
            gpuMinKHz,
        ).joinToString("|")
    }

    companion object {
        fun parse(line: String): PerformanceProfile? {
            val parts = line.trim().split("|")
            if (parts.size !in setOf(6, 12) || !PackageNames.isValid(parts[0])) {
                return null
            }
            val mode = PerformanceMode.fromId(parts[1]) ?: return null
            val profile = if (parts.size == 6) {
                val cpuMax = parts[2].toIntOrNull() ?: return null
                val cpuMin = parts[3].toIntOrNull() ?: return null
                val gpuMax = parts[4].toIntOrNull() ?: return null
                val gpuMin = parts[5].toIntOrNull() ?: return null
                PerformanceProfile(
                    parts[0],
                    mode,
                    cpuMax,
                    cpuMin,
                    cpuMax,
                    cpuMin,
                    cpuMax,
                    cpuMin,
                    cpuMax,
                    cpuMin,
                    gpuMax,
                    gpuMin,
                )
            } else {
                PerformanceProfile(
                    parts[0],
                    mode,
                    parts[2].toIntOrNull() ?: return null,
                    parts[3].toIntOrNull() ?: return null,
                    parts[4].toIntOrNull() ?: return null,
                    parts[5].toIntOrNull() ?: return null,
                    parts[6].toIntOrNull() ?: return null,
                    parts[7].toIntOrNull() ?: return null,
                    parts[8].toIntOrNull() ?: return null,
                    parts[9].toIntOrNull() ?: return null,
                    parts[10].toIntOrNull() ?: return null,
                    parts[11].toIntOrNull() ?: return null,
                )
            }
            return profile.takeIf { it.isValid() }
        }
    }

    private fun isValid(): Boolean =
        littleMinKHz > 0 && littleMaxKHz >= littleMinKHz &&
            bigMinKHz > 0 && bigMaxKHz >= bigMinKHz &&
            titanMinKHz > 0 && titanMaxKHz >= titanMinKHz &&
            megaMinKHz > 0 && megaMaxKHz >= megaMinKHz &&
            gpuMinKHz > 0 && gpuMaxKHz >= gpuMinKHz
}

object PackageNames {
    private val pattern = Regex("[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+")

    fun isValid(value: String): Boolean = pattern.matches(value)
}

enum class PerformanceMode(val id: String, val title: String) {
    BALANCED("balanced", "均衡"),
    POWER_SAVE("powersave", "省电"),
    SAVAGE("savage", "野兽");

    companion object {
        fun fromId(value: String): PerformanceMode? = entries.firstOrNull { it.id == value }
    }
}
