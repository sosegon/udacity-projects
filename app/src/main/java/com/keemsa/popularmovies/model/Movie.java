package com.keemsa.popularmovies.model;

/**
 * Created by sebastian on 31/08/16.
 */
public class Movie {
    private String title, posterUrl, synopsis, releaseDate;
    private float rating;

    public Movie(String title, String synopsis, String posterUrl, String releaseDate, float rating) {
        this.posterUrl = posterUrl;
        this.releaseDate = releaseDate;
        this.title = title;
        this.synopsis = synopsis;
        this.rating = rating;
    }

    public Movie(String title, String synopsis, String posterUrl, String releaseDate, String rating) {
        this(title, synopsis, posterUrl, releaseDate, rating.equals("") ? 0 : Float.parseFloat(rating));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
