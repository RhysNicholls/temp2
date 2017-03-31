package com.example.rhysn.finalproject.fragments;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.rhysn.finalproject.R;
import com.example.rhysn.finalproject.activities.NowPlayingActivity;

public class MiniPlayerFragment extends Fragment {

    public static final String Broadcast_REQUEST_PLAYSTATE = "com.example.rhysn.finalproject.MiniPlayerUpdate";
    public static final String Broadcast_PLAY = "com.example.rhysn.finalproject.PlayTrack";
    public static final String Broadcast_PAUSE = "com.example.rhysn.finalproject.PauseTrack";


    private ImageButton playPause;
    private TextView songTitle;
    private TextView artistName;
    private ImageView albumArt;
    private boolean isPlaying;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_miniplayer, container, false);

        playPause = (ImageButton) rootView.findViewById(R.id.play_pause);
        songTitle = (TextView) rootView.findViewById(R.id.player_title);
        artistName = (TextView) rootView.findViewById(R.id.player_artist);
        albumArt = (ImageView) rootView.findViewById(R.id.player_album_art);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), NowPlayingActivity.class);
                startActivity(i);
            }
        });
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    Intent broadcastIntent = new Intent(Broadcast_PAUSE);
                    getActivity().sendBroadcast(broadcastIntent);
                    playPause.setImageResource(R.drawable.ic_play_arrow_white_36dp);
                    isPlaying = false;
                } else {
                    Intent broadcastIntent = new Intent(Broadcast_PLAY);
                    getActivity().sendBroadcast(broadcastIntent);
                    playPause.setImageResource(R.drawable.ic_pause_white_36dp);
                    isPlaying = true;

                }
            }
        });

        return rootView;

    }

    public void getArt(long albumID) {
        Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumID);

        Glide.with(this).load(uri).asBitmap().into(albumArt);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (isPlaying) {
            playPause.setImageResource(R.drawable.ic_pause_white_36dp);

        } else {
            playPause.setImageResource(R.drawable.ic_play_arrow_white_36dp);

        }
    }

    public void updateInfo(Bundle b) {
        artistName.setText(b.getString("artistName"));
        songTitle.setText(b.getString("songTitle"));
        getArt(b.getLong("albumID"));
        isPlaying = true;

    }

}