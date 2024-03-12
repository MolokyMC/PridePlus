/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.item;

import net.ccbluex.liquidbounce.Pride;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.render.Animations;
import net.ccbluex.liquidbounce.features.module.modules.render.AntiBlind;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ItemRenderer.class)
@SideOnly(Side.CLIENT)
public abstract class MixinItemRenderer {

    float delay = 0.0F;

    MSTimer rotateTimer = new MSTimer();


    @Shadow
    private ItemStack itemStackMainHand;
    @Shadow
    private ItemStack itemStackOffHand;
    @Shadow
    private float equippedProgressMainHand;
    @Shadow
    private float prevEquippedProgressMainHand;
    @Shadow
    private float equippedProgressOffHand;
    @Shadow
    private float prevEquippedProgressOffHand;

    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    protected abstract void renderMapFirstPerson(float p_187463_1_, float p_187463_2_, float p_187463_3_);

    @Shadow
    protected abstract void transformFirstPerson(EnumHandSide hand, float swingProgress);

    @Shadow
    protected abstract void transformEatFirstPerson(float p_187454_1_, EnumHandSide hand, ItemStack stack);

    @Shadow
    protected abstract void renderArmFirstPerson(float p_187456_1_, float p_187456_2_, EnumHandSide p_187456_3_);

    @Shadow
    protected abstract void renderMapFirstPersonSide(float p_187465_1_, EnumHandSide hand, float p_187465_3_, ItemStack stack);

    @Shadow
    protected abstract void transformSideFirstPerson(EnumHandSide hand, float p_187459_2_);

    @Shadow
    public abstract void renderItemSide(EntityLivingBase entitylivingbaseIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform, boolean leftHanded);

    private void doItemRenderGLTranslate() {
        GlStateManager.translate(Animations.xhValue.get(), Animations.yhValue.get(), Animations.zhValue.get());
    }

    /**
     * @author CCBlueX (superblaubeere27)
     */
    @Overwrite
    public void renderItemInFirstPerson(AbstractClientPlayer player, float p_187457_2_, float p_187457_3_, EnumHand hand, float p_187457_5_, ItemStack stack, float p_187457_7_) {
        boolean flag = hand == EnumHand.MAIN_HAND;
        EnumHandSide enumhandside = flag ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
        GlStateManager.pushMatrix();

        if (stack.isEmpty()) {
            if (flag && !player.isInvisible()) {
                this.renderArmFirstPerson(p_187457_7_, p_187457_5_, enumhandside);
            }
        } else if (stack.getItem() instanceof ItemMap) {
            if (flag && this.itemStackOffHand.isEmpty()) {
                this.renderMapFirstPerson(p_187457_3_, p_187457_7_, p_187457_5_);
            } else {
                this.renderMapFirstPersonSide(p_187457_7_, enumhandside, p_187457_5_, stack);
            }
        } else {
            if (!(stack.getItem() instanceof ItemShield)) {

                final KillAura killAura = (KillAura) Pride.moduleManager.getModule(KillAura.class);

                final Animations anim = (Animations) Pride.moduleManager.getModule(Animations.class);

                boolean flag1 = enumhandside == EnumHandSide.RIGHT;

                if (player.isHandActive() && player.getItemInUseCount() > 0 && player.getActiveHand() == hand) {
                    int j = flag1 ? 1 : -1;

                    EnumAction enumaction = killAura.getBlockingStatus() ? EnumAction.BLOCK : stack.getItemUseAction();

                    switch (enumaction) {
                        case NONE:
                            this.transformSideFirstPerson(enumhandside,0F);
                            break;
                        case BLOCK:
                            transformSideFirstPersonBlock(enumhandside, p_187457_7_, p_187457_5_);
                            //this.transformSideFirstPerson(enumhandside, p_187457_7_);
                            break;
                        case EAT:
                        case DRINK:
                            this.transformEatFirstPerson(p_187457_2_, enumhandside, stack);
                            this.transformSideFirstPerson(enumhandside, p_187457_7_);
                            break;
                        case BOW:
                            this.transformSideFirstPerson(enumhandside, p_187457_7_);
                            GlStateManager.translate((float) j * -0.2785682F, 0.18344387F, 0.15731531F);
                            GlStateManager.rotate(-13.935F, 1.0F, 0.0F, 0.0F);
                            GlStateManager.rotate((float) j * 35.3F, 0.0F, 1.0F, 0.0F);
                            GlStateManager.rotate((float) j * -9.785F, 0.0F, 0.0F, 1.0F);
                            float f5 = (float) stack.getMaxItemUseDuration() - ((float) this.mc.player.getItemInUseCount() - p_187457_2_ + 1.0F);
                            float f6 = f5 / 20.0F;
                            f6 = (f6 * f6 + f6 * 2.0F) / 3.0F;

                            if (f6 > 1.0F) {
                                f6 = 1.0F;
                            }

                            if (f6 > 0.1F) {
                                float f7 = MathHelper.sin((f5 - 0.1F) * 1.3F);
                                float f3 = f6 - 0.1F;
                                float f4 = f7 * f3;
                                GlStateManager.translate(f4 * 0.0F, f4 * 0.004F, f4 * 0.0F);
                            }

                            GlStateManager.translate(f6 * 0.0F, f6 * 0.0F, f6 * 0.04F);
                            GlStateManager.scale(1.0F, 1.0F, 1.0F + f6 * 0.2F);
                            GlStateManager.rotate((float) j * 45.0F, 0.0F, -1.0F, 0.0F);
                    }
                } else {
                    if (mc.player.getHeldItemMainhand().getItem() != null && mc.player.getHeldItemMainhand().getItem() instanceof ItemSword
                            && ((killAura.getTarget() != null && killAura.getBlockingStatus())
                            || mc.gameSettings.keyBindUseItem.pressed) && anim.getState()) {
                        GlStateManager.translate(Animations.xValue.get(), Animations.yValue.get(), Animations.zValue.get());
                        float SP = (Animations.SPValue.get() ? p_187457_7_ : 0);
                        if(Animations.Sword.get().equals("Rotate360")){
                            this.func_178096_b(0.0f, 0.95f);

                            this.func_178103_d();
                            GlStateManager.rotate(this.delay, 1.0F, 0.0F, 2.0F);
                            if (this.rotateTimer.hasTimePassed(1)) {
                                ++this.delay;
                                this.delay = this.delay + Animations.SpeedRotate.get();
                                this.rotateTimer.reset();
                            }
                            if (this.delay > 360.0F) {
                                this.delay = 0.0F;
                            }
                        }
                        if (Animations.Sword.get().equals("1.7")) {
                            transformSideFirstPersonBlock(enumhandside, SP, p_187457_5_);
                        }
                        if (Animations.Sword.get().equals("Old")) {
                            transformSideFirstPersonBlock(enumhandside, -0.1F + SP, p_187457_5_);
                        }
                        if (Animations.Sword.get().equals("Test2")) {
                            Test2(enumhandside, p_187457_7_, p_187457_5_);
                        }
                        if (Animations.Sword.get().equals("New")) {
                            New2(enumhandside, p_187457_7_, p_187457_5_);
                        }

                        if (Animations.Sword.get().equals("Jello2")) {
                            float f1 = player.getSwingProgress(p_187457_5_);
                            this.func_178096_b(0, 0.0F);
                            this.func_178103_d();
                            int alpha = (int) Math.min(255, ((System.currentTimeMillis() % 255) > 255/2 ? (Math.abs(Math.abs(System.currentTimeMillis()) % 255 - 255)) : System.currentTimeMillis() % 255)*2);
                            float f5 = (f1 > 0.5 ? 1-f1 : f1);
                            GlStateManager.translate(0.3f, -0.0f, 0.40f);
                            GlStateManager.rotate(0.0f, 0.0f, 0.0f, 1.0f);
                            GlStateManager.translate(0, 0.5f, 0);

                            GlStateManager.rotate(90, 1.0f, 0.0f, -1.0f);
                            GlStateManager.translate(0.6f, 0.5f, 0);
                            GlStateManager.rotate(-90, 1.0f, 0.0f, -1.0f);


                            GlStateManager.rotate(-10, 1.0f, 0.0f, -1.0f);
                            GlStateManager.rotate((- f5) * 10.0f, 10.0f, 10.0f, -9.0f);
                            GlStateManager.rotate(10.0f, -1.0f, 0.0f, 0.0f);

                            GlStateManager.translate(0, 0, -0.5);
                            GlStateManager.rotate(mc.player.isSwingInProgress ? -alpha/5f : 1, 1.0f, -0.0f, 1.0f);
                            GlStateManager.translate(0, 0, 0.5);
                        }
                        if (Animations.Sword.get().equals("ETB")) {
                            ETB(enumhandside, p_187457_7_, p_187457_5_);
                        }
                        if (Animations.Sword.get().equals("Test")) {
                            Test(enumhandside, p_187457_7_, p_187457_5_);
                        }
                        if (Animations.Sword.get().equals("Push")) {
                            Push(enumhandside, SP, p_187457_5_);
                        }
                        if (Animations.Sword.get().equals("WindMill")) {
                            WindMill(enumhandside, -0.2F + SP, p_187457_5_);

                        }
                        if (Animations.Sword.get().equals("Smooth")) {
                            SmoothBlock(enumhandside, SP, p_187457_5_);

                        }
                        if (Animations.Sword.get().equals("Flux")) {
                            Flux(enumhandside, SP, p_187457_5_);
                        }
                        if (Animations.Sword.get().equals("BigGod")) {
                            ETB(enumhandside, SP, p_187457_5_);

                        }
                        if (Animations.Sword.get().equals("Exhibition")) {
                            Exhi(enumhandside, SP, p_187457_5_, p_187457_7_);
                        }
                        if (Animations.Sword.get().equals("SigmaNew")) {
                            transformSideFirstPersonBlock(enumhandside,p_187457_7_, p_187457_5_);
                            GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                            float var12 = MathHelper.sin(MathHelper.sqrt(p_187457_5_) * 3.1415927F);
                            GlStateManager.rotate(var12 * -5.0F, 1.0F, 0.0F, 0.0F);
                            GlStateManager.rotate(var12 * 0.0F, 0.0F, 0.0F, 1.0F);
                            GlStateManager.rotate(var12 * 25.0F, 0.0F, 1.0F, 0.0F);
                        }
                        if (Animations.Sword.get().equals("SideDown")) {
                            transformSideFirstPersonBlock2(enumhandside, p_187457_7_, p_187457_5_);

                        }
                        if (Animations.Sword.get().equals("Sigma")) {
                            Sigma(enumhandside, p_187457_7_, p_187457_5_);
                        }
                        if (Animations.Sword.get().equals("SigmaOld")) {
                            sigmaold(enumhandside, SP, p_187457_5_);
                        }
                        if (Animations.Sword.get().equals("Tap")) {
                            tap(enumhandside, SP, p_187457_5_);
                        }
                        if (Animations.Sword.get().equals("Zoom")) {
                            Zoom(enumhandside, SP, p_187457_5_);
                        }
                        if (Animations.Sword.get().equals("Jello")) {
                            jello(enumhandside, SP, p_187457_5_);
                        }
                        GlStateManager.scale(Animations.scaleValue.get(), Animations.scaleValue.get(), Animations.scaleValue.get());
                    } else {
                        if (Animations.heldValue.get()) {
                            GlStateManager.translate(Animations.xhValue.get(), Animations.yhValue.get(), Animations.zhValue.get());
                        }
                        float f = -0.4F * MathHelper.sin(MathHelper.sqrt(p_187457_5_) * (float) Math.PI);
                        float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt(p_187457_5_) * ((float) Math.PI * 2F));
                        float f2 = -0.2F * MathHelper.sin(p_187457_5_ * (float) Math.PI);
                        int i = flag1 ? 1 : -1;
                        GlStateManager.translate((float) i * f, f1, f2);
                        this.transformSideFirstPerson(enumhandside, p_187457_7_);
                        this.transformFirstPerson(enumhandside, p_187457_5_);
                        this.rotateItemAnim();
                        if (Animations.heldValue.get()) {
                            GlStateManager.scale(Animations.scalehValue.get(), Animations.scalehValue.get(), Animations.scalehValue.get());
                        }
                    }
                }

                this.renderItemSide(player, stack, flag1 ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !flag1);

            }
        }

        GlStateManager.popMatrix();
    }

    /**
     * @author CCBlueX
     */


    @Inject(method = "renderFireInFirstPerson", at = @At("HEAD"), cancellable = true)
    private void renderFireInFirstPerson(final CallbackInfo callbackInfo) {
        final AntiBlind antiBlind = (AntiBlind) Pride.moduleManager.getModule(AntiBlind.class);

        if (antiBlind.getState() && antiBlind.getFireEffect().get()) callbackInfo.cancel();
    }


    private void Zoom(float p_178096_1_, float p_178096_2_) {
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, p_178096_1_ * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float var3 = MathHelper.sin(p_178096_2_ * p_178096_2_ * (float) Math.PI);
        float var4 = MathHelper.sin(MathHelper.sqrt(p_178096_2_) * (float) Math.PI);
        GlStateManager.rotate(var3 * -20.0F, 0.0F, 0.0F, 0.0F);
        GlStateManager.rotate(var4 * -20.0F, 0.0F, 0.0F, 0.0F);
        GlStateManager.rotate(var4 * -20.0F, 0.0F, 0.0F, 0.0F);
    }

    private void func_178103_d(float v) {
        GlStateManager.translate(-0.5F, 0.2F, 0.0F);
        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
    }

    private void tap(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        float smooth = (swingProgress * 0.8f - (swingProgress * swingProgress) * 0.8f);
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equippedProg * -0.15F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float var3 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float var4 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(smooth * -90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(0.37F, 0.37F, 0.37F);
    }

    private void rotateItemAnim() {
        if (Animations.transformFirstPersonRotate.get().equalsIgnoreCase("RotateY")) {
            GlStateManager.rotate(this.delay, 0.0F, 1.0F, 0.0F);
        }
        if (Animations.transformFirstPersonRotate.get().equalsIgnoreCase("RotateXY")) {
            GlStateManager.rotate(this.delay, 1.0F, 1.0F, 0.0F);
        }

        if (Animations.transformFirstPersonRotate.get().equalsIgnoreCase("Custom")) {
            GlStateManager.rotate(this.delay, Animations.customRotate1.get(), Animations.customRotate2.get(), Animations.customRotate3.get());
        }

        if (this.rotateTimer.hasTimePassed(1)) {
            ++this.delay;
            this.delay = this.delay + Animations.SpeedRotate.get();
            this.rotateTimer.reset();
        }
        if (this.delay > 360.0F) {
            this.delay = 0.0F;
        }
    }

    // private void Zoom(float p_178096_1_, float p_178096_2_) {
    private void Zoom(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equippedProg * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float var3 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float var4 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(var3 * -20.0F, 0.0F, 0.0F, 0.0F);
        GlStateManager.rotate(var4 * -20.0F, 0.0F, 0.0F, 0.0F);
        GlStateManager.rotate(var4 * -20.0F, 0.0F, 0.0F, 0.0F);
    }
    public void Test(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equippedProg * -0.6F, 0.0F);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        float var3 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float var4 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        float var5 = MathHelper.ceil(MathHelper.floor(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(var3 * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(var4 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(var5 * -80.0F, 1.0F, 0.0F, 0.0F);
//        GlStateManager.scale(OldHitting.scaleValue.get(), OldHitting.scaleValue.get(), OldHitting.scaleValue.get());
    }
    public void transformSideFirstPersonBlock2(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(side * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        GlStateManager.translate(side * -0.1414214, 0.08, 0.1414214);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        GlStateManager.rotate((float) (f * -20.0F), 0, 1, 0);
        GlStateManager.rotate((float) (f1 * -20.0F), 0, 0, 1);
        GlStateManager.rotate((float) (f1 * -80.0F), 1, 0, 0);
//        GlStateManager.scale(OldHitting.Scale.get(), OldHitting.Scale.get(), OldHitting.Scale.get());
    }
    public void ETB(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equippedProg * -0.6F, 0.0F);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        float var3 = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
        float var4 = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
        GlStateManager.rotate(var3 * -34.0F, 0.0F, 1.0F, 0.2F);
        GlStateManager.rotate(var4 * -20.7F, 0.2F, 0.1F, 1.0F);
        GlStateManager.rotate(var4 * -68.6F, 1.3F, 0.1F, 0.2F);
    }

    private void func_178103_d() {
        GlStateManager.translate(-0.5F, 0.2F, 0.0F);
        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
    }
    private void func_178096_b(float p_178096_1_, float p_178096_2_) {
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, p_178096_1_ * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float var3 = MathHelper.sin(p_178096_2_ * p_178096_2_ * (float) Math.PI);
        float var4 = MathHelper.sin(MathHelper.sqrt(p_178096_2_) * (float) Math.PI);
        GlStateManager.rotate(var3 * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(var4 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(var4 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(Animations.scaleValue.get(), Animations.scaleValue.get(), Animations.scaleValue.get());
    }
    private static void transformSideFirstPersonBlock(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(side * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        GlStateManager.translate(side * -0.1414214, 0.08, 0.1414214);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        GlStateManager.rotate((float) (f * -20.0F), 0, 1, 0);
        GlStateManager.rotate((float) (f1 * -20.0F), 0, 0, 1);
        GlStateManager.rotate((float) (f1 * -80.0F), 1, 0, 0);
    }

    private static void jello(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(0.56f, -0.52f, -0.71999997f);
        GlStateManager.rotate(-102.25f, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate((float) side * 13.365f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate((float) side * 78.05f, 0.0f, 0.0f, 1.0f);
        float var13 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float var14 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(var13 * -35.0f, 0.0f, 0.0f, 0.0f);
        GlStateManager.rotate(var14 * 0.0f, 0.0f, 0.0f, 0.0f);
        GlStateManager.rotate(var14 * 20.0f, 1.0f, 1.0f, 1.0f);
    }
    private static void Sigma(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(side * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        GlStateManager.translate(side * -0.1414214, 0.08, 0.1414214);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(f) * 3.1415927F);
        GlStateManager.translate(0.0F, f * -0.6F, 0.0F);
        GlStateManager.rotate((float) (-f1 * -10.0F), 0.0F, 1.0F, 0.2F);
        GlStateManager.rotate((float) (-f1 * -10.0F), 0.2F, 0.1F, 1.0F);
        GlStateManager.rotate((float) (-f1 * -10.0F), 1.3F, 0.1F, 0.2F);
    }
    private static void sigmaold(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equippedProg * -0.6F, 0.0F);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        float var3 = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
        float var4 = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
        GlStateManager.rotate(var3 * -15F, 0.0F, 1.0F, 0.2F);
        GlStateManager.rotate(var4 * -10F, 0.2F, 0.1F, 1.0F);
        GlStateManager.rotate(var4 * -30F, 1.3F, 0.1F, 0.2F);
    }

    private static void WindMill(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(side * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        GlStateManager.translate(side * -0.1414214, 0.08, 0.1414214);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        GlStateManager.rotate((float) (f * -20.0F), 0, 1, 0);
        GlStateManager.rotate((float) (f1 * -20.0F), 0, 0, 1);
        GlStateManager.rotate((float) (f1 * -50.0F), 1, 0, 0);

    }

    private static void Push(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(side * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        GlStateManager.translate(side * -0.1414214, 0.08, 0.1414214);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        GlStateManager.rotate((float) (f * -10.0F), 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate((float) (f1 * -10.0F), 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate((float) (f1 * -10.0F), 1.0F, 1.0F, 1.0F);
    }

    private static void Flux(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(side * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        GlStateManager.translate(side * -0.1414214, 0.08, 0.1414214);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        GlStateManager.rotate((float) (f * -30.0F), 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate((float) (f1 * -15.0F), 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate((float) (f1 * -15.0F), 1.0F, 1.0F, 1.0F);

    }
    private void jello(float var11, float var12) {
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.rotate(48.57f, 0f, 0.24f, 0.14f);
        float var13 = MathHelper.sin(var12 * var12 * (float) Math.PI);
        float var14 = MathHelper.sin(MathHelper.sqrt(var12) * (float) Math.PI);
        GlStateManager.rotate(var13 * -35.0F, 0.0F, 0.0F, 0.0F);
        GlStateManager.rotate(var14 * 0.0F, 0.0F, 0.0f, 0.0F);
        GlStateManager.rotate(var14 * 20.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.scale(Animations.scaleValue.get(), Animations.scaleValue.get(), Animations.scaleValue.get());
    }
    private static void Test2(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(side * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        GlStateManager.translate(side * -0.1414214, 0.08, 0.1414214);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(f) * 3.1415927F);
        GlStateManager.translate(-0.0F, 0.4F, 0.3F);
        GlStateManager.rotate((float) (-f1 * 35.0F), -8.0F, -0.0F, 9.0F);
        GlStateManager.rotate((float) (-f1 * 10.0F), 1.0F, -0.4F, -0.5F);
    }
    private static void New2(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(side * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        GlStateManager.translate(side * -0.1414214, 0.08, 0.1414214);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        GlStateManager.translate(0.0F, f * -0.6F, 0.0F);
        GlStateManager.rotate((float) (-f1 * -10.0F), 0.0F, 1.0F, 0.2F);
        GlStateManager.rotate((float) (-f1 * -10.0F), 0.2F, 0.1F, 1.0F);
        GlStateManager.rotate((float) (-f1 * -10.0F), 1.3F, 0.1F, 0.2F);
    }
    private static void SmoothBlock(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(side * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        GlStateManager.translate(side * -0.1414214, 0.08, 0.1414214);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        GlStateManager.rotate((float) (f * -20.0F), 0, 1, 0);
        GlStateManager.rotate((float) (f1 * -20.0F), 0, 0, 1);
        GlStateManager.rotate((float) (f1 * -30.0F), 1, 0, 0);
    }



    @Overwrite
    public void updateEquippedItem() {
        final Animations oldhiting = (Animations) Pride.moduleManager.getModule(Animations.class);

        this.prevEquippedProgressMainHand = this.equippedProgressMainHand;
        this.prevEquippedProgressOffHand = this.equippedProgressOffHand;
        EntityPlayerSP entityplayersp = this.mc.player;
        ItemStack itemstack = entityplayersp.getHeldItemMainhand();
        ItemStack itemstack1 = entityplayersp.getHeldItemOffhand();
        if (entityplayersp.isRowingBoat()) {
            this.equippedProgressMainHand = MathHelper.clamp(this.equippedProgressMainHand - 0.4F, 0.0F, 1.0F);
            this.equippedProgressOffHand = MathHelper.clamp(this.equippedProgressOffHand - 0.4F, 0.0F, 1.0F);
        } else {
            float f = entityplayersp.getCooledAttackStrength(1.0F);
            boolean requipM = ForgeHooksClient.shouldCauseReequipAnimation(this.itemStackMainHand, itemstack, entityplayersp.inventory.currentItem);
            boolean requipO = ForgeHooksClient.shouldCauseReequipAnimation(this.itemStackOffHand, itemstack1, -1);
            if (!requipM && !Objects.equals(this.itemStackMainHand, itemstack)) {
                this.itemStackMainHand = itemstack;
            }

            if (!requipM && !Objects.equals(this.itemStackOffHand, itemstack1)) {
                this.itemStackOffHand = itemstack1;
            }

            float number = Animations.oldSPValue.get() ? 1f : f * f * f;
            this.equippedProgressMainHand += MathHelper.clamp((!requipM ? number : 0.0F) - this.equippedProgressMainHand, -0.4F, 0.4F);
            this.equippedProgressOffHand += MathHelper.clamp((float) (!requipO ? 1 : 0) - this.equippedProgressOffHand, -0.4F, 0.4F);
        }

        if (this.equippedProgressMainHand < 0.1F) {
            this.itemStackMainHand = itemstack;
        }

        if (this.equippedProgressOffHand < 0.1F) {
            this.itemStackOffHand = itemstack1;
        }
    }
    private void Exhi(EnumHandSide p_187459_1_, float equippedProg, float swingProgress, float sb) {
        float f1 = swingProgress;
        float f = 1.0F - (this.prevEquippedProgressMainHand + (this.equippedProgressMainHand - this.prevEquippedProgressMainHand) * sb);
        this.transformFirstPersonItem(f / 2.0F, 0.0F);
        GL11.glTranslated(-0.07D, 0.58D, 0.0D);
        float var9 = MathHelper.sin(MathHelper.sqrt(f1) * 3.1415927F);
//                                GL11.glRotated(-var9 * 60.0F / 2.0F, -var9 / 2.0F, -0.0F, 9.0F);
//                                GL11.glRotated(-var9 * 53.0F, 1.0F, var9 / 2.0F, -0.0F);
        GlStateManager.rotate(f * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f1 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
//        GlStateManager.scale(OldHitting.Scale.get(), OldHitting.Scale.get(), OldHitting.Scale.get());
        this.func_178103_d();

    }
    private void transformFirstPersonItem(float equipProgress, float swingProgress) {
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float f1 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(f * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f1 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }
}