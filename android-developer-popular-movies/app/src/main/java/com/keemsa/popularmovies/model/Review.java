package com.keemsa.popularmovies.model;

/**
 * Created by sebastian on 10/4/16.
 */
public class Review {

    private String id, author, content, url;
    private long movieId;

    public Review(String id, String author, String content, String url, long movieId) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.url = url;
        this.movieId = movieId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getMovieId() {
        return movieId;
    }

    public void setMovieId(long movieId) {
        this.movieId = movieId;
    }
}
