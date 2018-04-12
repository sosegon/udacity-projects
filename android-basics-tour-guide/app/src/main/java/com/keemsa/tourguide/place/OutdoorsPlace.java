package com.keemsa.tourguide.place;

/**
 * Created by sebastian on 05/07/16.
 */
public abstract class OutdoorsPlace extends Place {

    private String mapLocation;

    public OutdoorsPlace(String name, String mapLocation) {
        super(name);
        this.mapLocation = mapLocation;
    }

    public String getMapLocation() {
        return mapLocation;
    }

    public void setMapLocation(String mapLocation) {
        this.mapLocation = mapLocation;
    }
}
