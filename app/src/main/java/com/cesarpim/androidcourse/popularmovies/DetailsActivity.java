package com.cesarpim.androidcourse.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {

    private TextView movieTextView;
    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        movieTextView = (TextView) findViewById(R.id.text_movie);
        Intent launchIntent = getIntent();
        if (launchIntent.hasExtra(getString(R.string.intent_extra_movie_key))) {
            movie = (Movie) launchIntent.getSerializableExtra(getString(R.string.intent_extra_movie_key));
            movieTextView.setText(movie.toString());
        }

    }
}
