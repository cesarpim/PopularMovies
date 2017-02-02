package com.cesarpim.androidcourse.popularmovies;

/**
 * Created by CesarPim on 02-02-2017.
 *
 * Class containing various info about a movie.
 *
 * @author CesarPim
 */

public class Movie {

    private int id;
    private String originalTitle;
    private String posterPath;
    private String synopsis;
    private double rating;
    private String releaseDate;

    public Movie(int id,
                 String originalTitle,
                 String posterPath,
                 String synopsis,
                 double rating,
                 String releaseDate) {
        this.id = id;
        this.originalTitle = originalTitle;
        this.posterPath = posterPath;
        this.synopsis = synopsis;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    public int getId() {
        return id;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public double getRating() {
        return rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    @Override
    public String toString() {
        return "[" + id +
                " " + originalTitle +
                " " + posterPath +
                " " + rating +
                " " + releaseDate +
                " " + synopsis + "]";
    }
}
