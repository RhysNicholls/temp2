package com.example.rhysn.finalproject.utils;

import android.content.Context;

import com.example.rhysn.finalproject.R;
import com.example.rhysn.finalproject.activities.NowPlayingActivity;

import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;
import com.google.android.gms.cast.framework.media.MediaIntentReceiver;
import com.google.android.gms.cast.framework.media.NotificationOptions;

import java.util.ArrayList;
import java.util.List;


public class CastOptionsProvider implements OptionsProvider {

    @Override
    public CastOptions getCastOptions(Context context) {
        List<String> buttonActions = new ArrayList<>();
        buttonActions.add(MediaIntentReceiver.ACTION_SKIP_PREV);
        buttonActions.add(MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK);
        buttonActions.add(MediaIntentReceiver.ACTION_SKIP_NEXT);
        buttonActions.add(MediaIntentReceiver.ACTION_STOP_CASTING);
        int[] compatButtonActionsIndicies = new int[]{1, 3};

        NotificationOptions notificationOptions = new NotificationOptions.Builder()
                .setActions(buttonActions, compatButtonActionsIndicies)
                .setTargetActivityClassName(NowPlayingActivity.class.getName())
                .build();
        CastMediaOptions.Builder builder = new CastMediaOptions.Builder();
       builder.setNotificationOptions(notificationOptions);
        builder.setExpandedControllerActivityClassName(NowPlayingActivity.class.getName());
        CastMediaOptions mediaOptions = builder.build();
        System.out.println("");

        return new CastOptions.Builder()
                .setReceiverApplicationId(context.getString(R.string.cast_app_id))
                .setCastMediaOptions(mediaOptions)
                .build();
    }


    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }
}
