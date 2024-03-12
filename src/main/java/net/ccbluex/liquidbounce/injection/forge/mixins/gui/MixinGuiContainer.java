package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.features.module.modules.player.InvManager;
import op.wawa.utils.animation.EaseUtils;
import net.ccbluex.liquidbounce.Pride;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.render.Animations;
import net.ccbluex.liquidbounce.features.module.modules.world.ChestStealer;
import net.ccbluex.liquidbounce.injection.implementations.IMixinGuiContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer extends MixinGuiScreen implements IMixinGuiContainer {

    private GuiButton stealButton, chestStealerButton, invManagerButton, killAuraButton;

    private float progress = 0.0f;
    private long lastMS = 0;

    private long guiOpenTime = -1;

    @Shadow
    protected abstract void handleMouseClick(Slot p_handleMouseClick_1_, int p_handleMouseClick_2_, int p_handleMouseClick_3_, ClickType p_handleMouseClick_4_);


    @Inject(method = {"initGui"}, at = {@At("HEAD")}, cancellable = true)
    public void injectInitGui(CallbackInfo callbackInfo) {

        int firstY = 0;

        buttonList.add(killAuraButton = new GuiButton(1024576, 5, 5, 140, 20, "Disable KillAura"));
        firstY += 30;

        buttonList.add(invManagerButton = new GuiButton(321123, 5, 5 + firstY, 140, 20, "Disable InvManager"));
        firstY += 30;

        buttonList.add(chestStealerButton = new GuiButton(727, 5, 5 + firstY, 140, 20, "Disable ChestStealer"));
        firstY += 30;

    }


    @Inject(method = {"drawScreen"}, at = {@At("HEAD")},cancellable = true)
    protected void drawScreenHead(CallbackInfo callbackInfo) {
        final Animations animMod = (Animations) Pride.moduleManager.getModule(Animations.class);
        if (progress >= 1F) progress = 1F;
        else progress = (float)(System.currentTimeMillis() - lastMS) / (float) Animations.animTimeValue.get();
        double trueAnim = EaseUtils.easeOutQuart(progress);
        if (animMod != null && animMod.getState()) {
            GL11.glPushMatrix();
            switch (Animations.guiAnimations.get()) {
                case "Zoom":
                    GL11.glTranslated((1 - trueAnim) * (width / 2D), (1 - trueAnim) * (height / 2D), 0D);
                    GL11.glScaled(trueAnim, trueAnim, trueAnim);
                    break;
                case "Slide":
                    switch (Animations.hSlideValue.get()) {
                        case "Right":
                            GL11.glTranslated((1 - trueAnim) * -width, 0D, 0D);
                            break;
                        case "Left":
                            GL11.glTranslated((1 - trueAnim) * width, 0D, 0D);
                            break;
                    }
                    switch (Animations.vSlideValue.get()) {
                        case "Upward":
                            GL11.glTranslated(0D, (1 - trueAnim) * height, 0D);
                            break;
                        case "Downward":
                            GL11.glTranslated(0D, (1 - trueAnim) * -height, 0D);
                            break;
                    }
                    break;
                case "Smooth":
                    GL11.glTranslated((1 - trueAnim) * -width, (1 - trueAnim) * -height / 4F, 0D);
                    break;
            }
        }

    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    public void drawScreenReturn(CallbackInfo callbackInfo) {
        final Animations animMod = (Animations) Pride.moduleManager.getModule(Animations.class);
        ChestStealer chestStealer = (ChestStealer) Pride.moduleManager.getModule(ChestStealer.class);
        final Minecraft mc = Minecraft.getMinecraft();

        if (animMod != null && animMod.getState())
            GL11.glPopMatrix();
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void mouseClicked(int mouseX, int mouseY, int mouseButton,CallbackInfo callbackInfo) {
        for (Object aButtonList : this.buttonList) {
            GuiButton var52 = (GuiButton) aButtonList;
            if (var52.mousePressed(this.mc, mouseX, mouseY) && var52.id == 1024576) {
                Pride.moduleManager.getModule(KillAura.class).setState(false);
            }
            if (var52.mousePressed(this.mc, mouseX, mouseY) && var52.id == 321123) {
                Pride.moduleManager.getModule(InvManager.class).setState(false);
            }
            if (var52.mousePressed(this.mc, mouseX, mouseY) && var52.id == 727) {
                Pride.moduleManager.getModule(ChestStealer.class).setState(false);
            }
        }
    }

    @Override
    public void publicHandleMouseClick(Slot slot, int slotNumber, int clickedButton, ClickType clickType) {

        this.handleMouseClick(slot, slotNumber, clickedButton, clickType);
    }

}
