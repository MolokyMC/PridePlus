package op.wawa.sound;


import net.minecraft.client.Minecraft;


public class Sound {
    public static Sound INSTANCE = new Sound();
    public static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean notificationsAllowed = false;
    public static void notificationsAllowed(boolean value) {
        notificationsAllowed = value;
    }
    public void Spec(){
        new SoundPlayer().playSound(SoundPlayer.SoundType.SPECIAL, 15F);
    }
}
