package uni.vis.janle.knuckleinput;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.hcilab.libftsp.LocalDeviceHandler;
import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;
import org.hcilab.libftsp.listeners.LocalCapImgListener;

public class TaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        LocalDeviceHandler localDeviceHandler = new LocalDeviceHandler();
        localDeviceHandler.setLocalCapImgListener(new LocalCapImgListener() {
            @Override
            public void onLocalCapImg(CapacitiveImageTS capImg) { // called approximately every 50ms
                int[][] matrix = capImg.getMatrix(); // get the 27x15 capacitive image
                int[] flattenedMatrix = capImg.getFlattenedMatrix(); // get a flattened 27x15 capacitive image
                long imgTimestamp = capImg.getTimestamp(); // get timestamp of this image
            }
        });
        localDeviceHandler.startHandler();
    }
}
