/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.utils;

import com.google.gson.JsonObject;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.client.CPacketEncryptionResponse;
import net.minecraft.network.login.server.SPacketEncryptionRequest;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.awt.*;
import java.security.PublicKey;

@SideOnly(Side.CLIENT)
public final class ClientUtils extends MinecraftInstance {

    private static final Logger logger = LogManager.getLogger("PridePlus NextGen");

    public static Logger getLogger() {
        return logger;
    }

    public static int reAlpha(int color, float alpha) {
        Color c = new Color(color);
        float r = 0.003921569f * (float)c.getRed();
        float g = 0.003921569f * (float)c.getGreen();
        float b = 0.003921569f * (float)c.getBlue();
        return new Color(r, g, b, alpha).getRGB();
    }
    public static void sendEncryption(final NetworkManager networkManager, final SecretKey secretKey, final PublicKey publicKey, final SPacketEncryptionRequest encryptionRequest) {
        networkManager.sendPacket(new CPacketEncryptionResponse(secretKey, publicKey, encryptionRequest.getVerifyToken()), new GenericFutureListener<Future<? super Void>>() {
            public void operationComplete(Future<? super Void> p_operationComplete_1_) throws Exception {
                networkManager.enableEncryption(secretKey);
            }
        });
    }

    public static void displayChatMessage(final String message) {
        if (mc.player == null) {
            getLogger().info("(MCChat)" + message);
            return;
        }

        mc.player.sendMessage(new TextComponentString(message));
    }
}