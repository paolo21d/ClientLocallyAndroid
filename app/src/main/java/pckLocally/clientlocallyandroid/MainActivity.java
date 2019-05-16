package pckLocally.clientlocallyandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    Communication communication;
    TextView infoLabel;
    boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoLabel = findViewById(R.id.infoLabel);
        communication = new Communication();
    }

    public void playPauseClicked(View view) {
        infoLabel.setText("PLAY");
    }

    public void nextClicked(View view) {
    }

    public void prevClicked(View view) {
    }

    public void loopClicked(View view) {
    }

    public void replayClicked(View view) {
    }

    public void connectClicked(View view) {
//        try {
//            connected = communication.initConnection();
//            if (connected) {
//                infoLabel.setText("Connected");
//                communication.TCPConnection();
//            } else {
//                infoLabel.setText("NOT Connected");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
