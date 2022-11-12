package simu;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Season extends JPanel {
    private int index = 0;
    private List<Track> tracks = new ArrayList<>();
    private List<Standings> standings = new ArrayList<>();

    public void addTrack(Track track) {
        tracks.add(track);
    }

    public Track nextTrack() {
        if (index >= tracks.size()) {
            return null;
        }
        return tracks.get(index++);
    }

    public int getTrackCount() {
        return tracks.size();
    }

    public void save(Standings standings) {
        this.standings.add(standings);
    }

}
