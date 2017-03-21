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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
        extends AppCompatActivity
        implements TrailersAdapter.TrailerClickListener, LoaderManager.LoaderCallbacks<String> {

    private static final int TRAILERS_LOADER_ID = 2001;
    private static final int REVIEWS_LOADER_ID = 2002;
    private static final int FAVORITE_CHECK_LOADER_ID = 2003;
    private static final int FAVORITE_TOGGLE_LOADER_ID = 2004;

    private ImageView posterImageView;
    private TextView titleTextView;
    private TextView dateTextView;
    private TextView ratingTextView;
    private TextView synopsisTextView;
    private ToggleButton favoriteToggleButton;
    private TextView trailersTitleText;
    private TextView reviewsTitleText;
    private RecyclerView trailersRecyclerView;
    private RecyclerView reviewsRecyclerView;
    private Movie movie = null;
    private Uri movieUri;

    /**
     * Loader that returns a boolean indicating whether the movie is or isn't in the favorites.
     */
    private LoaderManager.LoaderCallbacks<Boolean> favoriteCheckLoaderListener =
            new LoaderManager.LoaderCallbacks<Boolean>() {
                @Override
                public Loader<Boolean> onCreateLoader(int id, Bundle args) {
                    return new AsyncTaskLoader<Boolean>(DetailsActivity.this) {
                        @Override
                        protected void onStartLoading() {
                            super.onStartLoading();
                            forceLoad();
                        }
                        @Override
                        public Boolean loadInBackground() {
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
                        protected void onStartLoading() {
                            super.onStartLoading();
                            forceLoad();
                        }
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
    public void onTrailerClick(String clickedTrailerYoutubeKey) {
        String trailerAddress = String.format(
                getString(R.string.youtube_video_template_address),
                clickedTrailerYoutubeKey);
        Uri trailerUri = Uri.parse(trailerAddress);
        Intent launchTrailerIntent = new Intent(Intent.ACTION_VIEW, trailerUri);
        startActivity(launchTrailerIntent);
    }

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
        trailersTitleText = (TextView) findViewById(R.id.text_trailers_title);
        reviewsTitleText = (TextView) findViewById(R.id.text_reviews_title);
        trailersRecyclerView = initRecyclerView(R.id.recycler_trailers);
        reviewsRecyclerView = initRecyclerView(R.id.recycler_reviews);

        Intent launchIntent = getIntent();
        if (launchIntent.hasExtra(getString(R.string.intent_extra_movie_key))) {
            movie = (Movie) launchIntent.getSerializableExtra(getString(R.string.intent_extra_movie_key));
        }
        if (movie != null) {
            loadViews();
        }
    }

    private RecyclerView initRecyclerView(int viewResId) {
        RecyclerView recyclerView = (RecyclerView) findViewById(viewResId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        return recyclerView;
    }

    private void loadViews() {
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
        movieUri = FavoriteMoviesContract.MovieEntry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(movie.getId()))
                .build();
        LoaderManager manager = getSupportLoaderManager();
        manager.initLoader(FAVORITE_CHECK_LOADER_ID, null, favoriteCheckLoaderListener);
        manager.initLoader(TRAILERS_LOADER_ID, null, this);
        manager.initLoader(REVIEWS_LOADER_ID, null, this);
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
                .restartLoader(FAVORITE_TOGGLE_LOADER_ID, null, favoriteToggleLoaderListener);
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
            getContentResolver()
                    .insert(FavoriteMoviesContract.MovieEntry.CONTENT_URI, contentValues);
        }
    }

    private void deleteFavorite() {
        if (movie != null) {
            getContentResolver().delete(movieUri, null, null);
        }
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

    private Review[] getReviewsFromJSONString(String s) throws JSONException, ParseException {
        JSONObject jsonObject = new JSONObject(s);
        JSONArray jsonArray = jsonObject.getJSONArray(
                getString(R.string.themoviedb_json_review_results_tag));
        int numReviews = jsonArray.length();
        Review[] reviewsRead = new Review[numReviews];
        for (int i = 0; i < numReviews; i++) {
            JSONObject jsonTrailer = jsonArray.getJSONObject(i);
            reviewsRead[i] = new Review(
                    jsonTrailer.getString(
                            getString(R.string.themoviedb_json_review_author_tag)),
                    jsonTrailer.getString(
                            getString(R.string.themoviedb_json_review_content_key_tag)));
        }
        return reviewsRead;
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        final int pathResId;
        switch (id) {
            case TRAILERS_LOADER_ID:
                pathResId = R.string.themoviedb_trailers_path;
                break;
            case REVIEWS_LOADER_ID:
                pathResId = R.string.themoviedb_reviews_path;
                break;
            default:
                throw new RuntimeException("Invalid loader id: " + id);
        }
        return new AsyncTaskLoader<String>(this) {
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                forceLoad();
            }
            @Override
            public String loadInBackground() {
                return MoviesApiUtils.getResponse(
                        DetailsActivity.this,
                        new String[] {"" + movie.getId(),
                                getString(pathResId)});
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        int id = loader.getId();
        switch (id) {
            case TRAILERS_LOADER_ID:
                if ((data != null) && (!data.equals(""))) {
                    Trailer[] trailers;
                    try {
                        trailers = getTrailersFromJSONString(data);
                    } catch (JSONException|ParseException e) {
                        trailers = new Trailer[0];
                        e.printStackTrace();
                    }
                    trailersTitleText.setText(getString(trailers.length == 0 ?
                            R.string.no_trailers_title :
                            R.string.trailers_title));
                    trailersRecyclerView
                            .setAdapter(new TrailersAdapter(trailers, DetailsActivity.this));
                }
                break;
            case REVIEWS_LOADER_ID:
                if ((data != null) && (!data.equals(""))) {
                    Review[] reviews;
                    try {
                        reviews = getReviewsFromJSONString(data);
                    } catch (JSONException|ParseException e) {
                        reviews = new Review[0];
                        e.printStackTrace();
                    }
                    reviewsTitleText.setText(getString(reviews.length == 0 ?
                            R.string.no_reviews_title :
                            R.string.reviews_title));
                    reviewsRecyclerView
                            .setAdapter(new ReviewsAdapter(reviews));
                }
                break;
            default:
                throw new RuntimeException("Invalid loader id: " + id);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        int id = loader.getId();
        switch (id) {
            case TRAILERS_LOADER_ID:
                ((TrailersAdapter) trailersRecyclerView.getAdapter()).updateTrailers(null);
                break;
            case REVIEWS_LOADER_ID:
                ((ReviewsAdapter) reviewsRecyclerView.getAdapter()).updateReviews(null);
                break;
            default:
                throw new RuntimeException("Invalid loader id: " + id);
        }
    }

}
