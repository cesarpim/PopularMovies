package com.cesarpim.androidcourse.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private Movie[] movies = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateMoviesFromInternet(getString(R.string.themoviedb_most_popular_path));
    }

    private URL buildMoviesURL(String selectionPath) {
        URL url = null;
        Uri uri = Uri.parse(getString(R.string.themoviedb_base_url)).buildUpon()
                .appendPath(selectionPath)
                .appendQueryParameter(
                        getString(R.string.themoviedb_param_key), BuildConfig.THEMOVIEDB_API_KEY)
                .build();
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    private void updateMoviesFromInternet(String selectionPath) {
        URL url = buildMoviesURL(selectionPath);
        if (url != null) {
            new DownloadMoviesTask().execute(url);
        }
    }

    private class DownloadMoviesTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            String results = null;
            try {
                results = getResponseFromURL(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        }

        private String getResponseFromURL(URL url) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String response = null;
            try {
                Scanner scanner = new Scanner(connection.getInputStream());
                scanner.useDelimiter(getString(R.string.scanner_delimiter));
                if (scanner.hasNext()) {
                    response = scanner.next();
                }
            } finally {
                connection.disconnect();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            if ((s != null) && (!s.equals(""))) {
                try {
                    movies = getMoviesFromJSONString(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (Movie m : movies) {
                    Log.i(MainActivity.class.getName(), m.toString() + "\n");
                }
            } else {
                Log.w(MainActivity.class.getName(), "Response from server is null or empty!");
            }
        }

        private Movie[] getMoviesFromJSONString (String s) throws JSONException {
            JSONObject jsonObject = new JSONObject(s);
            // TODO: Check for error codes from the API
            JSONArray jsonArray = jsonObject.getJSONArray(
                    getString(R.string.themoviedb_json_results_tag));
            int numMovies = jsonArray.length();
            Movie[] movies = new Movie[numMovies];
            for (int i = 0; i < numMovies; i++) {
                JSONObject jsonMovie = jsonArray.getJSONObject(i);
                movies[i] = new Movie(
                        jsonMovie.getInt(getString(R.string.themoviedb_json_id_tag)),
                        jsonMovie.getString(getString(R.string.themoviedb_json_original_title_tag)),
                        jsonMovie.getString(getString(R.string.themoviedb_json_poster_path_tag)),
                        jsonMovie.getString(getString(R.string.themoviedb_json_synopsis_tag)),
                        jsonMovie.getDouble(getString(R.string.themoviedb_json_rating_tag)),
                        jsonMovie.getString(getString(R.string.themoviedb_json_release_date_tag)));
            }
            return movies;
        }

    }

//    private static class PostersAdapter extends RecyclerView.Adapter<PostersAdapter.PosterViewHolder> {
//
//
//        static class PosterViewHolder extends RecyclerView.ViewHolder {
//
//            ImageView poster;
//
//            public PosterViewHolder(View itemView) {
//                super(itemView);
//                poster = (ImageView) itemView.findViewById(R.id.image_poster);
//            }
//        }
//    }

}
