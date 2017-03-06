package com.cesarpim.androidcourse.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cesarpim.androidcourse.popularmovies.data.FavoriteMoviesContract;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class DetailsActivity extends AppCompatActivity {

    private ImageView posterImageView;
    private TextView titleTextView;
    private TextView dateTextView;
    private TextView ratingTextView;
    private TextView synopsisTextView;
    private Movie movie = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        posterImageView = (ImageView) findViewById(R.id.image_poster);
        titleTextView = (TextView) findViewById(R.id.text_original_title);
        dateTextView = (TextView) findViewById(R.id.text_release_date);
        ratingTextView = (TextView) findViewById(R.id.text_rating);
        synopsisTextView = (TextView) findViewById(R.id.text_synopsis);
        Intent launchIntent = getIntent();
        if (launchIntent.hasExtra(getString(R.string.intent_extra_movie_key))) {
            movie = (Movie) launchIntent.getSerializableExtra(getString(R.string.intent_extra_movie_key));
        }
        if (movie != null) {
            setupViews();
        }
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

    public void onClickAddFavorite(View view) {
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

    public void onClickDeleteFavorite(View view) {
        // TODO: Should be in a background thread?
        if (movie != null) {
            Uri uri = FavoriteMoviesContract.MovieEntry.CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(movie.getId()))
                    .build();
            // TODO: Check if exists?
            int numDeletedMovies = getContentResolver().delete(uri, null, null);
            if ( numDeletedMovies > 0) {
                // TODO: Restart the loader? Same in the insert?
//            getSupportLoaderManager().restartLoader(MainActivity.FAVORITES_LOADER_ID, null, MainActivity.this);
            }
            Log.d("MOVIES DELETED", "" + numDeletedMovies);
        }
    }

}
