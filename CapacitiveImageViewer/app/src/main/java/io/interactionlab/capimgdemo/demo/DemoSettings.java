package io.interactionlab.capimgdemo.demo;

import android.graphics.Color;

/**
 * Created by Huy on 28/06/2018.
 */

public class DemoSettings {

    private static int[] colorBand(int size) {
        int[] band = new int[size];
        for (int i = 0; i < size; i++) {
            band[i] = Color.HSVToColor(new float[]{i/(float)size, 1.0f, 1.0f});
        }
        return band;
    }

    public static ModelDescription[] models = new ModelDescription[]{
            new ModelDescription(
                    "KnuckleFinger",
                    "file:///android_asset/10_01_19.pb",
                    "conv2d_1_input",
                    "output_node0",
                    new long[]{1, 27, 15, 1},
                    new String[]{"Knuckle", "Finger"},
                    new int[]{Color.GREEN, Color.YELLOW}
                    ),
            new ModelDescription(
                    "GestureRecognition",
                    "file:///android_asset/lstm.pb",
                    "conv2d_1_input",
                    "output_node0",
                    new long[]{1, 30, 27, 15, 1},
                    new String[]{"Knuckle Tap", "Finger Tap"},
                    colorBand(34)
                    )
    };
}


