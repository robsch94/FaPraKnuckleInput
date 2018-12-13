package uni.vis.janle.knuckleinput;

import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import org.hcilab.libftsp.LocalDeviceHandler;
import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;
import org.hcilab.libftsp.listeners.LocalCapImgListener;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class TaskActivity extends AppCompatActivity {
    private String TAG = "TaskActivity";

    public List<TaskContentDescription> taskContDescs;
    public boolean actualData;
    public int[] versionIDs = new int[34];  // distuingishes the version (how often the current task appeared yet)
    public int repititionID;  // distuingishes the repititions (how often a task is repeated)
    public int userID;
    public int taskID;        // distuingishes the task (e.g. tap with finger))
    // output stream for capacitive matrix
    private FileOutputStream matrixOutputStream;
    private DatagramSocket udp_sock;

    // GUI
    final TextView text_inputMethod = (TextView) findViewById(R.id.text_inputMethod);
    final TextView text_gesture = findViewById(R.id.text_gesture);
    final ImageView image_usecase = findViewById(R.id.image_usecase);
    final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
    final TextView text_progressBar = findViewById(R.id.text_progressBar);

    private List<TaskContentDescription> setupTasks() {
        List<TaskContentDescription> taskContDescs = new ArrayList<>();
        List<TaskContentDescription> knuckleTasks = new ArrayList<>();
        List<TaskContentDescription> fingerTasks = new ArrayList<>();
        for (int version = 1; version<=25; version++) {
            knuckleTasks.add(new TaskContentDescription(0, R.drawable.tap, "Tap", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(1, R.drawable.twotap, "Two knuckle tap", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(2, R.drawable.swipeleft, "Swipe left", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(3, R.drawable.swiperight, "Swipe right", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(4, R.drawable.swipeup, "Swipe up", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(5, R.drawable.swipedown, "Swipe down", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(6, R.drawable.twoswipeup, "Swipe up with two knuckles", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(7, R.drawable.twoswipedown, "Swipe down with two knuckles", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(8, R.drawable.circle, "Circle", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(9, R.drawable.arrowheadleft, "Arrowhead left", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(10, R.drawable.arrowheadright, "Arrowhead right", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(11, R.drawable.checkmark, "Checkmark", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(12, R.drawable.flashlight, "Γ", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(13, R.drawable.l, "L", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(14, R.drawable.lmirrored, "Mirrored L", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(15, R.drawable.screenshot, "S", "Knuckle"));
            knuckleTasks.add(new TaskContentDescription(16, R.drawable.rotate, "Press and rotate knuckle", "Knuckle"));

            fingerTasks.add(new TaskContentDescription(17, R.drawable.tap, "Tap", "Finger"));
            fingerTasks.add(new TaskContentDescription(18, R.drawable.twotap, "Two finger tap", "Finger"));
            fingerTasks.add(new TaskContentDescription(19, R.drawable.swipeleft, "Swipe left", "Finger"));
            fingerTasks.add(new TaskContentDescription(20, R.drawable.swiperight, "Swipe right", "Finger"));
            fingerTasks.add(new TaskContentDescription(21, R.drawable.swipeup, "Swipe up", "Finger"));
            fingerTasks.add(new TaskContentDescription(22, R.drawable.swipedown, "Swipe down", "Finger"));
            fingerTasks.add(new TaskContentDescription(23, R.drawable.twoswipeup, "Swipe up with two fingers", "Finger"));
            fingerTasks.add(new TaskContentDescription(24, R.drawable.twoswipedown, "Swipe down with two fingers", "Finger"));
            fingerTasks.add(new TaskContentDescription(25, R.drawable.circle, "Circle", "Finger"));
            fingerTasks.add(new TaskContentDescription(26, R.drawable.arrowheadleft, "Arrowhead left", "Finger"));
            fingerTasks.add(new TaskContentDescription(27, R.drawable.arrowheadright, "Arrowhead right", "Finger"));
            fingerTasks.add(new TaskContentDescription(28, R.drawable.checkmark, "Checkmark", "Finger"));
            fingerTasks.add(new TaskContentDescription(29, R.drawable.flashlight, "Γ", "Finger"));
            fingerTasks.add(new TaskContentDescription(30, R.drawable.l, "L", "Finger"));
            fingerTasks.add(new TaskContentDescription(31, R.drawable.lmirrored, "Mirrored L", "Finger"));
            fingerTasks.add(new TaskContentDescription(32, R.drawable.screenshot, "S", "Finger"));
            fingerTasks.add(new TaskContentDescription(33, R.drawable.rotate, "Press and rotate finger", "Finger"));
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
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // udp socket
        try {
            udp_sock = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            udp_sock = null;
        }


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
        hideSystemUI();

        // Initialise taskID, get userID from MainActivity
        actualData = true;
        taskID = 0;
        Bundle b = getIntent().getExtras();
        String id_string = b.getString("userID");
        this.userID = Integer.valueOf(id_string);
        Log.i(TAG, "userID:"+String.valueOf(userID));

        // setup TaskContentDescriptions
        this.taskContDescs = this.setupTasks();
        TaskContentDescription taskDescription = this.taskContDescs.remove(0);
        text_inputMethod.setText(taskDescription.getInputMethodText());
        text_gesture.setText(taskDescription.getGestureText());
        image_usecase.setImageResource(taskDescription.getImage());
        taskID = taskDescription.getID();
        versionIDs[taskID]++;

        //create file output for capacitive matrix
        try {
            matrixOutputStream = openFileOutput(String.valueOf(UserData.USERID) + "_studyData.csv", Context.MODE_APPEND);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void nextTask() {
        repititionID = 0;
        if (taskContDescs.size() == 425 && image_usecase.getAlpha() == 1.0) {
            actualData = false;
            image_usecase.setAlpha(1.0f);
            text_inputMethod.setText("");
            text_gesture.setText("Pause");
            progressBar.setAlpha(1.0f);
            text_progressBar.setAlpha(1.0f);
            // Pause screen
        } else if (this.taskContDescs.isEmpty()) {
            // All tasks done
            actualData = false;
            Toast.makeText(getApplicationContext(), "All tasks done!", Toast.LENGTH_SHORT).show();
        } else {
            // Next task
            actualData = true;
            TaskContentDescription taskDescription = taskContDescs.remove(0);
            text_inputMethod.setText(taskDescription.getInputMethodText());
            taskID = taskDescription.getID();
            versionIDs[taskID]++;
            text_gesture.setText(taskDescription.getGestureText());
            image_usecase.setAlpha(0.0f);
            image_usecase.setImageResource(taskDescription.getImage());
            progressBar.setAlpha(0.0f);
            text_progressBar.setAlpha(0.0f);

            if (progressBar.getProgress() == 425) {
                progressBar.setProgress(1);

            } else {
                progressBar.setProgress(progressBar.getProgress() + 1);
            }
            text_progressBar.setText(String.valueOf(progressBar.getProgress()) + "/425");
            // Send content
            /*
            Data to send:
                - userID
                - timestamp
                - touch (bool)
                - gestureID
                - versionID
                - inputMethod (finger/knuckle)
                - actualData (bool)
                - capacitiveImage (Matrix)
             */
        }
    }

    private void revertTask() {
        repititionID++;
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    void storeData(CapacitiveImageTS capImg) {
        try {
            //TODO flush at the end (not shure, if a problem)
            if (matrixOutputStream != null){
                String result = "";
                result += String.valueOf(userID);
                result += ";" + String.valueOf(System.currentTimeMillis());
                result += ";" + String.valueOf(taskID);
                result += ";" + String.valueOf(repititionID);
                result += ";" + capImg.toString();
                matrixOutputStream.write((result + "\n").getBytes());
                //matrixOutputStream.flush();
                //System.out.println(System.currentTimeMillis());

                // send via udp
                DatagramPacket packet = new DatagramPacket(result.getBytes(), result.getBytes().length, InetAddress.getByName("192.168.0.101"), 1234);
                if (udp_sock != null){
                    udp_sock.send(packet);
                    System.out.println("sent data to pc");
                } else {
                    System.out.println("udp socket is null");
                }
            } else {
                System.out.println("CapImg stream is null!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
