package com.zui.perfctl

object PerfCtlContract {
    const val CMD_APPLY_ZUIPP = "apply_zuipp"
    const val CMD_RESTORE_ZUIPP = "restore_zuipp"
    const val CMD_RESTART_ASOUL = "restart_asoul"
    const val CMD_SET_REFRESH = "set_refresh"
    const val CMD_RESTORE_REFRESH = "restore_refresh"
    const val CMD_ENABLE_AUTO_REFRESH = "enable_auto_refresh"
    const val CMD_DISABLE_AUTO_REFRESH = "disable_auto_refresh"
    const val CMD_STATUS = "status"
    const val CMD_SET_APP_PROFILE = "set_app_profile"
    const val CMD_REMOVE_APP_PROFILE = "remove_app_profile"

    const val KEY_REQUEST_TEXT = "zui_perfctl_request_text"
    const val KEY_REQUEST_ID = "zui_perfctl_request_id"
    const val KEY_CMD = "zui_perfctl_cmd"
    const val KEY_RATE = "zui_perfctl_rate"
    const val KEY_PACKAGE = "zui_perfctl_package"
    const val KEY_PROFILE_REFRESH = "zui_perfctl_profile_refresh"
    const val KEY_PROFILE_ZUIPP = "zui_perfctl_profile_zuipp"
    const val KEY_PROFILE_ASOUL = "zui_perfctl_profile_asoul"

    const val KEY_STATUS_TIME = "zui_perfctl_status_time"
    const val KEY_STATUS_LAST = "zui_perfctl_status_last"
    const val KEY_STATUS_TEXT = "zui_perfctl_status_text"
    const val KEY_RULES_TEXT = "zui_perfctl_refresh_rules_text"
    const val KEY_PROFILES_TEXT = "zui_perfctl_profiles_text"
    const val KEY_AUTO_REFRESH = "zui_perfctl_auto_refresh"

    const val ACTION_REFRESH_NOTIFICATION = "com.zui.zuiperfctl.action.REFRESH_NOTIFICATION"
    const val ACTION_SET_60 = "com.zui.zuiperfctl.action.SET_60"
    const val ACTION_SET_90 = "com.zui.zuiperfctl.action.SET_90"
    const val ACTION_SET_120 = "com.zui.zuiperfctl.action.SET_120"
    const val ACTION_SET_144 = "com.zui.zuiperfctl.action.SET_144"
    const val ACTION_RESTORE = "com.zui.zuiperfctl.action.RESTORE"
    const val ACTION_AUTO_ON = "com.zui.zuiperfctl.action.AUTO_ON"
    const val ACTION_AUTO_OFF = "com.zui.zuiperfctl.action.AUTO_OFF"

    val rates = listOf(60, 90, 120, 144)
}
