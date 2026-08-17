package tech.alomessi.vitra;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import tech.alomessi.vitra.widget.ClockWidgetProvider;
import tech.alomessi.vitra.widget.DateWidgetProvider;
import tech.alomessi.vitra.widget.PrayerWidgetProvider;
import tech.alomessi.vitra.widget.SearchWidgetProvider;
import tech.alomessi.vitra.widget.SystemWidgetProvider;
import tech.alomessi.vitra.widget.WeatherWidgetProvider;

/** The launcher-facing configuration activity for every Vitra AppWidget. */
@SuppressLint("SetTextI18n")
public class WidgetConfigActivity extends Activity {
    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private SharedPreferences prefs;
    private WidgetData selected;
    private WidgetPreviewView preview;
    private boolean arabic, showDate;
    private int accent, opacity, darkness, blur, radius;
    private String background;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setResult(RESULT_CANCELED);
        widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return; }
        prefs = getSharedPreferences("vitra", MODE_PRIVATE);
        selected = WidgetData.find(prefs.getString("pending_widget_style", "clock-classic"));
        arabic = prefs.getBoolean("widget_arabic_" + widgetId, prefs.getBoolean("arabic", true));
        showDate = prefs.getBoolean("widget_show_date_" + widgetId, prefs.getBoolean("last_show_date", true));
        accent = prefs.getInt("widget_accent_" + widgetId, prefs.getInt("accent", VitraUi.PURPLE));
        opacity = prefs.getInt("widget_opacity_" + widgetId, prefs.getInt("opacity_int", 76));
        darkness = prefs.getInt("widget_darkness_" + widgetId, prefs.getInt("darkness_int", 72));
        blur = prefs.getInt("widget_blur_" + widgetId, prefs.getInt("blur_int", 18));
        radius = prefs.getInt("widget_radius_" + widgetId, prefs.getInt("corner_int", 26));
        background = prefs.getString("widget_background_" + widgetId, prefs.getString("last_background", "Clear"));
        build();
    }

    private void build() {
        getWindow().setStatusBarColor(VitraUi.BG); getWindow().setNavigationBarColor(VitraUi.BG);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setClipToPadding(false);
        LinearLayout root = VitraUi.column(this); root.setPadding(d(18), d(16), d(18), d(28)); root.setBackgroundColor(VitraUi.BG); root.setLayoutDirection(arabic ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        LinearLayout header = VitraUi.row(this); header.setGravity(Gravity.CENTER_VERTICAL);
        Button close = VitraUi.circleButton(this, "×"); close.setTextSize(30); close.setOnClickListener(v -> finish()); header.addView(close, new LinearLayout.LayoutParams(d(48), d(48)));
        TextView title = VitraUi.text(this, arabic ? "إعداد الويدجت" : "Widget setup", 20, VitraUi.TEXT, true); title.setGravity(Gravity.CENTER); header.addView(title, new LinearLayout.LayoutParams(0, d(52), 1));
        TextView kind = VitraUi.text(this, selected.title(arabic), 11, VitraUi.PURPLE, true); kind.setGravity(Gravity.CENTER); header.addView(kind, new LinearLayout.LayoutParams(d(82), d(48))); root.addView(header, fill(d(56), d(12)));

        FrameLayout art = new FrameLayout(this); art.setBackground(VitraUi.glass(this, 28, .98f, 0xff606066));
        preview = new WidgetPreviewView(this, selected, arabic, accent); preview.setAppearance(accent, opacity, darkness, blur, radius, background); preview.setShowDate(showDate); art.addView(preview, new FrameLayout.LayoutParams(-1, -1)); root.addView(art, fill(d(242), d(17)));

        root.addView(section(arabic ? "الخلفية" : "BACKGROUND"), fill(d(24), d(6)));
        addBackgroundChoices(root);
        root.addView(slider(arabic ? "الشفافية" : "Opacity", 25, 100, opacity, value -> { opacity = value; updatePreview(); }), fill(d(58), d(5)));
        root.addView(slider(arabic ? "عتامة الزجاج" : "Glass darkness", 0, 100, darkness, value -> { darkness = value; updatePreview(); }), fill(d(58), d(5)));
        root.addView(slider(arabic ? "التمويه" : "Blur", 0, 40, blur, value -> { blur = value; updatePreview(); }), fill(d(58), d(5)));
        root.addView(slider(arabic ? "دائرية الحواف" : "Corner radius", 8, 48, radius, value -> { radius = value; updatePreview(); }), fill(d(58), d(14)));

        root.addView(section(arabic ? "لون الإضاءة" : "HIGHLIGHT COLOR"), fill(d(24), d(6)));
        LinearLayout palette = VitraUi.row(this); int[] colors = {Color.WHITE, VitraUi.PURPLE, VitraUi.BLUE, 0xff7cd69a, 0xffff7fa8, VitraUi.GOLD};
        for (int color : colors) {
            Button dot = VitraUi.circleButton(this, color == accent ? "✓" : ""); dot.setTextColor(color == Color.WHITE ? Color.BLACK : Color.WHITE); dot.setBackground(VitraUi.circle(this, color, color == accent));
            dot.setOnClickListener(v -> { accent = color; updatePreview(); build(); }); palette.addView(dot, weighted(1, d(44), d(5)));
        }
        root.addView(palette, fill(d(44), d(16)));

        root.addView(section(arabic ? "المحتوى" : "CONTENT"), fill(d(24), d(6)));
        Switch date = new Switch(this); date.setChecked(showDate); date.setOnCheckedChangeListener((button, checked) -> { showDate = checked; updatePreview(); });
        root.addView(preference(arabic ? "إظهار التاريخ" : "Show date", arabic ? "إظهار تاريخ اليوم داخل التصميم" : "Show today’s date in the design", date), fill(d(76), d(9)));
        Button language = VitraUi.secondary(this, arabic ? "اللغة والأرقام" : "Language & numerals"); language.setOnClickListener(v -> showLanguageDialog()); root.addView(language, fill(d(48), d(10)));
        Button studio = VitraUi.secondary(this, arabic ? "فتح استوديو Vitra الكامل" : "Open full Vitra studio"); studio.setOnClickListener(v -> { Intent intent = new Intent(this, MainActivity.class); intent.putExtra(MainActivity.EXTRA_OPEN_WIDGET, selected.id); startActivity(intent); }); root.addView(studio, fill(d(48), d(18)));

        Button save = VitraUi.primary(this, arabic ? "حفظ وإضافة الويدجت" : "Save & add widget"); save.setOnClickListener(v -> save()); root.addView(save, fill(d(57), 0));
        VitraUi.applyInsets(scroll); setContentView(scroll);
    }

    private void addBackgroundChoices(LinearLayout root) {
        LinearLayout row = VitraUi.row(this); String[] values = {"Clear", "Fill", "Gradient", "Image"};
        for (String value : values) {
            Button choice = VitraUi.chip(this, localizedBackground(value), value.equals(background));
            choice.setOnClickListener(v -> { background = value; updatePreview(); build(); });
            row.addView(choice, weighted(1, d(40), d(5)));
        }
        root.addView(row, fill(d(40), d(16)));
    }

    private void updatePreview() { if (preview != null) { preview.setArabic(arabic); preview.setAppearance(accent, opacity, darkness, blur, radius, background); preview.setShowDate(showDate); } }

    private void showLanguageDialog() {
        String[] choices = {"العربية", "English"};
        new AlertDialog.Builder(this).setTitle(arabic ? "لغة الويدجت" : "Widget language").setSingleChoiceItems(choices, arabic ? 0 : 1, (dialog, which) -> { arabic = which == 0; dialog.dismiss(); build(); }).setNegativeButton(arabic ? "إلغاء" : "Cancel", null).show();
    }

    private void save() {
        prefs.edit().putString("widget_style_" + widgetId, selected.id).putBoolean("widget_arabic_" + widgetId, arabic).putBoolean("widget_show_date_" + widgetId, showDate).putInt("widget_accent_" + widgetId, accent).putInt("widget_opacity_" + widgetId, opacity).putInt("widget_darkness_" + widgetId, darkness).putInt("widget_blur_" + widgetId, blur).putInt("widget_radius_" + widgetId, radius).putString("widget_background_" + widgetId, background).apply();
        refreshProvider();
        Intent result = new Intent(); result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId); setResult(RESULT_OK, result); finish();
    }

    private void refreshProvider() {
        AppWidgetManager manager = AppWidgetManager.getInstance(this); android.appwidget.AppWidgetProviderInfo info = manager.getAppWidgetInfo(widgetId); if (info == null || info.provider == null) return;
        ComponentName provider = info.provider; int[] ids = {widgetId};
        if (provider.equals(new ComponentName(this, ClockWidgetProvider.class))) new ClockWidgetProvider().onUpdate(this, manager, ids);
        else if (provider.equals(new ComponentName(this, DateWidgetProvider.class))) new DateWidgetProvider().onUpdate(this, manager, ids);
        else if (provider.equals(new ComponentName(this, WeatherWidgetProvider.class))) new WeatherWidgetProvider().onUpdate(this, manager, ids);
        else if (provider.equals(new ComponentName(this, PrayerWidgetProvider.class))) new PrayerWidgetProvider().onUpdate(this, manager, ids);
        else if (provider.equals(new ComponentName(this, SearchWidgetProvider.class))) new SearchWidgetProvider().onUpdate(this, manager, ids);
        else if (provider.equals(new ComponentName(this, SystemWidgetProvider.class))) new SystemWidgetProvider().onUpdate(this, manager, ids);
    }

    private LinearLayout preference(String title, String detail, View control) { LinearLayout row = VitraUi.row(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(d(14), 0, d(8), 0); row.setBackground(VitraUi.glass(this, 20, .92f, VitraUi.BORDER)); LinearLayout labels = VitraUi.column(this); labels.addView(VitraUi.text(this, title, 16, VitraUi.TEXT, true), fill(d(29), d(1))); labels.addView(VitraUi.text(this, detail, 11, VitraUi.MUTED, false), fill(d(20), 0)); row.addView(labels, new LinearLayout.LayoutParams(0, -1, 1)); row.addView(control, new LinearLayout.LayoutParams(d(62), -2)); return row; }
    private LinearLayout slider(String label, int min, int max, int current, NumberChanged changed) { LinearLayout block = VitraUi.column(this); LinearLayout head = VitraUi.row(this); TextView name = VitraUi.text(this, label, 14, VitraUi.TEXT, true); TextView number = VitraUi.text(this, String.valueOf(current), 13, VitraUi.MUTED, true); number.setGravity(Gravity.END | Gravity.CENTER_VERTICAL); head.addView(name, new LinearLayout.LayoutParams(0, d(24), 1)); head.addView(number, new LinearLayout.LayoutParams(d(44), d(24))); SeekBar bar = new SeekBar(this); bar.setMax(max - min); bar.setProgress(current - min); bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { public void onProgressChanged(SeekBar seek, int progress, boolean fromUser) { int v = progress + min; number.setText(String.valueOf(v)); changed.set(v); } public void onStartTrackingTouch(SeekBar seek) {} public void onStopTrackingTouch(SeekBar seek) {} }); block.addView(head); block.addView(bar, fill(d(34), 0)); return block; }
    private interface NumberChanged { void set(int value); }
    private TextView section(String title) { TextView view = VitraUi.text(this, title, 11, VitraUi.PURPLE, true); view.setLetterSpacing(.13f); return view; }
    private String localizedBackground(String value) { if (!arabic) return value; if ("Clear".equals(value)) return "شفاف"; if ("Fill".equals(value)) return "ممتلئ"; if ("Gradient".equals(value)) return "تدرج"; return "صورة"; }
    private int d(float value) { return VitraUi.dp(this, value); }
    private LinearLayout.LayoutParams fill(int height, int bottom) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, height); p.setMargins(0, 0, 0, bottom); return p; }
    private LinearLayout.LayoutParams weighted(float weight, int height, int end) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, height, weight); p.setMargins(0, 0, end, 0); return p; }
}
