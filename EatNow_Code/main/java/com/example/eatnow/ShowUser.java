package com.example.eatnow;

import android.app.AlertDialog;
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
import com.example.eatnow.utility.ImageConverter;
import com.example.eatnow.utility.ListConverter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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

public class ShowUser extends Fragment implements ErrorPopUp {

    // CONSTANTs to check RequestCode for this fragment
    public static final int SHOW_USER = 22;
    // Store selected user details
    Account user;
    // UIs
    ImageView backButton;
    TextView userName;
    CircleImageView userImage;
    TextView followers;
    TextView followings;
    MaterialButton followBtn;
    TextView reviewCount;
    // Recycler / adapter for reviews for this user
    RecyclerView reviewsRecycler = null;
    ReviewsAdapter reviewsAdapter = null;
    ArrayList<Review> reviews;
    // Temporary store dictionary of restaurant ids with restaurants
    Map<Integer, Restaurant> restaurantMap;

    boolean followed;
    boolean change = false;

    public ShowUser(Account user) {
        this.user = user;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.user_show, container, false);
        // Set back button as clickable to go back search fragment (i.e. where the fragment was generated from)
        backButton = view.findViewById(R.id.user_back_img);
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
        // Setting up UIs to display details of user
        userName = view.findViewById(R.id.username);
        userImage = view.findViewById(R.id.profile_image);
        followers = view.findViewById(R.id.userFollowers);
        followings = view.findViewById(R.id.userFollowings);

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

        followBtn = view.findViewById(R.id.follow_button);
        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UserAccount.getInstance() == null) {
                    errorPopUp("Login Required.",
                            "Please log in or register with us to follow. " +
                                    "You may log in or register under the profile tab.");
                } else {
                    UserAccount userAccount = UserAccount.getInstance();

                    followed = user.getFollowers().contains(userAccount.getAccount_id());

                    followBtn.setSelected(followed);

                    if (user.getAccount_id() != userAccount.getAccount_id()) {
                        change = true;

                        if (followed) {
                            user.getFollowers().remove(Integer.valueOf(userAccount.getAccount_id()));
                            userAccount.getFollowings().remove(Integer.valueOf(user.getAccount_id()));
                            followed = false;
                        } else {
                            user.getFollowers().add(userAccount.getAccount_id());
                            userAccount.getFollowings().add(user.getAccount_id());
                            followed = true;
                        }

                        followBtn.setSelected(followed);

                        updateAccountInDatabase(user);
                        updateOwnAccountInDatabase(userAccount);

                        followers.setText(String.valueOf(user.getFollowers().size()));
                    } else {
                        errorPopUp("Error", "You cannot follow your own profile.");
                    }
                }
            }
        });

        reviewCount = view.findViewById(R.id.txtReviewCount);

        return view;
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
        reviewsAdapter.setAccount(user);
    }

    // Called from other fragments
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHOW_USER) {
            if (resultCode == ShowReview.REVIEW_LIKE_CHANGE) {
                // reset adapter after liking review
                reviewsAdapter.setReviews(reviews);
            }
        }
    }

    private void updateAccountInDatabase(Account account) {
        int account_id = account.getAccount_id();

        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference().child("accounts").child(String.valueOf(account_id - 1)).child("followers");

        reference.setValue(ListConverter.convertIntsToString(account.getFollowers()));
    }

    private void updateOwnAccountInDatabase(Account userAccount) {
        int account_id = userAccount.getAccount_id();

        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference().child("accounts").child(String.valueOf(account_id - 1)).child("followings");

        reference.setValue(ListConverter.convertIntsToString(userAccount.getFollowings()));
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
    public void errorPopUp(String title, String error) {
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(error)
                .setNegativeButton(android.R.string.ok, null)
                .show();
    }
}
