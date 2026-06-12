package android.os;

public final class SystemProperties {
    private SystemProperties() {
    }

    public static boolean getBoolean(String key, boolean def) {
        return def;
    }
}
