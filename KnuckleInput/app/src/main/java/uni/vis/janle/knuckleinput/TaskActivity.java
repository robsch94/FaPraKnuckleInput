package uni.vis.janle.knuckleinput;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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

    public int[] versionIDs = new int[34];  // distuingishes the version (how often the current task appeared yet), index is taskID
    public int repititionID;  // revertButton
    public List<TaskContentDescription> taskContDescs = new ArrayList<>();
    public int taskContDescsSize;
    public boolean actualData;
    public int userID;
    public int taskID;        // 0-16 finger tasks, 18-33 knuckel tasks
    public int curTask;
    // output stream for capacitive matrix
    private FileOutputStream matrixOutputStream;
    private DatagramSocket udp_sock;
    private boolean isPause = false;
    private boolean isTutorial = false;
    private boolean isEnd = false;

    // GUI
    TextView text_inputMethod = null;
    TextView text_gesture = null;
    TextView text_before = null;
    TextView text_after = null;
    ImageView image_usecase = null;
    ProgressBar progressBar = null;
    TextView text_progressBar = null;


    private static final int UDP_SERVER_PORT = 1234;
    private static final int MAX_UDP_DATAGRAM_LEN = 1500;
    private String receivedUDP;


    private void setupTutorial() {
        // Initialise taskID, get userID from MainActivity
        actualData = false;
        //taskID = 0;
        isTutorial = true;

        // setup TaskContentDescriptions
        List<TaskContentDescription> tasks = new ArrayList<>();
        tasks.addAll(Constants.getFingerTasks());
        tasks.addAll(Constants.getKnuckleTasks());
        taskContDescsSize = tasks.size();
        this.taskContDescs = tasks;
        Log.i("taskContDescs", String.valueOf(this.taskContDescs.size()));
        progressBar.setProgress(0);
        progressBar.setMax(taskContDescsSize);

        // Get first TaskContenDescription
        pauseScreen("");
    }


    private void setupTasks() {
        //actualData = true;
        //taskID = 0;
        isTutorial = false;

        List<TaskContentDescription> knuckleTasks = new ArrayList<>();
        List<TaskContentDescription> fingerTasks = new ArrayList<>();
        for (int version = 1; version <= 20; version++) {
            knuckleTasks.addAll(Constants.getKnuckleTasks());
            fingerTasks.addAll(Constants.getFingerTasks());
        }
        taskContDescsSize = knuckleTasks.size() + fingerTasks.size();

        Collections.shuffle(fingerTasks);
        Collections.shuffle(knuckleTasks);

        if (this.userID % 2 == 0) {
            this.taskContDescs.addAll(fingerTasks);
            this.taskContDescs.addAll(knuckleTasks);
        } else {
            this.taskContDescs.addAll(knuckleTasks);
            this.taskContDescs.addAll(fingerTasks);
        }

        progressBar.setProgress(0);
        progressBar.setMax(taskContDescsSize);

        //nextTask();
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
        runUdpServer();


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

        // Init GUI
        text_inputMethod = (TextView) findViewById(R.id.text_inputMethod);
        text_gesture = findViewById(R.id.text_gesture);
        image_usecase = findViewById(R.id.image_usecase);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        text_progressBar = findViewById(R.id.text_progressBar);
        text_before = findViewById(R.id.text_before);
        text_after = findViewById(R.id.text_after);

        Bundle b = getIntent().getExtras();
        String id_string = b.getString("userID");
        this.userID = Integer.valueOf(id_string);

        //create file output for capacitive matrix
        try {
            matrixOutputStream = openFileOutput(String.valueOf(UserData.USERID) + "_studyData.csv", Context.MODE_APPEND);
            matrixOutputStream.write("userID;Timestamp;Current_Task;Task_amount;TaskID;VersionID;RepetitionID;Actual_Data;Is_Pause;Image\n".getBytes());
            Log.i("fileOutput", this.getFilesDir().getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setupTutorial();
    }

    private void nextTask() {
        String last_inputMethod = text_inputMethod.getText().toString();
        Log.i("last_inputMethod", last_inputMethod);
        if (!isEnd) {
            if (this.taskContDescs.isEmpty() && !isPause) {
                // Pause screen between tutorial and study OR All tasks done
                isPause = true;
                if (isTutorial) {
                    pauseScreen((userID % 2 == 0) ? "Start study with finger next" : "Start study wth knuckle next");
                    setupTasks();
                } else {
                    pauseScreen("End of study");
                    Toast.makeText(getApplicationContext(), "All tasks done!", Toast.LENGTH_SHORT).show();
                    try {
                        matrixOutputStream.close();
                        matrixOutputStream = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isEnd = true;
                }
            } else if (this.taskContDescs.isEmpty() && isPause) {
                //End Pause after pause between tutorial and study or all tasks done
                endPauseScreen();
                if (isTutorial) {
                    createNextTask();
                }
            } else if (!last_inputMethod.equals(this.taskContDescs.get(0).getInputMethodText()) && !isPause && !last_inputMethod.equals("TextView")) {
                // Pause screen between finger and knuckle tasks
                isPause = true;
                if (!isTutorial) {
                    Log.i("userid", String.valueOf(userID));
                    pauseScreen((userID % 2 == 1) ? "Finger next" : "Knuckle next");
                } else if (isTutorial) {
                    if (last_inputMethod == "") {
                        pauseScreen("Start tutorial with finger");
                    } else {
                        pauseScreen("Knuckle next");
                    }
                }
            } else if (!last_inputMethod.equals(this.taskContDescs.get(0).getInputMethodText()) && isPause && !last_inputMethod.equals("TextView")) {
                // Goto next task from pause screen
                endPauseScreen();
                createNextTask();
            } else {
                // Next task
                createNextTask();
            }
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
            //TODO flush at the end (not sure, if a problem)
            if (matrixOutputStream != null) {
                String result = "";
                result += String.valueOf(userID);
                result += ";" + String.valueOf(System.currentTimeMillis());
                result += ";" + String.valueOf(curTask);
                result += ";" + String.valueOf(taskContDescsSize);
                result += ";" + String.valueOf(taskID);
                result += ";" + String.valueOf(versionIDs[taskID]);
                result += ";" + String.valueOf(repititionID);
                result += ";" + String.valueOf(actualData);
                result += ";" + String.valueOf(isPause);
                result += ";" + capImg.toString();
                matrixOutputStream.write((result + "\n").getBytes());
                matrixOutputStream.flush();
                Log.i("fileOutput", result);

                // send via udp
                DatagramPacket packet = new DatagramPacket(result.getBytes(), result.getBytes().length, InetAddress.getByName("192.168.1.104"), UDP_SERVER_PORT);
                if (udp_sock != null) {
                    udp_sock.send(packet);
                    //System.out.println("sent data to pc");
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

    private void runUdpServer() {
        new Thread(new Runnable() {
            public void run() {
                String lText;
                byte[] lMsg = new byte[MAX_UDP_DATAGRAM_LEN];
                try {

                    byte[] buffer = new byte[2048];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    DatagramSocket dsocket = new DatagramSocket(UDP_SERVER_PORT);
                    while (true) {

                        dsocket.receive(packet);
                        lText = new String(buffer, 0, packet.getLength());
                        Log.i("UDP packet received", lText);
                        if (lText.equals("next")) {
                            Handler mainHandler = new Handler(Looper.getMainLooper());

                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {nextTask();} // This is your code
                            };
                            mainHandler.post(myRunnable);
                        } else if (lText.equals("revert")) {
                            revertTask();
                        }
                        //Thread.sleep(200);


                        packet.setLength(buffer.length);
                    }
                } catch (Exception e) {
                    System.err.println(e);
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void pauseScreen(String input) {
        image_usecase.setAlpha(0.0f);
        text_inputMethod.setText(input);
        text_gesture.setText("");
        progressBar.setAlpha(0.0f);
        text_progressBar.setAlpha(0.0f);
        text_before.setAlpha(0.0f);
        text_after.setAlpha(0.0f);
        actualData = false;
    }

    private void endPauseScreen() {
        image_usecase.setAlpha(1.0f);
        text_inputMethod.setText("");
        text_gesture.setText("");
        progressBar.setAlpha(1.0f);
        text_progressBar.setAlpha(1.0f);
        text_before.setAlpha(1.0f);
        text_after.setAlpha(1.0f);
    }

    private void createNextTask() {
        TaskContentDescription taskDescription = this.taskContDescs.remove(0);
        // TODO: Maybe put this in a method
        text_inputMethod.setText(taskDescription.getInputMethodText());
        text_gesture.setText(taskDescription.getGestureText());
        image_usecase.setImageResource(taskDescription.getImage());

        if (progressBar.getProgress() == taskContDescsSize) {
            progressBar.setProgress(0);
        } else {
            progressBar.setProgress(progressBar.getProgress() + 1);
        }
        text_progressBar.setText(String.valueOf(progressBar.getProgress()) + "/"+String.valueOf(taskContDescsSize));

        taskID = taskDescription.getID();
        curTask = taskContDescsSize - this.taskContDescs.size();
        versionIDs[taskID]++;
        repititionID = 0;
        actualData = !isTutorial;
        isPause = false;
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
