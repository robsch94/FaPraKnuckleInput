package uni.vis.janle.knuckleinput;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import org.hcilab.libftsp.LocalDeviceHandler;
import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;
import org.hcilab.libftsp.listeners.LocalCapImgListener;

import java.io.FileOutputStream;
import java.io.IOException;

public class TaskActivity extends AppCompatActivity {
    private String TAG = "TaskActivity";

    public int versionID;
    public int taskID;
    public TaskContentDescription [] taskContentDescriptions = new TaskContentDescription[20];
    // output stream for capacitive matrix
    private FileOutputStream matrixOutputStream;


    @Override
    protected void onStop() {
        /*if (matrixOutputStream != null) {
            try {
                matrixOutputStream.flush();
                matrixOutputStream.close();
                matrixOutputStream = null;
                System.out.println("flushed all files");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        super.onStop();
    }

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

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_task);

        LocalDeviceHandler localDeviceHandler = new LocalDeviceHandler();
        localDeviceHandler.setLocalCapImgListener(new LocalCapImgListener() {
            @Override
            public void onLocalCapImg(final CapacitiveImageTS capImg) { // called approximately every 50ms
                storeData(capImg);
            }
        });
        localDeviceHandler.startHandler();

        setupTask();

        ImageButton button_next = findViewById(R.id.button_next);
        ImageButton button_revert = findViewById(R.id.button_revert);

        final TextView text_inputMethod = (TextView) findViewById(R.id.text_inputMethod);

        final TextView text_gesture = findViewById(R.id.text_gesture);


        taskID = 0;

        TaskContentDescription taskDescription = taskContentDescriptions[0];
        //text_inputMethod.setText(taskDescription.getInputMethodText());
        System.out.println(text_inputMethod);
        text_inputMethod.setText("Hello World!");
        text_gesture.setText(taskDescription.getGestureText());

        versionID = 0;

        //create file output for capacitive matrix
        try {
            matrixOutputStream = openFileOutput(String.valueOf(UserData.USERID) + "_studyData.csv", Context.MODE_APPEND);
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    void storeData(CapacitiveImageTS capImg) {
        try {
            //TODO to slow (currently only 12 samples per second and not 20)
            //TODO flush at the end (not shure, if a problem)
            if (matrixOutputStream != null){
                matrixOutputStream.write((String.valueOf(System.currentTimeMillis()) + ";" + capImg.toString() + "\n").getBytes());
                matrixOutputStream.flush();
                //System.out.println(System.currentTimeMillis());
            } else {
                System.out.println("CapImg stream is null!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
