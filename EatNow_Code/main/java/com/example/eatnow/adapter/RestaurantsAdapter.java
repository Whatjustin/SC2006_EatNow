package com.example.eatnow.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eatnow.R;
import com.example.eatnow.ShowRestaurant;
import com.example.eatnow.model.Restaurant;
import com.example.eatnow.utility.ImageURLDownload;
import com.example.eatnow.utility.ListConverter;

import java.util.ArrayList;

/*
    Represents the Adapter for Restaurants
 */
public class RestaurantsAdapter extends RecyclerView.Adapter<RestaurantsAdapter.RestaurantsViewHolder> {

    // CONSTANT to limit number of categories shown to prevent text from spilling
    static final int MAX_CATEGORIES = 5;
    // Context of activity to display restaurants
    Context context;
    // List of restaurants
    ArrayList<Restaurant> restaurants;

    // Constructor to set context
    public RestaurantsAdapter(Context context) {
        this.context = context;
    }

    // Mutators and Accessors
    public ArrayList<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(ArrayList<Restaurant> restaurants) {
        this.restaurants = restaurants;
        notifyDataSetChanged();
    }

    // Construct view holder
    @NonNull
    @Override
    public RestaurantsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurants_item, parent, false);
        return new RestaurantsAdapter.RestaurantsViewHolder(view);
    }

    // Setting view holder parameters
    @Override
    public void onBindViewHolder(@NonNull RestaurantsAdapter.RestaurantsViewHolder holder, int position) {
        // Get Restaurant based on position
        Restaurant restaurant = restaurants.get(position);
        // If there is no user
        if (restaurant == null) {
            return;
        }
        // Set ImageView based on Restaurant's image url
        if (!restaurant.getImage_link().isEmpty()) {
            try {
                new ImageURLDownload(holder.imageView).execute(restaurant.getImage_link());
            } catch (Exception e) {
            }
        }
        // Set TextView based on Restaurant's name
        holder.restaurantName.setText(restaurant.getName());
        // Set TextView based on Restaurant's reviews
        holder.reviewCount.setText(String.valueOf(restaurant.getReviews()));
        // Set TextView based on Restaurant's first 5 features
        ArrayList<String> categories = new ArrayList<>();
        for (int i = 0; i < MAX_CATEGORIES; i++) {
            try {
                categories.add(restaurant.getCategories().get(i));
            } catch (Exception e) {
                break;
            }
        }
        holder.features.setText(ListConverter.convertStringListToString(categories));
        // Set Rating Bar based on Restaurant's ratings
        holder.rating.setRating(restaurant.getRating());
    }

    // Get number of items in adapter
    @Override
    public int getItemCount() {
        if (restaurants != null) {
            return restaurants.size();
        }
        return 0;
    }

    // ViewHolder class for restaurants
    public class RestaurantsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // UIs
        ImageView imageView;
        TextView restaurantName;
        TextView reviewCount;
        TextView features;
        RatingBar rating;

        public RestaurantsViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.restaurant_image);
            restaurantName = itemView.findViewById(R.id.restaurant_name);
            reviewCount = itemView.findViewById(R.id.restaurant_review_count);
            features = itemView.findViewById(R.id.restaurant_feat);
            rating = itemView.findViewById(R.id.restaurant_rating);

            // To set item as clickable
            itemView.setOnClickListener(this);
        }

        // If Item is clicked
        @Override
        public void onClick(View view) {
            int id = view.getId();

            if (id == itemView.getId()) {
                // Get selected restaurant
                Restaurant selectedRestaurant = restaurants.get(getAdapterPosition());

                // Configuring to change fragment to show restaurant
                FragmentActivity activity = (FragmentActivity) (context);
                FragmentManager fm = activity.getSupportFragmentManager();
                FrameLayout contentView = (FrameLayout) activity.findViewById(R.id.fragment_container);
                Fragment showRestaurant = new ShowRestaurant(selectedRestaurant);
                fm.beginTransaction()
                        .replace(contentView.getId(), showRestaurant, "SHOW_RESTAURANT")
                        .addToBackStack(null)
                        .commit();
            }
        }
    }
}
