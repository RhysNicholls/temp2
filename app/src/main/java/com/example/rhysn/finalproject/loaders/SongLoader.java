package com.example.rhysn.finalproject.loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.rhysn.finalproject.data.Song;

import java.util.ArrayList;

public class SongLoader {

    public static Song getSong(Cursor c) {


        if (c == null || !c.moveToFirst()) if (c != null) {
            c.close();
        }
        Song song;
        do {
            song = new Song(c.getString(0), c.getLong(1), c.getLong(8), c.getLong(7), c.getString(2), c.getString(3), c.getString(4), c.getInt(5), c.getInt(6));
        } while (c.moveToNext());
        if (c != null) {
            c.close();
        }
        return song;
    }

    public static ArrayList<Song> getAllSongs(Cursor c) {

        ArrayList songList = new ArrayList();

        if (c == null || !c.moveToFirst()) {
            if (c != null) {
                c.close();
            }
            return songList;
        }
        do {
            songList.add(new Song(c.getString(0),c.getLong(1), c.getLong(8), c.getLong(7), c.getString(2), c.getString(3), c.getString(4), c.getInt(5), c.getInt(6)));
        } while (c.moveToNext());
        if (c != null) {
            c.close();
        }
        return songList;
    }

    public static Song getSongById(Context context, long id) {
        return getSong(createSongCursor(context, "_id=" + String.valueOf(id), null));
    }

    public static ArrayList<Song> getSongs(Context context){

        return getAllSongs(createSongCursor(context, null, null));
    }

    public static ArrayList<Song> getSongsByAlbumId(Context context, long albumID){

        return getAllSongs(makeAlbumSongCursor(context, albumID));
    }

    public static ArrayList<Song> getSongsByArtistId(Context context, long artistID){

        return getAllSongs(makeArtistSongCursor(context, artistID));
    }

    private static Cursor createSongCursor(Context context, String selection, String[] parameters){

        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{"_data","_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id"}, selection, parameters, "album_key");
    }

    private static Cursor makeAlbumSongCursor(Context context, long albumID) {
       final ContentResolver contentResolver = context.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String string = "is_music=1 AND title != '' AND album_id=" + albumID;
        Cursor cursor = contentResolver.query(uri, new String[]{"_data","_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id"}, string, null, null);
        return cursor;
    }

    private static Cursor makeArtistSongCursor(Context context, long artistID) {
        final ContentResolver contentResolver = context.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String string = "is_music=1 AND title != '' AND artist_id=" + artistID;
        Cursor cursor = contentResolver.query(uri, new String[]{"_data","_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id"}, string, null, null);
        return cursor;
    }





}
