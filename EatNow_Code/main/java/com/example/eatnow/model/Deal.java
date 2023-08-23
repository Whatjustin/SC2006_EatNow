package com.example.eatnow.model;

/*
    Represents the entity class for Deal
    In a deal, only cuisine and locations can be empty.
 */
public class Deal {
    // Image of Deal (Stored as ASCII byte array)
    private byte[] image;
    // Deal message
    private String text;
    // Brand of Deal
    private String brand;
    // Type of Cuisine of Deal
    private String cuisine;
    // Start date of deal (Stored as Unix Timestamp in SGT)
    private long start_date;
    // End date of deal (Stored as Unix Timestamp in SGT)
    private long end_date;
    // String of list of locations that have the deal
    private String locations;

    // Constructor
    public Deal(byte[] image, String text, String brand, String cuisine, long start_date, long end_date, String locations) {
        this.image = image;
        this.text = text;
        this.brand = brand;
        this.cuisine = cuisine;
        this.start_date = start_date;
        this.end_date = end_date;
        this.locations = locations;
    }

    // Accessors and Mutators
    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public long getStart_date() {
        return start_date;
    }

    public void setStart_date(long start_date) {
        this.start_date = start_date;
    }

    public long getEnd_date() {
        return end_date;
    }

    public void setEnd_date(long end_date) {
        this.end_date = end_date;
    }

    public String getLocations() {
        return locations;
    }

    public void setLocations(String locations) {
        this.locations = locations;
    }
}
