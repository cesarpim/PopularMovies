package com.cesarpim.androidcourse.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class MainActivity
        extends AppCompatActivity
        implements PostersAdapter.PosterClickListener {

    private enum SortBy {MOST_POPULAR, HIGHEST_RATED}

    private Movie[] movies;
    private RecyclerView moviesRecyclerView;
    private PostersAdapter postersAdapter;
    private SortBy sortBy = null;

    @Override
    public void onPosterClick(int clickedPosterIndex) {
        Intent launchDetailsIntent = new Intent(MainActivity.this, DetailsActivity.class);
        launchDetailsIntent.putExtra(
                getString(R.string.intent_extra_movie_key),
                movies[clickedPosterIndex]);
        startActivity(launchDetailsIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        movies  = new Movie[0];
        moviesRecyclerView = (RecyclerView) findViewById(R.id.recycler_movies);
        GridLayoutManager layoutManager = new GridLayoutManager(this, calculatePosterGridSpan());
        moviesRecyclerView.setLayoutManager(layoutManager);
        moviesRecyclerView.setHasFixedSize(true);
        postersAdapter = new PostersAdapter(movies, this, this);
        moviesRecyclerView.setAdapter(postersAdapter);

        if (sortBy == null) {
            sortBy = SortBy.MOST_POPULAR;
        }
        updateMoviesFromInternet();
    }

    private int calculatePosterGridSpan() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        double availableWidthPixels = metrics.widthPixels;
        // Although dimension is written in dp, getDimension returns it in pixels
        double posterMaxWidthPixels = getResources().getDimension(R.dimen.poster_maximum_width);
        return (int) Math.ceil(availableWidthPixels / posterMaxWidthPixels);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (sortBy != null) {
            if (sortBy == SortBy.MOST_POPULAR) {
                menu.findItem(R.id.action_most_popular).setChecked(false);
            } else {
                menu.findItem(R.id.action_highest_rated).setChecked(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_most_popular:
                sortBy = SortBy.MOST_POPULAR;
                break;
            case R.id.action_highest_rated:
                sortBy = SortBy.HIGHEST_RATED;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        item.setChecked(false);
        updateMoviesFromInternet();
        return true;
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

    private void updateMoviesFromInternet() {
        URL url;
        if (sortBy == SortBy.MOST_POPULAR) {
            url = buildMoviesURL(getString(R.string.themoviedb_most_popular_path));
        } else {
            url = buildMoviesURL(getString(R.string.themoviedb_highest_rated_path));
        }
        if (url != null) {
            new DownloadMoviesTask().execute(url);
        }
    }

    private class DownloadMoviesTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            String results = null;
            try {
                results = getResponseFromURL(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        }

        private String getResponseFromURL(URL url) throws IOException {
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
        protected void onPostExecute(String s) {
            if ((s != null) && (!s.equals(""))) {
                try {
                    movies = getMoviesFromJSONString(s);
                } catch (JSONException|ParseException e) {
                    e.printStackTrace();
                }
//                for (Movie m : movies) {
//                    Log.d(MainActivity.class.getName(), m.toString() + "\n");
//                }
                postersAdapter.updateMovies(movies);
            } else {
                Log.w(MainActivity.class.getName(), "Response from server is null or empty!");
            }
        }

        private Movie[] getMoviesFromJSONString (String s) throws JSONException, ParseException {
            DateFormat themoviedbDateFormat =
                    new SimpleDateFormat(getString(R.string.themoviedb_json_release_date_format));
            JSONObject jsonObject = new JSONObject(s);
            // TODO: Check for error codes from the API
            JSONArray jsonArray = jsonObject.getJSONArray(
                    getString(R.string.themoviedb_json_results_tag));
            int numMovies = jsonArray.length();
            Movie[] moviesRead = new Movie[numMovies];
            for (int i = 0; i < numMovies; i++) {
                JSONObject jsonMovie = jsonArray.getJSONObject(i);
                moviesRead[i] = new Movie(
                        jsonMovie.getInt(getString(R.string.themoviedb_json_id_tag)),
                        jsonMovie.getString(getString(R.string.themoviedb_json_original_title_tag)),
                        jsonMovie.getString(getString(R.string.themoviedb_json_poster_path_tag)),
                        jsonMovie.getString(getString(R.string.themoviedb_json_synopsis_tag)),
                        jsonMovie.getDouble(getString(R.string.themoviedb_json_rating_tag)),
                        themoviedbDateFormat.parse(jsonMovie.getString(
                                getString(R.string.themoviedb_json_release_date_tag))));
            }
            return moviesRead;
        }

    }

}
