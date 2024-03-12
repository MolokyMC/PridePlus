/*
 * LiquidBounce++ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/TheMosKau/LiquidBouncePlusPlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan;

import net.ccbluex.liquidbounce.Pride;
import net.ccbluex.liquidbounce.event.EventState;
import net.ccbluex.liquidbounce.event.MotionEvent;
import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed;
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class VulcanHop2 extends SpeedMode {

    public VulcanHop2() {
        super("VulcanHop2");
    }

    private int groundTick = 0;

    @Override
    public void onMotion(@NotNull MotionEvent event) {
        final Speed speed = (Speed) Pride.moduleManager.getModule(Speed.class);

        if(speed == null || event.getEventState() != EventState.PRE)
            return;
        int L = 0;

        if (MovementUtils.isMoving()) {
            ((IMixinTimer) mc.timer).setTimerSpeed((Objects.requireNonNull(mc.player).motionY > 0) ? 1.65f : 0.73f);

            if (mc.player.onGround) {
                if (groundTick >= 0) {
                        MovementUtils.strafe(0.483f);
                    }
                    if (0.42 != 0) {
                        mc.player.motionY = 0.42;
                    }
                groundTick++;
            } else {
                groundTick = 0;
                L += 0.0 * 0.03;
                mc.player.motionY = L;
            }
        } 
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        ((IMixinTimer) mc.timer).setTimerSpeed(1F);
        super.onDisable();
    }

    @Override
    public void onUpdate() {

    }

    @Override
    public void onMove(MoveEvent event) {
    }
}
