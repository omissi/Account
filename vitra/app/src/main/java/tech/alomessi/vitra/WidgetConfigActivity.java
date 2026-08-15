package tech.alomessi.vitra;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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

@SuppressLint("SetTextI18n")
public class WidgetConfigActivity extends Activity {
    private static final int BG=0xff050505,TEXT=0xfff7f7f7,MUTED=0xff9b9b9f,BORDER=0xff3d3d3f;
    private int widgetId=AppWidgetManager.INVALID_APPWIDGET_ID,accent=0xff9587ff,opacity=76,radius=26,darkness=72,blur=18;
    private WidgetData selected=WidgetData.ALL.get(0); private SharedPreferences prefs;

    @Override protected void onCreate(Bundle state){super.onCreate(state);setResult(RESULT_CANCELED);widgetId=getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);if(widgetId==AppWidgetManager.INVALID_APPWIDGET_ID){finish();return;}prefs=getSharedPreferences("vitra",MODE_PRIVATE);accent=prefs.getInt("accent",accent);opacity=prefs.getInt("opacity_int",opacity);radius=prefs.getInt("corner_int",radius);darkness=prefs.getInt("darkness_int",darkness);blur=prefs.getInt("blur_int",blur);String style=prefs.getString("pending_widget_style","clock-classic");for(WidgetData d:WidgetData.ALL)if(d.id.equals(style)){selected=d;break;}build();}

    private void build(){ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(24),dp(18),dp(28));root.setBackgroundColor(BG);scroll.addView(root,new ScrollView.LayoutParams(-1,-2));TextView title=text("V I T R A",20,TEXT,true);title.setGravity(Gravity.CENTER);title.setLetterSpacing(.18f);root.addView(title,match(dp(3)));TextView tag=text("CUSTOMIZE WIDGET · تخصيص الويدجت",10,accent,true);tag.setGravity(Gravity.CENTER);tag.setLetterSpacing(.12f);root.addView(tag,match(dp(14)));
        FrameLayout preview=new FrameLayout(this);preview.setPadding(dp(16),dp(16),dp(16),dp(16));preview.setBackground(round(0xff101011,26,BORDER));preview.addView(new WidgetPreviewView(this,selected,prefs.getBoolean("arabic",false),accent),new FrameLayout.LayoutParams(-1,-1));root.addView(preview,new LinearLayout.LayoutParams(-1,dp(236)));
        root.addView(section("GLASS BACKGROUND · خلفية الزجاج"),top(dp(18),dp(8)));root.addView(choices(new String[]{"Clear","Fill","Gradient","Image"}),match(dp(14)));root.addView(section("EFFECT · المؤثر"),match(dp(8)));root.addView(choices(new String[]{"Normal","Blur","Fractal"}),match(dp(16)));
        root.addView(slider("Opacity · الشفافية",25,100,opacity,v->opacity=v),match(dp(7)));root.addView(slider("Darkness · العتامة",0,100,darkness,v->darkness=v),match(dp(7)));root.addView(slider("Blur · التمويه",0,40,blur,v->blur=v),match(dp(7)));root.addView(slider("Corner radius · الزوايا",8,48,radius,v->radius=v),match(dp(14)));
        root.addView(section("HIGHLIGHT COLOR · لون الإضاءة"),match(dp(8)));int[] colors={Color.WHITE,0xff9b9b9f,0xff9587ff,0xff6aa9ff,0xffff8fb8,0xffffb66a,0xff78d49a};LinearLayout palette=new LinearLayout(this);for(int color:colors){Button dot=new Button(this);dot.setText(color==accent?"✓":"");dot.setTextColor(color==Color.WHITE?Color.BLACK:Color.WHITE);dot.setPadding(0,0,0,0);dot.setBackground(circle(color));dot.setOnClickListener(v->accent=color);palette.addView(dot,new LinearLayout.LayoutParams(0,dp(46),1));}root.addView(palette,match(dp(14)));root.addView(toggle("Adapt to wallpaper · التكيف مع الخلفية",true),new LinearLayout.LayoutParams(-1,dp(58)));root.addView(toggle("Show settings icon · إظهار زر الإعدادات",true),top(0,dp(18)));
        Button save=new Button(this);save.setAllCaps(false);save.setText("Save & add · حفظ وإضافة");save.setTextColor(Color.BLACK);save.setTextSize(15);save.setTypeface(Typeface.DEFAULT,Typeface.BOLD);save.setBackground(round(Color.WHITE,21,Color.WHITE));save.setOnClickListener(v->save());root.addView(save,new LinearLayout.LayoutParams(-1,dp(58)));applyInsets(scroll);setContentView(scroll);}

    private void save(){prefs.edit().putString("widget_style_"+widgetId,selected.id).putInt("widget_accent_"+widgetId,accent).putInt("widget_opacity_"+widgetId,opacity).putInt("widget_radius_"+widgetId,radius).putInt("widget_darkness_"+widgetId,darkness).putInt("widget_blur_"+widgetId,blur).apply();AppWidgetManager manager=AppWidgetManager.getInstance(this);android.appwidget.AppWidgetProviderInfo info=manager.getAppWidgetInfo(widgetId);if(info!=null&&info.provider!=null){ComponentName provider=info.provider;int[] ids={widgetId};if(provider.equals(new ComponentName(this,ClockWidgetProvider.class)))new ClockWidgetProvider().onUpdate(this,manager,ids);else if(provider.equals(new ComponentName(this,DateWidgetProvider.class)))new DateWidgetProvider().onUpdate(this,manager,ids);else if(provider.equals(new ComponentName(this,WeatherWidgetProvider.class)))new WeatherWidgetProvider().onUpdate(this,manager,ids);else if(provider.equals(new ComponentName(this,PrayerWidgetProvider.class)))new PrayerWidgetProvider().onUpdate(this,manager,ids);else if(provider.equals(new ComponentName(this,SearchWidgetProvider.class)))new SearchWidgetProvider().onUpdate(this,manager,ids);else if(provider.equals(new ComponentName(this,SystemWidgetProvider.class)))new SystemWidgetProvider().onUpdate(this,manager,ids);}Intent result=new Intent();result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId);setResult(RESULT_OK,result);finish();}
    private LinearLayout choices(String[] names){LinearLayout row=new LinearLayout(this);for(int i=0;i<names.length;i++){Button b=new Button(this);b.setAllCaps(false);b.setText(names[i]);b.setTextColor(i==0?TEXT:MUTED);b.setTextSize(10);b.setPadding(0,0,0,0);b.setBackground(round(i==0?0xff303030:0xff181818,18,BORDER));row.addView(b,new LinearLayout.LayoutParams(0,dp(40),1));}return row;}
    private LinearLayout slider(String title,int min,int max,int value,Change listener){LinearLayout block=new LinearLayout(this);block.setOrientation(LinearLayout.VERTICAL);LinearLayout head=new LinearLayout(this);head.addView(text(title,13,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));TextView number=text(String.valueOf(value),12,MUTED,false);number.setGravity(Gravity.END);head.addView(number,new LinearLayout.LayoutParams(dp(48),-1));block.addView(head);SeekBar bar=new SeekBar(this);bar.setMax(max-min);bar.setProgress(value-min);bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean f){int v=p+min;number.setText(String.valueOf(v));listener.set(v);}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});block.addView(bar);return block;}
    private LinearLayout toggle(String title,boolean checked){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(14),0,dp(8),0);row.setBackground(round(0xff181818,18,BORDER));row.addView(text(title,13,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));Switch s=new Switch(this);s.setChecked(checked);row.addView(s);return row;}
    private TextView section(String s){TextView v=text(s,10,accent,true);v.setLetterSpacing(.12f);return v;}private TextView text(String s,float size,int color,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    private LinearLayout.LayoutParams match(int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,bottom);return p;}private LinearLayout.LayoutParams top(int top,int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,top,0,bottom);return p;}
    private GradientDrawable round(int color,int radius,int border){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));d.setStroke(dp(1),border);return d;}private GradientDrawable circle(int color){GradientDrawable d=new GradientDrawable();d.setShape(GradientDrawable.OVAL);d.setColor(color);d.setStroke(dp(1),0x55ffffff);return d;}
    @SuppressWarnings("deprecation") private void applyInsets(View view){final int l=view.getPaddingLeft(),t=view.getPaddingTop(),r=view.getPaddingRight(),b=view.getPaddingBottom();view.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(l+i.getSystemWindowInsetLeft(),t+i.getSystemWindowInsetTop(),r+i.getSystemWindowInsetRight(),b+i.getSystemWindowInsetBottom());return i;});view.requestApplyInsets();}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}private interface Change{void set(int value);}
}
