package uni.vis.janle.knuckleinput;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        final EditText userID = findViewById(R.id.id_input);
        final EditText userAge = findViewById(R.id.age_input);
        final RadioButton femaleInput = findViewById(R.id.female_input);
        final RadioButton maleInput = findViewById(R.id.male_input);
        final Button startButton = findViewById(R.id.startButton);

        final File storeDirectory = new File(getApplicationContext().getFilesDir().getAbsolutePath());
        System.out.println("directory: " + storeDirectory.getAbsolutePath());
        int currentId = 0;
        for (int i = 1; i < 99999; i++) {
            if(!new File(storeDirectory, Integer.toString(i) + "_userData.csv").exists()) {
                currentId = i;
                userID.setText(Integer.toString(i));
                break;
            }
        }

        startButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(userID.getText())) {
                    Toast.makeText(getApplicationContext(), "gj!", Toast.LENGTH_SHORT).show();
                    UserData.USERID = Integer.valueOf(userID.getText().toString());
                    String userGender = femaleInput.isChecked() ? "female" : "male";
                    String userData = userID.getText() + ";" + userAge.getText() + ";" + userGender;
                    FileOutputStream outputStream;
                    try {
                        outputStream = openFileOutput(userID.getText() + "_userData.csv", Context.MODE_APPEND);
                        outputStream.write(userData.getBytes());
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    startTask(v);
                }
            }
        });
    }

    public void startTask(View view) {
        Intent intent = new Intent(this, TaskActivity.class);
        startActivity(intent);
    }
}
