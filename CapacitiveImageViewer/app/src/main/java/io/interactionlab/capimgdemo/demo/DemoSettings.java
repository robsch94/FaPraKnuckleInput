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
                    "file:///android_asset/1110_01_19.pb",
                    "conv2d_1_input",
                    "output_node0",
                    new long[]{1, 27, 15, 1},
                    new String[]{"Knuckle", "Finger"},
                    new int[]{Color.GREEN, Color.YELLOW}
            ),
            new ModelDescription(
                    "GestureRecognition LSTM + CNN",
                    "file:///android_asset/lstm_cnn_01_02_19.pb",
                    "A0",
                    "output_node0",
                    new long[]{30, 27, 15, 1},
                    new String[]{"Tap", "Two Tap", "Swipe left",
                            "Swipe right", "Swipe up", "Swipe down",
                            "Swipe up with two", "Swipe down with two", "Circle",
                            "Arrowhead left", "Arrowhead right", "Checkmark",
                            "Γ", "L", "Mirrored L", "S",
                            "Press and rotate", "Knuckle", "Finger"}/*, "Finger Tap", "Two Finger Tap",
                            "Finger Swipe left", "Finger Swipe right", "Finger swipe up",
                            "Finger swipe down", "Swipe up with two fingers", "Swipe down with two fingers",
                            "Finger Circle", "Finger Arrowhead left", "Finger Arrowhead right",
                            "Finger Checkmark", "Finger Γ", "Finger L", "Finger Mirrored L",
                            "Finger S", "Press and rotate finger"}*/,
                    colorBand(17)
            ),
            new ModelDescription(
                    "GestureRecognition LSTM + CNN",
                    "file:///android_asset/11Gestures.pb",
                    "conv2d_5_input",
                    "output_node0",
                    new long[]{30, 27, 15, 1},
                    new String[]{"Knuckle Tap", "Two Knuckle Tap", "Knuckle Swipe left",
                            "Knuckle Swipe right", "Knuckle Swipe up", "Knuckle Swipe down",
                            "Swipe up with two knuckles", "Swipe down with two knuckles", "Knuckle Circle",
                            "Knuckle Arrowhead left", "Knuckle Arrowhead right", "Knuckle Checkmark",
                            "Knuckle Γ", "Knuckle L", "Knuckle Mirrored L", "Knuckle S",
                            "Press and rotate knuckle"}/*, "Finger Tap", "Two Finger Tap",
                            "Finger Swipe left", "Finger Swipe right", "Finger swipe up",
                            "Finger swipe down", "Swipe up with two fingers", "Swipe down with two fingers",
                            "Finger Circle", "Finger Arrowhead left", "Finger Arrowhead right",
                            "Finger Checkmark", "Finger Γ", "Finger L", "Finger Mirrored L",
                            "Finger S", "Press and rotate finger"}*/,
                    colorBand(17)
            )
    };
}


