package tech.alomessi.vitra;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Original icon treatment presets used by Vitra shortcut widgets. */
public final class IconPackData {
    public final String id,nameAr,nameEn,subtitleAr,subtitleEn;
    public final int start,end,accent,variant;
    private IconPackData(String id,String ar,String en,String sar,String sen,int start,int end,int accent,int variant){
        this.id=id;nameAr=ar;nameEn=en;subtitleAr=sar;subtitleEn=sen;this.start=start;this.end=end;this.accent=accent;this.variant=variant;
    }
    public String name(boolean ar){return ar?nameAr:nameEn;}
    public String subtitle(boolean ar){return ar?subtitleAr:subtitleEn;}
    private static IconPackData p(String id,String ar,String en,String sar,String sen,int a,int b,int c,int v){return new IconPackData(id,ar,en,sar,sen,a,b,c,v);}
    public static final List<IconPackData> ALL=Collections.unmodifiableList(Arrays.asList(
        p("midnight","منتصف الليل","Midnight","أيقونات زجاجية أحادية هادئة","Quiet monochrome glass icons",0xff151722,0xff292d38,0xffffffff,0),
        p("ocean","المحيط","Ocean","زجاج أزرق عميق وإضاءة سماوية","Deep blue glass with cyan light",0xff080d43,0xff0b7ed1,0xff56ccff,1),
        p("vibrant","حيوي","Vibrant","ألوان مرحة بتباين عالي","Playful high-contrast colors",0xff2c1231,0xff2d8af3,0xffff5ec8,2),
        p("gold","ذهبي","Gold","أسود زجاجي مع تفاصيل ذهبية","Black glass with golden details",0xff121212,0xff3b3020,0xffffc14d,3),
        p("soft","ناعم","Soft","رمادي لؤلؤي بأيقونات سوداء","Pearl gray with black icons",0xffc5c9d0,0xff555b68,0xff151515,4),
        p("forest","غابة","Forest","أخضر داكن بلمسات طبيعية","Deep green natural accents",0xff071b14,0xff156b4b,0xff9af4b7,5)
    ));
    public static IconPackData find(String id){for(IconPackData p:ALL)if(p.id.equals(id))return p;return ALL.get(0);}
    private IconPackData(){throw new AssertionError("No instances");}
}
