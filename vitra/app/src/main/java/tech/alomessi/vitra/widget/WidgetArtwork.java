package tech.alomessi.vitra.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

import tech.alomessi.vitra.WidgetData;
import tech.alomessi.vitra.WidgetPreviewView;

/** Draws the same live Canvas artwork used by the catalogue into a RemoteViews bitmap. */
final class WidgetArtwork {
    private WidgetArtwork() { }

    static Bitmap render(Context context, WidgetData data, boolean arabic, int accent,
                         int opacity, int darkness, int blur, int radius, String background,
                         boolean showDate) {
        int width = 900;
        int height = data.span == 2 ? 420 : 560;
        WidgetPreviewView preview = new WidgetPreviewView(context, data, arabic, accent);
        preview.setAppearance(accent, opacity, darkness, blur, radius, background);
        preview.setShowDate(showDate);
        preview.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        preview.layout(0, 0, width, height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        preview.draw(new Canvas(bitmap));
        return bitmap;
    }
}
