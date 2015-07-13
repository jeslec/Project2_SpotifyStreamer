package com.lecomte.jessy.spotifystreamerstage1v3;


import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

public class SearchableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        handleIntent(getIntent());

        /*// Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //doMySearch(query);
            new MyAsyncTask(this);
        }*/
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            // Do work using string
            new MyAsyncTask(this).execute("allo");
        }
    }

    private class MyAsyncTask extends AsyncTask<String, String, String> {

        private ProgressDialog mProgressDialog;
        private Activity mCallingActivity;

        public MyAsyncTask(Activity activity) {
            mCallingActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Note: must use the calling activity and not just a context obtained from
            // getRes.getContext or else it crashes!
            // http://stackoverflow.com/questions/2634991/android-1-6-android-view-windowmanagerbadtokenexception-unable-to-add-window#2639515
            // TODO: Replace this by a custom and better-looking dialog!
            mProgressDialog = new ProgressDialog(mCallingActivity);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setTitle("Querying Spotify Server");
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mProgressDialog.dismiss();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return new String("allo");
        }
    }

    /*private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()
            try {
                // Do your long operations here and return the result
                int time = Integer.parseInt(params[0]);
                // Sleeping for given time period
                Thread.sleep(time);
                resp = "Slept for " + time + " milliseconds";
            } catch (InterruptedException e) {
                e.printStackTrace();
                resp = e.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            return resp;
        }

        *//*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         *//*
        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            finalResult.setText(result);
        }

        *//*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPreExecute()
         *//*
        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog
        }

        *//*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         *//*
        @Override
        protected void onProgressUpdate(String... text) {
            finalResult.setText(text[0]);
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_searchable, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        // Make sure to use android.support.v7.widget.SearchView and not android.widget.SearchView
        // or else the app will crash during run-time
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
