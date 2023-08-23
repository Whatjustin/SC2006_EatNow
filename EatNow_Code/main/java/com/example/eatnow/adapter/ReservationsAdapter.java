package com.example.eatnow.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eatnow.R;
import com.example.eatnow.model.Reservation;
import com.example.eatnow.model.Restaurant;
import com.example.eatnow.utility.ImageURLDownload;
import com.example.eatnow.utility.TimeConverter;

import java.util.ArrayList;
import java.util.Map;

public class ReservationsAdapter extends RecyclerView.Adapter<ReservationsAdapter.ReservationsViewHolder> {

    // Context of activity to display reservations
    Context context;
    // List of reservations
    ArrayList<Reservation> reservations = null;

    // Dictionary of restaurant ids with restaurants
    Map<Integer, Restaurant> restaurantMap = null;

    public ReservationsAdapter(Context context) {
        this.context = context;
    }

    public ArrayList<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(ArrayList<Reservation> reservations) {
        this.reservations = reservations;
        notifyDataSetChanged();
    }

    public Map<Integer, Restaurant> getRestaurantMap() {
        return restaurantMap;
    }

    public void setRestaurantMap(Map<Integer, Restaurant> restaurantMap) {
        this.restaurantMap = restaurantMap;
    }

    // Construct view holder
    @NonNull
    @Override
    public ReservationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reservations_item, parent, false);
        return new ReservationsAdapter.ReservationsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationsViewHolder holder, int position) {
        // Get reservation based on position
        Reservation reservation = reservations.get(position);
        // If there is no reservation
        if (reservation == null) {
            return;
        }
        Restaurant selectedRestaurant = restaurantMap.get(reservation.getRestaurant_id());
        // Set ImageView based on Restaurant's image url
        if (!selectedRestaurant.getImage_link().isEmpty()) {
            try {
                new ImageURLDownload(holder.restaurantImage).execute(selectedRestaurant.getImage_link());
            } catch (Exception e) {
            }
        }
        // Set Restaurant's name
        holder.restaurantName.setText(selectedRestaurant.getName());
        // Set Reservation Date
        holder.reservationDate.setText(TimeConverter.convertUnixTStoStringFull(reservation.getReservation_date()));
        // Set Reservation Pax
        holder.reservationPax.setText(String.valueOf(reservation.getPax()));
        // Set Reservation Creation Date
        holder.creationDate.setText(TimeConverter.convertUnixTStoString(reservation.getCreation_date()));
    }

    // Get number of items in adapter
    @Override
    public int getItemCount() {
        if (reservations != null) {
            return reservations.size();
        }
        return 0;
    }

    // ViewHolder class for reservations
    public class ReservationsViewHolder extends RecyclerView.ViewHolder {

        // restaurant's image
        ImageView restaurantImage;
        // restaurant's name
        TextView restaurantName;
        // reservation's date
        TextView reservationDate;
        // reservation's pax
        TextView reservationPax;
        // reservation's creation date
        TextView creationDate;

        public ReservationsViewHolder(@NonNull View itemView) {
            super(itemView);

            restaurantImage = itemView.findViewById(R.id.restaurant_image);
            restaurantName = itemView.findViewById(R.id.restaurant_name);
            reservationDate = itemView.findViewById(R.id.reservation_date);
            reservationPax = itemView.findViewById(R.id.reservation_pax);
            creationDate = itemView.findViewById(R.id.reservation_date_created);
        }
    }
}
