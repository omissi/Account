package tech.alomessi.vitra;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Built-in original abstract wallpapers rendered locally by WallpaperPreviewView. */
public final class WallpaperData {
    public final String id, nameAr, nameEn, category;
    public final int start, end, glow;
    public final int pattern;

    private WallpaperData(String id, String ar, String en, String category,
                          int start, int end, int glow, int pattern) {
        this.id=id; this.nameAr=ar; this.nameEn=en; this.category=category;
        this.start=start; this.end=end; this.glow=glow; this.pattern=pattern;
    }
    public String name(boolean arabic){return arabic?nameAr:nameEn;}

    private static WallpaperData w(String id,String ar,String en,String cat,int a,int b,int c,int pattern){
        return new WallpaperData(id,ar,en,cat,a,b,c,pattern);
    }

    public static final List<String> CATEGORIES=Collections.unmodifiableList(Arrays.asList("All","Dark","Light","Colorful","Nature"));
    public static final List<WallpaperData> ALL=Collections.unmodifiableList(Arrays.asList(
        w("aurora-blue","هالة زرقاء","Blue Aura","Colorful",0xff03142c,0xff1049ba,0xff7b68ff,0),
        w("aurora-orange","وهج نحاسي","Copper Aura","Colorful",0xff260b05,0xffa13309,0xffffb151,0),
        w("aurora-violet","نور بنفسجي","Violet Halo","Colorful",0xff0a0c34,0xff4929a8,0xff8c9bff,0),
        w("violet-fold","طيّات بنفسجية","Violet Folds","Dark",0xff140a1a,0xff4c236f,0xffa69bff,1),
        w("sunset-fold","طيّات غروب","Sunset Folds","Light",0xff551700,0xffef9d45,0xffffeed0,1),
        w("ice-fold","طيّات جليدية","Ice Folds","Light",0xff70bdca,0xffd7fcff,0xff546fd0,1),
        w("cyan-ribbon","شريط سماوي","Cyan Ribbon","Light",0xffd8eff1,0xff0ea6b9,0xff37e3ed,2),
        w("indigo-ribbon","شريط نيلي","Indigo Ribbon","Dark",0xff111317,0xff3444a1,0xff8494ff,2),
        w("gold-ribbon","شريط ذهبي","Gold Ribbon","Dark",0xff111111,0xff77601b,0xffffd55e,2),
        w("night-mountain","جبال ليلية","Night Mountains","Nature",0xff020912,0xff0a3358,0xff9dcbff,3),
        w("rose-cloud","سحب وردية","Rose Cloud","Colorful",0xff241329,0xff8d486c,0xffffb9dd,3),
        w("emerald-wave","موج زمردي","Emerald Wave","Nature",0xff071a11,0xff1b7d59,0xffbdff88,4),
        w("obsidian","سبج زجاجي","Obsidian Glass","Dark",0xff080808,0xff292a2d,0xffd9d9df,4),
        w("twilight","شفق بارد","Cold Twilight","Dark",0xff071323,0xff1d5872,0xff98deff,4),
        w("lavender-sky","سماء خزامية","Lavender Sky","Light",0xff8bc1e7,0xffe6d2ff,0xfffed3f5,3)
    ));

    public static WallpaperData find(String id){for(WallpaperData w:ALL)if(w.id.equals(id))return w;return ALL.get(0);}
    private WallpaperData(){throw new AssertionError("No instances");}
}
