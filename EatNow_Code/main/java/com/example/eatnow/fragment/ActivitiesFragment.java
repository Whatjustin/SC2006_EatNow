package com.example.eatnow.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eatnow.R;
import com.example.eatnow.adapter.ReservationsAdapter;
import com.example.eatnow.databinding.FragmentActivitiesBinding;
import com.example.eatnow.model.Reservation;
import com.example.eatnow.model.Restaurant;
import com.example.eatnow.model.UserAccount;
import com.example.eatnow.utility.ErrorPopUp;
import com.example.eatnow.utility.TimeConverter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivitiesFragment extends Fragment implements ErrorPopUp {

    FragmentActivitiesBinding binding;
    // Recycler / adapter for reservations
    RecyclerView reservationsRecycler = null;
    ReservationsAdapter reservationsAdapter = null;
    // Store all non-expired reservations
    ArrayList<Reservation> reservations = null;
    // Dictionary of restaurant ids with restaurants
    Map<Integer, Restaurant> restaurantMap = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentActivitiesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (UserAccount.getInstance() == null) {
            errorPopUp("Login Required", "Please login or register with us to view your activities. " +
                    "You may login or register with us under the profile tab");
        } else {
            // Instantiate Adapter for deals
            reservationsAdapter = new ReservationsAdapter(this.getContext());
            // Extract reservations from Database
            extractReservations();
            // Set Recycler View to a single column
            reservationsRecycler = view.findViewById(R.id.recycler_view_activities);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
            // Setting layout for recycler view
            reservationsRecycler.setLayoutManager(gridLayoutManager);
            // Setting Adapter for recycler view
            reservationsRecycler.setAdapter(reservationsAdapter);
        }
    }

    private void extractReservations() {
        Double currentTime = TimeConverter.getSGTUnixTS();
        // Query for only non-expired reservations
        Query query = FirebaseDatabase.getInstance().getReference().child("reservations").orderByChild("reservation_date").startAt(currentTime);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                reservations = new ArrayList<>();

                int account_id;
                long creation_date;
                int pax;
                long reservation_date;
                int reservation_id;
                int restaurant_id;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    account_id = snapshot.child("account_id").getValue(int.class);

                    if (account_id != UserAccount.getInstance().getAccount_id()) {
                        continue;
                    }

                    creation_date = snapshot.child("creation_date").getValue(long.class);

                    pax = snapshot.child("pax").getValue(int.class);

                    reservation_date = snapshot.child("reservation_date").getValue(long.class);

                    reservation_id = snapshot.child("reservation_id").getValue(int.class);

                    restaurant_id = snapshot.child("restaurant_id").getValue(int.class);

                    reservations.add(new Reservation(reservation_id, reservation_date, pax, creation_date, account_id, restaurant_id));
                }
                extractRestaurants(reservations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void extractRestaurants(ArrayList<Reservation> reservations) {
        // Get a list of unique restaurant ids to retrieve from database
        ArrayList<Integer> restaurantIDs = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (!restaurantIDs.contains(reservation.getRestaurant_id())) {
                restaurantIDs.add(reservation.getRestaurant_id());
            }
        }

        // Retrieve restaurants data based on restaurant ids
        Query query = FirebaseDatabase.getInstance().getReference().child("restaurants");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                restaurantMap = new HashMap<>();

                int restaurant_id;
                String restaurant_image;
                String name;
                Restaurant restaurant;

                Object dummy;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Retrieve restaurant id of each restaurant
                    restaurant_id = snapshot.child("restaurant_id").getValue(int.class);
                    // If restaurant id does not correspond to the restaurant, go to the next one.
                    if (!(restaurantIDs.contains(restaurant_id))) {
                        continue;
                    }
                    // Retrieve other necessary details
                    dummy = snapshot.child("image_link").getValue();
                    if (dummy != null) {
                        restaurant_image = dummy.toString();
                    } else {
                        restaurant_image = "";
                    }

                    name = snapshot.child("name").getValue().toString();

                    // Construct restaurant object and put into hashmap
                    restaurant = new Restaurant();
                    restaurant.setRestaurant_id(restaurant_id);
                    restaurant.setImage_link(restaurant_image);
                    restaurant.setName(name);
                    restaurantMap.put(restaurant_id, restaurant);
                }
                // Set restaurant hashmap in adapter
                reservationsAdapter.setRestaurantMap(restaurantMap);

                reservationsAdapter.setReservations(reservations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void errorPopUp(String title, String error) {
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(error)
                .setNegativeButton(android.R.string.ok, null)
                .show();
    }
}