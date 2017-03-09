package com.cesarpim.androidcourse.popularmovies;

import android.content.Context;
import android.net.Uri;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by CesarPim on 09-03-2017.
 *
 * Utils class with static methods for communication with the movies API
 *
 * @author CesarPim
 */

public class MoviesApiUtils {

    private MoviesApiUtils() {}

    public static URL buildMoviesURL(Context context, String selectionPath) {
        URL url;
        Uri uri = Uri.parse(context.getString(R.string.themoviedb_base_url)).buildUpon()
                .appendPath(selectionPath)
                .appendQueryParameter(
                        context.getString(R.string.themoviedb_param_key),
                        BuildConfig.THEMOVIEDB_API_KEY)
                .build();
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            url = null;
            e.printStackTrace();
        }
        return url;
    }

    public static String getResponseFromURL(Context context, URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String response = null;
        try {
            Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter(context.getString(R.string.scanner_delimiter));
            if (scanner.hasNext()) {
                response = scanner.next();
            }
        } finally {
            connection.disconnect();
        }
        return response;
    }

}
