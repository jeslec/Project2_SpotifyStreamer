![](../../../Screenshots/blob/master/spotify.jpg)

# SPOTIFY STREAMER

## TABLE OF CONTENT

[APP DEMO](#app-demo)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[On a tablet](#tablet-demo)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[On a phone](#phone-demo)<br>
[DESCRIPTION](#description)<br>
[WORK DONE](#work-done)<br>
[KEY CONSIDERATIONS](#key-considerations)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[Libraries Used](#key-considerations-1)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[How data (artists/top tracks) is obtained from the Spotify API](#key-considerations-2)<br>
[EVALUATION CRITERIA](#evaluation-criteria)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[User Interface - Layout](#evaluation-criteria-1)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[User Interface - Function](#evaluation-criteria-2)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[Network API Implementation](#evaluation-criteria-3)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[Media Playback](#evaluation-criteria-4)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[Notifications](#evaluation-criteria-5)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[Sharing Functionality](#evaluation-criteria-6)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[Settings Menu](#evaluation-criteria-7)<br>
[SUPPORTING MATERIAL](#supporting-material)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[Documentation](#supporting-material-1)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[Udacity Courses](#supporting-material-2)<br>

## APP DEMO <a name="app-demo"></a>

### On a tablet <a name="tablet-demo"></a>
![](../../../Screenshots/blob/master/spotify-streamer-anim1.gif)

### On a phone <a name="phone-demo"></a>
![](../../../Screenshots/blob/master/spotify-streamer-anim2.gif)

## DESCRIPTION <a name="description"></a>
This project is based on the highly popular music streaming app Spotify and was one of the assignments as part of the Android Nanodegree certification. Users can search for artists, see their top tracks, listen to streamed music samples and share the currently playing track. The fragments-based layout adapts to the device (phone, tablet) the app is running on to offer the best UI experience.

## WORK DONE <a name="work-done"></a>
I have built this app from the ground up and successfully implemented all required features and met all criteria. 

## KEY CONSIDERATIONS <a name="key-considerations"></a>

### Libraries Used <a name="key-considerations-1"></a>
* <a href="http://square.github.io/picasso/">Picasso</a>: A powerful library that will handle image loading and caching on your behalf. Add compile 'com.squareup.picasso:picasso:2.5.2' to the dependencies block of your build.gradle (Module: app) file.

* <a href="https://github.com/kaaes/spotify-web-api-android">Spotify Wrapper</a>: A wrapper for Spotify Web API. It uses Retrofit to create Java interfaces from the Spotify API endpoints.

### How data (artists/top tracks) is obtained from the Spotify API <a name="key-considerations-2"></a>
I created a class, named Spotify, to handle all API requests. The searchArtist method returns a list of artists based on the supplied search term. The getArtistTopTrack method returns a list of top tracks for the given artist as specifed by its id. Both of these methods are using the Spotify API wrapper, which itself uses Retrofit.
```java
public class Spotify {

    public static ArtistsPager searchArtists(String searchTerm) {
        ArtistsPager artistsPager = new ArtistsPager();

        SpotifyApi spotifyApi = new SpotifyApi();
        SpotifyService spotifyService = spotifyApi.getService();

        try {
            artistsPager = spotifyService.searchArtists(searchTerm);
        } catch (RetrofitError e) {
            Log.e("Spotify", "searchArtists() - Error: " + e.getCause());
        }

        return artistsPager;
    }

    public static Tracks getArtistTopTrack(String artistId, Map<String, Object> queryOptions) {
        Tracks tracks = new Tracks();

        SpotifyApi spotifyApi = new SpotifyApi();
        SpotifyService spotifyService = spotifyApi.getService();

        try {
            tracks = spotifyService.getArtistTopTrack(artistId, queryOptions);
        } catch (RetrofitError e) {
            Log.e("Spotify", "getArtistTopTrack() - Error: " + e.getCause());
        }
        return tracks;
    }
}
```

Since these methods are making network calls, they must be called in a background thread. Therefore, I created two AsyncTasks: one to retrieve a list of artists (SearchArtistTask) and the other to get an artist's top tracks (GetTopTracksTask).

```java
public class SearchArtistTask extends AsyncTask<...> {
    // Get the  list of artists from the Spotify API
    @Override
    protected ArtistsPager doInBackground(String... params) {
        mSearchTerm = params[0];
        return Spotify.searchArtists(mSearchTerm);
    }
    
    @Override
    protected void onPostExecute(ArtistsPager artistsPager) {
        List<ArtistInfo> myArtistInfoList = new ArrayList<ArtistInfo>();
        
        // Extract the data we need, sort it and store it in myArtistInfoList 
        //myArtistInfoList = ...
        
        // Update the UI (ListView) with the list of artists
        mAdapter.clear();
        mAdapter.addAll(myArtistInfoList);
        mAdapter.notifyDataSetChanged();
    }
}
```

```java
public class GetTopTracksTask extends AsyncTask<...> {
    // Get the top tracks for an artist from the Spotify API
    @Override
    protected Tracks doInBackground(String... artistIdList) {
        Tracks tracks = new Tracks();
        tracks = Spotify.getArtistTopTrack(artistId, queryOptions);
        return tracks;
    }

    @Override
    protected void onPostExecute(Tracks top10Tracks) {
        List<TrackInfo> myTrackList = new ArrayList<TrackInfo>();

        // Extract the data we need and sort it
        // myTrackList = ...
        
        // Update the UI (ListView) with the top tracks
        mAdapter.clear();
        mAdapter.addAll(myTrackList);
        mAdapter.notifyDataSetChanged();
    }
}
```

## EVALUATION CRITERIA <a name="evaluation-criteria"></a>

These are the criteria used by the Android Nanodegree code reviewers. In order for a project to be a success, and therefore for us to be able to move on to the next project, the Spotify Streamer app must meet all of them. 

### User Interface - Layout <a name="evaluation-criteria-1"></a>
* [Phone] UI contains a screen for searching for an artist and displaying a list of artist results.

* Individual artist result layout contains - Artist Thumbnail , Artist name.

* [Phone] UI contains a screen for displaying the top tracks for a selected artist.

* Individual track layout contains - Album art thumbnail, track name , album name.

* [Phone] UI contains a screen that represents the player. It contains track info (e.g., track duration, elapsed time, artist name, album name, album artwork, and track name). It also contains playback controls (e.g., play/pause/previous track/next track buttons and scrub bar) for the currently selected track.

* Tablet UI uses a Master-Detail layout implemented using fragments. The left fragment is for searching artists and the right fragment is for displaying top tracks of a selected artist. The Now Playing controls are displayed in a DialogFragment.

### User Interface - Function <a name="evaluation-criteria-2"></a>

* App contains a search field that allows the user to enter in the name of an artist to search for.

* When an artist name is entered, app displays list of artist results in a ListView.

* App displays a message (for example, a toast) if the artist name/top tracks list for an artist is not found (asks to refine search).

* When an artist is selected, app launches the “Top Tracks” View.

* App displays a list of top tracks.

* When a track is selected, the app displays the Now Playing screen and starts playing the track.

* App displays a “Now Playing” Button in the ActionBar that serves to reopen the player UI should the user navigate back to browse content and then want to resume control over playback.

### Network API Implementation <a name="evaluation-criteria-3"></a>

* App implements Artist Search + GetTopTracks API Requests (Using the Spotify wrapper or by making a HTTP request and deserializing the JSON data).

* App stores the most recent top tracks query results and their respective metadata (track name , artist name, album name) locally in list. The queried results are retained on rotation.

### Media Playback <a name="evaluation-criteria-4"></a>

* App implements streaming playback of tracks.

* User is able to advance to the previous track.

* User is able to advance to the next track.

* Play button starts/resumes playback of currently selected track.

* Pause button pauses playback of currently selected track.

* If a user taps on another track while one is currently playing, playback is stopped on the currently playing track and the newly selected track (in other words, the tracks should not mix).

* Scrub bar displays the current playback position in time and is scrubbable. Scrubbing changes the track position appropriately.

### Notifications <a name="evaluation-criteria-5"></a>

* App implements a notification with playback controls (Play, pause , next & previous track).

* Notification media controls are usable on the lockscreen and drawer.

* Notification displays track name and album art thumbnail.

### Sharing Functionality <a name="evaluation-criteria-6"></a>

* App adds a menu for sharing the currently playing track.

* App uses a shareIntent to expose the external Spotify URL for the current track.

### Settings Menu <a name="evaluation-criteria-7"></a>

* App has a menu item to select the country code (which is automatically passed into the get Top Tracks query).

* App has menu item to toggle showing notification controls on the drawer and lock screen.

## SUPPORTING MATERIAL <a name="supporting-material"></a>

### Documentation <a name="supporting-material-1"></a>
* <a href="https://docs.google.com/presentation/d/1Q8LwzD5ODqirWG7K_e4sklE3fEFY_dr4kH4hfoRa0BQ/pub?start=false&loop=false&delayms=15000&slide=id.ga25585343_0_106">Spotify API Guide</a>

* <a href="https://developer.spotify.com/web-api/endpoint-reference/
">Spotify API Endpoint Reference</a>

### Udacity Courses <a name="supporting-material-2"></a>

* <a href="https://www.udacity.com/course/developing-android-apps--ud853
">Developing Android Apps (ud853)</a>
