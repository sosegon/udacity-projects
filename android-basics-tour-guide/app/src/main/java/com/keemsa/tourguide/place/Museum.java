package com.keemsa.tourguide.place;

/**
 * Created by sebastian on 05/07/16.
 */
public class Museum extends IndoorsPlace {
    private double price;

    public Museum(String name, String address, String serviceHours, double price) {
        super(name, address, serviceHours);
        this.price = price;
    }

    public Museum(String name, int imageId, String address, String serviceHours, double price) {
        super(name, imageId, address, serviceHours);
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
