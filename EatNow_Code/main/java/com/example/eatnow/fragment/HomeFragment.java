package com.example.eatnow.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eatnow.R;
import com.example.eatnow.ShowReview;
import com.example.eatnow.adapter.ReviewsAdapter;
import com.example.eatnow.databinding.FragmentHomeBinding;
import com.example.eatnow.model.Account;
import com.example.eatnow.model.Restaurant;
import com.example.eatnow.model.Review;
import com.example.eatnow.model.UserAccount;
import com.example.eatnow.utility.ErrorPopUp;
import com.example.eatnow.utility.ListConverter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/*
    Represents Fragment for Home
    Upon creation, a review feed will be displayed, sorted by likes.
 */
public class HomeFragment extends Fragment implements ErrorPopUp {

    // CONSTANT to check RequestCode for this fragment
    public static final int HOME_FRAGMENT = 8;
    // Recycler / adapter for reviews
    RecyclerView reviewsRecycler = null;
    ReviewsAdapter reviewsAdapter = null;
    // Temporary store dictionary of account ids with accounts
    Map<Integer, Account> accountMap;
    // Temporary store dictionary of restaurant ids with restaurants
    Map<Integer, Restaurant> restaurantMap = null;
    // Store  all reviews
    ArrayList<Review> reviews;
    FragmentHomeBinding binding;
    // Store review filters
    ArrayList<String> filters;
    ArrayList<String> availableFilters; // This is for clickable filters
    // Elements for filter
    AutoCompleteTextView reviewFilter;
    ArrayAdapter<String> filterAdapter;
    String filter = "";
    // To check whether database have retrieved the necessary information
    boolean retrievedData = false;

    // Called when view is created
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    // Called after view is created
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set to Home view type
        int viewType = 1;
        // Instantiate Adapter for reviews
        reviewsAdapter = new ReviewsAdapter(this.getContext(), viewType);
        // Extract accounts data from Firebase
        extractAccounts();
        // Extract reviews data from Firebase
        extractReviews();
        // Set Reviews Recycler View to a single column
        reviewsRecycler = view.findViewById(R.id.recycler_view_home);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        reviewsRecycler.setLayoutManager(gridLayoutManager);
        // Setting Adapter for Recycler view
        reviewsRecycler.setAdapter(reviewsAdapter);
        // Setting up UI for filters. To be continued in onResume() method.
        reviewFilter = view.findViewById(R.id.review_text_filter);
    }

    /*
     Bug: Required to put ArrayAdapter with DropDownBox in onResume method.
          Otherwise, navigating from one fragment to another will glitch the DropDownBox
     */
    @Override
    public void onResume() {
        super.onResume();
        // Setting up all filters. Filters are limited to All and User's Followers
        filters = new ArrayList<>();
        filters.add("All");
        filters.add("Followings");
        // On start up, set clickable filter to the rest except All.
        availableFilters = new ArrayList<>();
        availableFilters.addAll(filters);
        availableFilters.remove("All");
        // On start up, set current filter to all, since the fragment is destroyed and recreated everytime.
        reviewFilter.setText("All", false);
        // Setting up filter adapter to display available filters to be selected
        filterAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.dropdown_item, availableFilters);
        reviewFilter.setAdapter(filterAdapter);
        // Setting up UI for filter to be clickable
        reviewFilter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Get selected filter
                filter = adapterView.getItemAtPosition(i).toString();
                // Check what filter it is
                if (filter.equals("All")) {
                    // reset reviewsAdapter reviews to All
                    reviewsAdapter.setReviews(reviews);
                } else {
                    // set reviewsAdapter to display filtered reviews
                    if (UserAccount.getInstance() == null) {
                        // If user has not logged in -> no user account.
                        errorPopUp("Login Required.",
                                "Please log in or register with us. " +
                                        "You may log in or register under the profile tab.");
                        // empty reviews in reviewsAdapter
                        reviewsAdapter.setReviews(new ArrayList<>());
                    } else {
                        // If user has logged in. Filter reviews based on user's followings
                        // Get user's followings
                        ArrayList<Integer> followings = UserAccount.getInstance().getFollowings();
                        // Instantiate filteredReviews as empty
                        ArrayList<Review> filteredReviews = new ArrayList<>();
                        // Add review in filteredReviews if review is posted by user's followings
                        for (Review review : reviews) {
                            if (followings.contains(review.getAccount_id())) {
                                filteredReviews.add(review);
                            }
                        }
                        // Set filtered reviews in adapter
                        reviewsAdapter.setReviews(filteredReviews);
                    }
                }
                // Reset available filters to the others
                availableFilters = new ArrayList<>();
                availableFilters.addAll(filters);
                availableFilters.remove(filter);
                // clear filter adapter and add available filters for clicking
                filterAdapter.clear();
                filterAdapter.addAll(availableFilters);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Called from other fragments to change number of likes
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == HOME_FRAGMENT) {
            if (resultCode == ShowReview.REVIEW_LIKE_CHANGE) {
                // Get review id that has been changed and the number of likes
                int review_id = data.getExtras().getInt("review_id");
                ArrayList<Integer> likes = data.getExtras().getIntegerArrayList("likes");
                // Change number of likes in review in adapter
                for (Review review : reviewsAdapter.getReviews()) {
                    if (review.getReview_id() == review_id) {
                        review.setLikes(likes);
                        break;
                    }
                }
                // Notify changes in adapter
                reviewsAdapter.notifyDataSetChanged();
            }
        }
    }

    // To extract accounts from database
    // Note: Async Task
    public void extractAccounts() {
        Query query = FirebaseDatabase.getInstance().getReference().child("accounts");
        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                accountMap = new HashMap<>();

                int account_id;
                byte[] profile;
                String username;

                Account account;

                Object dummy;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get necessary information
                    account_id = snapshot.child("account_id").getValue(int.class);
                    dummy = snapshot.child("profile").getValue();
                    if (dummy != null) {
                        profile = dummy.toString().getBytes(StandardCharsets.UTF_8);
                    } else {
                        profile = null;
                    }
                    username = snapshot.child("username").getValue().toString();
                    // Construct account object and add in hashmap
                    account = new Account();
                    account.setAccount_id(account_id);
                    account.setProfile(profile);
                    account.setUsername(username);
                    accountMap.put(account_id, account);
                }
                // Setup account hashmap in adapter
                reviewsAdapter.setAccountMap(accountMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // To extract reviews from database
    // Note: Async Task
    public void extractReviews() {
        Query query = FirebaseDatabase.getInstance().getReference().child("reviews");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reviews = new ArrayList<Review>();

                int review_id;
                byte[] image;
                String caption;
                String text;
                int account_id;
                int restaurant_id;
                float rating;
                ArrayList<Integer> likes;
                int date;

                Object dummy;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get necessary information
                    review_id = snapshot.child("review_id").getValue(int.class);

                    dummy = snapshot.child("image").getValue();
                    if (dummy != null) {
                        image = dummy.toString().getBytes(StandardCharsets.UTF_8);
                    } else {
                        image = null;
                    }

                    caption = snapshot.child("caption").getValue().toString();

                    dummy = snapshot.child("text").getValue();
                    if (dummy != null) {
                        text = dummy.toString();
                    } else {
                        text = "";
                    }

                    account_id = snapshot.child("account_id").getValue(int.class);

                    restaurant_id = snapshot.child("restaurant_id").getValue(int.class);

                    rating = snapshot.child("rating").getValue(float.class);

                    dummy = snapshot.child("likes").getValue();
                    if (dummy != null) {
                        likes = ListConverter.convertStringToInts(dummy.toString());
                    } else {
                        likes = new ArrayList<>();
                    }

                    date = snapshot.child("date").getValue(int.class);

                    // Construct review object and add into reviews list
                    reviews.add(new Review(review_id, image, caption, text, account_id, restaurant_id, rating, likes, date));
                }
                // Sort reviews based on likes
                reviews = sortReviews(reviews);
                // Extract Restaurant Map based on reviews
                extractRestaurants(reviews);
                // Set reviews in adapter
                reviewsAdapter.setReviews(reviews);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // To extract restaurants from firebase. Once completed, retrievedData will be set to true.
    // Note: Async Task
    public void extractRestaurants(ArrayList<Review> reviews) {
        // Get a list of unique restaurant ids to retrieve from database
        ArrayList<Integer> restaurantIDs = new ArrayList<>();
        for (Review review : reviews) {
            if (!restaurantIDs.contains(review.getRestaurant_id())) {
                restaurantIDs.add(review.getRestaurant_id());
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
                reviewsAdapter.setRestaurantMap(restaurantMap);
                // Set retrieved data as true
                retrievedData = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // To sort list of reviews based on likes
    private ArrayList<Review> sortReviews(ArrayList<Review> reviews) {

        Comparator<Review> LikesComparator = new Comparator<Review>() {
            @Override
            public int compare(Review t0, Review t1) {
                return Integer.compare(t1.getLikes().size(), t0.getLikes().size());
            }
        };

        Collections.sort(reviews, LikesComparator);

        return reviews;
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