package com.example.eatnow.model;

import java.util.ArrayList;

public class Restaurant {
    // Unique id of restaurant
    private int restaurant_id;
    // HTTP String link of image of restaurant ("" if null)
    private String image_link;
    // Name of restaurant
    private String name;
    // Restaurant address
    private String address;
    // The area the restaurant is situated at (You can view the list of areas in restaurant_areas in firebase)
    private String area;
    // Restaurant website ("" if null)
    private String website;
    // Restaurant phone number ("" if null)
    private String phone;
    // List of restaurant categories (Empty array list if null)
    private ArrayList<String> categories;
    // List of restaurant features (Empty array list if null)
    private ArrayList<String> features;
    // Number of reviews the restaurant has
    private int reviews;
    // Rating of restaurant based on reviews
    private float rating;
    /*
        To determine whether the restaurant is reservable
        1 : Reservable
        0 : Not Reservable
     */
    private int reservation;

    // Constructors
    public Restaurant() {
    }

    public Restaurant(int restaurant_id, String image_link, String name, String address, String area, String website, String phone, ArrayList<String> categories, ArrayList<String> features, int reviews, float rating, int reservation) {
        this.restaurant_id = restaurant_id;
        this.image_link = image_link;
        this.name = name;
        this.address = address;
        this.area = area;
        this.website = website;
        this.phone = phone;
        this.categories = categories;
        this.features = features;
        this.reviews = reviews;
        this.rating = rating;
        this.reservation = reservation;
    }

    // Accessors and Mutators
    public int getRestaurant_id() {
        return restaurant_id;
    }

    public void setRestaurant_id(int restaurant_id) {
        this.restaurant_id = restaurant_id;
    }

    public String getImage_link() {
        return image_link;
    }

    public void setImage_link(String image_link) {
        this.image_link = image_link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public ArrayList<String> getFeatures() {
        return features;
    }

    public void setFeatures(ArrayList<String> features) {
        this.features = features;
    }

    public int getReviews() {
        return reviews;
    }

    public void setReviews(int reviews) {
        this.reviews = reviews;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getReservation() {
        return reservation;
    }

    public void setReservation(int reservation) {
        this.reservation = reservation;
    }
}
