package com.google.ar.sceneform.Visualization_of_Color_Theory.helpers;

import android.graphics.Color;
import android.graphics.Bitmap;

import android.util.Log;

public class ImageChannelDataPoint {
    private static final String TAG = "DataPointsGetter";

    public enum Line_State {ROW, COLUMN};

    public static int[] getDataPoints(Bitmap img, Line_State state, int noLine, int channel, int unit) {
        Log.i(TAG, "getDataPoints() called with image: " + img + ", state " + state.toString()
                                + " at line " + noLine + " with channel " + channel
                                + " at density of " + unit);
        int[] line = getLine(img, state, noLine, channel);
        line = segmentLine(line, unit);
        StringBuilder strLine = new StringBuilder();
        for (int i : line) {
            strLine.append(i);
            strLine.append(" ");
        }
        Log.i(TAG, "getDataPoints() returned with line = " + strLine);
        return line;
    }

    // That line of equivalent RYB values.
    private static int[] getLine(Bitmap img, Line_State state, int noLine, int channel) {
        int[] line;
        if (state == Line_State.ROW) {
            line = new int[img.getWidth()];
            for (int col = 0; col < img.getWidth(); col++) {
                line[col] = RYBConverter.getRYB(getColor(img, noLine, col))[channel];
            }
        } else {
            line = new int[img.getHeight()];
            for (int row = 0; row < img.getHeight(); row++) {
                line[row] = RYBConverter.getRYB(getColor(img, row, noLine))[channel];
            }
        }
        return line;
    }

    private static int[] getColor(Bitmap img, int row, int col) {
//        Log.i(TAG, "getColor() called on " + img.toString() + "[" + row + "][" + col +"]. ");
        int color = img.getPixel(col, row);
        return new int[] {Color.red(color), Color.green(color), Color.blue(color)};
    }

    private static int[] segmentLine(int[] line, int unit) {
        int len_line = line.length;
        int[] lineSegmented = new int[len_line / unit];
        int iStart;
        for (int i = 0; i < lineSegmented.length; i++) {
            iStart = i * unit;
            lineSegmented[i] = getAvg(line, iStart,
                                            iStart + unit < len_line ? // Checks last iteration
                                                    iStart + unit : len_line - 1);
        }
        return lineSegmented;
    }

    // Inclusive
    private static int getAvg(int[] line, int iStart, int iEnd) {
        int sum = 0;
        for (int i = iStart; i <= iEnd; i++) {
            sum += line[i];
        }
        return sum/(iEnd - iStart + 1);
    }
}
