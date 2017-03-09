package com.cesarpim.androidcourse.popularmovies;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by CesarPim on 02-02-2017.
 *
 * Class containing various info about a movie.
 *
 * @author CesarPim
 */

public class Movie implements Serializable {

    private int id;
    private String originalTitle;
    private String posterPath;
    private String synopsis;
    private double rating;
    private Date releaseDate;

    public Movie(int id,
                 String originalTitle,
                 String posterPath,
                 String synopsis,
                 double rating,
                 Date releaseDate) {
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

    public Date getReleaseDate() {
        return releaseDate;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", originalTitle='" + originalTitle + '\'' +
                ", posterPath='" + posterPath + '\'' +
                ", synopsis='" + synopsis + '\'' +
                ", rating=" + rating +
                ", releaseDate=" + releaseDate +
                '}';
    }

}
