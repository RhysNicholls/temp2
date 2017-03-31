package com.example.rhysn.finalproject.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.rhysn.finalproject.R;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

public class UpnpDeviceFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ArrayAdapter<DeviceDisplay> listAdapter;

    private BrowseRegistryListener registryListener = new BrowseRegistryListener();

    private AndroidUpnpService upnpService;
    private Service contentDirectoryService;
    private OnFragmentInteractionListener mListener;

    private UpnpFileBrowserFragment fileBrowserFragment;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            upnpService = (AndroidUpnpService) service;
            listAdapter.clear();

            upnpService.getRegistry().addListener(registryListener);

            for (Device device : upnpService.getRegistry().getDevices()) {
                registryListener.deviceAdded(device);
            }

            upnpService.getControlPoint().search();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            upnpService = null;
        }
    };

    public UpnpDeviceFragment() {
        // Required empty public constructor
    }

    public static UpnpDeviceFragment newInstance() {

        return new UpnpDeviceFragment();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getApplicationContext().bindService(new Intent(getActivity().getApplicationContext(), AndroidUpnpServiceImpl.class), serviceConnection, Context.BIND_AUTO_CREATE);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ListView listView = (ListView) inflater.inflate(R.layout.fragment_upnp_device, container, false);
        listAdapter = new ArrayAdapter<>(listView.getContext(), R.layout.fragment_upnp_device_item);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);

        return listView;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
        getActivity().getApplicationContext().unbindService(serviceConnection);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DeviceDisplay deviceDisplay = (DeviceDisplay) parent.getItemAtPosition(position);

        fileBrowserFragment = UpnpFileBrowserFragment.newInstance(deviceDisplay.getDevice()
                .getIdentity().getUdn().getIdentifierString());

        getFragmentManager().beginTransaction().add(R.id.fragment_container, fileBrowserFragment).remove(this).commit();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    protected class BrowseRegistryListener extends DefaultRegistryListener {
        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }

        public void deviceAdded(final Device device) {
            if (device.findService(new UDAServiceType("ContentDirectory")) == null)
                return;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DeviceDisplay d = new DeviceDisplay(device);
                    int position = listAdapter.getPosition(d);
                    if (position > 0) {
                        listAdapter.remove(d);
                        listAdapter.insert(d, position);
                    } else {
                        listAdapter.add(d);
                    }
                    listAdapter.notifyDataSetChanged();

                }
            });
        }

        public void deviceRemoved(final Device device) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listAdapter.remove(new DeviceDisplay(device));
                }
            });
        }
    }

    protected class DeviceDisplay {

        Device device;

        public DeviceDisplay(Device device) {
            this.device = device;
        }

        public Device getDevice() {
            return device;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeviceDisplay that = (DeviceDisplay) o;
            return device.equals(that.device);
        }

        @Override
        public int hashCode() {
            return device.hashCode();
        }

        @Override
        public String toString() {
            String name =
                    getDevice().getDetails() != null && getDevice().getDetails().getFriendlyName() != null
                            ? getDevice().getDetails().getFriendlyName()
                            : getDevice().getDisplayString();
            // Display a little star while the device is being loaded (see performance optimization earlier)
            return device.isFullyHydrated() ? name : name + " *";
        }

    }
}
