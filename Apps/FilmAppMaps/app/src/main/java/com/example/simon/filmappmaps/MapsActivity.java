package com.example.simon.filmappmaps;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.hcilab.libftsp.LocalDeviceHandler;
import org.hcilab.libftsp.capacitivematrix.blobdetection.BlobBoundingBox;
import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;
import org.hcilab.libftsp.listeners.LocalCapImgListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UiSettings uiSettings;
    public EditText editText;

    private BlobClassifier blobClassifier;
    // Maximum amount of missing blobs before gesture is recognized as finished
    int blobGap = 0;
    private final static int MAX_BLOB_GAP = 2;
    private final static int WINDOW_SIZE = 50;

    private List<Integer> cnnResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this ,R.color.statusbarColor));

        blobClassifier = new BlobClassifier(this);

        editText = (EditText) findViewById(R.id.search);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ImageButton person = (ImageButton) findViewById(R.id.person);
                if (editText.getText().length()>0) {
                    person.setImageResource(R.drawable.baseline_close_black_24);
                } else {
                    person.setImageResource(R.drawable.baseline_mic_black_24);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText, 0);
                    findViewById(R.id.satellite).setVisibility(View.INVISIBLE);
                    findViewById(R.id.search_overlay).setVisibility(View.VISIBLE);
                    mMap.getUiSettings().setScrollGesturesEnabled(false);
                    if (editText.getText().toString().equals("Search here")) {
                        editText.setText("");
                    }
                    ImageButton mic = (ImageButton) findViewById(R.id.mic);
                    ImageButton person = (ImageButton) findViewById(R.id.person);
                    ImageButton folder = (ImageButton) findViewById(R.id.folder);
                    folder.setImageResource(R.drawable.baseline_arrow_back_black_24);
                    mic.setVisibility(View.INVISIBLE);
                    if (editText.getText().length()>0) {
                        person.setImageResource(R.drawable.baseline_close_black_24);
                    } else {
                        person.setImageResource(R.drawable.baseline_mic_black_24);
                    }
                } else {
                    findViewById(R.id.satellite).setVisibility(View.VISIBLE);
                    findViewById(R.id.search_overlay).setVisibility(View.INVISIBLE);
                    mMap.getUiSettings().setScrollGesturesEnabled(true);
                    ImageButton mic = (ImageButton) findViewById(R.id.mic);
                    ImageButton person = (ImageButton) findViewById(R.id.person);
                    mic.setVisibility(View.VISIBLE);
                    person.setImageResource(R.drawable.baseline_person_black_24);
                }
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search();
                    return true;
                }
                return false;
            }
        });

        final List<int[][]> images = new ArrayList<>();

        LocalDeviceHandler localDeviceHandler = new LocalDeviceHandler();
        localDeviceHandler.setLocalCapImgListener(new LocalCapImgListener() {
            @Override
            public void onLocalCapImg(final CapacitiveImageTS capImg) { // called approximately every 50ms

                if (!editText.hasFocus()) {
                    return;
                }

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


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
        if ((cnnIndex == 0 && index == 8)||(1==1)) {     // Knuckle Circle
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    editText.setText("Universität Hamburg Edmund-Siemers-Allee 1 20146 Hamburg");
                    search();
                }
            });
        }
    }

    public void search() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        editText.clearFocus();
        //Universität Hamburg Edmund-Siemers-Allee 1 20146 Hamburg
        LatLng muc = new LatLng(53.563028, 9.988391);
        CameraUpdate muc_update = CameraUpdateFactory.newLatLngZoom(muc, 17f);
        mMap.addMarker(new MarkerOptions().position(muc).title("MUC"));
        mMap.animateCamera(muc_update);
    }
}
