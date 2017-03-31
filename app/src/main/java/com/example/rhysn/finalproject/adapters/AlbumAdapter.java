package com.example.rhysn.finalproject.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.rhysn.finalproject.R;
import com.example.rhysn.finalproject.activities.AlbumDetailsActivity;
import com.example.rhysn.finalproject.data.Album;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Album> albums;

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView albumTitle, noOfSongs;
        ImageView albumArt;

        MyViewHolder(View view) {
            super(view);
            albumTitle = (TextView) view.findViewById(R.id.album_title);
            noOfSongs = (TextView) view.findViewById(R.id.album_no_of_tracks);
            albumArt = (ImageView) view.findViewById(R.id.album_art);
            albumArt.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            long albumId = albums.get(getAdapterPosition()).getAlbumId();

            Intent i = new Intent(context, AlbumDetailsActivity.class);
            i.putExtra("albumID", albumId);
            context.startActivity(i);

        }
    }

    public AlbumAdapter(Context c, ArrayList<Album> albums) {
        this.context = c;
        this.albums = albums;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder albumHolder, int position) {
        Album currAlbum = albums.get(position);
        Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),currAlbum.getAlbumId());

        Glide.with(context).load(uri).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                albumHolder.albumArt.setImageBitmap(resource);
            }
        });

        albumHolder.albumTitle.setText(currAlbum.getAlbumName());
        albumHolder.noOfSongs.setText(currAlbum.getSongCount() + " songs");


    }

    @Override
    public int getItemCount() {
        return albums.size();
    }




}


