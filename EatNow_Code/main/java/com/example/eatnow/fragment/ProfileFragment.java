package com.example.eatnow.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eatnow.AccountSettings;
import com.example.eatnow.R;
import com.example.eatnow.adapter.ReviewsAdapter;
import com.example.eatnow.databinding.FragmentProfileBinding;
import com.example.eatnow.model.Account;
import com.example.eatnow.model.Restaurant;
import com.example.eatnow.model.Review;
import com.example.eatnow.model.UserAccount;
import com.example.eatnow.utility.ImageConverter;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    // CONSTANTs to check RequestCode for this fragment
    public static final int PROFILE_FRAGMENT = 24;
    // UIs
    TextView userName;
    CircleImageView userImage;
    TextView followers;
    TextView followings;
    TextView reviewCount;
    ImageView settingsBtn;
    // Store user details
    Account user = UserAccount.getInstance();
    // Recycler / adapter for reviews for this user
    RecyclerView reviewsRecycler = null;
    ReviewsAdapter reviewsAdapter = null;
    ArrayList<Review> reviews;
    // Temporary store dictionary of restaurant ids with restaurants
    Map<Integer, Restaurant> restaurantMap;

    private FragmentProfileBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Setting up UIs
        userName = root.findViewById(R.id.username);
        userImage = root.findViewById(R.id.profile_image);
        followers = root.findViewById(R.id.userFollowers);
        followings = root.findViewById(R.id.userFollowings);

        userName.setText(user.getUsername());
        if (user.getProfile() != null) {
            try {
                Bitmap bmp = ImageConverter.convertASCIItoBitmap(user.getProfile());
                userImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false));
            } catch (Exception e) {
            }
        }
        followers.setText(String.valueOf(user.getFollowers().size()));
        followings.setText(String.valueOf(user.getFollowings().size()));

        settingsBtn = root.findViewById(R.id.settings_image);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Configure Dialog Fragment (Pop-up) for settings
                FragmentManager fm = getFragmentManager();
                Fragment ft = fm.findFragmentById(R.id.fragment_container);

                fm = getActivity().getSupportFragmentManager();

                DialogFragment accountSettings = new AccountSettings();
                // Setting target fragment to pass data from Dialog Fragment to this Fragment.
                accountSettings.setTargetFragment(ft, PROFILE_FRAGMENT);
                accountSettings.show(fm, "Settings");
            }
        });

        reviewCount = root.findViewById(R.id.txtReviewCount);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Account view type
        int viewType = 3;
        // Instantiate Adapter for reviews
        reviewsAdapter = new ReviewsAdapter(this.getContext(), viewType);
        // Extract reviews data from Firebase
        extractReviews();
        // Set Reviews Recycler View to a single column
        reviewsRecycler = view.findViewById(R.id.profileRecView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        reviewsRecycler.setLayoutManager(gridLayoutManager);
        // Setting Adapter for Recycler view
        reviewsRecycler.setAdapter(reviewsAdapter);
        // Set account in review adapter
        reviewsAdapter.setAccount((Account) user);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PROFILE_FRAGMENT) {
            if (resultCode == AccountSettings.CHANGE_PROFILE) {
                // Reset Image
                try {
                    Bitmap bmp = ImageConverter.convertASCIItoBitmap(user.getProfile());
                    userImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false));
                } catch (Exception e) {
                }
            } else if (resultCode == AccountSettings.LOG_OUT) {
                // Change to login fragment
                FragmentManager fm = getFragmentManager();

                LoginFragment loginFragment = new LoginFragment();

                fm.beginTransaction()
                        .replace(R.id.fragment_container, loginFragment)
                        .commit();
            }
        }
    }

    // To extract user's reviews from database
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

                    account_id = snapshot.child("account_id").getValue(int.class);

                    if (account_id != user.getAccount_id()) {
                        continue;
                    }

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
                // Set reviews in adapter
                reviewsAdapter.setReviews(reviews);
                // Extract restaurants based on reviews
                extractRestaurants(reviews);
                // Set text according to number of reviews
                reviewCount.setText(String.valueOf(reviews.size()));
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

    // To extract restaurants from firebase. Once completed, retrievedData will be set to true.
    // Note: Async Task
    public void extractRestaurants(ArrayList<Review> reviews) {
        // Get a list of unique restaurant ids to retrieve from database
        if (reviews.size() == 0) {
            restaurantMap = new HashMap<>();
            reviewsAdapter.setRestaurantMap(restaurantMap);
            return;
        }
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
}