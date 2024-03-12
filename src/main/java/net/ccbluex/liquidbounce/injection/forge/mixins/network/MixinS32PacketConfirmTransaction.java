/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import net.ccbluex.liquidbounce.Pride;
import net.ccbluex.liquidbounce.features.module.modules.misc.Disabler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SPacketConfirmTransaction.class)
public class MixinS32PacketConfirmTransaction {

    @Shadow
    private int windowId;
    @Shadow
    private short actionNumber;
    @Shadow
    private boolean accepted;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void readPacketData(PacketBuffer buf) {
        Disabler disabler2 = (Disabler) Pride.moduleManager.getModule(Disabler.class);
        this.windowId = buf.readUnsignedByte();
        this.actionNumber = buf.readShort();
        this.accepted = buf.readBoolean();
        if (disabler2.getGrimPost() && this.actionNumber < 0) {
            Disabler.getPingPackets().add((int)this.actionNumber);
        }
    }
}