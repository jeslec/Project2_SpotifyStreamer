# TABLE OF CONTENT

[APP DEMO](#app-demo)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[On a tablet](#tablet-demo)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[On a phone](#phone-demo)<br>
[DESCRIPTION](#description)<br>
[MY ROLE](#my-role)<br>
[EVALUATION CRITERIA](#evaluation-criteria)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[User Interface - Layout](#evaluation-criteria-1)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[User Interface - Function](#evaluation-criteria-2)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[Network API Implementation](#evaluation-criteria-3)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[Media Playback](#evaluation-criteria-4)<br>

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

## User Interface - Layout <a name="evaluation-criteria-1"></a>
* [Phone] UI contains a screen for searching for an artist and displaying a list of artist results.

* Individual artist result layout contains - Artist Thumbnail , Artist name.

* [Phone] UI contains a screen for displaying the top tracks for a selected artist.

* Individual track layout contains - Album art thumbnail, track name , album name.

* [Phone] UI contains a screen that represents the player. It contains track info (e.g., track duration, elapsed time, artist name, album name, album artwork, and track name). It also contains playback controls (e.g., play/pause/previous track/next track buttons and scrub bar) for the currently selected track.

* Tablet UI uses a Master-Detail layout implemented using fragments. The left fragment is for searching artists and the right fragment is for displaying top tracks of a selected artist. The Now Playing controls are displayed in a DialogFragment.

## User Interface - Function <a name="evaluation-criteria-2"></a>

* App contains a search field that allows the user to enter in the name of an artist to search for.

* When an artist name is entered, app displays list of artist results in a ListView.

* App displays a message (for example, a toast) if the artist name/top tracks list for an artist is not found (asks to refine search).

* When an artist is selected, app launches the “Top Tracks” View.

* App displays a list of top tracks.

* When a track is selected, the app displays the Now Playing screen and starts playing the track.

## Network API Implementation <a name="evaluation-criteria-3"></a>

* App implements Artist Search + GetTopTracks API Requests (Using the Spotify wrapper or by making a HTTP request and deserializing the JSON data).

* App stores the most recent top tracks query results and their respective metadata (track name , artist name, album name) locally in list. The queried results are retained on rotation.

## Media Playback <a name="evaluation-criteria-4"></a>

* App implements streaming playback of tracks.

* User is able to advance to the previous track.

* User is able to advance to the next track.

* Play button starts/resumes playback of currently selected track.

* Pause button pauses playback of currently selected track.

* If a user taps on another track while one is currently playing, playback is stopped on the currently playing track and the newly selected track (in other words, the tracks should not mix).

* Scrub bar displays the current playback position in time and is scrubbable. Scrubbing changes the track position appropriately.
