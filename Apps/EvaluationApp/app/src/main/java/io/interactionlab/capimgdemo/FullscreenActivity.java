package io.interactionlab.capimgdemo;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.hcilab.libftsp.LocalDeviceHandler;
import org.hcilab.libftsp.capacitivematrix.blobdetection.BlobBoundingBox;
import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;
import org.hcilab.libftsp.listeners.LocalCapImgListener;
import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.interactionlab.capimgdemo.demo.DemoSettings;

import static java.lang.Math.max;
import static java.lang.Math.min;

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

    private BlobClassifier blobClassifier;

    private EditText textView;
    private Layout textViewLayout;
    private ViewFlipper viewFlipper;
    private List<String> textViewWords;
    String copiedWord;
    private int curWordIndex = 0;

    private int curCharIndex = 0;

    // Maximum amount of missing blobs before gesture is recognized as finished
    int blobGap = 0;
    private final static int MAX_BLOB_GAP = 2;
    private final static int WINDOW_SIZE = 50;

    private List<Integer> cnnResults = new ArrayList<>();

    protected float[] getCoordinatesOfWord(String word) {
        // Find where the WORD is
        int startOffsetOfClickedText = textView.getText().toString().indexOf(word);
        int endOffsetOfClickedText = startOffsetOfClickedText + word.length();
        Log.i("coordinates", String.valueOf(startOffsetOfClickedText)+", "+ String.valueOf(endOffsetOfClickedText));

        double startx = textViewLayout.getPrimaryHorizontal((int)startOffsetOfClickedText);
        double endx = textViewLayout.getPrimaryHorizontal((int)endOffsetOfClickedText);
        return new float[] {(float)startx, (float)endx};
    }

    protected float[] normalize(float[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i] / 255.0f;
        }
        return array;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        movableWindow = (RelativeLayout) findViewById(R.id.movableScreen);
        blobClassifier = new BlobClassifier(this);

        textView = (EditText) findViewById(R.id.tv);
        textView.setEnabled(false);
        textView.setTextColor(Color.BLACK);

        ViewTreeObserver vto = textView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                textViewLayout = textView.getLayout();
            }
        });

        textViewWords = Arrays.asList(textView.getText().toString().split(" "));
        textView.setText(Html.fromHtml(textView.getText().toString().replace("Click", "<font color='red'>Click</font>")));

        viewFlipper = (ViewFlipper) findViewById(R.id.flip);

        final List<int[][]> images = new ArrayList<>();

        LocalDeviceHandler localDeviceHandler = new LocalDeviceHandler();
        localDeviceHandler.setLocalCapImgListener(new LocalCapImgListener() {
            @Override
            public void onLocalCapImg(final CapacitiveImageTS capImg) { // called approximately every 50ms

                int[][] large = blobClassifier.preprocess(capImg);
                final List<BlobBoundingBox> blobBoundingBoxes = blobClassifier.getBlobBoundaries(large);

                // Check for blobs
                if (blobBoundingBoxes.isEmpty()) {
                    if (!images.isEmpty()) {
                        // No blobs but gesture in progress
                        blobGap += 1;
                        images.add(capImg.getMatrix());
                    }
                } else {
                    // Start new gesture or add to existing one
                    blobGap = 0;
                    images.add(capImg.getMatrix());
                    ClassificationResult cr = blobClassifier.classify(blobClassifier.getBlobContentIn27x15(large, blobBoundingBoxes.get(0)), true);
                    cnnResults.add(cr.index);
                }

                // Classifiy after WINDOW_SIZE images, or if there have been no blobs for MAX_BLOB_GAP steps
                if (images.size() == WINDOW_SIZE || blobGap == MAX_BLOB_GAP) {
                    System.out.println("len before: " + images.size());
                    if (images.size() != WINDOW_SIZE) {   // early classification due to blobGap
                        // Add black images
                        int numbersToAdd = WINDOW_SIZE - images.size();
                        for (int i = 0; i < numbersToAdd; i++) {
                            images.add(new int[27][15]);
                        }
                    }
                    blobGap = 0;

                    System.out.println("len after: " + images.size());
                    float[] tempA = blobClassifier.imagesToPixels(images);
                    System.out.println(tempA);

                    ClassificationResult crLstm = blobClassifier.classify(normalize(blobClassifier.imagesToPixels(images)), false);
                    int cnnIndex = getCnnIndexFromClassfications(cnnResults);
                    executeGesture(crLstm.index, cnnIndex);
                    images.clear();
                    cnnResults.clear();
                }
            }
        });
        localDeviceHandler.startHandler();

        // fill the whole screen.
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

        highlight(0);
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

    protected int getCnnIndexFromClassfications(List<Integer> results) {
        float sum = 0.0f;
        for (Integer result : results) {
            sum += (float) result;
        }
        if (sum/results.size()<0.5) {return 0;}
        else {return 1;}
    }

    protected void executeGesture(int index, int cnnIndex) {
        Log.i("GestureIndex", String.valueOf(index));
        Log.i("CnnIndex", String.valueOf(cnnIndex));
        if (cnnIndex==0) {
            if (index == 2) {
                highlight(-1);
            } else if (index == 3) {
                highlight(1);
            } else if (index == 1) {
                copy();
            } else if (index == 8) {
                paste();
            } else if (index == 9) {
                switchView(false);
            } else if (index == 10) {
                switchView(true);
            }
        }
    }

    protected String join(List<String> list, String delim) {
        // Supported just since Java 8
        String str = "";
        for (String s : list) {
            str = str + delim + s;
        }
        return str;
    }

    // Custom method to do a task
    protected void highlight(int dir) {
        List<String> temp = new ArrayList<>(textViewWords);
        //curWordIndex = max(0, min(8, curWordIndex + dir));
        curWordIndex = max(0, min(textViewWords.size(), curWordIndex + dir));
        String textToHighlight = textViewWords.get(curWordIndex);

        if (textViewLayout!=null) {
            float[] coors = getCoordinatesOfWord(textToHighlight);
            ImageView leftMarker = findViewById(R.id.markerLeft);
            ImageView rightMarker = findViewById(R.id.markerRight);
            leftMarker.setX(coors[0]-30);
            rightMarker.setX(coors[1]);
        }

        temp.set(curWordIndex, "<font color='red'>" + textToHighlight + "</font>");
        final String modifiedString = join(temp, " ");
        /*
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(Html.fromHtml(modifiedString));
            }
        });
        */

        String substring;
        final int indexLeft;
        if (dir==1) {
            substring = textView.getText().toString().substring(curCharIndex);
            indexLeft = Math.max(0, curCharIndex + substring.indexOf(textToHighlight));
        } else if (dir==-1) {
            substring = textView.getText().toString().substring(0, curCharIndex);
            indexLeft = Math.max(0, substring.lastIndexOf(textToHighlight));
        } else {
            indexLeft = 0;
        }
        curCharIndex = Math.max(0, indexLeft);
        final int indexRight = indexLeft + textToHighlight.length();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setSelection(indexLeft,indexRight);
            }
        });

    }

    protected void copy() {
        copiedWord = textViewWords.get(curWordIndex);
        System.out.println("copied: " + copiedWord);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Copied text", Toast.LENGTH_LONG).show();
            }
        });
        Log.i("Word", "Copying " + textViewWords.get(curWordIndex));
    }

    protected void paste() {
        int id = viewFlipper.getDisplayedChild();
        final TextView pasteView;
        if (id == 1) {
            pasteView = (TextView) findViewById(R.id.paste1);
        } else if (id == 2) {
            pasteView = (TextView) findViewById(R.id.paste2);
        } else {
            pasteView = null;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (pasteView != null){
                    pasteView.setText(pasteView.getText().toString() + " " + copiedWord);
                }
            }
        });
        Log.i("Word", "Pasting " + copiedWord);
    }

    protected void switchView(boolean right) {
        if (right) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    viewFlipper.showNext();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    viewFlipper.showPrevious();
                }
            });
        }
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