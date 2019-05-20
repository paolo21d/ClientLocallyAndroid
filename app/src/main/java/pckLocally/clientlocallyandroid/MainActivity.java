package pckLocally.clientlocallyandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    TextView infoLabel;
    TextView volumeLabel;
    ImageButton playPauseButton;
    ImageButton loopButton;
    boolean connected = false;
    Communication communication;
    double volume =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoLabel = findViewById(R.id.infoLabel);
        volumeLabel = findViewById(R.id.volumeLabel);
        playPauseButton = findViewById(R.id.playPauseButton);
        loopButton = findViewById(R.id.loopButton);

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
            communication.start();
            connected = true;
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
        vol
    }

    public void volumeMuteClicked(View view){

    }
    public void volumeDownClicked(View view){

    }
    public void volumeUpClicked(View view){

    }



}
