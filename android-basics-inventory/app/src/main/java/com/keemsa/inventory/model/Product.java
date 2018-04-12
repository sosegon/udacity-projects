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
    private String supplierName;
    private String supplierEmail;
    private int supplierPhone;

    public Product(int id, String name, int quantity, float price, Bitmap picture, String supplierName, String supplierEmail, int supplierPhone) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.picture = picture;
        this.supplierName = supplierName;
        this.supplierEmail = supplierEmail;
        this.supplierPhone = supplierPhone;
    }

    public Product(int id, String name, int quantity, float price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
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

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierEmail() {
        return supplierEmail;
    }

    public void setSupplierEmail(String supplierEmail) {
        this.supplierEmail = supplierEmail;
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

    public int getSupplierPhone() {
        return supplierPhone;
    }

    public void setSupplierPhone(int supplierPhone) {
        this.supplierPhone = supplierPhone;
    }

    public void copy(Product product){
        this.setId(product.getId());
        this.setName(product.getName());
        this.setQuantity(product.getQuantity());
        this.setPrice(product.getPrice());
        this.setPicture(product.getPicture());
        this.setSupplierName(product.getSupplierName());
        this.setSupplierEmail(product.getSupplierEmail());
        this.setSupplierPhone(product.getSupplierPhone());
    }

    public Product clone(){
        return new Product(
            this.id,
            this.name,
            this.quantity,
            this.price,
            this.picture,
            this.supplierName,
            this.supplierEmail,
            this.supplierPhone
        );
    }
}
