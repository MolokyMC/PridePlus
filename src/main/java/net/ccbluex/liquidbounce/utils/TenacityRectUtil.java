package net.ccbluex.liquidbounce.utils;

//import cc.paimon.utils.RoundedUtil;
//import cn.liying.utils.tena.render.ColorUtil;



import net.ccbluex.liquidbounce.utils.render.ColorUtil;
import net.ccbluex.liquidbounce.utils.render.RoundedUtil;

import java.awt.*;

public class TenacityRectUtil {
    public static void drawRect(int x,int y,int x1,int y1){
        Color gradientColor1 = Color.WHITE, gradientColor2 = Color.WHITE, gradientColor3 = Color.WHITE, gradientColor4 = Color.WHITE;
        gradientColor1 = ColorUtil.interpolateColorsBackAndForth(15, 0, getClientColor(), getAlternateClientColor(), false);
        gradientColor2 = ColorUtil.interpolateColorsBackAndForth(15, 90, getClientColor(), getAlternateClientColor(), false);
        gradientColor3 = ColorUtil.interpolateColorsBackAndForth(15, 180, getClientColor(), getAlternateClientColor(), false);
        gradientColor4 = ColorUtil.interpolateColorsBackAndForth(15, 270, getClientColor(), getAlternateClientColor(), false);
        RoundedUtil.drawGradientRound(x,y,x + x1,y + y1,6,ColorUtil.applyOpacity(gradientColor4, .85f), gradientColor1, gradientColor3, gradientColor2);

    }

    public static final Color getClientColor() {
        return new Color(236, 133, 209);
    }

    public static final Color getAlternateClientColor() {
        return new Color(28, 167, 222);
    }
}
