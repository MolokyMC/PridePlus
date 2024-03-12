package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.value.BoolValue;
import net.ccbluex.liquidbounce.features.value.FloatValue;
import net.ccbluex.liquidbounce.features.value.IntegerValue;
import net.ccbluex.liquidbounce.features.value.ListValue;

@ModuleInfo(name = "Animations",description = "Blocking animations", category = ModuleCategory.RENDER)
public class Animations extends Module {
    public static final FloatValue xValue = new FloatValue("Blocking-X", 0.0F, -2.0F, 2.0F);
    public static final FloatValue yValue = new FloatValue("Blocking-Y", 0.0F, -2.0F, 2.0F);
    public static final FloatValue zValue = new FloatValue("Blocking-Z", 0.0F, -2.0F, 2.0F);
    public static final FloatValue scaleValue = new FloatValue("Blocking-scale", 0.8F, 0.1F, 1.0F);
    public static final FloatValue xhValue = new FloatValue("Held-X", 0.0F, -2.0F, 2.0F);
    public static final FloatValue yhValue = new FloatValue("Held-Y", 0.0F, -2.0F, 2.0F);
    public static final FloatValue zhValue = new FloatValue("Held-Z", 0.0F, -2.0F, 2.0F);
    public static final FloatValue scalehValue = new FloatValue("Held-scale", 0.8F, 0.1F, 1.0F);
    public static final BoolValue heldValue = new BoolValue("Held", true);
    public static final BoolValue SPValue = new BoolValue("Progress", true);
    public static final BoolValue oldSPValue = new BoolValue("Progress1.8", true);


    public static final FloatValue SpeedRotate = new FloatValue("Rotate-Speed", 1f, 0f, 10f);


    public static final ListValue transformFirstPersonRotate = new ListValue("RotateMode", new String[]{"RotateY", "RotateXY", "Custom", "None"}, "None");

    public static final FloatValue customRotate1 = new FloatValue("CustomRotateXAxis", 0, -180, 180);
    public static final FloatValue customRotate2 = new FloatValue("CustomRotateYAxis", 0, -180, 180);
    public static final FloatValue customRotate3 = new FloatValue("CustomRotateZAxis", 0, -180, 180);

    public static final IntegerValue SpeedSwing = new IntegerValue("Swing-Speed", 4, 0, 20);

    public static final ListValue Sword = new ListValue("Sword", new String[]{"Old", "1.7", "WindMill", "Push", "Smooth", "SigmaOld", "BigGod", "Jello","Flux","Rotate360","Jello2","SideDown","Test2","ETB","SigmaNew","Exhibition","Test"}, "1.7");

    public static final ListValue guiAnimations = new ListValue("Container-Animation", new String[]{"None", "Zoom", "Slide", "Smooth"}, "None");
    public static final ListValue vSlideValue = new ListValue("Slide-Vertical", new String[]{"None", "Upward", "Downward"}, "Downward");
    public static final ListValue hSlideValue = new ListValue("Slide-Horizontal", new String[]{"None", "Right", "Left"}, "Right");
    public static final IntegerValue animTimeValue = new IntegerValue("Container-AnimTime", 750, 0, 3000);
    public static final ListValue tabAnimations = new ListValue("Tab-Animation", new String[]{"None", "Zoom", "Slide"}, "Zoom");
    @Override
    public String getTag() {
        return Sword.get();
    }
}