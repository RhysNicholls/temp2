package com.example.rhysn.finalproject.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rhysn.finalproject.R;
import com.example.rhysn.finalproject.adapters.SongAdapter;
import com.example.rhysn.finalproject.data.Song;
import com.example.rhysn.finalproject.loaders.SongLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * Created by rhysn on 21/03/2017.
 */


public class ArtistSongsFragment extends Fragment {


    private RecyclerView rView;
    private ArrayList<Song> songs;
    private RecyclerView.LayoutManager mLayoutManager;
    private DividerItemDecoration dividerItemDecoration;

    public ArtistSongsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long artistID = getActivity().getIntent().getExtras().getLong("artistID");


        songs = SongLoader.getSongsByArtistId(getActivity(), artistID);

        Collections.sort(songs, new Comparator<Song>() {
            @Override
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.song_fragment, container, false);
        rView = (RecyclerView) rootView.findViewById(R.id.song_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        rView.setLayoutManager(mLayoutManager);

        dividerItemDecoration = new DividerItemDecoration(rView.getContext(), 0);
        rView.addItemDecoration(dividerItemDecoration);

        SongAdapter songAdpt = new SongAdapter(getActivity(), songs);
        rView.setAdapter(songAdpt);

        return rootView;
    }
}

