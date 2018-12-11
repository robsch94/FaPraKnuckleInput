package uni.vis.janle.knuckleinput;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.hcilab.libftsp.LocalDeviceHandler;
import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;
import org.hcilab.libftsp.listeners.LocalCapImgListener;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class TaskActivity extends AppCompatActivity {
    private String TAG = "TaskActivity";

    public int versionID;
    public int userID;
    public int taskID;
    // output stream for capacitive matrix
    private FileOutputStream matrixOutputStream;


    private List<TaskContentDescription> setupTask() {
        List<TaskContentDescription> taskContDescs = new ArrayList<>();
        List<TaskContentDescription> knuckleTasks = new ArrayList<>();
        List<TaskContentDescription> fingerTasks = new ArrayList<>();
        for (int repitition = 0; repitition<25; repitition++) {
            knuckleTasks.add(new TaskContentDescription(R.drawable.tap, "Tap", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(R.drawable.twotap, "Two knuckle tap", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(R.drawable.swipeleft, "Swipe left", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(R.drawable.swiperight, "Swipe right", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(R.drawable.swipeup, "Swipe up", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(R.drawable.swipedown, "Swipe down", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(R.drawable.twoswipeup, "Swipe up with two knuckles", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(R.drawable.twoswipedown, "Swipe down with two knuckles", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(R.drawable.circle, "Circle", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(R.drawable.arrowheadleft, "Arrowhead left", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(R.drawable.arrowheadright, "Arrowhead right", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(R.drawable.checkmark, "Checkmark", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(R.drawable.flashlight, "Γ", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(R.drawable.l, "L", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(R.drawable.lmirrored, "Mirrored L", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(R.drawable.screenshot, "S", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(R.drawable.rotate, "Press and rotate knuckle", "Knuckle"));

            fingerTasks.add(new TaskContentDescription(R.drawable.tap, "Tap", "Finger"));
            fingerTasks.add(new TaskContentDescription(R.drawable.twotap, "Two finger tap", "Finger"));
            fingerTasks.add(new TaskContentDescription(R.drawable.swipeleft, "Swipe left", "Finger"));
            fingerTasks.add(new TaskContentDescription(R.drawable.swiperight, "Swipe right", "Finger"));
            fingerTasks.add(new TaskContentDescription(R.drawable.swipeup, "Swipe up", "Finger"));
            fingerTasks.add(new TaskContentDescription(R.drawable.swipedown, "Swipe down", "Finger"));
            fingerTasks.add(new TaskContentDescription(R.drawable.twoswipeup, "Swipe up with two fingers", "Finger"));
            fingerTasks.add(new TaskContentDescription(R.drawable.twoswipedown, "Swipe down with two fingers", "Finger"));
            fingerTasks.add(new TaskContentDescription(R.drawable.circle, "Circle", "Finger"));
            fingerTasks.add(new TaskContentDescription(R.drawable.arrowheadleft, "Arrowhead left", "Finger"));
            fingerTasks.add(new TaskContentDescription(R.drawable.arrowheadright, "Arrowhead right", "Finger"));
            fingerTasks.add(new TaskContentDescription(R.drawable.checkmark, "Checkmark", "Finger"));
            fingerTasks.add(new TaskContentDescription(R.drawable.flashlight, "Γ", "Finger"));
            fingerTasks.add(new TaskContentDescription(R.drawable.l, "L", "Finger"));
            fingerTasks.add(new TaskContentDescription(R.drawable.lmirrored, "Mirrored L", "Finger"));
            fingerTasks.add(new TaskContentDescription(R.drawable.screenshot, "S", "Finger"));
            fingerTasks.add(new TaskContentDescription(R.drawable.rotate, "Press and rotate finger", "Finger"));
        }

        Collections.shuffle(fingerTasks);
        Collections.shuffle(knuckleTasks);

        if (this.userID%2==0) {
            taskContDescs.addAll(fingerTasks);
            taskContDescs.addAll(knuckleTasks);
        } else {
            taskContDescs.addAll(knuckleTasks);
            taskContDescs.addAll(fingerTasks);
        }
        return taskContDescs;
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
                //storeData(capImg);
            }
        });
        localDeviceHandler.startHandler();

        ImageButton button_next = findViewById(R.id.button_next);
        ImageButton button_revert = findViewById(R.id.button_revert);
        final TextView text_inputMethod = (TextView) findViewById(R.id.text_inputMethod);
        final TextView text_gesture = findViewById(R.id.text_gesture);
        final ImageView image_usecase = findViewById(R.id.image_usecase);

        // Initialise taskID, get userID from MainActivity
        taskID = 0;
        Bundle b = getIntent().getExtras();
        /*String id_string = b.getString("userID");
        //TODO no userID here, always null
        try {
            this.userID = Integer.valueOf(id_string);
        }catch (NumberFormatException e) {
            this.userID = 1;
        }*/
        this.userID = 1;

        // setup TaskContentDescriptions
        final List<TaskContentDescription> taskContDescs = this.setupTask();
        TaskContentDescription taskDescription = taskContDescs.remove(0);
        text_inputMethod.setText(taskDescription.getInputMethodText());
        text_gesture.setText(taskDescription.getGestureText());
        image_usecase.setImageResource(taskDescription.getImage());

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

                if (taskContDescs.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "All tasks done!", Toast.LENGTH_SHORT).show();
                } else {
                    TaskContentDescription taskDescription = taskContDescs.remove(0);
                    text_inputMethod.setText(taskDescription.getInputMethodText());
                    text_gesture.setText(taskDescription.getGestureText());
                    image_usecase.setImageResource(taskDescription.getImage());
                }
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
            //TODO too slow (currently only 12 samples per second and not 20)
            //TODO flush at the end (not sure, if a problem)
            matrixOutputStream.write((capImg.toString() + "\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
