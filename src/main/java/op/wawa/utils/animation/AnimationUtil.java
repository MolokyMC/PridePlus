package op.wawa.utils.animation;

import ca.weblite.objc.Client;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;

public class AnimationUtil {
    public static double delta;
    public static float calculateCompensation(final float target, float current, final double speed, long delta) {

        final float diff = current - target;

        double add =  (delta * (speed / 50));

        if (diff > speed){
            if(current - add > target) {
                current -= add;
            }else {
                current = target;
            }
        }
        else if (diff < -speed) {
            if(current + add < target) {
                current += add;
            }else {
                current = target;
            }
        }
        else{
            current = target;
        }

        return current;
    }
    public static double easing(double now,double target,double speed) {
        return Math.abs(target - now) * speed;
    }
    public static double animate(double target, double current, double speed) {
        boolean larger;
        boolean bl = larger = target > current;
        if (speed < 0.0) {
            speed = 0.0;
        } else if (speed > 1.0) {
            speed = 1.0;
        }
        double dif = Math.max(target, current) - Math.min(target, current);
        double factor = dif * speed;
        if (factor < 0.1) {
            factor = 0.1;
        }
        current = larger ? (current += factor) : (current -= factor);
        return current;
    }
    public static float animate(float target, float current, float speed) {
        boolean larger;
        boolean bl = larger = target > current;
        if (speed < 0.0) {
            speed = 0.0F;
        } else if (speed > 1.0) {
            speed = 1.0F;
        }
        double dif = Math.max(target, current) - Math.min(target, current);
        double factor = dif * speed;
        if (factor < 0.1) {
            factor = 0.1;
        }
        current = larger ? (current += factor) : (current -= factor);
        return current;
    }
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

    public static float getAnimationState(float animation, float finalState, float speed) {
        final float add = (float) (delta * (speed / 1000f));
        if (animation < finalState) {
            if (animation + add < finalState) {
                animation += add;
            } else {
                animation = finalState;
            }
        } else if (animation - add > finalState) {
            animation -= add;
        } else {
            animation = finalState;
        }
        return animation;
    }

    public static float easeOut(float t, float d) {
        return (t = t / d - 1) * t * t + 1;
    }
    private static float curve(float t) {
        return AnimationUtil.clamp(t < 0.2f ? 3.125f * t * t : (t > 0.8f ? -3.125f * t * t + 6.25f * t - 2.125f : 1.25f * t - 0.125f));
    }

    private static double curve(double t) {
        return AnimationUtil.clamp(t < 0.2 ? 3.125 * t * t : (t > 0.8 ? -3.125 * t * t + 6.25 * t - 2.125 : 1.25 * t - 0.125));
    }
    private static double clamp(double t) {
        return t < 0.0 ? 0.0 : Math.min(t, 1.0);
    }

    private static float clamp(float t) {
        return t < 0.0f ? 0.0f : Math.min(t, 1.0f);
    }

}
