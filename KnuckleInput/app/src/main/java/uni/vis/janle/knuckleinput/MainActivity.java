package uni.vis.janle.knuckleinput;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        final EditText userID = findViewById(R.id.userIDText);
        final Button startButton = findViewById(R.id.startButton);



        startButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(userID.getText())) {
                    Toast.makeText(getApplicationContext(), "gj!", Toast.LENGTH_SHORT).show();
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
