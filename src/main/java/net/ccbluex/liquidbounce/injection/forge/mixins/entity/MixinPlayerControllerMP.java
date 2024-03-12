/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import net.ccbluex.liquidbounce.Pride;
import net.ccbluex.liquidbounce.event.AttackEvent;
import net.ccbluex.liquidbounce.event.ClickWindowEvent;
import net.ccbluex.liquidbounce.features.module.modules.exploit.AbortBreaking;
import net.ccbluex.liquidbounce.features.module.modules.misc.PostDisabler;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
@SideOnly(Side.CLIENT)
public class MixinPlayerControllerMP {

    @Shadow
    @Final
    private NetHandlerPlayClient connection;

    @Inject(method = "attackEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;syncCurrentPlayItem()V"))
    private void attackEntity(EntityPlayer entityPlayer, Entity targetEntity, CallbackInfo callbackInfo) {
        Pride.eventManager.callEvent(new AttackEvent(targetEntity));
    }

    @Inject(method = "getIsHittingBlock", at = @At("HEAD"), cancellable = true)
    private void getIsHittingBlock(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (Pride.moduleManager.getModule(AbortBreaking.class).getState())
            callbackInfoReturnable.setReturnValue(false);
    }

    /**
     * @author 驴子桥
     */
    @Overwrite
    public ItemStack windowClick(int windowId, int slotId, int mouseButton, ClickType type, EntityPlayer player)
    {
        final ClickWindowEvent event = new ClickWindowEvent(windowId, slotId, mouseButton, toInt(type));
        Pride.eventManager.callEvent(event);

        if (event.isCancelled())
            return null;


        short short1 = player.openContainer.getNextTransactionID(player.inventory);
        ItemStack itemstack = player.openContainer.slotClick(slotId, mouseButton, type, player);


        if (Pride.moduleManager.get(PostDisabler.class).getState())
            this.connection.sendPacket(new CPacketConfirmTransaction(windowId, (short) 1, true));

        this.connection.sendPacket(new CPacketClickWindow(windowId, slotId, mouseButton, type, itemstack, short1));
        return itemstack;
    }


    public int toInt(ClickType type) {
        int i = -1;
        switch (type){
            case PICKUP: i = 0; break;
            case QUICK_MOVE: i = 1; break;
            case SWAP: i = 2; break;
            case CLONE: i = 3; break;
            case THROW: i = 4; break;
            case QUICK_CRAFT: i = 5; break;
            case PICKUP_ALL: i = 6; break;
        }
        return i;
    }
}