package com.example.rhysn.finalproject.activities;


import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import com.example.rhysn.finalproject.BaseActivity;
import com.example.rhysn.finalproject.R;
import com.example.rhysn.finalproject.adapters.SongsByAlbumAdapter;
import com.example.rhysn.finalproject.data.Album;
import com.example.rhysn.finalproject.data.Song;
import com.example.rhysn.finalproject.loaders.AlbumLoader;
import com.example.rhysn.finalproject.loaders.SongLoader;

import java.util.ArrayList;


public class AlbumDetailsActivity extends BaseActivity {

    private RecyclerView rView;
    private ArrayList<Song> albumSongs;

    private long albumID;
    private SongsByAlbumAdapter adapter;
    private Album album;
    private String albumName;

    ImageView albumArt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activtiy_album_details);

        //Get albumID from Intent
        Bundle b = new Bundle();
        b = getIntent().getExtras();
        albumID = b.getLong("albumID");

        //setup
        albumArt = (ImageView) findViewById(R.id.detail_album_art);
        rView = (RecyclerView) findViewById(R.id.album_recycler_view);
        rView.setLayoutManager(new LinearLayoutManager(this));
        album = AlbumLoader.getAlbumById(this, albumID);
        albumSongs = SongLoader.getSongsByAlbumId(this, albumID);
        adapter = new SongsByAlbumAdapter(this, albumSongs);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rView.getContext(),
                DividerItemDecoration.HORIZONTAL);
        rView.addItemDecoration(dividerItemDecoration);
        rView.setItemAnimator(new DefaultItemAnimator());
        rView.setAdapter(adapter);
        albumName = album.getAlbumName();
        getAlbumArt();
        setupToolBar();

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                playAlbum();

            }
        });
    }

    public void getAlbumArt(){
        Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),album.getAlbumId());

        Glide.with(this).load(uri).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                albumArt.setImageBitmap(resource);
            }
        });

    }

    public void setupToolBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("" + albumName);

    }

    public void playAlbum(){

        Intent service = new Intent(Broadcast_PLAY_NEW_ALBUM);
        service.putExtra("albumID", albumID);
        sendBroadcast(service);
    }


}
