package com.example.rhysn.finalproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.example.rhysn.finalproject.activities.ArtistDetailActivity;
import com.example.rhysn.finalproject.data.Artists;

import java.util.ArrayList;

/**
 * Created by rhysn on 12/03/2017.
 */

public class ArtistAdapter extends RecyclerView.Adapter<com.example.rhysn.finalproject.adapters.ArtistAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Artists> artists;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        public TextView artistName, noOfAlbums;
        public ImageView artistArt;

        public MyViewHolder(View view) {
            super(view);
            artistName = (TextView) view.findViewById(R.id.artist_name);
            noOfAlbums = (TextView) view.findViewById(R.id.artist_no_of_albums);
            artistArt = (ImageView) view.findViewById(R.id.artist_art);
            artistArt.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            long artistId = artists.get(getAdapterPosition()).getArtistID();
            String artistName = artists.get(getAdapterPosition()).getArtistName();
            String artistArt = artists.get(getAdapterPosition()).getArtUrl();
            String artistBio = artists.get(getAdapterPosition()).getBio();
            Intent i = new Intent(context, ArtistDetailActivity.class);
            i.putExtra("artistID", artistId);
            i.putExtra("artistName", artistName);
            i.putExtra("artistArtUrl", artistArt);
            i.putExtra("artistBio", artistBio);
            context.startActivity(i);

        }
    }

    public ArtistAdapter(Context c, ArrayList<Artists> allArtists) {
        this.context = c;
        this.artists = allArtists;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.artist, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder artistHolder, int position) {
        Artists currArtist = artists.get(position);

        Glide.with(context)
                .load(currArtist.getArtUrl())
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        artistHolder.artistArt.setImageBitmap(resource);
                    }
                });

        artistHolder.artistName.setText(currArtist.getArtistName());
        artistHolder.noOfAlbums.setText(currArtist.getAlbumCount() + " Albums");
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }


}






