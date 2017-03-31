package com.example.rhysn.finalproject.activities;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.rhysn.finalproject.R;
import com.example.rhysn.finalproject.data.Song;
import com.example.rhysn.finalproject.services.MediaPlayerServicetemps;
import com.example.rhysn.finalproject.utils.StorageUtil;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.ImagePicker;
import com.google.android.gms.cast.framework.media.uicontroller.UIMediaController;

import java.util.ArrayList;

/**
 * Created by rhysn on 17/03/2017.
 */

public class NowPlayingActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    //Service Variables
    public static final String Broadcast_PLAY_NEW_ALBUM = "com.example.rhysn.finalproject.PlayNewAlbum";
    public static final String Broadcast_SKIP_NEXT = "com.example.rhysn.finalproject.SkipToNextTrack";
    public static final String Broadcast_SKIP_PREVIOUS = "com.example.rhysn.finalproject.SkipToPreviousTrack";
    public static final String Broadcast_PLAY = "com.example.rhysn.finalproject.PlayTrack";
    public static final String Broadcast_PAUSE = "com.example.rhysn.finalproject.PauseTrack";

    boolean serviceBound = false;
    ArrayList<Song> songList;
    private CastContext castContext;
    private CastSession castSession;
    private UIMediaController uiMediaController;
    private MediaPlayerServicetemps player;
    private ImageView btnPlay;
    private ImageView btnPrevious;
    private ImageView btnNext;
    private ImageView btnPlaylist;
    private ImageView albumArt;
    private SeekBar progressBar;
    private TextView songArtistName;
    private TextView songAlbumTitle;
    private TextView songTitle;
    private TextView duration;
    private TextView elapsed;
    private long albumID;
    private Handler seekBarHandler = new Handler();
    private int currSong;
    private Runnable updateUiTask = new Runnable() {
        @Override
        public void run() {
            if (serviceBound) {
                StorageUtil storage = new StorageUtil(getApplicationContext());
                long totalDuration = player.getDur();
                long currentDuration = player.getPosn();

                progressBar.setProgress((int) (currentDuration) / 1000);

                songList = storage.loadAudio();
                currSong = storage.loadAudioIndex();

                duration.setText(durationFromMilli(totalDuration));
                elapsed.setText(durationFromMilli(currentDuration));

                songArtistName.setText(songList.get(currSong).getArtistName());
                songTitle.setText(songList.get(currSong).getTitle());
                songAlbumTitle.setText(songList.get(currSong).getAlbumName());

                seekBarHandler.postDelayed(this, 1000);
            }

        }
    };
    private final ThreadLocal<ServiceConnection> serviceConnection = new ThreadLocal<ServiceConnection>() {
        @Override
        protected ServiceConnection initialValue() {
            return new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    MediaPlayerServicetemps.LocalBinder binder = (MediaPlayerServicetemps.LocalBinder) service;
                    player = binder.getService();
                    serviceBound = true;
                    updateUi();
                }


                @Override
                public void onServiceDisconnected(ComponentName name) {
                    serviceBound = false;
                }
            };
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_player);
        Intent playerIntent = new Intent(this, MediaPlayerServicetemps.class);
        bindService(playerIntent, serviceConnection.get(), Context.BIND_AUTO_CREATE);
        setupToolBar();

        //Playback Buttons
        btnPlay = (ImageView) findViewById(R.id.mainplayer_play);
        btnPrevious = (ImageView) findViewById(R.id.mainplayer_previous);
        btnNext = (ImageView) findViewById(R.id.mainplayer_next);

        //Song Info
        songArtistName = (TextView) findViewById(R.id.mainplayer_artist_name);
        songAlbumTitle = (TextView) findViewById(R.id.mainplayer_track_album);
        songTitle = (TextView) findViewById(R.id.mainplayer_track_name);
        duration = (TextView) findViewById(R.id.mainplayer_track_duration);
        elapsed = (TextView) findViewById(R.id.mainplayer_track_progress);
        albumArt = (ImageView) findViewById(R.id.main_player_background);

        //Playlist
        btnPlaylist = (ImageView) findViewById(R.id.playlist);

        //Seekbar
        progressBar = (SeekBar) findViewById(R.id.mainplayer_track_seekbar);

        progressBar.setOnSeekBarChangeListener(this);

        //onClickListeners

        btnPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), PlaylistActivity.class);
                startActivity(i);
            }
        });
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getState()) {
                    btnPlay.setImageResource(R.drawable.ic_play_circle_filled_white_48dp);
                    Intent broadcastIntent = new Intent(Broadcast_PAUSE);
                    sendBroadcast(broadcastIntent);
                } else {
                    btnPlay.setImageResource(R.drawable.ic_pause_circle_filled_white_48dp);
                    Intent broadcastIntent = new Intent(Broadcast_PLAY);
                    sendBroadcast(broadcastIntent);
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currSong < (songList.size() - 1)) {
                    Intent broadcastIntent = new Intent(Broadcast_SKIP_NEXT);
                    sendBroadcast(broadcastIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "End of Playlist",
                            Toast.LENGTH_LONG).show();
                    player.stopSelf();
                }

            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currSong > 0) {

                    Intent broadcastIntent = new Intent(Broadcast_SKIP_PREVIOUS);
                    sendBroadcast(broadcastIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "Start of Playlist",
                            Toast.LENGTH_LONG).show();
                    player.stopSelf();
                }

            }
        });

        castContext = CastContext.getSharedInstance(this);
        castSession = castContext.getSessionManager().getCurrentCastSession();
        uiMediaController = new UIMediaController(this);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
        //updateUi();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
        //loadSongListAndIndex();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection.get());
            seekBarHandler.removeCallbacks(updateUiTask);

        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        loadSongListAndIndex();

    }

    public void updateUi() {
        if(castSession != null){
            loadSongListAndIndex();
            //Seekbar
            uiMediaController.bindSeekBar(progressBar);
            //Song Title
            uiMediaController.bindTextViewToMetadataOfCurrentItem(songTitle, MediaMetadata.KEY_TITLE);
            //Album Title
            uiMediaController.bindTextViewToMetadataOfCurrentItem(songAlbumTitle,
                    MediaMetadata.KEY_ALBUM_TITLE);
            //Artist Name
            uiMediaController.bindTextViewToMetadataOfCurrentItem(songArtistName, MediaMetadata.KEY_ALBUM_ARTIST);
            //Song Suration
            uiMediaController.bindTextViewToStreamDuration(duration);
            //Elapsed Time
            uiMediaController.bindTextViewToStreamPosition(elapsed, true);
            //Album Art
            uiMediaController.bindImageViewToImageOfCurrentItem(albumArt,
                    ImagePicker.IMAGE_TYPE_LOCK_SCREEN_BACKGROUND,
                    R.drawable.cast_album_art_placeholder_large);
            //Skip track
            uiMediaController.bindViewToSkipNext(btnNext, R.drawable.ic_skip_next_white_36dp);
            //Previous Track
            uiMediaController.bindViewToSkipPrev(btnPrevious, R.drawable.ic_skip_previous_white_36dp);
            //Play and Puase Button
            uiMediaController.bindImageViewToPlayPauseToggle(btnPlay,
                    getResources().getDrawable(R.drawable.ic_play_circle_filled_white_48dp),
                    getResources().getDrawable(R.drawable.ic_pause_circle_filled_white_48dp),
                    getResources().getDrawable(R.drawable.cast_ic_stop_circle_filled_white),
                    null, false);
        }else{
            if (albumArt != null) {
                Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), songList.get(currSong).getAlbumId());

                Glide.with(getApplicationContext()).load(uri).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        albumArt.setImageBitmap(resource);
                    }
                });
            }
            progressBar.setProgress(0);
            progressBar.setMax((int) player.getDur() / 1000);
            seekBarHandler.postDelayed(updateUiTask, 1000);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            player.seek(progress * 1000);

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        seekBarHandler.removeCallbacks(updateUiTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        seekBarHandler.removeCallbacks(updateUiTask);
        updateUi();

    }

    public void setupToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setShowHideAnimationEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String durationFromMilli(long durationMs) {

        String duration = "";

        int dur = (int) durationMs;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hrs == 0) {
            duration = String.format("%02d:%02d", mns, scs);
        } else {
            duration = String.format("%02d:%02d:%02d", hrs, mns, scs);
        }

        return duration;
    }

    public void loadSongListAndIndex(){
        StorageUtil storage =new StorageUtil(this);
        songList = storage.loadAudio();
        currSong = storage.loadAudioIndex();
    }

}
