package com.zui.perfctl;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

public final class RefreshRateBridgeProvider extends ContentProvider {
    private static final String TAG = "ZuiperfCtlBridge";
    private static final float DEFAULT_RATE = 120.0f;
    private static final String KEY_PROVIDER_LAST_RATE = "zui_perfctl_provider_last_rate";
    private static final String KEY_PROVIDER_LAST_CALL = "zui_perfctl_provider_last_call";
    private static final String KEY_PROVIDER_LAST_URI = "zui_perfctl_provider_last_uri";
    private static final float[] SUPPORTED = new float[] {165.0f, 144.0f, 120.0f, 90.0f, 60.0f};

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String mode = selection == null ? "" : selection.trim();
        if ("supported".equals(mode)) {
            return supportedCursor();
        }
        if ("current".equals(mode)) {
            MatrixCursor cursor = new MatrixCursor(new String[] {"current"});
            cursor.addRow(new Object[] {currentRate()});
            remember("query_current", currentRate(), uri);
            return cursor;
        }

        float current = currentRate();
        MatrixCursor cursor = new MatrixCursor(new String[] {"current", "min", "max", "peak"});
        cursor.addRow(new Object[] {current, 0.0f, highestSupported(), current});
        remember("query_default", current, uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values == null || getContext() == null) {
            return 0;
        }
        Float rate = firstRate(values, "UGame");
        if (rate == null) {
            rate = firstRate(values, "appvote");
        }
        if (rate == null) {
            rate = firstAnyRate(values);
        }
        if (rate == null || rate <= 0.0f) {
            remember("update_ignored", 0.0f, uri);
            return 1;
        }

        float normalized = normalizeRate(rate);
        Settings.System.putFloat(getContext().getContentResolver(), "peak_refresh_rate", normalized);
        remember("update", normalized, uri);
        Log.i(TAG, "Mapped legacy refresh update to peak_refresh_rate=" + normalized);
        return 1;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        update(uri, values, null, null);
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        remember("delete", currentRate(), uri);
        return 1;
    }

    private MatrixCursor supportedCursor() {
        MatrixCursor cursor = new MatrixCursor(new String[] {"165.0", "144.0", "120.0", "90.0", "60.0"});
        cursor.addRow(new Object[] {165.0f, 144.0f, 120.0f, 90.0f, 60.0f});
        remember("query_supported", currentRate(), null);
        return cursor;
    }

    private float currentRate() {
        if (getContext() == null) {
            return DEFAULT_RATE;
        }
        try {
            return Settings.System.getFloat(getContext().getContentResolver(), "peak_refresh_rate");
        } catch (Settings.SettingNotFoundException ignored) {
            return DEFAULT_RATE;
        }
    }

    private float highestSupported() {
        return SUPPORTED[0];
    }

    private Float firstRate(ContentValues values, String key) {
        Object value = values.get(key);
        if (value == null) {
            return null;
        }
        return parseRate(String.valueOf(value));
    }

    private Float firstAnyRate(ContentValues values) {
        for (String key : values.keySet()) {
            Float rate = firstRate(values, key);
            if (rate != null) {
                return rate;
            }
        }
        return null;
    }

    private Float parseRate(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return Float.parseFloat(raw.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private float normalizeRate(float rate) {
        float best = SUPPORTED[0];
        float bestDistance = Math.abs(rate - best);
        for (float supported : SUPPORTED) {
            float distance = Math.abs(rate - supported);
            if (distance < bestDistance) {
                best = supported;
                bestDistance = distance;
            }
        }
        return best;
    }

    private void remember(String call, float rate, Uri uri) {
        if (getContext() == null) {
            return;
        }
        Settings.System.putString(getContext().getContentResolver(), KEY_PROVIDER_LAST_CALL, call);
        Settings.System.putString(getContext().getContentResolver(), KEY_PROVIDER_LAST_RATE, Float.toString(rate));
        if (uri != null) {
            Settings.System.putString(getContext().getContentResolver(), KEY_PROVIDER_LAST_URI, uri.toString());
        }
    }
}
