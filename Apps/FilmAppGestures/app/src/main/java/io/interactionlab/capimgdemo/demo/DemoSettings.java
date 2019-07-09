package io.interactionlab.capimgdemo.demo;

import android.graphics.Color;

/**
 * Created by Huy on 28/06/2018.
 */

public class DemoSettings {

    private static int[] colorBand(int size) {
        int[] band = new int[size];
        for (int i = 0; i < size; i++) {
            band[i] = Color.HSVToColor(new float[]{i*360.0f/(float)size, 1.0f, 1.0f});
        }
        return band;
    }

    public static ModelDescription[] models = new ModelDescription[]{
            new ModelDescription(
                    "KnuckleFinger",
                    "file:///android_asset/CNN.pb",
                    "conv2d_1_input",
                    "output_node0",
                    new long[]{1, 27, 15, 1},
                    new String[]{"Knuckle", "Finger"},
                    new int[]{Color.GREEN, Color.YELLOW}
            ),
            new ModelDescription(
                    "GestureRecognition",
                    "file:///android_asset/KnuckleFinger_LSTM_Jan_20190203_230639.pb",
                    "time_distributed_38_input",
                    "output_node0",
                    new long[]{50, 27, 15, 1},
                    new String[]{"Tap", "Two Tap", "Swipe left",
                            "Swipe right", "Swipe up", "Swipe down",
                            "Swipe up with two", "Swipe down with two", "Circle",
                            "Arrowhead left", "Arrowhead right", "Checkmark",
                            "Γ", "L", "Mirrored L", "S",
                            "Rotate"}/*, "Finger Tap", "Two Finger Tap",
                            "Finger Swipe left", "Finger Swipe right", "Finger swipe up",
                            "Finger swipe down", "Swipe up with two fingers", "Swipe down with two fingers",
                            "Finger Circle", "Finger Arrowhead left", "Finger Arrowhead right",
                            "Finger Checkmark", "Finger Γ", "Finger L", "Finger Mirrored L",
                            "Finger S", "Press and rotate finger"}*/,
                    colorBand(17)
            )
    };
}


