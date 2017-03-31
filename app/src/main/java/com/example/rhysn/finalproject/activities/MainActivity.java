package com.example.rhysn.finalproject.activities;

import android.content.Intent;
import android.os.Bundle;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.rhysn.finalproject.BaseActivity;
import com.example.rhysn.finalproject.R;
import com.example.rhysn.finalproject.fragments.AllAlbumsFragment;
import com.example.rhysn.finalproject.fragments.AllArtistsFragment;
import com.example.rhysn.finalproject.fragments.AllSongsFragment;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {


    private ViewPager viewPager;
    TabLayout tabLayout;
    CastContext castContext;
    //MenuItem mediaRouteMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        castContext = CastContext.getSharedInstance(this);

        viewPager = (ViewPager) findViewById(R.id.pager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//
//        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
//                menu,
//                R.id.media_route_menu_item);
//        return true;
//    }

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
}
