package uni.vis.janle.knuckleinput;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.hcilab.libftsp.LocalDeviceHandler;
import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;
import org.hcilab.libftsp.listeners.LocalCapImgListener;

public class TaskActivity extends AppCompatActivity {
    private String TAG = "TaskActivity";

    public int versionID;
    public int taskID;
    public TaskContentDescription [] taskContentDescriptions = new TaskContentDescription[20];

    DrawView drawView;

    private void setupTask() {

        final String [] topImages = new String[20];
        final String [] botImages = new String[20];
        final String [] gestureTexts = new String[20];
        final String [] inputMethodTexts = new String[20];

        for (int i = 0; i<20; i++) {
            //taskContentDescriptions[i] = new TaskContentDescription(topImages[i], botImages[i], gestureTexts[i], inputMethodTexts[i]);
            this.taskContentDescriptions[i] = new TaskContentDescription("@drawable/browser","@drawable/browser_menu" , "Swipe left", "Finger");
        }
        // TODO: Create list with every task from all iterations (400*2)
        // TODO: Shuffle
    }
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

        setupTask();

        ImageButton button_next = findViewById(R.id.button_next);
        ImageButton button_revert = findViewById(R.id.button_revert);

        final TextView text_inputMethod = findViewById(R.id.text_inputMethod);

        final TextView text_gesture = findViewById(R.id.text_gesture);


        taskID = 0;

        TaskContentDescription taskDescription = taskContentDescriptions[0];
        text_inputMethod.setText(taskDescription.getInputMethodText());
        text_gesture.setText(taskDescription.getGestureText());

        versionID = 0;

        button_next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                versionID = 0;
                taskID = (taskID + 1) % 20;

                TaskContentDescription taskDescription = taskContentDescriptions[taskID];
                text_inputMethod.setText(taskDescription.getInputMethodText());
                text_gesture.setText(taskDescription.getGestureText());
            }
        });
        button_revert.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                versionID++;
            }
        });


    }
}
