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
public class AudioPlayer {
    private final String TAG = getClass().getSimpleName();
    private MediaPlayer mPlayer;
    private int mTrackDuration;
    private Set<Callback> mListeners = new HashSet<Callback>();
    private ArrayList<TrackInfo> mPlaylist = new ArrayList<TrackInfo>();
    private SafeIndex mPlaylistIndex;
    private TrackInfo mTrack;
    // Don't put this in initializePlayer() or else observables will get deleted
    private ObservablePlayPauseState mPlayPauseState = new ObservablePlayPauseState();

    public AudioPlayer() {
        initializePlayer();
    }

    private void initializePlayer() {
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                notifyOnTrackCompleted();
            }
        });

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mTrackDuration = mp.getDuration();
                Utils.log(TAG, "onPrepared() - Track duration: " + mTrackDuration);
                mp.start();

                // Tell listening UIs the play/pause state is now: play
                mPlayPauseState.setIsPlayState(true);

                notifyOnReceiveTrackDuration(mTrackDuration);
            }
        });

        Utils.log(TAG, "initializePlayer() - Play/pause observables count: " + mPlayPauseState.countObservers());
    }

    public void addPlayPauseStateObserver(Observer observer) {
        mPlayPauseState.addObserver(observer);
        Utils.log(TAG, "addPlayPauseStateObserver() - Added: "
                + observer.getClass().getSimpleName()
                + " [Size: " + mPlayPauseState.countObservers() + "]");
    }

    public void removePlayPauseStateObserver(Observer observer) {
        mPlayPauseState.deleteObserver(observer);
        Utils.log(TAG, "removePlayPauseStateObserver() - Removed: "
                + observer.getClass().getSimpleName()
                + " [Size: " + mPlayPauseState.countObservers() + "]");
    }

    public boolean isPlayState() {
        return mPlayPauseState.isPlayState();
    }

    public int getCurrentPosition() {
        //Utils.log(TAG, "Track position: " + mPlayer.getCurrentPosition());
        return mPlayer.getCurrentPosition();
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

    public void stop() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
            Utils.log(TAG, R.string.AudioPlayer_debug_playerStopped);
        }
    }

    // This should only be called when player is in "Paused" state
    public void resume() {
        if (mPlayer != null) {
            notifyOnReceiveTrackDuration(mTrackDuration);
            mPlayer.start();

            // Tell listening UIs the play/pause state is now: play
            mPlayPauseState.setIsPlayState(true);
        }
    }

    private void notifyOnTrackCompleted() {
        Iterator iterator = mListeners.iterator();
        while (iterator.hasNext()) {
            Callback listener = (Callback)iterator.next();
            if (listener != null) {
                listener.onTrackCompleted();
            }
        }
    }

    // Notify all listeners the track duration is available
    private void notifyOnReceiveTrackDuration(int duration) {
        Iterator iterator = mListeners.iterator();
        while (iterator.hasNext()) {
            Callback listener = (Callback)iterator.next();
            if (listener != null) {
                listener.onReceiveTrackDuration(duration);
            }
        }
    }

    // After this, player will be in "Paused" state
    // Only valid actions after are: start() or stop()
    public void pause() {
        if (mPlayer != null) {
            mPlayer.pause();

            // Tell listening UIs the play/pause state is now: play
            mPlayPauseState.setIsPlayState(false);
        }
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        Utils.log(TAG, "isPlaying() - mPlayer is null!");
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
            Utils.log(TAG, "getArtist() - Playlist is null or empty!");
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

    public void removeListener(Callback listener) {
        mListeners.remove(listener);
    }

    public void deletePlayPauseStateObservers() {
        mPlayPauseState.deleteObservers();
    }

    public interface Callback {
        void onTrackCompleted();
        void onReceiveTrackDuration(int duration);
    }

    public void addListener(Callback listener) {
        if (listener != null) {
            mListeners.add(listener);
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
            Utils.log(TAG, "play(): playlist is empty! - Call setPlaylist() before play()");
        }
        mPlaylistIndex = new SafeIndex(trackIndex, mPlaylist.size() - 1);
        TrackInfo track = mPlaylist.get(mPlaylistIndex.get());
        play(track.getTrackPreviewUrl());

        //Utils.log(TAG, "play() - STARTED playing track: " + track.getTrackPreviewUrl());
    }

    // Playlist index initialized in play()
    // A call to play() must have been made before calling playNext()
    public void playNext() {
        if (mPlaylistIndex == null) {
            Utils.log(TAG, "playNext(): playlist index not initialized! - Call play()");
            return;
        }
        mTrack = mPlaylist.get(mPlaylistIndex.getNext());
        play(mTrack.getTrackPreviewUrl());
    }

    // Playlist index initialized in play()
    // A call to play() must have been made before calling playPrevious()
    public void playPrevious() {
        if (mPlaylistIndex == null) {
            Utils.log(TAG, "playPrevious(): playlist index not initialized! - Call play()");
            return;
        }
        mTrack = mPlaylist.get(mPlaylistIndex.getPrevious());
        play(mTrack.getTrackPreviewUrl());
    }

    public TrackInfo getTrackInfo() {
        if (mPlaylistIndex == null) {
            Utils.log(TAG, "getTrackInfo(): playlist index not initialized! - Call play()");
            return null;
        }
        return mPlaylist.get(mPlaylistIndex.get());
    }

    public int getPlaylistIndex() {
        if (mPlaylistIndex == null) {
            Utils.log(TAG, "getPlaylistIndex() - mPlaylistIndex is null! Index value returned: 0");
            return 0;
        }
        return mPlaylistIndex.get();
    }
}
