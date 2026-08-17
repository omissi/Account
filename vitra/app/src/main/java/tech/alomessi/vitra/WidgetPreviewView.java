package tech.alomessi.vitra;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Canvas-rendered preview used in the catalogue, editor and real AppWidget
 * artwork. It deliberately avoids static screenshots so every clock, date,
 * weather and prayer preview reflects the live device time and settings.
 */
@SuppressLint("DrawAllocation")
public final class WidgetPreviewView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private WidgetData data;
    private boolean arabic;
    private int accent;
    private int opacity = 76, darkness = 72, blur = 18, radius = 26;
    private String background = "Clear";
    private boolean showDate = true;

    public WidgetPreviewView(Context context) { this(context, WidgetData.ALL.get(0), false, VitraUi.PURPLE); }
    public WidgetPreviewView(Context context, WidgetData item, boolean arabic, int accent) {
        super(context); this.data=item; this.arabic=arabic; this.accent=accent;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null); setContentDescription(item.title(arabic));
    }

    public void setItem(WidgetData item) { data=item; setContentDescription(item.title(arabic)); invalidate(); }
    public void setArabic(boolean value) { arabic=value; setContentDescription(data.title(value)); invalidate(); }
    public void setAppearance(int color, int opacity, int darkness, int blur, int radius, String background) {
        this.accent=color; this.opacity=opacity; this.darkness=darkness; this.blur=blur; this.radius=radius;
        this.background=background==null?"Clear":background; invalidate();
    }
    public void setShowDate(boolean value){showDate=value;invalidate();}

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c); float w=getWidth(), h=getHeight(); if(w<4||h<4)return;
        drawGlassCard(c,w,h);
        if ("Clock".equals(data.category)) drawClock(c,w,h);
        else if ("Digital".equals(data.category)) drawDigital(c,w,h);
        else if ("Date".equals(data.category)) drawDate(c,w,h);
        else if ("Weather".equals(data.category)) drawWeather(c,w,h);
        else if ("Prayer".equals(data.category)) drawPrayer(c,w,h);
        else if ("Quotes".equals(data.category)) drawQuote(c,w,h);
        else if ("Counters".equals(data.category)) drawCounter(c,w,h);
        else if ("Search".equals(data.category)) drawSearch(c,w,h);
        else if ("Apps".equals(data.category) || "People".equals(data.category)) drawShortcuts(c,w,h);
        else if ("System".equals(data.category)) drawSystem(c,w,h);
        else if ("Sports".equals(data.category)) drawSports(c,w,h);
        if (data.isPro) { text(c,"♛",w*.90f,h*.16f,Math.max(11,h*.12f),VitraUi.GOLD,Paint.Align.CENTER,true); }
    }

    private void drawGlassCard(Canvas c,float w,float h){
        float m=Math.min(w,h),pad=Math.max(3,m*.045f),corner=Math.min(m*.19f,Math.max(12,m*(radius/220f)));
        rect.set(pad,pad,w-pad,h-pad); int shade=Math.max(6,Math.min(35,39-darkness/3)); int a=Math.max(68,Math.min(245,Math.round(opacity*2.4f)));
        int first="Image".equals(background)?0xff293a55:"Gradient".equals(background)?VitraUi.withAlpha(accent,a):Color.argb(a,shade+16,shade+16,shade+19);
        int second="Fill".equals(background)?VitraUi.withAlpha(accent,Math.max(90,a-45)):Color.argb(Math.max(55,a-22),shade,shade,shade+3);
        p.setShader(new LinearGradient(0,0,w,h,first,second,Shader.TileMode.CLAMP)); p.setStyle(Paint.Style.FILL);p.setShadowLayer(Math.max(4,blur*.35f),0,Math.max(2,blur*.2f),0x80000000);c.drawRoundRect(rect,corner,corner,p);p.clearShadowLayer();
        p.setShader(new LinearGradient(0,pad,0,h*.58f,0x4cffffff,0x00ffffff,Shader.TileMode.CLAMP));c.drawRoundRect(new RectF(pad*1.3f,pad*1.3f,w-pad*1.3f,h*.58f),Math.max(5,corner-3),Math.max(5,corner-3),p);p.setShader(null);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1,m*.006f));p.setColor(0x66ffffff);c.drawRoundRect(rect,corner,corner,p);p.setStyle(Paint.Style.FILL);
        p.setColor(VitraUi.withAlpha(accent,20));c.drawCircle(w*.74f,h*.74f,Math.max(w,h)*.33f,p);
    }

    private void drawClock(Canvas c,float w,float h){
        int mode=data.style; float cx=w*.5f,cy=h*.52f,rad=Math.min(w,h)*(data.span==2?.33f:.32f); Calendar cal=Calendar.getInstance();
        if(mode==6||mode==7||mode==8||mode==9) drawIslamicPattern(c,cx,cy,rad,mode);
        if(mode==1||mode==3){p.setColor(0x443f3f3f);c.drawCircle(cx,cy,rad,p);p.setColor(0x552a2a2a);c.drawCircle(cx,cy,rad*.43f,p);}
        if(mode==12||mode==13){ drawWideClock(c,w,h,cal,mode); return; }
        for(int i=0;i<12;i++){
            double a=Math.PI*2*i/12-Math.PI/2; float dot=(mode==2||mode==3)?(i%3==0?5:0):(i%3==0?5:2);
            if(dot>0){p.setColor(i%3==0?Color.WHITE:0x88ffffff);c.drawCircle(cx+(float)Math.cos(a)*rad*.87f,cy+(float)Math.sin(a)*rad*.87f,dot,p);}
            if(mode==0||mode==5||mode==10||mode==11){text(c,String.valueOf(i==0?12:i),cx+(float)Math.cos(a)*rad*.68f,cy+(float)Math.sin(a)*rad*.68f+4,Math.max(9,rad*.13f),0xffdddddd,Paint.Align.CENTER,false);}
        }
        float minute=cal.get(Calendar.MINUTE),hour=cal.get(Calendar.HOUR)%12+minute/60f;
        if(mode<=3||mode==10||mode==11) text(c,time(),cx,cy-rad*.46f,Math.max(9,rad*.15f),0xffc2c2c4,Paint.Align.CENTER,false);
        hand(c,cx,cy,hour/12f*360-90,rad*.50f,Math.max(4,rad*.047f),Color.WHITE);
        hand(c,cx,cy,minute/60f*360-90,rad*.73f,Math.max(3,rad*.031f),Color.WHITE);
        hand(c,cx,cy,cal.get(Calendar.SECOND)/60f*360-90,rad*.83f,Math.max(2,rad*.016f),0xffff1d30);p.setColor(0xffff1d30);c.drawCircle(cx,cy,Math.max(3,rad*.038f),p);
        if(showDate) text(c,arabic?arabicDay():new SimpleDateFormat("EEE",Locale.ENGLISH).format(new Date()).toUpperCase(Locale.ENGLISH),cx,cy+rad*.56f,Math.max(9,rad*.14f),0xffceced1,Paint.Align.CENTER,false);
    }

    private void drawWideClock(Canvas c,float w,float h,Calendar cal,int mode){
        float cx=w*.5f,cy=h*.54f,rx=w*.36f,ry=h*.32f; p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2,h*.018f));p.setColor(0xffdddddf);c.drawArc(new RectF(cx-rx,cy-ry,cx+rx,cy+ry),190,160,false,p);c.drawArc(new RectF(cx-rx,cy-ry,cx+rx,cy+ry),10,160,false,p);p.setStyle(Paint.Style.FILL);
        text(c,mode==13?"١٢":"12",w*.25f,cy+h*.09f,h*.32f,Color.WHITE,Paint.Align.CENTER,true);text(c,mode==13?"٢٤":"24",w*.75f,cy+h*.09f,h*.32f,Color.WHITE,Paint.Align.CENTER,true);
        float min=cal.get(Calendar.MINUTE),hour=cal.get(Calendar.HOUR)%12+min/60f;hand(c,cx,cy,hour/12f*360-90,Math.min(w,h)*.19f,h*.027f,Color.WHITE);hand(c,cx,cy,min/60f*360-90,Math.min(w,h)*.27f,h*.020f,Color.WHITE);hand(c,cx,cy,cal.get(Calendar.SECOND)/60f*360-90,Math.min(w,h)*.31f,h*.012f,0xffff1d30);p.setColor(0xffff1d30);c.drawCircle(cx,cy,h*.019f,p);
    }

    private void drawIslamicPattern(Canvas c,float cx,float cy,float r,int mode){
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2,r*.05f));p.setColor(mode==8||mode==9?0x886f6f73:0x665f5f64);
        for(int i=0;i<8;i++){double a=Math.PI*2*i/8;float x=cx+(float)Math.cos(a)*r*.45f,y=cy+(float)Math.sin(a)*r*.45f;float s=r*.48f;c.save();c.rotate(i*45,cx,cy);c.drawRect(cx-s*.35f,cy-s*.35f,cx+s*.35f,cy+s*.35f,p);c.restore();}
        if(mode==8||mode==9){p.setStyle(Paint.Style.FILL);text(c,mode==8?"الثلت":"الله",cx,cy+r*.05f,r*.35f,0x995f5f63,Paint.Align.CENTER,false);}p.setStyle(Paint.Style.FILL);
    }

    private void drawDigital(Canvas c,float w,float h){
        int mode=data.style;String t=time(); float size=mode==19?h*.50f:(mode==17||mode==18?h*.45f:h*.54f);int col=(mode==15||mode==16)?0xff68686b:Color.WHITE;
        if(mode==14){drawWorld(c,w,h);return;}
        if(mode==18){t=arabic?toArabicDigits(t):t;}
        text(c,t,w*.5f,h*.62f,size,col,Paint.Align.CENTER,true);
        if(showDate)text(c,arabic?arabicDay()+" · "+arabicDate():new SimpleDateFormat("EEE · dd MMM",Locale.ENGLISH).format(new Date()).toUpperCase(Locale.ENGLISH),w*.5f,h*.83f,h*.09f,0xffcbcbcf,Paint.Align.CENTER,false);
    }

    private void drawWorld(Canvas c,float w,float h){
        text(c,arabic?"صنعاء":"Sana'a",w*.24f,h*.34f,h*.10f,0xffc6c6c9,Paint.Align.CENTER,false);text(c,time(),w*.24f,h*.62f,h*.20f,Color.WHITE,Paint.Align.CENTER,true);p.setColor(0xff424246);c.drawCircle(w*.5f,h*.49f,h*.19f,p);text(c,"+3",w*.5f,h*.55f,h*.15f,Color.WHITE,Paint.Align.CENTER,true);text(c,arabic?"لندن":"London",w*.76f,h*.34f,h*.10f,0xffc6c6c9,Paint.Align.CENTER,false);text(c,"09:34",w*.76f,h*.62f,h*.20f,Color.WHITE,Paint.Align.CENTER,true);
    }

    private void drawDate(Canvas c,float w,float h){
        int mode=data.style;
        if(mode==20){drawYearDots(c,w,h);return;}
        if(mode==25){text(c,arabic?"١٩ مارس":"19 March",w*.72f,h*.42f,h*.19f,Color.WHITE,Paint.Align.CENTER,true);text(c,arabic?"١٠ شعبان":"10 Sha'ban",w*.27f,h*.42f,h*.18f,Color.WHITE,Paint.Align.CENTER,false);p.setColor(0x555f5f63);c.drawRect(w*.49f,h*.20f,w*.51f,h*.70f,p);return;}
        if(mode==26){drawCalendar(c,w,h);return;}
        if(mode==27||mode==28){drawDateLine(c,w,h,mode);return;}
        if(mode==21||mode==23){text(c,mode==21?arabicDay():arabicMonth(),w*.5f,h*.56f,h*.25f,Color.WHITE,Paint.Align.CENTER,false);return;}
        if(mode==22){text(c,new SimpleDateFormat("EEEE",Locale.ENGLISH).format(new Date()).toUpperCase(Locale.ENGLISH),w*.5f,h*.59f,h*.23f,Color.WHITE,Paint.Align.CENTER,false);return;}
        if(mode==24){text(c,new SimpleDateFormat("MMMM",Locale.ENGLISH).format(new Date()).toUpperCase(Locale.ENGLISH),w*.5f,h*.59f,h*.22f,Color.WHITE,Paint.Align.CENTER,false);text(c,String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)),w*.5f,h*.42f,h*.31f,0x77ffffff,Paint.Align.CENTER,true);return;}
        text(c,String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)),w*.5f,h*.48f,h*.36f,0x88ffffff,Paint.Align.CENTER,true);text(c,arabic?arabicMonth():new SimpleDateFormat("MMMM",Locale.ENGLISH).format(new Date()).toUpperCase(Locale.ENGLISH),w*.5f,h*.69f,h*.19f,Color.WHITE,Paint.Align.CENTER,false);text(c,arabicDate(),w*.5f,h*.84f,h*.08f,0xffbbbbc0,Paint.Align.CENTER,false);
    }

    private void drawYearDots(Canvas c,float w,float h){int total=69,active=Calendar.getInstance().get(Calendar.DAY_OF_YEAR)*total/365; text(c,String.valueOf(Calendar.getInstance().get(Calendar.YEAR)),w*.12f,h*.24f,h*.10f,0xffdddddf,Paint.Align.LEFT,false);text(c,(Calendar.getInstance().get(Calendar.DAY_OF_YEAR)*100/365)+"%",w*.88f,h*.24f,h*.10f,0xffdddddf,Paint.Align.RIGHT,false);for(int i=0;i<total;i++){int cols=23;float x=w*.13f+(i%cols)*(w*.74f/(cols-1));float y=h*.43f+(i/cols)*h*.13f;p.setColor(i<active?Color.WHITE:0x334f4f4f);c.drawCircle(x,y,Math.max(2,h*.018f),p);}}
    private void drawCalendar(Canvas c,float w,float h){String[] hd=arabic?new String[]{"ح","ن","ث","ر","خ","ج","س"}:new String[]{"S","M","T","W","T","F","S"};for(int i=0;i<7;i++)text(c,hd[i],w*(.17f+i*.11f),h*.22f,h*.095f,0xffbcbcc1,Paint.Align.CENTER,true);for(int d=1;d<=31;d++){int index=d+2,row=index/7,col=index%7;float x=w*(.17f+col*.11f),y=h*(.35f+row*.13f);if(d==Calendar.getInstance().get(Calendar.DAY_OF_MONTH)){p.setColor(0xff55555a);c.drawCircle(x,y,h*.075f,p);}text(c,String.valueOf(d),x,y+h*.025f,h*.10f,d==Calendar.getInstance().get(Calendar.DAY_OF_MONTH)?Color.WHITE:0xffe2e2e4,Paint.Align.CENTER,false);}}
    private void drawDateLine(Canvas c,float w,float h,int mode){text(c,arabic?arabicDay()+"، "+arabicDate():new SimpleDateFormat("EEEE, dd MMMM yyyy",Locale.ENGLISH).format(new Date()),w*.5f,h*.43f,h*.15f,Color.WHITE,Paint.Align.CENTER,true);if(mode==28){text(c,"23°  ·  "+time()+"  ·  "+(arabic?"غائم":"Cloudy"),w*.5f,h*.72f,h*.15f,Color.WHITE,Paint.Align.CENTER,true);}}

    private void drawWeather(Canvas c,float w,float h){int mode=data.style;text(c,arabic?"صنعاء":"Sana'a",w*.11f,h*.24f,h*.10f,0xffd0d0d3,Paint.Align.LEFT,false);text(c,"29°",w*.11f,h*.57f,h*.31f,Color.WHITE,Paint.Align.LEFT,true);p.setColor(Color.WHITE);c.drawCircle(w*.81f,h*.28f,h*.095f,p);p.setColor(0xffeeeeee);c.drawOval(new RectF(w*.72f,h*.27f,w*.92f,h*.42f),p);if(mode==32){text(c,"H:31° · L:21°",w*.11f,h*.78f,h*.10f,0xffd3d3d5,Paint.Align.LEFT,false);text(c,time(),w*.84f,h*.76f,h*.15f,Color.WHITE,Paint.Align.CENTER,true);}else{text(c,arabic?"غائم جزئياً":"Partly cloudy",w*.11f,h*.78f,h*.10f,0xffd3d3d5,Paint.Align.LEFT,false);text(c,arabic?"محدّث الآن":"Updated now",w*.11f,h*.90f,h*.07f,0xff96969d,Paint.Align.LEFT,false);}}

    private void drawPrayer(Canvas c,float w,float h){int mode=data.style;if(mode==39){text(c,"الشروق",w*.5f,h*.52f,h*.26f,Color.WHITE,Paint.Align.CENTER,false);text(c,"05:34",w*.5f,h*.78f,h*.15f,0xffcfcfd4,Paint.Align.CENTER,true);return;}if(mode==40){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(h*.07f);p.setColor(0x335d5d60);c.drawCircle(w*.5f,h*.48f,h*.28f,p);p.setColor(Color.WHITE);c.drawArc(new RectF(w*.5f-h*.28f,h*.20f,w*.5f+h*.28f,h*.76f),-90,236,false,p);p.setStyle(Paint.Style.FILL);text(c,"05:34",w*.5f,h*.49f,h*.13f,Color.WHITE,Paint.Align.CENTER,true);text(c,"الشروق",w*.5f,h*.66f,h*.10f,Color.WHITE,Paint.Align.CENTER,false);return;}if(mode==34){text(c,arabic?"المغرب 05:35":"MAGHRIB 05:35",w*.5f,h*.55f,h*.19f,Color.WHITE,Paint.Align.CENTER,true);text(c,arabic?"بعد ٣س ٤٣د":"in 3h 43m",w*.5f,h*.78f,h*.11f,0xffc3c3c7,Paint.Align.CENTER,false);return;}if(mode==37||mode==38){drawPrayerDetail(c,w,h,mode);return;}String[] labels=arabic?new String[]{"الفجر","الظهر","العصر","المغرب","العشاء"}:new String[]{"Fajr","Dhuhr","Asr","Maghrib","Isha"};String[] times={"05:02","12:34","03:45","06:32","08:00"};for(int i=0;i<5;i++){float x=w*(.12f+i*.19f);text(c,labels[i],x,h*.38f,h*.085f,0xffcfcfd3,Paint.Align.CENTER,true);text(c,times[i],x,h*.63f,h*.13f,i==1?Color.WHITE:0xffb5b5bb,Paint.Align.CENTER,true);if(mode==36&&i==1){p.setColor(0xff545459);c.drawRoundRect(new RectF(x-w*.075f,h*.69f,x+w*.075f,h*.75f),h*.03f,h*.03f,p);}}}
    private void drawPrayerDetail(Canvas c,float w,float h,int mode){String[] labels=arabic?new String[]{"الفجر","الظهر","العصر","المغرب","العشاء"}:new String[]{"Fajr","Dhuhr","Asr","Maghrib","Isha"};String[] times={"05:02","12:34","03:45","06:32","08:00"};text(c,mode==37?"الشروق":"الصلاة القادمة",w*.80f,h*.28f,h*.17f,Color.WHITE,Paint.Align.CENTER,false);text(c,mode==37?"05:34":"12:34",w*.18f,h*.31f,h*.17f,Color.WHITE,Paint.Align.CENTER,true);for(int i=0;i<5;i++){float x=w*(.12f+i*.19f);p.setColor(i==1?Color.WHITE:0xff85858b);c.drawCircle(x,h*.57f,h*.022f,p);text(c,labels[i],x,h*.72f,h*.08f,0xffbfc0c3,Paint.Align.CENTER,false);text(c,times[i],x,h*.86f,h*.10f,Color.WHITE,Paint.Align.CENTER,true);}p.setColor(0x555f5f62);c.drawRoundRect(new RectF(w*.10f,h*.92f,w*.90f,h*.95f),h*.014f,h*.014f,p);p.setColor(Color.WHITE);c.drawRoundRect(new RectF(w*.10f,h*.92f,w*.53f,h*.95f),h*.014f,h*.014f,p);}

    private void drawQuote(Canvas c,float w,float h){int mode=data.style;String q=mode==41?"قال رسول الله ﷺ: من اقتطع شبراً من الأرض ظلماً طوّقه الله يوم القيامة.":mode==42?"اللهم بك أصبحنا وبك أمسينا.":mode==43?"وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ":"ضع اقتباسك الخاص هنا";text(c,q,w*.5f,h*.48f,mode==43?h*.13f:h*.12f,Color.WHITE,Paint.Align.CENTER,false);if(mode==43)text(c,"And when My servants ask you concerning Me — I am near",w*.5f,h*.72f,h*.075f,0xffc8c8cc,Paint.Align.CENTER,false);if(mode==44){text(c,"— الكاتب —",w*.5f,h*.70f,h*.09f,0xffc8c8cc,Paint.Align.CENTER,false);text(c,"“",w*.13f,h*.83f,h*.32f,0x44ffffff,Paint.Align.CENTER,false);}}
    private void drawCounter(Canvas c,float w,float h){if(data.style==45){text(c,arabic?"التسبيح":"DHIKR",w*.5f,h*.26f,h*.11f,0xffc5c5c9,Paint.Align.CENTER,false);text(c,"033",w*.5f,h*.63f,h*.40f,Color.WHITE,Paint.Align.CENTER,true);text(c,arabic?"اضغط للعد":"TAP TO COUNT",w*.5f,h*.84f,h*.09f,0xffb7b7bc,Paint.Align.CENTER,false);}else{text(c,arabic?"المناسبة":"EVENT",w*.5f,h*.25f,h*.11f,0xffc5c5c9,Paint.Align.CENTER,false);text(c,"12",w*.5f,h*.63f,h*.40f,Color.WHITE,Paint.Align.CENTER,true);text(c,arabic?"يومًا متبقيًا":"DAYS LEFT",w*.5f,h*.84f,h*.09f,0xffb7b7bc,Paint.Align.CENTER,false);}}
    private void drawSearch(Canvas c,float w,float h){p.setColor(0x33ffffff);c.drawRoundRect(new RectF(w*.07f,h*.16f,w*.93f,h*.50f),h*.17f,h*.17f,p);text(c,"G",w*.13f,h*.39f,h*.18f,Color.WHITE,Paint.Align.CENTER,true);text(c,arabic?"ابحث في Google":"Search Google",w*.22f,h*.38f,h*.10f,0xffeeeeee,Paint.Align.LEFT,false);String[] marks={"✦","文","◉","◌"};for(int i=0;i<4;i++){float x=w*(.23f+i*.18f);p.setColor(0x33ffffff);c.drawCircle(x,h*.73f,h*.13f,p);text(c,marks[i],x,h*.78f,h*.14f,Color.WHITE,Paint.Align.CENTER,false);}}
    private void drawShortcuts(Canvas c,float w,float h){String[] marks="People".equals(data.category)?new String[]{"م","أ","س","ي"}:new String[]{"☎","●●","✦","◉","◷","⌖","⚙","±","●","▰"};int total=data.style==49?10:4;for(int i=0;i<total;i++){int cols=total == 10 ? 5 : 4,row=i/cols,col=i%cols;float x=w*((total == 10 ? .16f : .17f)+col*(total == 10 ? .17f : .22f)),y=h*(total == 10 ? .34f+row*.37f : .49f);float r=h*(total == 10 ? .115f : .18f);p.setColor(VitraUi.withAlpha(accent,55));c.drawCircle(x,y,r,p);text(c,marks[i],x,y+r*.24f,r*.85f,Color.WHITE,Paint.Align.CENTER,true);}text(c,"People".equals(data.category)?(arabic?"المفضّلون":"FAVORITES"):(arabic?"تطبيقاتك":"YOUR APPS"),w*.5f,h*.89f,h*.085f,0xffbdbdc2,Paint.Align.CENTER,false);}
    private void drawSystem(Canvas c,float w,float h){if(data.style==52){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(h*.055f);p.setColor(0x334f4f4f);c.drawCircle(w*.5f,h*.48f,h*.27f,p);p.setColor(accent);c.drawArc(new RectF(w*.5f-h*.27f,h*.21f,w*.5f+h*.27f,h*.75f),-90,302,false,p);p.setStyle(Paint.Style.FILL);text(c,"84%",w*.5f,h*.55f,h*.22f,Color.WHITE,Paint.Align.CENTER,true);return;}text(c,data.style==53?"SYSTEM PULSE":"Storage 68%",w*.08f,h*.30f,h*.12f,Color.WHITE,Paint.Align.LEFT,false);p.setColor(0x334f4f4f);c.drawRoundRect(new RectF(w*.08f,h*.48f,w*.92f,h*.57f),h*.045f,h*.045f,p);p.setColor(Color.WHITE);c.drawRoundRect(new RectF(w*.08f,h*.48f,w*.64f,h*.57f),h*.045f,h*.045f,p);text(c,data.style==53?"84% · 218 GB free":"218 GB free",w*.08f,h*.78f,h*.10f,0xffa9a9af,Paint.Align.LEFT,false);}
    private void drawSports(Canvas c,float w,float h){if(data.style==55){p.setColor(0x331b8cff);c.drawRoundRect(new RectF(w*.07f,h*.25f,w*.93f,h*.72f),h*.22f,h*.22f,p);text(c,"G",w*.15f,h*.57f,h*.18f,Color.WHITE,Paint.Align.CENTER,true);text(c,arabic?"ابحث عن مباراة":"Search matches",w*.27f,h*.56f,h*.12f,0xffeeeeee,Paint.Align.LEFT,false);return;}text(c,"WORLD CUP 2026",w*.5f,h*.18f,h*.09f,Color.WHITE,Paint.Align.CENTER,true);text(c,"YEM  10:00 PM  KSA",w*.5f,h*.43f,h*.18f,Color.WHITE,Paint.Align.CENTER,true);text(c,"GROUP A",w*.5f,h*.62f,h*.08f,0xffbdbdc2,Paint.Align.CENTER,false);String[] rows={"1  Yemen     6 pts","2  Saudi      4 pts","3  Egypt       3 pts"};for(int i=0;i<3;i++)text(c,rows[i],w*.18f,h*(.76f+i*.09f),h*.075f,0xffd5d5da,Paint.Align.LEFT,false);}

    private void hand(Canvas c,float cx,float cy,float angle,float length,float width,int color){double a=Math.toRadians(angle);p.setColor(color);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(cx,cy,cx+(float)Math.cos(a)*length,cy+(float)Math.sin(a)*length,p);}
    private void text(Canvas c,String value,float x,float y,float size,int color,Paint.Align align,boolean bold){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(size);p.setTextAlign(align);p.setTypeface(android.graphics.Typeface.create("sans",bold?android.graphics.Typeface.BOLD:android.graphics.Typeface.NORMAL));String[] lines=value.split("\\n");for(int i=0;i<lines.length;i++)c.drawText(lines[i],x,y+i*size*1.25f,p);}
    private String time(){
        android.content.SharedPreferences prefs=getContext().getSharedPreferences("vitra",Context.MODE_PRIVATE);
        boolean twentyFour=prefs.getBoolean("format_24",true);
        String value=new SimpleDateFormat(twentyFour?"HH:mm":"hh:mm",Locale.getDefault()).format(new Date());
        String numeralMode=prefs.getString("numeral_mode","auto");
        if("arabic".equals(numeralMode)||("auto".equals(numeralMode)&&arabic))value=toArabicDigits(value);
        return value;
    }
    private String arabicDay(){String[] d={"الأحد","الاثنين","الثلاثاء","الأربعاء","الخميس","الجمعة","السبت"};return d[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1];}
    private String arabicMonth(){String[] m={"يناير","فبراير","مارس","أبريل","مايو","يونيو","يوليو","أغسطس","سبتمبر","أكتوبر","نوفمبر","ديسمبر"};return m[Calendar.getInstance().get(Calendar.MONTH)];}
    private String arabicDate(){Calendar c=Calendar.getInstance();return c.get(Calendar.DAY_OF_MONTH)+" "+arabicMonth()+" "+c.get(Calendar.YEAR);}
    private String toArabicDigits(String value){return value.replace('0','٠').replace('1','١').replace('2','٢').replace('3','٣').replace('4','٤').replace('5','٥').replace('6','٦').replace('7','٧').replace('8','٨').replace('9','٩');}
}
