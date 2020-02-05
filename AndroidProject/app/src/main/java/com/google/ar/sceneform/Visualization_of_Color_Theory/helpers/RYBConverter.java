package com.google.ar.sceneform.Visualization_of_Color_Theory.helpers;

// in 8-bit color depth
import java.util.Arrays;
import java.util.List;

public class RYBConverter {

    public static void main(String[] args) {
        int[] rgb = new int[]{41, 204, 239};
        int[] rybConverted = getRYB(rgb);
        int[] rgbConverted = getRGB(rybConverted);
        System.out.println(Arrays.toString(rybConverted));
        System.out.println(Arrays.toString(rgbConverted));
    }

    private static float getMin(List<Float> list) {
        if (list.size() > 0) {
            float min= list.get(0);
            for (float curr : list) {
                if (curr < min) {
                    min= curr;
                }
            }
            return min;
        } else {
            return 0;
        }
    }

    private static float getMax(List<Float> list) {
        if (list.size() > 0) {
            float max= list.get(0);
            for (float curr : list) {
                if (curr > max) {
                    max= curr;
                }
            }
            return max;
        } else {
            return 0;
        }
    }

    static int[] getRYB(int[] rgb) {
        float red= rgb[0];
        float green= rgb[1];
        float blue= rgb[2];
        float white= getMin(Arrays.asList(red, green, blue));

        red-= white; // Remove white
        green-= white;
        blue-= white;

        float magnitudeRGB= getMax(Arrays.asList(red, green, blue));

        // Yellow is mixed with red and green in RGB
        float yellow= getMin(Arrays.asList(red, green));
        red-= yellow;
        green-= yellow;

        if (green != 0.0 && blue != 0.0) { // So that doesn't exceed maximum range
            green/= 2.0;
            blue/= 2.0;
        }

        yellow+= green; // Green is mixed with yellow and blue in RYB
        blue+= green;

        float magintudeRYB= getMax(Arrays.asList(red, yellow, blue)); // Normalize
        if (magintudeRYB != 0) {
            float ratio= magnitudeRGB/magintudeRYB;
            red*= ratio;
            yellow*= ratio;
            blue*= ratio;
        }

        red+= white;
        yellow+= white;
        blue+= white;
        return new int[] {(int)red, (int)yellow, (int)blue};
    }

    static int[] getRGB(int[] ryb) {
        float red= ryb[0];
        float yellow= ryb[1];
        float blue= ryb[2];
        float white= getMin(Arrays.asList(red, yellow, blue));

        red-= white; // Remove white
        yellow-= white;
        blue-= white;

        float magnitudeRYB= getMax(Arrays.asList(red, yellow, blue));

        // Green is mixed with yellow and blue in RYB
        float green= getMin(Arrays.asList(yellow, blue));
        yellow-= green;
        blue-= green;

        if (yellow != 0.0 && blue != 0.0) { // So that doesn't exceed maximum range
            yellow/= 2.0;
            blue/= 2.0;
        }

        green+= yellow; // Yellow is mixed with red and green in RGB
        blue+= yellow;

        float magintudeRGB= getMax(Arrays.asList(red, green, blue)); // Normalize
        if (magintudeRGB != 0) {
            float ratio= magnitudeRYB/magintudeRGB;
            red*= ratio;
            green*= ratio;
            blue*= ratio;
        }

        red+= white;
        green+= white;
        blue+= white;
        return new int[] {(int)red, (int)green, (int)blue};
    }
}
