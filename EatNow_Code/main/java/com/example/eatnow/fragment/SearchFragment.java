package com.example.eatnow.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eatnow.FilterRestaurant;
import com.example.eatnow.R;
import com.example.eatnow.adapter.RestaurantsAdapter;
import com.example.eatnow.adapter.UsersAdapter;
import com.example.eatnow.databinding.FragmentSearchBinding;
import com.example.eatnow.model.Account;
import com.example.eatnow.model.Restaurant;
import com.example.eatnow.utility.ErrorPopUp;
import com.example.eatnow.utility.ListConverter;
import com.google.android.material.textfield.TextInputEditText;
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
import java.util.regex.Pattern;

/*
    Represents Fragment for search
    Upon creation, list of users and restaurants will be displayed.
 */
public class SearchFragment extends Fragment implements ErrorPopUp {
    // CONSTANT to check RequestCode for this fragment
    public static final int SEARCH_FRAGMENT = 11;
    // Limit max number of restaurants to show
    static final int MAX_RESTAURANT_VIEW_LIMIT = 5;
    // Recycler / adapter for accounts
    RecyclerView usersRecycler = null;
    UsersAdapter usersAdapter = null;
    // To store list of all users
    ArrayList<Account> users;
    // Recycler / adapter for restaurants
    RecyclerView restaurantsRecycler = null;
    RestaurantsAdapter restaurantsAdapter = null;
    // To store list of all restaurants
    ArrayList<Restaurant> restaurants;
    // To store list of filtered restaurants
    ArrayList<Restaurant> filteredRestaurants;
    // To check whether database have retrieved the necessary information
    boolean retrievedRestaurants = false;
    // Store list of filterable categories and areas. This can be extracted from database.
    ArrayList<String> categories = null;
    ArrayList<String> areas = null;
    // Setting up limit for restaurants' view (Required to not cause memory crash since we are getting large data)
    // More restaurants will be shown upon reaching the end of the list.
    // Store counter for restaurants to limit number of restaurants shown
    int restaurantCounter;
    // To check if counter is refreshed
    boolean refreshed = true;
    // UIs
    TextInputEditText searchText;
    Button filterButton;

    FragmentSearchBinding binding;

    // Called when view is created
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setting up filter button to filter restaurants
        filterButton = root.findViewById(R.id.button_search_filter);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if data has been retrieved
                if (categories != null && areas != null) {
                    // Configuring filter button to call dialog fragment for filtering restaurants.
                    FragmentManager fm = getFragmentManager();
                    Fragment ft = fm.findFragmentById(R.id.fragment_container);
                    fm = getActivity().getSupportFragmentManager();

                    FilterRestaurant filterRestaurant = new FilterRestaurant(areas, categories);
                    // Setting target fragment to pass data from Dialog Fragment to this Fragment.
                    filterRestaurant.setTargetFragment(ft, SEARCH_FRAGMENT);
                    filterRestaurant.show(fm, "Filter Restaurant");
                } else {
                    errorPopUp("Error", "Please wait for data to load.");
                }
            }
        });

        // Setting up search button to search users / restaurants
        searchText = root.findViewById(R.id.search_text);
        searchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                // If the event is a key-down event on the "enter" button
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                    // Check if data for restaurants has been retrieved.
                    if (retrievedRestaurants) {
                        // Get user's input
                        String search = searchText.getText().toString();
                        // Filter users based on search
                        searchUsers(search);
                        // Filter restaurants based on search
                        searchRestaurants(search);
                    } else {
                        // if data has not been retrieved
                        errorPopUp("Error", "Please wait for data to load.");
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });
        return root;
    }

    // Called after view is created
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Instantiate Adapter for users
        usersAdapter = new UsersAdapter(this.getContext());
        // Extract accounts data from database
        extractUsers();
        // Configuring accounts recycler view
        usersRecycler = view.findViewById(R.id.recycler_view_search_user);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        usersRecycler.setLayoutManager(layoutManager);
        usersRecycler.setAdapter(usersAdapter);

        // Instantiate Adapter for restaurants
        restaurantsAdapter = new RestaurantsAdapter(this.getContext());
        // Extract restaurants data from database
        extractRestaurants();
        // Configuring restaurants recycler view
        restaurantsRecycler = view.findViewById(R.id.recycler_view_search_restaurant);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        restaurantsRecycler.setLayoutManager(layoutManager2);
        restaurantsRecycler.setAdapter(restaurantsAdapter);

        // Extract filters for restaurants
        extractAreas();
        extractRestaurantCategories();
    }

    // Scroll listener only works in onResume().
    @Override
    public void onResume() {
        super.onResume();
        // To show more restaurants in restaurants adapter once recycler view has reached the end of the list.
        restaurantsRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Load more restaurants into the adapter.
                    loadRestaurantsInAdapter();
                }
            }
        });
    }

    // Called from other fragments (i.e.e FilterRestaurant dialog fragment) to filter restaurants
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String area = "";
        String category = "";
        String feature = "";

        if (requestCode == SEARCH_FRAGMENT) {
            if (resultCode == FilterRestaurant.RESTAURANT_FILTER) {
                // Get selected filters and filter restaurants based on filters
                area = data.getExtras().getString("area");
                category = data.getExtras().getString("category");
                feature = data.getExtras().getString("feature");
                filterRestaurants(area, category, feature);
            } else if (resultCode == FilterRestaurant.RESET_RESTAURANT_FILTER) {
                filterRestaurants(area, category, feature);
            }
        }
    }

    // Filter restaurants based on filter
    private void filterRestaurants(String area, String category, String feature) {
        // Reset adapter
        refreshed = true;
        restaurantsAdapter.setRestaurants(new ArrayList<>());
        // Intialise list of filtered restaurants
        filteredRestaurants = new ArrayList<>();
        // To check if filter criterias have been met
        boolean matchArea;
        boolean matchCategory;
        boolean matchFeature;

        for (Restaurant restaurant : restaurants) {
            // Set as false at the start
            matchArea = false;
            matchCategory = false;
            matchFeature = false;
            // Check for area filter
            if (area.isEmpty()) {
                matchArea = true;
            } else {
                if (Pattern.compile(Pattern.quote(area), Pattern.CASE_INSENSITIVE).matcher(restaurant.getArea()).find()) {
                    matchArea = true;
                }
            }
            // Check for category filter
            if (category.isEmpty()) {
                matchCategory = true;
            } else {
                if (restaurant.getCategories().contains(category)) {
                    matchCategory = true;
                }
            }
            // Check for feature filter. For now, there is only reservable / not reservable
            if (feature.isEmpty()) {
                matchFeature = true;
            } else {
                if (feature.equals("Reservable")) {
                    if (restaurant.getReservation() == 1) {
                        matchFeature = true;
                    }
                } else {
                    if (restaurant.getReservation() == 0) {
                        matchFeature = true;
                    }
                }
            }
            // If all 3 conditions are met, add to filtered lists.
            if (matchArea && matchCategory && matchFeature) {
                filteredRestaurants.add(restaurant);
            }
        }
        // set restaurants adapter.
        loadRestaurantsInAdapter();
    }

    // To extract filterable areas from database
    private void extractAreas() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("unique_areas");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                areas = new ArrayList<>();
                String area;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    area = snapshot.child("area").getValue().toString();
                    areas.add(area);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // To extract filterable categories from database
    private void extractRestaurantCategories() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("restaurant_categories");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categories = new ArrayList<>();
                String category;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    category = snapshot.child("category").getValue().toString();
                    categories.add(category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // To filter accounts based on user's input in search function
    private void searchUsers(String search) {
        ArrayList<Account> filteredUsers = new ArrayList<>();
        for (Account user : users) {
            if (Pattern.compile(Pattern.quote(search), Pattern.CASE_INSENSITIVE).matcher(user.getUsername()).find()) {
                filteredUsers.add(user);
            }
        }
        usersAdapter.setUsers(filteredUsers);
    }

    // To filter restaurants based on user's input in search function
    private void searchRestaurants(String search) {
        // reset adapter
        refreshed = true;
        restaurantsAdapter.setRestaurants(new ArrayList<>());

        filteredRestaurants = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            if (Pattern.compile(Pattern.quote(search), Pattern.CASE_INSENSITIVE).matcher(restaurant.getName()).find()) {
                filteredRestaurants.add(restaurant);
            }
        }
        // set restaurants adapter.
        loadRestaurantsInAdapter();
    }

    // To extract list of accounts from database
    private void extractUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("accounts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users = new ArrayList<>();

                int account_id;
                byte[] profile;
                String username;
                ArrayList<Integer> followers;
                ArrayList<Integer> followings;

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

                    dummy = snapshot.child("followers").getValue();
                    if (dummy != null) {
                        followers = ListConverter.convertStringToInts(dummy.toString());
                    } else {
                        followers = new ArrayList<>();
                    }

                    dummy = snapshot.child("followings").getValue();
                    if (dummy != null) {
                        followings = ListConverter.convertStringToInts(dummy.toString());
                    } else {
                        followings = new ArrayList<>();
                    }

                    users.add(new Account(account_id, profile, username, followers, followings));
                }
                // sort users based on followers
                users = sortAccounts(users);
                // Set adapter
                usersAdapter.setUsers(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Sort accounts based on number of followers
    private ArrayList<Account> sortAccounts(ArrayList<Account> accounts) {
        Comparator<Account> FollowersComparator = new Comparator<Account>() {
            @Override
            public int compare(Account t0, Account t1) {
                return Integer.compare(t1.getFollowers().size(), t0.getFollowers().size());
            }
        };
        Collections.sort(accounts, FollowersComparator);
        return accounts;
    }

    // To extract list of restaurants from database (Warning: Large Data)
    private void extractRestaurants() {

        restaurants = new ArrayList<>();
        filteredRestaurants = new ArrayList<>();

        Query query;

        query = FirebaseDatabase.getInstance().getReference().child("restaurants").orderByChild("rating");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Restaurant restaurant;

                int restaurant_id;
                String restaurant_image;
                String name;
                String address;
                String area;
                String website;
                String phone;
                ArrayList<String> categories;
                ArrayList<String> features;
                int reviews;
                float rating;
                int reservation;

                Object dummy;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    restaurant_id = snapshot.child("restaurant_id").getValue(int.class);

                    dummy = snapshot.child("image_link").getValue();
                    if (dummy != null) {
                        restaurant_image = dummy.toString();
                    } else {
                        restaurant_image = "";
                    }

                    name = snapshot.child("name").getValue().toString();

                    dummy = snapshot.child("address").getValue();
                    if (dummy != null) {
                        address = dummy.toString();
                    } else {
                        address = "";
                    }

                    area = snapshot.child("area").getValue().toString();

                    dummy = snapshot.child("website").getValue();
                    if (dummy != null) {
                        website = dummy.toString();
                    } else {
                        website = "";
                    }

                    dummy = snapshot.child("phone").getValue();
                    if (dummy != null) {
                        phone = dummy.toString();
                    } else {
                        phone = "";
                    }

                    dummy = snapshot.child("category").getValue();
                    if (dummy != null) {
                        categories = ListConverter.convertStringToListByComma(dummy.toString());
                    } else {
                        categories = new ArrayList<>();
                    }

                    dummy = snapshot.child("features").getValue();
                    if (dummy != null) {
                        features = ListConverter.convertStringToListByComma(dummy.toString());
                    } else {
                        features = new ArrayList<>();
                    }

                    reviews = snapshot.child("reviews").getValue(int.class);

                    rating = snapshot.child("rating").getValue(float.class);

                    reservation = snapshot.child("reservation").getValue(int.class);

                    restaurant = new Restaurant(restaurant_id, restaurant_image, name, address, area, website, phone, categories, features, reviews, rating, reservation);
                    restaurants.add(restaurant);
                }
                // Sort Restaurants based on ratings
                restaurants = sortRestaurants(restaurants);
                // Set both restaurants (Which is a reference) and filtered restaurants to be the same.
                filteredRestaurants = new ArrayList<>(restaurants);
                // To show that the data has been retrieved
                retrievedRestaurants = true;
                // Setting up restaurants adapter
                loadRestaurantsInAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // To limit view of adapter to MAX_RESTAURANT_VIEW_LIMIT at a time
    // (Required to not cause memory crash since we are getting large data)
    private void loadRestaurantsInAdapter() {
        // If to be refreshed, reset counter
        if (refreshed) {
            restaurantCounter = 0;
            refreshed = false;
        }
        // stop if counter exceeds size
        if (restaurantCounter >= filteredRestaurants.size()) {
            return;
        }
        // Add counter
        restaurantCounter += MAX_RESTAURANT_VIEW_LIMIT;
        // Get sublist of arraylist to be displayed in adapter
        ArrayList<Restaurant> toBeDisplayed = new ArrayList<>(filteredRestaurants.subList(0, Math.min(restaurantCounter, filteredRestaurants.size())));
        // Set adapter
        restaurantsAdapter.setRestaurants(toBeDisplayed);
    }

    // Sort list of restaurants based on rating
    private ArrayList<Restaurant> sortRestaurants(ArrayList<Restaurant> restaurants) {
        Comparator<Restaurant> RatingsComparator = new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant t0, Restaurant t1) {
                return Float.compare(t1.getRating(), t0.getRating());
            }
        };
        Collections.sort(restaurants, RatingsComparator);
        return restaurants;
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