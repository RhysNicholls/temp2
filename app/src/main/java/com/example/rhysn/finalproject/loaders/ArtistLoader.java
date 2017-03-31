package com.example.rhysn.finalproject.loaders;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.example.rhysn.finalproject.data.Artists;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by rhysn on 12/03/2017.
 */

public class ArtistLoader {

    public static Artists getArtist(Cursor c) throws ExecutionException, InterruptedException {
        Artists a = null;
        if (c != null && c.moveToFirst()) {
            a = new Artists(c.getLong(0), c.getString(1), c.getInt(2), c.getInt(3), String.valueOf(new GetAlbumArt().execute(c.getString(1)).get()), String.valueOf(new GetSummary().execute(c.getString(1)).get()));
        }
        if (c != null) {
            c.close();
        }
        return a;
    }

    public static ArrayList<Artists> getAllArtists(Cursor c) throws ExecutionException, InterruptedException {
        ArrayList albumList = new ArrayList();
        if (c == null || !c.moveToFirst()) {
            if (c != null) {
                c.close();
            }
            return albumList;
        }
        do {
            albumList.add(new Artists(c.getLong(0), c.getString(1), c.getInt(2), c.getInt(3), String.valueOf(new GetAlbumArt().execute(c.getString(1)).get()), String.valueOf(new GetSummary().execute(c.getString(1)).get())));
        } while (c.moveToNext());
        if (c != null) {
            c.close();
        }
        return albumList;
    }

    public static ArrayList<Artists> getArtists(Context context) throws ExecutionException, InterruptedException {

        return getAllArtists(createArtistCursor(context, null, null));
    }

    public static Artists getArtistById(Context context, long id) throws ExecutionException, InterruptedException {

        return getArtist(createArtistCursor(context, "_id=?", new String[]{String.valueOf(id)}));
    }

    public static Cursor createArtistCursor(Context context, String selection, String[] parameters) {

        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, new String[]{"_id", "artist", "number_of_albums", "number_of_tracks"}, selection, parameters, "artist_key");
        return cursor;
    }

    public static class GetAlbumArt extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {
            String apiUrl = "http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=" + (params[0].replace(" ", "+")) + "&api_key=" + "2f82af21d58cac0b8f032d0c6facdf19";

            Document doc = null;
            try {
                doc = Jsoup.connect(apiUrl).timeout(20000).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements elems = doc.select("image");

            Element a1 = elems.get(3); //0 is the index first element increasing to (elems.size()-1)

            String url = a1.childNode(0).toString().replace("\n", "");
            System.out.println("");

            return url;
        }


    }

    public static class GetSummary extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String apiUrl = "http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=" + (params[0].replace(" ", "+")) + "&api_key=" + "2f82af21d58cac0b8f032d0c6facdf19";

            Document doc = null;
            try {
                doc = Jsoup.connect(apiUrl).timeout(20000).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements elems = doc.select("summary");
            Element a1 = elems.get(0); //0 is the index first element increasing to (elems.size()-1)

            String summary = a1.childNode(0).toString().replace("\n", "");
            System.out.println("");

            return summary;
        }
    }

}
