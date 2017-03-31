package com.example.rhysn.finalproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rhysn.finalproject.R;
import com.example.rhysn.finalproject.data.Song;

import java.util.ArrayList;

/**
 * Created by rhysn on 12/03/2017.
 */

public class SongAdapter extends RecyclerView.Adapter<com.example.rhysn.finalproject.adapters.SongAdapter.MyViewHolder> {

    private static final String Broadcast_PLAY_NEW_SONG = "com.example.rhysn.finalproject.PlayNewSong";

    private Context context;
    private ArrayList<Song> songs;

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView songTitle, songArtist, songDuration;

        MyViewHolder(View view) {
            super(view);
            songTitle = (TextView) view.findViewById(R.id.song_title);
            songArtist = (TextView) view.findViewById(R.id.song_artist);
            songDuration = (TextView) view.findViewById(R.id.song_duration);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long songID= songs.get(getAdapterPosition()).getId();
                    Intent i = new Intent(Broadcast_PLAY_NEW_SONG);
                    i.putExtra("songID", songID);
                    context.sendBroadcast(i);
                }
            });
        }
    }

        public SongAdapter(Context context, ArrayList<Song> allSongs){
            this.context = context;
            this.songs = allSongs;
        }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder songHolder, int position) {

        Song currSong = songs.get(position);

        songHolder.songArtist.setText(currSong.getArtistName());
        songHolder.songTitle.setText(currSong.getTitle());
        songHolder.songDuration.setText(duration(currSong.getDuration()));
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    private String duration(long durationMs){

        int dur = (int) durationMs;
        //int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        return String.format("%02d:%02d",  mns, scs);
    }


}
