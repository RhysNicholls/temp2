package com.example.rhysn.finalproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.rhysn.finalproject.fragments.MiniPlayerFragment;
import com.example.rhysn.finalproject.services.MediaPlayerServicetemps;

public abstract class BaseActivityTemp extends AppCompatActivity {

    public static final String Broadcast_ADD_END = "com.example.rhysn.finalproject.AddToEnd";
    public static final String Broadcast_ADD_START = "com.example.rhysn.finalproject.AddToStart";
    public static final String Broadcast_PLAY_NEW_ALBUM = "com.example.rhysn.finalproject.PlayNewAlbum";
    public static final String Broadcast_PLAY_NEW_SONG = "com.example.rhysn.finalproject.PlayNewSong";

    private MediaPlayerServicetemps player;
    private MiniPlayerFragment miniplayerFragment;

    private boolean serviceBound;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent playerIntent = new Intent(this, MediaPlayerServicetemps.class);
        startService(playerIntent);
        bindService(playerIntent, serviceConnection.get(), Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection.get());
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        miniplayerFragment = (MiniPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback_controls);
        miniPlayerVisibility();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void miniPlayerVisibility(){
        if(serviceBound){
            if(player.getState()) {
                getSupportFragmentManager().beginTransaction().show(miniplayerFragment).commit();
            }else{
                getSupportFragmentManager().beginTransaction().hide(miniplayerFragment).commit();
            }
        }else{
            getSupportFragmentManager().beginTransaction().hide(miniplayerFragment).commit();
        }

    }

    public void setupCastListener(){

    }
}

