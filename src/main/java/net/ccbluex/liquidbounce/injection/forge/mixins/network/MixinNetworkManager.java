/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import me.utils.PacketUtils;
import net.ccbluex.liquidbounce.Pride;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.features.module.modules.exploit.Clip;
import net.ccbluex.liquidbounce.features.module.modules.misc.Disabler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.server.SPacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Shadow
    private Channel channel;

    @Shadow
    private INetHandler packetListener;

    /**
     * @author
     * @reason
     */
    @Overwrite
    protected void channelRead0(ChannelHandlerContext p_channelRead0_1_, Packet<?> p_channelRead0_2_) {
        if (this.channel.isOpen()) {
            try {
                Packet<INetHandler> packet = (Packet<INetHandler>) p_channelRead0_2_;
                Disabler disabler2 = (Disabler) Pride.moduleManager.getModule(Disabler.class);
                if (p_channelRead0_2_ instanceof SPacketCustomPayload) {
                    final PacketEvent event = new PacketEvent(p_channelRead0_2_);
                    Pride.eventManager.callEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }
                    packet.processPacket(this.packetListener);

                } else if (disabler2.getGrimPost() && disabler2.grimPostDelay(p_channelRead0_2_)) {
                    Minecraft.getMinecraft().addScheduledTask(() -> Disabler.getStoredPackets().add(packet));
                } else {
                    final PacketEvent event = new PacketEvent(p_channelRead0_2_);
                    Pride.eventManager.callEvent(event);
                    if (event.isCancelled()){
                        return;}
                    packet.processPacket(this.packetListener);
                }
            }
            catch (ThreadQuickExitException ex) {
            }
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void send(Packet<?> packet, CallbackInfo callback) {
        if (!PacketUtils.isPacketSend) {
            final PacketEvent event = new PacketEvent(packet);
            Pride.eventManager.callEvent(event);

            if (event.isCancelled())
                callback.cancel();
        }
    }
}