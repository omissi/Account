package tech.alomessi.vitra;

import java.util.Arrays;
import java.util.List;

public final class WidgetData {
    public final String id, category, titleEn, titleAr, subtitleEn, subtitleAr, collection;
    public final boolean isNew, isPro;
    public final int style, span;

    public WidgetData(String id, String category, String titleEn, String titleAr,
                      String subtitleEn, String subtitleAr, String collection,
                      boolean isNew, boolean isPro, int style, int span) {
        this.id=id; this.category=category; this.titleEn=titleEn; this.titleAr=titleAr;
        this.subtitleEn=subtitleEn; this.subtitleAr=subtitleAr; this.collection=collection;
        this.isNew=isNew; this.isPro=isPro; this.style=style; this.span=span;
    }

    public String title(boolean ar){return ar?titleAr:titleEn;}
    public String subtitle(boolean ar){return ar?subtitleAr:subtitleEn;}

    public static final List<WidgetData> ALL = Arrays.asList(
        new WidgetData("clock-classic","Clock","Clock Classic","الساعة الكلاسيكية","Numbered glass dial","قرص زجاجي مرقّم","Chronos",true,false,0,1),
        new WidgetData("clock-modern","Clock","Clock Modern","الساعة العصرية","Layered modern dial","قرص عصري متعدد الطبقات","Chronos",true,false,1,1),
        new WidgetData("clock-minimal","Clock","Clock Minimal","الساعة البسيطة","Four-point watch face","واجهة ساعة بأربع نقاط","Chronos",false,false,2,1),
        new WidgetData("clock-fill","Clock","Clock Fill","الساعة الممتلئة","Soft filled watch face","واجهة ممتلئة ناعمة","Chronos",false,false,3,1),
        new WidgetData("clock-orbit","Clock","Clock Orbit","ساعة المدار","Twelve-dot dial","قرص مداري من 12 نقطة","Chronos",false,false,4,1),
        new WidgetData("clock-prism","Clock","Clock Prism","ساعة المنشور","Precision glass dial","قرص زجاجي دقيق","Chronos",false,false,5,1),
        new WidgetData("digital-world","Digital","Digital World","التوقيت العالمي","Two cities and time difference","مدينتان وفارق التوقيت","Chronos",true,false,6,2),
        new WidgetData("digital-glass-tall","Digital","Digital Glass","الوقت الزجاجي","Tall condensed numerals","أرقام زجاجية طويلة","Chronos",true,false,7,1),
        new WidgetData("digital-glass-wide","Digital","Digital Glass II","الوقت الزجاجي ٢","Wide glass numerals","أرقام زجاجية عريضة","Chronos",false,false,8,1),
        new WidgetData("digital-simple","Digital","Digital Simple","الوقت البسيط","Bold rounded time","وقت عريض بحواف ناعمة","Chronos",false,false,9,2),
        new WidgetData("insight-time","Digital","Insight Time","وقت الرؤية","Statement digital clock","ساعة رقمية بارزة","Insights",true,false,10,2),
        new WidgetData("year-dots","Date","Year Progress","تقدم السنة","A year rendered in dots","السنة ممثلة بالنقاط","Insights",false,false,11,2),
        new WidgetData("weekday-serif","Date","Weekday","يوم الأسبوع","Editorial serif weekday","يوم بخط تحريري","Insights",false,false,12,2),
        new WidgetData("calendar-focus","Date","Calendar Focus","تركيز التقويم","Day, month and full date","اليوم والشهر والتاريخ","Insights",false,false,13,2),
        new WidgetData("month-progress","Date","Month Progress","تقدم الشهر","Minimal monthly dots","نقاط تقدم الشهر","Midnight",false,false,14,1),
        new WidgetData("hijri-date","Date","Dual Calendar","التقويم المزدوج","Hijri and Gregorian date","هجري وميلادي","Midnight",true,false,15,2),
        new WidgetData("weather-midnight","Weather","Midnight Weather","طقس منتصف الليل","City, temperature and freshness","المدينة والحرارة وآخر تحديث","Midnight",true,false,16,2),
        new WidgetData("weather-orb","Weather","Weather Orb","كرة الطقس","Compact glass forecast","طقس زجاجي مدمج","Midnight",false,false,17,1),
        new WidgetData("prayer-calligraphy","Prayer","Prayer Calligraphy","مخطوطة الصلاة","Arabic prayer moment","لحظة الصلاة بخط عربي","Midnight",true,false,18,1),
        new WidgetData("prayer-next","Prayer","Next Prayer","الصلاة القادمة","Dhuhr, Asr and countdown","الظهر والعصر والعد التنازلي","Midnight",false,false,19,2),
        new WidgetData("search-vibrant","Search","Vibrant Search","البحث الحيوي","Search and quick actions","بحث وإجراءات سريعة","Vibrant",true,false,20,2),
        new WidgetData("apps-orbit","Apps","Apps Orbit","مدار التطبيقات","Four circular shortcuts","أربعة اختصارات دائرية","Vibrant",false,false,21,2),
        new WidgetData("people-glass","People","People Glass","جهات زجاجية","Favorite people shortcuts","اختصارات جهاتك المفضلة","Vibrant",false,false,22,2),
        new WidgetData("system-storage","System","Storage Glass","زجاج التخزين","Storage and battery progress","تقدم التخزين والبطارية","Midnight",false,false,23,2),
        new WidgetData("battery-ring","System","Battery Ring","حلقة البطارية","Circular system status","حالة النظام الدائرية","Insights",false,false,24,1),
        new WidgetData("quote-calm","Quotes","Calm Quote","اقتباس هادئ","Arabic wisdom and poetry","حكمة وشعر عربي","Midnight",false,false,25,2),
        new WidgetData("counter-dhikr","Counters","Dhikr Counter","عداد الذكر","Tap counter with haptics","عداد لمس مع اهتزاز","Midnight",true,false,26,1),
        new WidgetData("countdown-event","Countdown","Event Countdown","العد التنازلي","Days until your event","الأيام حتى مناسبتك","Insights",false,false,27,1),
        new WidgetData("sports-match","Sports","Match Center","مركز المباراة","Fixture, score and standings","المباراة والنتيجة والترتيب","Sports",true,false,28,2),
        new WidgetData("sports-search","Sports","Sports Search","بحث الرياضة","Match search bar","شريط بحث المباريات","Sports",false,false,29,2)
    );
}
