package com.cesarpim.androidcourse.popularmovies.data;

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

    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_POSTER_PATH = "poster_path";
    }

}
