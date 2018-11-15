package uni.vis.janle.knuckleinput;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.hcilab.libftsp.LocalDeviceHandler;
import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;
import org.hcilab.libftsp.listeners.LocalCapImgListener;

public class TaskActivity extends AppCompatActivity {
    private String TAG = "TaskActivity";
    DrawView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        drawView = new DrawView(this);
        setContentView(drawView);
        drawView.setBackgroundColor(Color.WHITE);

        LocalDeviceHandler localDeviceHandler = new LocalDeviceHandler();
        localDeviceHandler.setLocalCapImgListener(new LocalCapImgListener() {
            @Override
            public void onLocalCapImg(final CapacitiveImageTS capImg) { // called approximately every 50ms
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawView.setImage(capImg);
                    }
                });
            }
        });
        localDeviceHandler.startHandler();
    }
}
