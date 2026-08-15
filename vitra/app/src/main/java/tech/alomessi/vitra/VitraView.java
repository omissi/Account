package tech.alomessi.vitra;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import tech.alomessi.vitra.widget.ClockWidgetProvider;
import tech.alomessi.vitra.widget.DateWidgetProvider;
import tech.alomessi.vitra.widget.PrayerWidgetProvider;
import tech.alomessi.vitra.widget.SearchWidgetProvider;
import tech.alomessi.vitra.widget.SystemWidgetProvider;
import tech.alomessi.vitra.widget.WeatherWidgetProvider;

public class VitraView extends View {
    private enum Screen { HOME, WALLPAPERS, FAVORITES, SETTINGS, DETAIL, EDITOR }

    private static final int BG = Color.rgb(7, 9, 15);
    private static final int SURFACE = Color.rgb(17, 22, 35);
    private static final int SURFACE_2 = Color.rgb(25, 30, 46);
    private static final int TEXT = Color.rgb(244, 247, 255);
    private static final int MUTED = Color.rgb(158, 171, 195);
    private static final int CYAN = Color.rgb(48, 231, 255);
    private static final int VIOLET = Color.rgb(157, 92, 255);
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final SharedPreferences prefs;
    private final ArrayList<Hit> hits = new ArrayList<>();
    private final Set<String> favorites;
    private final Bitmap logo;
    private Screen screen = Screen.HOME;
    private WidgetData selected = WidgetData.ALL.get(0);
    private boolean arabic;
    private String category = "All";
    private float scrollY = 0f;
    private float contentHeight = 1400f;
    private float downY;
    private float lastY;
    private boolean dragging;
    private int accent = CYAN;
    private float glassOpacity = .78f;
    private float corner = 28f;

    public VitraView(Context context, SharedPreferences preferences) {
        super(context);
        prefs = preferences;
        arabic = prefs.getBoolean("arabic", Locale.getDefault().getLanguage().equals("ar"));
        favorites = new HashSet<>(prefs.getStringSet("favorites", new HashSet<>()));
        accent = prefs.getInt("accent", CYAN);
        glassOpacity = prefs.getFloat("opacity", .78f);
        corner = prefs.getFloat("corner", 28f);
        logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(dp(1));
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setFocusable(true);
        setContentDescription("Vitra widget studio");
    }

    public void setArabic(boolean value) {
        arabic = value;
        prefs.edit().putBoolean("arabic", value).apply();
        invalidate();
    }

    public boolean navigateBack() {
        if (screen == Screen.EDITOR) { screen = Screen.DETAIL; resetScroll(); return true; }
        if (screen == Screen.DETAIL) { screen = Screen.HOME; resetScroll(); return true; }
        if (screen != Screen.HOME) { screen = Screen.HOME; resetScroll(); return true; }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        hits.clear();
        drawBackdrop(canvas);
        canvas.save();
        canvas.translate(0, -scrollY);
        switch (screen) {
            case HOME: drawHome(canvas); break;
            case WALLPAPERS: drawWallpapers(canvas); break;
            case FAVORITES: drawFavorites(canvas); break;
            case SETTINGS: drawSettings(canvas); break;
            case DETAIL: drawDetail(canvas); break;
            case EDITOR: drawEditor(canvas); break;
        }
        canvas.restore();
        if (screen != Screen.DETAIL && screen != Screen.EDITOR) drawBottomNav(canvas);
    }

    private void drawBackdrop(Canvas c) {
        paint.setShader(new LinearGradient(0, 0, getWidth(), getHeight(),
            new int[]{Color.rgb(7, 9, 15), Color.rgb(12, 12, 27), Color.rgb(8, 18, 26)},
            null, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, getWidth(), getHeight(), paint);
        paint.setShader(null);
        paint.setColor(withAlpha(CYAN, 24));
        c.drawCircle(getWidth() * .86f, dp(115), dp(150), paint);
        paint.setColor(withAlpha(VIOLET, 18));
        c.drawCircle(dp(20), getHeight() * .56f, dp(180), paint);
    }

    private void drawHeader(Canvas c, String title, boolean back) {
        float top = dp(18);
        if (back) {
            drawCircleButton(c, dp(30), top + dp(26), "‹", "back");
        } else if (logo != null) {
            c.drawBitmap(logo, null, new RectF(dp(18), top + dp(4), dp(64), top + dp(50)), paint);
        }
        text(c, title, back ? dp(76) : dp(74), top + dp(34), 24, TEXT, true, Paint.Align.LEFT);
        if (!back) {
            pill(c, getWidth() - dp(84), top + dp(8), dp(66), dp(34), "BETA", false, null);
        }
    }

    private void drawHome(Canvas c) {
        drawHeader(c, "VITRA", false);
        float y = dp(94);
        text(c, ar("مساحتك، بطريقتك", "Your space, your way"), dp(18), y, 29, TEXT, true, Paint.Align.LEFT);
        text(c, ar("ويدجتات زجاجية تتكيف مع خلفيتك", "Glass widgets that adapt to your wallpaper"), dp(18), y + dp(27), 14, MUTED, false, Paint.Align.LEFT);

        glass(c, dp(18), y + dp(48), getWidth() - dp(36), dp(54), 22, .82f);
        text(c, "⌕", dp(38), y + dp(82), 25, MUTED, false, Paint.Align.LEFT);
        text(c, ar("ابحث عن ساعة، طقس، صلاة...", "Search clock, weather, prayer..."), dp(70), y + dp(80), 14, MUTED, false, Paint.Align.LEFT);
        pill(c, getWidth() - dp(92), y + dp(58), dp(62), dp(34), ar("تصفية", "Filter"), false, "filter");

        float cy = y + dp(124);
        String[] cats = {"All", "New", "Clock", "Digital", "Date", "Weather", "Prayer"};
        float x = dp(18);
        for (String cat : cats) {
            float w = dp(cat.length() * 8 + 28);
            if (x + w > getWidth() - dp(14)) { x = dp(18); cy += dp(44); }
            pill(c, x, cy, w, dp(34), displayCategory(cat), category.equals(cat), "cat:" + cat);
            x += w + dp(8);
        }

        float sy = cy + dp(64);
        text(c, ar("اختيارات مميزة", "Featured collection"), dp(18), sy, 21, TEXT, true, Paint.Align.LEFT);
        text(c, ar("عرض الكل", "See all"), getWidth() - dp(18), sy, 13, CYAN, true, Paint.Align.RIGHT);
        sy += dp(18);
        List<WidgetData> shown = filteredWidgets(false);
        float cardW = (getWidth() - dp(54)) / 2f;
        for (int i = 0; i < shown.size(); i++) {
            int col = i % 2;
            int row = i / 2;
            float left = dp(18) + col * (cardW + dp(18));
            float top = sy + row * dp(225);
            drawWidgetCard(c, shown.get(i), left, top, cardW, dp(207), i);
        }
        contentHeight = sy + ((shown.size() + 1) / 2f) * dp(225) + dp(110);
    }

    private List<WidgetData> filteredWidgets(boolean onlyFavorites) {
        ArrayList<WidgetData> out = new ArrayList<>();
        for (WidgetData item : WidgetData.ALL) {
            if (onlyFavorites && !favorites.contains(item.id)) continue;
            if (category.equals("All") ||
                (category.equals("New") && item.isNew) || item.category.equals(category)) out.add(item);
        }
        return out;
    }

    private String displayCategory(String value) {
        if (!arabic) return value;
        switch (value) {
            case "All": return "الكل"; case "New": return "جديد"; case "Clock": return "ساعة";
            case "Digital": return "رقمية"; case "Date": return "تاريخ"; case "Weather": return "طقس";
            case "Prayer": return "صلاة"; default: return value;
        }
    }

    private void drawWidgetCard(Canvas c, WidgetData data, float l, float t, float w, float h, int index) {
        glass(c, l, t, w, h, 26, .76f);
        RectF preview = new RectF(l + dp(10), t + dp(10), l + w - dp(10), t + dp(128));
        drawPreview(c, data, preview, accent, glassOpacity, corner);
        if (data.isNew) pill(c, l + dp(17), t + dp(17), dp(44), dp(24), ar("جديد", "NEW"), true, null);
        if (data.isPro) pill(c, l + w - dp(53), t + dp(17), dp(36), dp(24), "PRO", false, null);
        drawHeart(c, l + w - dp(25), t + dp(150), favorites.contains(data.id));
        text(c, data.title(arabic), l + dp(12), t + dp(157), 15, TEXT, true, Paint.Align.LEFT);
        text(c, data.subtitle(arabic), l + dp(12), t + dp(179), 11, MUTED, false, Paint.Align.LEFT);
        text(c, "2×2  ·  4×2", l + dp(12), t + dp(197), 10, withAlpha(TEXT, 150), false, Paint.Align.LEFT);
        addHit(l, t, w, h, "detail:" + data.id);
        addHit(l + w - dp(48), t + dp(130), dp(48), dp(48), "heart:" + data.id);
    }

    private void drawPreview(Canvas c, WidgetData data, RectF r, int color, float opacity, float radius) {
        Paint previewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        previewPaint.setShader(new LinearGradient(r.left, r.top, r.right, r.bottom,
            withAlpha(Color.rgb(13, 24, 42), (int)(opacity * 255)),
            withAlpha(Color.rgb(34, 20, 55), (int)(opacity * 255)), Shader.TileMode.CLAMP));
        c.drawRoundRect(r, dp(radius), dp(radius), previewPaint);
        stroke.setColor(withAlpha(color, 120));
        c.drawRoundRect(r, dp(radius), dp(radius), stroke);
        paint.setColor(withAlpha(color, 25));
        c.drawCircle(r.right - dp(15), r.top + dp(15), dp(42), paint);
        float cx = r.centerX(), cy = r.centerY();
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        switch (data.style) {
            case 0:
                stroke.setStrokeWidth(dp(4)); stroke.setColor(color);
                c.drawCircle(cx, cy - dp(5), Math.min(r.width(), r.height()) * .27f, stroke);
                text(c, time, cx, cy + dp(4), 18, TEXT, true, Paint.Align.CENTER); break;
            case 1:
                stroke.setStrokeWidth(dp(5)); stroke.setColor(color);
                RectF ring = new RectF(cx - dp(34), cy - dp(40), cx + dp(34), cy + dp(28));
                c.drawArc(ring, -90, 255, false, stroke);
                text(c, "72%", cx, cy + dp(3), 17, TEXT, true, Paint.Align.CENTER); break;
            case 2:
                text(c, time, cx, cy + dp(6), 30, TEXT, true, Paint.Align.CENTER);
                text(c, "FRIDAY", cx, cy + dp(27), 9, color, true, Paint.Align.CENTER); break;
            case 3:
                text(c, new SimpleDateFormat("dd", Locale.getDefault()).format(new Date()), r.left + dp(24), cy + dp(13), 37, TEXT, true, Paint.Align.LEFT);
                text(c, new SimpleDateFormat("MMM", Locale.getDefault()).format(new Date()).toUpperCase(), r.right - dp(18), cy, 14, color, true, Paint.Align.RIGHT); break;
            case 4:
                text(c, "24°", r.left + dp(18), cy + dp(7), 31, TEXT, true, Paint.Align.LEFT);
                text(c, "☁", r.right - dp(20), cy + dp(8), 31, color, false, Paint.Align.RIGHT); break;
            case 5:
                text(c, ar("المغرب", "MAGHRIB"), r.left + dp(16), cy - dp(2), 15, color, true, Paint.Align.LEFT);
                text(c, "18:31", r.left + dp(16), cy + dp(24), 24, TEXT, true, Paint.Align.LEFT); break;
            case 6:
                text(c, "G   ◉   ↗   ✦", cx, cy + dp(7), 22, TEXT, true, Paint.Align.CENTER); break;
            case 7:
                text(c, "84%", r.left + dp(18), cy + dp(4), 28, TEXT, true, Paint.Align.LEFT);
                progress(c, r.left + dp(18), cy + dp(20), r.width() - dp(36), .84f, color); break;
            case 8:
                text(c, "●  ◐  ✦  ◉", cx, cy + dp(7), 21, TEXT, false, Paint.Align.CENTER); break;
            case 9:
                text(c, "ARG  2  —  1  KSA", cx, cy + dp(6), 17, TEXT, true, Paint.Align.CENTER); break;
            case 10:
                text(c, ar("اصنع مساحة تشبهك", "Make space feel yours"), cx, cy + dp(4), 13, TEXT, true, Paint.Align.CENTER); break;
            default:
                text(c, "AUG  15", cx, cy + dp(6), 24, TEXT, true, Paint.Align.CENTER);
        }
        stroke.setStrokeWidth(dp(1));
    }

    private void drawDetail(Canvas c) {
        drawHeader(c, selected.title(arabic), true);
        float y = dp(92);
        RectF hero = new RectF(dp(18), y, getWidth() - dp(18), y + dp(272));
        glass(c, hero.left, hero.top, hero.width(), hero.height(), 32, .60f);
        drawPhoneFrame(c, hero, selected);
        y += dp(294);
        pill(c, dp(18), y, dp(64), dp(30), selected.isPro ? "PRO" : ar("مجاني", "FREE"), true, null);
        pill(c, dp(90), y, dp(74), dp(30), "2×2 · 4×2", false, null);
        pill(c, dp(172), y, dp(104), dp(30), ar("الرئيسية والقفل", "HOME + LOCK"), false, null);
        y += dp(58);
        text(c, selected.title(arabic), dp(18), y, 27, TEXT, true, Paint.Align.LEFT);
        text(c, selected.subtitle(arabic), dp(18), y + dp(28), 14, MUTED, false, Paint.Align.LEFT);
        y += dp(62);
        drawPrimaryButton(c, dp(18), y, getWidth() - dp(36), dp(54), ar("تخصيص التصميم", "Customize design"), "edit");
        y += dp(66);
        drawSecondaryButton(c, dp(18), y, (getWidth() - dp(48)) * .64f, dp(50), ar("إضافة للشاشة", "Add to screen"), "add");
        drawSecondaryButton(c, dp(30) + (getWidth() - dp(48)) * .64f, y, (getWidth() - dp(48)) * .36f, dp(50), favorites.contains(selected.id) ? "♥" : "♡", "favorite");
        y += dp(82);
        text(c, ar("معلومات التصميم", "Design information"), dp(18), y, 19, TEXT, true, Paint.Align.LEFT);
        y += dp(20);
        infoRow(c, y, ar("الأحجام", "Sizes"), "2×2 · 4×1 · 4×2"); y += dp(58);
        infoRow(c, y, ar("التوافق", "Compatibility"), ar("Android 8+ · شاشة رئيسية وقفل", "Android 8+ · Home and lock")); y += dp(58);
        infoRow(c, y, ar("الأذونات", "Permissions"), permissionLabel(selected)); y += dp(58);
        infoRow(c, y, ar("حالة البيانات", "Data status"), ar("متوفر دون اتصال", "Offline ready"));
        contentHeight = y + dp(150);
    }

    private void drawPhoneFrame(Canvas c, RectF host, WidgetData item) {
        RectF phone = new RectF(host.centerX() - dp(92), host.top + dp(18), host.centerX() + dp(92), host.bottom - dp(18));
        paint.setColor(Color.rgb(3, 6, 12)); c.drawRoundRect(phone, dp(30), dp(30), paint);
        stroke.setColor(withAlpha(TEXT, 70)); c.drawRoundRect(phone, dp(30), dp(30), stroke);
        paint.setShader(new LinearGradient(phone.left, phone.top, phone.right, phone.bottom,
            Color.rgb(15, 45, 69), Color.rgb(64, 20, 73), Shader.TileMode.CLAMP));
        RectF wallpaper = new RectF(phone.left + dp(6), phone.top + dp(6), phone.right - dp(6), phone.bottom - dp(6));
        c.drawRoundRect(wallpaper, dp(26), dp(26), paint); paint.setShader(null);
        RectF preview = new RectF(phone.left + dp(18), phone.centerY() - dp(50), phone.right - dp(18), phone.centerY() + dp(42));
        drawPreview(c, item, preview, accent, glassOpacity, corner);
        paint.setColor(TEXT); c.drawRoundRect(phone.centerX() - dp(24), phone.top + dp(10), phone.centerX() + dp(24), phone.top + dp(14), dp(3), dp(3), paint);
    }

    private String permissionLabel(WidgetData item) {
        if (item.category.equals("Weather") || item.category.equals("Prayer")) return ar("الموقع — عند الاستخدام فقط", "Location — only when used");
        return ar("لا توجد أذونات حساسة", "No sensitive permissions");
    }

    private void drawEditor(Canvas c) {
        drawHeader(c, ar("استوديو التخصيص", "Customization studio"), true);
        float y = dp(88);
        RectF stage = new RectF(dp(18), y, getWidth() - dp(18), y + dp(310));
        glass(c, stage.left, stage.top, stage.width(), stage.height(), 32, .55f);
        RectF widget = new RectF(dp(45), y + dp(86), getWidth() - dp(45), y + dp(218));
        drawPreview(c, selected, widget, accent, glassOpacity, corner);
        text(c, ar("معاينة حية · اسحب وخصص", "LIVE PREVIEW · DRAG & STYLE"), stage.centerX(), y + dp(34), 11, CYAN, true, Paint.Align.CENTER);
        pill(c, stage.left + dp(18), stage.bottom - dp(46), dp(70), dp(30), "↶ Undo", false, "undo");
        pill(c, stage.right - dp(105), stage.bottom - dp(46), dp(87), dp(30), "✦ Auto", true, "auto");
        y += dp(334);
        text(c, ar("لون التوهج", "Accent color"), dp(18), y, 18, TEXT, true, Paint.Align.LEFT);
        int[] colors = {CYAN, VIOLET, Color.rgb(255, 87, 148), Color.rgb(255, 188, 72), Color.rgb(89, 231, 142), Color.WHITE};
        for (int i = 0; i < colors.length; i++) {
            float cx = dp(37) + i * ((getWidth() - dp(74)) / 5f);
            paint.setColor(colors[i]); c.drawCircle(cx, y + dp(36), dp(15), paint);
            if (accent == colors[i]) { stroke.setColor(TEXT); stroke.setStrokeWidth(dp(3)); c.drawCircle(cx, y + dp(36), dp(20), stroke); stroke.setStrokeWidth(dp(1)); }
            addHit(cx - dp(24), y + dp(12), dp(48), dp(48), "accent:" + colors[i]);
        }
        y += dp(88);
        drawSlider(c, y, ar("شفافية الزجاج", "Glass opacity"), glassOpacity, "opacity");
        y += dp(84);
        drawSlider(c, y, ar("استدارة الزوايا", "Corner radius"), corner / 48f, "corner");
        y += dp(92);
        text(c, ar("أدوات الطبقات", "Layer tools"), dp(18), y, 18, TEXT, true, Paint.Align.LEFT);
        y += dp(18);
        String[] tools = arabic ? new String[]{"نص", "خلفية", "أيقونة", "محاذاة"} : new String[]{"Text", "Background", "Icon", "Align"};
        float bw = (getWidth() - dp(54)) / 4f;
        for (int i = 0; i < tools.length; i++) {
            drawSecondaryButton(c, dp(18) + i * (bw + dp(6)), y, bw, dp(54), tools[i], "tool");
        }
        y += dp(78);
        drawPrimaryButton(c, dp(18), y, getWidth() - dp(36), dp(56), ar("حفظ وإضافة إلى الشاشة", "Save and add to screen"), "save_add");
        contentHeight = y + dp(100);
    }

    private void drawSlider(Canvas c, float y, String label, float value, String action) {
        text(c, label, dp(18), y, 16, TEXT, true, Paint.Align.LEFT);
        text(c, Math.round(value * 100) + "%", getWidth() - dp(18), y, 13, MUTED, false, Paint.Align.RIGHT);
        float left = dp(18), right = getWidth() - dp(18), lineY = y + dp(34);
        paint.setColor(withAlpha(TEXT, 35)); c.drawRoundRect(left, lineY, right, lineY + dp(7), dp(4), dp(4), paint);
        paint.setColor(accent); c.drawRoundRect(left, lineY, left + (right-left)*value, lineY + dp(7), dp(4), dp(4), paint);
        c.drawCircle(left + (right-left)*value, lineY + dp(3.5f), dp(10), paint);
        addHit(left, lineY - dp(20), right-left, dp(48), "slider:" + action);
    }

    private void drawWallpapers(Canvas c) {
        drawHeader(c, ar("الخلفيات", "Wallpapers"), false);
        float y = dp(92);
        text(c, ar("اختبر الويدجت على أجواء مختلفة", "Preview widgets across different moods"), dp(18), y, 16, MUTED, false, Paint.Align.LEFT);
        y += dp(32);
        int[][] palettes = {
            {Color.rgb(12,42,65), Color.rgb(84,27,103)}, {Color.rgb(2,52,55), Color.rgb(25,86,68)},
            {Color.rgb(80,28,20), Color.rgb(181,89,34)}, {Color.rgb(13,18,34), Color.rgb(44,51,80)},
            {Color.rgb(31,19,58), Color.rgb(111,44,132)}, {Color.rgb(5,46,67), Color.rgb(24,121,153)}
        };
        float w = (getWidth()-dp(54))/2f;
        for (int i=0;i<palettes.length;i++) {
            float l=dp(18)+(i%2)*(w+dp(18)), t=y+(i/2)*dp(240);
            paint.setShader(new LinearGradient(l,t,l+w,t+dp(205),palettes[i][0],palettes[i][1],Shader.TileMode.CLAMP));
            c.drawRoundRect(l,t,l+w,t+dp(205),dp(28),dp(28),paint); paint.setShader(null);
            paint.setColor(withAlpha(Color.WHITE,25)); c.drawCircle(l+w*.72f,t+dp(60),dp(55),paint);
            text(c, "VITRA", l+dp(14), t+dp(184), 12, withAlpha(TEXT,190), true, Paint.Align.LEFT);
            addHit(l,t,w,dp(205),"wallpaper:"+i);
        }
        contentHeight=y+dp(770);
    }

    private void drawFavorites(Canvas c) {
        drawHeader(c, ar("المفضلة", "Favorites"), false);
        float y = dp(96);
        List<WidgetData> items = filteredWidgets(true);
        if (items.isEmpty()) {
            text(c, "♡", getWidth()/2f, y+dp(85), 54, withAlpha(CYAN,180), false, Paint.Align.CENTER);
            text(c, ar("لا توجد مفضلات بعد", "No favorites yet"), getWidth()/2f, y+dp(136), 21, TEXT, true, Paint.Align.CENTER);
            text(c, ar("اضغط القلب على أي تصميم للاحتفاظ به هنا", "Tap the heart on any design to keep it here"), getWidth()/2f, y+dp(164), 13, MUTED, false, Paint.Align.CENTER);
            contentHeight=dp(600); return;
        }
        float cardW=(getWidth()-dp(54))/2f;
        for(int i=0;i<items.size();i++) drawWidgetCard(c,items.get(i),dp(18)+(i%2)*(cardW+dp(18)),y+(i/2)*dp(225),cardW,dp(207),i);
        contentHeight=y+((items.size()+1)/2f)*dp(225)+dp(110);
    }

    private void drawSettings(Canvas c) {
        drawHeader(c, ar("الإعدادات", "Settings"), false);
        float y=dp(94);
        text(c, ar("التجربة", "EXPERIENCE"), dp(18), y, 11, CYAN, true, Paint.Align.LEFT); y+=dp(16);
        setting(c,y,ar("اللغة", "Language"),arabic?"العربية":"English",true,"language"); y+=dp(70);
        setting(c,y,ar("الوضع الداكن", "Dark appearance"),ar("مفعّل", "On"),true,"theme"); y+=dp(70);
        setting(c,y,ar("حركة مخففة", "Reduced motion"),prefs.getBoolean("reduced",false)?ar("مفعّل","On"):ar("متوقف","Off"),true,"reduced"); y+=dp(88);
        text(c, ar("الخصوصية والبيانات", "PRIVACY & DATA"), dp(18), y, 11, CYAN, true, Paint.Align.LEFT); y+=dp(16);
        setting(c,y,ar("الأذونات", "Permissions"),ar("تُطلب عند الحاجة فقط", "Just in time"),true,"permissions"); y+=dp(70);
        setting(c,y,ar("التحليلات", "Analytics"),ar("متوقفة افتراضيًا", "Off by default"),true,"analytics"); y+=dp(70);
        setting(c,y,ar("حذف البيانات المحلية", "Clear local data"),ar("المفضلة والأنماط", "Favorites and styles"),true,"clear"); y+=dp(88);
        text(c, ar("التشخيص", "DIAGNOSTICS"), dp(18), y, 11, CYAN, true, Paint.Align.LEFT); y+=dp(16);
        setting(c,y,ar("تحديث الويدجت", "Widget refresh"),ar("يعمل · آخر فحص الآن", "Healthy · checked now"),false,null); y+=dp(70);
        setting(c,y,ar("تحسين البطارية", "Battery optimization"),ar("افتح إعدادات النظام", "Open system settings"),true,"battery"); y+=dp(70);
        setting(c,y,ar("إصدار التطبيق", "App version"),"0.1.0 (1)",false,null); y+=dp(88);
        glass(c,dp(18),y,getWidth()-dp(36),dp(104),24,.70f);
        text(c,"VITRA",dp(34),y+dp(37),21,TEXT,true,Paint.Align.LEFT);
        text(c,ar("صُنع بعناية بواسطة ALOMESSI TECH", "Crafted by ALOMESSI TECH"),dp(34),y+dp(64),13,MUTED,false,Paint.Align.LEFT);
        text(c,ar("نسخة تجريبية مفتوحة دون تسجيل", "Open preview — no sign-in required"),dp(34),y+dp(84),11,CYAN,false,Paint.Align.LEFT);
        contentHeight=y+dp(220);
    }

    private void setting(Canvas c,float y,String title,String value,boolean chevron,String action){
        glass(c,dp(18),y,getWidth()-dp(36),dp(60),20,.65f);
        text(c,title,dp(34),y+dp(27),15,TEXT,true,Paint.Align.LEFT);
        text(c,value,dp(34),y+dp(47),11,MUTED,false,Paint.Align.LEFT);
        if(chevron) text(c,"›",getWidth()-dp(36),y+dp(38),24,CYAN,false,Paint.Align.RIGHT);
        if(action!=null) addHit(dp(18),y,getWidth()-dp(36),dp(60),action);
    }

    private void drawBottomNav(Canvas c) {
        float h=dp(78), top=getHeight()-h;
        paint.setColor(withAlpha(Color.rgb(9,13,22),245)); c.drawRect(0,top,getWidth(),getHeight(),paint);
        stroke.setColor(withAlpha(TEXT,25)); c.drawLine(0,top,getWidth(),top,stroke);
        String[] icons={"◫","◒","♡","⚙"};
        String[] en={"Widgets","Wallpapers","Favorites","Settings"};
        String[] ar={"الويدجت","الخلفيات","المفضلة","الإعدادات"};
        Screen[] screens={Screen.HOME,Screen.WALLPAPERS,Screen.FAVORITES,Screen.SETTINGS};
        float slot=getWidth()/4f;
        for(int i=0;i<4;i++){
            float cx=slot*(i+.5f); boolean active=screen==screens[i]; int color=active?CYAN:MUTED;
            if(active){paint.setColor(withAlpha(CYAN,25));c.drawRoundRect(cx-dp(28),top+dp(8),cx+dp(28),top+dp(39),dp(18),dp(18),paint);}
            text(c,icons[i],cx,top+dp(31),20,color,false,Paint.Align.CENTER);
            text(c,arabic?ar[i]:en[i],cx,top+dp(58),10,color,active,Paint.Align.CENTER);
            hits.add(new Hit(new RectF(i*slot,top,(i+1)*slot,getHeight()),"nav:"+screens[i].name(),false));
        }
    }

    private void infoRow(Canvas c,float y,String label,String value){
        glass(c,dp(18),y,getWidth()-dp(36),dp(48),18,.60f);
        text(c,label,dp(32),y+dp(21),12,MUTED,false,Paint.Align.LEFT);
        text(c,value,getWidth()-dp(32),y+dp(30),13,TEXT,true,Paint.Align.RIGHT);
    }

    private void drawPrimaryButton(Canvas c,float l,float t,float w,float h,String label,String action){
        paint.setShader(new LinearGradient(l,t,l+w,t+h,CYAN,VIOLET,Shader.TileMode.CLAMP));
        c.drawRoundRect(l,t,l+w,t+h,dp(20),dp(20),paint); paint.setShader(null);
        text(c,label,l+w/2,t+h/2+dp(6),15,Color.rgb(4,8,15),true,Paint.Align.CENTER);
        addHit(l,t,w,h,action);
    }

    private void drawSecondaryButton(Canvas c,float l,float t,float w,float h,String label,String action){
        glass(c,l,t,w,h,18,.84f); text(c,label,l+w/2,t+h/2+dp(5),14,TEXT,true,Paint.Align.CENTER); addHit(l,t,w,h,action);
    }

    private void drawCircleButton(Canvas c,float cx,float cy,String label,String action){
        paint.setColor(withAlpha(SURFACE_2,230));c.drawCircle(cx,cy,dp(22),paint);stroke.setColor(withAlpha(TEXT,35));c.drawCircle(cx,cy,dp(22),stroke);
        text(c,label,cx,cy+dp(8),28,TEXT,false,Paint.Align.CENTER);addHit(cx-dp(25),cy-dp(25),dp(50),dp(50),action);
    }

    private void drawHeart(Canvas c,float cx,float cy,boolean filled){text(c,filled?"♥":"♡",cx,cy,20,filled?Color.rgb(255,95,151):TEXT,false,Paint.Align.CENTER);}

    private void pill(Canvas c,float l,float t,float w,float h,String label,boolean selected,String action){
        paint.setColor(selected?withAlpha(CYAN,40):withAlpha(SURFACE_2,215));c.drawRoundRect(l,t,l+w,t+h,h/2,h/2,paint);
        stroke.setColor(selected?withAlpha(CYAN,130):withAlpha(TEXT,28));c.drawRoundRect(l,t,l+w,t+h,h/2,h/2,stroke);
        text(c,label,l+w/2,t+h/2+dp(4),10,selected?CYAN:TEXT,true,Paint.Align.CENTER);
        if(action!=null)addHit(l,t,w,h,action);
    }

    private void glass(Canvas c,float l,float t,float w,float h,float radius,float alpha){
        paint.setShader(new LinearGradient(l,t,l+w,t+h,withAlpha(SURFACE_2,(int)(alpha*245)),withAlpha(SURFACE,(int)(alpha*230)),Shader.TileMode.CLAMP));
        c.drawRoundRect(l,t,l+w,t+h,dp(radius),dp(radius),paint);paint.setShader(null);
        stroke.setColor(withAlpha(TEXT,28));c.drawRoundRect(l,t,l+w,t+h,dp(radius),dp(radius),stroke);
    }

    private void progress(Canvas c,float l,float y,float w,float value,int color){paint.setColor(withAlpha(TEXT,30));c.drawRoundRect(l,y,l+w,y+dp(5),dp(3),dp(3),paint);paint.setColor(color);c.drawRoundRect(l,y,l+w*value,y+dp(5),dp(3),dp(3),paint);}

    private void text(Canvas c,String value,float x,float y,float size,int color,boolean bold,Paint.Align align){
        paint.setShader(null);paint.setStyle(Paint.Style.FILL);paint.setColor(color);paint.setTextSize(dp(size));paint.setTextAlign(align);paint.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));
        if (arabic && align==Paint.Align.LEFT) { paint.setTextAlign(Paint.Align.LEFT); }
        c.drawText(value,x,y,paint);
    }

    private String ar(String ar,String en){return arabic?ar:en;}
    private int withAlpha(int color,int alpha){return Color.argb(Math.max(0,Math.min(255,alpha)),Color.red(color),Color.green(color),Color.blue(color));}
    private float dp(float value){return value*getResources().getDisplayMetrics().density;}
    private void addHit(float l,float t,float w,float h,String action){hits.add(new Hit(new RectF(l,t,l+w,t+h),action,true));}
    private void resetScroll(){scrollY=0;invalidate();}

    @Override
    public boolean onTouchEvent(MotionEvent e){
        float y=e.getY();
        switch(e.getActionMasked()){
            case MotionEvent.ACTION_DOWN: downY=lastY=y;dragging=false;return true;
            case MotionEvent.ACTION_MOVE:
                float dy=y-lastY;if(Math.abs(y-downY)>dp(8))dragging=true;
                if(dragging){scrollY=Math.max(0,Math.min(maxScroll(),scrollY-dy));invalidate();}lastY=y;return true;
            case MotionEvent.ACTION_UP:
                if(!dragging)handleTap(e.getX(),y);return true;
        }
        return super.onTouchEvent(e);
    }

    private float maxScroll(){float nav=(screen==Screen.DETAIL||screen==Screen.EDITOR)?dp(20):dp(86);return Math.max(0,contentHeight-getHeight()+nav);}

    private void handleTap(float x,float y){
        for(int i=hits.size()-1;i>=0;i--){Hit hit=hits.get(i);float adjusted=hit.scrolls?y+scrollY:y;if(hit.rect.contains(x,adjusted)){perform(hit.action,x);return;}}
    }

    private void perform(String action,float touchX){
        if(action.startsWith("nav:")){screen=Screen.valueOf(action.substring(4));resetScroll();return;}
        if(action.startsWith("cat:")){category=action.substring(4);resetScroll();return;}
        if(action.startsWith("detail:")){findSelected(action.substring(7));screen=Screen.DETAIL;resetScroll();return;}
        if(action.startsWith("heart:")){toggleFavorite(action.substring(6));invalidate();return;}
        if(action.equals("back")){navigateBack();return;}
        if(action.equals("edit")){screen=Screen.EDITOR;resetScroll();return;}
        if(action.equals("favorite")){toggleFavorite(selected.id);invalidate();return;}
        if(action.equals("add")){requestPin();return;}
        if(action.equals("save_add")){saveStyle();requestPin();return;}
        if(action.startsWith("accent:")){accent=Integer.parseInt(action.substring(7));invalidate();return;}
        if(action.equals("auto")){accent=autoAccent();glassOpacity=.82f;corner=32f;invalidate();return;}
        if(action.startsWith("slider:")){
            float value=Math.max(0f,Math.min(1f,(touchX-dp(18))/(getWidth()-dp(36))));
            if(action.endsWith("opacity"))glassOpacity=.25f+value*.75f;else corner=8f+value*40f;invalidate();return;
        }
        if(action.equals("language")){setArabic(!arabic);return;}
        if(action.equals("reduced")){prefs.edit().putBoolean("reduced",!prefs.getBoolean("reduced",false)).apply();invalidate();return;}
        if(action.equals("battery")){try{getContext().startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));}catch(Exception ignored){}return;}
        if(action.equals("permissions")){try{Intent i=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);i.setData(android.net.Uri.parse("package:"+getContext().getPackageName()));getContext().startActivity(i);}catch(Exception ignored){}return;}
        if(action.equals("clear")){favorites.clear();prefs.edit().remove("favorites").remove("accent").remove("opacity").remove("corner").apply();Toast.makeText(getContext(),ar("تم حذف البيانات المحلية","Local data cleared"),Toast.LENGTH_SHORT).show();invalidate();return;}
        if(action.startsWith("wallpaper:")){accent=new int[]{CYAN,Color.rgb(89,231,142),Color.rgb(255,188,72),Color.WHITE,VIOLET,Color.rgb(71,196,255)}[Integer.parseInt(action.substring(10))];Toast.makeText(getContext(),ar("تم اقتراح لون متناسق","Matching accent suggested"),Toast.LENGTH_SHORT).show();invalidate();return;}
        if(action.equals("filter")||action.equals("tool")||action.equals("undo")){Toast.makeText(getContext(),ar("الميزة جاهزة في الاستوديو","Available in the studio preview"),Toast.LENGTH_SHORT).show();}
    }

    private void findSelected(String id){for(WidgetData d:WidgetData.ALL)if(d.id.equals(id)){selected=d;break;}}
    private void toggleFavorite(String id){if(!favorites.add(id))favorites.remove(id);prefs.edit().putStringSet("favorites",new HashSet<>(favorites)).apply();}
    private int autoAccent(){int[] colors={CYAN,VIOLET,Color.rgb(255,87,148),Color.rgb(89,231,142)};return colors[Math.abs(selected.id.hashCode())%colors.length];}
    private void saveStyle(){prefs.edit().putInt("accent",accent).putFloat("opacity",glassOpacity).putFloat("corner",corner).apply();}

    private void requestPin(){
        if(android.os.Build.VERSION.SDK_INT<26){Toast.makeText(getContext(),ar("أضف الويدجت من قائمة الشاشة الرئيسية","Add the widget from your launcher"),Toast.LENGTH_LONG).show();return;}
        AppWidgetManager manager=AppWidgetManager.getInstance(getContext());
        ComponentName provider=new ComponentName(getContext(),providerFor(selected));
        if(!manager.isRequestPinAppWidgetSupported()){Toast.makeText(getContext(),ar("المشغل لا يدعم الإضافة المباشرة","Launcher does not support direct pinning"),Toast.LENGTH_LONG).show();return;}
        Intent callback=new Intent(getContext(),MainActivity.class);
        PendingIntent success=PendingIntent.getActivity(getContext(),0,callback,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Bundle extras=new Bundle();extras.putString("vitra_style",selected.id);
        manager.requestPinAppWidget(provider,extras,success);
    }

    private Class<?> providerFor(WidgetData data){
        switch(data.category){case "Date":return DateWidgetProvider.class;case "Weather":return WeatherWidgetProvider.class;case "Prayer":return PrayerWidgetProvider.class;case "Search":case "Apps":return SearchWidgetProvider.class;case "System":case "Sports":return SystemWidgetProvider.class;default:return ClockWidgetProvider.class;}
    }

    private static final class Hit{
        final RectF rect;final String action;final boolean scrolls;
        Hit(RectF rect,String action,boolean scrolls){this.rect=rect;this.action=action;this.scrolls=scrolls;}
    }
}
