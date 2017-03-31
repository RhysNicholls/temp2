package com.example.rhysn.finalproject.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.rhysn.finalproject.BaseActivity;
import com.example.rhysn.finalproject.R;
import com.example.rhysn.finalproject.fragments.UpnpDeviceFragment;
import com.example.rhysn.finalproject.fragments.UpnpFileBrowserFragment;

import org.fourthline.cling.android.AndroidUpnpServiceImpl;

public class UpnpBrowserActivity extends AppCompatActivity implements UpnpFileBrowserFragment.OnFragmentInteractionListener,
        UpnpDeviceFragment.OnFragmentInteractionListener {

    private UpnpDeviceFragment upnpDeviceFragment;
    private UpnpFileBrowserFragment upnpFileBrowserFragment = null;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upnp_browser);
//        if (!getApplicationContext().bindService(
//                new Intent(this, AndroidUpnpServiceImpl.class),
//                serviceConnection,
//                Context.BIND_AUTO_CREATE)) {
//            throw new IllegalStateException("Unable to bind AndroidUpnpServiceImpl");
//        }

        upnpDeviceFragment = UpnpDeviceFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, upnpDeviceFragment)
                .commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
