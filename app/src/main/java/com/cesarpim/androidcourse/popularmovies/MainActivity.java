package com.cesarpim.androidcourse.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cesarpim.androidcourse.popularmovies.data.FavoriteMoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity
        extends AppCompatActivity
        implements PostersAdapter.PosterClickListener, LoaderManager.LoaderCallbacks<Movie[]> {

    private static final int MOVIES_LOADER_ID = 1001;
    private static final String SORT_BY_KEY = "sort by";

    private enum SortBy {MOST_POPULAR, HIGHEST_RATED, FAVORITES}

    private Movie[] movies;
    private RecyclerView moviesRecyclerView;
    private TextView errorTextView;
    private ProgressBar loadingProgressBar;
    private PostersAdapter postersAdapter;
    private SortBy sortBy;

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

        errorTextView = (TextView) findViewById(R.id.text_error_main);
        loadingProgressBar = (ProgressBar) findViewById(R.id.progress_loading);
        if (savedInstanceState == null) {
            sortBy = SortBy.MOST_POPULAR;
        } else {
            sortBy = SortBy.values()[savedInstanceState.getInt(SORT_BY_KEY)];
        }
        updateMoviesFromSource();
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SORT_BY_KEY, sortBy.ordinal());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (sortBy != null) {
            switch (sortBy) {
                case MOST_POPULAR:
                    menu.findItem(R.id.action_most_popular).setChecked(false);
                    break;
                case HIGHEST_RATED:
                    menu.findItem(R.id.action_highest_rated).setChecked(false);
                    break;
                case FAVORITES:
                    menu.findItem(R.id.action_favorites).setChecked(false);
                    break;
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
            case R.id.action_favorites:
                sortBy = SortBy.FAVORITES;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        item.setChecked(false);
        updateMoviesFromSource();
        moviesRecyclerView.scrollToPosition(0);
        return true;
    }

    private void makePostersVisible() {
        errorTextView.setVisibility(View.INVISIBLE);
        moviesRecyclerView.setVisibility(View.VISIBLE);
    }

    private void makeErrorVisible() {
        moviesRecyclerView.setVisibility(View.INVISIBLE);
        errorTextView.setVisibility(View.VISIBLE);
    }

    private void updateMoviesFromSource() {
//        Bundle loaderArgs = new Bundle();
//        loaderArgs.putInt(SORT_BY_KEY, sortBy.ordinal());
        LoaderManager manager = getSupportLoaderManager();
        if (manager.getLoader(MOVIES_LOADER_ID) == null) {
            manager.initLoader(MOVIES_LOADER_ID, null, this);
        } else {
            manager.restartLoader(MOVIES_LOADER_ID, null, this);
        }
    }

    private Movie[] getMoviesFromJSONString (String s) throws JSONException, ParseException {
        DateFormat themoviedbDateFormat =
                new SimpleDateFormat(getString(R.string.themoviedb_json_release_date_format));
        JSONObject jsonObject = new JSONObject(s);
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

    private Movie[] getMoviesFromCursor (Cursor cursor) {
        int numMovies = cursor.getCount();
        Movie[] moviesRead = new Movie[numMovies];
        Log.d(MainActivity.class.getName(), "numMovies = " + numMovies);
        cursor.moveToFirst();
        for (int i = 0; i < numMovies; i++) {
            moviesRead[i] = new Movie(
                    cursor.getInt(cursor.getColumnIndex(
                            FavoriteMoviesContract.MovieEntry.COLUMN_API_MOVIE_ID)),
                    cursor.getString(cursor.getColumnIndex(
                            FavoriteMoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE)),
                    cursor.getString(cursor.getColumnIndex(
                            FavoriteMoviesContract.MovieEntry.COLUMN_POSTER_PATH)),
                    cursor.getString(cursor.getColumnIndex(
                            FavoriteMoviesContract.MovieEntry.COLUMN_SYNOPSIS)),
                    cursor.getInt(cursor.getColumnIndex(
                            FavoriteMoviesContract.MovieEntry.COLUMN_RATING)),
                    new Date(cursor.getLong(cursor.getColumnIndex(
                            FavoriteMoviesContract.MovieEntry.COLUMN_RELEASE_DATE))));
            Log.v(MainActivity.class.getName(), "movie " + i + " READ = " + moviesRead[i]);
            cursor.moveToNext();
        }
        return moviesRead;
    }

    private Movie[] loadMoviesFromInternet(SortBy currentSortBy) {
        Movie[] loadedMovies = null;
        URL url;
        url = MoviesApiUtils.buildMoviesURL(this,
                getString( currentSortBy == SortBy.MOST_POPULAR ?
                        R.string.themoviedb_most_popular_path :
                        R.string.themoviedb_highest_rated_path));
        if (url != null) {
            String response;
            try {
                response = MoviesApiUtils.getResponseFromURL(this, url);
            } catch (IOException e) {
                response = null;
                e.printStackTrace();
            }
            if ((response != null) && (!response.equals(""))) {
                try {
                    loadedMovies = getMoviesFromJSONString(response);
                } catch (JSONException|ParseException e) {
                    loadedMovies = null;
                    e.printStackTrace();
                }
            }
        }
        return loadedMovies;
    }

    private Movie[] loadMoviesFromProvider() {
        Movie [] loadedMovies = null;
        Cursor queryResults;
        try {
            queryResults = getContentResolver().query(
                    FavoriteMoviesContract.MovieEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    FavoriteMoviesContract.MovieEntry._ID);
        } catch (Exception e) {
            queryResults = null;
            e.printStackTrace();
        }
        if (queryResults != null) {
            loadedMovies = getMoviesFromCursor(queryResults);
        }
        return loadedMovies;
    }

    @Override
    public Loader<Movie[]> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Movie[]>(this) {

            @Override
            protected void onStartLoading() {
                Log.d(MainActivity.class.getName(), "onStartLoading CALLED");
                loadingProgressBar.setVisibility(View.VISIBLE);
                forceLoad();
            }

            @Override
            public Movie[] loadInBackground() {
                Log.d(MainActivity.class.getName(), "loadInBackground CALLED");
                SortBy currentSortBy = sortBy; //SortBy.values()[args.getInt(SORT_BY_KEY)];
                Movie[] newMovies;
                if (currentSortBy == SortBy.FAVORITES) {
                    newMovies = loadMoviesFromProvider();
                } else {
                    newMovies = loadMoviesFromInternet(currentSortBy);
                }
                return newMovies;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Movie[]> loader, Movie[] data) {
        Log.d(MainActivity.class.getName(), "onLoadFinished CALLED");
        loadingProgressBar.setVisibility(View.INVISIBLE);
        if (data != null) {
            movies = data;
            makePostersVisible();
            postersAdapter.updateMovies(movies);
        } else {
            makeErrorVisible();
        }
    }

    @Override
    public void onLoaderReset(Loader<Movie[]> loader) {
        // TODO: ???
        Log.d(MainActivity.class.getName(), "onLoaderReset CALLED");
    }

}

/*
    private class DownloadMoviesTask extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            String results;
            try {
                results = getResponseFromURL(url);
            } catch (IOException e) {
                results = null;
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(String s) {
            loadingProgressBar.setVisibility(View.INVISIBLE);
            if ((s != null) && (!s.equals(""))) {
                try {
                    movies = getMoviesFromJSONString(s);
                } catch (JSONException|ParseException e) {
                    movies = null;
                    e.printStackTrace();
                }
                makePostersVisible();
                postersAdapter.updateMovies(movies);
            } else {
                makeErrorVisible();
            }
        }
    }
*/

/*
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // TODO: Use CursorLoader or AsyncTaskLoader??
        return new CursorLoader(
                this,
                FavoriteMoviesContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                FavoriteMoviesContract.MovieEntry._ID);
*/
/*
        return new AsyncTaskLoader<Cursor>(this) {

//            Cursor loadedData = null;

            @Override
            protected void onStartLoading() {
//                if (loadedData != null) {
//                    deliverResult(loadedData);
//                } else {
                    Log.d("ON START LOADING", "YES");
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    forceLoad();
//                }
            }

            @Override
            public Cursor loadInBackground() {
                Cursor results;
                Log.d("LOAD IN BACKGROUND", "YES");
                try {
                    results = getContentResolver().query(
                            FavoriteMoviesContract.MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            FavoriteMoviesContract.MovieEntry._ID);
                } catch (Exception e) {
                    results = null;
                    e.printStackTrace();
                }
                return results;
            }

//            @Override
//            public void deliverResult(Cursor data) {
//                loadedData = data;
//                super.deliverResult(data);
//            }
        };
*//*

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // TODO: progress bar not active
        // loadingProgressBar.setVisibility(View.INVISIBLE);
        if (sortBy == SortBy.FAVORITES) {
            if (data != null) {
                movies = getMoviesFromCursor(data);
                makePostersVisible();
                postersAdapter.updateMovies(movies);
            } else {
                makeErrorVisible();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // TODO: ???
    }
*/
