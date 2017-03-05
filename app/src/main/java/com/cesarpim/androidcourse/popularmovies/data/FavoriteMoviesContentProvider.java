package com.cesarpim.androidcourse.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by CesarPim on 05-03-2017.
 *
 * Content provider for the favorite movies database.
 *
 * @author CesarPim
 */

public class FavoriteMoviesContentProvider extends ContentProvider {

    public static final int CODE_MOVIES = 100;
    public static final int CODE_MOVIE_WITH_ID = 101;
    private static final UriMatcher uriMatcher = buildUriMatcher();

    private FavoriteMoviesDBHelper dbHelper;


    public static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(
                FavoriteMoviesContract.CONTENT_AUTHORITY,
                FavoriteMoviesContract.PATH_FAVORITES,
                CODE_MOVIES);
        matcher.addURI(
                FavoriteMoviesContract.CONTENT_AUTHORITY,
                FavoriteMoviesContract.PATH_FAVORITES + "/#",
                CODE_MOVIE_WITH_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new FavoriteMoviesDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int code = uriMatcher.match(uri);
        Uri resultUri;
        switch (code) {
            case CODE_MOVIES:
                long id = db.insert(
                        FavoriteMoviesContract.MovieEntry.TABLE_NAME, null, contentValues);
                if (id > 0) {
                    resultUri = ContentUris.withAppendedId(
                            FavoriteMoviesContract.MovieEntry.CONTENT_URI,
                            id);
                } else {
                    throw new SQLException("Failed to insert into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Invalid URI: " + uri);
        }
        // TODO: treat exception?
        getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }

    @Nullable
    @Override
    public Cursor query(
            @NonNull Uri uri,
            @Nullable String[] strings,
            @Nullable String s,
            @Nullable String[] strings1,
            @Nullable String s1) {
        return null;
    }

    @Override
    public int update(
            @NonNull Uri uri,
            @Nullable ContentValues contentValues,
            @Nullable String s,
            @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

}
