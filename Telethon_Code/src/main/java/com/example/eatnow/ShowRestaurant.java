package com.example.eatnow;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eatnow.adapter.ReviewsAdapter;
import com.example.eatnow.fragment.SearchFragment;
import com.example.eatnow.model.Account;
import com.example.eatnow.model.Restaurant;
import com.example.eatnow.model.Review;
import com.example.eatnow.model.UserAccount;
import com.example.eatnow.utility.ErrorPopUp;
import com.example.eatnow.utility.ImageURLDownload;
import com.example.eatnow.utility.ListConverter;
import com.google.android.material.button.MaterialButton;
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
    Represents Fragment for displaying a restaurant
    Upon creation, details of selected restaurant is displayed
 */
public class ShowRestaurant extends Fragment implements ErrorPopUp {

    // CONSTANTs to check RequestCode for this fragment
    public static final int SHOW_RESTAURANT = 19;
    // Store selected restaurant details
    Restaurant restaurant;
    // UIs for editing
    TextView restaurantName;
    ImageView restaurantImage;
    TextView restaurantReviewCount;
    RatingBar restaurantRating;
    // UIs for clickables
    ImageView backButton;
    MaterialButton addReview;
    MaterialButton details;
    MaterialButton addReservation;
    MaterialButton order;
    // Recycler / adapter for reviews for this restaurant
    RecyclerView reviewsRecycler = null;
    ReviewsAdapter reviewsAdapter = null;
    ArrayList<Review> reviews;
    // Store list of accounts
    Map<Integer, Account> accountMap;

    // Constructor for fragment
    public ShowRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    // Called when view is created
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.restaurant_show, container, false);
        // Set back button as clickable to go back search fragment (i.e. where the fragment was generated from)
        backButton = view.findViewById(R.id.restaurant_back_ic);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Fragment searchFragment = new SearchFragment();

                FragmentManager fm = getActivity().getSupportFragmentManager();

                fm.beginTransaction()
                        .replace(R.id.fragment_container, searchFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        // Setting up UIs to display details of restaurant
        restaurantName = view.findViewById(R.id.restaurant_name);
        restaurantImage = view.findViewById(R.id.restaurant_image);
        restaurantReviewCount = view.findViewById(R.id.restaurant_review_count);
        restaurantRating = view.findViewById(R.id.rating_review_bar);

        restaurantName.setText(restaurant.getName());
        if (!restaurant.getImage_link().isEmpty()) {
            try {
                new ImageURLDownload(restaurantImage).execute(restaurant.getImage_link());
            } catch (Exception e) {
            }
        }
        restaurantReviewCount.setText(String.valueOf(restaurant.getReviews()));
        restaurantRating.setRating(restaurant.getRating());
        // Setting up clickable UIs
        // For adding review
        addReview = view.findViewById(R.id.add_review_button);
        addReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UserAccount.getInstance() == null) {
                    errorPopUp("Login Required",
                            "Please log in or register with to add a review. " +
                                    "You may log in or register under the profile tab.");
                } else {
                    Fragment ft = getActivity().getSupportFragmentManager().findFragmentByTag("SHOW_RESTAURANT");

                    FragmentManager fm = getActivity().getSupportFragmentManager();

                    DialogFragment addReview = new AddReview(restaurant);
                    // Setting target fragment to pass data from Dialog Fragment to this Fragment.
                    addReview.setTargetFragment(ft, SHOW_RESTAURANT);
                    addReview.show(fm, "Creating Review");
                }
            }
        });
        // For getting more details
        details = view.findViewById(R.id.details_button);
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Configure pop-up for show details
                FragmentManager fm = getActivity().getSupportFragmentManager();
                DialogFragment showDetails = new ShowRestaurantDetails(restaurant);
                showDetails.show(fm, "Showing restaurant details");
            }
        });
        // For adding reservation
        addReservation = view.findViewById(R.id.add_reservation_button);
        if (restaurant.getReservation() == 1) {
            addReservation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (UserAccount.getInstance() == null) {
                        errorPopUp("Login Required",
                                "Please log in or register with to add a reservation. " +
                                        "You may log in or register under the profile tab.");
                    } else {
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        DialogFragment addReservation = new AddReservation(restaurant);
                        addReservation.show(fm, "Creating Reservation");
                    }
                }
            });
        } else {
            addReservation.setVisibility(View.GONE);
        }

        order = view.findViewById(R.id.order_button);
        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UserAccount.getInstance() == null) {
                    errorPopUp("Login Required",
                            "Please log in or register with to order. " +
                                    "You may log in or register under the profile tab.");
                } else {
                    // TODO: order
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restaurant view type
        int viewType = 2;

        // Instantiate Adapter for reviews
        reviewsAdapter = new ReviewsAdapter(this.getContext(), viewType);
        // Set selected restaurant
        reviewsAdapter.setRestaurant(restaurant);
        // Extract accounts data from Firebase
        extractAccounts();
        // Extract reviews data from Firebase
        extractReviews();
        // Set Reviews Recycler View to a single column
        reviewsRecycler = view.findViewById(R.id.recycler_view_restaurant_reviews);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        reviewsRecycler.setLayoutManager(gridLayoutManager);
        // Setting Adapter for Recycler view
        reviewsRecycler.setAdapter(reviewsAdapter);
    }

    // Called from other fragments
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHOW_RESTAURANT) {
            if (resultCode == AddReview.ADD_REVIEW) {
                // reset text view after adding review
                restaurantReviewCount.setText(String.valueOf(restaurant.getReviews()));
                restaurantRating.setRating(restaurant.getRating());
            } else if (resultCode == ShowReview.REVIEW_LIKE_CHANGE) {
                // reset adapter after liking review
                reviewsAdapter.setReviews(reviews);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    // To extract accounts from firebase
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

                    account_id = snapshot.child("account_id").getValue(int.class);

                    dummy = snapshot.child("profile").getValue();
                    if (dummy != null) {
                        profile = dummy.toString().getBytes(StandardCharsets.UTF_8);
                    } else {
                        profile = null;
                    }

                    username = snapshot.child("username").getValue().toString();

                    account = new Account();
                    account.setAccount_id(account_id);
                    account.setProfile(profile);
                    account.setUsername(username);
                    accountMap.put(account_id, account);
                }
                reviewsAdapter.setAccountMap(accountMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // To extract reviews from firebase
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

                    restaurant_id = snapshot.child("restaurant_id").getValue(int.class);

                    if (restaurant.getRestaurant_id() == restaurant_id) {

                        review_id = snapshot.child("review_id").getValue(int.class);

                        dummy = snapshot.child("image").getValue();
                        if (dummy != null) {
                            image = dummy.toString().getBytes(StandardCharsets.UTF_8);
                        } else {
                            image = null;
                        }

                        caption = snapshot.child("caption").getValue().toString();

                        text = snapshot.child("text").getValue().toString();

                        account_id = snapshot.child("account_id").getValue(int.class);


                        rating = snapshot.child("rating").getValue(float.class);

                        dummy = snapshot.child("likes").getValue();
                        if (dummy != null) {
                            likes = ListConverter.convertStringToInts(dummy.toString());
                        } else {
                            likes = new ArrayList<>();
                        }

                        date = snapshot.child("date").getValue(int.class);

                        reviews.add(new Review(review_id, image, caption, text, account_id, restaurant_id, rating, likes, date));
                    }
                }

                reviews = sortReviews(reviews);

                reviewsAdapter.setReviews(reviews);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

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

    // Construct Error Pop Up
    @Override
    public void errorPopUp(String title, String error) {
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(error)
                .setNegativeButton(android.R.string.ok, null)
                .show();
    }
}
