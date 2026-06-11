ZuiperfCtl v18 payload

System components:
- /system/priv-app/ZuiperfCtl/ZuiperfCtl.apk
- /system/bin/zui_perfctld
- /system/bin/AsoulOpt
- /system/etc/init/zui_perfctld.rc
- /system/etc/init/zui_asoulopt.rc

Runtime data:
- /data/local/tmp/zui_perfctl/refresh/rules.prop
- /data/local/tmp/zui_perfctl/performance/profiles.prop
- /data/local/tmp/zui_perfctl/zuipp/game_policy.xml
- /data/local/tmp/zui_perfctl/zuipp/performanceconfig.xml
- /data/local/tmp/zui_perfctl/log/perfctld.log
- /data/local/tmp/zui_perfctl/log/asoulopt.log

App request channel:
- Settings.System key: zui_perfctl_request_text
- Atomic format:
  id|cmd|rate|package|mode|little_max_khz|little_min_khz|big_max_khz|big_min_khz|titan_max_khz|titan_min_khz|mega_max_khz|mega_min_khz|gpu_max_khz|gpu_min_khz
- Accessibility scene channel:
  zui_perfctl_scene_event_text = elapsedRealtimeNanos|foreground.package

User-facing commands:
- status
- learn_refresh
- restore_refresh
- remove_refresh_rule
- set_performance_profile
- remove_performance_profile
- apply_performance
- restore_zuipp
- export_logs

Maintenance commands kept for ADB diagnostics:
- apply_zuipp
- restart_zuipp
- apply_asoul
- restore_asoul
- restart_asoul

Behavior:
- Refresh-rate baseline is a hard 120Hz lock.
- The app includes a minimal accessibility service for foreground-scene events.
  It does not retrieve window content or perform gestures; it only receives the
  foreground package name and immediately applies that scene's refresh rule.
- The root daemon consumes the accessibility scene channel for GPU/profile
  enforcement. It does not poll dumpsys for foreground-app detection.
- Notification clicks learn and lock the current reliable scene, including the
  launcher. Unconfigured scenes simply fall back to the 120Hz baseline.
- The daemon validates accessibility scene events with a 60-second uptime TTL
  before using them as fresh foreground events, then falls back to the last
  known top package for long-running foreground apps.
- On first boot, the daemon patches SafeCenter BlackWhite.db once by adding
  ZuiperfCtl to AccelerateList with the same AES package encoding used by
  ZUI Security. It retries only until success or a bounded attempt limit, then
  stops.
- The image patcher also adds ZuiperfCtl to ZUI memory-cleaner, power-policy,
  background-intent, OverHeat, and DeviceIdle keepalive whitelists.
- The daemon re-checks peak_refresh_rate and min_refresh_rate even when the
  foreground scene has not changed, so ZUI drift is locked back to the learned
  scene value.
- SystemUI and ZuiperfCtl are treated as transient overlays. Notification clicks
  learn the most recent real foreground scene, so pulling the shade over a game
  records the game, not SystemUI or ZuiperfCtl.
- Choosing 120Hz removes the current scene's exception rule.
- If no scene target can be resolved, the click is ignored instead of creating a
  global manual override.
- Performance profiles are converted into ZuiPP XML by XmlProfileGenerator.
- CPU/GPU level IDs use 1-based lowIndex*100+highIndex inside each hardware type.
- Saving or deleting a performance profile regenerates the working XML files.
- Applying performance regenerates both XML files, bind mounts them through init,
  and restarts ZuiPP/game helper packages.
- If ZuiPP does not enforce a foreground profile's GPU range, the daemon also
  applies the same range to KGSL pwrlevel nodes and publishes the live GPU state.
- Leaving a profiled app restores both KGSL pwrlevel and devfreq min/max limits.
- AsoulOpt is not configured per app. The UI only reports service health and
  exports its log.
