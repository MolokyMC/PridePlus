/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.utils.render;

import static java.lang.Math.pow;
import static java.lang.Math.sin;

public class AnimationUtils {

    public static float lstransition(float now, float desired, double speed) {
        final double dif = Math.abs(desired - now);
        float a = (float) Math.abs((desired - (desired - (Math.abs(desired - now)))) / (100 - (speed * 10)));
        float x = now;

        if (dif > 0) {
            if (now < desired)
                x += a * RenderUtils.deltaTime;
            else if (now > desired)
                x -= a * RenderUtils.deltaTime;
        } else
            x = desired;

        if(Math.abs(desired - x) < 10.0E-3 && x != desired)
            x = desired;

        return x;
    }
    /**
     * In-out-easing function
     * https://github.com/jesusgollonet/processing-penner-easing
     *
     * @param t Current iteration
     * @param d Total iterations
     * @return Eased value
     */
    public static float easeOut(float t, float d) {
        return (t = t / d - 1) * t * t + 1;
    }

    /**
     * Source: https://easings.net/#easeOutElastic
     *
     * @return A value larger than 0
     */
    public static float easeOutElastic(float x) {
        double c4 = (2 * Math.PI) / 3.0f;

        return x == 0
                ? 0
                : (float) (x == 1
                ? 1
                : pow(2, -10 * x) * sin((x * 10 - 0.75) * c4) + 1);

    }
    public static float changer(float current, float add, float min, float max) {
        current += add;
        if (current > max) {
            current = max;
        }
        if (current < min) {
            current = min;
        }

        return current;
    }
}
