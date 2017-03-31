package com.example.rhysn.finalproject.activities;

import android.view.Menu;

import com.example.rhysn.finalproject.R;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity;

public class ExpandedControlsActivity extends ExpandedControllerActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_cast_playing, menu);
        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item_expanded);
        return true;
    }
}
