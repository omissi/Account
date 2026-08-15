package tech.alomessi.vitra;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import tech.alomessi.vitra.widget.ClockWidgetProvider;
import tech.alomessi.vitra.widget.DateWidgetProvider;
import tech.alomessi.vitra.widget.PrayerWidgetProvider;
import tech.alomessi.vitra.widget.SearchWidgetProvider;
import tech.alomessi.vitra.widget.SystemWidgetProvider;
import tech.alomessi.vitra.widget.WeatherWidgetProvider;

@SuppressLint("SetTextI18n")
public class WidgetConfigActivity extends Activity {
    private int widgetId=AppWidgetManager.INVALID_APPWIDGET_ID;
    private int accent=0xff30e7ff;
    private int opacity=82;
    private int radius=28;

    @Override protected void onCreate(Bundle state){super.onCreate(state);setResult(RESULT_CANCELED);widgetId=getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);if(widgetId==AppWidgetManager.INVALID_APPWIDGET_ID){finish();return;}android.content.SharedPreferences p=getSharedPreferences("vitra",MODE_PRIVATE);accent=p.getInt("accent",accent);opacity=p.getInt("opacity_int",opacity);radius=p.getInt("corner_int",radius);build();}
    private void build(){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(22),dp(34),dp(22),dp(24));root.setBackgroundColor(0xff07090f);TextView title=text("Customize Vitra · تخصيص Vitra",25,Color.WHITE,true);root.addView(title,match(dp(12)));root.addView(text("Accent color · لون التوهج",14,0xff9daac2,false),match(dp(14)));int[] colors={0xff30e7ff,0xff9d5cff,0xffff5794,0xffffbc48,0xff59e78e,Color.WHITE};LinearLayout row=new LinearLayout(this);for(int color:colors){Button b=new Button(this);b.setContentDescription("Accent color");b.setBackground(circle(color));b.setOnClickListener(v->{accent=color;});row.addView(b,new LinearLayout.LayoutParams(0,dp(52),1));}root.addView(row,match(dp(18)));root.addView(text("Glass opacity · شفافية الزجاج",14,0xff9daac2,false),match(dp(4)));SeekBar alpha=new SeekBar(this);alpha.setMax(100);alpha.setProgress(opacity);alpha.setOnSeekBarChangeListener(change(v->opacity=Math.max(25,v)));root.addView(alpha,match(dp(12)));root.addView(text("Corner radius · استدارة الزوايا",14,0xff9daac2,false),match(dp(4)));SeekBar corners=new SeekBar(this);corners.setMax(48);corners.setProgress(radius);corners.setOnSeekBarChangeListener(change(v->radius=Math.max(8,v)));root.addView(corners,match(dp(20)));Button save=new Button(this);save.setAllCaps(false);save.setText("Save and add · حفظ وإضافة");save.setTextColor(0xff07090f);save.setTextSize(16);save.setBackground(round(0xff30e7ff,22));save.setOnClickListener(v->save());root.addView(save,new LinearLayout.LayoutParams(-1,dp(58)));applyInsets(root);setContentView(root);}
    private void save(){
        getSharedPreferences("vitra",MODE_PRIVATE).edit().putInt("widget_accent_"+widgetId,accent).putInt("widget_opacity_"+widgetId,opacity).putInt("widget_radius_"+widgetId,radius).apply();
        AppWidgetManager manager=AppWidgetManager.getInstance(this);
        android.appwidget.AppWidgetProviderInfo info=manager.getAppWidgetInfo(widgetId);
        if(info!=null&&info.provider!=null){
            ComponentName provider=info.provider;
            int[] ids={widgetId};
            if(provider.equals(new ComponentName(this,ClockWidgetProvider.class)))new ClockWidgetProvider().onUpdate(this,manager,ids);
            else if(provider.equals(new ComponentName(this,DateWidgetProvider.class)))new DateWidgetProvider().onUpdate(this,manager,ids);
            else if(provider.equals(new ComponentName(this,WeatherWidgetProvider.class)))new WeatherWidgetProvider().onUpdate(this,manager,ids);
            else if(provider.equals(new ComponentName(this,PrayerWidgetProvider.class)))new PrayerWidgetProvider().onUpdate(this,manager,ids);
            else if(provider.equals(new ComponentName(this,SearchWidgetProvider.class)))new SearchWidgetProvider().onUpdate(this,manager,ids);
            else if(provider.equals(new ComponentName(this,SystemWidgetProvider.class)))new SystemWidgetProvider().onUpdate(this,manager,ids);
        }
        Intent result=new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId);
        setResult(RESULT_OK,result);
        finish();
    }
    private TextView text(String s,float size,int color,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setGravity(Gravity.CENTER);if(bold)v.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);return v;}
    private LinearLayout.LayoutParams match(int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,bottom);return p;}
    private GradientDrawable circle(int color){GradientDrawable d=new GradientDrawable();d.setShape(GradientDrawable.OVAL);d.setColor(color);d.setStroke(dp(2),0x33ffffff);return d;}
    private GradientDrawable round(int color,int r){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(r));return d;}
    private SeekBar.OnSeekBarChangeListener change(IntChange c){return new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean fromUser){c.set(p);}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}};}private interface IntChange{void set(int v);}
    @SuppressWarnings("deprecation") private void applyInsets(View view){final int l=view.getPaddingLeft(),t=view.getPaddingTop(),r=view.getPaddingRight(),b=view.getPaddingBottom();view.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(l+i.getSystemWindowInsetLeft(),t+i.getSystemWindowInsetTop(),r+i.getSystemWindowInsetRight(),b+i.getSystemWindowInsetBottom());return i;});view.requestApplyInsets();}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}
