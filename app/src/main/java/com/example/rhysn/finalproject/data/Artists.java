package com.example.rhysn.finalproject.data;

/**
 * Created by Rhys on 19/01/2017.
 */

public class Artists {

    private long artistID;
    private String artistName;
    private final int albumCount;
    private final int songCount;
    private String artUrl;
    private String bio;

    public int getAlbumCount() {
        return albumCount;
    }

    public int getSongCount() {
        return songCount;
    }

    public Artists(long artistID, String atristName, int albumCount, int songCount, String artUrl, String bio) {
        this.artistID = artistID;
        this.artistName = atristName;
        this.albumCount = albumCount;
        this.songCount = songCount;
        this.artUrl = artUrl;
        this.bio = bio;
    }

    public long getArtistID() {
        return artistID;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getArtUrl() {
        return artUrl;
    }

    public String getBio() {
        return bio;
    }
}
