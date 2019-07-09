package io.interactionlab.capimgdemo.demo;

/**
 * Created by Huy on 28/06/2018.
 */

public class DemoSettings {
    public static ModelDescription[] models = new ModelDescription[]{
            new ModelDescription(
                    "CNN",
                    "file:///android_asset/CNN.pb",
                    "conv2d_1_input", //"conv2d_1_input"
                    "output_node0", //"output_node0"
                    new long[]{1, 27, 15, 1},
                    new String[]{"Knuckle", "Finger"}
            ),
            new ModelDescription(
                    "LSTM",
                    "file:///android_asset/LSTM.pb",
                    "time_distributed_10_input",
                    "output_node0", //"output_node0"
                    new long[]{50, 27, 15, 1},
                    new String[]{"Tap", "Two Tap", "Swipe left",
                            "Swipe right", "Swipe up", "Swipe down",
                            "Swipe up with two", "Swipe down with two", "Circle",
                            "Arrowhead left", "Arrowhead right", "Checkmark",
                            "Γ", "L", "Mirrored L", "S",
                            "Rotate"}
            )
    };
}


