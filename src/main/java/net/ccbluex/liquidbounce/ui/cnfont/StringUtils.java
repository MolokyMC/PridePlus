package net.ccbluex.liquidbounce.ui.cnfont;

import java.util.concurrent.ThreadLocalRandom;

public class StringUtils {
    public static String randomStringDefault(int length) {
        return randomString("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_", length);
    }

    public static String randomString(String pool, int length) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; ++i) {
            builder.append(pool.charAt(ThreadLocalRandom.current().nextInt(0, pool.length() - 1)));
        }

        return builder.toString();
    }

    public static boolean isBlank(String s) {
        if (s == null) {
            return true;
        } else {
            for (int i = 0; i < s.length(); ++i) {
                if (!Character.isWhitespace(s.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    private static boolean isEmojiCharacter(char codePoint) {
        return codePoint == 0 || codePoint == '\t' || codePoint == '\n' || codePoint == '\r' || codePoint >= ' ' && codePoint <= '\ud7ff' || codePoint >= '\ue000' && codePoint <= '\ufffd' || codePoint >= 65536 && codePoint <= 1114111;
    }

    public static String filterEmoji(String source) {
        if (isBlank(source)) {
            return source;
        } else {
            StringBuilder buf = null;
            int len = source.length();

            for (int i = 0; i < len; ++i) {
                char codePoint = source.charAt(i);
                if (isEmojiCharacter(codePoint)) {
                    if (buf == null) {
                        buf = new StringBuilder(source.length());
                    }

                    buf.append(codePoint);
                }
            }

            if (buf == null) {
                return source;
            } else if (buf.length() == len) {
                return source;
            } else {
                return buf.toString();
            }
        }
    }
}

