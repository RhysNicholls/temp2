package com.example.rhysn.finalproject.services;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;


import com.example.rhysn.finalproject.data.Song;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;

/**
 * Created by rhysn on 23/03/2017.
 */

public class CastService  {

    private Song activeAudio;
    private CastSession castSession;
    private SessionManagerListener<CastSession>  sessionManagaer;
    private CastContext castContext;


    public enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE
    }




    public void setupCastListener(){
        sessionManagaer = new SessionManagerListener<CastSession>() {
            @Override
            public void onSessionStarting(CastSession session) {

            }

            @Override
            public void onSessionStarted(CastSession castSession, String s) {

            }

            @Override
            public void onSessionStartFailed(CastSession castSession, int i) {

            }

            @Override
            public void onSessionEnding(CastSession castSession) {

            }

            @Override
            public void onSessionEnded(CastSession castSession, int i) {

            }

            @Override
            public void onSessionResuming(CastSession castSession, String s) {

            }

            @Override
            public void onSessionResumed(CastSession castSession, boolean b) {

            }

            @Override
            public void onSessionResumeFailed(CastSession castSession, int i) {

            }

            @Override
            public void onSessionSuspended(CastSession castSession, int i) {

            }
        };
    }

    public MediaInfo buildMediaInfo(){
        MediaMetadata songMetaData = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);

        Uri artUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), activeAudio.getAlbumId());


        songMetaData.putString(MediaMetadata.KEY_TITLE, activeAudio.getTitle());
        songMetaData.putString(MediaMetadata.KEY_ALBUM_TITLE, activeAudio.getAlbumName());
        songMetaData.putString(MediaMetadata.KEY_ALBUM_ARTIST, activeAudio.getArtistName());
        WebImage art = new WebImage(artUri);
        songMetaData.addImage(art);
        songMetaData.addImage(art);

        return new MediaInfo.Builder(activeAudio.getData())
                .setContentType("audio/mpeg")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(songMetaData)
                .build();
    }

    public void loadRemoteMedia(int position, boolean autoPlay){
        if (castSession == null){
            return;
        }
        RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
        if(remoteMediaClient == null){
            return;
        }
        remoteMediaClient.load(buildMediaInfo(), autoPlay, position);
    }
}
