package com.keemsa.tourguide.place;

/**
 * Created by sebastian on 05/07/16.
 */
public abstract class Place {
    private String name;
    private int imageId;
    private static final int NO_IMAGE_PROVIDED = -1;

    public Place(String name) {
        this.name = name;
    }

    public Place(String name, int imageId) {
        this.imageId = imageId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public boolean hasImage() {
        return this.imageId != Place.NO_IMAGE_PROVIDED;
    }
}
