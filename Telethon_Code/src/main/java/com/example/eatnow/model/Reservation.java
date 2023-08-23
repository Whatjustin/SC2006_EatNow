package com.example.eatnow.model;

public class Reservation {

    private int reservation_id;
    private long reservation_date;
    private int pax;
    private long creation_date;
    private int account_id;
    private int restaurant_id;

    public Reservation(int reservation_id, long reservation_date, int pax, long creation_date, int account_id, int restaurant_id) {
        this.reservation_id = reservation_id;
        this.reservation_date = reservation_date;
        this.pax = pax;
        this.creation_date = creation_date;
        this.account_id = account_id;
        this.restaurant_id = restaurant_id;
    }

    public int getReservation_id() {
        return reservation_id;
    }

    public void setReservation_id(int reservation_id) {
        this.reservation_id = reservation_id;
    }

    public long getReservation_date() {
        return reservation_date;
    }

    public void setReservation_date(long reservation_date) {
        this.reservation_date = reservation_date;
    }

    public int getPax() {
        return pax;
    }

    public void setPax(int pax) {
        this.pax = pax;
    }

    public long getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(long creation_date) {
        this.creation_date = creation_date;
    }

    public int getAccount_id() {
        return account_id;
    }

    public void setAccount_id(int account_id) {
        this.account_id = account_id;
    }

    public int getRestaurant_id() {
        return restaurant_id;
    }

    public void setRestaurant_id(int restaurant_id) {
        this.restaurant_id = restaurant_id;
    }
}
