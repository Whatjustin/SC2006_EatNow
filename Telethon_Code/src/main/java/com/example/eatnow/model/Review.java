package com.example.eatnow.model;

import java.util.ArrayList;

public class Review {

    // Review unique id
    private int review_id;
    // Image of Review (Stored as ASCII byte array)
    private byte[] image;
    // Review's caption (Title)
    private String caption;
    // Review's text
    private String text;
    // Account id of account that has submitted the review
    private int account_id;
    // Restaurant id of restaurant that the review is being made for
    private int restaurant_id;
    // Rating of restaurant given by review
    private float rating;
    // List of account ids that have liked the review
    private ArrayList<Integer> likes;
    // Start date of review (Stored as Unix Timestamp in SGT)
    private int date;

    // Constructor
    public Review(int review_id, byte[] image, String caption, String text, int account_id, int restaurant_id, float rating, ArrayList<Integer> likes, int date) {
        this.review_id = review_id;
        this.image = image;
        this.caption = caption;
        this.text = text;
        this.account_id = account_id;
        this.restaurant_id = restaurant_id;
        this.rating = rating;
        this.likes = likes;
        this.date = date;
    }

    // Accessors and Mutators
    public int getReview_id() {
        return review_id;
    }

    public void setReview_id(int review_id) {
        this.review_id = review_id;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getRestaurant_id() {
        return restaurant_id;
    }

    public void setRestaurant_id(int restaurant_id) {
        this.restaurant_id = restaurant_id;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getAccount_id() {
        return account_id;
    }

    public void setAccount_id(int account_id) {
        this.account_id = account_id;
    }

    public ArrayList<Integer> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<Integer> likes) {
        this.likes = likes;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }
}

