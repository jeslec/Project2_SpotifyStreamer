package com.lecomte.jessy.spotifystreamerstage1v3.other;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.NowPlayingActivity;
import com.squareup.picasso.Picasso;

/**
 * Created by Jessy on 2015-07-24.
 */
public class AudioPlayerService extends Service {

    private static final String TAG = "AudioPlayerService";
    private WindowManager mWindowManager;
    private ImageView mChatHead;
    private View mOverlayView;
    private BroadcastReceiver mReceiver;

    // Responses this service will send to the client
    public static final String RESPONSE_TRACK_PREPARED =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.response.TRACK_PREPARED";

    // Data this service will send to the client in his response
    public static final String EXTRA_TRACK_DURATION =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.extra.TRACK_DURATION";

    public static final String ACTION_START_FOREGROUND =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.START_FOREGROUND";
    public static final String ACTION_STOP_FOREGROUND =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.STOP_FOREGROUND";

    private static final String ACTION_PLAY_NEXT_TRACK =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.PLAY_NEXT_TRACK";

    private static final String ACTION_PLAY_PREVIOUS_TRACK =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.PLAY_PREV_TRACK";

    private static final String ACTION_PAUSE =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.PAUSE";

    private static final String ACTION_RESUME =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.RESUME";

    private static final int NOTIFICATION_ID_AUDIO_PLAYER_SERVICE = 1000;

    private LocalBinder mLocalBinder = new LocalBinder();
    private AudioPlayer mAudioPlayer = new AudioPlayer();

    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    public AudioPlayer getPlayer() {
        return mAudioPlayer;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public AudioPlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return AudioPlayerService.this;
        }
    }

    // Used for service-to-client communication
    // Get idea from book: Android Programming - Pushing the Limits, p.125-129
    public void setCallback(AudioPlayer.Callback callback) {
        if (mAudioPlayer != null) {
            mAudioPlayer.setCallback(callback);
        }
    }

    private Notification buildCustomNotification() {

        // Get currently playing track info (or last played)
        TrackInfo track = mAudioPlayer.getTrackInfo();

        RemoteViews notificationRemoteView = new RemoteViews(getPackageName(),
                R.layout.notification_player);

        Intent notificationIntent = new Intent(this, NowPlayingActivity.class);
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        //--------------------------------------------------------------------------
        // Intent for the activity to open when user selects the notification
        Intent nowPlayingIntent = new Intent(this, NowPlayingActivity.class);
        nowPlayingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Use TaskStackBuilder to build the back stack and get the PendingIntent
        PendingIntent pendingIntent =
                TaskStackBuilder.create(this)
                        // add all of DetailsActivity's parents to the stack,
                        // followed by DetailsActivity itself
                        .addParentStack(NowPlayingActivity.class)
                        .addNextIntent(nowPlayingIntent)
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        /*NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(pendingIntent);*/

        //-----------------------------------------------------------------

        // Previous track button intent
        Intent previousIntent = new Intent(this, AudioPlayerService.class);
        previousIntent.setAction(AudioPlayerService.ACTION_PLAY_PREVIOUS_TRACK);
        PendingIntent prevPendingIntent = PendingIntent.getService(this, 0, previousIntent, 0);

        // Pause track button intent
        Intent pauseIntent = new Intent(this, AudioPlayerService.class);
        pauseIntent.setAction(AudioPlayerService.ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

        // Resume track button intent
        Intent resumeIntent = new Intent(this, AudioPlayerService.class);
        resumeIntent.setAction(AudioPlayerService.ACTION_RESUME);
        PendingIntent resumePendingIntent = PendingIntent.getService(this, 0, resumeIntent, 0);

        // Next track button intent
        Intent nextIntent = new Intent(this, AudioPlayerService.class);
        nextIntent.setAction(AudioPlayerService.ACTION_PLAY_NEXT_TRACK);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        // Set onClick events for media control buttons: each button calls a pending intent
        notificationRemoteView.setOnClickPendingIntent(R.id.notification_buttonPrev,
                prevPendingIntent);
        notificationRemoteView.setOnClickPendingIntent(R.id.notification_buttonNext,
                nextPendingIntent);

        // Put "pause" button icon and set pending intent to call when button is pressed
        if (getPlayer().isPlaying()) {
            notificationRemoteView.setInt(R.id.notification_buttonPlay, "setBackgroundResource",
                    android.R.drawable.ic_media_pause);
            notificationRemoteView.setOnClickPendingIntent(R.id.notification_buttonPlay,
                    pausePendingIntent);

        } else {
            // Set RemoteView widget background
            // http://stackoverflow.com/questions/6201410/how-to-change-widget-layout-background-programatically#14669011
            notificationRemoteView.setInt(R.id.notification_buttonPlay, "setBackgroundResource",
                    android.R.drawable.ic_media_play);
            // Set an onClick event for a widget located in a remoteView
            // http://stackoverflow.com/questions/22585696/android-notification-with-remoteviews-having-activity-associated-with-remotevi#22585875
            notificationRemoteView.setOnClickPendingIntent(R.id.notification_buttonPlay,
                    resumePendingIntent);
        }

        // Set notification texts
        notificationRemoteView.setTextViewText(R.id.notification_textTrack, track.getTrackName());
        notificationRemoteView.setTextViewText(R.id.notification_textArtist, track.getArtistName());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContent(notificationRemoteView)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_audio_player)
                .setContentTitle(track.getTrackName())
                .setContentText(track.getArtistName())
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true); // user cannot remove notification from the notification drawer

        Notification notification = builder.build();

        // Load image asynchronously for this notification (see lines 51-56 on page of link)
        // https://github.com/square/picasso/blob/master/picasso-sample/src/main/java/com/example/picasso/PicassoSampleAdapter.java
        Picasso.with(this).load(track.getAlbumSmallImageUrl())
                .resizeDimen(R.dimen.notification_icon_width_height,
                        R.dimen.notification_icon_width_height)
                .into(notificationRemoteView, R.id.notification_imageAlbum,
                        NOTIFICATION_ID_AUDIO_PLAYER_SERVICE, notification);

        return notification;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.log(TAG, "onStartCommand()");
        int returnCode = START_STICKY;

        if (intent == null || intent.getAction() == null) {
            return returnCode;
        }

        String action = intent.getAction();

        if (action.equals(ACTION_START_FOREGROUND)) {
            Utils.log(TAG, "onStartCommand() - Action: ACTION_START_FOREGROUND");
            startForeground(NOTIFICATION_ID_AUDIO_PLAYER_SERVICE, buildCustomNotification());
        }

        else if (action.equals(ACTION_STOP_FOREGROUND)) {
            Utils.log(TAG, "onStartCommand() - Action: ACTION_STOP_FOREGROUND");
            stopForeground(true);
        }

        else if (action.equals(ACTION_PLAY_PREVIOUS_TRACK)) {
            Utils.log(TAG, "onStartCommand() - Action: ACTION_PLAY_PREVIOUS_TRACK");
            mAudioPlayer.playPrevious();
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_ID_AUDIO_PLAYER_SERVICE, buildCustomNotification());
        }

        else if (action.equals(ACTION_PAUSE)) {
            Utils.log(TAG, "onStartCommand() - Action: ACTION_PAUSE");
            mAudioPlayer.pause();
            // TODO: change icon to play
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_ID_AUDIO_PLAYER_SERVICE, buildCustomNotification());
        }

        else if (action.equals(ACTION_RESUME)) {
            Utils.log(TAG, "onStartCommand() - Action: ACTION_RESUME");
            mAudioPlayer.resume();
            // TODO: change icon to pause
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_ID_AUDIO_PLAYER_SERVICE, buildCustomNotification());
        }

        else if (action.equals(ACTION_PLAY_NEXT_TRACK)) {
            Utils.log(TAG, "onStartCommand() - Action: ACTION_PLAY_NEXT_TRACK");
            mAudioPlayer.playNext();
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_ID_AUDIO_PLAYER_SERVICE, buildCustomNotification());
        }
        return returnCode;
    }
    
    public class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Utils.log(TAG, "ScreenReceiver::onReceive()");
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // do whatever you need to do here
                //wasScreenOn = false;
                Utils.log(TAG, "Screen is OFF");
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                // and do whatever you need to do here
                //wasScreenOn = true;
                Utils.log(TAG, "Screen is ON");
                //addViewToLockScreen2();
            }else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
                Utils.log(TAG, "User present");
                //removeViewFromLockScreen2();
            }
        }
    }

    private void removeViewFromLockScreen() {
        if (mChatHead != null) {
            mWindowManager.removeView(mChatHead);
        }
    }

    private void removeViewFromLockScreen2() {
        if (mOverlayView != null) {
            mWindowManager.removeView(mOverlayView);
        }
    }

    private void addViewToLockScreen2() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mOverlayView = inflater.inflate(R.layout.overlay_lockscreen, null);

        ImageView imageViewOverlay = (ImageView)mOverlayView.findViewById(R.id.overlay_imageView);
        imageViewOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.log(TAG, "onClick() - Overlay imageView");
            }
        });

        if (mOverlayView == null) {
            Utils.log(TAG, "mOverlayView is null!");
            return;
        }

       /* mChatHead.setClickable(true);
        mChatHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.log(TAG, "Android head touched!");
            }
        });*/

        /*WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, //TYPE_SYSTEM_ALERT, //TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);*/

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 800;

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mOverlayView, params);
    }

    private void addViewToLockScreen() {
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // TEST: use a theme that does not set window as floating
        //setTheme(R.style.ShowOnTopOfLockScreen);

        // TEST
        /*Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);*/


        mChatHead = new ImageView(this);
        mChatHead.setImageResource(R.drawable.android_head);

        mChatHead.setClickable(true);
        mChatHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.log(TAG, "Android head touched!");
            }
        });

        //mChatHead.setLayoutParams(new ViewGroup.LayoutParams());

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE, //TYPE_SYSTEM_OVERLAY, //TYPE_SYSTEM_ALERT, //TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 800;

        mWindowManager.addView(mChatHead, params);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.log(TAG, "onCreate()");

        // ******** TEST *************
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        final BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
        //***********************************

        //addViewToLockScreen();

        /*mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        mChatHead = new ImageView(this);
        mChatHead.setImageResource(R.drawable.android_head);

        mChatHead.setClickable(true);
        mChatHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.log(TAG, "Android head touched!");
            }
        });

        //mChatHead.setLayoutParams(new ViewGroup.LayoutParams());

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 800;

        mWindowManager.addView(mChatHead, params);*/
    }

    @Override
    public void onDestroy() {
        Utils.log(TAG, "onDestroy() - Calling AudioPlayer.stop()...");

        // TEST: stop playing track and destroy media player when service gets killed
        mAudioPlayer.stop();

        if (mChatHead != null) {
            mWindowManager.removeView(mChatHead);
        }

        super.onDestroy();
    }
}

/*
WindowManager.LayoutParams params = new WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT //TYPE_SYSTEM_OVERLAY  TYPE_SYSTEM_ALERT TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        //WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        //WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT);*/
