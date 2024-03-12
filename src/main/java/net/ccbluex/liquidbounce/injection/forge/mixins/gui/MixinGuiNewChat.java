/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.Pride;
import net.ccbluex.liquidbounce.features.module.modules.render.HUD;
import net.minecraft.client.gui.*;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiNewChat.class)
public abstract class MixinGuiNewChat {



    @Inject(method = "drawChat", at = @At("HEAD"), cancellable = true)
    private void drawChat(int p_drawChat_1_, final CallbackInfo callbackInfo) {
        final HUD hud = (HUD) Pride.moduleManager.getModule(HUD.class);


    }

    // TODO: Make real fix
    /*@Inject(method = "setChatLine", at = @At("HEAD"), cancellable = true)
    private void setChatLine(IChatComponent p_setChatLine_1_, int p_setChatLine_2_, int p_setChatLine_3_, boolean p_setChatLine_4_, final CallbackInfo callbackInfo) {
        final HUD hud = (HUD) LiquidBounce.moduleManager.getModule(HUD.class);

        if(hud.getState() && hud.fontChatValue.asBoolean()) {
            callbackInfo.cancel();

            if (p_setChatLine_2_ != 0) {
                this.deleteChatLine(p_setChatLine_2_);
            }

            int lvt_5_1_ = MathHelper.floor_float((float)this.getChatWidth() / this.getChatScale());
            List<IChatComponent> lvt_6_1_ = GuiUtilRenderComponents.splitText(p_setChatLine_1_, lvt_5_1_, Fonts.font40, false, false);
            boolean lvt_7_1_ = this.getChatOpen();

            IChatComponent lvt_9_1_;
            for(Iterator lvt_8_1_ = lvt_6_1_.iterator(); lvt_8_1_.hasNext(); this.drawnChatLines.add(0, new ChatLine(p_setChatLine_3_, lvt_9_1_, p_setChatLine_2_))) {
                lvt_9_1_ = (IChatComponent)lvt_8_1_.next();
                if (lvt_7_1_ && this.scrollPos > 0) {
                    this.isScrolled = true;
                    this.scroll(1);
                }
            }

            while(this.drawnChatLines.size() > 100) {
                this.drawnChatLines.remove(this.drawnChatLines.size() - 1);
            }

            if (!p_setChatLine_4_) {
                this.chatLines.add(0, new ChatLine(p_setChatLine_3_, p_setChatLine_1_, p_setChatLine_2_));

                while(this.chatLines.size() > 100) {
                    this.chatLines.remove(this.chatLines.size() - 1);
                }
            }
        }
    }*/

    @Inject(method = "getChatComponent", at = @At("HEAD"), cancellable = true)
    private void getChatComponent(int p_getChatComponent_1_, int p_getChatComponent_2_, final CallbackInfoReturnable<ITextComponent> callbackInfo) {
        final HUD hud = (HUD) Pride.moduleManager.getModule(HUD.class);

        if (hud.getState() && hud.getFontChatValue().get()) {


            callbackInfo.setReturnValue(null);
        }
    }
}
