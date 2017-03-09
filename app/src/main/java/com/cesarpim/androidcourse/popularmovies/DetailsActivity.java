package com.cesarpim.androidcourse.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cesarpim.androidcourse.popularmovies.data.FavoriteMoviesContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DetailsActivity
        extends AppCompatActivity {

    private static final int FAVORITE_CHECK_LOADER_ID = 2001;
    private static final int FAVORITE_TOGGLE_LOADER_ID = 2002;

    private ImageView posterImageView;
    private TextView titleTextView;
    private TextView dateTextView;
    private TextView ratingTextView;
    private TextView synopsisTextView;
    private ToggleButton favoriteToggleButton;
    private Movie movie = null;
    private Uri movieUri;
    private Trailer[] trailers;

    /**
     * Loader that returns a boolean indicating whether the movie is or isn't in the favorites.
     */
    private LoaderManager.LoaderCallbacks<Boolean> favoriteCheckLoaderListener =
            new LoaderManager.LoaderCallbacks<Boolean>() {
                @Override
                public Loader<Boolean> onCreateLoader(int id, Bundle args) {
                    return new AsyncTaskLoader<Boolean>(DetailsActivity.this) {

                        @Override
                        public Boolean loadInBackground() {
                            // TODO: THIS IS SUPPOSED TO GO TO ITS OWN LOADER PERHAPS BOUND TO THE ACTIVITY
                            trailers = loadTrailersFromInternet(movie.getId());
                            Boolean isFavorite = false;
                            Cursor queryResult = null;
                            try {
                                queryResult = getContentResolver()
                                        .query(movieUri, null, null, null, null);
                            } finally {
                                if (queryResult != null) {
                                    isFavorite = queryResult.getCount() > 0;
                                    queryResult.close();
                                }
                            }
                            return isFavorite;
                        }
                    };
                }

                @Override
                public void onLoadFinished(Loader<Boolean> loader, Boolean data) {
                    favoriteToggleButton.setChecked(data);
                    favoriteToggleButton.setEnabled(true);
                }

                @Override
                public void onLoaderReset(Loader<Boolean> loader) {
                }
            };

    /**
     * Loader that inserts/deletes the movie to/from the favorites
     */
    private LoaderManager.LoaderCallbacks<Void> favoriteToggleLoaderListener =
            new LoaderManager.LoaderCallbacks<Void>() {
                @Override
                public Loader<Void> onCreateLoader(int id, Bundle args) {
                    return new AsyncTaskLoader<Void>(DetailsActivity.this) {

                        @Override
                        public Void loadInBackground() {
                            if (favoriteToggleButton.isChecked()) {
                                addFavorite();
                            } else {
                                deleteFavorite();
                            }
                            return null;
                        }
                    };
                }

                @Override
                public void onLoadFinished(Loader<Void> loader, Void data) {
                }

                @Override
                public void onLoaderReset(Loader<Void> loader) {
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        posterImageView = (ImageView) findViewById(R.id.image_poster);
        titleTextView = (TextView) findViewById(R.id.text_original_title);
        dateTextView = (TextView) findViewById(R.id.text_release_date);
        ratingTextView = (TextView) findViewById(R.id.text_rating);
        synopsisTextView = (TextView) findViewById(R.id.text_synopsis);
        favoriteToggleButton = (ToggleButton) findViewById(R.id.toggle_favorite);
        Intent launchIntent = getIntent();
        if (launchIntent.hasExtra(getString(R.string.intent_extra_movie_key))) {
            movie = (Movie) launchIntent.getSerializableExtra(getString(R.string.intent_extra_movie_key));
        }
        if (movie != null) {
            setupViews();
        }
        movieUri = FavoriteMoviesContract.MovieEntry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(movie.getId()))
                .build();
        getSupportLoaderManager()
                .initLoader(FAVORITE_CHECK_LOADER_ID, null, favoriteCheckLoaderListener)
                .forceLoad();
    }

    private void setupViews () {
        String posterStringURL =
                getString(R.string.themoviedb_image_base_url)
                        + getString(R.string.themoviedb_image_size)
                        + movie.getPosterPath();
        Picasso.with(this).load(posterStringURL).into(posterImageView);
        titleTextView.setText(movie.getOriginalTitle());
        DateFormat dateFormat =
                new SimpleDateFormat(getString(R.string.details_release_date_format));
        dateTextView.setText(dateFormat.format(movie.getReleaseDate()));
        String ratingString = String.valueOf(movie.getRating()) + getString(R.string.details_rating_cap);
        ratingTextView.setText(ratingString);
        synopsisTextView.setText(movie.getSynopsis());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClickToggleFavorite(View view) {
        getSupportLoaderManager()
                .initLoader(FAVORITE_TOGGLE_LOADER_ID, null, favoriteToggleLoaderListener)
                .forceLoad();
    }

    private void addFavorite() {
        if (movie != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(FavoriteMoviesContract.MovieEntry.COLUMN_API_MOVIE_ID, movie.getId());
            contentValues.put(
                    FavoriteMoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
                    movie.getOriginalTitle());
            contentValues.put(
                    FavoriteMoviesContract.MovieEntry.COLUMN_POSTER_PATH,
                    movie.getPosterPath());
            contentValues.put(
                    FavoriteMoviesContract.MovieEntry.COLUMN_SYNOPSIS,
                    movie.getSynopsis());
            contentValues.put(
                    FavoriteMoviesContract.MovieEntry.COLUMN_RATING,
                    movie.getRating());
            contentValues.put(
                    FavoriteMoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
                    movie.getReleaseDate().getTime());
            Uri uri = getContentResolver()
                    .insert(FavoriteMoviesContract.MovieEntry.CONTENT_URI, contentValues);
            Log.d(DetailsActivity.class.getName(), "INSERT URI: " + uri);
        }
    }

    private void deleteFavorite() {
        if (movie != null) {
            int numDeletedMovies = getContentResolver().delete(movieUri, null, null);
            Log.d(DetailsActivity.class.getName(), "MOVIES DELETED: " + numDeletedMovies);
        }
    }

    private Trailer[] loadTrailersFromInternet(int movieId) {
        Trailer[] loadedTrailers = null;
        String response = MoviesApiUtils.getResponse(
                this,
                new String[] {"" + movieId, getString(R.string.themoviedb_trailers_path)});
        Log.v(DetailsActivity.class.getName(), "TRAILERS JSON: " + response);
        if ((response != null) && (!response.equals(""))) {
            try {
                loadedTrailers = getTrailersFromJSONString(response);
            } catch (JSONException|ParseException e) {
                loadedTrailers = null;
                e.printStackTrace();
            }
        }
        // TODO: DEBUG
        for (Trailer trailer : loadedTrailers) {
            Log.v(DetailsActivity.class.getName(), trailer.toString());
        }
        return loadedTrailers;
    }

    private Trailer[] getTrailersFromJSONString(String s) throws JSONException, ParseException {
        JSONObject jsonObject = new JSONObject(s);
        JSONArray jsonArray = jsonObject.getJSONArray(
                getString(R.string.themoviedb_json_trailer_results_tag));
        int numTrailers = jsonArray.length();
        Trailer[] trailersRead = new Trailer[numTrailers];
        for (int i = 0; i < numTrailers; i++) {
            JSONObject jsonTrailer = jsonArray.getJSONObject(i);
            trailersRead[i] = new Trailer(
                    jsonTrailer.getString(
                            getString(R.string.themoviedb_json_trailer_title_tag)),
                    jsonTrailer.getString(
                            getString(R.string.themoviedb_json_trailer_youtube_key_tag)));
        }
        return trailersRead;
    }

}
