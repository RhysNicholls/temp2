package com.example.rhysn.finalproject.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.rhysn.finalproject.BaseActivity;
import com.example.rhysn.finalproject.R;
import com.example.rhysn.finalproject.activities.AlbumDetailsActivity;
import com.example.rhysn.finalproject.activities.MainActivity;
import com.example.rhysn.finalproject.activities.NowPlayingActivity;
import com.example.rhysn.finalproject.data.Song;
import com.example.rhysn.finalproject.fragments.MiniPlayerFragment;
import com.example.rhysn.finalproject.utils.PlaybackStatus;
import com.example.rhysn.finalproject.utils.StorageUtil;
import com.example.rhysn.finalproject.utils.TestHttpServer;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import static com.example.rhysn.finalproject.loaders.SongLoader.getSongById;
import static com.example.rhysn.finalproject.loaders.SongLoader.getSongsByAlbumId;

public class MediaPlayerServicetemps extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_PLAY = "com.example.rhysn.finalproject.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.rhysn.finalproject.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.example.rhysn.finalproject.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.example.rhysn.finalproject.ACTION_NEXT";
    public static final String ACTION_STOP = "com.example.rhysn.finalproject.ACTION_STOP";

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private int resumePosition;
    private boolean ongoingCall = false;
    private boolean shuffleOn;
    private boolean repeatAll;
    private boolean repeatOne;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    //List of available Audio files
    private ArrayList<Song> audioList;
    private int audioIndex = 0;
    private Song activeAudio; //an object of the currently playing audio

    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    //Casting
    CastContext castContext;
    CastSession castSession;
    boolean remote;
    TestHttpServer server;

    private static final int NOTIFICATION_ID = 101;

    public void onCreate() {
        super.onCreate();
        // Perform one-time setup procedures

        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAlbum();
        register_skipToNextTrack();
        register_skipToPreviousTrack();
        register_pauseTrack();
        register_playTrack();
        register_addToEnd();
        register_addToStart();
        register_updateMiniPlayer();
        register_playNewSong();

    }

    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                broadcastToMiniPlayer(audioList.get(audioIndex));
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                updateMetaData();
                broadcastToMiniPlayer(audioList.get(audioIndex));
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                updateMetaData();

                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void updateMetaData() {

        Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), activeAudio.getAlbumId());

        Glide
                .with(getApplicationContext())
                .load(uri)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, resource)
                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getArtistName())
                                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getAlbumName())
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getTitle())
                                .build());
                    }

                });
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);

        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

    }

    public long getPosn() {
        return mediaPlayer.getCurrentPosition();
    }

    public long getDur() {
        return mediaPlayer.getDuration();
    }

    public void seek(int position) {
        mediaPlayer.seekTo(position);
    }

    public boolean getState() {
        return mediaPlayer.isPlaying();
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }


    // Binder given to clients
    private final IBinder iBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        StorageUtil storage = new StorageUtil(getApplicationContext());
        if (audioIndex < audioList.size()) {
            skipToNext();
            audioIndex++;
            storage.storeAudioIndex(audioIndex);
          //  broadcastToMiniPlayer(audioList.get(audioIndex));
        } else {
            stopMedia();
            stopSelf();
        }
    }

    //Handle errors
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation.
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            //Load data from SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            audioList = storage.loadAudio();
            audioIndex = storage.loadAudioIndex();

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }

        //Request audio focus
        if (requestAudioFocus() == false) {
            //Could not gain focus
            stopSelf();
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            // buildNotification(PlaybackStatus.PLAYING);
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onAudioFocusChange(int focusChange) {

        //Invoked when the audio focus of the system is updated.
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private void callStateListener() {

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {

                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:

                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };

        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    public class LocalBinder extends Binder {
        public MediaPlayerServicetemps getService() {
            return MediaPlayerServicetemps.this;
        }
    }

    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    private BroadcastReceiver playNewAlbum = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            StorageUtil storage = new StorageUtil(getApplicationContext());
            castContext = CastContext.getSharedInstance(getApplicationContext());
            castSession = castContext.getSessionManager().getCurrentCastSession();
            if (castSession != null && castSession.isConnected()) {
                remote = true;
            }

            stopMedia();
            mediaPlayer.reset();

            storage.clearCachedAudioPlaylist();

            Bundle b = intent.getExtras();
            long albumID = b.getLong("albumID");
            audioList = getSongsByAlbumId(getApplicationContext(), albumID);
            storage.storeAudio(audioList);
            audioIndex = 0;
            storage.storeAudioIndex(audioIndex);

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }
            if (remote) {
                if (server == null || !server.isAlive()) {
                    startServer();
                }
                server.setMedia(activeAudio);
                castAudio();
            } else {
                initMediaPlayer();
                try {
                    mediaPlayer.setDataSource(activeAudio.getData());
                } catch (IOException e) {
                    stopSelf();
                }
                mediaPlayer.prepareAsync();
                updateMetaData();
                broadcastToMiniPlayer(audioList.get(audioIndex));

                Intent i = new Intent(context, NowPlayingActivity.class);
                i.putExtra("albumID", albumID);
                context.startActivity(i);

                buildNotification(PlaybackStatus.PLAYING);
            }
        }
    };

    private void register_playNewAlbum() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(NowPlayingActivity.Broadcast_PLAY_NEW_ALBUM);
        registerReceiver(playNewAlbum, filter);
    }

    private BroadcastReceiver playNewSong = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            StorageUtil storage = new StorageUtil(getApplicationContext());
            castContext = CastContext.getSharedInstance(getApplicationContext());
            castSession = castContext.getSessionManager().getCurrentCastSession();
            if (castSession != null && castSession.isConnected()) {
                remote = true;
            }

            stopMedia();

            storage.clearCachedAudioPlaylist();

            Bundle b = intent.getExtras();
            long songID = b.getLong("songID");
            activeAudio = getSongById(getApplicationContext(), songID);
            audioList = new ArrayList<Song>();
            audioList.add(activeAudio);
            audioIndex = 0;
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);


            if (remote) {
                if (server == null || !server.isAlive()) {
                    startServer();
                }
                server.setMedia(activeAudio);
                castAudio();
            } else {
                mediaPlayer.reset();
                initMediaPlayer();
                try {
                    mediaPlayer.setDataSource(activeAudio.getData());
                    mediaPlayer.prepareAsync();
                    broadcastToMiniPlayer(audioList.get(audioIndex));
                    Intent i = new Intent(context, NowPlayingActivity.class);
                    i.putExtra("songID", songID);
                    context.startActivity(i);

                } catch (IOException e) {
                    stopSelf();
                }

                updateMetaData();


                buildNotification(PlaybackStatus.PLAYING);


            }


        }
    };

    private void register_playNewSong() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(BaseActivity.Broadcast_PLAY_NEW_SONG);
//        filter.addAction(MainActivity.Broadcast_PLAY_NEW_SONG);
//        filter.addAction(AlbumDetailsActivity.Broadcast_PLAY_NEW_SONG);
        registerReceiver(playNewSong, filter);
    }

    private BroadcastReceiver skipToNextTrack = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            skipToNext();
        }
    };

    private void register_skipToNextTrack() {
        IntentFilter filter = new IntentFilter(NowPlayingActivity.Broadcast_SKIP_NEXT);
        registerReceiver(skipToNextTrack, filter);
    }

    private BroadcastReceiver updateMiniPlayer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // if (audioIndex < audioList.size()) {
            broadcastToMiniPlayer(audioList.get(audioIndex));
            //}
        }
    };

    private void register_updateMiniPlayer() {
        IntentFilter filter = new IntentFilter(MiniPlayerFragment.Broadcast_REQUEST_PLAYSTATE);
        registerReceiver(updateMiniPlayer, filter);
    }

    private BroadcastReceiver playTrack = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playMedia();
        }
    };

    private void register_playTrack() {
        IntentFilter filter = new IntentFilter(NowPlayingActivity.Broadcast_PLAY);
        registerReceiver(playTrack, filter);
    }

    private BroadcastReceiver pauseTrack = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
        }
    };

    private void register_pauseTrack() {
        IntentFilter filter = new IntentFilter(NowPlayingActivity.Broadcast_PAUSE);
        registerReceiver(pauseTrack, filter);
    }

    private BroadcastReceiver skipToPreviousTrack = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            skipToPrevious();
        }
    };

    private void register_skipToPreviousTrack() {
        IntentFilter filter = new IntentFilter(NowPlayingActivity.Broadcast_SKIP_PREVIOUS);
        registerReceiver(skipToPreviousTrack, filter);
    }

    private BroadcastReceiver addToEnd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playlistAddToEnd((long) intent.getExtras().get("songID"));
        }
    };

    private void register_addToEnd() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.Broadcast_ADD_END);
        filter.addAction(AlbumDetailsActivity.Broadcast_ADD_END);
        registerReceiver(addToEnd, filter);
    }

    private BroadcastReceiver addToStart = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playlistAddToStart((long) intent.getExtras().get("songID"));
        }
    };

    private void register_addToStart() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.Broadcast_ADD_START);
        filter.addAction(AlbumDetailsActivity.Broadcast_ADD_START);
        registerReceiver(addToStart, filter);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        if (server != null && server.isAlive()) {
            server.stop();
        }
        if (castSession != null) {
            castSession.getRemoteMediaClient().stop();
        }

        //removeNotification();

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAlbum);
        unregisterReceiver(skipToNextTrack);
        unregisterReceiver(skipToPreviousTrack);
        unregisterReceiver(pauseTrack);
        unregisterReceiver(playTrack);
        unregisterReceiver(addToStart);
        unregisterReceiver(addToEnd);
        unregisterReceiver(updateMiniPlayer);
        unregisterReceiver(playNewSong);

        //clear cached playlist
        new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
    }

    private void skipToNext() {

        StorageUtil storage = new StorageUtil(this);
        audioList = storage.loadAudio();

        if (audioIndex == audioList.size() - 1) {
            //if last in playlist
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get next in playlist
            activeAudio = audioList.get(++audioIndex);

        }

        //Update stored index
        storage.storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer

        if (remote) {
            if (server == null || !server.isAlive()) {
                startServer();
            }
            server.setMedia(activeAudio);
            castAudio();
        } else {
            mediaPlayer.reset();
            initMediaPlayer();
            try {
                mediaPlayer.setDataSource(activeAudio.getData());
            } catch (IOException e) {
                stopSelf();
            }
            mediaPlayer.prepareAsync();
            broadcastToMiniPlayer(audioList.get(audioIndex));
            buildNotification(PlaybackStatus.PLAYING);
        }

    }

    private void skipToPrevious() {

        StorageUtil storage = new StorageUtil(this);
        audioList = storage.loadAudio();

        if (audioIndex == 0) {
            audioIndex = audioList.size() - 1;
            activeAudio = audioList.get(audioIndex);
        } else {

            activeAudio = audioList.get(--audioIndex);
        }

        //Update stored index
        storage.storeAudioIndex(audioIndex);

        stopMedia();

        if (remote) {
            if (server == null || !server.isAlive()) {
                startServer();
            }
            server.setMedia(activeAudio);
            castAudio();
        } else {
            mediaPlayer.reset();
            initMediaPlayer();
            try {
                mediaPlayer.setDataSource(activeAudio.getData());
            } catch (IOException e) {
                stopSelf();
            }

            mediaPlayer.prepareAsync();
            broadcastToMiniPlayer(audioList.get(audioIndex));
            buildNotification(PlaybackStatus.PLAYING);
        }

    }

    private void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }
        Bitmap largeIcon = null;
        Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), activeAudio.getAlbumId());
        try {
            largeIcon = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Bitmap largeIcon = BitmapFactory.decodeStream(uri); //replace with your own image

        // Create a new Notification
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setShowWhen(false)
                // Set the Notification style
                .setStyle(new NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
                        // Show our playback controls in the compact notification view.
                        .setShowActionsInCompactView(0, 1, 2))
                // Set the Notification color
                .setColor(getResources().getColor(R.color.colorPrimary))
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.dark_header)
                // Set Notification content information
                .setContentText(activeAudio.getArtistName())
                .setContentTitle(activeAudio.getAlbumName())
                .setContentInfo(activeAudio.getTitle())
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerServicetemps.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }

    private void playlistAddToEnd(long songID) {

        StorageUtil storage = new StorageUtil(this);

        int audioIndexTemp = storage.loadAudioIndex();
        ArrayList<Song> temp = storage.loadAudio();

        temp.add(getSongById(this, songID));

        storage.clearCachedAudioPlaylist();
        storage.storeAudio(temp);
        storage.storeAudioIndex(audioIndexTemp);
    }

    private void playlistAddToStart(long songID) {
        StorageUtil storage = new StorageUtil(this);

        int audioIndexTemp = storage.loadAudioIndex();
        ArrayList<Song> temp = storage.loadAudio();

        temp.add(1, getSongById(this, songID));

        storage.clearCachedAudioPlaylist();
        storage.storeAudio(temp);
        storage.storeAudioIndex(audioIndexTemp);
    }

    private void broadcastToMiniPlayer(Song playingSong) {

        Intent i = new Intent();

        i.putExtra("artistName", playingSong.getArtistName());
        i.putExtra("songTitle", playingSong.getTitle());
        i.putExtra("albumID", playingSong.getAlbumId());
        i.putExtra("isPlaying", mediaPlayer.isPlaying());
        i.setAction("PLAYER_UPDATE");
        sendBroadcast(i);

    }

    public MediaInfo buildMediaInfo(String url) {
        MediaMetadata songMetaData = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);

        songMetaData.putString(MediaMetadata.KEY_TITLE, activeAudio.getTitle());
        songMetaData.putString(MediaMetadata.KEY_ALBUM_TITLE, activeAudio.getAlbumName());
        songMetaData.putString(MediaMetadata.KEY_ALBUM_ARTIST, activeAudio.getArtistName());
        WebImage art = new WebImage(new Uri.Builder().encodedPath(url + "/" + activeAudio.getAlbumId() + "/image").build());
        songMetaData.addImage(art);
        songMetaData.addImage(art);

        return new MediaInfo.Builder(url)
                .setContentType("audio/mpeg")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(songMetaData)
                .build();

    }

    public void castAudio() {
        if (castSession != null) {

            if (server == null || !server.isAlive()) {
                startServer();
            }
            String url = "http://" + getWifiAddress() + ":" + server.getListeningPort();
            server.setMedia(activeAudio);

            final RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();

            remoteMediaClient.load(buildMediaInfo(url));
            Intent i = new Intent(getApplicationContext(), NowPlayingActivity.class);
            startActivity(i);
        }
    }

    private String getWifiAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return Formatter.formatIpAddress(ipAddress);
    }

    private int findOpenPort(String ip, int startPort) {
        final int timeout = 200;
        for (int port = startPort; port <= 65535; port++) {
            if (isPortAvailable(ip, port, timeout)) {
                return port;
            }
        }
        throw new RuntimeException("There is no open port.");
    }

    private boolean isPortAvailable(String ip, int port, int timeout) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private void startServer() {
        try {
            String ip = getWifiAddress();
            int port = findOpenPort(ip, 8080);
            server = new TestHttpServer(port, getApplicationContext());
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}