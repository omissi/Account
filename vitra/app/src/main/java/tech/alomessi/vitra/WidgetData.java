package tech.alomessi.vitra;

import java.util.Arrays;
import java.util.List;

public final class WidgetData {
    public final String id;
    public final String category;
    public final String titleEn;
    public final String titleAr;
    public final String subtitleEn;
    public final String subtitleAr;
    public final boolean isNew;
    public final boolean isPro;
    public final int style;

    public WidgetData(String id, String category, String titleEn, String titleAr,
                      String subtitleEn, String subtitleAr, boolean isNew, boolean isPro, int style) {
        this.id = id;
        this.category = category;
        this.titleEn = titleEn;
        this.titleAr = titleAr;
        this.subtitleEn = subtitleEn;
        this.subtitleAr = subtitleAr;
        this.isNew = isNew;
        this.isPro = isPro;
        this.style = style;
    }

    public String title(boolean ar) { return ar ? titleAr : titleEn; }
    public String subtitle(boolean ar) { return ar ? subtitleAr : subtitleEn; }

    public static final List<WidgetData> ALL = Arrays.asList(
        new WidgetData("clock-prism", "Clock", "Prism Clock", "ساعة المنشور", "Live glass dial", "قرص زجاجي حي", true, false, 0),
        new WidgetData("clock-orbit", "Clock", "Orbit Clock", "ساعة المدار", "Progressive day ring", "حلقة تقدم اليوم", false, true, 1),
        new WidgetData("digital-flow", "Digital", "Digital Flow", "التدفق الرقمي", "Bold adaptive time", "وقت متكيف وواضح", true, false, 2),
        new WidgetData("date-focus", "Date", "Date Focus", "التاريخ المركّز", "Calendar at a glance", "تقويم سريع وواضح", false, false, 3),
        new WidgetData("weather-aura", "Weather", "Weather Aura", "هالة الطقس", "Forecast and freshness", "توقعات وحالة التحديث", true, true, 4),
        new WidgetData("prayer-noor", "Prayer", "Noor Prayer", "مواقيت نور", "Regional prayer schedule", "مواقيت حسب منطقتك", false, false, 5),
        new WidgetData("search-glide", "Search", "Search Glide", "البحث السريع", "Reorderable quick actions", "إجراءات بحث قابلة للترتيب", false, false, 6),
        new WidgetData("system-pulse", "System", "System Pulse", "نبض النظام", "Battery and storage", "البطارية والتخزين", false, true, 7),
        new WidgetData("apps-lens", "Apps", "Apps Lens", "عدسة التطبيقات", "Expandable app folder", "مجلد تطبيقات قابل للتوسيع", true, true, 8),
        new WidgetData("sports-score", "Sports", "Score Glass", "زجاج النتائج", "Favorite teams first", "فرقك المفضلة أولًا", true, true, 9),
        new WidgetData("quote-calm", "Quotes", "Calm Quote", "اقتباس هادئ", "Daily Arabic and English", "عربي وإنجليزي يوميًا", false, false, 10),
        new WidgetData("calendar-arc", "Date", "Calendar Arc", "قوس التقويم", "Month progress", "تقدم الشهر", false, true, 11)
    );
}
