package io.interactionlab.capimgdemo.demo;

import android.graphics.Color;

/**
 * Created by Huy on 28/06/2018.
 */

public class DemoSettings {
    public static ModelDescription[] models = new ModelDescription[]{
            new ModelDescription(
                    "KnuckleFinger",
                    "file:///android_asset/knucklebad.pb",
                    "conv2d_1_input",
                    "output_node0",
                    new long[]{1, 27, 15, 1},
                    new String[]{"Knuckle", "Finger"},
                    new int[]{Color.GREEN, Color.YELLOW})
//            new ModelDescription(
//                    "Left vs. Right Thumb",
//                    "file:///android_asset/leftVsRightThumb.pb",
//                    "conv2d_1_input",
//                    "output_node0",
//                    new long[]{1, 27, 15, 1},
//                    new String[]{"Left Thumb", "Right Thumb"},
//                    new int[]{Color.YELLOW, Color.GREEN})

    };
};


