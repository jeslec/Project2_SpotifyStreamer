package com.lecomte.jessy.spotifystreamerstage1v3.other.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.MediaController;

/**
 * Created by Jessy on 2015-07-20.
 */
public class MusicControler extends MediaController {

    public MusicControler(Context context) {
        super(context);
    }

    public MusicControler(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MusicControler(Context context, boolean useFastForward) {
        super(context, useFastForward);
    }

    @Override
    public void hide() {
        // Don't hide controls after 3 seconds
        //super.hide();
    }
}
