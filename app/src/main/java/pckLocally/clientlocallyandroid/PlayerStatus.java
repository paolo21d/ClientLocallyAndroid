package pckLocally.clientlocallyandroid;

import java.time.Duration;

public class PlayerStatus {
    public boolean played = false;
    public boolean paused = false;
    public Playlist currentPlaylist;
    public Duration allDuration;
    public Duration currentDuration;
    public double volumeValue = 1;
    public int currentlyPlayedSongIndex = 0;
    public String path;
    public String title;
    public LoopType loopType = LoopType.RepeatAll;

    public enum LoopType {
        RepeatAll, RepeatOne, Random;
    }
}
