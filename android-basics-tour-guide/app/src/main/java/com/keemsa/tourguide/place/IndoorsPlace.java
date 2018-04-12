package com.keemsa.tourguide.place;

/**
 * Created by sebastian on 05/07/16.
 */
public abstract class IndoorsPlace extends Place {
    private String address;
    private String serviceHours;

    public IndoorsPlace(String name, String address, String serviceHours) {
        super(name);
        this.address = address;
        this.serviceHours = serviceHours;
    }

    public IndoorsPlace(String name, int imageId, String address, String serviceHours) {
        super(name, imageId);
        this.address = address;
        this.serviceHours = serviceHours;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getServiceHours() {
        return serviceHours;
    }

    public void setServiceHours(String serviceHours) {
        this.serviceHours = serviceHours;
    }
}
