package tech.alomessi.vitra;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

/** Compact graphical preview of an original Vitra icon treatment. */
public final class IconPackPreviewView extends View {
    private IconPackData pack;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final String[] MARKS={"☎","●●●","✦","◉","◷","⌖","⚙","±","●","▰"};
    public IconPackPreviewView(Context context, IconPackData pack){super(context);this.pack=pack;setContentDescription(pack.name(false));}
    public void setPack(IconPackData value){pack=value;setContentDescription(value.name(false));invalidate();}

    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas); float w=getWidth(),h=getHeight();
        paint.setShader(new LinearGradient(0,0,w,h,pack.start,pack.end,Shader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(0,0,w,h),h*.12f,h*.12f,paint); paint.setShader(null);
        paint.setColor(withAlpha(pack.accent,84)); canvas.drawCircle(w*.78f,h*.18f,w*.44f,paint);
        for(int i=0;i<MARKS.length;i++){
            int col=i%5,row=i/5; float x=w*(.11f+col*.195f),y=h*.28f+row*h*.38f; float size=Math.min(w,h)*.125f;
            paint.setShader(new LinearGradient(x-size,y-size,x+size,y+size,withAlpha(Color.WHITE,52),withAlpha(pack.accent,110),Shader.TileMode.CLAMP));
            canvas.drawRoundRect(new RectF(x-size,y-size,x+size,y+size),size*.42f,size*.42f,paint);paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(1,size*.045f));paint.setColor(0x66ffffff);canvas.drawRoundRect(new RectF(x-size,y-size,x+size,y+size),size*.42f,size*.42f,paint);paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(size*.9f);paint.setColor(pack.variant==4?0xff121212:Color.WHITE);paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);canvas.drawText(MARKS[i],x,y+size*.30f,paint);
        }
    }
    private static int withAlpha(int color,int alpha){return Color.argb(Math.max(0,Math.min(255,alpha)),Color.red(color),Color.green(color),Color.blue(color));}
}
