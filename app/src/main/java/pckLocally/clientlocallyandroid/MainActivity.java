package pckLocally.clientlocallyandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    TextView infoLabel;
    TextView volumeLabel;
    ImageButton playPauseButton;
    ImageButton loopButton;
    EditText pinField;
    static boolean connected = false;
    Communication communication;
    double volume = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoLabel = findViewById(R.id.infoLabel);
        volumeLabel = findViewById(R.id.volumeLabel);
        playPauseButton = findViewById(R.id.playPauseButton);
        loopButton = findViewById(R.id.loopButton);
        pinField = findViewById(R.id.pinText);

        communication = new Communication(this);
    }

    public void playPauseClicked(View view) throws Exception {
        infoLabel.setText("PLAY");
        communication.comPlayPause();
    }

    public void nextClicked(View view) {
        communication.comNext();
    }

    public void prevClicked(View view) {
        communication.comPrev();
    }

    public void loopClicked(View view) {
        communication.comLoop();
    }

    public void replayClicked(View view) {
        communication.comReplay();
    }

    public void connectClicked(View view) {
        if (!connected) {
            String pin = pinField.getText().toString();
            if (pin.equals("")) {
                Toast.makeText(getApplicationContext(), "Input PIN to connect!", Toast.LENGTH_LONG).show();
                return;
            }else if(pin.length()!=4){
                Toast.makeText(getApplicationContext(), "PIN is 4 digits!", Toast.LENGTH_SHORT).show();
                return;
            }
            communication.setPin(pin);
            communication.start();
            //communication.initConnection();
            if(connected){
                infoLabel.setText("Connectd");
                //communication.TCPConnection();
            }else{
                infoLabel.setText("NOT Connected");
            }
        }
    }

    public void refreshInfo(PlayerStatus status) {
        System.out.println(status.played + " " + status.paused + " " + status.loopType);
        if (status.played) {
            playPauseButton.setImageResource(R.drawable.ic_pause);
        }
        if (status.paused) {
            playPauseButton.setImageResource(R.drawable.ic_play);
        }

        if (status.loopType == PlayerStatus.LoopType.RepeatAll) {
            loopButton.setImageResource(R.drawable.ic_loop_all);
        } else if (status.loopType == PlayerStatus.LoopType.RepeatOne) {
            loopButton.setImageResource(R.drawable.ic_loop_one);
        } else if (status.loopType == PlayerStatus.LoopType.Random) {
            loopButton.setImageResource(R.drawable.ic_loop_random);
        }

        /*tableView.getItems().clear();
        for (Song s : status.currentPlaylist.getSongs()) {
            tableView.getItems().add(new SongTable(s));
        }*/

        infoLabel.setText(status.title);
        int volume = (int) (status.volumeValue * 100);
        volumeLabel.setText(Integer.toString(volume) + "%");
    }

    public void volumeMuteClicked(View view) {
        communication.comVolMute();
    }

    public void volumeDownClicked(View view) {
        communication.comVolDown();
    }

    public void volumeUpClicked(View view) {
        communication.comVolUp();
    }

    public void closeCommunication() {
        connected = false;
        infoLabel.setText("Disconnected");
        communication.closeCommunication();
        communication = null;
        communication = new Communication(this);
    }
}
