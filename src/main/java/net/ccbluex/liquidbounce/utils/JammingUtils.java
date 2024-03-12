package net.ccbluex.liquidbounce.utils;

import com.google.common.base.Charsets;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import net.minecraft.entity.player.EntityPlayer;

import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Skid or Made By WaWa
 *
 * @author WaWa
 * @date 2023/7/10 14:18
 */
public class JammingUtils {
    public static final JammingUtils INSTANCE;

    public static void SendMsg( String ip, String port, String msg, String name, EntityPlayer entity) {
        Base64.Encoder var10000 = Base64.getEncoder();
        String dsfs132 = entity.getGameProfile().getName() + "," + entity.getGameProfile().getId().toString() + "," + msg + "," + name;
        Charset UTF_8 = Charsets.UTF_8;
        boolean dsf32 = false;
        if (dsfs132 == null) {
            throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
        } else {
            byte[] Bytes = dsfs132.getBytes(UTF_8);
            byte[] var10 = Bytes;
            dsfs132 = var10000.encodeToString(var10);
            if (dsfs132 == null) {
                throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
            } else {
                Bytes = dsfs132.getBytes(UTF_8);
                byte[] sendBytes = Bytes;

                try {
                    dsf32 = false;
                    int port1 = Integer.parseInt(port);
                    Socket socketClient = new Socket(ip, port1);
                    socketClient.getOutputStream().write(sendBytes, 0, sendBytes.length);
                    socketClient.close();
                } catch (Exception var15) {
                    ClientUtils.displayChatMessage(var15.toString());
                }

            }
        }
    }

    public static void SendMsg(String ip, String port, String msg, String name, String player, String id) {
        Base64.Encoder var10000 = Base64.getEncoder();
        String var7 = player + ',' + id + ',' + msg + ',' + name;
        Charset var8 = Charsets.UTF_8;
        boolean var9 = false;
        if (var7 == null) {
            throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
        } else {
            byte[] var18 = var7.getBytes(var8);
            Intrinsics.checkExpressionValueIsNotNull(var18, "(this as java.lang.String).getBytes(charset)");
            byte[] var11 = var18;
            String var19 = var10000.encodeToString(var11);
            Intrinsics.checkExpressionValueIsNotNull(var19, "Base64.getEncoder().enco\u2026yteArray(Charsets.UTF_8))");
            var7 = var19;
            var8 = Charsets.UTF_8;
            var9 = false;
            if (var7 == null) {
                throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
            } else {
                var18 = var7.getBytes(var8);
                Intrinsics.checkExpressionValueIsNotNull(var18, "(this as java.lang.String).getBytes(charset)");
                byte[] sendBytes = var18;

                try {
                    var9 = false;
                    int var13 = Integer.parseInt(port);
                    Socket socketClient = new Socket(ip, var13);
                    socketClient.getOutputStream().write(sendBytes, 0, sendBytes.length);
                    socketClient.close();
                } catch (Exception var16) {
                    ClientUtils.displayChatMessage(var16.toString());
                }

            }
        }
    }

    private JammingUtils() {
    }

    static {
        JammingUtils a114514 = new JammingUtils();
        INSTANCE = a114514;
    }

}
