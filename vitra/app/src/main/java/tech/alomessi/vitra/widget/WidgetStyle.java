package tech.alomessi.vitra.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.widget.RemoteViews;

import tech.alomessi.vitra.R;

final class WidgetStyle {
    private WidgetStyle() {}

    static void apply(Context context, RemoteViews views, int widgetId) {
        SharedPreferences prefs=context.getSharedPreferences("vitra",Context.MODE_PRIVATE);
        int accent=prefs.getInt("widget_accent_"+widgetId,prefs.getInt("accent",0xff9587ff));
        int opacity=prefs.getInt("widget_opacity_"+widgetId,prefs.getInt("opacity_int",76));
        int radius=prefs.getInt("widget_radius_"+widgetId,prefs.getInt("corner_int",26));
        int darkness=prefs.getInt("widget_darkness_"+widgetId,prefs.getInt("darkness_int",72));
        views.setImageViewBitmap(R.id.widget_background,background(accent,opacity,radius,darkness));
        views.setTextColor(R.id.widget_eyebrow,accent);
        views.setTextColor(R.id.widget_primary,Color.WHITE);
        views.setTextColor(R.id.widget_secondary,0xffc7c7cb);
    }

    private static Bitmap background(int accent,int opacity,int radius,int darkness) {
        int width=480,height=240;
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);
        int alpha=Math.max(58,Math.min(242,Math.round(opacity*2.42f)));
        int shade=Math.max(8,Math.min(38,38-darkness/4));
        Paint fill=new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setShader(new LinearGradient(0,0,width,height,
            Color.argb(alpha,shade+20,shade+20,shade+22),Color.argb(Math.max(48,alpha-18),shade,shade,shade+2),Shader.TileMode.CLAMP));
        float r=Math.max(12,Math.min(52,radius))*2f;
        RectF rect=new RectF(4,4,width-4,height-4);
        canvas.drawRoundRect(rect,r,r,fill);
        Paint orb=new Paint(Paint.ANTI_ALIAS_FLAG);
        orb.setShader(new android.graphics.RadialGradient(width*.72f,height*.72f,width*.46f,
            Color.argb(38,Color.red(accent),Color.green(accent),Color.blue(accent)),0x00111111,Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect,r,r,orb);
        Paint shine=new Paint(Paint.ANTI_ALIAS_FLAG);
        shine.setShader(new LinearGradient(0,0,0,height*.56f,0x42ffffff,0x00ffffff,Shader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(8,8,width-8,height*.58f),r-4,r-4,shine);
        Paint stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeWidth(3);stroke.setColor(0x66ffffff);
        canvas.drawRoundRect(rect,r,r,stroke);
        return bitmap;
    }
}
