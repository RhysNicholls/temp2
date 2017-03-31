package com.example.rhysn.finalproject.fragments;


import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rhysn.finalproject.R;
import com.example.rhysn.finalproject.adapters.ArtistAdapter;
import com.example.rhysn.finalproject.data.Artists;
import com.example.rhysn.finalproject.loaders.ArtistLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;


/**
 * Created by rhysn on 13/03/2017.
 */

public class AllArtistsFragment extends Fragment {

        private RecyclerView rView;

        private ArrayList<Artists> artists;
        private RecyclerView.LayoutManager mLayoutManager;

        public AllArtistsFragment() {
            // Required empty public constructor
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            try {
                artists = ArtistLoader.getArtists(getActivity());
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Collections.sort(artists, new Comparator<Artists>() {
           @Override
            public int compare(Artists a, Artists b) {
                return a.getArtistName().compareTo(b.getArtistName());
            }
        });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView =  inflater.inflate(R.layout.fragment_artist, container, false);
            rView = (RecyclerView) rootView.findViewById(R.id.artist_recycler_view);
            mLayoutManager = new GridLayoutManager(getActivity(), 2);
            rView.setLayoutManager(mLayoutManager);
            rView.addItemDecoration(new GridSpacingItemDecoration(2, 10, true));
            rView.setItemAnimator(new DefaultItemAnimator());

            ArtistAdapter albumAdpt = new ArtistAdapter(getActivity(), artists);
            rView.setAdapter(albumAdpt);

            return rootView;
        }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
    }
