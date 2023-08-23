package com.example.eatnow.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eatnow.FilterDeal;
import com.example.eatnow.R;
import com.example.eatnow.adapter.DealsAdapter;
import com.example.eatnow.databinding.FragmentDealsBinding;
import com.example.eatnow.model.Deal;
import com.example.eatnow.utility.ErrorPopUp;
import com.example.eatnow.utility.ImageConverter;
import com.example.eatnow.utility.TimeConverter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/*
    Represents Fragment for Deals
    Upon creation, deals will be displayed.
 */
public class DealsFragment extends Fragment implements ErrorPopUp {

    // CONSTANT to check RequestCode for this fragment
    public static final int DEAL_FRAGMENT = 1;
    // Recycler / adapter for deals
    RecyclerView dealsRecycler = null;
    DealsAdapter dealsAdapter = null;
    // Store all non-expired deals
    ArrayList<Deal> deals = null;

    Button filterButton;

    FragmentDealsBinding binding;

    // Called when view is created
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDealsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setting up filter button to filter deals
        filterButton = root.findViewById(R.id.button_deals_filter);
        filterButton.setOnClickListener(v -> {
            if (deals == null) {
                // If deals data has not been retrieved, display error message
                errorPopUp("Error", "Please wait for data to load.");
            } else {
                if (deals.size() == 0) {
                    // If there is currently no deals, display unable to filter
                    errorPopUp("Filter Unavailable", "There are no deals currently.");
                } else {
                    // If deals data has been retrieved and there are deals
                    // Configure Dialog Fragment (Pop-up) for filtering deals
                    FragmentManager fm = getFragmentManager();
                    Fragment ft = fm.findFragmentById(R.id.fragment_container);

                    fm = getActivity().getSupportFragmentManager();

                    DialogFragment filterDeal = FilterDeal.newInstance(deals);
                    // Setting target fragment to pass data from Dialog Fragment to this Fragment.
                    filterDeal.setTargetFragment(ft, DEAL_FRAGMENT);
                    filterDeal.show(fm, "Filter Deal");
                }
            }
        });
        return root;
    }

    // Called after view is created
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Instantiate Adapter for deals
        dealsAdapter = new DealsAdapter(this.getContext());
        // Extract deals from Database
        extractDeals();
        // Set Deals Recycler View to a single column
        dealsRecycler = view.findViewById(R.id.recycler_view_deals);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        // Setting layout for recycler view
        dealsRecycler.setLayoutManager(gridLayoutManager);
        // Setting Adapter for recycler view
        dealsRecycler.setAdapter(dealsAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Called from FilterDeal Dialog Fragment to filter deals
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String brand = "";
        String cuisine = "";
        String location = "";

        if (requestCode == DEAL_FRAGMENT) {
            if (resultCode == FilterDeal.DEAL_FILTER) {
                brand = data.getExtras().getString("brand");
                cuisine = data.getExtras().getString("cuisine");
                location = data.getExtras().getString("location");
                filterDeals(brand, cuisine, location);
            } else if (resultCode == FilterDeal.RESET_DEAL_FILTER) {
                filterDeals(brand, cuisine, location);
            }
        }
    }

    // To filter deals and set filtered deals in Adapter
    private void filterDeals(String brand, String cuisine, String location) {
        ArrayList<Deal> filteredDeals = new ArrayList<Deal>();
        for (Deal deal : deals) {
            if (deal.getBrand().contains(brand) && deal.getCuisine().contains(cuisine) && deal.getLocations().contains(location)) {
                filteredDeals.add(deal);
            }
        }
        dealsAdapter.setDeals(filteredDeals);
    }

    // To extract deals from firebase
    // Note: Async Task
    private void extractDeals() {

        Double currentTime = TimeConverter.getSGTUnixTS();
        // Query for only non-expired deals
        Query query = FirebaseDatabase.getInstance().getReference().child("deal_messages").orderByChild("end_date").startAt(currentTime);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                deals = new ArrayList<>();

                byte[] image;
                String text;
                String brand;
                String cuisine;
                long start_date;
                long end_date;
                String locations;

                Object dummy;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get deal image (should not be empty)
                    dummy = snapshot.child("image").getValue();
                    image = ImageConverter.convertStringtoASCII(dummy.toString());
                    // Get deal text (should not be empty)
                    text = snapshot.child("text").getValue().toString();
                    // Get deal brand (should not be empty)
                    brand = snapshot.child("brand").getValue().toString();
                    // Get deal cuisine
                    dummy = snapshot.child("cuisines").getValue();
                    if (dummy != null) {
                        cuisine = dummy.toString();
                    } else {
                        cuisine = "";
                    }
                    // Get deal start date
                    start_date = snapshot.child("start_date").getValue(Long.class);
                    // Get deal end date
                    end_date = snapshot.child("end_date").getValue(Long.class);
                    // Get deal locations
                    dummy = snapshot.child("locations").getValue();
                    if (dummy != null) {
                        locations = dummy.toString();
                    } else {
                        locations = "";
                    }

                    deals.add(new Deal(image, text, brand, cuisine, start_date, end_date, locations));
                }
                dealsAdapter.setDeals(deals);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

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