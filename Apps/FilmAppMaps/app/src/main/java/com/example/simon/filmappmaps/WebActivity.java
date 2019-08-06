package com.example.simon.filmappmaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.hcilab.libftsp.LocalDeviceHandler;
import org.hcilab.libftsp.capacitivematrix.blobdetection.BlobBoundingBox;
import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;
import org.hcilab.libftsp.listeners.LocalCapImgListener;
import org.opencv.android.OpenCVLoader;

import static android.provider.AlarmClock.EXTRA_MESSAGE;


public class WebActivity extends AppCompatActivity {
    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }

    private WebView webview;

    private BlobClassifier blobClassifier;
    // Maximum amount of missing blobs before gesture is recognized as finished
    int blobGap = 0;
    private final static int MAX_BLOB_GAP = 2;
    private final static int WINDOW_SIZE = 50;

    private List<Integer> cnnResults = new ArrayList<>();

    private LocalDeviceHandler localDeviceHandler;

    private int gestureIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        setTitle("Browser");

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this ,R.color.statusbarColor));

        webview =(WebView)findViewById(R.id.webView);

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                webview.findAllAsync("Finkenau 35, 22081 Hamburg");//, Edmund-Siemers-Allee 1, 20146 Hamburg");
                Method m = null;
                try {
                    m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                    m.invoke(webview, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                webview.scrollTo(0,-500);
            }
        });
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webview.loadUrl("http://129.69.180.77/muc.html");
        //webview.scrollTo(0,1500);
        webview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
        Canvas c = new Canvas();
        Paint myPaint = new Paint();
        myPaint.setColor(Color.rgb(0, 0, 0));
        myPaint.setStrokeWidth(10);
        c.drawRect(100, 100, 200, 200, myPaint);
        //webview.loadUrl("javascript:window.document.getElementById('pgc-521-5-0').style.display='none';");

        blobClassifier = new BlobClassifier(this);

        final List<int[][]> images = new ArrayList<>();

        localDeviceHandler = new LocalDeviceHandler();
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
    }

    protected float[] normalize(float[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i] / 255.0f;
        }
        return array;
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


        if (gestureIndex==0) {
            copy();
            gestureIndex = 1;
        } else if (gestureIndex==1) {
            startMapActivity(new View(this));
        }
        /*
        if (cnnIndex==0) {
             if (index == 1) {   // Knuckle two tap
                copy();
            } else if (index == 9) {    // Knuckle Arrowhead left
                startMapActivity(new View(this));
            }
        }
        */
    }

    protected void copy() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Copied", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void startMapActivity(View view) {
        localDeviceHandler.stopHandler();
        String copiedWord = "Universität Hamburg Edmund-Siemers-Allee 1 20146 Hamburg";
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(EXTRA_MESSAGE, "Starting Maps activity");
        startActivity(intent);
    }
}