package com.example.rhysn.finalproject;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.rhysn.finalproject.fragments.MiniPlayerFragment;
import com.example.rhysn.finalproject.services.MediaPlayerServicetemps;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;

public abstract class BaseActivity extends AppCompatActivity {

    public static final String Broadcast_ADD_END = "com.example.rhysn.finalproject.AddToEnd";
    public static final String Broadcast_ADD_START = "com.example.rhysn.finalproject.AddToStart";
    public static final String Broadcast_PLAY_NEW_ALBUM = "com.example.rhysn.finalproject.PlayNewAlbum";
    public static final String Broadcast_PLAY_NEW_SONG = "com.example.rhysn.finalproject.PlayNewSong";
    public static final String Broadcast_PLAY_STATE = "com.example.rhysn.finalproject.UpdateMiniplayer";

    private MediaPlayerServicetemps player;
    private MiniPlayerFragment miniplayerFragment;
    MenuItem mediaRouteMenuItem;
    CastContext castContext;
    CastSession castSession;


    private boolean serviceBound;

    private BroadcastReceiver playingInfoReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = new Bundle();
            b.putString("artistName", intent.getExtras().get("artistName").toString());
            b.putString("songTitle", intent.getExtras().get("songTitle").toString());
            b.putLong("albumID", intent.getExtras().getLong("albumID"));
            miniplayerFragment.updateInfo(b);

        }
    };
    private void register_updatePlayingInfo() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("PLAYER_UPDATE");
        registerReceiver(playingInfoReciever, filter);

    }

    @Override
    public void setContentView(View view) {

        miniplayerFragment =(MiniPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback_controls);
        super.setContentView(view);
    }

    private final ThreadLocal<ServiceConnection> serviceConnection = new ThreadLocal<ServiceConnection>() {
        @Override
        protected ServiceConnection initialValue() {
            return new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    // We've bound to LocalService, cast the IBinder and get LocalService instance
                    MediaPlayerServicetemps.LocalBinder binder = (MediaPlayerServicetemps.LocalBinder) service;
                    player = binder.getService();
                    serviceBound = true;
                }


                @Override
                public void onServiceDisconnected(ComponentName name) {
                    serviceBound = false;
                }
            };
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);


        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                menu,
                R.id.media_route_menu_item);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent playerIntent = new Intent(this, MediaPlayerServicetemps.class);
        startService(playerIntent);
        bindService(playerIntent, serviceConnection.get(), Context.BIND_AUTO_CREATE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        castContext = CastContext.getSharedInstance(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection.get());
        }
        unregisterReceiver(playingInfoReciever);
    }

    @Override
    protected void onResume(){
        super.onResume();
        register_updatePlayingInfo();
        sendBroadcast(new Intent(Broadcast_PLAY_STATE));
       miniPlayerVisibility();
    }

    @Override
    protected void onPause() {
        super.onPause();
        register_updatePlayingInfo();
        miniPlayerVisibility();
    }

    @Override
    protected void onStart() {
        super.onStart();
        register_updatePlayingInfo();
        miniplayerFragment = (MiniPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback_controls);
        miniPlayerVisibility();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void miniPlayerVisibility() {
        if (serviceBound) {
            if (player.getState()) {
                getSupportFragmentManager().beginTransaction().show(miniplayerFragment).commit();
            } else {
                getSupportFragmentManager().beginTransaction().hide(miniplayerFragment).commit();
            }
        } else {
            getSupportFragmentManager().beginTransaction().hide(miniplayerFragment).commit();
        }

    }

}

