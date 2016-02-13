# TABLE OF CONTENT

[APP DEMO](#app-demo)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[On a tablet](#tablet-demo)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[On a phone](#phone-demo)<br>
[DESCRIPTION](#description)<br>
[MY ROLE](#my-role)<br>
[EVALUATION CRITERIA](#evaluation-criteria)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[User Interface - Layout](#user-interface-layout-criteria)<br>

## APP DEMO <a name="app-demo"></a>

### On a tablet <a name="tablet-demo"></a>
![](../../../Screenshots/blob/master/spotify-streamer-anim1.gif)

### On a phone <a name="phone-demo"></a>
![](../../../Screenshots/blob/master/spotify-streamer-anim2.gif)

# DESCRIPTION <a name="description"></a>
This project is based on the highly popular music streaming app Spotify and was one of the assignments as part of the Android Nanodegree certification. Users can search for artists, see their top tracks, listen to streamed music samples and share the currently playing track. The fragments-based layout adapts to the device (phone, tablet) the app is running on to offer the best UI experience.

# MY ROLE <a name="my-role"></a>
I have built this app from the ground up and successfully implemented all required features and met all criteria. 

# EVALUATION CRITERIA <a name="evaluation-criteria"></a>

## User Interface - Layout <a name="user-interface-layout-criteria"></a>
* [Phone] UI contains a screen for searching for an artist and displaying a list of artist results.
Individual artist result layout contains - Artist Thumbnail , Artist name.
* [Phone] UI contains a screen for displaying the top tracks for a selected artist.
Individual track layout contains - Album art thumbnail, track name , album name.
* [Phone] UI contains a screen that represents the player. It contains track info (e.g., track duration, elapsed time, artist name, album name, album artwork, and track name). It also contains playback controls (e.g., play/pause/previous track/next track buttons and scrub bar) for the currently selected track.
* Tablet UI uses a Master-Detail layout implemented using fragments. The left fragment is for searching artists and the right fragment is for displaying top tracks of a selected artist. The Now Playing controls are displayed in a DialogFragment.

