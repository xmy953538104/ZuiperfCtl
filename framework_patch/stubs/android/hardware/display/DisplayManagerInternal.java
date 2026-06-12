package android.hardware.display;

public abstract class DisplayManagerInternal {
    public abstract void setDisplayProperties(int displayId, boolean hasContent,
            float requestedRefreshRate, int requestedModeId, float requestedMinRefreshRate,
            float requestedMaxRefreshRate, boolean requestedMinimalPostProcessing,
            boolean disableHdrConversion, boolean inTraversal);
}
