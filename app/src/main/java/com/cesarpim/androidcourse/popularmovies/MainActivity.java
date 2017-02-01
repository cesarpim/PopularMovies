package com.cesarpim.androidcourse.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private final static String THEMOVIEDB_BASE_URL = "https://api.themoviedb.org/3/movie/";
    private final static String THEMOVIEDB_PARAM_KEY = "api_key";
    private final static String THEMOVIEDB_MOST_POPULAR_PATH = "popular";
    private final static String THEMOVIEDB_TOP_RATED_PATH = "top_rated";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((TextView) findViewById(R.id.text_message)).setText(BuildConfig.THEMOVIEDB_API_KEY);
        updateMovies(THEMOVIEDB_MOST_POPULAR_PATH);
    }

    private void updateMovies(String sortPath) {
        URL url = null;
        Uri uri = Uri.parse(THEMOVIEDB_BASE_URL).buildUpon()
                .appendPath(sortPath)
                .appendQueryParameter(THEMOVIEDB_PARAM_KEY, BuildConfig.THEMOVIEDB_API_KEY)
                .build();
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url != null) {
            new MoviesQueryTask().execute(url);
        }
    }

    private class MoviesQueryTask extends AsyncTask<URL, Void, String> {

        public String getResponseFromHttpUrl(URL url) throws IOException {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = urlConnection.getInputStream();

                Scanner scanner = new Scanner(in);
                scanner.useDelimiter("\\A");

                boolean hasInput = scanner.hasNext();
                if (hasInput) {
                    return scanner.next();
                } else {
                    return null;
                }
            } finally {
                urlConnection.disconnect();
            }
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            String results = null;
            try {
                results = getResponseFromHttpUrl(url);
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
