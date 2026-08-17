package tech.alomessi.vitra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Catalogue data for every Vitra widget preset. The launcher only needs a
 * small group of providers; every preset below is a usable configuration of
 * one of those providers, rather than a dead catalogue card.
 */
public final class WidgetData {
    public final String id, category, titleEn, titleAr, subtitleEn, subtitleAr, collection;
    public final boolean isNew, isPro;
    public final int style, span;

    public WidgetData(String id, String category, String titleEn, String titleAr,
                      String subtitleEn, String subtitleAr, String collection,
                      boolean isNew, boolean isPro, int style, int span) {
        this.id = id; this.category = category; this.titleEn = titleEn; this.titleAr = titleAr;
        this.subtitleEn = subtitleEn; this.subtitleAr = subtitleAr; this.collection = collection;
        this.isNew = isNew; this.isPro = isPro; this.style = style; this.span = span;
    }

    public String title(boolean arabic) { return arabic ? titleAr : titleEn; }
    public String subtitle(boolean arabic) { return arabic ? subtitleAr : subtitleEn; }

    private static WidgetData w(String id, String category, String en, String ar,
                                String enSub, String arSub, String collection,
                                boolean fresh, boolean pro, int style, int span) {
        return new WidgetData(id, category, en, ar, enSub, arSub, collection, fresh, pro, style, span);
    }

    public static final List<String> CATEGORIES = Collections.unmodifiableList(Arrays.asList(
            "All", "New", "Clock", "Digital", "Date", "Weather", "Prayer", "Quotes",
            "System", "Apps", "People", "Search", "Counters", "Sports"));

    public static final List<WidgetData> ALL = Collections.unmodifiableList(Arrays.asList(
            // Chronos — analog and digital time.
            w("clock-classic", "Clock", "Clock Classic", "ساعة كلاسيكية", "Numbered glass dial", "قرص زجاجي مرقّم", "Chronos", true, false, 0, 1),
            w("clock-modern", "Clock", "Clock Modern", "ساعة عصرية", "Layered modern dial", "قرص عصري متعدد الطبقات", "Chronos", true, false, 1, 1),
            w("clock-minimal", "Clock", "Clock Minimal", "ساعة بسيطة", "Four-point minimal dial", "واجهة بأربع نقاط", "Chronos", false, false, 2, 1),
            w("clock-fill", "Clock", "Clock Fill", "ساعة ممتلئة", "Soft filled face", "واجهة ممتلئة ناعمة", "Chronos", false, false, 3, 1),
            w("clock-orbit", "Clock", "Clock Orbit", "ساعة المدار", "Twelve-dot orbit", "قرص مداري من 12 نقطة", "Chronos", false, false, 4, 1),
            w("clock-prism", "Clock", "Clock Prism", "ساعة المنشور", "Precise glass dial", "قرص زجاجي دقيق", "Chronos", false, false, 5, 1),
            w("clock-arabesque", "Clock", "Arabesque Clock", "ساعة أرابيسك", "Islamic geometric face", "واجهة هندسية إسلامية", "Chronos", true, false, 6, 1),
            w("clock-mashreq", "Clock", "Mashreq Clock", "ساعة مشرق", "Eastern geometry dial", "قرص هندسي شرقي", "Chronos", false, false, 7, 1),
            w("clock-calligraphy", "Clock", "Thuluth Clock", "ساعة ثلث", "Arabic calligraphy face", "واجهة خط عربي", "Chronos", false, true, 8, 1),
            w("clock-calligraphy-ring", "Clock", "Calligraphy Ring", "ساعة نقش", "Circular Arabic pattern", "نقش عربي دائري", "Chronos", false, true, 9, 1),
            w("clock-western", "Clock", "Western Clock", "ساعة غربية", "High-contrast numbers", "أرقام عالية التباين", "Chronos", true, false, 10, 1),
            w("clock-classic-date", "Clock", "Classic Date", "ساعة كلاسيكية بالتاريخ", "Time, day and date", "وقت ويوم وتاريخ", "Chronos", false, false, 11, 1),
            w("clock-wide-24", "Clock", "Wide 24", "ساعة عريضة ٢٤", "Wide analog 24-hour face", "واجهة عريضة ٢٤ ساعة", "Chronos", true, true, 12, 2),
            w("clock-wide-arabic", "Clock", "Wide Arabic", "ساعة عريضة عربية", "Arabic numerals wide face", "واجهة أرقام عربية عريضة", "Chronos", true, true, 13, 2),
            w("digital-world", "Digital", "Digital World", "التوقيت العالمي", "Two cities and time difference", "مدينتان وفارق توقيت", "Chronos", true, false, 14, 2),
            w("digital-glass-tall", "Digital", "Digital Glass", "رقمي زجاجي", "Tall condensed numerals", "أرقام زجاجية طويلة", "Chronos", true, false, 15, 1),
            w("digital-glass-wide", "Digital", "Digital Glass II", "رقمي زجاجي عريض", "Wide glass numerals", "أرقام زجاجية عريضة", "Chronos", false, false, 16, 1),
            w("digital-simple", "Digital", "Digital Simple", "رقمي بسيط", "Bold rounded time", "وقت عريض بحواف ناعمة", "Chronos", false, false, 17, 2),
            w("digital-arabic", "Digital", "Arabic Digital", "ديجيتال عربي", "Arabic 12/24 numerals", "أرقام عربية ١٢/٢٤", "Chronos", true, true, 18, 2),
            w("digital-insight", "Digital", "Insight Time", "وقت الرؤية", "Statement digital clock", "ساعة رقمية بارزة", "Insights", true, false, 19, 2),

            // Insights and Midnight — date information.
            w("year-dots", "Date", "Year Progress", "تقدم السنة", "A year rendered in dots", "السنة ممثلة بالنقاط", "Insights", false, false, 20, 2),
            w("weekday-thuluth", "Date", "Thuluth Day", "أيام بخط ثلث", "Arabic calligraphic weekday", "يوم بخط عربي", "Insights", true, true, 21, 1),
            w("weekday-serif", "Date", "Editorial Weekday", "أيام إنجليزية", "Editorial serif weekday", "يوم بخط تحريري", "Insights", false, false, 22, 2),
            w("month-thuluth", "Date", "Thuluth Month", "شهور خط ثلث", "Arabic month card", "بطاقة شهر عربي", "Insights", false, true, 23, 1),
            w("month-editorial", "Date", "Editorial Month", "شهور إنجليزية", "Large editorial month", "شهر إنجليزي تحريري", "Insights", false, false, 24, 2),
            w("hijri-date", "Date", "Dual Calendar", "تقويم مزدوج", "Hijri and Gregorian date", "هجري وميلادي", "Midnight", true, false, 25, 2),
            w("calendar-focus", "Date", "Calendar Focus", "تقويم", "Monthly calendar focus", "تقويم شهري كامل", "Midnight", false, false, 26, 1),
            w("date-line", "Date", "Simple Line", "شريط بسيط", "Day, month and full date", "اليوم والشهر والتاريخ", "Midnight", false, false, 27, 2),
            w("date-weather", "Date", "Weather Line", "شريط طقس", "Date, weather and time", "تاريخ وطقس ووقت", "Midnight", true, false, 28, 2),
            w("date-simple", "Date", "Simple Date", "تقويم بسيط", "Large day and month", "يوم وشهر كبيران", "Insights", false, false, 29, 1),

            // Weather.
            w("weather-midnight", "Weather", "Midnight Weather", "طقس منتصف الليل", "City, temperature and freshness", "مدينة وحرارة وآخر تحديث", "Midnight", true, false, 30, 2),
            w("weather-orb", "Weather", "Weather Orb", "كرة الطقس", "Compact glass forecast", "طقس زجاجي مدمج", "Midnight", false, false, 31, 1),
            w("weather-wide", "Weather", "Weather Wide", "طقس عريض", "Temperature, forecast and date", "حرارة وتوقعات وتاريخ", "Midnight", false, true, 32, 2),
            w("weather-status", "Weather", "Weather Status", "حالة الطقس", "Minimal weather status", "حالة طقس مصغرة", "Insights", false, false, 33, 1),

            // Prayer.
            w("prayer-compact", "Prayer", "Compact Prayer", "صلاة مختصر", "Current prayer and countdown", "الصلاة الحالية والعد التنازلي", "Midnight", true, false, 34, 2),
            w("prayer-row", "Prayer", "Prayer Row", "Five prayers in one row", "الصلوات الخمس في صف", "Midnight", false, false, 35, 2),
            w("prayer-selected", "Prayer", "Selected Prayer", "Focused next prayer", "تركيز الصلاة القادمة", "Midnight", false, false, 36, 2),
            w("prayer-next", "Prayer", "Next Prayer", "الصلاة القادمة", "Five times with progress", "المواقيت مع تقدم الوقت", "Midnight", true, false, 37, 2),
            w("prayer-detail", "Prayer", "Prayer Detail", "صلاة تفصيل", "Detailed daily timings", "تفاصيل مواقيت اليوم", "Midnight", false, true, 38, 2),
            w("prayer-calligraphy", "Prayer", "Prayer Calligraphy", "مخطوطة الصلاة", "Arabic prayer moment", "لحظة الصلاة بخط عربي", "Midnight", true, true, 39, 1),
            w("prayer-ring", "Prayer", "Prayer Ring", "حلقة الصلاة", "Circular prayer countdown", "عد تنازلي دائري للصلاة", "Midnight", false, true, 40, 1),

            // Quotes and daily content.
            w("quote-hadith", "Quotes", "Hadith Quote", "اقتباسات أحاديث", "Daily hadith card", "بطاقة حديث يومية", "Midnight", false, false, 41, 2),
            w("quote-dhikr", "Quotes", "Dhikr Quote", "اقتباسات أذكار", "Morning and evening dhikr", "Midnight", false, false, 42, 2),
            w("quote-ayah", "Quotes", "Ayah Quote", "اقتباسات آيات", "Quranic verse card", "بطاقة آية قرآنية", "Midnight", true, true, 43, 2),
            w("quote-custom", "Quotes", "Custom Quote", "اقتباس خاص", "Write your own quotation", "اكتب اقتباسك الخاص", "Midnight", false, false, 44, 2),
            w("counter-dhikr", "Counters", "Dhikr Counter", "عداد الذكر", "Tap counter with haptics", "عداد لمس مع اهتزاز", "Midnight", true, false, 45, 1),
            w("countdown-event", "Counters", "Event Countdown", "العد التنازلي", "Days until your event", "الأيام حتى مناسبتك", "Insights", false, false, 46, 1),

            // Utilities / icon-oriented widgets.
            w("search-vibrant", "Search", "Vibrant Search", "البحث الحيوي", "Search and quick actions", "بحث وإجراءات سريعة", "Vibrant", true, false, 47, 2),
            w("apps-orbit", "Apps", "Apps Orbit", "مدار التطبيقات", "Four circular shortcuts", "أربعة اختصارات دائرية", "Vibrant", false, false, 48, 2),
            w("apps-glass", "Apps", "Apps Glass", "تطبيقات زجاجية", "Ten glass app icons", "عشرة أيقونات زجاجية", "Vibrant", true, true, 49, 2),
            w("people-glass", "People", "People Glass", "جهات زجاجية", "Favorite people shortcuts", "اختصارات جهاتك المفضلة", "Vibrant", false, false, 50, 2),
            w("system-storage", "System", "Storage Glass", "زجاج التخزين", "Storage and battery progress", "تقدم التخزين والبطارية", "Midnight", false, false, 51, 2),
            w("battery-ring", "System", "Battery Ring", "حلقة البطارية", "Circular system status", "حالة النظام الدائرية", "Insights", false, false, 52, 1),
            w("system-pulse", "System", "System Pulse", "نبض النظام", "Battery, storage and memory", "بطارية وتخزين وذاكرة", "Insights", true, false, 53, 2),
            w("sports-match", "Sports", "Match Center", "مركز المباراة", "Fixture, score and standings", "مباراة ونتيجة وترتيب", "Sports", true, false, 54, 2),
            w("sports-search", "Sports", "Sports Search", "بحث الرياضة", "Match search bar", "شريط بحث المباريات", "Sports", false, false, 55, 2)
    ));

    public static WidgetData find(String id) {
        for (WidgetData item : ALL) if (item.id.equals(id)) return item;
        return ALL.get(0);
    }

    public static List<WidgetData> inCategory(String category) {
        if ("All".equals(category)) return new ArrayList<>(ALL);
        List<WidgetData> result = new ArrayList<>();
        for (WidgetData item : ALL) if ("New".equals(category) ? item.isNew : item.category.equals(category)) result.add(item);
        return result;
    }

    private WidgetData() { throw new AssertionError("No instances"); }
}
