package com.cesarpim.androidcourse.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
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
        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return resultUri;
    }

    @Nullable
    @Override
    public Cursor query(
            @NonNull Uri uri,
            @Nullable String[] projection,
            @Nullable String selection,
            @Nullable String[] selectionArgs,
            @Nullable String sortOrder) {
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        int code = uriMatcher.match(uri);
        Cursor resultCursor;
        switch (code) {
            case CODE_MOVIES:
                resultCursor = db.query(
                        FavoriteMoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_MOVIE_WITH_ID:
                String movieId = uri.getLastPathSegment();
                resultCursor = db.query(
                        FavoriteMoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        FavoriteMoviesContract.MovieEntry.COLUMN_API_MOVIE_ID + " = ? ",
                        new String[]{movieId},
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Invalid URI: " + uri);
        }
        Context context = getContext();
        if (context != null) {
            resultCursor.setNotificationUri(context.getContentResolver(), uri);
        }
        return resultCursor;
    }

    @Override
    public int update(
            @NonNull Uri uri,
            @Nullable ContentValues contentValues,
            @Nullable String s,
            @Nullable String[] strings) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int code = uriMatcher.match(uri);
        int numDeletedMovies;
        switch (code) {
            case CODE_MOVIE_WITH_ID:
                String movieId = uri.getLastPathSegment();
                numDeletedMovies = db.delete(
                        FavoriteMoviesContract.MovieEntry.TABLE_NAME,
                        FavoriteMoviesContract.MovieEntry.COLUMN_API_MOVIE_ID + " = ? ",
                        new String[]{movieId});
                break;
            default:
                throw new UnsupportedOperationException("Invalid URI: " + uri);
        }
        if (numDeletedMovies > 0) {
            Context context = getContext();
            if (context != null) {
                context.getContentResolver().notifyChange(uri, null);
            }
        }
        return numDeletedMovies;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

}
