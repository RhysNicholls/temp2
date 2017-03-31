package com.example.rhysn.finalproject.data;

/**
 * Created by Rhys on 18/01/2017.
 */

public class Album {

    private long albumId;
    private String albumName;
    private String albumArtist;
    private long artistID;
    private int year;
    private int songCount;
    private String artUri;


    public Album(long albumId, String albumName, String albumArtist, long artistID, int year, String artUri, int songCount) {
        this.albumId = albumId;
        this.albumName = albumName;
        this.albumArtist = albumArtist;
        this.artistID = artistID;
        this.year = year;
        this.artUri = artUri;
        this.songCount = songCount;

    }

    public int getSongCount() {
        return songCount;
    }

    public long getAlbumId() {
        return albumId;
    }

    public int getYear() {
        return year;
    }

    public String getArtUri() {
        return artUri;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }
}
