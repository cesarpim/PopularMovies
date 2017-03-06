package com.cesarpim.androidcourse.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by CesarPim on 04-03-2017.
 *
 * Contract class for the favorite movies database.
 *
 * @author CesarPim
 */

public class FavoriteMoviesContract {

    private FavoriteMoviesContract() {}

    public static final String CONTENT_AUTHORITY = "com.cesarpim.androidcourse.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FAVORITES = "favorites";

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_API_MOVIE_ID = "api_movie_id";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_RELEASE_DATE = "release_date";
    }

}
