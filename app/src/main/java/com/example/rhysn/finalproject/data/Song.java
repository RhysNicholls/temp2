package com.example.rhysn.finalproject.data;

/**
 * Created by rhysn on 10/03/2017.
 */

public class Song {
    public final long albumId;
    public final String albumName;
    public final long artistId;
    public final String artistName;
    public final int duration;
    public final long id;
    public final String title;
    public final int trackNumber;
    public final String data;

    public String getArtistName() {
        return artistName;
    }

    public int getDuration() {
        return duration;
    }

    public String getTitle() {
        return title;
    }

    public String getData() {
        return data;
    }

    public long getId() {
        return id;
    }

    public long getAlbumId() {

        return albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public Song(String data, long id, long albumId, long artistId, String title, String artistName, String  albumName, int duration, int trackNumber) {
        this.data = data;
        this.id = id;
        this.albumId = albumId;
        this.artistId = artistId;
        this.title =  title;

        this.artistName =  artistName;
        this.albumName =  albumName;
        this.duration =  duration;

        this.trackNumber =  trackNumber;
    }

}
