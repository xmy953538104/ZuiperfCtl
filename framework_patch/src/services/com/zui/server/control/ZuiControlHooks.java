package com.zui.server.control;

import com.android.server.wm.ActivityRecord;

public final class ZuiControlHooks {
    private ZuiControlHooks() {
    }

    public static void onFocusedAppChanged(ActivityRecord record, int displayId) {
        ZuiControlService service = ZuiControlService.getInstance();
        if (service != null) {
            service.onFocusedAppChanged(record, displayId);
        }
    }
}
