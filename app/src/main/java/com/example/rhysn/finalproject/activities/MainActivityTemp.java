package com.example.rhysn.finalproject.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.example.rhysn.finalproject.BaseActivity;
import com.example.rhysn.finalproject.R;
import com.example.rhysn.finalproject.fragments.AllAlbumsFragment;
import com.example.rhysn.finalproject.fragments.AllArtistsFragment;
import com.example.rhysn.finalproject.fragments.AllSongsFragment;
import com.example.rhysn.finalproject.fragments.MiniPlayerFragment;
import com.example.rhysn.finalproject.services.MediaPlayerServicetemps;

import java.util.ArrayList;
import java.util.List;

public class MainActivityTemp extends BaseActivity {

    public static final String Broadcast_ADD_END = "com.example.rhysn.finalproject.AddToEnd";
    public static final String Broadcast_ADD_START = "com.example.rhysn.finalproject.AddToStart";


    private ViewPager viewPager;
    TabLayout tabLayout;
    ImageView btnPlaying;
    private boolean serviceBound;
    private MediaPlayerServicetemps player;
    private MiniPlayerFragment miniplayerFragment;

    private final ThreadLocal<ServiceConnection> serviceConnection = new ThreadLocal<ServiceConnection>() {
        @Override
        protected ServiceConnection initialValue() {
            return new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    // We've bound to LocalService, cast the IBinder and get LocalService instance
                    MediaPlayerServicetemps.LocalBinder binder = (MediaPlayerServicetemps.LocalBinder) service;
                    player = binder.getService();
                    serviceBound = true;
                }


                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Intent playerIntent = new Intent(this, MediaPlayerServicetemps.class);
        startService(playerIntent);
        bindService(playerIntent, serviceConnection.get(), Context.BIND_AUTO_CREATE);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.pager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void setupViewPager(ViewPager v) {
        ViewPagerAdapter a = new ViewPagerAdapter(getSupportFragmentManager());
        a.addFragment(new AllArtistsFragment(), "Artists");
        a.addFragment(new AllAlbumsFragment(), "Albums");
        a.addFragment(new AllSongsFragment(), "Songs");
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection.get());
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        miniplayerFragment = (MiniPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback_controls);
        miniplayerVisibility();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void miniplayerVisibility(){
        if(serviceBound){
            if(player.getState()) {
                getSupportFragmentManager().beginTransaction().show(miniplayerFragment).commit();
            }else{
                getSupportFragmentManager().beginTransaction().hide(miniplayerFragment).commit();
            }
        }else{
            getSupportFragmentManager().beginTransaction().hide(miniplayerFragment).commit();
        }

    }
}
