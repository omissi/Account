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
        int accent=prefs.getInt("widget_accent_"+widgetId,prefs.getInt("accent",0xff30e7ff));
        int opacity=prefs.getInt("widget_opacity_"+widgetId,prefs.getInt("opacity_int",82));
        int radius=prefs.getInt("widget_radius_"+widgetId,prefs.getInt("corner_int",28));
        views.setImageViewBitmap(R.id.widget_background,background(accent,opacity,radius));
        views.setTextColor(R.id.widget_eyebrow,accent);
    }

    private static Bitmap background(int accent,int opacity,int radius) {
        int width=480,height=240;
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);
        int alpha=Math.max(64,Math.min(245,Math.round(opacity*2.45f)));
        Paint fill=new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setShader(new LinearGradient(0,0,width,height,
            Color.argb(alpha,18,24,39),Color.argb(Math.max(50,alpha-20),24,16,40),Shader.TileMode.CLAMP));
        float r=Math.max(12,Math.min(52,radius))*2.0f;
        RectF rect=new RectF(4,4,width-4,height-4);
        canvas.drawRoundRect(rect,r,r,fill);
        Paint stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeWidth(4);stroke.setColor(Color.argb(150,Color.red(accent),Color.green(accent),Color.blue(accent)));
        canvas.drawRoundRect(rect,r,r,stroke);
        return bitmap;
    }
}
