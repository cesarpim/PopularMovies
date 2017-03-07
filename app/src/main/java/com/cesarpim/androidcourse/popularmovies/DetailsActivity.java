package com.cesarpim.androidcourse.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class DetailsActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Boolean> {

    private static final int FAVORITE_CHECK_LOADER_ID = 2001;

    private ImageView posterImageView;
    private TextView titleTextView;
    private TextView dateTextView;
    private TextView ratingTextView;
    private TextView synopsisTextView;
    private ToggleButton favoriteToggleButton;
    private Movie movie = null;
    private Uri movieUri;

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
        getSupportLoaderManager().initLoader(FAVORITE_CHECK_LOADER_ID, null, this).forceLoad();
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
        if (((ToggleButton) view).isChecked()) {
            addFavorite();
        } else {
            deleteFavorite();
        }
    }

    private void addFavorite() {
        // TODO: Should be in a background thread?
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
            // TODO: Check if duplicate?
            Uri uri = getContentResolver()
                    .insert(FavoriteMoviesContract.MovieEntry.CONTENT_URI, contentValues);
            Log.d("INSERT URI", uri.toString());
        }
    }

    private void deleteFavorite() {
        // TODO: Should be in a background thread?
        if (movie != null) {
            // TODO: Check if exists?
            int numDeletedMovies = getContentResolver().delete(movieUri, null, null);
            if (numDeletedMovies > 0) {
                // TODO: Restart the loader? Same in the insert?
//            getSupportLoaderManager().restartLoader(MainActivity.FAVORITES_LOADER_ID, null, MainActivity.this);
            }
            Log.d("MOVIES DELETED", "" + numDeletedMovies);
        }
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Boolean>(this) {

            @Override
            public Boolean loadInBackground() {
                Boolean isFavorite = false;
                Cursor queryResult = null;
                try {
                    queryResult = getContentResolver().query(movieUri, null, null, null, null);
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
}
