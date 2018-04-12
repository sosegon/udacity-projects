package com.keemsa.tourguide.place;

/**
 * Created by sebastian on 05/07/16.
 */
public class Restaurant extends IndoorsPlace {

    private String foodType;

    public Restaurant(String name, String address, String serviceHours, String foodType) {
        super(name, address, serviceHours);
        this.foodType = foodType;
    }

    public Restaurant(String name, int imageId, String address, String serviceHours, String foodType) {
        super(name, imageId, address, serviceHours);
        this.foodType = foodType;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }
}
