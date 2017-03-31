package com.example.rhysn.finalproject.utils;

import android.content.Context;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;

import com.example.rhysn.finalproject.data.Song;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;

import static fi.iki.elonen.NanoHTTPD.Response.Status.NOT_FOUND;
import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;

public class TestHttpServer extends NanoHTTPD {

    final int port;
    Song song;
    Context appContext;

    public TestHttpServer(int portNumber, Context context) throws IOException {
        super(portNumber);
        port = portNumber;
        appContext = context;
    }

   public void setMedia(Song songToPlay) {
        song = songToPlay;
    }
    /* @Override, overrides the default serve provided by NannoHttps
     Checks what media format is being served and then runs the corresponding
      sub serve
     */

    @Override
    public Response serve(IHTTPSession session) {
        if (song == null) {
            return new Response(NOT_FOUND, MIME_PLAINTEXT, "No music");
        }
        if (session.getUri().contains("image")) {
            return serveImage();
        } else {
            return serveMusic();
        }
    }
    /* Response to serve an Audio file.
       Takes the local media uri and serves it through an imputstream
       to be read by the connected Cast device
     */

    private Response serveMusic() {
        InputStream stream = null;
        try {
            stream = new FileInputStream(
                    song.getData());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new Response(OK, "audio/mp3", stream);
    }

    /* Response to serve an Image.
       Takes the local media uri and serves it through an imputstream
       to be read by the connected Cast device
     */
    private Response serveImage() {
        InputStream stream = null;
        try {
            stream = appContext.getContentResolver().openInputStream(Uri.parse("content://media/external/audio/albumart/"+
                    song.getAlbumId()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new Response(OK, "image/jpeg", stream);
    }
}
