package com.example.rhysn.finalproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rhysn.finalproject.R;
import com.example.rhysn.finalproject.data.Song;

import java.util.ArrayList;

public class SongsByAlbumAdapter extends RecyclerView.Adapter<SongsByAlbumAdapter.MyViewHolder> {

    private static final String Broadcast_ADD_END = "com.example.rhysn.finalproject.AddToEnd";
    private static final String Broadcast_ADD_START = "com.example.rhysn.finalproject.AddToStart";
    private static final String Broadcast_PLAY_NEW_SONG = "com.example.rhysn.finalproject.PlayNewSong";

    private Context context;
    private ArrayList<Song> albumSongs;

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView songTitle, songDuration;
        private ImageView menu;


        private MyViewHolder(View view) {
            super(view);
            songTitle = (TextView) view.findViewById(R.id.detail_song_title);
            songTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long songID= albumSongs.get(getAdapterPosition()).getId();
                    Intent i = new Intent(Broadcast_PLAY_NEW_SONG);
                    i.putExtra("songID", songID);
                    context.sendBroadcast(i);
                }
            });
            songDuration = (TextView) view.findViewById(R.id.detail_song_duration);
            menu = (ImageView) view.findViewById(R.id.song_menu);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(context, menu);
                    popup.inflate(R.menu.menu_songs);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.add_to_playlist:
                                    Intent add = new Intent(Broadcast_ADD_END);
                                    add.putExtra("songID", albumSongs.get(getAdapterPosition()).getId());
                                    context.sendBroadcast(add);
                                    break;
                                case R.id.play_next:
                                    Intent next = new Intent(Broadcast_ADD_START);
                                    next.putExtra("songID", albumSongs.get(getAdapterPosition()).getId());
                                    context.sendBroadcast(next);
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                }
            });
        }
    }

    public SongsByAlbumAdapter(Context context, ArrayList<Song> albumSongs) {
        this.context = context;
        this.albumSongs = albumSongs;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_song, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder songHolder, int position) {

        Song currSong = albumSongs.get(position);
        songHolder.songTitle.setText(currSong.getTitle());
        songHolder.songDuration.setText(duration(currSong.getDuration()));

    }

    @Override
    public int getItemCount() {
        return albumSongs.size();
    }

    private String duration(long durationMs) {

        int dur = (int) durationMs;
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        return String.format("%02d:%02d", mns, scs);
    }

}
