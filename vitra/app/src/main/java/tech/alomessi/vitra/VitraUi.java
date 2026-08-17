package tech.alomessi.vitra;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Shared native Android styling for the Vitra glass interface. */
public final class VitraUi {
    public static final int BG=0xff0c0c0d, CARD=0xff1a1a1c, CARD_2=0xff242427;
    public static final int TEXT=0xfff7f7f8, MUTED=0xff9c9ca2, BORDER=0xff3c3c40;
    public static final int BLUE=0xff1689f7, PURPLE=0xff998bff, GOLD=0xffffbd47;
    private VitraUi(){}
    public static int dp(Context c,float value){return Math.round(value*c.getResources().getDisplayMetrics().density);}
    public static LinearLayout column(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.VERTICAL);return l;}
    public static LinearLayout row(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.HORIZONTAL);return l;}
    public static TextView text(Context c,String value,float size,int color,boolean bold){
        TextView t=new TextView(c);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));t.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);t.setLineSpacing(0,1.08f);return t;
    }
    public static Button button(Context c,String label,int size,int textColor,GradientDrawable background){
        Button b=new Button(c);b.setAllCaps(false);b.setText(label);b.setTextSize(size);b.setTextColor(textColor);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setGravity(Gravity.CENTER);b.setPadding(dp(c,10),0,dp(c,10),0);b.setMinWidth(0);b.setMinHeight(0);b.setBackground(background);return b;
    }
    public static Button primary(Context c,String label){return button(c,label,15,Color.BLACK,gradient(c,Color.WHITE,0xffd8d8dc,21));}
    public static Button secondary(Context c,String label){return button(c,label,14,TEXT,glass(c,21,.96f,BORDER));}
    public static Button chip(Context c,String label,boolean active){
        Button b=button(c,label,11,active?TEXT:0xffd1d1d4,glass(c,19,active?.96f:.56f,active?0xff68686d:BORDER));
        b.setTypeface(Typeface.create("sans",active?Typeface.BOLD:Typeface.NORMAL));return b;
    }
    public static Button circleButton(Context c,String label){Button b=button(c,label,17,TEXT,circle(c,CARD_2,false));return b;}
    public static GradientDrawable gradient(Context c,int start,int end,int corner){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{start,end});d.setCornerRadius(dp(c,corner));return d;}
    public static GradientDrawable glass(Context c,int corner,float alpha,int border){GradientDrawable d=gradient(c,withAlpha(CARD_2,(int)(alpha*245)),withAlpha(CARD,(int)(alpha*235)),corner);d.setStroke(dp(c,1),border);return d;}
    public static GradientDrawable circle(Context c,int color,boolean selected){GradientDrawable d=new GradientDrawable();d.setShape(GradientDrawable.OVAL);d.setColor(color);d.setStroke(dp(c,selected?3:1),selected?Color.WHITE:BORDER);return d;}
    public static int withAlpha(int color,int alpha){return Color.argb(Math.max(0,Math.min(255,alpha)),Color.red(color),Color.green(color),Color.blue(color));}
    @SuppressWarnings("deprecation") public static void applyInsets(View v){final int l=v.getPaddingLeft(),t=v.getPaddingTop(),r=v.getPaddingRight(),b=v.getPaddingBottom();v.setOnApplyWindowInsetsListener((view,insets)->{view.setPadding(l+insets.getSystemWindowInsetLeft(),t+insets.getSystemWindowInsetTop(),r+insets.getSystemWindowInsetRight(),b+insets.getSystemWindowInsetBottom());return insets;});v.requestApplyInsets();}
}
