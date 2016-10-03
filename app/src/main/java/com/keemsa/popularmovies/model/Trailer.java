package com.keemsa.popularmovies.model;

/**
 * Created by sebastian on 10/4/16.
 */
public class Trailer {

    private String id, site, key;
    private long movieId;

    public Trailer(String id, String site, String key, long movieId) {
        this.id = id;
        this.site = site;
        this.key = key;
        this.movieId = movieId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getMovieId() {
        return movieId;
    }

    public void setMovieId(long movieId) {
        this.movieId = movieId;
    }
}
