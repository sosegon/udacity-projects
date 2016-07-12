package com.keemsa.inventory.model;

import android.graphics.Bitmap;

/**
 * Created by sebastian on 11/07/16.
 */
public class Product {

    private int id;
    private String name;
    private int quantity;
    private float price;
    private Bitmap picture;
    private String supplier;
    private String emailSupplier;

    public Product(int id, String name, int quantity, float price, Bitmap picture, String supplier, String emailSupplier) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.picture = picture;
        this.supplier = supplier;
        this.emailSupplier = emailSupplier;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getEmailSupplier() {
        return emailSupplier;
    }

    public void setEmailSupplier(String emailSupplier) {
        this.emailSupplier = emailSupplier;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
