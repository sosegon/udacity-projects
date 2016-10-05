package com.keemsa.popularmovies.model;

/**
 * Created by sebastian on 10/4/16.
 */
public class Trailer {

    private String id, site, key, name, type;
    private long movieId;

    public Trailer(String id, String site, String key, String name, String type, long movieId) {
        this.id = id;
        this.site = site;
        this.key = key;
        this.name = name;
        this.type = type;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
