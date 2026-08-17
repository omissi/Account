package tech.alomessi.vitra;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
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

/**
 * Vitra's complete, dependency-free native catalogue and studio.  The UI is
 * intentionally built with Android views so it remains stable on API 26+ and
 * every visible control has a concrete action rather than a static mock-up.
 */
@SuppressLint("SetTextI18n")
public class MainActivity extends Activity {
    public static final String EXTRA_OPEN_WIDGET = "tech.alomessi.vitra.OPEN_WIDGET";
    private static final String PREFS = "vitra";
    private static final int REQUEST_WALLPAPER = 41;
    private static final int REQUEST_LOCATION = 42;
    private static final int REQUEST_NOTIFICATIONS = 43;

    private SharedPreferences prefs;
    private boolean arabic;
    private String screen = "widgets";
    private String widgetCategory = "All";
    private String wallpaperCategory = "All";
    private String searchQuery = "";
    private WidgetData selectedWidget;
    private WallpaperData selectedWallpaper;
    private IconPackData selectedPack;
    private Uri importedWallpaper;
    private final ArrayList<String> history = new ArrayList<>();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(VitraUi.BG);
        getWindow().setNavigationBarColor(VitraUi.BG);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        arabic = prefs.getBoolean("arabic", true);
        selectedWidget = WidgetData.find(prefs.getString("last_widget", "clock-classic"));
        selectedWallpaper = WallpaperData.find(prefs.getString("wallpaper", "aurora-blue"));
        selectedPack = IconPackData.find(prefs.getString("icon_pack", "midnight"));
        String savedUri = prefs.getString("custom_wallpaper_uri", null);
        if (savedUri != null) importedWallpaper = Uri.parse(savedUri);
        DataRepository.refresh(this, false);
        openIncomingWidget(getIntent());
        render();
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (openIncomingWidget(intent)) render();
    }

    private boolean openIncomingWidget(Intent intent) {
        if (intent == null) return false;
        String id = intent.getStringExtra(EXTRA_OPEN_WIDGET);
        if (id == null || id.trim().isEmpty()) return false;
        selectedWidget = WidgetData.find(id);
        screen = "detail";
        history.clear();
        return true;
    }

    private void render() {
        if (!prefs.getBoolean("onboarded", false)) {
            showOnboarding();
            return;
        }
        FrameLayout shell = new FrameLayout(this);
        shell.setBackgroundColor(VitraUi.BG);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        LinearLayout root = VitraUi.column(this);
        root.setLayoutDirection(arabic ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        root.setPadding(d(18), d(14), d(18), d(isTabScreen() ? 102 : 28));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));
        shell.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
        buildScreen(root);
        if (isTabScreen()) {
            View nav = buildBottomNav();
            FrameLayout.LayoutParams navParams = new FrameLayout.LayoutParams(-1, d(84), Gravity.BOTTOM);
            navParams.setMargins(d(28), 0, d(28), d(14));
            shell.addView(nav, navParams);
        }
        VitraUi.applyInsets(shell);
        setContentView(shell);
    }

    private boolean isTabScreen() {
        return "widgets".equals(screen) || "icons".equals(screen) || "wallpapers".equals(screen);
    }

    private void buildScreen(LinearLayout root) {
        switch (screen) {
            case "icons": buildIcons(root); break;
            case "wallpapers": buildWallpapers(root); break;
            case "wallpaper_detail": buildWallpaperDetail(root); break;
            case "detail": buildWidgetDetail(root); break;
            case "editor": buildEditor(root); break;
            case "search": buildSearch(root); break;
            case "favorites": buildFavorites(root); break;
            case "settings": buildSettings(root); break;
            case "appearance": buildAppearance(root); break;
            case "date_time": buildDateTime(root); break;
            case "weather": buildWeather(root); break;
            case "prayer": buildPrayer(root); break;
            case "sync": buildSync(root); break;
            case "permissions": buildPermissions(root); break;
            case "about": buildAbout(root); break;
            case "terms": buildTerms(root, false); break;
            case "privacy": buildTerms(root, true); break;
            case "tutorials": buildTutorials(root); break;
            case "pro": buildPro(root); break;
            default: buildWidgets(root); break;
        }
    }

    private void showOnboarding() {
        FrameLayout shell = new FrameLayout(this);
        shell.setBackground(VitraUi.gradient(this, 0xff08132e, 0xff171126, 0));
        LinearLayout root = VitraUi.column(this);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(d(28), d(52), d(28), d(32));
        TextView logo = VitraUi.text(this, "V", 48, Color.WHITE, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(VitraUi.circle(this, 0x332c64b6, true));
        root.addView(logo, size(d(94), d(94), 0));
        TextView brand = VitraUi.text(this, "V I T R A", 25, Color.WHITE, true);
        brand.setLetterSpacing(.16f); brand.setGravity(Gravity.CENTER);
        root.addView(brand, fill(d(46), d(10)));
        TextView title = VitraUi.text(this, t("Your screen, illuminated", "شاشتك، بأسلوب أكثر أناقة"), 30, Color.WHITE, true);
        title.setGravity(Gravity.CENTER); title.setLineSpacing(d(4), 1f);
        root.addView(title, fill(-2, d(12)));
        TextView copy = VitraUi.text(this, t("Live widgets, original glass backgrounds, and shortcut styles made for your Android home screen.", "ويدجت حية وخلفيات زجاجية أصلية وأنماط اختصارات لشاشة Android الرئيسية."), 15, 0xffdad9e7, false);
        copy.setGravity(Gravity.CENTER); copy.setLineSpacing(d(5), 1f);
        root.addView(copy, fill(-2, d(30)));
        FrameLayout example = new FrameLayout(this);
        example.setBackground(VitraUi.glass(this, 30, .95f, 0x665f6daa));
        WidgetPreviewView preview = new WidgetPreviewView(this, WidgetData.find("digital-insight"), arabic, VitraUi.PURPLE);
        example.addView(preview, new FrameLayout.LayoutParams(-1, -1));
        root.addView(example, fill(d(232), d(30)));
        Button language = VitraUi.secondary(this, arabic ? "English" : "العربية");
        language.setOnClickListener(v -> { arabic = !arabic; prefs.edit().putBoolean("arabic", arabic).apply(); showOnboarding(); });
        root.addView(language, fill(d(48), d(12)));
        Button start = VitraUi.primary(this, t("Start creating", "ابدأ التصميم"));
        start.setOnClickListener(v -> { prefs.edit().putBoolean("onboarded", true).apply(); screen = "widgets"; render(); });
        root.addView(start, fill(d(58), 0));
        shell.addView(root, new FrameLayout.LayoutParams(-1, -1));
        VitraUi.applyInsets(shell);
        setContentView(shell);
    }

    private void buildWidgets(LinearLayout root) {
        header(root, "V I T R A", false, true);
        TextView tagline = VitraUi.text(this, t("EVERY WIDGET, YOUR WAY", "كل ويدجت بطريقتك"), 11, VitraUi.PURPLE, true);
        tagline.setLetterSpacing(.16f); tagline.setGravity(Gravity.CENTER);
        root.addView(tagline, fill(-2, d(16)));
        LinearLayout actions = VitraUi.row(this);
        Button search = VitraUi.secondary(this, "⌕  " + t("Search", "بحث"));
        search.setOnClickListener(v -> navigate("search"));
        Button refresh = VitraUi.secondary(this, "↻  " + t("Refresh", "تحديث"));
        refresh.setOnClickListener(v -> { DataRepository.refresh(this, true); toast(t("Live data refresh started", "بدأ تحديث البيانات الحية")); });
        actions.addView(search, weighted(1, d(46), d(8), 0)); actions.addView(refresh, weighted(1, d(46), 0, 0));
        root.addView(actions, fill(d(46), d(16)));
        addWidgetCategories(root);
        TextView count = VitraUi.text(this, WidgetData.inCategory(widgetCategory).size() + " " + t("ready-to-use designs", "تصميماً جاهزاً"), 12, VitraUi.MUTED, false);
        root.addView(count, fill(d(26), d(6)));
        addWidgetGrid(root, WidgetData.inCategory(widgetCategory));
    }

    private void addWidgetCategories(LinearLayout root) {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = VitraUi.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        for (String category : WidgetData.CATEGORIES) {
            Button chip = VitraUi.chip(this, categoryTitle(category), category.equals(widgetCategory));
            chip.setOnClickListener(v -> { widgetCategory = category; render(); });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, d(40));
            params.setMargins(0, 0, d(8), 0);
            row.addView(chip, params);
        }
        scroll.addView(row, new HorizontalScrollView.LayoutParams(-2, -1));
        root.addView(scroll, fill(d(46), d(8)));
    }

    private void addWidgetGrid(LinearLayout root, List<WidgetData> items) {
        LinearLayout row = null;
        for (WidgetData item : items) {
            if (item.span == 2) {
                if (row != null) { root.addView(row, fill(-2, d(12))); row = null; }
                root.addView(widgetCard(item, true), fill(-2, d(14)));
            } else {
                if (row == null) { row = VitraUi.row(this); }
                View card = widgetCard(item, false);
                int right = row.getChildCount() == 0 ? d(10) : 0;
                row.addView(card, weighted(1, -2, right, 0));
                if (row.getChildCount() == 2) { root.addView(row, fill(-2, d(14))); row = null; }
            }
        }
        if (row != null) {
            SpaceFill fill = new SpaceFill(this);
            row.addView(fill, weighted(1, -2, 0, 0));
            root.addView(row, fill(-2, d(14)));
        }
    }

    private View widgetCard(WidgetData item, boolean wide) {
        LinearLayout card = VitraUi.column(this);
        card.setClickable(true);
        card.setPadding(0, 0, 0, d(2));
        FrameLayout art = new FrameLayout(this);
        art.setBackground(VitraUi.glass(this, 26, .96f, VitraUi.BORDER));
        WidgetPreviewView preview = new WidgetPreviewView(this, item, arabic, activeAccent());
        preview.setAppearance(activeAccent(), prefs.getInt("opacity_int", 76), prefs.getInt("darkness_int", 72), prefs.getInt("blur_int", 18), prefs.getInt("corner_int", 26), "Clear");
        art.addView(preview, new FrameLayout.LayoutParams(-1, -1));
        if (item.isNew) {
            TextView badge = badge(t("NEW", "جديد"), VitraUi.BLUE);
            FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(-2, d(28), Gravity.TOP | (arabic ? Gravity.RIGHT : Gravity.LEFT));
            p.setMargins(d(10), d(10), d(10), 0); art.addView(badge, p);
        }
        if (item.isPro) {
            TextView crown = badge("♛  PRO", VitraUi.GOLD);
            FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(-2, d(28), Gravity.TOP | (arabic ? Gravity.LEFT : Gravity.RIGHT));
            p.setMargins(d(10), d(10), d(10), 0); art.addView(crown, p);
        }
        card.addView(art, fill(wide ? d(178) : d(150), d(7)));
        TextView name = VitraUi.text(this, item.title(arabic), 14, VitraUi.TEXT, true);
        name.setMaxLines(1); name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        card.addView(name, fill(d(23), 0));
        TextView sub = VitraUi.text(this, item.subtitle(arabic), 11, VitraUi.MUTED, false);
        sub.setMaxLines(1); sub.setEllipsize(android.text.TextUtils.TruncateAt.END);
        card.addView(sub, fill(d(20), 0));
        card.setOnClickListener(v -> openWidget(item));
        return card;
    }

    private TextView badge(String label, int color) {
        TextView badge = VitraUi.text(this, label, 10, color == VitraUi.GOLD ? 0xff1a1405 : Color.WHITE, true);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(d(9), 0, d(9), 0);
        badge.setBackground(VitraUi.gradient(this, color, color, 15));
        return badge;
    }

    private void buildIcons(LinearLayout root) {
        header(root, t("Icon treatments", "الأيقونات"), false, true);
        TextView copy = VitraUi.text(this, t("Original Vitra shortcut styles. Select one, then add a working shortcuts widget to your home screen.", "أنماط اختصارات Vitra الأصلية. اختر النمط ثم أضف ويدجت اختصارات عاملة إلى الشاشة الرئيسية."), 13, VitraUi.MUTED, false);
        copy.setLineSpacing(d(3), 1f); root.addView(copy, fill(-2, d(18)));
        FrameLayout selected = new FrameLayout(this);
        selected.setBackground(VitraUi.glass(this, 28, .98f, 0xff5d5d66));
        IconPackPreviewView selectedPreview = new IconPackPreviewView(this, selectedPack);
        selected.addView(selectedPreview, new FrameLayout.LayoutParams(-1, -1));
        TextView selectedLabel = badge(t("SELECTED", "المحدد") + " · " + selectedPack.name(arabic), VitraUi.BLUE);
        FrameLayout.LayoutParams selectedLabelP = new FrameLayout.LayoutParams(-2, d(29), Gravity.TOP | (arabic ? Gravity.RIGHT : Gravity.LEFT));
        selectedLabelP.setMargins(d(12), d(12), d(12), 0); selected.addView(selectedLabel, selectedLabelP);
        root.addView(selected, fill(d(210), d(13)));
        Button addShortcuts = VitraUi.primary(this, t("Add shortcuts widget", "إضافة ويدجت الاختصارات"));
        addShortcuts.setOnClickListener(v -> requestPin(WidgetData.find("apps-glass")));
        root.addView(addShortcuts, fill(d(54), d(24)));
        TextView heading = VitraUi.text(this, t("Choose an icon treatment", "اختر نمط الأيقونات"), 17, VitraUi.TEXT, true);
        root.addView(heading, fill(d(33), d(8)));
        LinearLayout row = null;
        for (IconPackData pack : IconPackData.ALL) {
            if (row == null) row = VitraUi.row(this);
            row.addView(iconPackCard(pack), weighted(1, d(188), row.getChildCount() == 0 ? d(10) : 0, 0));
            if (row.getChildCount() == 2) { root.addView(row, fill(d(188), d(14))); row = null; }
        }
        if (row != null) { row.addView(new SpaceFill(this), weighted(1, d(188), 0, 0)); root.addView(row, fill(d(188), d(14))); }
    }

    private View iconPackCard(IconPackData pack) {
        LinearLayout card = VitraUi.column(this);
        card.setClickable(true);
        FrameLayout art = new FrameLayout(this);
        art.setBackground(VitraUi.glass(this, 22, .96f, pack.id.equals(selectedPack.id) ? VitraUi.BLUE : VitraUi.BORDER));
        art.addView(new IconPackPreviewView(this, pack), new FrameLayout.LayoutParams(-1, -1));
        card.addView(art, fill(d(122), d(7)));
        TextView title = VitraUi.text(this, pack.name(arabic), 14, VitraUi.TEXT, true); card.addView(title, fill(d(23), 0));
        TextView sub = VitraUi.text(this, pack.subtitle(arabic), 10, VitraUi.MUTED, false); sub.setMaxLines(2); card.addView(sub, fill(d(34), 0));
        card.setOnClickListener(v -> { selectedPack = pack; prefs.edit().putString("icon_pack", pack.id).apply(); toast(t("Shortcut style selected", "تم اختيار نمط الاختصارات")); render(); });
        return card;
    }

    private void buildWallpapers(LinearLayout root) {
        header(root, t("Wallpapers", "الخلفيات"), false, true);
        TextView copy = VitraUi.text(this, t("Original, locally rendered Vitra backgrounds. Apply to the system home screen or lock screen.", "خلفيات Vitra أصلية تُرسم محلياً. طبّقها على الشاشة الرئيسية أو شاشة القفل."), 13, VitraUi.MUTED, false);
        copy.setLineSpacing(d(3), 1f); root.addView(copy, fill(-2, d(12)));
        Button importPhoto = VitraUi.secondary(this, "▧  " + t("Import your photo", "استيراد صورتك"));
        importPhoto.setOnClickListener(v -> chooseWallpaperPhoto());
        root.addView(importPhoto, fill(d(48), d(14)));
        addWallpaperCategories(root);
        LinearLayout row = null;
        for (WallpaperData wallpaper : WallpaperData.ALL) {
            if (!"All".equals(wallpaperCategory) && !wallpaperCategory.equals(wallpaper.category)) continue;
            if (row == null) row = VitraUi.row(this);
            row.addView(wallpaperCard(wallpaper), weighted(1, d(206), row.getChildCount() < 2 ? d(8) : 0, 0));
            if (row.getChildCount() == 3) { root.addView(row, fill(d(206), d(12))); row = null; }
        }
        if (row != null) {
            while (row.getChildCount() < 3) row.addView(new SpaceFill(this), weighted(1, d(206), row.getChildCount() < 2 ? d(8) : 0, 0));
            root.addView(row, fill(d(206), d(12)));
        }
    }

    private void addWallpaperCategories(LinearLayout root) {
        HorizontalScrollView scroll = new HorizontalScrollView(this); scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = VitraUi.row(this);
        for (String category : WallpaperData.CATEGORIES) {
            Button chip = VitraUi.chip(this, wallpaperCategoryTitle(category), category.equals(wallpaperCategory));
            chip.setOnClickListener(v -> { wallpaperCategory = category; render(); });
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, d(40)); p.setMargins(0, 0, d(8), 0); row.addView(chip, p);
        }
        scroll.addView(row); root.addView(scroll, fill(d(45), d(10)));
    }

    private View wallpaperCard(WallpaperData wallpaper) {
        FrameLayout card = new FrameLayout(this);
        card.setClickable(true); card.setForegroundGravity(Gravity.CENTER);
        card.setBackground(VitraUi.glass(this, 18, .96f, wallpaper.id.equals(selectedWallpaper.id) ? VitraUi.BLUE : VitraUi.BORDER));
        WallpaperPreviewView view = new WallpaperPreviewView(this, wallpaper);
        card.addView(view, new FrameLayout.LayoutParams(-1, -1));
        if (wallpaper.id.equals(selectedWallpaper.id)) {
            TextView check = badge("✓", VitraUi.BLUE);
            FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(d(30), d(30), Gravity.TOP | (arabic ? Gravity.LEFT : Gravity.RIGHT));
            p.setMargins(d(7), d(7), d(7), 0); card.addView(check, p);
        }
        card.setOnClickListener(v -> { selectedWallpaper = wallpaper; prefs.edit().putString("wallpaper", wallpaper.id).apply(); navigate("wallpaper_detail"); });
        return card;
    }

    private void buildWallpaperDetail(LinearLayout root) {
        header(root, importedWallpaper == null ? selectedWallpaper.name(arabic) : t("Your photo", "صورتك"), true, false);
        FrameLayout preview = new FrameLayout(this);
        preview.setBackground(VitraUi.glass(this, 30, 1f, 0xff6a6a72));
        if (importedWallpaper != null) {
            ImageView image = new ImageView(this); image.setScaleType(ImageView.ScaleType.CENTER_CROP); image.setImageURI(importedWallpaper); preview.addView(image, new FrameLayout.LayoutParams(-1, -1));
        } else preview.addView(new WallpaperPreviewView(this, selectedWallpaper), new FrameLayout.LayoutParams(-1, -1));
        TextView mark = VitraUi.text(this, "V I T R A", 12, 0xccffffff, true); mark.setLetterSpacing(.14f);
        FrameLayout.LayoutParams markP = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL); markP.setMargins(0, 0, 0, d(18)); preview.addView(mark, markP);
        root.addView(preview, fill(d(390), d(22)));
        TextView intro = VitraUi.text(this, t("Wallpaper permission is used only after you tap one of the apply buttons below.", "لا يُستخدم إذن الخلفية إلا بعد الضغط على أحد زري التطبيق أدناه."), 12, VitraUi.MUTED, false);
        intro.setGravity(Gravity.CENTER); root.addView(intro, fill(-2, d(18)));
        Button home = VitraUi.primary(this, t("Apply to home screen", "تعيين للشاشة الرئيسية")); home.setOnClickListener(v -> applyWallpaper(WallpaperManager.FLAG_SYSTEM));
        Button lock = VitraUi.secondary(this, t("Apply to lock screen", "تعيين لشاشة القفل")); lock.setOnClickListener(v -> applyWallpaper(WallpaperManager.FLAG_LOCK));
        Button choose = VitraUi.secondary(this, t("Choose a different photo", "اختيار صورة أخرى")); choose.setOnClickListener(v -> chooseWallpaperPhoto());
        root.addView(home, fill(d(56), d(10))); root.addView(lock, fill(d(54), d(10))); root.addView(choose, fill(d(48), 0));
    }

    private void buildWidgetDetail(LinearLayout root) {
        header(root, selectedWidget.title(arabic), true, false);
        LinearLayout row = VitraUi.row(this); row.setGravity(Gravity.CENTER_VERTICAL);
        TextView category = badge(collectionTitle(selectedWidget.collection) + " · " + categoryTitle(selectedWidget.category), VitraUi.BLUE);
        row.addView(category, new LinearLayout.LayoutParams(-2, d(30)));
        SpaceFill space = new SpaceFill(this); row.addView(space, new LinearLayout.LayoutParams(0, 1, 1));
        Button fav = VitraUi.circleButton(this, isFavorite(selectedWidget.id) ? "♥" : "♡"); fav.setTextColor(isFavorite(selectedWidget.id) ? 0xffff6f91 : Color.WHITE); fav.setOnClickListener(v -> toggleFavorite(selectedWidget));
        row.addView(fav, new LinearLayout.LayoutParams(d(46), d(46))); root.addView(row, fill(d(50), d(10)));
        FrameLayout art = new FrameLayout(this); art.setBackground(VitraUi.glass(this, 30, .98f, 0xff5d5d62));
        WidgetPreviewView preview = new WidgetPreviewView(this, selectedWidget, arabic, activeAccent());
        preview.setAppearance(activeAccent(), prefs.getInt("opacity_int", 76), prefs.getInt("darkness_int", 72), prefs.getInt("blur_int", 18), prefs.getInt("corner_int", 26), prefs.getString("last_background", "Clear"));
        art.addView(preview, new FrameLayout.LayoutParams(-1, -1)); root.addView(art, fill(d(selectedWidget.span == 2 ? 246 : 280), d(14)));
        TextView description = VitraUi.text(this, selectedWidget.subtitle(arabic), 15, VitraUi.MUTED, false); root.addView(description, fill(-2, d(18)));
        if (selectedWidget.isPro && !prefs.getBoolean("pro_preview", false)) {
            LinearLayout locked = glassCard();
            TextView lockTitle = VitraUi.text(this, "♛  " + t("Pro collection", "مجموعة Pro"), 16, VitraUi.GOLD, true);
            TextView lockCopy = VitraUi.text(this, t("This design is shown in the catalogue. Use the development preview to test it before connecting Google Play Billing.", "هذا التصميم ظاهر في الكتالوج. استخدم معاينة التطوير لاختباره قبل ربط Google Play Billing."), 12, VitraUi.MUTED, false);
            lockCopy.setLineSpacing(d(3), 1f); locked.addView(lockTitle, fill(d(28), d(4))); locked.addView(lockCopy, fill(-2, d(12)));
            Button viewPro = VitraUi.secondary(this, t("View Pro options", "عرض خيارات Pro")); viewPro.setOnClickListener(v -> navigate("pro")); locked.addView(viewPro, fill(d(45), 0)); root.addView(locked, fill(-2, d(16)));
        }
        Button add = VitraUi.primary(this, t("Add to home screen", "إضافة إلى الشاشة الرئيسية"));
        add.setOnClickListener(v -> { if (selectedWidget.isPro && !prefs.getBoolean("pro_preview", false)) navigate("pro"); else requestPin(selectedWidget); });
        Button edit = VitraUi.secondary(this, t("Customize this widget", "تخصيص هذه الويدجت")); edit.setOnClickListener(v -> navigate("editor"));
        root.addView(add, fill(d(57), d(10))); root.addView(edit, fill(d(52), d(24)));
        TextView how = VitraUi.text(this, t("Tip: selecting Add opens your launcher’s official widget placement flow, where Vitra stores the chosen design per widget instance.", "معلومة: يفتح زر الإضافة مسار وضع الويدجت الرسمي في اللانشر، ويحفظ Vitra التصميم المختار لكل نسخة ويدجت."), 12, VitraUi.MUTED, false);
        how.setLineSpacing(d(3), 1f); root.addView(how, fill(-2, 0));
    }

    private void buildEditor(LinearLayout root) {
        header(root, t("Widget studio", "استوديو الويدجت"), true, false);
        TextView name = VitraUi.text(this, selectedWidget.title(arabic), 15, VitraUi.PURPLE, true); name.setGravity(Gravity.CENTER); root.addView(name, fill(d(27), d(8)));
        final int[] accent = { prefs.getInt("accent", VitraUi.PURPLE) };
        final int[] opacity = { prefs.getInt("opacity_int", 76) };
        final int[] darkness = { prefs.getInt("darkness_int", 72) };
        final int[] blur = { prefs.getInt("blur_int", 18) };
        final int[] radius = { prefs.getInt("corner_int", 26) };
        final String[] background = { prefs.getString("last_background", "Clear") };
        final boolean[] showDate = { prefs.getBoolean("last_show_date", true) };
        FrameLayout art = new FrameLayout(this); art.setBackground(VitraUi.glass(this, 30, .98f, 0xff606065));
        WidgetPreviewView preview = new WidgetPreviewView(this, selectedWidget, arabic, accent[0]);
        preview.setAppearance(accent[0], opacity[0], darkness[0], blur[0], radius[0], background[0]); preview.setShowDate(showDate[0]);
        art.addView(preview, new FrameLayout.LayoutParams(-1, -1)); root.addView(art, fill(d(254), d(16)));
        root.addView(section(t("BACKGROUND", "الخلفية")), fill(d(22), d(7)));
        LinearLayout bgChoices = VitraUi.row(this);
        String[] bgs = {"Clear", "Fill", "Gradient", "Image"};
        for (String value : bgs) {
            Button choice = VitraUi.chip(this, localizedBackground(value), value.equals(background[0]));
            choice.setOnClickListener(v -> { background[0] = value; preview.setAppearance(accent[0], opacity[0], darkness[0], blur[0], radius[0], background[0]); refreshChoiceRow(bgChoices, bgs, background[0], this::localizedBackground, selected -> { background[0] = selected; preview.setAppearance(accent[0], opacity[0], darkness[0], blur[0], radius[0], background[0]); }); });
            bgChoices.addView(choice, weighted(1, d(40), d(5), 0));
        }
        root.addView(bgChoices, fill(d(40), d(16)));
        root.addView(slider(t("Opacity", "الشفافية"), 25, 100, opacity[0], value -> { opacity[0] = value; preview.setAppearance(accent[0], opacity[0], darkness[0], blur[0], radius[0], background[0]); }), fill(d(59), d(6)));
        root.addView(slider(t("Glass darkness", "عتامة الزجاج"), 0, 100, darkness[0], value -> { darkness[0] = value; preview.setAppearance(accent[0], opacity[0], darkness[0], blur[0], radius[0], background[0]); }), fill(d(59), d(6)));
        root.addView(slider(t("Blur", "التمويه"), 0, 40, blur[0], value -> { blur[0] = value; preview.setAppearance(accent[0], opacity[0], darkness[0], blur[0], radius[0], background[0]); }), fill(d(59), d(6)));
        root.addView(slider(t("Corner radius", "دائرية الحواف"), 8, 48, radius[0], value -> { radius[0] = value; preview.setAppearance(accent[0], opacity[0], darkness[0], blur[0], radius[0], background[0]); }), fill(d(59), d(14)));
        root.addView(section(t("HIGHLIGHT COLOR", "لون الإضاءة")), fill(d(22), d(7)));
        LinearLayout palette = VitraUi.row(this);
        int[] colors = {Color.WHITE, VitraUi.PURPLE, VitraUi.BLUE, 0xff7cd69a, 0xffff7fa8, VitraUi.GOLD};
        for (int color : colors) {
            Button dot = VitraUi.circleButton(this, color == accent[0] ? "✓" : "");
            dot.setTextColor(color == Color.WHITE ? Color.BLACK : Color.WHITE); dot.setBackground(VitraUi.circle(this, color, color == accent[0]));
            dot.setOnClickListener(v -> { accent[0] = color; preview.setAppearance(accent[0], opacity[0], darkness[0], blur[0], radius[0], background[0]); });
            palette.addView(dot, weighted(1, d(44), d(5), 0));
        }
        root.addView(palette, fill(d(44), d(16)));
        root.addView(section(t("CONTENT", "المحتوى")), fill(d(22), d(7)));
        Switch date = new Switch(this); date.setChecked(showDate[0]);
        LinearLayout dateRow = preferenceRow(t("Show date", "إظهار التاريخ"), t("Display the current date under time", "إظهار التاريخ الحالي أسفل الوقت"), date, v -> {});
        date.setOnCheckedChangeListener((button, checked) -> { showDate[0] = checked; preview.setShowDate(checked); });
        root.addView(dateRow, fill(d(72), d(10)));
        Button language = VitraUi.secondary(this, t("Language & numeral formatting", "اللغة وتنسيق الأرقام")); language.setOnClickListener(v -> showLanguageDialog()); root.addView(language, fill(d(48), d(18)));
        Button save = VitraUi.primary(this, t("Save studio defaults", "حفظ إعدادات الاستوديو"));
        save.setOnClickListener(v -> { prefs.edit().putInt("accent", accent[0]).putInt("opacity_int", opacity[0]).putInt("darkness_int", darkness[0]).putInt("blur_int", blur[0]).putInt("corner_int", radius[0]).putString("last_background", background[0]).putBoolean("last_show_date", showDate[0]).apply(); toast(t("Studio defaults saved", "تم حفظ إعدادات الاستوديو")); });
        root.addView(save, fill(d(56), d(10)));
        Button reset = VitraUi.secondary(this, t("Reset studio defaults", "إعادة ضبط الاستوديو")); reset.setOnClickListener(v -> { prefs.edit().remove("accent").remove("opacity_int").remove("darkness_int").remove("blur_int").remove("corner_int").remove("last_background").remove("last_show_date").apply(); toast(t("Defaults reset", "تمت إعادة الضبط")); render(); }); root.addView(reset, fill(d(48), 0));
    }

    private interface ChoiceLabel { String label(String value); }
    private interface ChoiceChanged { void select(String value); }
    private void refreshChoiceRow(LinearLayout row, String[] values, String selected, ChoiceLabel label, ChoiceChanged changed) {
        row.removeAllViews();
        for (String value : values) {
            Button item = VitraUi.chip(this, label.label(value), value.equals(selected));
            item.setOnClickListener(v -> changed.select(value));
            row.addView(item, weighted(1, d(40), d(5), 0));
        }
    }

    private void buildSearch(LinearLayout root) {
        header(root, t("Search designs", "البحث في التصاميم"), true, false);
        LinearLayout row = VitraUi.row(this);
        EditText input = new EditText(this); input.setSingleLine(true); input.setText(searchQuery); input.setTextColor(VitraUi.TEXT); input.setTextSize(15); input.setHintTextColor(VitraUi.MUTED); input.setHint(t("Search clocks, weather, prayer…", "ابحث عن ساعة أو طقس أو صلاة…")); input.setPadding(d(15), 0, d(15), 0); input.setBackground(VitraUi.glass(this, 20, .95f, VitraUi.BORDER));
        Button submit = VitraUi.primary(this, t("Search", "بحث"));
        submit.setOnClickListener(v -> { searchQuery = input.getText().toString().trim(); render(); });
        row.addView(input, weighted(1, d(50), d(8), 0)); row.addView(submit, new LinearLayout.LayoutParams(d(96), d(50))); root.addView(row, fill(d(50), d(14)));
        List<WidgetData> result = new ArrayList<>();
        String query = searchQuery.toLowerCase(Locale.ROOT);
        for (WidgetData item : WidgetData.ALL) if (query.isEmpty() || item.titleEn.toLowerCase(Locale.ROOT).contains(query) || item.titleAr.contains(searchQuery) || item.category.toLowerCase(Locale.ROOT).contains(query)) result.add(item);
        TextView count = VitraUi.text(this, result.size() + " " + t("results", "نتيجة"), 13, VitraUi.MUTED, false); root.addView(count, fill(d(28), d(4)));
        addWidgetGrid(root, result);
    }

    private void buildFavorites(LinearLayout root) {
        header(root, t("Favorites", "المفضلة"), true, false);
        ArrayList<WidgetData> list = new ArrayList<>(); Set<String> ids = favorites(); for (WidgetData item : WidgetData.ALL) if (ids.contains(item.id)) list.add(item);
        if (list.isEmpty()) {
            LinearLayout empty = glassCard();
            TextView icon = VitraUi.text(this, "♡", 42, VitraUi.PURPLE, false); icon.setGravity(Gravity.CENTER); empty.addView(icon, fill(d(64), d(4)));
            TextView title = VitraUi.text(this, t("No saved designs yet", "لا توجد تصاميم محفوظة بعد"), 18, VitraUi.TEXT, true); title.setGravity(Gravity.CENTER); empty.addView(title, fill(d(30), d(6)));
            TextView detail = VitraUi.text(this, t("Open any widget and tap the heart to build your personal collection.", "افتح أي ويدجت واضغط القلب لبناء مجموعتك الشخصية."), 13, VitraUi.MUTED, false); detail.setGravity(Gravity.CENTER); empty.addView(detail, fill(-2, d(12)));
            Button browse = VitraUi.secondary(this, t("Browse widgets", "استعراض الويدجت")); browse.setOnClickListener(v -> switchTab("widgets")); empty.addView(browse, fill(d(48), 0)); root.addView(empty, fill(-2, 0));
        } else addWidgetGrid(root, list);
    }

    private void buildSettings(LinearLayout root) {
        header(root, t("Settings", "الإعدادات"), true, false);
        root.addView(section(t("DISPLAY", "العرض")), fill(d(24), d(7)));
        LinearLayout display = glassCard();
        display.addView(settingsRow(t("App language", "لغة التطبيق"), arabic ? "العربية" : "English", "◀", v -> showLanguageDialog()), fill(d(68), 0));
        display.addView(divider(), fill(d(1), 0));
        display.addView(settingsRow(t("Date & time", "التاريخ والوقت"), t("Format and calendar", "التنسيق والتقويم"), "‹", v -> navigate("date_time")), fill(d(68), 0));
        display.addView(divider(), fill(d(1), 0));
        display.addView(settingsRow(t("Prayer times", "أوقات الصلاة"), t("Location and method", "الموقع وطريقة الحساب"), "‹", v -> navigate("prayer")), fill(d(68), 0));
        display.addView(divider(), fill(d(1), 0));
        display.addView(settingsRow(t("Weather", "الطقس"), prefs.getString("city_name", t("Sana'a", "صنعاء")), "‹", v -> navigate("weather")), fill(d(68), 0));
        display.addView(divider(), fill(d(1), 0));
        display.addView(settingsRow(t("Customization", "التخصيص"), t("Glass defaults", "إعدادات الزجاج"), "‹", v -> navigate("appearance")), fill(d(68), 0));
        root.addView(display, fill(-2, d(20)));
        root.addView(section(t("LIVE DATA & PERMISSIONS", "البيانات الحية والأذونات")), fill(d(24), d(7)));
        LinearLayout sync = glassCard();
        sync.addView(settingsRow(t("Live updates", "التحديث المباشر"), t("Weather and prayer refresh", "تحديث الطقس والصلاة"), "‹", v -> navigate("sync")), fill(d(68), 0));
        sync.addView(divider(), fill(d(1), 0)); sync.addView(settingsRow(t("Permissions", "الأذونات"), t("Location, notifications and battery", "الموقع والإشعارات والبطارية"), "‹", v -> navigate("permissions")), fill(d(68), 0)); root.addView(sync, fill(-2, d(20)));
        root.addView(section(t("ABOUT", "حول")), fill(d(24), d(7)));
        LinearLayout about = glassCard();
        about.addView(settingsRow(t("Terms of use", "شروط الاستخدام"), "", "↗", v -> navigate("terms")), fill(d(62), 0)); about.addView(divider(), fill(d(1), 0));
        about.addView(settingsRow(t("Privacy policy", "سياسة الخصوصية"), "", "↗", v -> navigate("privacy")), fill(d(62), 0)); about.addView(divider(), fill(d(1), 0));
        about.addView(settingsRow(t("About Vitra", "عن Vitra"), "1.2.0", "‹", v -> navigate("about")), fill(d(62), 0)); root.addView(about, fill(-2, 0));
    }

    private void buildAppearance(LinearLayout root) {
        header(root, t("Customization", "التخصيص"), true, false);
        TextView copy = VitraUi.text(this, t("These defaults are used by new widget configurations. Existing widgets keep their own saved configuration.", "تُستخدم هذه الإعدادات الافتراضية في إعدادات الويدجت الجديدة. تحافظ الويدجت الموجودة على إعداداتها الخاصة."), 13, VitraUi.MUTED, false); copy.setLineSpacing(d(3), 1f); root.addView(copy, fill(-2, d(18)));
        LinearLayout card = glassCard();
        card.addView(settingsRow(t("Accent color", "لون الإضاءة"), colorName(activeAccent()), "‹", v -> showAccentDialog()), fill(d(68), 0)); card.addView(divider(), fill(d(1), 0));
        card.addView(settingsRow(t("Corner radius", "دائرية الحواف"), String.valueOf(prefs.getInt("corner_int", 26)), "‹", v -> showNumberDialog("corner_int", t("Corner radius", "دائرية الحواف"), 8, 48, 26)), fill(d(68), 0)); card.addView(divider(), fill(d(1), 0));
        card.addView(settingsRow(t("Glass opacity", "شفافية الزجاج"), prefs.getInt("opacity_int", 76) + "%", "‹", v -> showNumberDialog("opacity_int", t("Glass opacity", "شفافية الزجاج"), 25, 100, 76)), fill(d(68), 0)); card.addView(divider(), fill(d(1), 0));
        Switch reduced = new Switch(this); reduced.setChecked(prefs.getBoolean("reduce_motion", false)); reduced.setOnCheckedChangeListener((b, checked) -> prefs.edit().putBoolean("reduce_motion", checked).apply()); card.addView(preferenceRow(t("Reduce motion", "تقليل الحركة"), t("Keep transitions subtle", "إبقاء الانتقالات هادئة"), reduced, v -> {}), fill(d(74), 0));
        root.addView(card, fill(-2, d(20)));
        Button studio = VitraUi.primary(this, t("Open widget studio", "فتح استوديو الويدجت")); studio.setOnClickListener(v -> navigate("editor")); root.addView(studio, fill(d(56), 0));
    }

    private void buildDateTime(LinearLayout root) {
        header(root, t("Date & time", "التاريخ والوقت"), true, false);
        LinearLayout card = glassCard();
        card.addView(settingsRow(t("Clock format", "تنسيق الساعة"), prefs.getBoolean("format_24", true) ? "24-hour" : "12-hour", "‹", v -> showClockFormatDialog()), fill(d(68), 0)); card.addView(divider(), fill(d(1), 0));
        card.addView(settingsRow(t("Numerals", "الأرقام"), numeralSummary(), "‹", v -> showNumeralDialog()), fill(d(68), 0)); card.addView(divider(), fill(d(1), 0));
        card.addView(settingsRow(t("System date & time", "تاريخ ووقت النظام"), t("Open Android settings", "فتح إعدادات Android"), "↗", v -> openSystemDateTime()), fill(d(68), 0)); root.addView(card, fill(-2, d(18)));
        TextView note = VitraUi.text(this, t("Vitra reads device time. Android controls the actual system clock and time zone.", "يقرأ Vitra وقت الجهاز. يتحكم Android بالساعة الفعلية والمنطقة الزمنية."), 13, VitraUi.MUTED, false); note.setLineSpacing(d(3), 1f); root.addView(note, fill(-2, 0));
    }

    private void buildWeather(LinearLayout root) {
        header(root, t("Weather", "الطقس"), true, false);
        TextView status = VitraUi.text(this, prefs.getString("weather_temp", "—") + " · " + prefs.getString("weather_desc", t("Not updated yet", "لم يتم التحديث بعد")), 16, VitraUi.TEXT, true); root.addView(status, fill(d(32), d(12)));
        LinearLayout cityCard = glassCard();
        cityCard.addView(settingsRow(t("City", "المدينة"), prefs.getString("city_name", t("Sana'a", "صنعاء")), "‹", v -> showCityDialog()), fill(d(68), 0)); cityCard.addView(divider(), fill(d(1), 0));
        cityCard.addView(settingsRow(t("Use my location", "استخدام موقعي"), t("Requests Android location permission", "يطلب إذن الموقع في Android"), "◎", v -> requestLocation()), fill(d(68), 0)); root.addView(cityCard, fill(-2, d(16)));
        Button refresh = VitraUi.primary(this, t("Refresh weather now", "تحديث الطقس الآن")); refresh.setOnClickListener(v -> { DataRepository.refresh(this, true); toast(t("Weather refresh started", "بدأ تحديث الطقس")); }); root.addView(refresh, fill(d(56), 0));
    }

    private void buildPrayer(LinearLayout root) {
        header(root, t("Prayer times", "أوقات الصلاة"), true, false);
        LinearLayout now = glassCard();
        TextView next = VitraUi.text(this, t("Next prayer", "الصلاة القادمة") + " · " + prefs.getString("prayer_next", "—"), 14, VitraUi.MUTED, false); now.addView(next, fill(d(28), d(4)));
        TextView time = VitraUi.text(this, prefs.getString("prayer_time", "—"), 34, VitraUi.TEXT, true); now.addView(time, fill(d(48), d(4)));
        TextView dates = VitraUi.text(this, "Fajr " + prefs.getString("prayer_fajr", "—") + "   ·   Dhuhr " + prefs.getString("prayer_dhuhr", "—") + "   ·   Maghrib " + prefs.getString("prayer_maghrib", "—"), 12, VitraUi.MUTED, false); now.addView(dates, fill(d(30), 0)); root.addView(now, fill(d(120), d(16)));
        LinearLayout settings = glassCard();
        settings.addView(settingsRow(t("Calculation method", "طريقة الحساب"), prefs.getString("prayer_method_name", "Umm al-Qura"), "‹", v -> showPrayerMethodDialog()), fill(d(68), 0)); settings.addView(divider(), fill(d(1), 0));
        settings.addView(settingsRow(t("Location", "الموقع"), prefs.getString("city_name", t("Sana'a", "صنعاء")), "‹", v -> navigate("weather")), fill(d(68), 0)); root.addView(settings, fill(-2, d(16)));
        Button refresh = VitraUi.primary(this, t("Refresh prayer times", "تحديث مواقيت الصلاة")); refresh.setOnClickListener(v -> { DataRepository.refresh(this, true); toast(t("Prayer refresh started", "بدأ تحديث المواقيت")); }); root.addView(refresh, fill(d(56), 0));
    }

    private void buildSync(LinearLayout root) {
        header(root, t("Live updates", "التحديث المباشر"), true, false);
        LinearLayout card = glassCard();
        Switch live = new Switch(this); live.setChecked(prefs.getBoolean("live_updates", true)); live.setOnCheckedChangeListener((b, checked) -> prefs.edit().putBoolean("live_updates", checked).apply()); card.addView(preferenceRow(t("Enable live refresh", "تمكين التحديث المباشر"), t("Weather and prayer data are refreshed when the app or a related widget updates.", "تُحدّث بيانات الطقس والصلاة عند فتح التطبيق أو تحديث الويدجت المرتبطة."), live, v -> {}), fill(d(94), 0)); card.addView(divider(), fill(d(1), 0));
        Switch battery = new Switch(this); battery.setChecked(prefs.getBoolean("battery_saver", false)); battery.setOnCheckedChangeListener((b, checked) -> prefs.edit().putBoolean("battery_saver", checked).apply()); card.addView(preferenceRow(t("Battery saver", "توفير البطارية"), t("Reduce refresh activity", "تقليل نشاط التحديث"), battery, v -> {}), fill(d(76), 0)); root.addView(card, fill(-2, d(18)));
        Button now = VitraUi.primary(this, t("Sync now", "مزامنة الآن")); now.setOnClickListener(v -> { DataRepository.refresh(this, true); toast(t("Sync started", "بدأت المزامنة")); }); root.addView(now, fill(d(56), d(12)));
        Button system = VitraUi.secondary(this, t("Open app battery settings", "فتح إعدادات بطارية التطبيق")); system.setOnClickListener(v -> openAppDetails()); root.addView(system, fill(d(48), 0));
    }

    private void buildPermissions(LinearLayout root) {
        header(root, t("Permissions", "الأذونات"), true, false);
        TextView copy = VitraUi.text(this, t("Vitra works without location or notifications. Enabling them improves local weather, prayer timing and update reminders.", "يعمل Vitra بدون الموقع أو الإشعارات. تمكينهما يحسن الطقس المحلي ومواقيت الصلاة وتنبيهات التحديث."), 13, VitraUi.MUTED, false); copy.setLineSpacing(d(3), 1f); root.addView(copy, fill(-2, d(18)));
        LinearLayout card = glassCard();
        card.addView(settingsRow(t("Location", "الموقع"), locationGranted() ? t("Granted", "مسموح") : t("Not granted", "غير مسموح"), "◎", v -> requestLocation()), fill(d(70), 0)); card.addView(divider(), fill(d(1), 0));
        card.addView(settingsRow(t("Notifications", "الإشعارات"), notificationStatus(), "◌", v -> requestNotifications()), fill(d(70), 0)); card.addView(divider(), fill(d(1), 0));
        card.addView(settingsRow(t("App settings", "إعدادات التطبيق"), t("Manage all permissions in Android", "إدارة جميع الأذونات في Android"), "↗", v -> openAppDetails()), fill(d(70), 0)); root.addView(card, fill(-2, d(18)));
        TextView safe = VitraUi.text(this, t("No contacts, files, microphone or accessibility access is requested by Vitra.", "لا يطلب Vitra الوصول إلى جهات الاتصال أو الملفات أو الميكروفون أو خدمات إمكانية الوصول."), 13, VitraUi.PURPLE, true); safe.setLineSpacing(d(3), 1f); root.addView(safe, fill(-2, 0));
    }

    private void buildAbout(LinearLayout root) {
        header(root, t("About Vitra", "عن Vitra"), true, false);
        LinearLayout card = glassCard();
        TextView mark = VitraUi.text(this, "V", 45, Color.WHITE, true); mark.setGravity(Gravity.CENTER); mark.setBackground(VitraUi.circle(this, 0xff273866, true)); card.addView(mark, size(d(86), d(86), d(12)));
        TextView brand = VitraUi.text(this, "V I T R A", 22, VitraUi.TEXT, true); brand.setLetterSpacing(.16f); brand.setGravity(Gravity.CENTER); card.addView(brand, fill(d(34), d(4)));
        TextView desc = VitraUi.text(this, t("An original Android widget studio for clocks, live information, shortcuts and wallpapers.", "استوديو ويدجت Android أصلي للساعات والمعلومات الحية والاختصارات والخلفيات."), 14, VitraUi.MUTED, false); desc.setGravity(Gravity.CENTER); desc.setLineSpacing(d(3), 1f); card.addView(desc, fill(-2, d(15)));
        TextView version = VitraUi.text(this, "Version 1.2.0 · build 12", 12, VitraUi.MUTED, false); version.setGravity(Gravity.CENTER); card.addView(version, fill(d(24), 0)); root.addView(card, fill(-2, d(18)));
        Button share = VitraUi.secondary(this, t("Share Vitra", "مشاركة Vitra")); share.setOnClickListener(v -> shareApp()); root.addView(share, fill(d(50), d(10)));
        Button contact = VitraUi.secondary(this, t("Contact support", "تواصل مع الدعم")); contact.setOnClickListener(v -> emailSupport()); root.addView(contact, fill(d(50), 0));
    }

    private void buildTerms(LinearLayout root, boolean privacy) {
        header(root, privacy ? t("Privacy policy", "سياسة الخصوصية") : t("Terms of use", "شروط الاستخدام"), true, false);
        LinearLayout card = glassCard();
        String text;
        if (privacy) text = t("Vitra stores its widget choices and studio settings on this device. Weather and prayer refreshes send only the selected latitude and longitude to their configured public data services. Vitra does not collect contacts, messages, files or advertising identifiers. Before publishing, replace this in-app summary with the final hosted privacy-policy URL for your organisation.", "يحفظ Vitra اختيارات الويدجت وإعدادات الاستوديو على هذا الجهاز. ترسل تحديثات الطقس والصلاة خط العرض والطول المختارين فقط إلى خدمات البيانات العامة المهيأة. لا يجمع Vitra جهات الاتصال أو الرسائل أو الملفات أو معرّفات الإعلانات. قبل النشر، استبدل هذا الملخص داخل التطبيق برابط سياسة الخصوصية النهائي المستضاف لمنظمتك.");
        else text = t("Use Vitra's original content and artwork according to applicable law. Android, launcher behaviour, weather availability and prayer calculations can vary by device and region. The Pro catalogue state in this development build is a preview only; connect verified Google Play Billing products and final legal text before commercial release.", "استخدم محتوى وأعمال Vitra الأصلية وفق القوانين المعمول بها. قد يختلف Android وسلوك اللانشر وتوفر الطقس وحسابات الصلاة حسب الجهاز والمنطقة. حالة Pro في نسخة التطوير هذه للمعاينة فقط؛ اربط منتجات Google Play Billing الموثقة والنصوص القانونية النهائية قبل الإصدار التجاري.");
        TextView body = VitraUi.text(this, text, 15, VitraUi.TEXT, false); body.setLineSpacing(d(7), 1f); card.addView(body, fill(-2, d(18)));
        TextView date = VitraUi.text(this, t("Last updated: August 2026", "آخر تحديث: أغسطس 2026"), 12, VitraUi.MUTED, false); card.addView(date, fill(d(22), 0)); root.addView(card, fill(-2, 0));
    }

    private void buildTutorials(LinearLayout root) {
        header(root, t("Tutorials", "شروحات"), true, false);
        TextView copy = VitraUi.text(this, t("Quick, in-app walkthroughs for the features that require a launcher or Android permission.", "إرشادات سريعة داخل التطبيق للميزات التي تتطلب لانشر أو إذن Android."), 13, VitraUi.MUTED, false); copy.setLineSpacing(d(3), 1f); root.addView(copy, fill(-2, d(18)));
        addTutorialCard(root, "01", t("Add your first widget", "أضف أول ويدجت"), t("Choose a design → Add to home screen → complete placement in your launcher.", "اختر تصميماً ← أضف للشاشة الرئيسية ← أكمل الوضع في اللانشر."), v -> { selectedWidget = WidgetData.find("clock-classic"); navigate("detail"); });
        addTutorialCard(root, "02", t("Customize a glass design", "خصص تصميماً زجاجياً"), t("Change the background, opacity, blur and accent in the Widget Studio.", "غيّر الخلفية والشفافية والتمويه والإضاءة في الاستوديو."), v -> { selectedWidget = WidgetData.find("digital-insight"); navigate("editor"); });
        addTutorialCard(root, "03", t("Use a wallpaper", "استخدم خلفية"), t("Choose an original Vitra background or import a photo, then choose Home or Lock.", "اختر خلفية Vitra أصلية أو استورد صورة ثم اختر الرئيسية أو القفل."), v -> switchTab("wallpapers"));
        addTutorialCard(root, "04", t("Keep live data current", "حافظ على تحديث البيانات"), t("Set a city or allow location, then refresh weather and prayer times.", "حدد مدينة أو اسمح بالموقع ثم حدّث الطقس ومواقيت الصلاة."), v -> navigate("weather"));
    }

    private void addTutorialCard(LinearLayout root, String number, String title, String detail, View.OnClickListener listener) {
        LinearLayout card = glassCard(); card.setGravity(Gravity.CENTER_VERTICAL); card.setOrientation(LinearLayout.HORIZONTAL); card.setPadding(d(16), d(12), d(16), d(12));
        TextView n = VitraUi.text(this, number, 23, VitraUi.PURPLE, true); n.setGravity(Gravity.CENTER); n.setBackground(VitraUi.circle(this, 0x332c4b9b, true)); card.addView(n, new LinearLayout.LayoutParams(d(52), d(52)));
        LinearLayout texts = VitraUi.column(this); texts.setPadding(d(12), 0, d(8), 0); TextView h = VitraUi.text(this, title, 16, VitraUi.TEXT, true); TextView p = VitraUi.text(this, detail, 12, VitraUi.MUTED, false); p.setLineSpacing(d(2), 1f); texts.addView(h, fill(d(25), d(2))); texts.addView(p, fill(-2, 0)); card.addView(texts, new LinearLayout.LayoutParams(0, -2, 1));
        TextView arrow = VitraUi.text(this, "‹", 32, VitraUi.TEXT, false); arrow.setGravity(Gravity.CENTER); card.addView(arrow, new LinearLayout.LayoutParams(d(28), d(52))); card.setOnClickListener(listener); root.addView(card, fill(d(90), d(12)));
    }

    private void buildPro(LinearLayout root) {
        header(root, "Vitra Pro", true, false);
        LinearLayout hero = glassCard(); hero.setGravity(Gravity.CENTER_HORIZONTAL); hero.setPadding(d(18), d(24), d(18), d(20));
        TextView crown = VitraUi.text(this, "♛", 52, VitraUi.GOLD, false); crown.setGravity(Gravity.CENTER); hero.addView(crown, fill(d(62), d(6)));
        TextView title = VitraUi.text(this, t("Explore the Pro collection", "استكشف مجموعة Pro"), 25, VitraUi.TEXT, true); title.setGravity(Gravity.CENTER); hero.addView(title, fill(d(38), d(6)));
        String checks = "✓  " + t("Advanced clock and prayer designs", "تصاميم ساعات وصلاة متقدمة") + "\n✓  " + t("More customization presets", "إعدادات تخصيص أكثر") + "\n✓  " + t("No advertising surface in Vitra", "لا توجد مساحة إعلانية في Vitra") + "\n✓  " + t("Future collection updates", "تحديثات مجموعات مستقبلية");
        TextView benefits = VitraUi.text(this, checks, 15, VitraUi.TEXT, false); benefits.setLineSpacing(d(7), 1f); hero.addView(benefits, fill(-2, d(18)));
        Button preview = VitraUi.button(this, t("Enable development preview", "تفعيل معاينة التطوير"), 16, Color.BLACK, VitraUi.gradient(this, VitraUi.GOLD, 0xffffd37e, 21));
        preview.setOnClickListener(v -> { prefs.edit().putBoolean("pro_preview", true).apply(); toast(t("Pro preview enabled on this device", "تم تفعيل معاينة Pro على هذا الجهاز")); navigate("widgets"); }); hero.addView(preview, fill(d(56), d(10)));
        Button billing = VitraUi.secondary(this, t("What is needed before public billing?", "ما المطلوب قبل الدفع العام؟")); billing.setOnClickListener(v -> showBillingInfo()); hero.addView(billing, fill(d(48), 0)); root.addView(hero, fill(-2, d(18)));
        TextView note = VitraUi.text(this, t("This APK deliberately does not pretend to take payment. Before publishing a commercial edition, add your verified Google Play Billing product IDs and backend entitlement handling.", "لا تدّعي نسخة APK هذه تنفيذ دفع. قبل نشر إصدار تجاري، أضف معرّفات منتجات Google Play Billing الموثقة ومعالجة الاستحقاق في الخلفية."), 12, VitraUi.MUTED, false); note.setLineSpacing(d(3), 1f); root.addView(note, fill(-2, 0));
    }

    private void header(LinearLayout root, String title, boolean back, boolean menu) {
        LinearLayout header = VitraUi.row(this); header.setGravity(Gravity.CENTER_VERTICAL); header.setMinimumHeight(d(58));
        Button left = VitraUi.circleButton(this, back ? "‹" : "V"); left.setTextSize(back ? 34 : 20); left.setOnClickListener(v -> { if (back) goBack(); else switchTab("widgets"); });
        header.addView(left, new LinearLayout.LayoutParams(d(48), d(48)));
        TextView heading = VitraUi.text(this, title, 21, VitraUi.TEXT, true); heading.setGravity(Gravity.CENTER); if ("V I T R A".equals(title)) heading.setLetterSpacing(.14f); header.addView(heading, new LinearLayout.LayoutParams(0, d(52), 1));
        Button right = VitraUi.circleButton(this, menu ? "⋯" : ""); right.setTextSize(menu ? 28 : 17); right.setEnabled(menu); if (menu) right.setOnClickListener(v -> showMenu()); header.addView(right, new LinearLayout.LayoutParams(d(48), d(48)));
        root.addView(header, fill(d(58), d(4)));
    }

    private View buildBottomNav() {
        LinearLayout nav = VitraUi.row(this); nav.setPadding(d(8), d(7), d(8), d(7)); nav.setGravity(Gravity.CENTER); nav.setBackground(VitraUi.glass(this, 31, .98f, 0xff4b4b52));
        nav.addView(navItem("▦", t("Widgets", "ويدجت"), "widgets"), new LinearLayout.LayoutParams(0, -1, 1));
        nav.addView(navItem("▦", t("Icons", "أيقونات"), "icons"), new LinearLayout.LayoutParams(0, -1, 1));
        nav.addView(navItem("▧", t("Wallpapers", "خلفيات"), "wallpapers"), new LinearLayout.LayoutParams(0, -1, 1));
        return nav;
    }

    private View navItem(String symbol, String text, String target) {
        LinearLayout item = VitraUi.column(this); item.setGravity(Gravity.CENTER); item.setClickable(true); boolean active = target.equals(screen);
        TextView icon = VitraUi.text(this, symbol, 22, active ? VitraUi.BLUE : VitraUi.TEXT, true); icon.setGravity(Gravity.CENTER); item.addView(icon, fill(d(28), 0));
        TextView label = VitraUi.text(this, text, 12, active ? VitraUi.BLUE : VitraUi.TEXT, active); label.setGravity(Gravity.CENTER); item.addView(label, fill(d(21), 0)); item.setOnClickListener(v -> switchTab(target)); return item;
    }

    private void showMenu() {
        final Dialog dialog = new Dialog(this);
        LinearLayout panel = VitraUi.column(this); panel.setPadding(d(20), d(18), d(20), d(20)); panel.setBackground(VitraUi.glass(this, 30, 1f, 0xff5b5b60)); panel.setLayoutDirection(arabic ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        LinearLayout pro = VitraUi.row(this); pro.setGravity(Gravity.CENTER_VERTICAL); pro.setPadding(d(16), 0, d(12), 0); pro.setBackground(VitraUi.gradient(this, 0xff302719, 0xff3d2a17, 22)); TextView crown = VitraUi.text(this, "♛", 30, VitraUi.GOLD, true); pro.addView(crown, new LinearLayout.LayoutParams(d(48), d(60))); LinearLayout ptext = VitraUi.column(this); ptext.addView(VitraUi.text(this, "Vitra Pro", 17, VitraUi.TEXT, true), fill(d(26), 0)); ptext.addView(VitraUi.text(this, t("Explore the extended collection", "استكشف المجموعة الموسعة"), 11, 0xffdfd1b6, false), fill(d(20), 0)); pro.addView(ptext, new LinearLayout.LayoutParams(0, d(60), 1)); TextView arr = VitraUi.text(this, "‹", 30, VitraUi.GOLD, false); arr.setGravity(Gravity.CENTER); pro.addView(arr, new LinearLayout.LayoutParams(d(28), d(60))); pro.setOnClickListener(v -> { dialog.dismiss(); navigate("pro"); }); panel.addView(pro, fill(d(60), d(14)));
        addMenuRow(panel, "♡", t("Favorites", "المفضلة"), v -> { dialog.dismiss(); navigate("favorites"); });
        addMenuRow(panel, "◫", t("About Vitra", "عن التطبيق"), v -> { dialog.dismiss(); navigate("about"); });
        addMenuRow(panel, "☆", t("Rate / send feedback", "تقييم التطبيق وإرسال رأي"), v -> { dialog.dismiss(); showFeedbackDialog(); });
        addMenuRow(panel, "⌯", t("Share with friends", "شارك مع أصدقائك"), v -> { dialog.dismiss(); shareApp(); });
        addMenuRow(panel, "▦", t("Tutorials", "شروحات"), v -> { dialog.dismiss(); navigate("tutorials"); });
        addMenuRow(panel, "?", t("Contact support", "تواصل ودعم"), v -> { dialog.dismiss(); emailSupport(); });
        addMenuRow(panel, "⚙", t("Settings", "الإعدادات"), v -> { dialog.dismiss(); navigate("settings"); });
        dialog.setContentView(panel); Window window = dialog.getWindow(); if (window != null) { window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)); window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * .91f), WindowManager.LayoutParams.WRAP_CONTENT); window.setGravity(arabic ? Gravity.RIGHT | Gravity.TOP : Gravity.LEFT | Gravity.TOP); window.getAttributes().y = d(28); }
        dialog.show(); if (dialog.getWindow() != null) dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * .91f), WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void addMenuRow(LinearLayout parent, String icon, String title, View.OnClickListener listener) {
        LinearLayout row = VitraUi.row(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(d(10), 0, d(10), 0); TextView i = VitraUi.text(this, icon, 24, VitraUi.TEXT, false); i.setGravity(Gravity.CENTER); row.addView(i, new LinearLayout.LayoutParams(d(44), d(58))); TextView label = VitraUi.text(this, title, 17, VitraUi.TEXT, false); row.addView(label, new LinearLayout.LayoutParams(0, d(58), 1)); TextView arrow = VitraUi.text(this, "‹", 26, VitraUi.MUTED, false); arrow.setGravity(Gravity.CENTER); row.addView(arrow, new LinearLayout.LayoutParams(d(26), d(58))); row.setOnClickListener(listener); parent.addView(row, fill(d(58), 0)); parent.addView(divider(), fill(d(1), 0));
    }

    private void openWidget(WidgetData item) {
        selectedWidget = item; prefs.edit().putString("last_widget", item.id).apply(); navigate("detail");
    }

    private void requestPin(WidgetData item) {
        prefs.edit().putString("pending_widget_style", item.id).apply();
        Class<?> provider = ClockWidgetProvider.class;
        if ("Date".equals(item.category)) provider = DateWidgetProvider.class;
        else if ("Weather".equals(item.category)) provider = WeatherWidgetProvider.class;
        else if ("Prayer".equals(item.category)) provider = PrayerWidgetProvider.class;
        else if ("Search".equals(item.category) || "Apps".equals(item.category) || "People".equals(item.category)) provider = SearchWidgetProvider.class;
        else if ("System".equals(item.category) || "Quotes".equals(item.category) || "Counters".equals(item.category) || "Sports".equals(item.category)) provider = SystemWidgetProvider.class;
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        ComponentName component = new ComponentName(this, provider);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.isRequestPinAppWidgetSupported()) {
            try { manager.requestPinAppWidget(component, null, null); toast(t("Choose a place in your launcher to finish adding the widget", "اختر موضعاً في اللانشر لإتمام إضافة الويدجت")); }
            catch (RuntimeException e) { showAddInstructions(); }
        } else showAddInstructions();
    }

    private void showAddInstructions() {
        new AlertDialog.Builder(this).setTitle(t("Add widget", "إضافة ويدجت")).setMessage(t("Your launcher does not expose the one-tap placement flow. Long-press an empty area on your home screen, choose Widgets, then find Vitra.", "لا يوفر اللانشر مسار الوضع بنقرة واحدة. اضغط مطولاً على مساحة فارغة في الشاشة الرئيسية، ثم اختر ويدجت وابحث عن Vitra.")).setPositiveButton(t("OK", "حسناً"), null).show();
    }

    private void chooseWallpaperPhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); intent.setType("image/*"); intent.addCategory(Intent.CATEGORY_OPENABLE); intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION); startActivityForResult(intent, REQUEST_WALLPAPER);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WALLPAPER && resultCode == RESULT_OK && data != null && data.getData() != null) {
            importedWallpaper = data.getData();
            try { getContentResolver().takePersistableUriPermission(importedWallpaper, data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (SecurityException ignored) { }
            prefs.edit().putString("custom_wallpaper_uri", importedWallpaper.toString()).apply(); navigate("wallpaper_detail");
        }
    }

    private void applyWallpaper(int flag) {
        try {
            Bitmap bitmap = wallpaperBitmap();
            if (bitmap == null) { toast(t("Could not read that image", "تعذر قراءة هذه الصورة")); return; }
            WallpaperManager.getInstance(this).setBitmap(bitmap, null, true, flag);
            toast(flag == WallpaperManager.FLAG_LOCK ? t("Lock-screen wallpaper applied", "تم تعيين خلفية شاشة القفل") : t("Home-screen wallpaper applied", "تم تعيين خلفية الشاشة الرئيسية"));
        } catch (Exception error) { toast(t("Android could not apply the wallpaper", "تعذر على Android تعيين الخلفية")); }
    }

    private Bitmap wallpaperBitmap() {
        if (importedWallpaper != null) {
            try (InputStream stream = getContentResolver().openInputStream(importedWallpaper)) { return BitmapFactory.decodeStream(stream); } catch (Exception ignored) { }
        }
        return WallpaperPreviewView.bitmap(selectedWallpaper, 1080, 2400);
    }

    private void showLanguageDialog() {
        String[] labels = {"العربية", "English"}; int checked = arabic ? 0 : 1;
        new AlertDialog.Builder(this).setTitle(t("App language", "لغة التطبيق")).setSingleChoiceItems(labels, checked, (dialog, which) -> { arabic = which == 0; prefs.edit().putBoolean("arabic", arabic).apply(); dialog.dismiss(); render(); }).setNegativeButton(t("Cancel", "إلغاء"), null).show();
    }

    private void showClockFormatDialog() {
        String[] values = {"24-hour", "12-hour"}; new AlertDialog.Builder(this).setTitle(t("Clock format", "تنسيق الساعة")).setSingleChoiceItems(values, prefs.getBoolean("format_24", true) ? 0 : 1, (dialog, which) -> { prefs.edit().putBoolean("format_24", which == 0).apply(); dialog.dismiss(); render(); }).show();
    }

    private void showNumeralDialog() {
        String[] values = {t("Automatic", "تلقائي"), "123", "١٢٣"}; String mode = prefs.getString("numeral_mode", "auto"); int checked = "arabic".equals(mode) ? 2 : "latin".equals(mode) ? 1 : 0; new AlertDialog.Builder(this).setTitle(t("Numerals", "الأرقام")).setSingleChoiceItems(values, checked, (dialog, which) -> { prefs.edit().putString("numeral_mode", which == 2 ? "arabic" : which == 1 ? "latin" : "auto").apply(); dialog.dismiss(); render(); }).show();
    }

    private void showCityDialog() {
        String[] labels = new String[DataRepository.CITIES.length]; for (int i = 0; i < labels.length; i++) labels[i] = DataRepository.CITIES[i][0];
        new AlertDialog.Builder(this).setTitle(t("Choose city", "اختر مدينة")).setItems(labels, (dialog, which) -> { String[] city = DataRepository.CITIES[which]; prefs.edit().putString("city_name", city[0]).putLong("city_lat", Double.doubleToLongBits(Double.parseDouble(city[1]))).putLong("city_lon", Double.doubleToLongBits(Double.parseDouble(city[2]))).apply(); DataRepository.refresh(this, true); toast(t("City saved; refreshing live data", "تم حفظ المدينة وتحديث البيانات")); render(); }).show();
    }

    private void showPrayerMethodDialog() {
        String[] labels = {"Umm al-Qura", "Muslim World League", "Egyptian General Authority", "Karachi"}; int[] codes = {4, 3, 5, 1};
        new AlertDialog.Builder(this).setTitle(t("Calculation method", "طريقة الحساب")).setItems(labels, (dialog, which) -> { prefs.edit().putInt("prayer_method", codes[which]).putString("prayer_method_name", labels[which]).apply(); DataRepository.refresh(this, true); toast(t("Method saved; refreshing prayer times", "تم حفظ الطريقة وتحديث المواقيت")); render(); }).show();
    }

    private void showAccentDialog() {
        String[] names = {t("White", "أبيض"), t("Purple", "بنفسجي"), t("Blue", "أزرق"), t("Green", "أخضر"), t("Pink", "وردي"), t("Gold", "ذهبي")}; int[] colors = {Color.WHITE, VitraUi.PURPLE, VitraUi.BLUE, 0xff7cd69a, 0xffff7fa8, VitraUi.GOLD};
        new AlertDialog.Builder(this).setTitle(t("Accent color", "لون الإضاءة")).setItems(names, (dialog, which) -> { prefs.edit().putInt("accent", colors[which]).apply(); render(); }).show();
    }

    private void showNumberDialog(final String key, String title, int min, int max, int fallback) {
        LinearLayout box = VitraUi.column(this); box.setPadding(d(22), d(10), d(22), 0); TextView value = VitraUi.text(this, "", 19, Color.WHITE, true); value.setGravity(Gravity.CENTER); SeekBar bar = new SeekBar(this); int current = prefs.getInt(key, fallback); bar.setMax(max - min); bar.setProgress(current - min); value.setText(String.valueOf(current)); bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { public void onProgressChanged(SeekBar seek, int progress, boolean fromUser) { value.setText(String.valueOf(progress + min)); } public void onStartTrackingTouch(SeekBar seek) {} public void onStopTrackingTouch(SeekBar seek) {} }); box.addView(value, new LinearLayout.LayoutParams(-1, d(40))); box.addView(bar, new LinearLayout.LayoutParams(-1, d(48)));
        new AlertDialog.Builder(this).setTitle(title).setView(box).setPositiveButton(t("Save", "حفظ"), (dialog, which) -> { prefs.edit().putInt(key, bar.getProgress() + min).apply(); render(); }).setNegativeButton(t("Cancel", "إلغاء"), null).show();
    }

    private void requestLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !locationGranted()) { requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION); return; }
        saveLastLocation();
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQUEST_LOCATION) { if (locationGranted()) saveLastLocation(); else toast(t("Location permission was not granted", "لم يتم منح إذن الموقع")); }
        else if (requestCode == REQUEST_NOTIFICATIONS) toast(notificationStatus());
        render();
    }

    @SuppressLint("MissingPermission") private void saveLastLocation() {
        try {
            android.location.LocationManager manager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
            android.location.Location location = manager == null ? null : manager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
            if (location == null && manager != null) location = manager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
            if (location == null) { toast(t("No recent device location is available. Choose a city instead.", "لا يتوفر موقع حديث للجهاز. اختر مدينة بدلاً من ذلك.")); return; }
            prefs.edit().putString("city_name", t("My location", "موقعي")).putLong("city_lat", Double.doubleToLongBits(location.getLatitude())).putLong("city_lon", Double.doubleToLongBits(location.getLongitude())).apply(); DataRepository.refresh(this, true); toast(t("Location saved; refreshing live data", "تم حفظ الموقع وتحديث البيانات")); render();
        } catch (SecurityException e) { toast(t("Android blocked location access", "حجب Android الوصول إلى الموقع")); }
    }

    private void requestNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS); else openNotificationSettings();
    }

    private void openNotificationSettings() { try { Intent i = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS); i.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()); startActivity(i); } catch (Exception e) { openAppDetails(); } }
    private void openSystemDateTime() { try { startActivity(new Intent(Settings.ACTION_DATE_SETTINGS)); } catch (Exception e) { openAppDetails(); } }
    private void openAppDetails() { Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())); startActivity(i); }
    private boolean locationGranted() { return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED; }
    private String notificationStatus() { if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return t("Managed in Android settings", "تُدار من إعدادات Android"); return checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED ? t("Granted", "مسموح") : t("Not granted", "غير مسموح"); }

    private void showFeedbackDialog() {
        EditText note = new EditText(this); note.setHint(t("Tell us what needs improvement", "أخبرنا بما يحتاج إلى تحسين")); note.setMinLines(4); note.setPadding(d(20), d(12), d(20), d(12)); new AlertDialog.Builder(this).setTitle(t("Feedback", "ملاحظات")).setView(note).setPositiveButton(t("Send", "إرسال"), (dialog, which) -> emailSupport(note.getText().toString())).setNegativeButton(t("Cancel", "إلغاء"), null).show();
    }

    private void showBillingInfo() { new AlertDialog.Builder(this).setTitle(t("Before public billing", "قبل الدفع العام")).setMessage(t("Create products in Google Play Console, add the Play Billing Library, verify purchases, restore entitlements and publish your final privacy policy. This build keeps the Pro collection testable without fabricating a transaction.", "أنشئ المنتجات في Google Play Console، وأضف مكتبة Play Billing، وتحقق من المشتريات، واستعد الاستحقاقات، وانشر سياسة الخصوصية النهائية. تبقي هذه النسخة مجموعة Pro قابلة للاختبار دون اختلاق عملية دفع.")).setPositiveButton(t("OK", "حسناً"), null).show(); }

    private void emailSupport() { emailSupport(""); }
    private void emailSupport(String body) { try { Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@vitra.app")); intent.putExtra(Intent.EXTRA_SUBJECT, "Vitra feedback"); intent.putExtra(Intent.EXTRA_TEXT, body); startActivity(Intent.createChooser(intent, t("Contact support", "تواصل مع الدعم"))); } catch (Exception e) { toast(t("No email app is available", "لا يتوفر تطبيق بريد إلكتروني")); } }
    private void shareApp() { Intent intent = new Intent(Intent.ACTION_SEND); intent.setType("text/plain"); intent.putExtra(Intent.EXTRA_TEXT, t("Try Vitra — an original Android glass widget studio.", "جرّب Vitra — استوديو ويدجت زجاجية أصلي لنظام Android.")); startActivity(Intent.createChooser(intent, t("Share Vitra", "مشاركة Vitra"))); }

    private Set<String> favorites() { return new HashSet<>(prefs.getStringSet("favorites", new HashSet<>())); }
    private boolean isFavorite(String id) { return favorites().contains(id); }
    private void toggleFavorite(WidgetData item) { Set<String> values = favorites(); boolean now; if (values.contains(item.id)) { values.remove(item.id); now = false; } else { values.add(item.id); now = true; } prefs.edit().putStringSet("favorites", values).apply(); toast(now ? t("Saved to favorites", "تم الحفظ في المفضلة") : t("Removed from favorites", "تمت الإزالة من المفضلة")); render(); }

    private void navigate(String target) { if (!target.equals(screen)) history.add(screen); screen = target; render(); }
    private void switchTab(String target) { screen = target; history.clear(); render(); }
    private void goBack() { if (history.isEmpty()) { switchTab("widgets"); return; } screen = history.remove(history.size() - 1); render(); }
    @Override public void onBackPressed() { if (!history.isEmpty()) goBack(); else if (!"widgets".equals(screen)) switchTab("widgets"); else super.onBackPressed(); }

    private LinearLayout glassCard() { LinearLayout card = VitraUi.column(this); card.setPadding(d(16), d(14), d(16), d(14)); card.setBackground(VitraUi.glass(this, 25, .98f, VitraUi.BORDER)); return card; }
    private View divider() { View v = new View(this); v.setBackgroundColor(0xff313135); return v; }
    private LinearLayout settingsRow(String title, String summary, String action, View.OnClickListener click) { LinearLayout row = VitraUi.row(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(d(13), 0, d(8), 0); LinearLayout labels = VitraUi.column(this); TextView h = VitraUi.text(this, title, 17, VitraUi.TEXT, true); labels.addView(h, fill(d(29), summary.isEmpty() ? 0 : d(1))); if (!summary.isEmpty()) { TextView s = VitraUi.text(this, summary, 12, VitraUi.MUTED, false); s.setMaxLines(1); s.setEllipsize(android.text.TextUtils.TruncateAt.END); labels.addView(s, fill(d(20), 0)); } row.addView(labels, new LinearLayout.LayoutParams(0, -1, 1)); TextView a = VitraUi.text(this, action, 24, VitraUi.MUTED, false); a.setGravity(Gravity.CENTER); row.addView(a, new LinearLayout.LayoutParams(d(42), -1)); row.setOnClickListener(click); return row; }
    private LinearLayout preferenceRow(String title, String summary, View control, View.OnClickListener click) { LinearLayout row = VitraUi.row(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(d(13), 0, d(8), 0); LinearLayout labels = VitraUi.column(this); labels.addView(VitraUi.text(this, title, 17, VitraUi.TEXT, true), fill(d(29), d(1))); TextView s = VitraUi.text(this, summary, 12, VitraUi.MUTED, false); s.setLineSpacing(d(2), 1f); labels.addView(s, fill(-2, 0)); row.addView(labels, new LinearLayout.LayoutParams(0, -1, 1)); row.addView(control, new LinearLayout.LayoutParams(d(62), -2)); row.setOnClickListener(click); return row; }
    private LinearLayout slider(String label, int min, int max, int current, NumberChanged changed) { LinearLayout block = VitraUi.column(this); LinearLayout head = VitraUi.row(this); TextView h = VitraUi.text(this, label, 14, VitraUi.TEXT, true); TextView value = VitraUi.text(this, String.valueOf(current), 13, VitraUi.MUTED, true); value.setGravity(Gravity.CENTER_VERTICAL | Gravity.END); head.addView(h, new LinearLayout.LayoutParams(0, d(24), 1)); head.addView(value, new LinearLayout.LayoutParams(d(48), d(24))); SeekBar bar = new SeekBar(this); bar.setMax(max - min); bar.setProgress(current - min); bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { public void onProgressChanged(SeekBar seek, int progress, boolean fromUser) { int v = progress + min; value.setText(String.valueOf(v)); changed.change(v); } public void onStartTrackingTouch(SeekBar seek) {} public void onStopTrackingTouch(SeekBar seek) {} }); block.addView(head); block.addView(bar, fill(d(34), 0)); return block; }
    private interface NumberChanged { void change(int value); }
    private TextView section(String value) { TextView view = VitraUi.text(this, value, 11, VitraUi.PURPLE, true); view.setLetterSpacing(.12f); return view; }
    private String localizedBackground(String value) { if (!arabic) return value; if ("Clear".equals(value)) return "شفاف"; if ("Fill".equals(value)) return "ممتلئ"; if ("Gradient".equals(value)) return "تدرّج"; return "صورة"; }
    private String categoryTitle(String value) { if (!arabic) return value; switch (value) { case "All": return "الكل"; case "New": return "جديد"; case "Clock": return "ساعة"; case "Digital": return "رقمي"; case "Date": return "تاريخ"; case "Weather": return "طقس"; case "Prayer": return "صلاة"; case "Quotes": return "اقتباسات"; case "System": return "نظام"; case "Apps": return "تطبيقات"; case "People": return "جهات"; case "Search": return "بحث"; case "Counters": return "عدادات"; case "Sports": return "رياضة"; default: return value; } }
    private String wallpaperCategoryTitle(String value) { if (!arabic) return value; if ("All".equals(value)) return "الكل"; if ("Dark".equals(value)) return "داكنة"; if ("Light".equals(value)) return "فاتحة"; if ("Colorful".equals(value)) return "ملونة"; return "طبيعة"; }
    private String collectionTitle(String value) { return value; }
    private String colorName(int color) { if (color == Color.WHITE) return t("White", "أبيض"); if (color == VitraUi.BLUE) return t("Blue", "أزرق"); if (color == VitraUi.GOLD) return t("Gold", "ذهبي"); if (color == 0xff7cd69a) return t("Green", "أخضر"); if (color == 0xffff7fa8) return t("Pink", "وردي"); return t("Purple", "بنفسجي"); }
    private String numeralSummary() { String mode = prefs.getString("numeral_mode", "auto"); return "arabic".equals(mode) ? "١٢٣" : "latin".equals(mode) ? "123" : t("Automatic", "تلقائي"); }
    private int activeAccent() { return prefs.getInt("accent", VitraUi.PURPLE); }
    private String t(String english, String arabicValue) { return arabic ? arabicValue : english; }
    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
    private int d(float value) { return VitraUi.dp(this, value); }
    private LinearLayout.LayoutParams fill(int height, int bottom) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, height); p.setMargins(0, 0, 0, bottom); return p; }
    private LinearLayout.LayoutParams size(int width, int height, int bottom) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width, height); p.gravity = Gravity.CENTER_HORIZONTAL; p.setMargins(0, 0, 0, bottom); return p; }
    private LinearLayout.LayoutParams weighted(float weight, int height, int end, int bottom) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, height, weight); p.setMargins(0, 0, end, bottom); return p; }
    private static final class SpaceFill extends View { SpaceFill(Context context) { super(context); } }
}
