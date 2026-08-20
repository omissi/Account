package tech.alomessi.vitra;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

/** Renders original abstract backgrounds locally, for both preview and real wallpaper apply. */
public final class WallpaperPreviewView extends View {
    private WallpaperData wallpaper;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public WallpaperPreviewView(Context context, WallpaperData wallpaper) {
        super(context);
        this.wallpaper = wallpaper;
        setContentDescription(wallpaper.name(false));
    }

    public void setWallpaper(WallpaperData value) {
        wallpaper = value;
        setContentDescription(value.name(false));
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawArtwork(canvas, wallpaper, getWidth(), getHeight());
    }

    public static Bitmap bitmap(WallpaperData wallpaper, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(Math.max(1, width), Math.max(1, height), Bitmap.Config.ARGB_8888);
        drawArtwork(new Canvas(bitmap), wallpaper, width, height);
        return bitmap;
    }

    private static void drawArtwork(Canvas canvas, WallpaperData data, int width, int height) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setShader(new LinearGradient(0, 0, width, height, data.start, data.end, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, p);
        p.setShader(new RadialGradient(width * .75f, height * .22f, Math.max(width, height) * .78f,
                withAlpha(data.glow, 180), withAlpha(data.glow, 0), Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, p);
        p.setShader(null);
        switch (data.pattern) {
            case 0: drawAura(canvas, p, data, width, height); break;
            case 1: drawFolds(canvas, p, data, width, height); break;
            case 2: drawRibbon(canvas, p, data, width, height); break;
            case 3: drawMountains(canvas, p, data, width, height); break;
            default: drawGlassBeams(canvas, p, data, width, height); break;
        }
        p.setShader(new LinearGradient(0, 0, 0, height, 0x30000000, 0x00000000, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, p);
    }

    private static void drawAura(Canvas c, Paint p, WallpaperData d, int w, int h) {
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(18, w * .16f));
        p.setColor(withAlpha(d.glow, 200));
        c.drawOval(new RectF(-w * .25f, h * .02f, w * 1.15f, h * 1.12f), p);
        p.setStrokeWidth(Math.max(9, w * .06f));
        p.setColor(withAlpha(Color.WHITE, 70));
        c.drawOval(new RectF(w * .12f, -h * .15f, w * 1.25f, h * .88f), p);
        p.setStyle(Paint.Style.FILL);
    }

    private static void drawFolds(Canvas c, Paint p, WallpaperData d, int w, int h) {
        p.setColor(withAlpha(Color.WHITE, 35));
        c.drawOval(new RectF(-w * .62f, h * .25f, w * .72f, h * 1.35f), p);
        p.setColor(withAlpha(d.glow, 80));
        c.drawOval(new RectF(w * .12f, -h * .18f, w * 1.22f, h * .86f), p);
        p.setShader(new LinearGradient(0, h * .55f, w, h, withAlpha(Color.WHITE, 0), withAlpha(Color.WHITE, 90), Shader.TileMode.CLAMP));
        c.drawRoundRect(new RectF(-w * .1f, h * .72f, w * 1.1f, h * 1.08f), w * .08f, w * .08f, p);
        p.setShader(null);
    }

    private static void drawRibbon(Canvas c, Paint p, WallpaperData d, int w, int h) {
        Path band = new Path();
        band.moveTo(-w * .1f, h * .88f);
        band.lineTo(w * .25f, h * .45f);
        band.lineTo(w * .47f, h * .6f);
        band.lineTo(w * 1.12f, h * .08f);
        band.lineTo(w * 1.12f, h * .36f);
        band.lineTo(w * .50f, h * .83f);
        band.lineTo(w * .27f, h * .68f);
        band.lineTo(w * .05f, h * 1.05f);
        band.close();
        p.setShader(new LinearGradient(0, 0, w, h, withAlpha(d.glow, 230), withAlpha(Color.WHITE, 30), Shader.TileMode.CLAMP));
        c.drawPath(band, p);
        p.setShader(null);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(2, w * .008f));
        p.setColor(withAlpha(Color.WHITE, 120));
        c.drawPath(band, p);
        p.setStyle(Paint.Style.FILL);
    }

    private static void drawMountains(Canvas c, Paint p, WallpaperData d, int w, int h) {
        Path rear = new Path();
        rear.moveTo(0, h * .72f); rear.lineTo(w * .28f, h * .36f); rear.lineTo(w * .54f, h * .65f); rear.lineTo(w * .78f, h * .22f); rear.lineTo(w, h * .57f); rear.lineTo(w, h); rear.lineTo(0, h); rear.close();
        p.setColor(withAlpha(d.glow, 95)); c.drawPath(rear, p);
        Path front = new Path();
        front.moveTo(0, h * .86f); front.lineTo(w * .32f, h * .53f); front.lineTo(w * .52f, h * .8f); front.lineTo(w * .78f, h * .47f); front.lineTo(w, h * .75f); front.lineTo(w, h); front.lineTo(0, h); front.close();
        p.setColor(withAlpha(0xff02050a, 155)); c.drawPath(front, p);
        p.setColor(withAlpha(Color.WHITE, 55)); c.drawCircle(w * .75f, h * .22f, Math.max(6, w * .045f), p);
    }

    private static void drawGlassBeams(Canvas c, Paint p, WallpaperData d, int w, int h) {
        p.setColor(withAlpha(Color.WHITE, 28));
        c.drawRoundRect(new RectF(w * .10f, -h * .12f, w * .34f, h * 1.16f), w * .08f, w * .08f, p);
        p.setColor(withAlpha(d.glow, 165));
        c.drawRoundRect(new RectF(w * .54f, -h * .10f, w * .78f, h * 1.16f), w * .08f, w * .08f, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(2, w * .009f));
        p.setColor(withAlpha(Color.WHITE, 85));
        c.drawRoundRect(new RectF(w * .54f, -h * .10f, w * .78f, h * 1.16f), w * .08f, w * .08f, p);
        p.setStyle(Paint.Style.FILL);
    }

    private static int withAlpha(int color, int alpha) {
        return Color.argb(Math.max(0, Math.min(255, alpha)), Color.red(color), Color.green(color), Color.blue(color));
    }
}
