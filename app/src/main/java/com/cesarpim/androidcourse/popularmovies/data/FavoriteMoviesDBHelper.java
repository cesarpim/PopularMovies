package com.cesarpim.androidcourse.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cesarpim.androidcourse.popularmovies.data.FavoriteMoviesContract.MovieEntry;

/**
 * Created by CesarPim on 05-03-2017.
 *
 * SQL helper for the favorite movies database.
 *
 * @author CesarPim
 */

public class FavoriteMoviesDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "favorite_movies.db";
    public static final int DATABASE_VERSION = 1;


    public FavoriteMoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String CREATE_TABLE_SQL_STATEMENT = "CREATE TABLE " +
                MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.COLUMN_API_MOVIE_ID + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL);";
        sqLiteDatabase.execSQL(CREATE_TABLE_SQL_STATEMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
