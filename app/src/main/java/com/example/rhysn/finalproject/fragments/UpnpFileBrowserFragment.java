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
import android.widget.ListView;

import com.example.rhysn.finalproject.adapters.UpnpFileAdapter;
import com.example.rhysn.finalproject.utils.ContainerWrapper;
import com.example.rhysn.finalproject.R;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class UpnpFileBrowserFragment extends Fragment implements AdapterView.OnItemClickListener, ServiceConnection {

    private static final String ARG_PARAM_UDN = "com.example.rhys.finalprojectupnp.upnp.ARG_PRAM_UDN";
    private static final String TAG = "BrowserFragment";

    private String deviceUdn;
    private UpnpFileAdapter fileAdapter;
    private AndroidUpnpService upnpService;
    private OnFragmentInteractionListener mListener;

    private Map<String,ContainerWrapper> containerWrapperMap;
    private ContainerWrapper currentContainer;
    private Service contentDirectoryService;

    public UpnpFileBrowserFragment() {
        // Required empty public constructor
    }

    public static UpnpFileBrowserFragment newInstance(String deviceUdn) {
        UpnpFileBrowserFragment fragment = new UpnpFileBrowserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_UDN, deviceUdn);
        fragment.setArguments(args);
        return fragment;
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

        deviceUdn = getArguments().getString(ARG_PARAM_UDN);

        containerWrapperMap = new HashMap<>();
        containerWrapperMap.put(ContainerWrapper.ROOT_CONTAINER_ID, ContainerWrapper.ROOT_CONTAINER);
        currentContainer = ContainerWrapper.ROOT_CONTAINER;

        if (!getActivity().getApplicationContext().bindService(
                new Intent(getActivity(), AndroidUpnpServiceImpl.class),
                this,
                Context.BIND_AUTO_CREATE)) {
            throw new IllegalStateException("Unable to bind AndroidUpnpServiceImpl");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ListView view = (ListView) inflater.inflate(R.layout.fragment_browser, container, false);
        fileAdapter = new UpnpFileAdapter(this, view.getContext(), R.layout.fragment_browser_item);
        fileAdapter.add(UpnpFileAdapter.ListItem.PREVIOUS_CONTAINER_LIST_ITEM);
        view.setAdapter(fileAdapter);
        view.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        getActivity().getApplicationContext().unbindService(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UpnpFileAdapter.ListItem listItem = (UpnpFileAdapter.ListItem) parent.getItemAtPosition(position);
        if(listItem.isPreviousContainer()){

        }else if(listItem.hasContainer()){
            currentContainer = listItem.getContainerWrapper();
            ContainerBrowse containerBrowse = new ContainerBrowse(
                    contentDirectoryService, listItem.getContainerWrapper().getId(), BrowseFlag.DIRECT_CHILDREN);
            upnpService.getControlPoint().execute(containerBrowse);
        }else{
            List<Item> items = new ArrayList<>();
            items.add(listItem.getListItem());
            System.out.println();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        upnpService = (AndroidUpnpService) service;

        Device device = upnpService.getRegistry().getDevice(new UDN(deviceUdn), false);
        contentDirectoryService = device.findService(new UDAServiceType("ContentDirectory"));
        ContainerBrowse containerBrowse = new ContainerBrowse(
                contentDirectoryService, ContainerWrapper.ROOT_CONTAINER.getId(), BrowseFlag.DIRECT_CHILDREN);
        upnpService.getControlPoint().execute(containerBrowse);

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        upnpService = null;

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class ContainerBrowse extends Browse{
        public ContainerBrowse(Service service, String containerId, BrowseFlag flag) {
            super(service, containerId, flag);
        }

        @Override
        public void received(ActionInvocation actionInvocation, final DIDLContent didl) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fileAdapter.clear();
                    fileAdapter.add(UpnpFileAdapter.ListItem.PREVIOUS_CONTAINER_LIST_ITEM);
                    for (Container container : didl.getContainers()) {
                        UpnpFileAdapter.ListItem listItem = new UpnpFileAdapter.ListItem(
                                new ContainerWrapper(container));
                        fileAdapter.add(listItem);
                        if(!containerWrapperMap.containsKey(container.getId())){
                            containerWrapperMap.put(container.getId(), listItem.getContainerWrapper());
                        }
                        getItems(container.getId());


                    }
                    for (Item item: didl.getItems()){
                        fileAdapter.add(new UpnpFileAdapter.ListItem(item));

                    }
                }
            });
        }

        @Override
        public void updateStatus(Status status) {

        }

        @Override
        public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {

        }
    };

    public void getItems(final String containerID) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Browse items = new Browse(contentDirectoryService, containerID, BrowseFlag.DIRECT_CHILDREN) {
                    @Override
                    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {

                    }

                    @Override
                    public void received(ActionInvocation actionInvocation, final DIDLContent didl) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for(int i = 0; i < fileAdapter.getCount(); i++){
                                    UpnpFileAdapter.ListItem listItem = fileAdapter.getItem(i);
                                    if(listItem != null && listItem.hasContainer() && Objects.equals(listItem.getContainerWrapper().getId(), containerID)){
                                        listItem.setMediaItems(didl.getItems());
                                        fileAdapter.notifyDataSetChanged();
                                    }}}});
                    }

                    @Override
                    public void updateStatus(Status status) {

                    }
                };
                upnpService.getControlPoint().execute(items);
            }

        });


    }
}
