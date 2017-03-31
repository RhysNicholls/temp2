package com.example.rhysn.finalproject.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rhysn.finalproject.R;
import com.example.rhysn.finalproject.adapters.AlbumAdapter;
import com.example.rhysn.finalproject.adapters.SongAdapter;
import com.example.rhysn.finalproject.data.Album;
import com.example.rhysn.finalproject.data.Song;
import com.example.rhysn.finalproject.loaders.AlbumLoader;
import com.example.rhysn.finalproject.loaders.SongLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AllSongsFragment extends Fragment {

        private RecyclerView rView;
        private ArrayList<Song> songs;
        private RecyclerView.LayoutManager mLayoutManager;
        private DividerItemDecoration dividerItemDecoration;

        public AllSongsFragment() {
            // Required empty public constructor
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            songs = SongLoader.getSongs(getActivity());

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

            View rootView =  inflater.inflate(R.layout.song_fragment, container, false);
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
