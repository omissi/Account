package tech.alomessi.vitra.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

final class WidgetArtwork {
    private WidgetArtwork(){}

    static Bitmap analog(int accent,int style){
        int w=480,h=260;Bitmap bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bitmap);Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);RectF card=new RectF(4,4,w-4,h-4);
        p.setShader(new LinearGradient(0,0,w,h,new int[]{0xee333333,0xee121212,0xee242424},null,Shader.TileMode.CLAMP));c.drawRoundRect(card,54,54,p);p.setShader(new LinearGradient(0,0,0,h*.55f,0x46ffffff,0x00ffffff,Shader.TileMode.CLAMP));c.drawRoundRect(new RectF(8,8,w-8,h*.55f),50,50,p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(0x66ffffff);c.drawRoundRect(card,54,54,p);p.setStyle(Paint.Style.FILL);
        float cx=w*.5f,cy=h*.50f,r=100;if(style==1||style==3){p.setColor(0x553f3f3f);c.drawCircle(cx,cy,r,p);p.setColor(0x55303030);c.drawCircle(cx,cy,r*.42f,p);}Calendar cal=Calendar.getInstance();float minute=cal.get(Calendar.MINUTE),hour=cal.get(Calendar.HOUR)%12+minute/60f;
        for(int i=0;i<12;i++){double a=Math.PI*2*i/12-Math.PI/2;float dot=(style==2||style==3)?(i%3==0?5:0):(i%3==0?5:2);if(dot==0)continue;p.setColor(i%3==0?Color.WHITE:0x88ffffff);c.drawCircle(cx+(float)Math.cos(a)*r*.86f,cy+(float)Math.sin(a)*r*.86f,dot,p);if(style==0||style==5)label(c,p,String.valueOf(i==0?12:i),cx+(float)Math.cos(a)*r*.68f,cy+(float)Math.sin(a)*r*.68f+4,14,0xffdddddd,Paint.Align.CENTER,false);}
        hand(c,p,cx,cy,hour/12f*360-90,r*.48f,7,Color.WHITE);hand(c,p,cx,cy,minute/60f*360-90,r*.72f,5,Color.WHITE);hand(c,p,cx,cy,cal.get(Calendar.SECOND)/60f*360-90,r*.82f,2,0xffff1b2d);p.setColor(0xffff1b2d);c.drawCircle(cx,cy,5,p);label(c,p,new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date()),cx,cy-r*.46f,15,0xffbbbbbb,Paint.Align.CENTER,false);label(c,p,new SimpleDateFormat("EEE",Locale.ENGLISH).format(new Date()).toUpperCase(),cx,cy+r*.58f,14,0xffcccccc,Paint.Align.CENTER,false);label(c,p,"VITRA",34,42,12,accent,Paint.Align.LEFT,true);return bitmap;
    }
    private static void hand(Canvas c,Paint p,float x,float y,float degrees,float length,float width,int color){double a=Math.toRadians(degrees);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(color);c.drawLine(x,y,x+(float)Math.cos(a)*length,y+(float)Math.sin(a)*length,p);}
    private static void label(Canvas c,Paint p,String s,float x,float y,float size,int color,Paint.Align align,boolean bold){p.setStyle(Paint.Style.FILL);p.setShader(null);p.setColor(color);p.setTextSize(size);p.setTextAlign(align);p.setTypeface(android.graphics.Typeface.create("sans",bold?android.graphics.Typeface.BOLD:android.graphics.Typeface.NORMAL));c.drawText(s,x,y,p);}
}
