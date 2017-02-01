package com.cesarpim.androidcourse.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((TextView) findViewById(R.id.text_message)).setText(BuildConfig.THEMOVIEDB_API_KEY);
        updateMovies(getString(R.string.themoviedb_most_popular_path));
    }

    private URL buildMoviesURL(String selectionPath) {
        URL url = null;
        Uri uri = Uri.parse(getString(R.string.themoviedb_base_url)).buildUpon()
                .appendPath(selectionPath)
                .appendQueryParameter(
                        getString(R.string.themoviedb_param_key), BuildConfig.THEMOVIEDB_API_KEY)
                .build();
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    private void updateMovies(String selectionPath) {
        URL url = buildMoviesURL(selectionPath);
        if (url != null) {
            new DownloadMoviesTask().execute(url);
        }
    }

    private class DownloadMoviesTask extends AsyncTask<URL, Void, String> {

        private String getResponseFromUrl(URL url) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String response = null;
            try {
                Scanner scanner = new Scanner(connection.getInputStream());
                scanner.useDelimiter(getString(R.string.scanner_delimiter));
                if (scanner.hasNext()) {
                    response = scanner.next();
                }
            } finally {
                connection.disconnect();
            }
            return response;
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            String results = null;
            try {
                results = getResponseFromUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(String s) {
            if ((s != null) && (!s.equals(""))) {
                ((TextView) findViewById(R.id.text_message)).setText(s);
            }
        }
    }

}
