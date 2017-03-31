package com.example.rhysn.finalproject.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Artists.Albums;

import com.example.rhysn.finalproject.data.Album;

import java.util.ArrayList;

/**
 * Created by rhysn on 10/03/2017.
 */

public class AlbumByArtistLoader {

    public static ArrayList<Album> getAlbumsByArtist(Context context, long artistId){

        ArrayList artistAlbumList = new ArrayList();

        Cursor c = albumByArtistCursor(context, artistId);

        if (c == null || !c.moveToFirst()) {
            if (c != null) {
                c.close();
            }
            return artistAlbumList;
        }
        do {
            artistAlbumList.add(new Album (c.getLong(0), c.getString(1), c.getString(2), artistId, c.getInt(3), c.getString(4), c.getInt(5)));
        } while (c.moveToNext());
        if (c != null) {
            c.close();
        }
        return artistAlbumList;
    }

    public static Cursor albumByArtistCursor(Context context, long artistID) {

        return context.getContentResolver().query(Albums.getContentUri("external", artistID), new String[]{"_id", "album", "artist", "minyear","album_art", "numsongs"}, null, null, "album_key");
    }
}
