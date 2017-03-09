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

    public static String getResponse(Context context, String[] pathSegments) {
        URL url = buildMoviesURL(context, pathSegments);
        String response = null;
        if (url != null) {
            try {
                response = getResponseFromURL(context, url);
            } catch (IOException e) {
                response = null;
                e.printStackTrace();
            }
        }
        return response;
    }

    private static URL buildMoviesURL(Context context, String[] extraPathSegments) {
        URL url;
        Uri uri = Uri.parse(context.getString(R.string.themoviedb_base_url));
        for (String segment : extraPathSegments) {
            uri = uri.buildUpon().appendPath(segment).build();
        }
        uri = uri.buildUpon().appendQueryParameter(
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

    private static String getResponseFromURL(Context context, URL url) throws IOException {
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
