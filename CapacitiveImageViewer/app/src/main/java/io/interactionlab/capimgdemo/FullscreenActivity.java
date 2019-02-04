package io.interactionlab.capimgdemo;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.hcilab.libftsp.LocalDeviceHandler;
import org.hcilab.libftsp.capacitivematrix.MatrixUtils;
import org.hcilab.libftsp.capacitivematrix.blobdetection.BlobBoundingBox;
import org.hcilab.libftsp.capacitivematrix.blobdetection.BlobDetector;
import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;
import org.hcilab.libftsp.listeners.LocalCapImgListener;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.interactionlab.capimgdemo.demo.BlobDetectionTest;
import io.interactionlab.capimgdemo.demo.DemoSettings;
import io.interactionlab.capimgdemo.demo.ModelDescription;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private final Handler mHideHandler = new Handler();

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            movableWindow.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private static final String TAG = FullscreenActivity.class.getSimpleName();
    private RelativeLayout movableWindow;
    private DrawView drawView;
    private TextView textViewMode;

    private BlobClassifier blobClassifier;
    private ModelDescription currentModel;

    private boolean lstm = true;
    private final static int WINDOW_SIZE = 50;
    private int classification_display_length = 0;

    private void setModel(ModelDescription modelDescription) {
        currentModel = modelDescription;
        blobClassifier.setModel(currentModel);
        textViewMode.setText(Html.fromHtml("<html>Model: <b>" + modelDescription.modelName + "</b></html>"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        movableWindow = (RelativeLayout) findViewById(R.id.movableScreen);
        blobClassifier = new BlobClassifier(this);

        final List<int[][]> images = new ArrayList<>();

        LocalDeviceHandler localDeviceHandler = new LocalDeviceHandler();
        localDeviceHandler.setLocalCapImgListener(new LocalCapImgListener() {
            @Override
            public void onLocalCapImg(final CapacitiveImageTS capImg) { // called approximately every 50ms

                if (classification_display_length == 0) {
                    int[][] large = blobClassifier.preprocess(capImg);
                    //Log.i("Large", "Large right here?? \n" + Arrays.deepToString(large));
                    final List<BlobBoundingBox> blobBoundingBoxes = blobClassifier.getBlobBoundaries(large);
                    final List<String> labelNames = new ArrayList<>();
                    final List<Integer> colors = new ArrayList<>();
                    List<float[]> flattenedBlobs = new ArrayList<>();

                    //TODO: following line just for testing
                    //matrix = BlobDetectionTest.t1_pre;
                    //Log.i("Large", "Large still right?? \n" + Arrays.deepToString(large));

                    // if first blob already detected, add each image up to 30
                    if (lstm && !images.isEmpty()) {
                        images.add(capImg.getMatrix());
                    }

                    for (BlobBoundingBox bbb : blobBoundingBoxes) {
                        if (images.isEmpty()) {
                            images.add(capImg.getMatrix());
                        }         // first detected blob
                        flattenedBlobs.add(blobClassifier.getBlobContentIn27x15(large, bbb));
                    }

                    if (lstm) {
                        if (images.size() == WINDOW_SIZE) {                   // classify after 30 images
                            Log.i("Test", "Classifying LSTM+CNN");
                            ClassificationResult cr = blobClassifier.classify(blobClassifier.imagesToPixels(images), lstm);
                            labelNames.add(cr.label + " (" + ((int) Math.round(cr.confidence * 100)) + "%)");
                            colors.add(cr.color);
                            images.clear();
                        }
                    } else {
                        for (int i = 0; i < flattenedBlobs.size(); i++) {      // always classify
                            //Log.i("Test", "flattenedBlobs.get(0): "+ Arrays.toString(flattenedBlobs.get(i)));
                            ClassificationResult cr = blobClassifier.classify(flattenedBlobs.get(i), lstm);
                            labelNames.add(cr.label + " (" + ((int) Math.round(cr.confidence * 100)) + "%)");
                            colors.add(cr.color);
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            drawView.updateView(capImg, blobBoundingBoxes, labelNames, colors, lstm);
                        }
                    });

                    if (!labelNames.isEmpty()) {
                        classification_display_length = 20;
                    }
                } else {classification_display_length--;}
            }
        });
        localDeviceHandler.startHandler();

        // fill the whole screen.
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

        drawView = new DrawView(this);
        movableWindow.removeAllViews();
        movableWindow.addView(drawView, params);

        textViewMode = new TextView(this);
        textViewMode.setText(Html.fromHtml("x"));
        textViewMode.setTextColor(Color.WHITE);
        textViewMode.setTextSize(25);
        textViewMode.setBackgroundColor(Color.TRANSPARENT);
        RelativeLayout.LayoutParams tvmLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tvmLayout.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        movableWindow.addView(textViewMode, tvmLayout);

        textViewMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] modelNames = new String[DemoSettings.models.length];
                for (int i = 0; i < DemoSettings.models.length; i++) {
                    modelNames[i] = DemoSettings.models[i].modelName;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
                builder.setTitle("Select a Model");
                builder.setItems(modelNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setModel(DemoSettings.models[which]);
                        delayedHide(100);
                    }
                });
                builder.show();
            }
        });

        setModel(DemoSettings.models[0]);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
//        // Show the system bar
        movableWindow.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}