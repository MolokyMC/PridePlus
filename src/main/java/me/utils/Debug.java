package me.utils;

import net.ccbluex.liquidbounce.features.module.modules.render.Animations;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public class Debug extends MinecraftInstance {



    private static void func_178103_d2()
    {
        GlStateManager.translate( Animations.xhValue.get(), Animations.yhValue.get(), Animations.zhValue.get());
        GlStateManager.translate(-0.5F, 0.2F, 0.0F);
        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(Animations.scalehValue.get(), Animations.scalehValue.get(), Animations.scalehValue.get());
    }
    private static void genCustom(float p_178096_1_, float p_178096_2_) {
        GlStateManager.translate(Animations.xhValue.get(), Animations.yhValue.get(), Animations.zhValue.get());
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, p_178096_1_ * -0.6F, 0.0F);
        GlStateManager.rotate(25F, 0.0F, 1.0F, 0.0F);
        float var3 = MathHelper.sin(p_178096_2_ * p_178096_2_ * 3.1415927F);
        float var4 = MathHelper.sin(MathHelper.sqrt(p_178096_2_) * 3.1415927F);
        GlStateManager.rotate(var3 * -15F, 0.0F, 1.0F, 0.2F);
        GlStateManager.rotate(var4 * -10F, 0.2F, 0.1F, 1.0F);
        GlStateManager.rotate(var4 * -30F, 1.3F, 0.1F, 0.2F);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
        GlStateManager.scale( new Animations().scalehValue.get(), Animations.scalehValue.get(), Animations.scalehValue.get());
    }
    public static Boolean thePlayerisBlocking = false;

    public static void transformSideFirstPersonBlock(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
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
    public static void Remix(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
            double f = 0;
            genCustom((float) f, 0.83f);
            func_178103_d2();
            f = Math.sin(swingProgress * swingProgress * Math.PI);
            double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
            float f4 = MathHelper.sin(MathHelper.sqrt(f1) * 3.83f);
        GlStateManager.translate(-0.5f, 0.2f, 0.2f);
        GlStateManager.rotate((-f4) * 0.0f, 0.0f, 0.0f, 0.0f);
        GlStateManager.rotate((-f4) * 43.0f, 58.0f, 23.0f, 45.0f);
    }
    public static void slide2(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(side * 0.25, -0.55 + equippedProg * -0.6, -0.72);
        GlStateManager.translate(side * -0.1414214, 0.20, 0.1414214);
        GlStateManager.rotate(-100.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(side * 13.365F, 0, 0, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        GlStateManager.rotate((float) (f * -20.0F), 0, 1, 0);
        GlStateManager.rotate((float) (f1 * -20.0F), 0, 0, 1);
        GlStateManager.rotate((float) (f1 * -80.0F), 1, 0, 0);

    }
    public static void WindMill(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
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
    public static void Push(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(side * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        GlStateManager.translate(side * -0.1414214, 0.08, 0.1414214);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        GlStateManager.rotate((float) (f *  -10.0F), 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate((float) (f1 * -10.0F), 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate((float) (f1 * -10.0F), 1.0F, 1.0F, 1.0F);
    }

    public static void Pride(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        double f = 0;
        genCustom((float) f, 0.90f);
        func_178103_d2();
        f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        float f4 = MathHelper.sin(MathHelper.sqrt(f1) * 3.83f);
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        // translate的第一个数值是Y第二个是Z第三个是X
        GlStateManager.translate(-0.0F, 0.4F, 0f);
        GlStateManager.translate(-0.5f, 0.2f, 0f);
        //rotate同理
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        //下面这个 第一个emmm我也不知 貌似是抖动的程度 接下来按顺序是YZX
        GlStateManager.rotate((float) (f1*100.0F), 3.0F, 3F, 1F);
    }


    public static void Vanilla(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        double f = 0;
        genCustom((float) f, 0.83f);
        func_178103_d2();
        f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        float f4 = MathHelper.sin(MathHelper.sqrt(f1) * 3.83f);
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        // translate的第一个数值是Y第二个是Z第三个是X
        GlStateManager.translate(-0.0F, 0.4F, 0f);
        GlStateManager.translate(-0.5f, 0.2f, 0f);
        //rotate同理
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        //下面这个 第一个emmm我也不知 貌似是抖动的程度 接下来按顺序是YZX
        GlStateManager.rotate((float) (f1*100.0F), 3.0F, 3F, 1F);
    }
}
