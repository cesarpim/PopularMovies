package com.cesarpim.androidcourse.popularmovies;

/**
 * Created by CesarPim on 10-03-2017.
 *
 * Class containing various info about a movie review.
 *
 * @author CesarPim
 */

public class Review {

    private String author;
    private String content;

    public Review(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Review{" +
                "author='" + author + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

}
