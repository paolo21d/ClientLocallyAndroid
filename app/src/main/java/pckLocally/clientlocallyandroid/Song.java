package pckLocally.clientlocallyandroid;

public class Song {
    private String songName;
    private String songTime;
    private String songPath;

    public Song(String name, String time){
        songName= name;
        songTime= time;
    }
    public Song(String name, String time, String path){
        songName= name;
        songTime= time;
        songPath= path;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongTime() {
        return songTime;
    }

    public void setSongTime(String songTime) {
        this.songTime = songTime;
    }

    public String getSongPath() {
        return songPath;
    }

    public void setSongPath(String songPath) {
        this.songPath = songPath;
    }
}
