package com.example.rhysn.finalproject.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.example.rhysn.finalproject.R;
import com.example.rhysn.finalproject.adapters.SongAdapter;
import com.example.rhysn.finalproject.data.Song;
import com.example.rhysn.finalproject.utils.StorageUtil;

import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity {

    private RecyclerView rView;
    private ArrayList<Song> songList;
    private SongAdapter songAdpt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Currently Playing");

        StorageUtil storage = new StorageUtil(this);
        songList = storage.loadAudio();

        rView = (RecyclerView) findViewById(R.id.song_recycler_view);
        songAdpt = new SongAdapter(this, songList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rView.setLayoutManager(layoutManager);
        //rView.setOnClickListener();
        rView.setAdapter(songAdpt);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home  :
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
