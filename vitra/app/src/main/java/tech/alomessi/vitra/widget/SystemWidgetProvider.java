package tech.alomessi.vitra.widget;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;

public class SystemWidgetProvider extends BaseInfoWidgetProvider {
    private int battery(Context c) {
        BatteryManager manager = (BatteryManager)c.getSystemService(Context.BATTERY_SERVICE);
        return manager == null ? -1 : manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }
    protected String eyebrow(Context c) { return "SYSTEM PULSE"; }
    protected String primary(Context c) { int b=battery(c); return b<0 ? "—%" : b+"%"; }
    protected String secondary(Context c) {
        StatFs fs = new StatFs(Environment.getDataDirectory().getPath());
        long free = fs.getAvailableBytes() / (1024L*1024L*1024L);
        return free + " GB free · tap for diagnostics";
    }
}
