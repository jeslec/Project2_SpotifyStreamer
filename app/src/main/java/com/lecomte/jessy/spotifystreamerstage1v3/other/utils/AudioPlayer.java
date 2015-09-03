package com.lecomte.jessy.spotifystreamerstage1v3.other.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;

import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.observables.ObservablePlayPauseState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observer;
import java.util.Set;

/**
 * Created by Jessy on 2015-07-20.
 */
public class AudioPlayer implements MediaPlayer.OnErrorListener {

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Utils.log(TAG, "MediaPlayer error: " + what);
        return false;
    }

    public enum TrackPlayingState {
        TRACK_NOT_PLAYING   (false),
        TRACK_PLAYING       (true);

        private final boolean mState;

        private TrackPlayingState(boolean state) {
            mState = (boolean)state;
        }
    }

    //**********************************************************************************************
    // CONSTANTS
    //**********************************************************************************************

    private final String TAG = getClass().getSimpleName();

    //**********************************************************************************************
    // VARIABLES
    //**********************************************************************************************

    //**** [Primitive] ****
    private int mTrackDuration;

    //**** [Listeners] ****
    // TODO: Change this to Obversables like I did for the play/pause button
    private Set<Callback> mOnTrackCompletedListeners = new HashSet<Callback>();

    //**** [Other] ****
    private MediaPlayer mPlayer;
    private ArrayList<TrackInfo> mPlaylist = new ArrayList<TrackInfo>();
    private SafeIndex mPlaylistIndex;
    private TrackInfo mTrack;
    // Don't put this in initializePlayer() or else observables will get deleted
    private ObservablePlayPauseState mTrackPlayingState = new ObservablePlayPauseState();


    //**********************************************************************************************
    // CONSTRUCTORS
    //**********************************************************************************************

    public AudioPlayer() {
        initializePlayer();
    }

    //**********************************************************************************************
    // PRIVATE METHODS
    //**********************************************************************************************

    private void initializePlayer() {
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Track is stopped so button must be set to play for all listening UIs
                mTrackPlayingState.setState(TrackPlayingState.TRACK_NOT_PLAYING);
                notifyOnTrackCompleted();
            }
        });

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mTrackDuration = mp.getDuration();
                //Utils.log(TAG, "onPrepared() - Track duration: " + mTrackDuration);
                mp.start();
                mTrackPlayingState.setState(TrackPlayingState.TRACK_PLAYING);
                notifyOnReceiveTrackDuration(mTrackDuration);
            }
        });

        //Utils.log(TAG, "initializePlayer() - Play/pause observables count: " + mTrackPlayingState.countObservers());
    }

    private void play(String audioFileUrl) {

        stop();
        initializePlayer();

        try {
            mPlayer.setDataSource(audioFileUrl);
            mPlayer.prepareAsync(); // might take long! (for buffering, etc)
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notifyOnTrackCompleted() {
        Iterator iterator = mOnTrackCompletedListeners.iterator();
        while (iterator.hasNext()) {
            Callback listener = (Callback)iterator.next();
            if (listener != null) {
                //Utils.log(TAG, "notifyOnTrackCompleted() - Notifying listener: "
                        //+ listener.getClass().getSimpleName());
                listener.onTrackCompleted();
            }
        }
    }

    // Notify all listeners the track duration is available
    private void notifyOnReceiveTrackDuration(int duration) {
        Iterator iterator = mOnTrackCompletedListeners.iterator();
        while (iterator.hasNext()) {
            Callback listener = (Callback)iterator.next();
            if (listener != null) {
                //Utils.log(TAG, "notifyOnReceiveTrackDuration() - Notifying listener: "
                /*        + listener.getClass().getSimpleName());*/
                listener.onReceiveTrackDuration(duration);
            }
        }
    }

    //**********************************************************************************************
    // PUBLIC METHODS
    //**********************************************************************************************

    public void addPlayPauseStateObserver(Observer observer) {
        mTrackPlayingState.addObserver(observer);
        //Utils.log(TAG, "addPlayPauseStateObserver() - Added: "
                /*+ observer.getClass().getSimpleName()
                + " [Size: " + mTrackPlayingState.countObservers() + "]");*/
    }

    public void removePlayPauseStateObserver(Observer observer) {
        mTrackPlayingState.deleteObserver(observer);
        //Utils.log(TAG, "removePlayPauseStateObserver() - Removed: "
                /*+ observer.getClass().getSimpleName()
                + " [Size: " + mTrackPlayingState.countObservers() + "]");*/
    }

    public boolean isPlayState() {
        return mTrackPlayingState.isTrackPlaying();
    }

    public int getCurrentPosition() {
        ////Utils.log(TAG, "Track position: " + mPlayer.getCurrentPosition());
        return mPlayer.getCurrentPosition();
    }

    public void stop() {
        if (mPlayer != null) {

            if (mPlayer.isPlaying()) {
                mPlayer.stop();
                mTrackPlayingState.setState(TrackPlayingState.TRACK_NOT_PLAYING);
            }

            // Do a reset() before release() to avoid getting "mediaplayer went away with unhandled
            // events" message in Logcat as suggested by:
            // http://stackoverflow.com/questions/9609479/android-mediaplayer-went-away-with-unhandled-events
            mPlayer.reset();

            mPlayer.release();
            mPlayer = null;
            //Utils.log(TAG, R.string.AudioPlayer_debug_playerStopped);
        }
    }

    // This should only be called when player is in "Paused" state
    public void resume() {
        //Utils.log(TAG, "resume()");
        if (mPlayer != null) {
            // Important: player must be started before player state is sent to listeners
            // so make sure to keep order of method calling as it currently is
            mPlayer.start();
            mTrackPlayingState.setState(TrackPlayingState.TRACK_PLAYING);
            notifyOnReceiveTrackDuration(mTrackDuration);
        }
    }

    // After this, player will be in "Paused" state
    // Only valid actions after are: start() or stop()
    public void pause() {
        //Utils.log(TAG, "pause()");
        if (mPlayer != null) {
            mPlayer.pause();

            // Tell listening UIs the play/pause state is now: play
            mTrackPlayingState.setState(TrackPlayingState.TRACK_NOT_PLAYING);
        }
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        //Utils.log(TAG, "isPlaying() - mPlayer is null!");
        return false;
    }

    public void seekTo(int progress) {
        if (mPlayer != null) {
            mPlayer.seekTo(progress);
        }
    }

    // For now, we use the name of the artist of the first track in the playlist as an Id
    public String getPlaylistId() {
        if (mPlaylist == null || mPlaylist.isEmpty()) {
            //Utils.log(TAG, "getArtist() - Playlist is null or empty!");
            return "";
        }
        return mPlaylist.get(0).getArtistName();
    }

    public ArrayList<TrackInfo> getPlaylist() {
        return mPlaylist;
    }

    public int getTrackDuration() {
        return mTrackDuration;
    }

    public void deletePlayPauseStateObservers() {
        mTrackPlayingState.deleteObservers();
    }

    public void togglePlayPauseState() {
        if (isPlaying()) {
            pause();
        }
        else {
            resume();
        }
    }

    public interface Callback {
        void onTrackCompleted();
        void onReceiveTrackDuration(long duration);
    }

    public void addListener(Callback listener) {
        if (listener != null) {
            mOnTrackCompletedListeners.add(listener);
            //Utils.log(TAG, "addListener() - Added listener to onTrackCompleted event: "
                    //+ listener.getClass().getSimpleName());
        }
    }

    public void removeListener(Callback listener) {
        if (listener != null) {
            mOnTrackCompletedListeners.remove(listener);
            //Utils.log(TAG, "removeListener() - Removed listener to onTrackCompleted event: "
                    //+ listener.getClass().getSimpleName());
        }
    }

    public void setPlaylist(ArrayList<TrackInfo> trackList) {
        mPlaylist = trackList;
    }

    public boolean isPlaylistEmpty() {
        if (mPlaylist == null || mPlaylist.isEmpty()) {
            return true;
        }
        return false;
    }

    // Before calling play(), setPlaylist must have been called
    // Note: play() must be called before any call to playNext()/playPrevious is made
    // because the playList index is initialized in play() and the index is used by these 2 methods
    public void play(int trackIndex) {
        if (mPlaylist.isEmpty()) {
            //Utils.log(TAG, "play(): playlist is empty! - Call setPlaylist() before play()");
        }
        mPlaylistIndex = new SafeIndex(trackIndex, mPlaylist.size() - 1);
        TrackInfo track = mPlaylist.get(mPlaylistIndex.get());
        play(track.getTrackPreviewUrl());

        ////Utils.log(TAG, "play() - STARTED playing track: " + track.getTrackPreviewUrl());
    }

    // Playlist index initialized in play()
    // A call to play() must have been made before calling playNext()
    public void playNext() {
        if (mPlaylistIndex == null) {
            //Utils.log(TAG, "playNext(): playlist index not initialized! - Call play()");
            return;
        }
        mTrack = mPlaylist.get(mPlaylistIndex.getNext());
        play(mTrack.getTrackPreviewUrl());
    }

    // Playlist index initialized in play()
    // A call to play() must have been made before calling playPrevious()
    public void playPrevious() {
        if (mPlaylistIndex == null) {
            //Utils.log(TAG, "playPrevious(): playlist index not initialized! - Call play()");
            return;
        }
        mTrack = mPlaylist.get(mPlaylistIndex.getPrevious());
        play(mTrack.getTrackPreviewUrl());
    }

    public TrackInfo getTrackInfo() {
        if (mPlaylistIndex == null) {
            //Utils.log(TAG, "getTrackInfo(): playlist index not initialized! - Call play()");
            return null;
        }

        // Overwrite the real track length with track sample length
        TrackInfo track = mPlaylist.get(mPlaylistIndex.get());
        track.setTrackDuration(mTrackDuration);
        return track;
    }

    public int getPlaylistIndex() {
        if (mPlaylistIndex == null) {
            //Utils.log(TAG, "getPlaylistIndex() - mPlaylistIndex is null! Index value returned: 0");
            return 0;
        }
        return mPlaylistIndex.get();
    }
}
