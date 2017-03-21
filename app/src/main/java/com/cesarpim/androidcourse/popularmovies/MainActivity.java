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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cesarpim.androidcourse.popularmovies.data.FavoriteMoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity
        extends AppCompatActivity
        implements PostersAdapter.PosterClickListener, LoaderManager.LoaderCallbacks<Movie[]> {

    private static final int MOVIES_LOADER_ID = 1001;
    private static final String SORT_BY_KEY = "sort by";
//    private static final String MOVIES_LAYOUT_MANAGER_STATE_KEY = "movies layout manager state";

    private enum SortBy {MOST_POPULAR, HIGHEST_RATED, FAVORITES}

    private Movie[] movies;
    private RecyclerView moviesRecyclerView;
    private TextView errorTextView;
    private ProgressBar loadingProgressBar;
    private PostersAdapter postersAdapter;
    private SortBy sortBy;
//    private Parcelable moviesLayoutManagerState;
    private Boolean scrollUp;

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
        scrollUp = false;
        if (savedInstanceState == null) {
            sortBy = SortBy.MOST_POPULAR;
//            moviesLayoutManagerState = null;
        } else {
            sortBy = SortBy.values()[savedInstanceState.getInt(SORT_BY_KEY)];
//            moviesLayoutManagerState
//                    = savedInstanceState.getParcelable(MOVIES_LAYOUT_MANAGER_STATE_KEY);
        }
        getSupportLoaderManager().initLoader(MOVIES_LOADER_ID, null, this);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
//    }

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
//        outState.putParcelable(
//                MOVIES_LAYOUT_MANAGER_STATE_KEY,
//                moviesRecyclerView.getLayoutManager().onSaveInstanceState());
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
        scrollUp = true;
        getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
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

    private Movie[] getMoviesFromJSONString(String s) throws JSONException, ParseException {
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
            cursor.moveToNext();
        }
        return moviesRead;
    }

    private Movie[] loadMoviesFromInternet(SortBy currentSortBy) {
        Movie[] loadedMovies = null;
        String response = MoviesApiUtils.getResponse(
                this,
                new String[] { getString( currentSortBy == SortBy.MOST_POPULAR ?
                        R.string.themoviedb_most_popular_path :
                        R.string.themoviedb_highest_rated_path)});
        if ((response != null) && (!response.equals(""))) {
            try {
                loadedMovies = getMoviesFromJSONString(response);
            } catch (JSONException|ParseException e) {
                loadedMovies = null;
                e.printStackTrace();
            }
        }
        return loadedMovies;
    }

    private Movie[] loadMoviesFromProvider() {
        Movie[] loadedMovies = null;
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
                super.onStartLoading();
                loadingProgressBar.setVisibility(View.VISIBLE);
                forceLoad();
            }

            @Override
            public Movie[] loadInBackground() {
                SortBy currentSortBy = sortBy;
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
        loadingProgressBar.setVisibility(View.INVISIBLE);
        if (data != null) {
            movies = data;
            makePostersVisible();
            postersAdapter.updateMovies(movies);
            if (scrollUp) {
                moviesRecyclerView.scrollToPosition(0);
                scrollUp = false;
//            } else if (moviesLayoutManagerState != null) {
//                moviesRecyclerView.getLayoutManager()
//                        .onRestoreInstanceState(moviesLayoutManagerState);
//                // Setting the saved state to null to prevent restoring to that state again when
//                // the activity is NOT being recreated
//                moviesLayoutManagerState = null;
            }
        } else {
            makeErrorVisible();
        }
    }

    @Override
    public void onLoaderReset(Loader<Movie[]> loader) {
        if (postersAdapter != null) {
            postersAdapter.updateMovies(null);
        }
    }

}
