package com.cesarpim.androidcourse.popularmovies;

/**
 * Created by CesarPim on 09-03-2017.
 *
 * Class containing various info about a movie trailer.
 *
 * @author CesarPim
 */

public class Trailer {

    private String title;
    private String youtubeKey;

    public Trailer(String title, String youtubeKey) {
        this.title = title;
        this.youtubeKey = youtubeKey;
    }

    public String getTitle() {
        return title;
    }

    public String getYoutubeKey() {
        return youtubeKey;
    }

    @Override
    public String toString() {
        return "Trailer{" +
                "title='" + title + '\'' +
                ", youtubeKey='" + youtubeKey + '\'' +
                '}';
    }
}
