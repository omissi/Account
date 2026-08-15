package tech.alomessi.vitra;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;
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

public class MainActivity extends Activity {
    private static final int BG=0xff07090f,SURFACE=0xff131826,SURFACE_2=0xff1b2133,TEXT=0xfff5f7ff,MUTED=0xff9daac2,CYAN=0xff30e7ff,VIOLET=0xff9d5cff;
    private SharedPreferences prefs;
    private boolean arabic;
    private String screen="home";
    private WidgetData selected=WidgetData.ALL.get(0);
    private FrameLayout root;
    private LinearLayout page;
    private final Set<String> favorites=new HashSet<>();
    private int accent=CYAN,opacity=82,radius=28;

    @Override protected void onCreate(Bundle state){
        super.onCreate(state);configureWindow();prefs=getSharedPreferences("vitra",Context.MODE_PRIVATE);
        arabic=prefs.getBoolean("arabic",Locale.getDefault().getLanguage().equals("ar"));
        Set<String> saved=prefs.getStringSet("favorites",null);if(saved!=null)favorites.addAll(saved);
        accent=prefs.getInt("accent",CYAN);opacity=prefs.getInt("opacity_int",82);radius=prefs.getInt("corner_int",28);
        DataRepository.refresh(this,false);
        if(!prefs.getBoolean("onboarded",false))showOnboarding();else render("home");
    }

    private void configureWindow(){Window w=getWindow();w.setStatusBarColor(BG);w.setNavigationBarColor(BG);if(android.os.Build.VERSION.SDK_INT>=30){WindowInsetsController c=w.getInsetsController();if(c!=null)c.setSystemBarsAppearance(0,WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);}}

    private void showOnboarding(){
        root=new FrameLayout(this);root.setBackground(background(BG,0xff10162a,0));root.setPadding(dp(28),dp(36),dp(28),dp(28));
        LinearLayout box=column();box.setGravity(Gravity.CENTER_HORIZONTAL);root.addView(box,new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER));
        ImageView logo=new ImageView(this);logo.setImageResource(R.mipmap.ic_launcher);logo.setContentDescription("Vitra");box.addView(logo,new LinearLayout.LayoutParams(dp(132),dp(132)));
        TextView brand=label("VITRA",34,TEXT,true);brand.setGravity(Gravity.CENTER);box.addView(brand,matchWrap(dp(16),dp(2)));
        TextView lead=label("Glass widgets, made personal\nويدجتات زجاجية بطابعك الخاص",16,MUTED,false);lead.setGravity(Gravity.CENTER);box.addView(lead,matchWrap(dp(8),dp(34)));
        Button ar=primaryButton("ابدأ بالعربية");ar.setOnClickListener(v->finishOnboarding(true));box.addView(ar,matchHeight(dp(56),0,dp(10)));
        Button en=secondaryButton("Continue in English");en.setOnClickListener(v->finishOnboarding(false));box.addView(en,matchHeight(dp(56),0,dp(10)));
        TextView privacy=label("No account · No first-launch permissions\nلا يحتاج إلى تسجيل",12,MUTED,false);privacy.setGravity(Gravity.CENTER);box.addView(privacy,matchWrap(dp(18),0));applySystemInsets(root);setContentView(root);
    }
    private void finishOnboarding(boolean ar){arabic=ar;prefs.edit().putBoolean("arabic",ar).putBoolean("onboarded",true).apply();render("home");}

    private void render(String destination){
        screen=destination;root=new FrameLayout(this);root.setBackground(background(BG,0xff0b111d,0));root.setLayoutDirection(arabic?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipToPadding(false);scroll.setPadding(0,0,0,destination.equals("detail")||destination.equals("editor")?dp(20):dp(88));
        page=column();page.setPadding(dp(18),dp(14),dp(18),dp(30));scroll.addView(page,new ScrollView.LayoutParams(-1,-2));root.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        switch(destination){case"home":buildHome();break;case"wallpapers":buildWallpapers();break;case"favorites":buildFavorites();break;case"settings":buildSettings();break;case"detail":buildDetail();break;case"editor":buildEditor();break;}
        if(!destination.equals("detail")&&!destination.equals("editor"))addBottomNavigation();applySystemInsets(root);setContentView(root);
    }

    private void buildHome(){
        page.addView(brandHeader(),matchHeight(dp(58),0,dp(12)));page.addView(label(ar("مساحتك، بطريقتك","Your space, your way"),29,TEXT,true));page.addView(label(ar("استوديو متكامل للويدجتات والخلفيات","A complete widget and wallpaper studio"),14,MUTED,false),matchWrap(dp(6),dp(18)));
        EditText search=new EditText(this);search.setHint(ar("ابحث عن ساعة، طقس، صلاة...","Search clock, weather, prayer..."));search.setHintTextColor(MUTED);search.setTextColor(TEXT);search.setTextSize(14);search.setSingleLine(true);search.setPadding(dp(18),0,dp(18),0);search.setBackground(glass(22,.92f,accent));page.addView(search,matchHeight(dp(54),0,dp(14)));
        HorizontalScrollView chipScroll=new HorizontalScrollView(this);chipScroll.setHorizontalScrollBarEnabled(false);LinearLayout chips=row();String[] categories={"All","New","Clock","Digital","Date","Weather","Prayer","System"};for(String category:categories){Button chip=chip(displayCategory(category),category.equals("All"));chip.setTag(category);chips.addView(chip,wrapHeight(dp(38),0,dp(8)));}chipScroll.addView(chips);page.addView(chipScroll,matchWrap(0,dp(22)));page.addView(sectionTitle(ar("اختيارات مميزة","Featured collection")));
        GridLayout grid=new GridLayout(this);grid.setColumnCount(2);page.addView(grid,matchWrap(dp(12),dp(18)));populateGrid(grid,WidgetData.ALL);
        View.OnClickListener select=v->{String cat=String.valueOf(v.getTag());for(int i=0;i<chips.getChildCount();i++){View child=chips.getChildAt(i);child.setBackground(glass(18,.9f,cat.equals(child.getTag())?accent:Color.TRANSPARENT));}populateGrid(grid,filter(cat,search.getText().toString()));};for(int i=0;i<chips.getChildCount();i++)chips.getChildAt(i).setOnClickListener(select);
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){populateGrid(grid,filter("All",s.toString()));}public void afterTextChanged(Editable s){}});
    }

    private void buildWallpapers(){
        page.addView(brandHeader(),matchHeight(dp(58),0,dp(12)));page.addView(label(ar("الخلفيات الذكية","Smart wallpapers"),28,TEXT,true));page.addView(label(ar("اختبر التباين واختر لونًا متناسقًا بضغطة واحدة","Test contrast and apply a matching accent in one tap"),14,MUTED,false),matchWrap(dp(6),dp(20)));
        int[][] palettes={{0xff0b304b,0xff66226f},{0xff06393b,0xff196a51},{0xff542016,0xffba6632},{0xff111626,0xff414b78},{0xff241742,0xff823a9a},{0xff07354d,0xff1a89a9}};String[] names=arabic?new String[]{"ليل بنفسجي","غابة هادئة","غروب دافئ","حبر داكن","أورورا","محيط"}:new String[]{"Violet night","Calm forest","Warm dusk","Dark ink","Aurora","Ocean"};GridLayout grid=new GridLayout(this);grid.setColumnCount(2);page.addView(grid,matchWrap(0,dp(20)));
        for(int i=0;i<palettes.length;i++){final int index=i;LinearLayout card=column();card.setPadding(dp(14),dp(14),dp(14),dp(14));card.setBackground(background(palettes[i][0],palettes[i][1],26));card.addView(label("VITRA",13,TEXT,true));Space space=new Space(this);card.addView(space,new LinearLayout.LayoutParams(1,dp(118)));card.addView(label(names[i],15,TEXT,true));card.setContentDescription(names[i]);card.setOnClickListener(v->{int[] s={CYAN,0xff59e78e,0xffffbc48,Color.WHITE,VIOLET,0xff47c4ff};accent=s[index];saveStyle();toast(ar("تم تطبيق اللون المقترح","Suggested accent applied"));});addGridChild(grid,card,dp(190));}
    }

    private void buildFavorites(){page.addView(brandHeader(),matchHeight(dp(58),0,dp(12)));page.addView(label(ar("المفضلة","Favorites"),28,TEXT,true));List<WidgetData> items=new ArrayList<>();for(WidgetData d:WidgetData.ALL)if(favorites.contains(d.id))items.add(d);if(items.isEmpty()){TextView empty=label(ar("♡\nلا توجد مفضلات بعد\nاضغط القلب على أي تصميم للاحتفاظ به هنا","♡\nNo favorites yet\nTap the heart on a design to keep it here"),18,MUTED,false);empty.setGravity(Gravity.CENTER);page.addView(empty,matchWrap(dp(90),0));return;}GridLayout grid=new GridLayout(this);grid.setColumnCount(2);page.addView(grid,matchWrap(dp(20),0));populateGrid(grid,items);}

    private void buildSettings(){
        page.addView(brandHeader(),matchHeight(dp(58),0,dp(12)));page.addView(label(ar("الإعدادات","Settings"),28,TEXT,true));page.addView(smallHeader(ar("التجربة","EXPERIENCE")),matchWrap(dp(24),dp(8)));
        page.addView(settingRow(ar("اللغة","Language"),arabic?"العربية":"English",v->{arabic=!arabic;prefs.edit().putBoolean("arabic",arabic).apply();render("settings");}),matchHeight(dp(66),0,dp(8)));page.addView(switchRow(ar("تقليل الحركة","Reduced motion"),"reduced"),matchHeight(dp(66),0,dp(8)));page.addView(switchRow(ar("تحليلات اختيارية","Optional analytics"),"analytics"),matchHeight(dp(66),0,dp(18)));
        page.addView(smallHeader(ar("الموقع والبيانات","LOCATION & DATA")),matchWrap(0,dp(8)));page.addView(settingRow(ar("المدينة","City"),prefs.getString("city_name","صنعاء · Sana'a"),v->showCityDialog()),matchHeight(dp(66),0,dp(8)));page.addView(settingRow(ar("مصادر البيانات","Data sources"),"Open-Meteo · Aladhan",null),matchHeight(dp(66),0,dp(18)));
        page.addView(smallHeader(ar("الخصوصية والبيانات","PRIVACY & DATA")),matchWrap(0,dp(8)));page.addView(settingRow(ar("أذونات التطبيق","App permissions"),ar("تُطلب وقت الحاجة فقط","Requested just in time"),v->openAppSettings()),matchHeight(dp(66),0,dp(8)));page.addView(settingRow(ar("تحسين البطارية","Battery optimization"),ar("افتح إعدادات النظام","Open system settings"),v->openBatterySettings()),matchHeight(dp(66),0,dp(8)));page.addView(settingRow(ar("حذف البيانات المحلية","Clear local data"),ar("المفضلة والأنماط","Favorites and styles"),v->clearLocal()),matchHeight(dp(66),0,dp(18)));
        page.addView(smallHeader(ar("التشخيص","DIAGNOSTICS")),matchWrap(0,dp(8)));page.addView(settingRow(ar("حالة الويدجت","Widget status"),ar("سليم · تحديث النظام مفعّل","Healthy · system updates active"),null),matchHeight(dp(66),0,dp(8)));page.addView(settingRow(ar("الإصدار","Version"),"1.0.0 (10)",null),matchHeight(dp(66),0,dp(16)));
        LinearLayout about=column();about.setPadding(dp(18),dp(18),dp(18),dp(18));about.setBackground(glass(24,.9f,accent));about.addView(label("VITRA",22,TEXT,true));about.addView(label(ar("بواسطة ALOMESSI TECH\nدون تسجيل · خصوصية أولًا","By ALOMESSI TECH\nNo account · Privacy first"),13,MUTED,false),matchWrap(dp(6),0));page.addView(about);
    }

    private void buildDetail(){
        page.addView(backHeader(selected.title(arabic)),matchHeight(dp(58),0,dp(12)));page.addView(phonePreview(selected),matchHeight(dp(310),0,dp(18)));
        LinearLayout badges=row();badges.addView(chip(selected.isPro?"PRO":ar("مجاني","FREE"),true),wrapHeight(dp(34),0,dp(8)));badges.addView(chip("2×2 · 4×2",false),wrapHeight(dp(34),0,dp(8)));badges.addView(chip(ar("الرئيسية والقفل","HOME + LOCK"),false),wrapHeight(dp(34),0,0));page.addView(badges,matchWrap(0,dp(20)));
        page.addView(label(selected.title(arabic),28,TEXT,true));page.addView(label(selected.subtitle(arabic),14,MUTED,false),matchWrap(dp(6),dp(20)));Button edit=primaryButton(ar("تخصيص التصميم","Customize design"));edit.setOnClickListener(v->render("editor"));page.addView(edit,matchHeight(dp(56),0,dp(10)));
        LinearLayout actions=row();Button add=secondaryButton(ar("إضافة إلى الشاشة","Add to screen"));add.setOnClickListener(v->requestPin());actions.addView(add,new LinearLayout.LayoutParams(0,dp(52),1));Button fav=secondaryButton(favorites.contains(selected.id)?"♥":"♡");fav.setOnClickListener(v->{toggleFavorite(selected.id);render("detail");});actions.addView(fav,new LinearLayout.LayoutParams(dp(64),dp(52)));page.addView(actions,matchWrap(0,dp(24)));
        page.addView(sectionTitle(ar("معلومات التصميم","Design information")));page.addView(infoRow(ar("الأحجام","Sizes"),"2×2 · 4×1 · 4×2"),matchHeight(dp(56),dp(8),0));page.addView(infoRow(ar("التوافق","Compatibility"),"Android 8+"),matchHeight(dp(56),dp(8),0));page.addView(infoRow(ar("الأذونات","Permissions"),permissionLabel()),matchHeight(dp(56),dp(8),0));page.addView(infoRow(ar("حالة البيانات","Data status"),ar("تخزين محلي وحالة فارغة صادقة","Local cache and honest empty state")),matchHeight(dp(56),dp(8),0));
    }

    private void buildEditor(){
        page.addView(backHeader(ar("استوديو التخصيص","Customization studio")),matchHeight(dp(58),0,dp(12)));page.addView(phonePreview(selected),matchHeight(dp(278),0,dp(16)));
        LinearLayout quick=row();Button undo=secondaryButton("↶ "+ar("تراجع","Undo"));undo.setOnClickListener(v->{accent=prefs.getInt("accent",CYAN);opacity=prefs.getInt("opacity_int",82);radius=prefs.getInt("corner_int",28);render("editor");});quick.addView(undo,new LinearLayout.LayoutParams(0,dp(48),1));Button auto=primaryButton("✦ Auto Style");auto.setOnClickListener(v->{accent=autoAccent();opacity=84;radius=32;render("editor");});quick.addView(auto,new LinearLayout.LayoutParams(0,dp(48),1));page.addView(quick,matchWrap(0,dp(24)));
        page.addView(sectionTitle(ar("لون التوهج","Accent color")));LinearLayout colors=row();int[] palette={CYAN,VIOLET,0xffff5794,0xffffbc48,0xff59e78e,Color.WHITE};for(int color:palette){Button dot=new Button(this);dot.setText(accent==color?"✓":"");dot.setTextColor(BG);dot.setTextSize(13);dot.setBackground(circle(color,accent==color));dot.setContentDescription(ar("اختيار اللون","Select color"));dot.setOnClickListener(v->{accent=color;render("editor");});colors.addView(dot,new LinearLayout.LayoutParams(0,dp(48),1));}page.addView(colors,matchWrap(dp(10),dp(18)));
        page.addView(label(ar("شفافية الزجاج","Glass opacity"),16,TEXT,true));SeekBar alpha=new SeekBar(this);alpha.setMax(100);alpha.setProgress(opacity);alpha.setOnSeekBarChangeListener(seek(value->opacity=Math.max(25,value)));page.addView(alpha,matchWrap(dp(4),dp(14)));page.addView(label(ar("استدارة الزوايا","Corner radius"),16,TEXT,true));SeekBar corners=new SeekBar(this);corners.setMax(48);corners.setProgress(radius);corners.setOnSeekBarChangeListener(seek(value->radius=Math.max(8,value)));page.addView(corners,matchWrap(dp(4),dp(16)));
        page.addView(switchLine(ar("تكيّف مع الوضع الداكن والفاتح","Adapt to light and dark mode"),true),matchHeight(dp(58),0,dp(8)));page.addView(switchLine(ar("إظهار آخر تحديث","Show last update status"),true),matchHeight(dp(58),0,dp(8)));page.addView(switchLine(ar("استخدام أرقام عربية","Use Arabic numerals"),arabic),matchHeight(dp(58),0,dp(20)));Button save=primaryButton(ar("حفظ وإضافة إلى الشاشة","Save and add to screen"));save.setOnClickListener(v->{saveStyle();requestPin();});page.addView(save,matchHeight(dp(58),0,0));
    }

    private void populateGrid(GridLayout grid,List<WidgetData> items){grid.removeAllViews();for(WidgetData item:items)addGridChild(grid,widgetCard(item),dp(228));grid.requestLayout();}
    private LinearLayout widgetCard(WidgetData item){LinearLayout card=column();card.setPadding(dp(10),dp(10),dp(10),dp(12));card.setBackground(glass(24,.9f,Color.TRANSPARENT));card.setContentDescription(item.title(arabic)+". "+item.subtitle(arabic));card.addView(widgetPreview(item),matchHeight(dp(122),0,dp(10)));LinearLayout titleRow=row();titleRow.addView(label(item.title(arabic),15,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));TextView heart=label(favorites.contains(item.id)?"♥":"♡",20,favorites.contains(item.id)?0xffff5794:TEXT,false);heart.setGravity(Gravity.CENTER);heart.setOnClickListener(v->{toggleFavorite(item.id);heart.setText(favorites.contains(item.id)?"♥":"♡");});titleRow.addView(heart,new LinearLayout.LayoutParams(dp(38),dp(38)));card.addView(titleRow);card.addView(label(item.subtitle(arabic),11,MUTED,false));card.addView(label("2×2 · 4×2"+(item.isPro?" · PRO":""),10,accent,true),matchWrap(dp(6),0));card.setOnClickListener(v->{selected=item;render("detail");});return card;}
    private LinearLayout widgetPreview(WidgetData item){LinearLayout p=column();p.setGravity(Gravity.CENTER_VERTICAL);p.setPadding(dp(15),dp(10),dp(15),dp(10));p.setBackground(glass(radius,opacity/100f,accent));p.addView(label(item.category.toUpperCase(Locale.ROOT),10,accent,true));String one,two;if(item.category.equals("Clock")||item.category.equals("Digital")){one=new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date());two=new SimpleDateFormat("EEE, MMM d",Locale.getDefault()).format(new Date());}else if(item.category.equals("Date")){one=new SimpleDateFormat("dd MMM",Locale.getDefault()).format(new Date());two=new SimpleDateFormat("EEEE",Locale.getDefault()).format(new Date());}else if(item.category.equals("Weather")){one=prefs.getString("weather_temp","—°");two=prefs.getString("city_name",ar("صنعاء","Sana'a"))+" · Open-Meteo";}else if(item.category.equals("Prayer")){one=prefs.getString("prayer_next",ar("الصلاة القادمة","Next prayer"))+" "+prefs.getString("prayer_time","—:—");two=prefs.getString("city_name",ar("صنعاء","Sana'a"))+" · Aladhan";}else if(item.category.equals("Sports")){one="ARG  2 — 1  KSA";two=ar("فريقك المفضل","Your favorite team");}else if(item.category.equals("System")){one="84%";two=ar("البطارية · النظام سليم","Battery · system healthy");}else if(item.category.equals("Search")){one="G  ◉  ↗  ✦";two=ar("بحث سريع","Quick search");}else if(item.category.equals("Apps")){one="●  ◐  ✦  ◉";two=ar("مجلد التطبيقات","Apps folder");}else{one=ar("اصنع مساحة تشبهك","Make space feel yours");two="VITRA";}p.addView(label(one,(item.category.equals("Clock")||item.category.equals("Digital"))?28:22,TEXT,true),matchWrap(dp(4),0));p.addView(label(two,11,MUTED,false),matchWrap(dp(3),0));return p;}
    private LinearLayout phonePreview(WidgetData item){LinearLayout frame=column();frame.setPadding(dp(24),dp(24),dp(24),dp(24));frame.setGravity(Gravity.CENTER);frame.setBackground(background(0xff11334b,0xff5d225f,32));frame.addView(widgetPreview(item),new LinearLayout.LayoutParams(-1,dp(132)));return frame;}

    private LinearLayout brandHeader(){LinearLayout h=row();h.setGravity(Gravity.CENTER_VERTICAL);ImageView icon=new ImageView(this);icon.setImageResource(R.mipmap.ic_launcher);icon.setContentDescription("Vitra");h.addView(icon,new LinearLayout.LayoutParams(dp(48),dp(48)));h.addView(label("VITRA",24,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));TextView version=label("1.0",10,accent,true);version.setGravity(Gravity.CENTER);version.setBackground(glass(18,.9f,accent));h.addView(version,new LinearLayout.LayoutParams(dp(56),dp(34)));return h;}
    private LinearLayout backHeader(String title){LinearLayout h=row();h.setGravity(Gravity.CENTER_VERTICAL);Button b=secondaryButton("‹");b.setContentDescription(ar("رجوع","Back"));b.setOnClickListener(v->onBackPressed());h.addView(b,new LinearLayout.LayoutParams(dp(48),dp(48)));h.addView(label(title,22,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));return h;}
    private void addBottomNavigation(){LinearLayout nav=row();nav.setGravity(Gravity.CENTER);nav.setPadding(dp(8),dp(8),dp(8),dp(10));nav.setBackground(background(0xf70a0e18,0xf70a0e18,0));String[] ids={"home","wallpapers","favorites","settings"},ar={"الويدجت","الخلفيات","المفضلة","الإعدادات"},en={"Widgets","Wallpapers","Favorites","Settings"},icons={"▦","◒","♡","⚙"};for(int i=0;i<ids.length;i++){final String target=ids[i];Button b=new Button(this);b.setAllCaps(false);b.setText(icons[i]+"\n"+(arabic?ar[i]:en[i]));b.setTextSize(10);b.setTextColor(screen.equals(ids[i])?accent:MUTED);b.setGravity(Gravity.CENTER);b.setPadding(0,0,0,0);b.setBackgroundColor(Color.TRANSPARENT);b.setOnClickListener(v->render(target));nav.addView(b,new LinearLayout.LayoutParams(0,dp(66),1));}root.addView(nav,new FrameLayout.LayoutParams(-1,dp(76),Gravity.BOTTOM));}

    private List<WidgetData> filter(String category,String query){String q=query==null?"":query.trim().toLowerCase(Locale.ROOT);List<WidgetData> out=new ArrayList<>();for(WidgetData d:WidgetData.ALL){boolean cat=category.equals("All")||(category.equals("New")&&d.isNew)||d.category.equals(category);boolean txt=q.isEmpty()||d.title(false).toLowerCase(Locale.ROOT).contains(q)||d.title(true).contains(q)||d.category.toLowerCase(Locale.ROOT).contains(q);if(cat&&txt)out.add(d);}return out;}
    private String displayCategory(String v){if(!arabic)return v;switch(v){case"All":return"الكل";case"New":return"جديد";case"Clock":return"ساعة";case"Digital":return"رقمية";case"Date":return"تاريخ";case"Weather":return"طقس";case"Prayer":return"صلاة";case"System":return"النظام";default:return v;}}
    private void addGridChild(GridLayout grid,View child,int h){int w=(getResources().getDisplayMetrics().widthPixels-dp(54))/2;GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=w;p.height=h;p.setMargins(0,0,dp(9),dp(14));grid.addView(child,p);}
    private LinearLayout infoRow(String key,String value){LinearLayout r=row();r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(14),dp(8),dp(14),dp(8));r.setBackground(glass(18,.78f,Color.TRANSPARENT));r.addView(label(key,12,MUTED,false),new LinearLayout.LayoutParams(0,-2,.45f));TextView v=label(value,12,TEXT,true);v.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);r.addView(v,new LinearLayout.LayoutParams(0,-1,.55f));return r;}
    private LinearLayout settingRow(String title,String value,View.OnClickListener listener){LinearLayout r=column();r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(16),dp(9),dp(16),dp(9));r.setBackground(glass(18,.82f,Color.TRANSPARENT));r.addView(label(title,15,TEXT,true));r.addView(label(value,11,MUTED,false),matchWrap(dp(3),0));if(listener!=null){r.setClickable(true);r.setFocusable(true);r.setOnClickListener(listener);}return r;}
    private LinearLayout switchRow(String title,String key){LinearLayout r=row();r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(16),dp(8),dp(12),dp(8));r.setBackground(glass(18,.82f,Color.TRANSPARENT));r.addView(label(title,15,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));Switch s=new Switch(this);s.setChecked(prefs.getBoolean(key,false));s.setOnCheckedChangeListener((b,c)->prefs.edit().putBoolean(key,c).apply());r.addView(s);return r;}
    private LinearLayout switchLine(String title,boolean checked){LinearLayout r=row();r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(14),0,dp(8),0);r.setBackground(glass(16,.72f,Color.TRANSPARENT));r.addView(label(title,14,TEXT,false),new LinearLayout.LayoutParams(0,-2,1));Switch s=new Switch(this);s.setChecked(checked);r.addView(s);return r;}
    private TextView sectionTitle(String v){return label(v,20,TEXT,true);}private TextView smallHeader(String v){return label(v,11,accent,true);}
    private Button primaryButton(String v){Button b=new Button(this);b.setAllCaps(false);b.setText(v);b.setTextSize(15);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setTextColor(BG);b.setGravity(Gravity.CENTER);b.setBackground(background(accent,VIOLET,20));return b;}
    private Button secondaryButton(String v){Button b=new Button(this);b.setAllCaps(false);b.setText(v);b.setTextSize(14);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setTextColor(TEXT);b.setGravity(Gravity.CENTER);b.setBackground(glass(18,.88f,Color.TRANSPARENT));return b;}
    private Button chip(String v,boolean active){Button b=new Button(this);b.setAllCaps(false);b.setText(v);b.setTextSize(11);b.setTextColor(active?accent:TEXT);b.setPadding(dp(16),0,dp(16),0);b.setMinHeight(0);b.setMinWidth(0);b.setBackground(glass(18,.9f,active?accent:Color.TRANSPARENT));return b;}
    private TextView label(String v,float size,int color,boolean bold){TextView t=new TextView(this);t.setText(v);t.setTextSize(size);t.setTextColor(color);t.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));t.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);t.setLineSpacing(0,1.05f);return t;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);return l;}
    private GradientDrawable glass(int corner,float alpha,int border){GradientDrawable d=background(withAlpha(SURFACE_2,(int)(alpha*245)),withAlpha(SURFACE,(int)(alpha*232)),corner);d.setStroke(dp(1),border==Color.TRANSPARENT?withAlpha(TEXT,28):withAlpha(border,115));return d;}
    private GradientDrawable background(int start,int end,int corner){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{start,end});d.setCornerRadius(dp(corner));return d;}
    private GradientDrawable circle(int color,boolean selected){GradientDrawable d=new GradientDrawable();d.setShape(GradientDrawable.OVAL);d.setColor(color);d.setStroke(dp(selected?3:1),withAlpha(TEXT,selected?230:35));return d;}
    private int withAlpha(int color,int a){return Color.argb(Math.max(0,Math.min(255,a)),Color.red(color),Color.green(color),Color.blue(color));}
    @SuppressWarnings("deprecation") private void applySystemInsets(View view){if(android.os.Build.VERSION.SDK_INT>=21){final int left=view.getPaddingLeft(),top=view.getPaddingTop(),right=view.getPaddingRight(),bottom=view.getPaddingBottom();view.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(left+insets.getSystemWindowInsetLeft(),top+insets.getSystemWindowInsetTop(),right+insets.getSystemWindowInsetRight(),bottom+insets.getSystemWindowInsetBottom());return insets;});view.requestApplyInsets();}}
    private LinearLayout.LayoutParams matchHeight(int h,int top,int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,h);p.setMargins(0,top,0,bottom);return p;}private LinearLayout.LayoutParams matchWrap(int top,int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,top,0,bottom);return p;}private LinearLayout.LayoutParams wrapHeight(int h,int top,int end){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,h);p.setMargins(0,top,end,0);return p;}
    private int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}private String ar(String a,String e){return arabic?a:e;}private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private String permissionLabel(){return selected.category.equals("Weather")||selected.category.equals("Prayer")?ar("الإنترنت فقط؛ الموقع اختياري","Internet only; location optional"):ar("لا توجد أذونات حساسة","No sensitive permissions");}
    private int autoAccent(){int[] p={CYAN,VIOLET,0xffff5794,0xff59e78e};return p[Math.abs(selected.id.hashCode())%p.length];}
    private void toggleFavorite(String id){if(!favorites.add(id))favorites.remove(id);prefs.edit().putStringSet("favorites",new HashSet<>(favorites)).apply();}
    private void saveStyle(){prefs.edit().putInt("accent",accent).putInt("opacity_int",opacity).putInt("corner_int",radius).apply();}
    private void clearLocal(){favorites.clear();prefs.edit().remove("favorites").remove("accent").remove("opacity_int").remove("corner_int").apply();accent=CYAN;opacity=82;radius=28;toast(ar("تم حذف البيانات المحلية","Local data cleared"));}
    private void showCityDialog(){String[] names=new String[DataRepository.CITIES.length];for(int i=0;i<names.length;i++)names[i]=DataRepository.CITIES[i][0];new AlertDialog.Builder(this).setTitle(ar("اختر المدينة","Choose city")).setItems(names,(dialog,which)->{String[] c=DataRepository.CITIES[which];prefs.edit().putString("city_name",c[0]).putLong("city_lat",Double.doubleToRawLongBits(Double.parseDouble(c[1]))).putLong("city_lon",Double.doubleToRawLongBits(Double.parseDouble(c[2]))).remove("weather_updated").remove("prayer_updated").apply();DataRepository.refresh(this,true);toast(ar("جارٍ تحديث الطقس والمواقيت","Refreshing weather and prayer times"));render("settings");}).setNegativeButton(ar("إلغاء","Cancel"),null).show();}
    private void openAppSettings(){try{Intent i=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);i.setData(android.net.Uri.parse("package:"+getPackageName()));startActivity(i);}catch(Exception e){toast(ar("تعذر فتح الإعدادات","Unable to open settings"));}}private void openBatterySettings(){try{startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));}catch(Exception e){toast(ar("تعذر فتح الإعدادات","Unable to open settings"));}}
    private SeekBar.OnSeekBarChangeListener seek(IntValue r){return new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean u){r.set(p);}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){saveStyle();render("editor");}};}private interface IntValue{void set(int v);}
    private void requestPin(){if(android.os.Build.VERSION.SDK_INT<26){toast(ar("أضف الويدجت من قائمة الشاشة الرئيسية","Add the widget from your launcher"));return;}AppWidgetManager m=AppWidgetManager.getInstance(this);if(!m.isRequestPinAppWidgetSupported()){toast(ar("المشغل لا يدعم الإضافة المباشرة","Launcher does not support direct pinning"));return;}ComponentName p=new ComponentName(this,providerFor(selected));Bundle extras=new Bundle();extras.putString("vitra_style",selected.id);PendingIntent ok=PendingIntent.getActivity(this,100,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);m.requestPinAppWidget(p,extras,ok);}
    private Class<?> providerFor(WidgetData d){switch(d.category){case"Date":return DateWidgetProvider.class;case"Weather":return WeatherWidgetProvider.class;case"Prayer":return PrayerWidgetProvider.class;case"Search":case"Apps":return SearchWidgetProvider.class;case"System":case"Sports":return SystemWidgetProvider.class;default:return ClockWidgetProvider.class;}}
    @Override public void onBackPressed(){if(screen.equals("editor")){render("detail");return;}if(!screen.equals("home")){render("home");return;}super.onBackPressed();}
}
