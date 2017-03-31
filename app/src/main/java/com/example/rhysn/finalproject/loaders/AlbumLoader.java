package com.example.rhysn.finalproject.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Albums;

import com.example.rhysn.finalproject.data.Album;

import java.util.ArrayList;

/**
 * Created by rhysn on 10/03/2017.
 */

public class AlbumLoader {

    public static Album getAlbum(Cursor c){
        
        Album a = null;
        if(c != null && c.moveToFirst()){
          a = new Album(c.getLong(0), c.getString(1), c.getString(2),c.getInt(3), c.getInt(4), c.getString(5), c.getInt(6));
        }if (c!= null) {
            c.close();
        }
        return a;

    }

    public static ArrayList<Album> getAllAlbums(Cursor c) {
        ArrayList albumList = new ArrayList();
        if (c == null || !c.moveToFirst()) {
            if (c != null) {
                c.close();
            }
            return albumList;
        }
        do {
            albumList.add(new Album(c.getLong(0), c.getString(1), c.getString(2),c.getInt(3), c.getInt(4), c.getString(5), c.getInt(6)));
        } while (c.moveToNext());
        if (c != null) {
            c.close();
        }
        return albumList;
    }

    public static ArrayList<Album> getAlbums(Context context){

        return getAllAlbums(createAlbumCursor(context, null, null));
    }

    public static Album getAlbumById(Context context, long id){

        return getAlbum(createAlbumCursor(context, "_id=?", new String[]{String.valueOf(id)}));
    }

    public static Cursor createAlbumCursor(Context context, String selection, String[] parameters){

        return context.getContentResolver().query(Albums.EXTERNAL_CONTENT_URI, new String[]{"_id", "album", "artist", "album_key", "minyear","album_art", "numsongs"}, selection, parameters, "album_key");
                }
                }
