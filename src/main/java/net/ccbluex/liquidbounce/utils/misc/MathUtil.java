package net.ccbluex.liquidbounce.utils.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MathUtil {
    public static Random random = new Random();
    public static float wrapAngleTo180_float(float value)
    {
        value = value % 360.0F;

        if (value >= 180.0F)
        {
            value -= 360.0F;
        }

        if (value < -180.0F)
        {
            value += 360.0F;
        }

        return value;
    }
    public static double toDecimalLength(double in, int places) {
        return Double.parseDouble(String.format("%." + places + "f", in));
    }
    public static double roundToHalf(double d) {
        return (double)Math.round(d * 2.0) / 2.0;
    }
    public static double incValue(double val, double inc) {
        double one = 1.0 / inc;
        return (double)Math.round(val * one) / one;
    }
    public static double round(double in, int places) {
        places = (int) MathHelper.clamp(places, 0.0, 2.147483647E9);
        return Double.parseDouble(String.format("%." + places + "f", in));
    }

    public static String removeColorCode(String text) {
        String finalText = text;
        if (text.contains("§")) {
            for (int i = 0; i < finalText.length(); ++i) {
                if (Character.toString(finalText.charAt(i)).equals("§")) {
                    try {
                        String part1 = finalText.substring(0, i);
                        String part2 = finalText.substring(Math.min(i + 2, finalText.length()));
                        finalText = part1 + part2;
                    } catch (Exception var5) {
                    }
                }
            }
        }

        return finalText;
    }

    public static int reAlpha(int color, float alpha) {
        Color c = new Color(color);
        float r = 0.003921569F * (float) c.getRed();
        float g = 0.003921569F * (float) c.getGreen();
        float b = 0.003921569F * (float) c.getBlue();
        return (new Color(r, g, b, alpha)).getRGB();
    }

    public static boolean parsable(String s, byte type) {
        try {
            switch (type) {
                case 0: {
                    Short.parseShort(s);
                    break;
                }
                case 1: {
                    Byte.parseByte(s);
                    break;
                }
                case 2: {
                    Integer.parseInt(s);
                    break;
                }
                case 3: {
                    Float.parseFloat(s);
                    break;
                }
                case 4: {
                    Double.parseDouble(s);
                    break;
                }
                case 5: {
                    Long.parseLong(s);
                }
                default: {
                    break;
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static double square(double in) {
        return in * in;
    }

    public static double randomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }



    public static float randomFloat(float min, float max) {
        return (min + random.nextFloat() * (max - min));
    }

    public static double getIncremental(double val, double inc) {
        double one = 1 / inc;
        return Math.round(val * one) / one;
    }

    public static double randomNumber(double max, double min) {
        return Math.random() * (max - min) + min;
    }

    public static double getRandom(double d, double d2) {
        double d3;
        Random random = new Random();
        double d4 = d2 - d;
        double d5 = random.nextDouble() * d4;
        if (d5 > d2) {
            d5 = d2;
        }
        if ((d3 = d5 + d) > d2) {
            d3 = d2;
        }
        return d3;
    }

    public static int getRandom(int n, int n2) {
        if (n2 < n) {
            return 0;
        }
        return n + random.nextInt(n2 - n + 1);
    }

    public static Random getRandom() {
        return random;
    }


    public static class NumberType {
        public static final byte SHORT = 0;
        public static final byte BYTE = 1;
        public static final byte INT = 2;
        public static final byte FLOAT = 3;
        public static final byte DOUBLE = 4;
        public static final byte LONG = 5;

        public static byte getByType(Class<?> cls) {
            if (cls == Short.class) {
                return 0;
            }
            if (cls == Byte.class) {
                return 1;
            }
            if (cls == Integer.class) {
                return 2;
            }
            if (cls == Float.class) {
                return 3;
            }
            if (cls == Double.class) {
                return 4;
            }
            if (cls == Long.class) {
                return 5;
            }
            return -1;
        }
    }

}



