ZuiperfCtl v6 payload

Goal:
- Keep official ZuiPP and ZuiGameHelper installed.
- Add our own privileged app: com.zui.zuiperfctl.
- Add root init daemon: /system/bin/zui_perfctld.
- Add embedded AsoulOpt service from the proven 187 payload.
- Runtime data lives under /data/local/tmp/zui_perfctl.
- XML/asopt runtime bind mounts are requested through zui_perfctl.* properties and performed by init.

Data layout after boot:
- /data/local/tmp/zui_perfctl/zuipp/game_policy.xml
- /data/local/tmp/zui_perfctl/zuipp/performanceconfig.xml
- /data/local/tmp/zui_perfctl/asoul/asopt.conf
- /data/local/tmp/zui_perfctl/refresh/rules.prop
- /data/local/tmp/zui_perfctl/profiles/apps.prop
- /data/local/tmp/zui_perfctl/perfctl/request.prop
- /data/local/tmp/zui_perfctl/perfctl/status.prop
- /data/local/tmp/zui_perfctl/log/perfctld.log

Request format:
  id=optional-number
  cmd=apply_zuipp
  package=optional.package.name
  rate=60|90|120|144
  refresh=0|1
  zuipp=0|1
  asoul=0|1

App request transport:
  zui_perfctl_request_text=id|cmd|rate|package|refresh|zuipp|asoul

The complete request is written in one Settings.System update. The legacy
multi-key Settings.System transport and request.prop remain supported for ADB
maintenance.

Supported commands:
- apply_zuipp
- restore_zuipp
- restart_zuipp
- apply_asoul
- restore_asoul
- restart_asoul
- set_refresh with rate=60, 90, 120, or 144
- restore_refresh
- set_refresh_rule with package=package.name and rate=60, 90, 120, or 144
- remove_refresh_rule with package=package.name
- set_app_profile with package=package.name, rate=60/90/120/144, refresh=0/1, zuipp=0/1, asoul=0/1
- remove_app_profile with package=package.name
- enable_auto_refresh
- disable_auto_refresh
- status

Important:
- XML changes are applied through bind mount. ZuiPP/GameHelper still need restart to reload cached XML.
- Automatic refresh switching is disabled by default. Add rules to /data/local/tmp/zui_perfctl/refresh/rules.prop and enable it explicitly.
- The APK is built from the root app module and should be copied to:
  system/priv-app/ZuiperfCtl/ZuiperfCtl.apk
