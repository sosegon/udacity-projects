package com.keemsa.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sebastian on 31/08/16.
 */
public class Movie implements Parcelable {
    private String title, posterUrl, synopsis, releaseDate;
    private float rating;
    private long id;

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

    private Movie(Parcel in) {
        posterUrl = in.readString();
        releaseDate = in.readString();
        title = in.readString();
        synopsis = in.readString();
        rating = in.readFloat();
        id = in.readLong();
    }

    public int year() {
        String[] parts = releaseDate.split("-");

        if (parts.length > 0) {
            return Integer.parseInt(parts[0]);
        }
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(posterUrl);
        parcel.writeString(releaseDate);
        parcel.writeString(title);
        parcel.writeString(synopsis);
        parcel.writeFloat(rating);
        parcel.writeLong(id);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }
    };

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
