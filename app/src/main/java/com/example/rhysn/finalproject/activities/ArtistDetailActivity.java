package com.example.rhysn.finalproject.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import com.example.rhysn.finalproject.BaseActivity;
import com.example.rhysn.finalproject.R;
import com.example.rhysn.finalproject.fragments.ArtistAlbumsFragment;
import com.example.rhysn.finalproject.fragments.ArtistSongsFragment;

import java.util.ArrayList;
import java.util.List;

public class ArtistDetailActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageView artistArt;
    private TextView artistTitle;
    private String artistArtUrl;
    private String artistName;
    private ExpandableTextView artistBio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);

       // artistID = getIntent().getExtras().getLong("artistID");
        artistName = getIntent().getExtras().getString("artistName");
        artistArtUrl = getIntent().getStringExtra("artistArtUrl");

        viewPager = (ViewPager) findViewById(R.id.artist_pager);
        artistBio = (ExpandableTextView) findViewById(R.id.expand_text_view);
        tabLayout = (TabLayout) findViewById(R.id.artist_tab_layout);
        artistArt = (ImageView) findViewById(R.id.detail_artist_art);
        artistTitle =(TextView) findViewById(R.id.artist_title);

        tabLayout.setupWithViewPager(viewPager);
        setupViewPager(viewPager);

        setArtistDetails();
        setupToolBar();

    }

    private void setupViewPager(ViewPager v) {
        ViewPagerAdapter a = new ViewPagerAdapter(getSupportFragmentManager());
        a.addFragment(new ArtistAlbumsFragment(), "Albums");
        a.addFragment(new ArtistSongsFragment(), "Songs");
        viewPager.setAdapter(a);
    }
    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment f, String t) {
            mFragmentList.add(f);
            mFragmentTitleList.add(t);
        }

        @Override
        public CharSequence getPageTitle(int p) {
            return mFragmentTitleList.get(p);
        }
    }

    public void setArtistDetails(){
        Glide.with(this)
                .load(artistArtUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(artistArt);

        artistBio.setText(getIntent().getStringExtra("artistBio"));

    }

    public void setupToolBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        artistTitle.setText(artistName);
    }
}
